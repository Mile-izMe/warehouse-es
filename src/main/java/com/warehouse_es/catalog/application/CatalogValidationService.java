package com.warehouse_es.catalog.application;

import com.warehouse_es.catalog.domain.product.ProductRepository;
import com.warehouse_es.catalog.domain.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogValidationService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public boolean checkActiveProductAndWarehouse(String skuCode, String warehouseCode) {
        return productRepository.existsBySkuAndStatus(skuCode, "ACTIVE")
                && warehouseRepository.existsByWarehouseCodeAndStatus(warehouseCode, "ACTIVE");
    }

    public boolean checkActiveProduct(String skuCode) {
        return productRepository.existsBySkuAndStatus(skuCode, "ACTIVE");
    }

    public boolean checkActiveWarehouse(String warehouseCode) {
        return warehouseRepository.existsByWarehouseCodeAndStatus(warehouseCode, "ACTIVE");
    }
}
