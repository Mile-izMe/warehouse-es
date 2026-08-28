package com.warehouse_kyoei.inventory.presentation.dto.response;

import java.time.LocalDate;

public record StockDailyMovementResponse(
        LocalDate date,
        int totalReceived,
        int totalPicked,
        int totalAdjusted
) {}