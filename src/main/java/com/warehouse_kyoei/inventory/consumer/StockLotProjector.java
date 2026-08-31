package com.warehouse_kyoei.inventory.consumer;

import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_kyoei.inventory.infrastructure.StockLotReadModelRepository;
import com.warehouse_kyoei.inventory.projection.StockLotReadModel;
import com.warehouse_kyoei.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockLotProjector {

    private final StockLotReadModelRepository lotRepo;
    private final IdempotencyHelper idempotencyHelper;

    private static final String PROJECTION_NAME = "LOT";

    @KafkaListener(
            topics = "${app.kafka.topic.stock-topic}",
            groupId = "${app.kafka.consumer.group-id-prefix}-lot"
    )
    @Transactional
    public void onEvent(DomainEvent event) {
        switch (event) {
            case StockReceived e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                StockLotReadModel lot = lotRepo
                        .findByWarehouseCodeAndSkuCodeAndLotNumber(e.warehouseCode(), e.sku(), e.lotNumber())
                        .orElseGet(() -> new StockLotReadModel(e.warehouseCode(), e.sku(), e.lotNumber(), e.occurredAt()));
                lot.addQuantity(e.quantity());
                lotRepo.save(lot);
            });

            case StockPicked e -> idempotencyHelper.execute(e.eventId(), PROJECTION_NAME, () -> {
                applyFefoDeduction(e.warehouseCode(), e.sku(), e.quantity());
            });

            default -> {}
        }
    }

    private void applyFefoDeduction(String warehouseCode, String skuCode, int qtyToDeduct) {
        List<StockLotReadModel> lots = lotRepo
                .findByWarehouseCodeAndSkuCodeOrderByReceivedAtAsc(warehouseCode, skuCode);

        int remaining = qtyToDeduct;
        List<StockLotReadModel> modifiedLots = new ArrayList<>();

        for (StockLotReadModel lot : lots) {
            if (remaining <= 0) break;
            if (lot.getQuantity() <= 0) continue;

            int deducted = lot.deduct(remaining);
            remaining -= deducted;
            modifiedLots.add(lot);
        }

        if (!modifiedLots.isEmpty()) {
            lotRepo.saveAll(modifiedLots);
        }

        if (remaining > 0) {
            log.warn("[FEFO] Lot breakdown missing {} unit for {}:{} — maybe due to previous StockAdjusted not sync at lot.",
                    remaining, warehouseCode, skuCode);
        }
    }

}
