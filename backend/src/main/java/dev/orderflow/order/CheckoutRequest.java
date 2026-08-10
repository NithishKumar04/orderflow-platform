package dev.orderflow.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CheckoutRequest(
        @NotEmpty @Size(max = 20) List<@Valid CheckoutItem> items,
        @NotNull PaymentMethod paymentMethod
) {
    public record CheckoutItem(
            @NotNull UUID productId,
            @Min(1) @Max(10) int quantity
    ) {
    }
}
