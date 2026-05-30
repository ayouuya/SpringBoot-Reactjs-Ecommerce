package com.cart.ecom_proj.config;

import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.Cart;
import com.cart.ecom_proj.model.CartItem;
import com.cart.ecom_proj.model.Order;
import com.cart.ecom_proj.model.OrderItem;
import com.cart.ecom_proj.model.OrderStatus;
import com.cart.ecom_proj.model.Payment;
import com.cart.ecom_proj.model.PaymentMethod;
import com.cart.ecom_proj.model.PaymentStatus;
import com.cart.ecom_proj.model.Product;
import com.cart.ecom_proj.model.UserRole;
import com.cart.ecom_proj.repo.CartRepo;
import com.cart.ecom_proj.repo.OrderRepo;
import com.cart.ecom_proj.repo.ProductRepo;
import com.cart.ecom_proj.repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            ProductRepo productRepo,
            UserRepo userRepo,
            CartRepo cartRepo,
            OrderRepo orderRepo
    ) {
        return args -> {
            if (productRepo.count() > 0 || userRepo.count() > 0 || cartRepo.count() > 0 || orderRepo.count() > 0) {
                return;
            }

            Product laptop = productRepo.save(product(
                    "DIGITECH NovaBook 14",
                    "Lightweight 14-inch laptop with 16GB RAM and 512GB SSD.",
                    "DIGITECH",
                    "Laptop",
                    "9499.00",
                    24,
                    true,
                    2024, 1, 15
            ));

            Product phone = productRepo.save(product(
                    "Atlas Pro 5G",
                    "6.7-inch AMOLED display, 128GB storage, 50MP triple camera.",
                    "Atlas",
                    "Mobile",
                    "5799.00",
                    38,
                    true,
                    2024, 2, 10
            ));

            Product headphones = productRepo.save(product(
                    "WaveBeat ANC",
                    "Wireless headphones with active noise cancellation and 30h battery.",
                    "WaveBeat",
                    "Headphone",
                    "1299.00",
                    60,
                    true,
                    2024, 2, 24
            ));

            Product homeHub = productRepo.save(product(
                    "Spark Home Hub",
                    "Smart home hub with voice control and multi-device support.",
                    "Spark",
                    "Electronics",
                    "899.00",
                    34,
                    true,
                    2024, 3, 5
            ));

            Product drone = productRepo.save(product(
                    "PixelPlay Drone Mini",
                    "Compact drone with 4K camera and stabilized flight.",
                    "PixelPlay",
                    "Toys",
                    "1599.00",
                    20,
                    true,
                    2024, 3, 20
            ));

            Product backpack = productRepo.save(product(
                    "UrbanFlex Backpack",
                    "Water-resistant backpack with laptop sleeve and USB port.",
                    "UrbanFlex",
                    "Fashion",
                    "499.00",
                    75,
                    true,
                    2024, 4, 2
            ));

            Product tablet = productRepo.save(product(
                    "AeroTab Plus",
                    "11-inch tablet built for media, notes, and light productivity.",
                    "Aero",
                    "Tablet",
                    "3299.00",
                    18,
                    true,
                    2024, 5, 11
            ));

            AppUser admin = userRepo.save(user("Admin DIGITECH", "admin@digitech.ma", UserRole.ADMIN));
            AppUser sara = userRepo.save(user("Sara El Idrissi", "sara@digitech.ma", UserRole.USER));
            AppUser yassine = userRepo.save(user("Yassine Benali", "yassine@digitech.ma", UserRole.USER));

            Cart saraCart = new Cart();
            saraCart.setCartKey("cart-sara");
            saraCart.getItems().add(cartItem(saraCart, headphones, 2));
            saraCart.getItems().add(cartItem(saraCart, backpack, 1));
            cartRepo.save(saraCart);

            Cart yassineCart = new Cart();
            yassineCart.setCartKey("cart-yassine");
            yassineCart.getItems().add(cartItem(yassineCart, tablet, 1));
            yassineCart.getItems().add(cartItem(yassineCart, homeHub, 1));
            cartRepo.save(yassineCart);

            Order order = new Order();
            order.setOrderNumber("DG-SEED-0001");
            order.setStatus(OrderStatus.PAID);
            order.setCurrency("MAD");
            order.setCustomerName(sara.getFullName());
            order.setCustomerEmail(sara.getEmail());
            order.setCustomerPhone("+212600000001");
            order.setShippingAddress("123 Boulevard Hassan II, Casablanca");
            order.setUser(sara);
            order.setCreatedAt(LocalDateTime.now().minusDays(2));

            OrderItem laptopItem = orderItem(order, laptop, 1);
            OrderItem phoneItem = orderItem(order, phone, 2);
            order.getItems().addAll(List.of(laptopItem, phoneItem));

            BigDecimal subtotal = laptopItem.getLineTotal().add(phoneItem.getLineTotal());
            order.setSubtotal(subtotal);
            order.setTotal(subtotal);

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod(PaymentMethod.CARD);
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setReference("PAY-SEED-0001");
            payment.setAmount(subtotal);
            payment.setProviderMessage("Seeded approved payment");
            payment.setCreatedAt(LocalDateTime.now().minusDays(2));
            order.setPayment(payment);

            orderRepo.save(order);
        };
    }

    private AppUser user(String fullName, String email, UserRole role) {
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private Product product(
            String name,
            String description,
            String brand,
            String category,
            String price,
            int stockQuantity,
            boolean available,
            int year,
            int month,
            int day
    ) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setBrand(brand);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        product.setProductAvailable(available);
        product.setReleaseDate(Date.valueOf(LocalDate.of(year, month, day)));
        product.setImageName(null);
        product.setImageType(null);
        product.setImageDate(null);
        return product;
    }

    private CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private OrderItem orderItem(Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
