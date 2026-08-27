package com.warehouse_kyoei.shared.eventstore;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventStoreRepository extends JpaRepository<EventStoreEntity, UUID> {

    // Get all event of 1 aggregate, right order to replay
    List<EventStoreEntity> findByAggregateIdOrderByAggregateVersionAsc(String aggregateId);

    List<EventStoreEntity> findByAggregateIdAndAggregateVersionGreaterThanOrderByAggregateVersionAsc(
            String aggregateId, long version);

    // Check optimistic concurrency: newest version
    // Get MAX of version col, combine with Index UNIQUE => Complexity O(1).
    @Query("SELECT COALESCE(MAX(e.aggregateVersion), 0) FROM EventStoreEntity e WHERE e.aggregateId = :aggregateId")
    long getCurrentVersion(@Param("aggregateId") String aggregateId);

    /**
     * "safetyThreshold" = now() - a buffer (eg: 2s) — RETRIEVE ONLY older event than this timestamp.
     * Prevent "ID gaps": with reasonable assumption that 1 event-writing transaction takes no longer than 2s,
     * by the time poller run, any transactions with a LOWER ID is guaranteed to have committed success
     * No longer any pending transactions trailing behind the cursor
     */
    @Query("SELECT e FROM EventStoreEntity e " +
            "WHERE e.id > :afterId AND e.occurredAt <= :safetyThreshold " +
            "ORDER BY e.id ASC")
    List<EventStoreEntity> findUnprocessedBatch(long afterId, Instant safetyThreshold, Pageable pageable);
}
