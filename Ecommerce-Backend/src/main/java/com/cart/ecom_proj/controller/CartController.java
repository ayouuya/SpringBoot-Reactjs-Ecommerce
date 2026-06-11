package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.AddCartItemRequest;
import com.cart.ecom_proj.dto.CartDto;
import com.cart.ecom_proj.dto.UpdateCartItemRequest;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/cart")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{cartKey}")
    public ResponseEntity<?> getCart(
            @PathVariable String cartKey,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            return new ResponseEntity<>(cartService.getCart(cartKey, currentUser), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{cartKey}/items")
    public ResponseEntity<?> addItem(
            @PathVariable String cartKey,
            @RequestBody AddCartItemRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            return new ResponseEntity<>(cartService.addItem(cartKey, request, currentUser), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{cartKey}/items/{productId}")
    public ResponseEntity<?> updateItem(
            @PathVariable String cartKey,
            @PathVariable int productId,
            @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            return new ResponseEntity<>(cartService.updateItem(cartKey, productId, request, currentUser), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{cartKey}/items/{productId}")
    public ResponseEntity<?> removeItem(
            @PathVariable String cartKey,
            @PathVariable int productId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            return new ResponseEntity<>(cartService.removeItem(cartKey, productId, currentUser), HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{cartKey}")
    public ResponseEntity<?> clearCart(
            @PathVariable String cartKey,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        try {
            cartService.clearCart(cartKey, currentUser);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
