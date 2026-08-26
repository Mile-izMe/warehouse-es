package com.warehouse_es.inventory.infrastructure;

import com.warehouse_es.inventory.projection.StockLotReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLotReadModelRepository extends JpaRepository<StockLotReadModel, Long> {
    List<StockLotReadModel> findByWarehouseCodeAndSkuCodeOrderByReceivedAtAsc(String warehouseCode, String skuCode);

    Optional<StockLotReadModel> findByWarehouseCodeAndSkuCodeAndLotNumber(
            String warehouseCode, String skuCode, String lotNumber);
}