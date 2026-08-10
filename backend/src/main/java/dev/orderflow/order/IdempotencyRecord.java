package dev.orderflow.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_user_key", columnNames = {"user_id", "idempotency_key"})
)
public class IdempotencyRecord {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 200)
    private String userId;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String userId, String idempotencyKey, Order order) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.order = order;
        this.createdAt = Instant.now();
    }

    public Order getOrder() {
        return order;
    }
}
