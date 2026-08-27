package com.warehouse_kyoei.shared.exception;


/**
 * Thrown when two concurrent requests attempt to write events to the SAME aggregate
 * based on a stale version (i.e., the version was read, but another process wrote to the aggregate in the interim).

 * Implement OPTIMISTIC CONCURRENCY CONTROL mechanism:
 * it does not lock the database but instead verifies the version at the time of writing.
 * If it conflicts, service layer decides whether to retry or report an error to the user.
 */
public class ConcurrencyConflictException extends RuntimeException {
    public ConcurrencyConflictException(String aggregateId, long expectedVersion, long actualVersion) {
        super("Conflict writing data for '" + aggregateId + "': expected version "
                + expectedVersion + " but current version is: " + actualVersion
                + ". There is previous request, please try again.");
    }
}
