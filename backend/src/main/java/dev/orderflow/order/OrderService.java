package dev.orderflow.order;

import dev.orderflow.catalog.Product;
import dev.orderflow.catalog.ProductRepository;
import dev.orderflow.common.ConflictException;
import dev.orderflow.common.ResourceNotFoundException;
import dev.orderflow.events.OutboxService;
import dev.orderflow.operations.OrderMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OutboxService outboxService;
    private final InventoryService inventoryService;
    private final OrderMetrics orderMetrics;
    private final Clock clock;

    @Autowired
    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            OutboxService outboxService,
            InventoryService inventoryService,
            OrderMetrics orderMetrics
    ) {
        this(orderRepository, productRepository, idempotencyRecordRepository,
                outboxService, inventoryService, orderMetrics, Clock.systemUTC());
    }

    OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            OutboxService outboxService,
            InventoryService inventoryService,
            OrderMetrics orderMetrics,
            Clock clock
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.outboxService = outboxService;
        this.inventoryService = inventoryService;
        this.orderMetrics = orderMetrics;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse checkout(String userId, String idempotencyKey, CheckoutRequest request) {
        var existing = idempotencyRecordRepository
                .findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            return OrderResponse.from(existing.get().getOrder());
        }

        Map<UUID, Integer> quantities = mergeQuantities(request.items());
        List<Product> products = productRepository.findAllById(quantities.keySet());
        if (products.size() != quantities.size()) {
            throw new ResourceNotFoundException("One or more products no longer exist.");
        }

        Map<UUID, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order(nextOrderNumber(), userId, request.paymentMethod());
        quantities.forEach((productId, quantity) -> {
            Product product = productsById.get(productId);
            order.addItem(
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    product.getPrice(),
                    quantity
            );
        });

        orderRepository.save(order);
        idempotencyRecordRepository.save(new IdempotencyRecord(userId, idempotencyKey, order));
        outboxService.appendOrderPlaced(order.getId());
        orderMetrics.orderCreated();
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String userId, UUID orderId) {
        return OrderResponse.from(findOwnedOrder(userId, orderId));
    }

    @Transactional
    public OrderResponse cancel(String userId, UUID orderId) {
        Order order = findOwnedOrder(userId, orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.getItems().forEach(item ->
                    inventoryService.release(item.getProductId(), item.getQuantity()));
        } else if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Order " + order.getOrderNumber() + " can no longer be cancelled.");
        }

        order.transitionTo(
                OrderStatus.CANCELLED,
                "Order cancelled",
                "The order was cancelled and any reserved inventory was released."
        );
        return OrderResponse.from(order);
    }

    private Order findOwnedOrder(String userId, UUID orderId) {
        return orderRepository.findDetailedByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found."));
    }

    private Map<UUID, Integer> mergeQuantities(List<CheckoutRequest.CheckoutItem> items) {
        Map<UUID, Integer> quantities = new LinkedHashMap<>();
        for (CheckoutRequest.CheckoutItem item : items) {
            int merged = quantities.getOrDefault(item.productId(), 0) + item.quantity();
            if (merged > 10) {
                throw new ConflictException("A checkout can contain at most 10 units of one product.");
            }
            quantities.put(item.productId(), merged);
        }
        return quantities;
    }

    private String nextOrderNumber() {
        String date = LocalDate.now(clock.withZone(ZoneOffset.UTC)).format(ORDER_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "OF-" + date + "-" + suffix;
    }
}
