package com.warehouse_kyoei.inventory.consumer;

import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockAdjusted;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_kyoei.inventory.infrastructure.StockDailyMovementReadModelRepository;
import com.warehouse_kyoei.inventory.projection.StockDailyMovementReadModel;
import com.warehouse_kyoei.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDailyProjector {

    private final StockDailyMovementReadModelRepository dailyRepo;
    private final IdempotencyHelper idempotencyHelper;

    private static final String PROJECTION_NAME = "DAILY";

    @KafkaListener(
            topics = "${app.kafka.topic.stock-topic}",
            groupId = "${app.kafka.consumer.group-id-prefix}-daily"
    )
    @Transactional
    public void onEvent(DomainEvent event) {
        switch (event) {
            case StockReceived e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                updateDaily(e.warehouseCode(), e.sku(), e.occurredAt(), e.quantity(), 0, 0);
            });

            case StockPicked e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                updateDaily(e.warehouseCode(), e.sku(), e.occurredAt(), 0, e.quantity(), 0);
            });

            case StockAdjusted e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                updateDaily(e.warehouseCode(), e.sku(), e.occurredAt(), 0, 0, e.delta());
            });
            default -> {}
        }
    }

    private void updateDaily(String warehouseCode, String skuCode, Instant occurredAt, int received, int picked, int adjusted) {
        LocalDate date = occurredAt.atZone(ZoneOffset.UTC).toLocalDate();
        StockDailyMovementReadModel d = dailyRepo.findByWarehouseCodeAndSkuCodeAndMovementDate(warehouseCode, skuCode, date)
                .orElseGet(() -> new StockDailyMovementReadModel(warehouseCode, skuCode, date));

        if (received > 0) d.addReceived(received);
        if (picked > 0) d.addPicked(picked);
        if (adjusted != 0) d.addAdjusted(adjusted);

        dailyRepo.save(d);
    }
}
