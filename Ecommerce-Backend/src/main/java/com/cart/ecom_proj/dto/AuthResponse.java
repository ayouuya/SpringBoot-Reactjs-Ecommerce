package com.cart.ecom_proj.dto;

import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.UserRole;

public record AuthResponse(
        String token,
        String tokenType,
        Long id,
        String fullName,
        String email,
        UserRole role
) {

    public static AuthResponse from(AppUser user, String token) {
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
