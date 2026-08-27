package com.warehouse_kyoei.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StockPickRequest(
        @Positive int quantity,

        @NotBlank(message = "Reason cannot be empty")
        String reason,

        @NotBlank(message = "Performed by cannot be empty")
        String performedBy
) {}