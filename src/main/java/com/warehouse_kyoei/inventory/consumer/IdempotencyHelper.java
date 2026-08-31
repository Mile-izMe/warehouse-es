package com.warehouse_kyoei.inventory.consumer;

import com.warehouse_kyoei.shared.processedEvent.ProcessedEvent;
import com.warehouse_kyoei.shared.processedEvent.ProcessedEventRepository;
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
        String idempotencyKey = eventId.toString() + ":" + projectionName;

        if (processedEventRepo.existsById(idempotencyKey)) {
            log.info("[{}] Event {} đã xử lý. Bỏ qua.", projectionName, eventId);
            return;
        }

        businessLogic.run();

        processedEventRepo.save(new ProcessedEvent(idempotencyKey, Instant.now()));
    }
}
