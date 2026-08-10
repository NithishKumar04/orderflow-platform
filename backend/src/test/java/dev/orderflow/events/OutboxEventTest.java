package dev.orderflow.events;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void movesToDeadLetterAfterRetryBudgetIsExhausted() {
        OutboxEvent event = new OutboxEvent(UUID.randomUUID(), "order.placed.v1", "{}");

        event.recordFailure(new RuntimeException("broker unavailable"), 3);
        event.recordFailure(new RuntimeException("broker unavailable"), 3);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);

        event.recordFailure(new RuntimeException("broker unavailable"), 3);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.DEAD_LETTER);
        assertThat(event.getAttempts()).isEqualTo(3);
    }
}
