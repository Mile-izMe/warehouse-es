package com.warehouse_es.inventory.projection;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stock_summary_read_model",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_code", "sku_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryReadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "last_movement_at")
    private Instant lastMovementAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StockSummaryReadModel(String warehouseCode, String skuCode) {
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.quantity = 0;
    }


    public void applyDelta(int delta, Instant occurredAt) {
        this.quantity += delta;
        this.lastMovementAt = occurredAt;
        this.updatedAt = Instant.now();
    }
}
