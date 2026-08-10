package dev.orderflow.order;

import dev.orderflow.common.ConflictException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "user_id", nullable = false, length = 200)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 40)
    private PaymentMethod paymentMethod;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC")
    private List<OrderTimelineEntry> timeline = new ArrayList<>();

    protected Order() {
    }

    public Order(String orderNumber, String userId, PaymentMethod paymentMethod) {
        this.id = UUID.randomUUID();
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        addTimeline(
                OrderStatus.PENDING,
                "Order received",
                "The order was accepted and queued for asynchronous processing."
        );
    }

    public void addItem(UUID productId, String sku, String name, BigDecimal unitPrice, int quantity) {
        items.add(new OrderItem(this, productId, sku, name, unitPrice, quantity));
        totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public void transitionTo(OrderStatus nextStatus, String title, String description) {
        if (!OrderStateMachine.canTransition(status, nextStatus)) {
            throw new ConflictException("Cannot transition order from " + status + " to " + nextStatus + ".");
        }
        this.status = nextStatus;
        this.updatedAt = Instant.now();
        addTimeline(nextStatus, title, description);
    }

    private void addTimeline(OrderStatus status, String title, String description) {
        timeline.add(new OrderTimelineEntry(this, status, title, description));
    }

    public UUID getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<OrderTimelineEntry> getTimeline() {
        return Collections.unmodifiableList(timeline);
    }
}
