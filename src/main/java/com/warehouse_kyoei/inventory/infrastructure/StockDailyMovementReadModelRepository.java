package com.warehouse_kyoei.inventory.infrastructure;

import com.warehouse_kyoei.inventory.projection.StockDailyMovementReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockDailyMovementReadModelRepository extends JpaRepository<StockDailyMovementReadModel, Long> {
    Optional<StockDailyMovementReadModel> findByWarehouseCodeAndSkuCodeAndMovementDate(
            String warehouseCode, String skuCode, LocalDate movementDate);

    List<StockDailyMovementReadModel> findByWarehouseCodeAndSkuCodeAndMovementDateBetweenOrderByMovementDateAsc(
            String warehouseCode, String skuCode, LocalDate from, LocalDate to);
}
