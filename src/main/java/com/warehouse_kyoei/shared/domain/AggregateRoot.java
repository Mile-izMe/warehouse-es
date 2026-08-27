package com.warehouse_kyoei.shared.domain;

import com.warehouse_kyoei.shared.event.DomainEvent;
import com.warehouse_kyoei.shared.snapshot.SnapshotSerializer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Use for all Aggregate.
 * Logic handle version, event history & uncommitted events.
 */
@Getter
public abstract class AggregateRoot {

    protected String id; // Aggregate ID (eg: warehouse_sku)
    protected long version = 0; // Start from 0

    // Provide new events generated in RAM, await to save DB
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    /**
     * Load Aggregate from DB.
     * Recreate state by replay event's history in DB.
     */
    public void replay(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            mutate(event);
            this.version = event.aggregateVersion();
        }
    }

    /**
     * Subclass to call when there is a valid action.
     */
    protected void raise(DomainEvent event) {
        mutate(event);
        this.version = event.aggregateVersion(); // Increase version in RAM
        uncommittedEvents.add(event);
    }

    /**
     * Get list of new events to save DB (Event Store),
     * by the way swipe it out of RAM.
     */
    public List<DomainEvent> pullUncommittedEvents() {
        List<DomainEvent> copy = List.copyOf(this.uncommittedEvents);
        this.uncommittedEvents.clear();
        return copy;
    }

    /**
     * Get next version when build new Event
     */
    protected long getNextVersion() {
        return this.version + 1;
    }

    /**
     * Required SubAggregate define how State change
     * when receive an Event. (NO validation rule).
     */
    protected abstract void mutate(DomainEvent event);

    // ==================== SNAPSHOT SUPPORT (Memento Pattern) ====================
    /**
     * Serialize business state (object) to JSON string.
     * NOT serialize uncommittedEvents or version.
     */
    public abstract String createSnapshotPayload(SnapshotSerializer serializer);

    /**
     * Restore business state (object) from JSON string + version.
     */
    public abstract void restoreFromSnapshot(String payload, long version, SnapshotSerializer serializer);
}