package com.warehouse_kyoei.inventory.consumer;

import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockAdjusted;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_kyoei.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_kyoei.inventory.infrastructure.StockDailyMovementReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockLotReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockSummaryReadModelRepository;
import com.warehouse_kyoei.inventory.projection.StockDailyMovementReadModel;
import com.warehouse_kyoei.inventory.projection.StockLotReadModel;
import com.warehouse_kyoei.inventory.projection.StockSummaryReadModel;
import com.warehouse_kyoei.shared.processedEvent.ProcessedEvent;
import com.warehouse_kyoei.shared.processedEvent.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockProjectionHandler {

    private final StockSummaryReadModelRepository summaryRepo;
    private final StockLotReadModelRepository lotRepo;
    private final StockDailyMovementReadModelRepository dailyRepo;
    private final ProcessedEventRepository processedEventRepo;

    @Transactional
    public void handleStockReceived(StockReceived event) {
        withIdempotency(event.eventId(), () -> {
            applySummaryDelta(event.warehouseCode(), event.sku(), event.quantity(), event.occurredAt());
            upsertLot(event.warehouseCode(), event.sku(), event.lotNumber(), event.quantity(), event.occurredAt());
            upsertDailyMovement(event.warehouseCode(), event.sku(), event.occurredAt(), d -> d.addReceived(event.quantity()));
        });
    }

    @Transactional
    public void handleStockPicked(StockPicked event) {
        withIdempotency(event.eventId(), () -> {
            applySummaryDelta(event.warehouseCode(), event.sku(), -event.quantity(), event.occurredAt());
            applyFefoDeduction(event.warehouseCode(), event.sku(), event.quantity());
            upsertDailyMovement(event.warehouseCode(), event.sku(), event.occurredAt(), d -> d.addPicked(event.quantity()));
        });
    }

    @Transactional
    public void handleStockAdjusted(StockAdjusted event) {
        withIdempotency(event.eventId(), () -> {
            applySummaryDelta(event.warehouseCode(), event.sku(), event.delta(), event.occurredAt());
            upsertDailyMovement(event.warehouseCode(), event.sku(), event.occurredAt(), d -> d.addAdjusted(event.delta()));
        });
    }

    // ==================== IDEMPOTENCY WRAPPER ====================
    /**
     * Wrapper check duplicate & save processed event.
     * Business logic code execute by passing through Runnable.
     */
    private void withIdempotency(UUID eventId, Runnable businessLogic) {
        if (processedEventRepo.existsById(eventId)) {
            log.info("Event {} đã được xử lý. Bỏ qua để tránh duplicate.", eventId);
            return;
        }

        businessLogic.run();

        processedEventRepo.save(new ProcessedEvent(eventId, Instant.now()));
    }

    // ==================== SUMMARY ====================
    private void applySummaryDelta(String wh, String sku, int delta, Instant occurredAt) {
        StockSummaryReadModel summary = summaryRepo
                .findByWarehouseCodeAndSkuCode(wh, sku)
                .orElseGet(() -> new StockSummaryReadModel(wh, sku));
        summary.applyDelta(delta, occurredAt);
        summaryRepo.save(summary);
    }

    // ==================== LOT (FEFO) ====================
    private void upsertLot(String wh, String sku, String lotNumber, int qty, Instant receivedAt) {
        StockLotReadModel lot = lotRepo
                .findByWarehouseCodeAndSkuCodeAndLotNumber(wh, sku, lotNumber)
                .orElseGet(() -> new StockLotReadModel(wh, sku, lotNumber, receivedAt));
        lot.addQuantity(qty);
        lotRepo.save(lot);
    }


    /**
     * FEFO: trừ dần từ lot có received_at CŨ NHẤT trước, sang lot mới hơn nếu chưa đủ.
     * Đây là logic THUẦN TÚY CHO READ MODEL/BÁO CÁO — không phải business rule bắt buộc
     * của domain (domain chỉ validate tổng số lượng đủ, không quan tâm lot).
     */
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

        // Batch update
        if (!modifiedLots.isEmpty()) {
            lotRepo.saveAll(modifiedLots);
        }

        // Nếu remaining > 0 sau khi duyệt hết lot: nghĩa là dữ liệu lot đang lệch so với
        // summary (vd do StockAdjusted đã tăng summary nhưng không tăng lot tương ứng).
        if (remaining > 0) {
            log.warn(
                    "[FEFO] Lot breakdown missing {} unit for {}:{} — " +
                            "maybe due to previous StockAdjusted not sync at lot.",
                    remaining, warehouseCode, skuCode
            );
        }
    }

    // ==================== DAILY MOVEMENT ====================
    private void upsertDailyMovement(String wh, String sku, Instant occurredAt,
                                     Consumer<StockDailyMovementReadModel> mutator) {
        LocalDate date = dateOf(occurredAt);

        StockDailyMovementReadModel daily = dailyRepo
                .findByWarehouseCodeAndSkuCodeAndMovementDate(wh, sku, date)
                .orElseGet(() -> new StockDailyMovementReadModel(wh, sku, date));

        mutator.accept(daily);
        dailyRepo.save(daily);
    }
    private LocalDate dateOf(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
