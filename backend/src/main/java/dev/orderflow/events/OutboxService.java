package dev.orderflow.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxService {

    public static final String ORDER_PLACED = "order.placed.v1";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void appendOrderPlaced(UUID orderId) {
        try {
            UUID eventId = UUID.randomUUID();
            String payload = objectMapper.writeValueAsString(new OrderPlacedPayload(eventId, orderId));
            outboxEventRepository.save(new OutboxEvent(eventId, orderId, ORDER_PLACED, payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize the order event.", exception);
        }
    }

    public record OrderPlacedPayload(UUID eventId, UUID orderId) {
    }
}
