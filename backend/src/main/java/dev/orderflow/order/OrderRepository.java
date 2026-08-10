package dev.orderflow.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Order> findDetailedByIdAndUserId(UUID id, String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct o from Order o left join fetch o.items where o.id = :id")
    Optional<Order> findForProcessing(@Param("id") UUID id);
}
