package dev.orderflow.order;

import dev.orderflow.common.ResourceNotFoundException;
import dev.orderflow.events.OrderPlacedMessage;
import dev.orderflow.events.ProcessedEvent;
import dev.orderflow.events.ProcessedEventRepository;
import dev.orderflow.operations.OrderMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderWorkflowProcessor {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryService inventoryService;
    private final PaymentGateway paymentGateway;
    private final OrderMetrics orderMetrics;

    public OrderWorkflowProcessor(
            OrderRepository orderRepository,
            ProcessedEventRepository processedEventRepository,
            InventoryService inventoryService,
            PaymentGateway paymentGateway,
            OrderMetrics orderMetrics
    ) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.inventoryService = inventoryService;
        this.paymentGateway = paymentGateway;
        this.orderMetrics = orderMetrics;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(OrderPlacedMessage message) {
        if (processedEventRepository.existsById(message.eventId())) {
            return;
        }

        Order order = orderRepository.findForProcessing(message.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order " + message.orderId() + " was not found."));

        if (order.getStatus() != OrderStatus.PENDING) {
            processedEventRepository.save(new ProcessedEvent(message.eventId()));
            return;
        }

        List<OrderItem> reserved = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            if (!inventoryService.reserve(item.getProductId(), item.getQuantity())) {
                reserved.forEach(this::release);
                order.transitionTo(
                        OrderStatus.REJECTED_OUT_OF_STOCK,
                        "Inventory unavailable",
                        "One or more items sold out before inventory could be reserved."
                );
                orderMetrics.outcome(OrderStatus.REJECTED_OUT_OF_STOCK);
                processedEventRepository.save(new ProcessedEvent(message.eventId()));
                return;
            }
            reserved.add(item);
        }

        order.transitionTo(
                OrderStatus.INVENTORY_RESERVED,
                "Inventory reserved",
                "All items were reserved with an atomic inventory update."
        );

        PaymentGateway.PaymentResult payment = paymentGateway.authorize(order.getPaymentMethod());
        if (!payment.approved()) {
            reserved.forEach(this::release);
            order.transitionTo(OrderStatus.PAYMENT_FAILED, "Payment declined", payment.message());
            orderMetrics.outcome(OrderStatus.PAYMENT_FAILED);
            processedEventRepository.save(new ProcessedEvent(message.eventId()));
            return;
        }

        order.transitionTo(OrderStatus.PAYMENT_CONFIRMED, "Payment authorized", payment.message());
        order.transitionTo(
                OrderStatus.CONFIRMED,
                "Order confirmed",
                "The order is ready for fulfillment."
        );
        orderMetrics.outcome(OrderStatus.CONFIRMED);
        processedEventRepository.save(new ProcessedEvent(message.eventId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessingFailed(UUID orderId) {
        orderRepository.findForProcessing(orderId)
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .ifPresent(order -> {
                    order.transitionTo(
                            OrderStatus.PROCESSING_FAILED,
                            "Processing paused",
                            "The event exhausted its retry budget and was moved to the dead-letter queue."
                    );
                    orderMetrics.outcome(OrderStatus.PROCESSING_FAILED);
                });
    }

    private void release(OrderItem item) {
        inventoryService.release(item.getProductId(), item.getQuantity());
    }
}
