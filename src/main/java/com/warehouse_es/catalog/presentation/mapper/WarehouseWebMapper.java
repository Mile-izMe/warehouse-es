package com.warehouse_es.catalog.presentation.mapper;

import com.warehouse_es.catalog.domain.warehouse.Warehouse;
import com.warehouse_es.catalog.presentation.dto.response.WarehouseResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseWebMapper {

    // Map Domain Model -> Response DTO
    WarehouseResponse toResponse(Warehouse warehouse);
}