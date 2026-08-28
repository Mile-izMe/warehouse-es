package com.warehouse_kyoei.inventory.presentation.dto.response;

import java.util.List;

public record StockLotBreakdownResponse(
        String warehouseCode,
        String skuCode,
        int totalQuantity,       // = total of all lots, match with StockSummaryResponse.quantity
        List<StockLotResponse> lots
) {}