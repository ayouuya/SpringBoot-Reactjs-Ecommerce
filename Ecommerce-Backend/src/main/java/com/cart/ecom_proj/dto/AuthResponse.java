package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.UserRole;

public record AuthResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
}
