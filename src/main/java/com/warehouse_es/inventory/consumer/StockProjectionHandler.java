package com.warehouse_es.inventory.consumer;

import com.warehouse_es.inventory.domain.event.StockEvents.StockAdjusted;
import com.warehouse_es.inventory.domain.event.StockEvents.StockPicked;
import com.warehouse_es.inventory.domain.event.StockEvents.StockReceived;
import com.warehouse_es.inventory.infrastructure.StockDailyMovementReadModelRepository;
import com.warehouse_es.inventory.infrastructure.StockLotReadModelRepository;
import com.warehouse_es.inventory.infrastructure.StockSummaryReadModelRepository;
import com.warehouse_es.inventory.projection.StockDailyMovementReadModel;
import com.warehouse_es.inventory.projection.StockLotReadModel;
import com.warehouse_es.inventory.projection.StockSummaryReadModel;
import com.warehouse_es.shared.processedEvent.ProcessedEvent;
import com.warehouse_es.shared.processedEvent.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockProjectionHandler {

    private final StockSummaryReadModelRepository summaryRepo;
    private final StockLotReadModelRepository lotRepo;
    private final StockDailyMovementReadModelRepository dailyRepo;
    private final ProcessedEventRepository processedEventRepo;

    public void handleStockReceived(StockReceived event) {

         if (processedEventRepo.existsById(event.eventId())) {
             log.info("Event {} đã được xử lý. Bỏ qua để tránh duplicate.", event.eventId());
             return;
         }

        // Summary: Plus to quantity
        StockSummaryReadModel summary = summaryRepo
                .findByWarehouseCodeAndSkuCode(event.warehouseCode(), event.sku())
                .orElseGet(() -> new StockSummaryReadModel(event.warehouseCode(), event.sku()));
        summary.applyDelta(event.quantity(), event.occurredAt());
        summaryRepo.save(summary);

        // Lot: Plus to lot
        StockLotReadModel lot = lotRepo
                .findByWarehouseCodeAndSkuCodeAndLotNumber(event.warehouseCode(), event.sku(), event.lotNumber())
                .orElseGet(() -> new StockLotReadModel(
                        event.warehouseCode(), event.sku(), event.lotNumber(), event.occurredAt()));
        lot.addQuantity(event.quantity());
        lotRepo.save(lot);

        // Daily movement: Plus to right date
        dailyMovementFor(event.warehouseCode(), event.sku(), event.occurredAt())
                .ifPresentOrElse(
                        d -> { d.addReceived(event.quantity()); dailyRepo.save(d); },
                        () -> {
                            var d = new StockDailyMovementReadModel(
                                    event.warehouseCode(), event.sku(), dateOf(event.occurredAt()));
                            d.addReceived(event.quantity());
                            dailyRepo.save(d);
                        });

        processedEventRepo.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    public void handleStockPicked(StockPicked event) {

        if (processedEventRepo.existsById(event.eventId())) {
            log.info("Event {} đã được xử lý. Bỏ qua để tránh duplicate.", event.eventId());
            return;
        }

        // Summary: minus quantity
        summaryRepo.findByWarehouseCodeAndSkuCode(event.warehouseCode(), event.sku())
                .ifPresent(summary -> {
                    summary.applyDelta(-event.quantity(), event.occurredAt());
                    summaryRepo.save(summary);
                });

        // Lot: minus follow FEFO (First-Expiry-First-Out ~ "nhập trước xuất trước"
        applyFefoDeduction(event.warehouseCode(), event.sku(), event.quantity());

        // Daily movement
        dailyMovementFor(event.warehouseCode(), event.sku(), event.occurredAt())
                .ifPresentOrElse(
                        d -> { d.addPicked(event.quantity()); dailyRepo.save(d); },
                        () -> {
                            var d = new StockDailyMovementReadModel(
                                    event.warehouseCode(), event.sku(), dateOf(event.occurredAt()));
                            d.addPicked(event.quantity());
                            dailyRepo.save(d);
                        });

        processedEventRepo.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    public void handleStockAdjusted(StockAdjusted event) {

        if (processedEventRepo.existsById(event.eventId())) {
            log.info("Event {} đã được xử lý. Bỏ qua để tránh duplicate.", event.eventId());
            return;
        }

        // Summary: plus/minus by delta
        StockSummaryReadModel summary = summaryRepo
                .findByWarehouseCodeAndSkuCode(event.warehouseCode(), event.sku())
                .orElseGet(() -> new StockSummaryReadModel(event.warehouseCode(), event.sku()));
        summary.applyDelta(event.delta(), event.occurredAt());
        summaryRepo.save(summary);

        // Daily movement
        dailyMovementFor(event.warehouseCode(), event.sku(), event.occurredAt())
                .ifPresentOrElse(
                        d -> { d.addAdjusted(event.delta()); dailyRepo.save(d); },
                        () -> {
                            var d = new StockDailyMovementReadModel(
                                    event.warehouseCode(), event.sku(), dateOf(event.occurredAt()));
                            d.addAdjusted(event.delta());
                            dailyRepo.save(d);
                        });

        processedEventRepo.save(new ProcessedEvent(event.eventId(), Instant.now()));
    }

    // ================ HELPERS =====================

    /**
     * FEFO: trừ dần từ lot có received_at CŨ NHẤT trước, sang lot mới hơn nếu chưa đủ.
     * Đây là logic THUẦN TÚY CHO READ MODEL/BÁO CÁO — không phải business rule bắt buộc
     * của domain (domain chỉ validate tổng số lượng đủ, không quan tâm lot).
     */
    private void applyFefoDeduction(String warehouseCode, String skuCode, int qtyToDeduct) {
        List<StockLotReadModel> lots = lotRepo
                .findByWarehouseCodeAndSkuCodeOrderByReceivedAtAsc(warehouseCode, skuCode);

        int remaining = qtyToDeduct;
        for (StockLotReadModel lot : lots) {
            if (remaining <= 0) break;
            if (lot.getQuantity() <= 0) continue;

            int deducted = lot.deduct(remaining);
            remaining -= deducted;
            lotRepo.save(lot);
        }
        // Nếu remaining > 0 sau khi duyệt hết lot: nghĩa là dữ liệu lot đang lệch so với
        // summary (vd do StockAdjusted đã tăng summary nhưng không tăng lot tương ứng).
        // Đây là tín hiệu nên log cảnh báo — không throw exception vì đây chỉ là read model,
        // không được phép làm hỏng luồng ghi event chính (write side đã thành công rồi).
        if (remaining > 0) {
            System.err.printf(
                    "[StockProjectionHandler] CẢNH BÁO: lot breakdown thiếu %d đơn vị cho %s:%s — " +
                            "có thể do StockAdjusted trước đó không đồng bộ ở mức lot.%n",
                    remaining, warehouseCode, skuCode);
        }
    }

    private Optional<StockDailyMovementReadModel> dailyMovementFor(
            String warehouseCode, String skuCode, Instant occurredAt
    ) {
        return dailyRepo.findByWarehouseCodeAndSkuCodeAndMovementDate(
                warehouseCode, skuCode, dateOf(occurredAt)
        );
    }

    private LocalDate dateOf(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
