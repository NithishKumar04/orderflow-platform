package dev.orderflow.events;

public interface EventTransport {

    void publish(OutboxEvent event);
}
