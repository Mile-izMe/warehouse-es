package com.warehouse_kyoei.inventory.presentation.dto.response;

import java.time.Instant;

public record StockSummaryResponse(
        String warehouseCode,
        String skuCode,
        int quantity,
        Instant lastMovementAt
) {}