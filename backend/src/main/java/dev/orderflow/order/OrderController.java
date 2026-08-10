package dev.orderflow.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @RequestHeader("Idempotency-Key") @Size(min = 8, max = 120) String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request
    ) {
        OrderResponse response = orderService.checkout(authentication.getName(), idempotencyKey, request);
        return ResponseEntity
                .created(URI.create("/api/orders/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<OrderResponse> list(Authentication authentication) {
        return orderService.list(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(Authentication authentication, @PathVariable UUID orderId) {
        return orderService.get(authentication.getName(), orderId);
    }

    @DeleteMapping("/{orderId}")
    public OrderResponse cancel(Authentication authentication, @PathVariable UUID orderId) {
        return orderService.cancel(authentication.getName(), orderId);
    }
}
