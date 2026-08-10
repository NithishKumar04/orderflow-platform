package dev.orderflow.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_timeline")
public class OrderTimelineEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected OrderTimelineEntry() {
    }

    OrderTimelineEntry(Order order, OrderStatus status, String title, String description) {
        this.id = UUID.randomUUID();
        this.order = order;
        this.status = status;
        this.title = title;
        this.description = description;
        this.occurredAt = Instant.now();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
