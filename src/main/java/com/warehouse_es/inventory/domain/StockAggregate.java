package com.warehouse_es.inventory.domain;


import com.warehouse_es.common.exception.ErrorCode;
import com.warehouse_es.common.exception.WarehouseException;
import com.warehouse_es.inventory.domain.dto.StockSnapshot;
import com.warehouse_es.inventory.domain.event.StockEvents;
import com.warehouse_es.shared.domain.AggregateRoot;
import com.warehouse_es.shared.event.DomainEvent;
import com.warehouse_es.shared.snapshot.SnapshotSerializer;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate = where business logic concentrate for 1 SKU (Stock Keeping Unit).

 * The most different from normal entity CRUD:
 * - Aggregate NOT ALLOW to load directly from 1 row in DB.
 * - It is RECREATED by replay all event from initial (`apply` func).
 * - All business action (receive/pick/adjust) -> Generate a new event,
 *   not edit field actively.
 */
@Getter
public class StockAggregate extends AggregateRoot {

    // aggregateId = "warehouseCode:skuCode", e.g "WH01:SKU001"
    // -> inventory is calculated for each storage, for later Saga transfer.
    private final String warehouseCode;
    private final String skuCode;
    private int quantity = 0;

    // --- CONSTRUCTOR ---
    public StockAggregate(String warehouseCode, String skuCode) {
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.id = aggregateId(warehouseCode, skuCode);
    }

    public static String aggregateId(String warehouseCode, String skuCode) {
        return warehouseCode + ":" + skuCode;
    }

    /** Recreate aggregate by replay event's history in DB */
    public static StockAggregate replay(String warehouseCode, String skuCode, List<DomainEvent> history) {
        StockAggregate aggregate = new StockAggregate(warehouseCode, skuCode);
        aggregate.replay(history);
        return aggregate;
    }

    // ===== BUSINESS LOGIC (business behavior) =====
    // This is where event rules are validated BEFORE the event is raised.
    // Principle: If valid -> Build Event -> raise()

    // IMPORT
    public void receive(int qty, String lotNumber, String sourceRef) {
        if (qty <= 0) throw new WarehouseException(ErrorCode.IMPORT_QUANTITY_INVALID);

        StockEvents.StockReceived event = StockEvents.StockReceived.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(this.id)
                .aggregateVersion(getNextVersion())
                .warehouseCode(this.warehouseCode)
                .sku(this.skuCode)
                .quantity(qty)
                .lotNumber(lotNumber)
                .sourceRef(sourceRef)
                .occurredAt(Instant.now())
                .build();

        raise(event);
    }

    // EXPORT
    public void pick(int qty, String reason, String performedBy) {
        if (qty <= 0) throw new WarehouseException(ErrorCode.EXPORT_QUANTITY_INVALID);
        if (qty > quantity) {
            throw new IllegalStateException(
                    "Not enough inventory at " + warehouseCode + ": require " + qty + " but only " + this.quantity);
        }

        StockEvents.StockPicked event = StockEvents.StockPicked.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(this.id)
                .aggregateVersion(getNextVersion())
                .warehouseCode(this.warehouseCode)
                .sku(this.skuCode)
                .quantity(qty)
                .reason(reason)
                .performedBy(performedBy)
                .occurredAt(Instant.now())
                .build();

        raise(event);
    }

    // ADJUST
    public void adjust(int delta, String reason, String performedBy) {
        if (quantity + delta < 0) throw new WarehouseException(ErrorCode.ADJUSTMENT_INVALID);

        StockEvents.StockAdjusted event = StockEvents.StockAdjusted.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(this.id)
                .aggregateVersion(getNextVersion())
                .warehouseCode(this.warehouseCode)
                .sku(this.skuCode)
                .delta(delta)
                .reason(reason)
                .performedBy(performedBy)
                .occurredAt(Instant.now())
                .build();

        raise(event);
    }

    // ===== MUTATOR: update state in RAM =====
    // This func NOT CONTAIN validate rule — applied only fact that had occurred.
    @Override
    protected void mutate(DomainEvent event) {
        switch (event) {
            case StockEvents.StockReceived e -> quantity += e.quantity();
            case StockEvents.StockPicked e -> quantity -= e.quantity();
            case StockEvents.StockAdjusted e -> quantity += e.delta();
            default -> throw new WarehouseException(
                    ErrorCode.UNSUPPORTED_EVENT,
                    event.getClass().getSimpleName()
            );
        }
    }

    @Override
    public String createSnapshotPayload(SnapshotSerializer serializer) {
        StockSnapshot snapshot = new StockSnapshot(this.warehouseCode, this.skuCode, this.quantity);
        return serializer.serialize(snapshot);
    }

    @Override
    public void restoreFromSnapshot(String payload, long version, SnapshotSerializer serializer) {
        StockSnapshot snapshot = serializer.deserialize(payload, StockSnapshot.class);
        this.quantity = snapshot.getQuantity();
        this.version = version;
    }
}
