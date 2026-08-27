package com.warehouse_kyoei.inventory.presentation.dto.response;

import lombok.Builder;

@Builder
public record StockResponse (
        String warehouseCode,
        String skuCode,
        int quantity,
        long version
) {
}
