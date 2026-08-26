# 📦 WarehouseES

> A backend system for warehouse and inventory management, built with Java and Spring Boot, focusing on Event Sourcing,
> CQRS, transactional outbox pattern, Kafka message ordering, idempotent projections, and scalable read models.

---

## 📖 Project Overview

**WarehouseES** is an inventory management platform designed to simulate the backend architecture of a real-world, high-concurrency supply chain system.

The system allows users to:
- Receive stock from suppliers
- Pick and dispatch stock for orders
- Adjust stock levels manually (audits/reconciliations)
- View real-time inventory summaries
- Track specific stock lots (FEFO logic)
- Monitor daily stock movements

The main purpose of WarehouseES is not simply to implement CRUD operations, but to explore and demonstrate backend engineering concepts commonly found in distributed, event-driven transactional systems.

The project focuses particularly on:
- **Event Sourcing**
- **CQRS (Command Query Responsibility Segregation)**
- **Transactional Outbox Pattern**
- **Dual-write prevention**
- **Strict message ordering (Kafka)**
- **Exactly-once processing (Idempotency)**
- **Denormalized read models (Projections)**
- **Scalable backend design**

---

## ✨ Features

### 🔄 Event Sourcing & CQRS
- Append-only event store
- Complete historical audit trail
- Separation of Write (Command) and Read (Query) concerns
- Domain Event versioning

### 📦 Inventory Management
- Real-time stock summary calculation
- Lot-level tracking with FEFO (First-Expiry-First-Out) logic
- Daily stock movement tracking
- Aggregate validation

### 📨 Reliable Messaging (Outbox Pattern)
- Database cursor tracking
- Scheduled event relay worker
- Pessimistic locking for concurrent worker execution
- Guaranteed at-least-once delivery to Kafka

### 🛡️ Idempotent Projections
- Automatic duplicate event detection
- Processed event tracking
- Atomic read-model updates
- Safe replayability

### ⚠️ Error Handling
Centralized error handling with structured error responses.

Example:
```json
{
  "timestamp": "2026-08-26T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "errorCode": "INSUFFICIENT_STOCK",
  "message": "Aggregate WH01-SKU001 does not have enough stock to fulfill the request",
  "path": "/api/inventory/pick",
  "traceId": "f9e8d7c6"
}
```

### 🛠️ Tech Stack

- Backend: Java 17+, Spring Boot
- Database & Storage: PostgreSQL (Event Store & Read Models), Flyway
- Messaging & Real-time: Apache Kafka
- Infrastructure
- Testing & API

### 🏗️ Architecture

- WarehouseES follows a package-by-feature architecture.
- Instead of organizing the entire application by technical layer, 
the project groups code around business features and architectural concepts:

```
src/
└── main/
    └── java/
        └── com/
            └── warehouse_es/
                ├── common/
                ├── shared/
                │   ├── event/
                │   ├── eventstore/
                │   ├── publisher/
                │   ├── processedEvent/
                ├── inventory/
                │   ├── command/         (Write Model - Domain, Handlers)
                │   ├── consumer/        (Read Model - Kafka Listeners, Projectors)
                │   ├── domain/          (Aggregates, Events)
                │   ├── infrastructure/  (Repositories)
                │   ├── projection/      (Read Model Entities)
```

## 🔄 Event Sourcing Flow
- Why use event sourcing instead of UPDATING directly to DB

- **Advantages**: 
  - Reason of getting that data state (History)
  - Avoid RACE CONDITION (due to PESSIMISTIC LOCK when insert to events table)

- **Disadvantages**:
    - If the server downtime → All data will be lost

```
User / System
 │
 │ 1. HTTP Request (JSON)
 ▼
Controller
 │
 │ 2. Convert to Command (e.g. PickStockCommand)
 ▼
Command Service (Orchestrator)
 │
 │ 3. Request historical events by AggregateID
 ▼
Event Store
 │                                                       
 │ 4. Return Event Stream: [Event v1, Event v2, ...]     
 ▼                                                       
Aggregate Root (In RAM)
 │
 │ 5. Replay: Apply past events -> Current State (e.g. Stock = 10)                                   
 │ 6. Execute Command: Validate business logic (e.g. 10 >= 3? OK!)                               
 │ 7. Apply New Event: StockPicked (Quantity = 3, Version = 4)                                 
 │ 8. Push to "uncommittedEvents" list (In RAM)  
 ▼                                                       
Command Service                                     
 │                                                       
 │ 9. Collect uncommittedEvents from Aggregate         
 ▼                                                       
Event Store (Database)
 │
 │ 10. Append to "events" table (Check Optimistic Lock / Version = 4)
 ▼
Event Relay / Outbox (Waiting to push Kafka)
```



## 🔄 Core Event Flow

- The core flow is designed around the concept of a Single Source of Truth (The Event Store).
```
User / System
 │
 │ Import Stock Command
 ▼
Command Service
 │
 │ Validate & Save Event
 ▼
Database Transaction (Event Store)
 │
 │ Event Saved (Status: UNPUBLISHED)
 ▼
Event Relay (Outbox Job)
 │
 │ Polls DB & Locks Cursor
 ▼
KafkaEventPublisher
 │
 │ Send to Kafka
 ▼
Kafka Topic (Key = AggregateID)
 │
 │ Consumed by StockConsumer
 ▼
StockProjectionHandler
 │
 ├── 1. Check Idempotency (Processed DB)
 ├── 2. Update Stock Summary
 ├── 3. Update Stock Lot (FEFO)
 ├── 4. Update Daily Movement
 └── 5. Save EventID to Processed DB
 │
 ▼
Read Model Ready for Client Queries
```

## 🔒 Transactional Outbox & Ordering
- Directly publishing to Kafka during a database transaction causes the "Dual-Write" problem. 
- WarehouseES uses the Outbox Pattern to guarantee consistency.

```
Database Transaction ─────────┐
                              │
                              ▼
                        Save Event to DB
                              │
                              ▼
                        Commit Success
                              │
  (Async/Cron Job)            │
Event Relay Worker ───────────┘
                              │
                              ▼
                      Fetch Unprocessed Events
                              │
                              ▼
                    Publish to Kafka (Key: WHC:SKU)
                              │
                              ▼
                    Update Cursor in DB
```
## 🔁 Idempotent Projection Flow
- Kafka guarantees "at-least-once" delivery. 
- The projection layer is designed to be idempotent to handle duplicate deliveries gracefully.

```
Kafka Callback (Message Received)
   │
   │ eventId: "123e4567-e89b..."
   ▼
Check ProcessedEventRepository
   │
   ├────────── EXISTS? ──────────┐
   │ (Yes)                       │ (No)
   ▼                             ▼
Skip Processing            Apply Data Delta
   │                             │
   │                             ▼
No Duplicate Data          Save to Read Models
                                 │
                                 ▼
                           Save eventId to DB
                                 │
                                 ▼
                           Transaction Commit
```