package com.warehouse_kyoei.catalog.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String skuCode);

    boolean existsBySkuAndStatus(String skuCode, ProductStatus status);

}
