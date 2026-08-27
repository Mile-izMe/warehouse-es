package com.warehouse_es.inventory.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSnapshot {
    private String warehouseCode;
    private String skuCode;
    private int quantity;
}