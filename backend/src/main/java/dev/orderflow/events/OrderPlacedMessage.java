package dev.orderflow.events;

import java.util.UUID;

public record OrderPlacedMessage(UUID eventId, UUID orderId) {
}
