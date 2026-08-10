package dev.orderflow.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "orderflow.events.transport", havingValue = "kafka")
public class KafkaEventTransport implements EventTransport {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaEventTransport(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${orderflow.events.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload())
                    .get(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            throw new EventPublicationException("Kafka did not acknowledge event " + event.getId(), exception);
        }
    }
}
