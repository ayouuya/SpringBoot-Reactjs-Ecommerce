package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.PaymentStatus;

public record PaymentResult(
        PaymentStatus status,
        String reference,
        String message
) {
}
