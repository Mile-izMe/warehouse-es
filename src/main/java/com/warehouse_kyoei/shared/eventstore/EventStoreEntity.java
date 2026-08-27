package com.warehouse_kyoei.shared.eventstore;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "events")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class EventStoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType; // E.g: "Stock, Order, ..."

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId; // E.g: "WH01:SKU001"

    @Column(name = "event_type", nullable = false)
    private String eventType;   // E.g: "StockReceived"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;     // JSON of event, E.g: {"quantity":50,"lotNumber":"LOT01",...}

    @Column(name = "aggregate_version", nullable = false)
    private long aggregateVersion;    // Version of event aggregate (0,1,2,...)

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

}
