package com.warehouse_es.shared.snapshot;

import com.warehouse_es.shared.domain.AggregateRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotStore {

    private final SnapshotStoreRepository repository;
    private final SnapshotSerializer serializer;

    /**
     * Create a snapshot of event when version of an aggregate comes to 100
     */
    public void saveSnapshot(AggregateRoot aggregate) {
        String payload = aggregate.createSnapshotPayload(serializer);

        SnapshotStoreEntity entity = SnapshotStoreEntity.builder()
                .aggregateId(aggregate.getId())
                .snapshotVersion((int) aggregate.getVersion())
                .snapshotPayload(payload)
                .build();

        repository.save(entity);
        log.info("Took snapshot for Aggregate {} at version {}", aggregate.getId(), aggregate.getVersion());
    }

    /**
     * Read Snapshot & convert follow Class Aggregate (StockAggregate, OrderAggregate...)
     */
    public boolean loadSnapshot(String aggregateId, AggregateRoot emptyAggregate) {
        Optional<SnapshotStoreEntity> snapOpt = repository.findById(aggregateId);

        if (snapOpt.isPresent()) {
            SnapshotStoreEntity snap = snapOpt.get();
            emptyAggregate.restoreFromSnapshot(snap.getSnapshotPayload(), snap.getSnapshotVersion(), serializer);
            log.info("snapshot success for {} from version {}", aggregateId, snap.getSnapshotVersion());
            return true;
        }
        return false;
    }
}
