package com.warehouse_kyoei.inventory.infrastructure;

import com.warehouse_kyoei.inventory.projection.StockSummaryReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockSummaryReadModelRepository extends JpaRepository<StockSummaryReadModel, Long> {
    Optional<StockSummaryReadModel> findByWarehouseCodeAndSkuCode(String warehouseCode, String skuCode);
}