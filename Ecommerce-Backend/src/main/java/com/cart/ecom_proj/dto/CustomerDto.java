package com.cart.ecom_proj.dto;

public record CustomerDto(
        String fullName,
        String email,
        String phone,
        String address
) {
}
