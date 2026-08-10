package dev.orderflow.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.orderflow.order.OrderWorkflowProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "orderflow.events.transport", havingValue = "kafka")
public class KafkaOrderConsumer {

    private final ObjectMapper objectMapper;
    private final OrderWorkflowProcessor workflowProcessor;

    public KafkaOrderConsumer(ObjectMapper objectMapper, OrderWorkflowProcessor workflowProcessor) {
        this.objectMapper = objectMapper;
        this.workflowProcessor = workflowProcessor;
    }

    @KafkaListener(topics = "${orderflow.events.topic}")
    public void consume(String payload) {
        try {
            OutboxService.OrderPlacedPayload event = objectMapper.readValue(
                    payload,
                    OutboxService.OrderPlacedPayload.class
            );
            workflowProcessor.process(new OrderPlacedMessage(event.eventId(), event.orderId()));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid order event payload.", exception);
        }
    }
}
