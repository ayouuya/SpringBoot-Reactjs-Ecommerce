package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.AddCartItemRequest;
import com.cart.ecom_proj.dto.CartDto;
import com.cart.ecom_proj.dto.UpdateCartItemRequest;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.service.CartService;
import com.cart.ecom_proj.service.UserAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserAccessService userAccessService;

    public CartController(CartService cartService, UserAccessService userAccessService) {
        this.cartService = cartService;
        this.userAccessService = userAccessService;
    }

    @GetMapping("/{cartKey}")
    public ResponseEntity<?> getCart(
            @PathVariable String cartKey,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(cartService.getCart(cartKey), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{cartKey}/items")
    public ResponseEntity<?> addItem(
            @PathVariable String cartKey,
            @RequestBody AddCartItemRequest request,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(cartService.addItem(cartKey, request), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{cartKey}/items/{productId}")
    public ResponseEntity<?> updateItem(
            @PathVariable String cartKey,
            @PathVariable int productId,
            @RequestBody UpdateCartItemRequest request,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(cartService.updateItem(cartKey, productId, request), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{cartKey}/items/{productId}")
    public ResponseEntity<?> removeItem(
            @PathVariable String cartKey,
            @PathVariable int productId,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(cartService.removeItem(cartKey, productId), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{cartKey}")
    public ResponseEntity<?> clearCart(
            @PathVariable String cartKey,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireUser(email, role, UserRole.USER);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        try {
            cartService.clearCart(cartKey);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
