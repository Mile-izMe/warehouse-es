package com.warehouse_kyoei.inventory.application.query;

import com.warehouse_kyoei.inventory.application.query.StockQuery.GetStockQuery;
import com.warehouse_kyoei.inventory.infrastructure.StockDailyMovementReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockLotReadModelRepository;
import com.warehouse_kyoei.inventory.infrastructure.StockSummaryReadModelRepository;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


}
