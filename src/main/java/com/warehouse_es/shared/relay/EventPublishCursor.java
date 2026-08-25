package com.warehouse_es.shared.relay;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "event_publish_cursor")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPublishCursor {

    @Id
    private String workerId;

    private long lastProcessedEventId;

    public void advanceTo(long eventId) {
        this.lastProcessedEventId = eventId;
    }
}
