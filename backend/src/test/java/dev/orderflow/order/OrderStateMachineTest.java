package dev.orderflow.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStateMachineTest {

    @Test
    void allowsOnlyExplicitForwardTransitions() {
        assertThat(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.INVENTORY_RESERVED))
                .isTrue();
        assertThat(OrderStateMachine.canTransition(OrderStatus.INVENTORY_RESERVED, OrderStatus.PAYMENT_CONFIRMED))
                .isTrue();
        assertThat(OrderStateMachine.canTransition(OrderStatus.PAYMENT_CONFIRMED, OrderStatus.CONFIRMED))
                .isTrue();
        assertThat(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.PENDING))
                .isFalse();
        assertThat(OrderStateMachine.canTransition(OrderStatus.CANCELLED, OrderStatus.CONFIRMED))
                .isFalse();
    }

    @Test
    void allowsCompensatingAndTerminalTransitions() {
        assertThat(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.REJECTED_OUT_OF_STOCK))
                .isTrue();
        assertThat(OrderStateMachine.canTransition(OrderStatus.INVENTORY_RESERVED, OrderStatus.PAYMENT_FAILED))
                .isTrue();
        assertThat(OrderStateMachine.canTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED))
                .isTrue();
    }
}
