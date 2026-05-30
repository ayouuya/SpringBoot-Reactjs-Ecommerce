package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        String orderNumber,
        OrderStatus status,
        List<OrderItemDto> items,
        BigDecimal subtotal,
        BigDecimal total,
        String currency,
        String customerName,
        String customerEmail,
        String customerPhone,
        String shippingAddress,
        LocalDateTime createdAt
) {
}
