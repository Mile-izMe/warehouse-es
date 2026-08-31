package com.warehouse_kyoei.catalog.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {
    private UUID id;
    private String sku;
    private String name;
    private String description;
    private String unit;

    private BigDecimal price;
    private Integer stockQuantity;
    private String categoryName;
    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;
}
