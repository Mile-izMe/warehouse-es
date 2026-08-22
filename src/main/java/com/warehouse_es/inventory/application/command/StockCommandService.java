package com.warehouse_es.inventory.application.command;

import com.warehouse_es.catalog.application.CatalogValidationService;
import com.warehouse_es.common.exception.ErrorCode;
import com.warehouse_es.common.exception.WarehouseException;
import com.warehouse_es.inventory.domain.StockAggregate;
import com.warehouse_es.inventory.presentation.dto.request.StockAdjustRequest;
import com.warehouse_es.inventory.presentation.dto.request.StockPickRequest;
import com.warehouse_es.inventory.presentation.dto.request.StockReceiveRequest;
import com.warehouse_es.inventory.presentation.dto.response.StockResponse;
import com.warehouse_es.shared.event.DomainEvent;
import com.warehouse_es.shared.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final EventStore eventStore;
    private final CatalogValidationService catalogValidator;

    public StockResponse receive(String warehouseCode, String skuCode, StockReceiveRequest request) {
        validateReferenceData(warehouseCode, skuCode);
        StockAggregate aggregate = load(warehouseCode, skuCode);
        aggregate.receive(request.quantity(), request.lotNumber(), request.sourceRef());
        return persist(aggregate);
    }

    public StockResponse pick(String warehouseCode, String skuCode, StockPickRequest request) {
        validateReferenceData(warehouseCode, skuCode);
        StockAggregate aggregate = load(warehouseCode, skuCode);
        aggregate.pick(request.quantity(), request.reason(), request.performedBy());
        return persist(aggregate);
    }

    public StockResponse adjust(String warehouseCode, String skuCode, StockAdjustRequest request) {
        validateReferenceData(warehouseCode, skuCode);
        StockAggregate aggregate = load(warehouseCode, skuCode);
        aggregate.adjust(request.delta(), request.reason(), request.performedBy());
        return persist(aggregate);
    }

    // ----- HELPERS -----
    /** ONLY READ, NO WRITE — use for GET /stock/{warehouseCode}/{skuCode} */
    public StockResponse getCurrent(String warehouseCode, String skuCode) {
        StockAggregate aggregate = load(warehouseCode, skuCode);
        return toResponse(aggregate);
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

    private StockAggregate load(String warehouseCode, String skuCode) {
        String aggregateId = StockAggregate.aggregateId(warehouseCode, skuCode);

        // Load history
        List<DomainEvent> history = eventStore.loadHistory(aggregateId);

        // Replay
        return StockAggregate.replay(warehouseCode, skuCode, history);
    }

    // DRY - Don't Repeat Yourself
    private StockResponse persist(StockAggregate aggregate) {
        List<DomainEvent> newEvents = aggregate.pullUncommittedEvents();

        // EXPECTED VERSION: Old version = Current version - nums of new events
        long expectedVersion = aggregate.getVersion() - newEvents.size();

        eventStore.append(
                aggregate.getAggregateId(),
                newEvents,
                expectedVersion
        );

        return toResponse(aggregate);
    }

    private StockResponse toResponse(StockAggregate aggregate) {
        return StockResponse.builder()
                .warehouseCode(aggregate.getWarehouseCode())
                .skuCode(aggregate.getSkuCode())
                .quantity(aggregate.getQuantity())
                .version(aggregate.getVersion())
                .build();
    }
}
