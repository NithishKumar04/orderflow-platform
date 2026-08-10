package dev.orderflow.events;

import dev.orderflow.order.OrderWorkflowProcessor;
import dev.orderflow.operations.OrderMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "orderflow.events.relay-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxRelay {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository outboxEventRepository;
    private final EventTransport eventTransport;
    private final OrderWorkflowProcessor workflowProcessor;
    private final OrderMetrics orderMetrics;
    private final int maxAttempts;

    public OutboxRelay(
            OutboxEventRepository outboxEventRepository,
            EventTransport eventTransport,
            OrderWorkflowProcessor workflowProcessor,
            OrderMetrics orderMetrics,
            @Value("${orderflow.events.max-attempts}") int maxAttempts
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventTransport = eventTransport;
        this.workflowProcessor = workflowProcessor;
        this.orderMetrics = orderMetrics;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${orderflow.events.relay-delay:500}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        OutboxStatus.PENDING,
                        Instant.now()
                );

        for (OutboxEvent event : events) {
            try {
                eventTransport.publish(event);
                event.markPublished();
                orderMetrics.outboxPublished();
            } catch (RuntimeException exception) {
                event.recordFailure(exception, maxAttempts);
                orderMetrics.outboxPublicationFailed(event.getStatus() == OutboxStatus.DEAD_LETTER);
                LOGGER.warn(
                        "Event {} publication failed on attempt {}: {}",
                        event.getId(),
                        event.getAttempts(),
                        exception.getMessage()
                );
                if (event.getStatus() == OutboxStatus.DEAD_LETTER) {
                    workflowProcessor.markProcessingFailed(event.getAggregateId());
                }
            }
        }
    }
}
