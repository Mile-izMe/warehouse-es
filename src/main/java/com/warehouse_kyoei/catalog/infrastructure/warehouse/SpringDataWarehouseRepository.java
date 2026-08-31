package com.warehouse_kyoei.catalog.infrastructure.warehouse;

import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {

    Optional<WarehouseEntity> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCodeAndStatus(String warehouseCode, WarehouseStatus status);

    List<WarehouseEntity> findAllByStatus(WarehouseStatus status);
}