package com.warehouse_kyoei.shared.relay;

import com.warehouse_kyoei.shared.event.DomainEvent;
import com.warehouse_kyoei.shared.eventstore.EventSerializer;
import com.warehouse_kyoei.shared.eventstore.EventStoreEntity;
import com.warehouse_kyoei.shared.eventstore.EventStoreRepository;
import com.warehouse_kyoei.shared.publisher.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventRelay {

    private static final int BATCH_SIZE = 100;
    private static final int SAFETY_WINDOW_SECONDS = 2;
    private static final String WORKER_ID = "kafka_main_relay";

    private final EventPublishCursorRepository cursorRepository;
    private final EventStoreRepository eventStoreRepository;
    private final EventSerializer serializer;
    private final KafkaEventPublisher kafkaPublisher;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void relay() {
        // LOCK cursor first
        EventPublishCursor cursor = cursorRepository.findForUpdate(WORKER_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Not initialize for Worker: " + WORKER_ID));

        Instant safetyThreshold = Instant.now().minus(SAFETY_WINDOW_SECONDS, ChronoUnit.SECONDS);

        List<EventStoreEntity> batch = eventStoreRepository.findUnprocessedBatch(
                cursor.getLastProcessedEventId(), safetyThreshold, PageRequest.of(0, BATCH_SIZE));

        long lastSuccessId = cursor.getLastProcessedEventId();

        for (EventStoreEntity row : batch) {
            try {
                DomainEvent event = serializer.deserialize(row.getEventType(), row.getPayload());
                kafkaPublisher.publish(event);
                lastSuccessId = row.getId();
            } catch (Exception e) {
                // STOP IMMEDIATELY — Do not process subsequent rows to guarantee strict ordering.
                // The cursor will only advance to lastSuccessId. This failed event will be retried in the next relay().
                log.error("[EventRelay] Stopping batch at event id={} ({}) due to error: {}. " +
                                "Subsequent events will wait until this is resolved.",
                        row.getId(), row.getEventType(), e.getMessage(), e);
                break;
            }
        }

        if (lastSuccessId != cursor.getLastProcessedEventId()) {
            cursor.advanceTo(lastSuccessId);
            cursorRepository.save(cursor);
        }
    }
}
