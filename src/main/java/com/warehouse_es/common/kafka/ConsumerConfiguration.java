package com.warehouse_es.common.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.consumer")
@Data
public class ConsumerConfiguration {
    private String groupIdPrefix; // Automatically map with 'group-id-prefix' in application.yaml
    private int maxFailure;
}
