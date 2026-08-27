package com.warehouse_es.inventory.consumer;

import com.warehouse_es.shared.processedEvent.ProcessedEvent;
import com.warehouse_es.shared.processedEvent.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyHelper {

    private final ProcessedEventRepository processedEventRepo;

    /**
     * @param eventId UUID of event
     * @param projectionName Eg: "SUMMARY", "LOT"
     * @param businessLogic
     */
    public void execute(UUID eventId, String projectionName, Runnable businessLogic) {
        if (processedEventRepo.existsById(eventId)) {
            log.info("[{}] Event {} đã xử lý. Bỏ qua.", projectionName, eventId);
            return;
        }

        businessLogic.run();

        processedEventRepo.save(new ProcessedEvent(eventId, Instant.now()));
    }
}
