package com.cart.ecom_proj.repo;

import com.cart.ecom_proj.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
	List<Order> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
