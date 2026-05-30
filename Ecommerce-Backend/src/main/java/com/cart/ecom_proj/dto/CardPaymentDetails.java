package com.cart.ecom_proj.dto;

public record CardPaymentDetails(
        String cardHolder,
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvv
) {
}
