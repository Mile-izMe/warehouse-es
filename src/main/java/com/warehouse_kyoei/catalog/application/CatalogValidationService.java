package com.warehouse_kyoei.catalog.application;

import com.warehouse_kyoei.catalog.domain.product.ProductRepository;
import com.warehouse_kyoei.catalog.domain.product.ProductStatus;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseRepository;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogValidationService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public boolean checkActiveProductAndWarehouse(String skuCode, String warehouseCode) {
        return productRepository.existsBySkuAndStatus(skuCode, ProductStatus.ACTIVE)
                && warehouseRepository.existsByWarehouseCodeAndStatus(warehouseCode, WarehouseStatus.ACTIVE);
    }

    public boolean checkActiveProduct(String skuCode) {
        return productRepository.existsBySkuAndStatus(skuCode, ProductStatus.ACTIVE);
    }

    public boolean checkActiveWarehouse(String warehouseCode) {
        return warehouseRepository.existsByWarehouseCodeAndStatus(warehouseCode, WarehouseStatus.ACTIVE);
    }
}
