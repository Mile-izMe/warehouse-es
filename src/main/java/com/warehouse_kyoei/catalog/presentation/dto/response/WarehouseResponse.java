package com.warehouse_kyoei.catalog.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WarehouseResponse {
    private UUID id;
    private String warehouseCode;
    private String name;
    private String address;
    private String status;
}