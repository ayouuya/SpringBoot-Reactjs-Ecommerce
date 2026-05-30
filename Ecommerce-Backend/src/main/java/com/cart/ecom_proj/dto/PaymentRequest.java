package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String customerEmail,
        CardPaymentDetails card,
        PayPalPaymentDetails paypal
) {
}
