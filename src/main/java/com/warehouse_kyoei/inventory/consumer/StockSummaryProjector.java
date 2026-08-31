package com.warehouse_kyoei.inventory.consumer;

import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockAdjusted;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_kyoei.inventory.infrastructure.StockSummaryReadModelRepository;
import com.warehouse_kyoei.inventory.projection.StockSummaryReadModel;
import com.warehouse_kyoei.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockSummaryProjector {

    private final StockSummaryReadModelRepository summaryRepo;
    private final IdempotencyHelper idempotencyHelper;

    private static final String PROJECTION_NAME = "SUMMARY";

    @KafkaListener(
            topics = "${app.kafka.topic.stock-topic}",
            groupId = "${app.kafka.consumer.group-id-prefix}-summary"
    )
    @Transactional
    public void onEvent(DomainEvent event) {
        switch (event) {
            case StockReceived e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                StockSummaryReadModel summary = summaryRepo
                        .findByWarehouseCodeAndSkuCode(e.warehouseCode(), e.sku())
                        .orElseGet(() -> new StockSummaryReadModel(e.warehouseCode(), e.sku()));
                summary.applyDelta(e.quantity(), e.occurredAt());
                summaryRepo.save(summary);
            });

            case StockPicked e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                summaryRepo.findByWarehouseCodeAndSkuCode(e.warehouseCode(), e.sku())
                        .ifPresent(summary -> {
                            summary.applyDelta(-e.quantity(), e.occurredAt());
                            summaryRepo.save(summary);
                        });
            });

            case StockAdjusted e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                StockSummaryReadModel summary = summaryRepo
                        .findByWarehouseCodeAndSkuCode(e.warehouseCode(), e.sku())
                        .orElseGet(() -> new StockSummaryReadModel(e.warehouseCode(), e.sku()));
                summary.applyDelta(e.delta(), e.occurredAt());
                summaryRepo.save(summary);
            });
            default -> {}
        }
    }
}
