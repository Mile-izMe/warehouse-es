package com.warehouse_es.inventory.presentation.dto;

import lombok.Builder;

@Builder
public record StockResponse (
        String warehouseCode,
        String skuCode,
        int quantity,
        long version
) {
}
