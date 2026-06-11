package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.AddCartItemRequest;
import com.cart.ecom_proj.dto.CartDto;
import com.cart.ecom_proj.dto.CartItemDto;
import com.cart.ecom_proj.dto.UpdateCartItemRequest;
import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.Cart;
import com.cart.ecom_proj.model.CartItem;
import com.cart.ecom_proj.model.Product;
import com.cart.ecom_proj.repo.CartRepo;
import com.cart.ecom_proj.repo.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CartService {

    private static final String CURRENCY = "MAD";

    private final CartRepo cartRepo;
    private final ProductRepo productRepo;

    public CartService(CartRepo cartRepo, ProductRepo productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public CartDto getCart(String cartKey, AppUser user) {
        Cart cart = getOrCreateCart(cartKey, user);
        return toDto(cart);
    }

    public CartDto addItem(String cartKey, AddCartItemRequest request, AppUser user) {
        validateCartKey(cartKey);
        if (request == null || request.productId() <= 0) {
            throw new IllegalArgumentException("Valid productId is required.");
        }
        int quantity = request.quantity() <= 0 ? 1 : request.quantity();

        Cart cart = getOrCreateCart(cartKey, user);
        Product product = productRepo.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (!product.isProductAvailable()) {
            throw new IllegalArgumentException("Product is not available.");
        }

        CartItem existing = findItem(cart, product.getId());
        int newQuantity = (existing == null ? 0 : existing.getQuantity()) + quantity;
        if (newQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds stock.");
        }

        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cart.getItems().add(item);
        } else {
            existing.setQuantity(newQuantity);
        }

        return toDto(cartRepo.save(cart));
    }

    public CartDto updateItem(String cartKey, int productId, UpdateCartItemRequest request, AppUser user) {
        validateCartKey(cartKey);
        if (request == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        Cart cart = getOrCreateCart(cartKey, user);
        CartItem existing = findItem(cart, productId);
        if (existing == null) {
            throw new IllegalArgumentException("Cart item not found.");
        }

        Product product = existing.getProduct();
        if (!product.isProductAvailable()) {
            throw new IllegalArgumentException("Product is not available.");
        }
        if (request.quantity() > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds stock.");
        }

        existing.setQuantity(request.quantity());
        return toDto(cartRepo.save(cart));
    }

    public CartDto removeItem(String cartKey, int productId, AppUser user) {
        validateCartKey(cartKey);
        Cart cart = getOrCreateCart(cartKey, user);
        CartItem existing = findItem(cart, productId);
        if (existing == null) {
            throw new IllegalArgumentException("Cart item not found.");
        }

        cart.getItems().remove(existing);
        return toDto(cartRepo.save(cart));
    }

    public void clearCart(String cartKey, AppUser user) {
        validateCartKey(cartKey);
        Cart cart = getOrCreateCart(cartKey, user);
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    private Cart getOrCreateCart(String cartKey, AppUser user) {
        validateCartKey(cartKey);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        return cartRepo.findByUserEmailIgnoreCase(user.getEmail())
                .orElseGet(() -> createCartForUser(cartKey, user));
    }

    private Cart createCartForUser(String cartKey, AppUser user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartKey(resolveCartKey(cartKey, user));
        return cartRepo.save(cart);
    }

    private String resolveCartKey(String requestedCartKey, AppUser user) {
        String normalizedKey = requestedCartKey.trim();
        if (!cartRepo.existsByCartKey(normalizedKey)) {
            return normalizedKey;
        }
        return "cart-user-" + user.getId();
    }

    private void validateCartKey(String cartKey) {
        if (cartKey == null || cartKey.isBlank()) {
            throw new IllegalArgumentException("cartKey is required.");
        }
    }

    private CartItem findItem(Cart cart, int productId) {
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId() == productId) {
                return item;
            }
        }
        return null;
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            itemCount += item.getQuantity();
            subtotal = subtotal.add(lineTotal);

            items.add(new CartItemDto(
                    product.getId(),
                    product.getName(),
                    product.getBrand(),
                    product.getCategory(),
                    item.getQuantity(),
                    unitPrice,
                    lineTotal,
                    product.isProductAvailable(),
                    product.getStockQuantity(),
                    product.getImageUrl()
            ));
        }

        return new CartDto(
                cart.getCartKey(),
                items,
                subtotal,
                subtotal,
                itemCount,
                CURRENCY
        );
    }
}
