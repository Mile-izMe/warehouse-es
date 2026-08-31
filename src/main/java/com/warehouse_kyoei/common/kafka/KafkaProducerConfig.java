package com.warehouse_kyoei.common.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * ProducerFactory<K, V extends @Nullable Object>
 * Type Parameters: K - the key type. V - the value type.
 */
@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;


    // NOTE: This factory creates configuration for a Producer that only sends plain Strings.
    // It uses StringSerializer for both Key and Value.
    @Bean(name = "producerFactoryForString")
    public ProducerFactory<String, String> producerFactoryForString() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress
        );
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        configProps.put(
                ProducerConfig.ACKS_CONFIG,
                "1"
        );
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // NOTE: This template uses the String factory above.
    // You inject this KafkaTemplate into your services when you want to send String messages.
    @Bean(name = "kafkaTemplateForString")
    public KafkaTemplate<String, String> kafkaTemplateForString() {
        return new KafkaTemplate<>(producerFactoryForString());
    }

    // NOTE: This factory creates configuration for a Producer that sends Objects (POJOs).
    // It serializes the Java Object into a JSON string before sending it to Kafka.
    @Bean(name = "producerFactory")
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress
        );
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );
        configProps.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );
        configProps.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(configProps);

        // NOTE: If you enable transactions, every kafkaTemplate.send() must be executed within a @Transactional method
        // or using kafkaTemplate.executeInTransaction(). Otherwise, it will throw an IllegalStateException.
        // factory.setTransactionIdPrefix("tx-");

        return factory;
    }

    @Bean(name = "kafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
