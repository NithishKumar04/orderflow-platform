package dev.orderflow.order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(
                OrderStatus.INVENTORY_RESERVED,
                OrderStatus.REJECTED_OUT_OF_STOCK,
                OrderStatus.PROCESSING_FAILED,
                OrderStatus.CANCELLED
        ));
        TRANSITIONS.put(OrderStatus.INVENTORY_RESERVED, EnumSet.of(
                OrderStatus.PAYMENT_CONFIRMED,
                OrderStatus.PAYMENT_FAILED,
                OrderStatus.PROCESSING_FAILED,
                OrderStatus.CANCELLED
        ));
        TRANSITIONS.put(OrderStatus.PAYMENT_CONFIRMED, EnumSet.of(
                OrderStatus.CONFIRMED,
                OrderStatus.PROCESSING_FAILED
        ));
        TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.CANCELLED));
    }

    private OrderStateMachine() {
    }

    public static boolean canTransition(OrderStatus current, OrderStatus next) {
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(next);
    }
}
