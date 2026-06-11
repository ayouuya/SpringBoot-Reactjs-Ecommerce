package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.AuthRequest;
import com.cart.ecom_proj.dto.AuthResponse;
import com.cart.ecom_proj.dto.RegisterRequest;
import com.cart.ecom_proj.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthenticationException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
