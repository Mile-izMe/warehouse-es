package com.warehouse_es.inventory.presentation.dto.response;

import lombok.Builder;

@Builder
public record StockResponse (
        String warehouseCode,
        String skuCode,
        int quantity,
        long version
) {
}
