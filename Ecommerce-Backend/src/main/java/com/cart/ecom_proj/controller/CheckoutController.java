package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.CheckoutRequest;
import com.cart.ecom_proj.dto.CheckoutResponse;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.OrderStatus;
import com.cart.ecom_proj.model.PaymentStatus;
import com.cart.ecom_proj.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            CheckoutResponse response = checkoutService.checkout(request, currentUser);
            if (response.success()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException ex) {
            CheckoutResponse response = new CheckoutResponse(false, ex.getMessage(), null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, "MAD");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
