package com.warehouse_es.inventory.presentation;

import com.warehouse_es.inventory.application.command.StockCommandService;
import com.warehouse_es.inventory.presentation.dto.request.StockAdjustRequest;
import com.warehouse_es.inventory.presentation.dto.request.StockPickRequest;
import com.warehouse_es.inventory.presentation.dto.request.StockReceiveRequest;
import com.warehouse_es.inventory.presentation.dto.response.StockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses/{warehouseCode}/stock")
@RequiredArgsConstructor
public class StockCommandController {

    private final StockCommandService service;

    @GetMapping("/{skuCode}")
    public StockResponse get(@PathVariable String warehouseCode, @PathVariable String skuCode) {
        return service.getCurrent(warehouseCode, skuCode);
    }

    @PostMapping("/{skuCode}/receive")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public StockResponse receive(@PathVariable String warehouseCode, @PathVariable String skuCode,
                                 @Valid @RequestBody StockReceiveRequest req) {
        return service.receive(warehouseCode, skuCode, req);
    }

    @PostMapping("/{skuCode}/pick")
    public StockResponse pick(@PathVariable String warehouseCode, @PathVariable String skuCode,
                              @Valid @RequestBody StockPickRequest req) {
        return service.pick(warehouseCode, skuCode, req);
    }

    @PostMapping("/{skuCode}/adjust")
    public StockResponse adjust(@PathVariable String warehouseCode, @PathVariable String skuCode,
                                @Valid @RequestBody StockAdjustRequest req) {
        return service.adjust(warehouseCode, skuCode, req);
    }
}
