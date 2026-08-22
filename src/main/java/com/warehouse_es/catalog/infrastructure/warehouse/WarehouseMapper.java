package com.warehouse_es.catalog.infrastructure.warehouse;

import com.warehouse_es.catalog.domain.warehouse.Warehouse;
import com.warehouse_es.common.mapper.DomainMapper;
import org.mapstruct.Mapper;

@Mapper (componentModel = "spring")
public interface WarehouseMapper extends DomainMapper<Warehouse, WarehouseEntity> {
}
