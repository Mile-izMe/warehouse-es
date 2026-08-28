package com.warehouse_kyoei.inventory.presentation.dto.response;

import java.time.Instant;

public record StockLotResponse(
        String lotNumber,
        int quantity,
        Instant receivedAt
) {}

