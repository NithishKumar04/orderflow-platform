package dev.orderflow.order;

import dev.orderflow.catalog.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public boolean reserve(UUID productId, int quantity) {
        return productRepository.reserve(productId, quantity, Instant.now()) == 1;
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void release(UUID productId, int quantity) {
        productRepository.release(productId, quantity, Instant.now());
    }
}
