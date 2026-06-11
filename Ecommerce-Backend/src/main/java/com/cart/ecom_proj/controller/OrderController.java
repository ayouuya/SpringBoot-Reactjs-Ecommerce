package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.OrderDto;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        List<OrderDto> orders = orderService.getOrdersForUser(currentUser.getEmail());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
