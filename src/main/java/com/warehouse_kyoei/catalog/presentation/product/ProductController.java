package com.warehouse_kyoei.catalog.presentation.product;

import com.warehouse_kyoei.catalog.domain.product.Product;
import com.warehouse_kyoei.catalog.domain.product.ProductRepository;
import com.warehouse_kyoei.catalog.presentation.dto.response.ProductResponse;
import com.warehouse_kyoei.catalog.presentation.mapper.ProductWebMapper;
import com.warehouse_kyoei.common.response.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repository;
    private final ProductWebMapper webMapper;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<ProductResponse>>> getAllProducts() {
        List<Product> products = repository.findAll();
        List<ProductResponse> res = products.stream()
                .map(webMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiSuccessResponse.<List<ProductResponse>>builder()
                .message("Get list products success!")
                .data(res)
                .build());
    };

}
