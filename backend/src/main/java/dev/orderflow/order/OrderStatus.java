package dev.orderflow.order;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_CONFIRMED,
    CONFIRMED,
    REJECTED_OUT_OF_STOCK,
    PAYMENT_FAILED,
    PROCESSING_FAILED,
    CANCELLED
}
