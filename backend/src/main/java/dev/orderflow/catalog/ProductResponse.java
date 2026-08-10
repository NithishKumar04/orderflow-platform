package dev.orderflow.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        String imageUrl,
        int inventory,
        boolean featured
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getImageUrl(),
                product.getInventory(),
                product.isFeatured()
        );
    }
}
