package com.warehouse_kyoei.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StockReceiveRequest(
        @Positive int quantity,

        @NotBlank(message = "Lot number cannot be empty")
        String lotNumber,

        @NotBlank(message = "Source ref cannot be empty")
        String sourceRef
) {}