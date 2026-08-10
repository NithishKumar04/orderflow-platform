package dev.orderflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.orderflow.catalog.Product;
import dev.orderflow.catalog.ProductRepository;
import dev.orderflow.events.OrderPlacedMessage;
import dev.orderflow.order.OrderStatus;
import dev.orderflow.order.OrderWorkflowProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "orderflow.events.relay-enabled=false")
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderWorkflowProcessor workflowProcessor;

    private String token;

    @BeforeEach
    void login() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@orderflow.dev",
                                  "password": "Demo123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Nithish Demo"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void returnsPublicCatalogAndProtectsOrders() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.length()").value(6));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutIsIdempotentAndCompletesTheWorkflow() throws Exception {
        Product product = productRepository.findAllByOrderByFeaturedDescNameAsc().getFirst();
        int stockBefore = product.getInventory();
        String key = "test-" + UUID.randomUUID();
        String body = checkoutBody(product.getId(), "DEMO_APPROVED");

        String firstResponse = checkout(key, body);
        String duplicateResponse = checkout(key, body);
        JsonNode first = objectMapper.readTree(firstResponse);
        JsonNode duplicate = objectMapper.readTree(duplicateResponse);

        assertThat(duplicate.get("id").asText()).isEqualTo(first.get("id").asText());
        assertThat(first.get("status").asText()).isEqualTo(OrderStatus.PENDING.name());

        UUID orderId = UUID.fromString(first.get("id").asText());
        workflowProcessor.process(new OrderPlacedMessage(UUID.randomUUID(), orderId));

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.timeline.length()").value(4));

        int stockAfter = productRepository.findById(product.getId()).orElseThrow().getInventory();
        assertThat(stockAfter).isEqualTo(stockBefore - 1);
    }

    @Test
    void declinedPaymentReleasesReservedInventory() throws Exception {
        Product product = productRepository.findAllByOrderByFeaturedDescNameAsc().get(1);
        int stockBefore = product.getInventory();
        String body = checkoutBody(product.getId(), "DEMO_DECLINED");
        JsonNode created = objectMapper.readTree(checkout("decline-" + UUID.randomUUID(), body));
        UUID orderId = UUID.fromString(created.get("id").asText());

        workflowProcessor.process(new OrderPlacedMessage(UUID.randomUUID(), orderId));

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_FAILED"));

        int stockAfter = productRepository.findById(product.getId()).orElseThrow().getInventory();
        assertThat(stockAfter).isEqualTo(stockBefore);
    }

    private String checkout(String idempotencyKey, String body) throws Exception {
        return mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String checkoutBody(UUID productId, String paymentMethod) {
        return """
                {
                  "items": [{"productId": "%s", "quantity": 1}],
                  "paymentMethod": "%s"
                }
                """.formatted(productId, paymentMethod);
    }
}
