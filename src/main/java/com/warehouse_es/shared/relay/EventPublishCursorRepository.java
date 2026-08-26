package com.warehouse_es.shared.relay;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EventPublishCursorRepository extends JpaRepository<EventPublishCursor, String> {

    /**
     * LOCK required: If scale-out later, this lock ensure:
     * ONLY 1 instance read/write cursor at 1 time — 2 instance both read cursor=10 then
     * both advance to 15 -> Lead to each of 2 advance becomes "losing" if no lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM EventPublishCursor c WHERE c.workerId = :workerId")
    Optional<EventPublishCursor> findForUpdate(String workerId);

    boolean existsByWorkerId(String workerId);
}
