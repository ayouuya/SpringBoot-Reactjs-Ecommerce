package com.cart.ecom_proj.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password
) {
}
