package com.cart.ecom_proj.dto;

public record AuthRequest(
        String email,
        String password
) {
}
