package com.cart.ecom_proj.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        int productId,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
