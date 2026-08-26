package com.warehouse_es.inventory.projection;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stock_daily_movement_read_model",
        uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_code", "sku_code", "movement_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDailyMovementReadModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "total_received", nullable = false)
    private int totalReceived = 0;

    @Column(name = "total_picked", nullable = false)
    private int totalPicked = 0;

    @Column(name = "total_adjusted", nullable = false)
    private int totalAdjusted = 0;

    public StockDailyMovementReadModel(String warehouseCode, String skuCode, LocalDate movementDate) {
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.movementDate = movementDate;
    }

    public void addReceived(int qty) { this.totalReceived += qty; }
    public void addPicked(int qty) { this.totalPicked += qty; }
    public void addAdjusted(int delta) { this.totalAdjusted += delta; }
}
