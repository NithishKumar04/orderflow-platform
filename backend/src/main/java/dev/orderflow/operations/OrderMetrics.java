package dev.orderflow.operations;

import dev.orderflow.order.OrderStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter outboxPublished;
    private final Counter outboxPublicationFailures;
    private final Counter outboxDeadLetters;
    private final Map<OrderStatus, Counter> outcomes = new EnumMap<>(OrderStatus.class);

    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = registry.counter("orderflow.orders.created");
        this.outboxPublished = registry.counter("orderflow.outbox.published");
        this.outboxPublicationFailures = registry.counter("orderflow.outbox.publication.failures");
        this.outboxDeadLetters = registry.counter("orderflow.outbox.dead.letters");
        for (OrderStatus status : OrderStatus.values()) {
            outcomes.put(
                    status,
                    Counter.builder("orderflow.orders.outcomes")
                            .tag("status", status.name().toLowerCase())
                            .register(registry)
            );
        }
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void outcome(OrderStatus status) {
        outcomes.get(status).increment();
    }

    public void outboxPublished() {
        outboxPublished.increment();
    }

    public void outboxPublicationFailed(boolean deadLettered) {
        outboxPublicationFailures.increment();
        if (deadLettered) {
            outboxDeadLetters.increment();
        }
    }
}
