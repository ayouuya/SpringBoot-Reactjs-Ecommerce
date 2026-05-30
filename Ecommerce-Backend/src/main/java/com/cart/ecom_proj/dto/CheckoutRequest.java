package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.PaymentMethod;

public record CheckoutRequest(
        String cartKey,
        CustomerDto customer,
        PaymentMethod paymentMethod,
        CardPaymentDetails card,
        PayPalPaymentDetails paypal
) {
}
