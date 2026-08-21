package com.warehouse_es.inventory.presentation.dto;

public record StockResponse (
        String warehouseCode,
        String skuCode,
        int quantity,
        long version
) {
}
