package com.warehouse_kyoei.inventory.application.command;

import com.warehouse_kyoei.catalog.application.CatalogValidationService;
import com.warehouse_kyoei.common.exception.ErrorCode;
import com.warehouse_kyoei.common.exception.WarehouseException;
import com.warehouse_kyoei.inventory.domain.StockAggregate;
import com.warehouse_kyoei.inventory.infrastructure.StockCommandRepository;
import com.warehouse_kyoei.inventory.presentation.dto.request.StockAdjustRequest;
import com.warehouse_kyoei.inventory.presentation.dto.request.StockPickRequest;
import com.warehouse_kyoei.inventory.presentation.dto.request.StockReceiveRequest;
import com.warehouse_kyoei.inventory.presentation.dto.response.StockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application Service — Orchestration of Event Sourcing:

 *   0. validate product + warehouse exists (master data, avoid entering unexisted SKU/storage)
 *   1. loadHistory(aggregateId)          -> Get all event's history from DB
 *   2. StockItem.replay(history)         -> Recreated aggregate at current state
 *   3. Call business logic               -> validate rule + generate new event (in RAM)
 *   4. eventStore.append(...)            -> write new event, attach check optimistic concurrency
 */

@Service
@RequiredArgsConstructor
public class StockCommandService {

    private final CatalogValidationService catalogValidator;
    private final StockCommandRepository repository;

    public StockResponse receive(String warehouseCode, String skuCode, StockReceiveRequest request) {
        validateReferenceData(skuCode, warehouseCode);
        StockAggregate aggregate = repository.load(warehouseCode, skuCode);
        aggregate.receive(request.quantity(), request.lotNumber(), request.sourceRef());
        return repository.persist(aggregate);
    }

    public StockResponse pick(String warehouseCode, String skuCode, StockPickRequest request) {
        validateReferenceData(skuCode, warehouseCode);
        StockAggregate aggregate = repository.load(warehouseCode, skuCode);
        aggregate.pick(request.quantity(), request.reason(), request.performedBy());
        return repository.persist(aggregate);
    }

    public StockResponse adjust(String warehouseCode, String skuCode, StockAdjustRequest request) {
        validateReferenceData(skuCode, warehouseCode);
        StockAggregate aggregate = repository.load(warehouseCode, skuCode);
        aggregate.adjust(request.delta(), request.reason(), request.performedBy());
        return repository.persist(aggregate);
    }

    // ----- HELPERS -----
    /** ONLY READ, NO WRITE — use for GET /stock/{warehouseCode}/{skuCode} */
    public StockResponse getCurrent(String warehouseCode, String skuCode) {
        StockAggregate aggregate = repository.load(warehouseCode, skuCode);
        return repository.toResponse(aggregate);
    }

    /**
     * Prevent imp/exp storage for SKU or kho not declared master data.
     */
    private void validateReferenceData(String skuCode, String warehouseCode) {
        boolean result = catalogValidator.checkActiveProductAndWarehouse(skuCode, warehouseCode);
        if (!result) {
            throw new WarehouseException(ErrorCode.PRODUCT_WAREHOUSE_INVALID);
        }
    }
}
