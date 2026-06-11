package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.*;
import com.cart.ecom_proj.model.*;
import com.cart.ecom_proj.repo.CartRepo;
import com.cart.ecom_proj.repo.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    private static final String CURRENCY = "MAD";

    private final CartRepo cartRepo;
    private final OrderRepo orderRepo;
    private final PaymentServiceRegistry paymentRegistry;

    public CheckoutService(CartRepo cartRepo, OrderRepo orderRepo, PaymentServiceRegistry paymentRegistry) {
        this.cartRepo = cartRepo;
        this.orderRepo = orderRepo;
        this.paymentRegistry = paymentRegistry;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request, AppUser user) {
        if (request == null || request.cartKey() == null || request.cartKey().isBlank()) {
            return new CheckoutResponse(false, "Cart key is required.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }
        if (request.paymentMethod() == null) {
            return new CheckoutResponse(false, "Payment method is required.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return new CheckoutResponse(false, "Authenticated user is required.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }

        Cart cart = cartRepo.findByUserEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        if (cart.getItems().isEmpty()) {
            return new CheckoutResponse(false, "Cart is empty.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }

        CustomerDto customer = request.customer();
        if (customer == null || isBlank(customer.fullName()) || isBlank(customer.email()) || isBlank(customer.address())) {
            return new CheckoutResponse(false, "Customer name, email, and address are required.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }

        if (!customer.email().equalsIgnoreCase(user.getEmail())) {
            return new CheckoutResponse(false, "Customer email does not match logged-in user.", null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isProductAvailable()) {
                return new CheckoutResponse(false, "Product is not available: " + product.getName(), null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
            }
            if (item.getQuantity() > product.getStockQuantity()) {
                return new CheckoutResponse(false, "Not enough stock for: " + product.getName(), null, null, OrderStatus.FAILED, PaymentStatus.DECLINED, CURRENCY);
            }
        }

        BigDecimal subtotal = calculateSubtotal(cart);
        PaymentRequest paymentRequest = new PaymentRequest(
                request.paymentMethod(),
                subtotal,
                CURRENCY,
                customer.email(),
                request.card(),
                request.paypal()
        );

        PaymentResult paymentResult = paymentRegistry.getService(request.paymentMethod()).process(paymentRequest);
        if (paymentResult.status() != PaymentStatus.APPROVED) {
            return new CheckoutResponse(false, paymentResult.message(), null, null, OrderStatus.FAILED, paymentResult.status(), CURRENCY);
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PAID);
        order.setCurrency(CURRENCY);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal);
        order.setCustomerName(customer.fullName());
        order.setCustomerEmail(customer.email());
        order.setCustomerPhone(customer.phone());
        order.setShippingAddress(customer.address());
        order.setCreatedAt(LocalDateTime.now());

        order.setUser(user);

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());

            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            order.getItems().add(orderItem);

            int updatedStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(updatedStock);
            product.setProductAvailable(updatedStock > 0);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.paymentMethod());
        payment.setStatus(paymentResult.status());
        payment.setReference(paymentResult.reference());
        payment.setAmount(subtotal);
        payment.setProviderMessage(paymentResult.message());
        payment.setCreatedAt(LocalDateTime.now());
        order.setPayment(payment);

        Order saved = orderRepo.save(order);
        cart.getItems().clear();
        cartRepo.save(cart);

        log.info("Order {} completed with payment {}", saved.getOrderNumber(), paymentResult.reference());

        return new CheckoutResponse(true, "Order confirmed.", saved.getId(), saved.getOrderNumber(), saved.getStatus(), paymentResult.status(), CURRENCY);
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return subtotal;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String generateOrderNumber() {
        return "DG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
