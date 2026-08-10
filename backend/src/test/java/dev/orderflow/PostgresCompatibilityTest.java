package dev.orderflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "orderflow.events.relay-enabled=false")
class PostgresCompatibilityTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16.6-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void migrationAndCatalogSeedRunOnPostgres() {
        Integer migrationCount = jdbcClient.sql("""
                        select count(*)
                          from flyway_schema_history
                         where success = true
                        """)
                .query(Integer.class)
                .single();
        Integer productCount = jdbcClient.sql("select count(*) from products")
                .query(Integer.class)
                .single();

        assertThat(migrationCount).isGreaterThanOrEqualTo(1);
        assertThat(productCount).isEqualTo(6);
    }
}
