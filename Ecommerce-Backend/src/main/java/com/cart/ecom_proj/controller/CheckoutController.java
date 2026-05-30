package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.CheckoutRequest;
import com.cart.ecom_proj.dto.CheckoutResponse;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.OrderStatus;
import com.cart.ecom_proj.model.PaymentStatus;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.service.CheckoutService;
import com.cart.ecom_proj.service.UserAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserAccessService userAccessService;

    public CheckoutController(CheckoutService checkoutService, UserAccessService userAccessService) {
        this.checkoutService = checkoutService;
        this.userAccessService = userAccessService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestBody CheckoutRequest request,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        AppUser user;
        try {
            user = userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            CheckoutResponse response = new CheckoutResponse(false, ex.getMessage(), null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, "MAD");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            CheckoutResponse response = new CheckoutResponse(false, ex.getMessage(), null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, "MAD");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            CheckoutResponse response = checkoutService.checkout(request, user);
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
