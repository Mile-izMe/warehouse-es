package com.warehouse_es.shared.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "snapshot_store")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotStoreEntity {

    @Id
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "snapshot_version", nullable = false)
    private int snapshotVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload", columnDefinition = "jsonb")
    private String snapshotPayload;
}
