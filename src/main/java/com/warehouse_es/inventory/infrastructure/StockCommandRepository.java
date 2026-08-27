package com.warehouse_es.inventory.infrastructure;

import com.warehouse_es.inventory.domain.StockAggregate;
import com.warehouse_es.inventory.presentation.dto.response.StockResponse;
import com.warehouse_es.shared.event.DomainEvent;
import com.warehouse_es.shared.eventstore.EventStore;
import com.warehouse_es.shared.snapshot.SnapshotStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockCommandRepository {

    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private static final int SNAPSHOT_THRESHOLD = 100;

    // ==================== SMART LOAD ====================
    public StockAggregate load(String warehouseCode, String skuCode) {
        String aggregateId = StockAggregate.aggregateId(warehouseCode, skuCode);
        StockAggregate aggregate = new StockAggregate(warehouseCode, skuCode);

        // Load snapshot (if available)
        // loadSnapshot automatically inject quantity & version from DB into object 'aggregate'
        boolean hasSnapshot = snapshotStore.loadSnapshot(aggregateId, aggregate);

        // Define version to query DB
        // If snapshot existed -> Get version of snapshot (eg: 100). If not -> Start from 0.
        long versionToLoadFrom = hasSnapshot ? aggregate.getVersion() : 0;
        List<DomainEvent> subsequentEvents = eventStore.loadEventsAfterVersion(aggregateId, versionToLoadFrom);

        // Replay
        aggregate.replay(subsequentEvents);

        return aggregate;
    }

    // ==================== PERSIST & SNAPSHOT ====================
    @Transactional
    public StockResponse persist(StockAggregate aggregate) {
        List<DomainEvent> newEvents = aggregate.pullUncommittedEvents();

        // NO NEW EVENTS = DO NOTHING
        if (newEvents.isEmpty()) {
            return toResponse(aggregate);
        }

        // EXPECTED VERSION: Old version = Current version - nums of new events
        long oldVersion = aggregate.getVersion() - newEvents.size();
        long currentVersion = aggregate.getVersion();

        eventStore.append(
                aggregate.getId(),
                newEvents,
                oldVersion
        );

        if ((currentVersion / SNAPSHOT_THRESHOLD) > (oldVersion / SNAPSHOT_THRESHOLD)) {
            snapshotStore.saveSnapshot(aggregate);
        }

        return toResponse(aggregate);
    }

    public StockResponse toResponse(StockAggregate aggregate) {
        return StockResponse.builder()
                .warehouseCode(aggregate.getWarehouseCode())
                .skuCode(aggregate.getSkuCode())
                .quantity(aggregate.getQuantity())
                .version(aggregate.getVersion())
                .build();
    }
}
