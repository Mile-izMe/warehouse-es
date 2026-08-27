package com.warehouse_es.shared.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnapshotSerializer {

    private final ObjectMapper mapper;

    /** Object (DTO) -> JSON string */
    public String serialize(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Can not serialize object: " + payload, e);
        }
    }

    /** JSON string -> Object (DTO) */
    public <T> T deserialize(String payload, Class<T> clazz) {
        try {
            return mapper.readValue(payload, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Can not deserialize snapshot to " + clazz.getSimpleName(), e);
        }
    }
}
