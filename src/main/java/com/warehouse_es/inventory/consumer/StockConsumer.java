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
public class StockConsumer {

    private final StockProjectionHandler stockProjectionHandler;

    @KafkaListener(
            topics = "${app.kafka.topic.stock-topic}",
            groupId = "${app.kafka.consumer.group-id-prefix}"
    )
    @Transactional
    public void handleStockEvents(DomainEvent event) {
        log.info("Received event {} for aggregate {}", event.eventType(), event.aggregateId());

        switch (event) {
            case StockReceived e:
                stockProjectionHandler.handleStockReceived(e);
                break;
            case StockPicked e:
                 stockProjectionHandler.handleStockPicked(e);
                 break;
            case StockAdjusted e:
                stockProjectionHandler.handleStockAdjusted(e);
                break;
            default:
                log.warn("Passing event not supported: {}", event.eventType());
        }
    }
}
