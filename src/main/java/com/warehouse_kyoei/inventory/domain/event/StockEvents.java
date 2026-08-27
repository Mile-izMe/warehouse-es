package com.warehouse_kyoei.inventory.domain.event;

import com.warehouse_kyoei.shared.event.DomainEvent;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public class StockEvents {
    // -> inventory is calculated separately for EACH warehouse, not combined across the system.
    // This is a required condition for Saga transfer between warehouses to work properly.

    // 1. Warehouse Import: receive goods from the supplier
    @Builder
    public record StockReceived(
            UUID eventId,
            String aggregateId,
            long aggregateVersion,

            String warehouseCode,
            String sku,
            int quantity,
            String lotNumber,
            String sourceRef, // E.g: Import code PO#001
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String aggregateType() { return "STOCK"; }
    }

    // 2. Warehouse Export: Delivery / for products
    @Builder
    public record StockPicked(
            UUID eventId,
            String aggregateId,
            long aggregateVersion,

            String warehouseCode,
            String sku,
            int quantity,
            String reason,        // E.g: "Order#123"
            String performedBy,   // Who - audit
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String aggregateType() { return "STOCK"; }
    }

    // 3. Manual Modify: Check for the differences
    @Builder
    public record StockAdjusted(
            UUID eventId,
            String aggregateId,
            long aggregateVersion,

            String warehouseCode,
            String sku,
            int delta,            // Negative or positive
            String reason,        // E.g: "Check Q3 - mismatch due to broken"
            String performedBy,
            Instant occurredAt
    ) implements DomainEvent {
        @Override
        public String aggregateType() { return "STOCK"; }
    }
}
