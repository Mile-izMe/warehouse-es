package com.warehouse_kyoei.catalog.presentation.mapper;

import com.warehouse_kyoei.catalog.domain.product.Product;
import com.warehouse_kyoei.catalog.presentation.dto.response.ProductResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    // Map Domain Model -> Response DTO
    ProductResponse toResponse(Product product);
}
