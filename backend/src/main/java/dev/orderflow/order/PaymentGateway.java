package dev.orderflow.order;

import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {

    public PaymentResult authorize(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.DEMO_DECLINED) {
            return new PaymentResult(false, "The demo payment provider declined the authorization.");
        }
        return new PaymentResult(true, "Payment authorization approved.");
    }

    public record PaymentResult(boolean approved, String message) {
    }
}
