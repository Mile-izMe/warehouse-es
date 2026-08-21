package com.warehouse_es.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * All events implement this interface.
 * eventId: prevent save same event
 * aggregateId: Aggregate identify (eg: "WH01-SKU001")
 * aggregateType: Categorize aggregate (eg: "STOCK", "TRANSFER")
 * aggregateVersion: After event occurs => Handle optimistic
 * occurredAt: Used to replay follow the right time
 */
public interface DomainEvent {

    UUID eventId();
    String aggregateId();
    String aggregateType();
    long aggregateVersion();
    Instant occurredAt();

}
