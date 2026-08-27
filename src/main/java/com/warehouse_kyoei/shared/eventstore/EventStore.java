package com.warehouse_kyoei.shared.eventstore;


import com.warehouse_kyoei.shared.event.DomainEvent;
import com.warehouse_kyoei.shared.exception.ConcurrencyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EventStore is the "ONLY ENTRY" of events table.
 * All domain/application layer talks to DB through this class —
 * No other repository are allowed to read/write direct to events table.
 */
@Component
@RequiredArgsConstructor
public class EventStore {

    private final EventStoreRepository repository;
    private final EventSerializer serializer;

    /**
     * Read all history event of 1 aggregate, followed by asc version.
     * Use to replay -> get current state (StockItem.replay()).
     */
    public List<DomainEvent> loadHistory(String aggregateId) {
        return loadEventsAfterVersion(aggregateId, 0L);
    }

    /**
     * Use when there is Snapshot: Load event after snapshot
     */
    public List<DomainEvent> loadEventsAfterVersion(String aggregateId, long version) {
        return repository.findByAggregateIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(aggregateId, version)
                .stream()
                .map(e -> serializer.deserialize(e.getEventType(), e.getPayload()))
                .toList();
    }

    /** Current Version (= Nums of events possessed)*/
    public long currentVersion(String aggregateId) {
        return repository.getCurrentVersion(aggregateId);
    }

    /**
     * Write new events after business call (receive/pick/adjust) to DB.

     * expectedVersion: version that caller READ when load aggregate (before handle).
     * In the process, if the version in DB has differed (someone write already)
     * -> Throw exception, NO OVERWRITE
     */
    @Transactional
    public void append(String aggregateId, List<DomainEvent> newEvents, long expectedVersion) {
        long actualVersion = currentVersion(aggregateId);
        if (actualVersion != expectedVersion) {
            throw new ConcurrencyConflictException(aggregateId, expectedVersion, actualVersion);
        }

        long version = expectedVersion;

        List<EventStoreEntity> entitiesToSave = new java.util.ArrayList<>();

        for (DomainEvent event : newEvents) {
            version++;

            EventStoreEntity entity = EventStoreEntity.builder()
                    .aggregateType(event.aggregateType())
                    .aggregateId(aggregateId)
                    .eventType(serializer.eventTypeOf(event))
                    .payload(serializer.serialize(event))
                    .aggregateVersion(version)
                    .occurredAt(event.occurredAt())
                    .build();

            entitiesToSave.add(entity);
        }
        repository.saveAll(entitiesToSave);
    }

}
