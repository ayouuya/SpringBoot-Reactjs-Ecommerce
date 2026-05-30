package com.cart.ecom_proj.dto;

public record AuthRequest(
        String fullName,
        String email,
        String role
) {
}
