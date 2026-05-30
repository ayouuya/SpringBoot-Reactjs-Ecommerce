package com.cart.ecom_proj.dto;

import java.math.BigDecimal;

public record CartItemDto(
        int productId,
        String name,
        String brand,
        String category,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        boolean productAvailable,
        int stockQuantity
) {
}
