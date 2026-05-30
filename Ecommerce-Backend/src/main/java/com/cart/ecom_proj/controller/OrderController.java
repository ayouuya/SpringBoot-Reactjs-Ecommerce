package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.OrderDto;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.service.OrderService;
import com.cart.ecom_proj.service.UserAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserAccessService userAccessService;

    public OrderController(OrderService orderService, UserAccessService userAccessService) {
        this.orderService = orderService;
        this.userAccessService = userAccessService;
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            AppUser user = userAccessService.requireUser(email, role, UserRole.USER);
            List<OrderDto> orders = orderService.getOrdersForUser(user.getEmail());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-USER-EMAIL", required = false) String email,
            @RequestHeader(value = "X-USER-ROLE", required = false) String role
    ) {
        try {
            userAccessService.requireAdmin(email, role);
            OrderDto order = orderService.getOrder(orderId);
            if (order == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (SecurityException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
