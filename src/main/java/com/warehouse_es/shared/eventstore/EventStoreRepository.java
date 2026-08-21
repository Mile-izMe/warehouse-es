package com.warehouse_es.shared.eventstore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventStoreRepository extends JpaRepository<EventStoreEntity, UUID> {

    // Get all event of 1 aggregate, right order to replay
    List<EventStoreEntity> findByAggregateIdOrderByAggregateVersionAsc(String aggregateId);

    // Check optimistic concurrency: newest version
    // Get MAX of version col, combine with Index UNIQUE => Complexity O(1).
    @Query("SELECT COALESCE(MAX(e.aggregateVersion), 0) FROM EventStoreEntity e WHERE e.aggregateId = :aggregateId")
    long getCurrentVersion(@Param("aggregateId") String aggregateId);

}
