package com.warehouse_es.inventory.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application Service — Orchestration of Event Sourcing:

 *   0. validate product + warehouse exists (master data, avoid entering unexisted SKU/storage)
 *   1. loadHistory(aggregateId)          -> Get all event's history from DB
 *   2. StockItem.replay(history)         -> Recreated aggregate at current state
 *   3. Call business logic               -> validate rule + generate new event (in RAM)
 *   4. eventStore.append(...)            -> write new event, attach check optimistic concurrency
 */

@Service
@RequiredArgsConstructor
public class StockCommandService {


}
