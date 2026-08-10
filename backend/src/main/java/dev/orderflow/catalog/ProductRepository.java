package dev.orderflow.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByOrderByFeaturedDescNameAsc();

    @Modifying(flushAutomatically = true)
    @Query("""
            update Product p
               set p.inventory = p.inventory - :quantity,
                   p.updatedAt = :updatedAt,
                   p.version = p.version + 1
             where p.id = :productId
               and p.inventory >= :quantity
            """)
    int reserve(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity,
            @Param("updatedAt") Instant updatedAt
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            update Product p
               set p.inventory = p.inventory + :quantity,
                   p.updatedAt = :updatedAt,
                   p.version = p.version + 1
             where p.id = :productId
            """)
    int release(
            @Param("productId") UUID productId,
            @Param("quantity") int quantity,
            @Param("updatedAt") Instant updatedAt
    );
}
