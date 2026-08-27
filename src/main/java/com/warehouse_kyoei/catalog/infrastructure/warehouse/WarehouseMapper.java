package com.warehouse_kyoei.catalog.infrastructure.warehouse;

import com.warehouse_kyoei.catalog.domain.warehouse.Warehouse;
import com.warehouse_kyoei.common.mapper.DomainMapper;
import org.mapstruct.Mapper;

@Mapper (componentModel = "spring")
public interface WarehouseMapper extends DomainMapper<Warehouse, WarehouseEntity> {
}
