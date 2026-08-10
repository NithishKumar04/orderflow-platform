package dev.orderflow.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID aggregateId, String eventType, String payload) {
        this(UUID.randomUUID(), aggregateId, eventType, payload);
    }

    public OutboxEvent(UUID eventId, UUID aggregateId, String eventType, String payload) {
        this.id = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.nextAttemptAt = this.createdAt;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void recordFailure(Throwable throwable, int maxAttempts) {
        attempts++;
        lastError = abbreviate(throwable.getMessage());
        if (attempts >= maxAttempts) {
            status = OutboxStatus.DEAD_LETTER;
            return;
        }
        long delaySeconds = 1L << Math.min(attempts - 1, 5);
        nextAttemptAt = Instant.now().plus(Duration.ofSeconds(delaySeconds));
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "Event publication failed without an error message.";
        }
        return value.substring(0, Math.min(value.length(), 1000));
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }
}
