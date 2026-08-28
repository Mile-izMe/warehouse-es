package com.warehouse_kyoei.inventory.application.query;

import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockDailyQuery;
import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockQuery;
import com.warehouse_kyoei.inventory.infrastructure.StockDailyMovementReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockLotReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockSummaryReadModelRepository;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockDailyMovementResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockLotBreakdownResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockLotResponse;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockQueryService {

    private final StockDailyMovementReadModelRepository stockDailyMovementRepository;
    private final StockLotReadModelRepository stockLotRepository;
    private final StockSummaryReadModelRepository stockSummaryRepository;

    @Transactional(readOnly = true)
    public StockSummaryResponse getSummary(GetStockQuery query) {
        return stockSummaryRepository.findByWarehouseCodeAndSkuCode(query.warehouseCode(), query.skuCode())
                .map(s -> new StockSummaryResponse(
                        s.getWarehouseCode(),
                        s.getSkuCode(),
                        s.getQuantity(),
                        s.getLastMovementAt()
                ))
                .orElse(new StockSummaryResponse(
                        query.warehouseCode(),
                        query.skuCode(),
                        0,
                        null
                ));
    }

    public StockLotBreakdownResponse getLotBreakdown(GetStockQuery query) {
        List<StockLotResponse> lots = stockLotRepository
                .findByWarehouseCodeAndSkuCodeOrderByReceivedAtAsc(query.warehouseCode(), query.skuCode())
                .stream()
                .filter(l -> l.getQuantity() > 0)
                .map(l -> new StockLotResponse(
                        l.getLotNumber(),
                        l.getQuantity(),
                        l.getReceivedAt()
                ))
                .toList();

        int total = lots.stream().mapToInt(StockLotResponse::quantity).sum();
        return new StockLotBreakdownResponse(query.warehouseCode(), query.skuCode(), total, lots);
    }

    public List<StockDailyMovementResponse> getDailyMovement(GetStockDailyQuery query) {
        return stockDailyMovementRepository.findByWarehouseCodeAndSkuCodeAndMovementDateBetweenOrderByMovementDateAsc(
                query.warehouseCode(), query.skuCode(), query.from(), query.to())
                .stream()
                .map(d -> new StockDailyMovementResponse(
                        d.getMovementDate(),
                        d.getTotalReceived(),
                        d.getTotalPicked(),
                        d.getTotalAdjusted()
                ))
                .toList();
    }
}
