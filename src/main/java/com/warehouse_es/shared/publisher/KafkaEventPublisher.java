package com.warehouse_es.shared.publisher;

import com.warehouse_es.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    @Qualifier("kafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.event-topic:Stock}")
    private String topic;

    /**
     * Sends an event payload to Kafka.
     * aggregateId Used as Kafka Key to guarantee message ordering in the same partition.
     * eventPayload The actual Event object (will be serialized to JSON).
     */
    public void publish(DomainEvent event) {
        String kafkaKey = event.aggregateId();
        log.debug("Publishing event to topic: {} with key: {}", topic, kafkaKey);
        kafkaTemplate.send(topic, kafkaKey, event);
    }
}
