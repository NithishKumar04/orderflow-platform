package dev.orderflow.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        Instant createdAt,
        Instant updatedAt,
        List<Item> items,
        List<TimelineEntry> timeline
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(Item::from).toList(),
                order.getTimeline().stream().map(TimelineEntry::from).toList()
        );
    }

    public record Item(
            UUID productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        static Item from(OrderItem item) {
            return new Item(
                    item.getProductId(),
                    item.getSku(),
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getLineTotal()
            );
        }
    }

    public record TimelineEntry(
            OrderStatus status,
            String title,
            String description,
            Instant occurredAt
    ) {
        static TimelineEntry from(OrderTimelineEntry entry) {
            return new TimelineEntry(
                    entry.getStatus(),
                    entry.getTitle(),
                    entry.getDescription(),
                    entry.getOccurredAt()
            );
        }
    }
}
