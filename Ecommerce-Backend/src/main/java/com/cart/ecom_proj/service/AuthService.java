package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.AuthRequest;
import com.cart.ecom_proj.dto.AuthResponse;
import com.cart.ecom_proj.dto.RegisterRequest;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.repo.UserRepo;
import com.cart.ecom_proj.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration payload is required.");
        }

        String fullName = normalizeName(request.fullName());
        String email = normalizeEmail(request.email());
        String password = normalizePassword(request.password());

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("An account already exists with this email.");
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        AppUser saved = userRepo.save(user);
        String token = jwtService.generateToken(saved);
        return AuthResponse.from(saved, token);
    }

    public AuthResponse login(AuthRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login payload is required.");
        }

        String email = normalizeEmail(request.email());
        String password = normalizePassword(request.password());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        AppUser user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));
        String token = jwtService.generateToken(user);
        return AuthResponse.from(user, token);
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return value.trim().toLowerCase();
    }

    private String normalizePassword(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (value.trim().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        return value.trim();
    }
}
