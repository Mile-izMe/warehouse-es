package com.warehouse_kyoei.catalog.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WarehouseCreateRequest(
        @NotBlank(message = "Code must not be empty")
        String warehouseCode,

        @NotBlank(message = "Name must not be empty")
        String name,

        String address
) {}