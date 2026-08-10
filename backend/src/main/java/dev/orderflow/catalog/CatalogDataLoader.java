package dev.orderflow.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CatalogDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    public CatalogDataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
                new Product(
                        "AUDIO-001",
                        "Arc Studio Headphones",
                        "Low-latency over-ear headphones with 40-hour battery life.",
                        "Audio",
                        new BigDecimal("189.00"),
                        "/products/headphones.jpg",
                        18,
                        true
                ),
                new Product(
                        "DESK-002",
                        "Slate Mechanical Keyboard",
                        "Compact hot-swappable keyboard with tactile switches.",
                        "Workspace",
                        new BigDecimal("129.00"),
                        "/products/keyboard.jpg",
                        24,
                        true
                ),
                new Product(
                        "LIGHT-003",
                        "Halo Task Light",
                        "Dimmable desk light with adjustable color temperature.",
                        "Workspace",
                        new BigDecimal("79.00"),
                        "/products/lamp.jpg",
                        31,
                        false
                ),
                new Product(
                        "CARRY-004",
                        "Transit Work Pack",
                        "Weather-resistant day pack with a suspended laptop sleeve.",
                        "Carry",
                        new BigDecimal("118.00"),
                        "/products/backpack.jpg",
                        12,
                        true
                ),
                new Product(
                        "HYDRATE-005",
                        "Orbit Steel Bottle",
                        "Double-wall insulated bottle designed for all-day temperature control.",
                        "Everyday",
                        new BigDecimal("36.00"),
                        "/products/bottle.jpg",
                        42,
                        false
                ),
                new Product(
                        "NOTES-006",
                        "Grid Project Notebook",
                        "Lay-flat dot-grid notebook for planning systems and products.",
                        "Everyday",
                        new BigDecimal("22.00"),
                        "/products/notebook.jpg",
                        55,
                        false
                )
        ));
    }
}
