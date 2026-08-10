package dev.orderflow.events;

public class EventPublicationException extends RuntimeException {

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
