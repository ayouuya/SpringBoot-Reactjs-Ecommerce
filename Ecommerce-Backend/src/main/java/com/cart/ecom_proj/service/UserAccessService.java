package com.cart.ecom_proj.service;

import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserAccessService {

    private final UserRepo userRepo;

    public UserAccessService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public AppUser requireUser(String email, String roleValue, UserRole requiredRole) {
        AppUser user = requireUser(email, roleValue);
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new SecurityException("Access denied.");
        }
        return user;
    }

    public AppUser requireUser(String email, String roleValue) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Login required.");
        }

        UserRole role = parseRole(roleValue);
        AppUser user = userRepo.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != role) {
            throw new SecurityException("Invalid role.");
        }

        return user;
    }

    public void requireAdmin(String email, String roleValue) {
        requireUser(email, roleValue, UserRole.ADMIN);
    }

    private UserRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
