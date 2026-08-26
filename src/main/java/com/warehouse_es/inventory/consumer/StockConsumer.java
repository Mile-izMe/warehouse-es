package com.warehouse_es.inventory.consumer;

import com.warehouse_es.inventory.domain.event.StockEvents.StockAdjusted;
import com.warehouse_es.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_es.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_es.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(
        topics = "${app.kafka.topic.stock-topic}",
        groupId = "${app.kafka.consumer.group-id-prefix}"
)
public class StockConsumer {

    private final StockProjectionHandler stockProjectionHandler;

    @Transactional
    public void handleStockEvents(DomainEvent event) {
        log.info("Received event {} for aggregate {}", event.eventType(), event.aggregateId());

        switch (event.eventType()) {
            case "StockReceived":
                stockProjectionHandler.handleStockReceived((StockReceived) event);
                break;
            case "StockPicked":
                 stockProjectionHandler.handleStockPicked((StockPicked) event);
                 break;
            case "StockAdjusted":
                stockProjectionHandler.handleStockAdjusted((StockAdjusted) event);
                break;
            default:
                log.warn("Passing event not supported: {}", event.eventType());
        }
    }
}
