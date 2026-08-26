package com.warehouse_es.inventory.projection;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "stock_lot_read_model",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_code", "sku_code", "lot_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLotReadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StockLotReadModel(String warehouseCode, String skuCode, String lotNumber, Instant receivedAt) {
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.lotNumber = lotNumber;
        this.receivedAt = receivedAt;
        this.quantity = 0;
    }

    public void addQuantity(int qty) {
        this.quantity += qty;
        this.updatedAt = Instant.now();
    }

    /** Minus maximum "qty" from this lot, return ACTUAL quantity has been minus */
    public int deduct(int qty) {
        int actual = Math.min(this.quantity, qty);
        this.quantity -= actual;
        this.updatedAt = Instant.now();
        return actual;
    }
}
