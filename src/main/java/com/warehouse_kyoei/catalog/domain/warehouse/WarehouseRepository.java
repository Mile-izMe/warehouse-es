package com.warehouse_kyoei.catalog.domain.warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCodeAndStatus(String warehouseCode, WarehouseStatus status);

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(UUID id);

    List<Warehouse> findAllActive();

    int findNumDataInDatabase();

    List<Warehouse> saveAll(List<Warehouse> warehouses);
}
