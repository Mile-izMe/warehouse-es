package com.warehouse_es.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StockAdjustRequest(
        int delta, // can be negative (-)

        @NotBlank(message = "Reason cannot be empty")
        String reason,

        @NotBlank(message = "Performed by cannot be empty")
        String performedBy
) {}