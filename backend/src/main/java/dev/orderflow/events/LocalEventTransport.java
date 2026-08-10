package dev.orderflow.events;

import dev.orderflow.order.OrderWorkflowProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "orderflow.events.transport", havingValue = "local", matchIfMissing = true)
public class LocalEventTransport implements EventTransport {

    private final OrderWorkflowProcessor workflowProcessor;

    public LocalEventTransport(OrderWorkflowProcessor workflowProcessor) {
        this.workflowProcessor = workflowProcessor;
    }

    @Override
    public void publish(OutboxEvent event) {
        if (!OutboxService.ORDER_PLACED.equals(event.getEventType())) {
            throw new IllegalArgumentException("Unsupported event type: " + event.getEventType());
        }
        workflowProcessor.process(new OrderPlacedMessage(event.getId(), event.getAggregateId()));
    }
}
