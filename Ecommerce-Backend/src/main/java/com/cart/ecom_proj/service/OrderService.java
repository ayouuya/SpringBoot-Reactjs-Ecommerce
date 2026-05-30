package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.OrderDto;
import com.cart.ecom_proj.dto.OrderItemDto;
import com.cart.ecom_proj.model.Order;
import com.cart.ecom_proj.model.OrderItem;
import com.cart.ecom_proj.repo.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepo orderRepo;

    public OrderService(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) {
            return null;
        }
        return toDto(order);
    }

        @Transactional(readOnly = true)
        public List<OrderDto> getOrdersForUser(String email) {
        return orderRepo.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(email).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        }

        private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());

        return new OrderDto(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            items,
            order.getSubtotal(),
            order.getTotal(),
            order.getCurrency(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getCustomerPhone(),
            order.getShippingAddress(),
            order.getCreatedAt()
        );
        }

    private OrderItemDto toItemDto(OrderItem item) {
        return new OrderItemDto(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
