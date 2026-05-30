package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.OrderStatus;
import com.cart.ecom_proj.model.PaymentStatus;

public record CheckoutResponse(
        boolean success,
        String message,
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        String currency
) {
}
