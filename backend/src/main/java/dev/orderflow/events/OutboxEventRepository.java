package dev.orderflow.events;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            Instant now
    );
}
