package dev.orderflow.events;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    DEAD_LETTER
}
