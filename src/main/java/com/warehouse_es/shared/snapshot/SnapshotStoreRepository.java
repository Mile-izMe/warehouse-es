package com.warehouse_es.shared.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotStoreRepository extends JpaRepository<SnapshotStoreEntity, String> {
}
