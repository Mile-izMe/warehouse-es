package com.warehouse_kyoei.inventory.presentation;

import com.warehouse_kyoei.common.response.ApiSuccessResponse;
import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockDailyQuery;
import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockQuery;
import com.warehouse_kyoei.inventory.application.query.StockQueryService;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockDailyMovementResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockLotBreakdownResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses/{warehouseCode}/stock/{skuCode}")
@RequiredArgsConstructor
public class StockQueryController {

    private final StockQueryService service;

    @GetMapping("/summary")
    public ResponseEntity<ApiSuccessResponse<StockSummaryResponse>> summary(
            @PathVariable String warehouseCode,
            @PathVariable String skuCode) {
        GetStockQuery query = new GetStockQuery(warehouseCode, skuCode);
        StockSummaryResponse response = service.getSummary(query);

        ApiSuccessResponse<StockSummaryResponse> apiResponse = ApiSuccessResponse.<StockSummaryResponse>builder()
                .message("Get stock summary success!")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/lots")
    public ResponseEntity<ApiSuccessResponse<StockLotBreakdownResponse>> lots(
            @PathVariable String warehouseCode,
            @PathVariable String skuCode) {
        GetStockQuery query = new GetStockQuery(warehouseCode, skuCode);
        StockLotBreakdownResponse response = service.getLotBreakdown(query);


        ApiSuccessResponse<StockLotBreakdownResponse> apiResponse = ApiSuccessResponse.<StockLotBreakdownResponse>builder()
                .message("Get stock lot success!")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiSuccessResponse<List<StockDailyMovementResponse>>> daily(
            @PathVariable String warehouseCode, @PathVariable String skuCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        GetStockDailyQuery query = new GetStockDailyQuery(warehouseCode, skuCode, from, to);
        List<StockDailyMovementResponse> responses = service.getDailyMovement(query);

        ApiSuccessResponse<List<StockDailyMovementResponse>> apiResponse = ApiSuccessResponse.<List<StockDailyMovementResponse>>builder()
                .message("Get list stock daily movement success!")
                .data(responses)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
