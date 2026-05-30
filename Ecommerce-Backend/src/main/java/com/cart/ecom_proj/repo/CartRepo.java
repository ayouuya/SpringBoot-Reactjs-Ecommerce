package com.cart.ecom_proj.repo;

import com.cart.ecom_proj.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCartKey(String cartKey);
}
