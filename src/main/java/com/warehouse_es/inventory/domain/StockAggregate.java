package com.warehouse_es.inventory.domain;


import com.warehouse_es.common.exception.ErrorCode;
import com.warehouse_es.common.exception.WarehouseException;
import com.warehouse_es.inventory.domain.event.StockEvents;
import com.warehouse_es.shared.event.DomainEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
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
@RequiredArgsConstructor
public class StockAggregate {

    // aggregateId = "warehouseCode:skuCode", e.g "WH01:SKU001"
    // -> inventory is calculated for each storage, for later Saga transfer.
    private final String warehouseCode;
    private final String skuCode;
    private int quantity = 0;
    private long version = 0;

    // List of NEW EVENT generated in this behavior (not save DB yet)
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    public static String aggregateId(String warehouseCode, String skuCode) {
        return warehouseCode + ":" + skuCode;
    }

    /** Recreate aggregate by replay event's history in DB */
    public static StockAggregate replay(String warehouseCode, String skuCode, List<DomainEvent> history) {
        StockAggregate aggregate = new StockAggregate(warehouseCode, skuCode);
        for (DomainEvent event : history) {
            aggregate.apply(event);
            aggregate.version = event.aggregateVersion();
        }
        return aggregate;
    }

    // ===== BUSINESS LOGIC (business behavior) =====
    // This is where event rules are validated BEFORE the event is raised.
    // Principle: A saved event represents a fact that has already occurred;
    // events that violate rules must not be raised.

    // IMPORT
    public void receive(int qty, String lotNumber, String sourceRef) {
        if (qty <= 0) throw new WarehouseException(ErrorCode.IMPORT_QUANTITY_INVALID);

        StockEvents.StockReceived event = StockEvents.StockReceived.builder()
                .eventId(UUID.randomUUID())
                .aggregateId(getAggregateId())
                .aggregateVersion(this.version + 1)
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
                .aggregateId(getAggregateId())
                .aggregateVersion(this.version + 1)
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
                .aggregateId(getAggregateId())
                .aggregateVersion(this.version + 1)
                .warehouseCode(this.warehouseCode)
                .sku(this.skuCode)
                .delta(delta)
                .reason(reason)
                .performedBy(performedBy)
                .occurredAt(Instant.now())
                .build();

        raise(event);
    }

    // ===== APPLY: how event change the state in RAM =====
    // This func NOT CONTAIN validate rule — applied only fact that had occurred.
    private void apply(DomainEvent event) {
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

    private void raise(DomainEvent event) {
        apply(event);
        this.version++; // in RAM
        uncommittedEvents.add(event);
    }

    public List<DomainEvent> pullUncommittedEvents() {
        List<DomainEvent> copy = List.copyOf(uncommittedEvents);
        uncommittedEvents.clear();
        return copy;
    }

    public String getAggregateId() {
        return aggregateId(warehouseCode, skuCode);
    }
}
