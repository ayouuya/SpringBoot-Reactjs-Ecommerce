package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.AuthRequest;
import com.cart.ecom_proj.dto.AuthResponse;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepo userRepo;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public AuthResponse login(AuthRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        AppUser user = userRepo.findByEmailIgnoreCase(request.email())
                .orElseGet(AppUser::new);

        boolean isNew = user.getId() == null;
        if (isNew) {
            user.setEmail(request.email().trim());
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (isNew) {
            user.setRole(parseRole(request.role()));
        }

        AppUser saved = userRepo.save(user);
        return new AuthResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
    }

    private UserRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            return UserRole.USER;
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
