package com.warehouse_kyoei.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * ConsumerFactory and ListenerContainerFactory configuration for "custom" topic case which expects message value can
 * be converted into POJO.
 */

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Autowired
    ConsumerConfiguration consumerConfiguration;

    // NOTE: This factory tells Spring how to connect to Kafka to read messages.
    // It reads all incoming messages as plain Strings first.
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfiguration.getGroupIdPrefix());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.warehouse_kyoei.*");
        props.put(JsonDeserializer.TYPE_MAPPINGS,
                "StockReceived:com.warehouse_kyoei.inventory.domain.event.StockEvents$StockReceived," +
                        "StockPicked:com.warehouse_kyoei.inventory.domain.event.StockEvents$StockPicked," +
                        "StockAdjusted:com.warehouse_kyoei.inventory.domain.event.StockEvents$StockAdjusted");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // NOTE: This is the main container that manages @KafkaListener annotations.
    // It wires together the consumer properties, the message converter, and the error handler.
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            @Qualifier(value = "kafkaTemplate") KafkaTemplate<String, Object> template) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // NOTE: Configures what happens when message processing fails.
        // If processing fails 'maxFailure' times, the message is sent to a Dead Letter Topic (DLT) using the provided template.
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new FixedBackOff(1000L, consumerConfiguration.getMaxFailure())
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
