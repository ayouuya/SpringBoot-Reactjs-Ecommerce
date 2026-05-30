package com.cart.ecom_proj.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        String cartKey,
        List<CartItemDto> items,
        BigDecimal subtotal,
        BigDecimal total,
        int itemCount,
        String currency
) {
}
