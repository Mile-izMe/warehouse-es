package com.warehouse_kyoei.catalog.presentation.mapper;

import com.warehouse_kyoei.catalog.domain.warehouse.Warehouse;
import com.warehouse_kyoei.catalog.presentation.dto.response.WarehouseResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseWebMapper {

    // Map Domain Model -> Response DTO
    WarehouseResponse toResponse(Warehouse warehouse);
}