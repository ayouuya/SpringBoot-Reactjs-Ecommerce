package com.cart.ecom_proj.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}
