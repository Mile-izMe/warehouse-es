package com.warehouse_kyoei.inventory.presentation;

import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockDailyQuery;
import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockQuery;
import com.warehouse_kyoei.inventory.application.query.StockQueryService;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockDailyMovementResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockLotBreakdownResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses/{warehouseCode}/stock/{skuCode}")
@RequiredArgsConstructor
public class StockQueryController {

    private final StockQueryService service;

    @GetMapping("/summary")
    public StockSummaryResponse summary(
            @PathVariable String warehouseCode,
            @PathVariable String skuCode) {
        GetStockQuery query = new GetStockQuery(warehouseCode, skuCode);
        return service.getSummary(query);
    }

    @GetMapping("/lots")
    public StockLotBreakdownResponse lots(
            @PathVariable String warehouseCode,
            @PathVariable String skuCode) {
        GetStockQuery query = new GetStockQuery(warehouseCode, skuCode);
        return service.getLotBreakdown(query);
    }

    @GetMapping("/daily")
    public List<StockDailyMovementResponse> daily(
            @PathVariable String warehouseCode, @PathVariable String skuCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        GetStockDailyQuery query = new GetStockDailyQuery(warehouseCode, skuCode, from, to);
        return service.getDailyMovement(query);
    }
}
