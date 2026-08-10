package dev.orderflow.catalog;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "products", key = "'catalog'")
    public List<ProductResponse> listProducts() {
        return productRepository.findAllByOrderByFeaturedDescNameAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> search(String query, String category) {
        String normalizedQuery = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null ? "" : category.strip().toLowerCase(Locale.ROOT);

        return listProducts().stream()
                .filter(product -> normalizedQuery.isBlank()
                        || product.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || product.description().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .filter(product -> normalizedCategory.isBlank()
                        || product.category().toLowerCase(Locale.ROOT).equals(normalizedCategory))
                .toList();
    }
}
