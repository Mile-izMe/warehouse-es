package com.warehouse_es.shared.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * All events implement this interface.
 * eventId: prevent save same event
 * eventType: Identity unique type for each event (eg: "StockReceivedEvent")
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

    /**
     * Tự động lấy tên Class làm eventType (VD: "StockReceived").
     * @JsonProperty giúp Jackson buộc phải thêm trường này vào chuỗi JSON.
     * Bên Consumer sẽ đọc trường này để phân loại sự kiện.
     */
    @JsonProperty("eventType")
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
