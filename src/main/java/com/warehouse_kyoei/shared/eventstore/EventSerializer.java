package com.warehouse_kyoei.shared.eventstore;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.warehouse_kyoei.inventory.domain.event.StockEvents;
import com.warehouse_kyoei.shared.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Bridge between "DomainEvent" (object Java, in domain layer)
 * & "payload JSON string" (store into TEXT col in events table).

 * Mapping eventType (String) -> respectively class Java.
 * When new event appears -> register 1 new row into DESERIALIZERS.
 */

@Component
public class EventSerializer {

    private final ObjectMapper mapper;

    // Register mapping: eventName in DB -> function parse JSON to object
    private final Map<String, Function<String, DomainEvent>> deserializers;

    public EventSerializer() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.deserializers = Map.of(
                "StockReceived", json -> readValue(json, StockEvents.StockReceived.class),
                "StockPicked", json -> readValue(json, StockEvents.StockPicked.class),
                "StockAdjusted", json -> readValue(json, StockEvents.StockAdjusted.class)
        );
    }

    /** Object event -> JSON string to save DB */
    public String serialize(DomainEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Can not serialize event: " + event, e);
        }
    }

    /** JSON string -> convert back to object DomainEvent to replay */
    public DomainEvent deserialize(String eventType, String payload) {
        Function<String, DomainEvent> fn = deserializers.get(eventType);
        if (fn == null) {
            throw new IllegalArgumentException("Can not recognize eventType: " + eventType);
        }
        return fn.apply(payload);
    }

    /** Class event name (eventType col) */
    public String eventTypeOf(DomainEvent event) {
        return event.eventType();
    }

    private <T> T readValue(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Can not deserialize payload for " + clazz, e);
        }
    }
}
