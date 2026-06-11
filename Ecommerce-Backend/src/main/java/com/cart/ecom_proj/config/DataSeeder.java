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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DataSeeder {

    private static final int PRODUCTS_PER_CATEGORY = 15;
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";
    private static final String DEFAULT_USER_PASSWORD = "User@12345";
    private static final String BROKEN_LAPTOP_IMAGE =
            "https://images.unsplash.com/photo-1517336714739-489689fd1ca8?auto=format&fit=crop&w=1200&q=80";
    private static final String LAPTOP_IMAGE_REPLACEMENT =
            "https://images.unsplash.com/photo-1484788984921-03950022c9ef?auto=format&fit=crop&w=1200&q=80";

    private final Random random = new Random(42L);

    @Bean
    CommandLineRunner seedDatabase(
            ProductRepo productRepo,
            UserRepo userRepo,
            CartRepo cartRepo,
            OrderRepo orderRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            List<Product> catalog;
            if (productRepo.count() < 120 && cartRepo.count() == 0 && orderRepo.count() == 0) {
                productRepo.deleteAll();
                catalog = seedCatalog(productRepo);
            } else {
                catalog = productRepo.findAll();
            }
            repairCatalogImages(productRepo, catalog);

            ensureUser(
                    userRepo,
                    "Admin DigiTech",
                    "admin@digitech.ma",
                    DEFAULT_ADMIN_PASSWORD,
                    UserRole.ADMIN,
                    passwordEncoder
            );
            AppUser sara = ensureUser(
                    userRepo,
                    "Sara El Idrissi",
                    "sara@digitech.ma",
                    DEFAULT_USER_PASSWORD,
                    UserRole.USER,
                    passwordEncoder
            );
            AppUser yassine = ensureUser(
                    userRepo,
                    "Yassine Benali",
                    "yassine@digitech.ma",
                    DEFAULT_USER_PASSWORD,
                    UserRole.USER,
                    passwordEncoder
            );

            if (cartRepo.count() == 0 && catalog.size() >= 62) {
                Cart saraCart = new Cart();
                saraCart.setUser(sara);
                saraCart.setCartKey("cart-user-" + sara.getId());
                saraCart.getItems().add(cartItem(saraCart, catalog.get(2), 1));
                saraCart.getItems().add(cartItem(saraCart, catalog.get(17), 2));
                cartRepo.save(saraCart);

                Cart yassineCart = new Cart();
                yassineCart.setUser(yassine);
                yassineCart.setCartKey("cart-user-" + yassine.getId());
                yassineCart.getItems().add(cartItem(yassineCart, catalog.get(34), 1));
                yassineCart.getItems().add(cartItem(yassineCart, catalog.get(61), 1));
                cartRepo.save(yassineCart);
            }

            if (orderRepo.count() == 0 && catalog.size() >= 49) {
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

                OrderItem orderItemOne = orderItem(order, catalog.get(0), 1);
                OrderItem orderItemTwo = orderItem(order, catalog.get(48), 1);
                order.getItems().addAll(List.of(orderItemOne, orderItemTwo));

                BigDecimal subtotal = orderItemOne.getLineTotal().add(orderItemTwo.getLineTotal());
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
            }
        };
    }

    private List<Product> seedCatalog(ProductRepo productRepo) {
        List<CategorySeed> categorySeeds = buildCategorySeeds();
        List<Product> products = new ArrayList<>();

        for (CategorySeed seed : categorySeeds) {
            for (int i = 0; i < PRODUCTS_PER_CATEGORY; i++) {
                products.add(productRepo.save(buildProduct(seed, i)));
            }
        }

        return products;
    }

    private void repairCatalogImages(ProductRepo productRepo, List<Product> catalog) {
        List<Product> repaired = catalog.stream()
                .filter(product -> BROKEN_LAPTOP_IMAGE.equals(product.getImageUrl()))
                .peek(product -> product.setImageUrl(LAPTOP_IMAGE_REPLACEMENT))
                .toList();

        if (!repaired.isEmpty()) {
            productRepo.saveAll(repaired);
        }
    }

    private Product buildProduct(CategorySeed seed, int index) {
        String brand = pick(seed.brands(), index);
        String family = pick(seed.families(), index);
        String trim = pick(List.of("Core", "Plus", "Pro", "Max", "Ultra"), index);
        String color = pick(List.of("Graphite", "Sand", "Silver", "Ocean", "Forest"), index);
        int generation = 2024 + (index % 3);

        Product product = new Product();
        product.setName(brand + " " + family + " " + trim + " " + generation);
        product.setBrand(brand);
        product.setCategory(seed.category());
        product.setDescription(buildDescription(seed, brand, family, trim, color, index));
        product.setPrice(randomPrice(seed.minPriceMad(), seed.maxPriceMad()));

        int stock = randomBetween(6, 120);
        product.setStockQuantity(stock);
        product.setProductAvailable(stock > 0);
        product.setReleaseDate(Date.valueOf(LocalDate.now().minusDays(randomBetween(15, 720))));
        product.setImageUrl(pick(seed.imageUrls(), index));
        product.setImageName(slugify(product.getName()) + ".jpg");
        product.setImageType("image/jpeg");
        product.setImageDate(null);
        return product;
    }

    private String buildDescription(
            CategorySeed seed,
            String brand,
            String family,
            String trim,
            String color,
            int index
    ) {
        String featureOne = pick(seed.features(), index);
        String featureTwo = pick(seed.features(), index + 2);
        String featureThree = pick(seed.features(), index + 4);
        String useCase = pick(seed.useCases(), index);

        return brand + " " + family + " " + trim + " is a realistic " + seed.category().toLowerCase()
                + " product prepared for a premium e-commerce catalog. It delivers " + featureOne + ", "
                + featureTwo + ", and " + featureThree + " in a refined " + color.toLowerCase()
                + " finish, making it a strong fit for " + useCase + ".";
    }

    private AppUser user(
            String fullName,
            String email,
            String rawPassword,
            UserRole role,
            PasswordEncoder passwordEncoder
    ) {
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }

    private AppUser ensureUser(
            UserRepo userRepo,
            String fullName,
            String email,
            String rawPassword,
            UserRole role,
            PasswordEncoder passwordEncoder
    ) {
        AppUser appUser = userRepo.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        appUser.setFullName(fullName);
        appUser.setEmail(email.toLowerCase());
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setRole(role);
        return userRepo.save(appUser);
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

    private BigDecimal randomPrice(int minMad, int maxMad) {
        int step = minMad < 1000 ? 25 : 50;
        int raw = randomBetween(minMad, maxMad);
        int normalized = Math.max(minMad, (raw / step) * step);
        return BigDecimal.valueOf(normalized).setScale(2, RoundingMode.HALF_UP);
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt((max - min) + 1);
    }

    private String slugify(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String pick(List<String> values, int index) {
        return values.get(Math.floorMod(index, values.size()));
    }

    private List<CategorySeed> buildCategorySeeds() {
        return List.of(
                new CategorySeed(
                        "Electronique",
                        1599,
                        12999,
                        List.of("AeroTech", "Nova", "Zenith", "Voltis", "Keystone"),
                        List.of("NovaBook 14", "VisionView 27", "UltraPad 11", "SmartHub Mini", "OfficeDock 8K"),
                        List.of("an IPS or OLED display", "fast SSD storage", "solid battery endurance", "stable Wi-Fi 6 connectivity", "premium aluminum construction"),
                        List.of("hybrid work", "university projects", "daily productivity", "business travel", "content streaming"),
                        List.of(
                                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=80",
                                LAPTOP_IMAGE_REPLACEMENT,
                                "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1587614382346-4ec70e388b28?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Smartphones",
                        2499,
                        8999,
                        List.of("Atlas", "Pulse", "Aster", "Orbit", "Luma"),
                        List.of("One 5G", "View Pro", "Edge Lite", "Flex Max", "Pocket Neo"),
                        List.of("a bright AMOLED panel", "multi-day battery life", "fast charging support", "stabilized photo capture", "responsive 5G performance"),
                        List.of("mobile photography", "social media usage", "remote work", "navigation on the go", "video streaming"),
                        List.of(
                                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1605236453806-6ff36851218e?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Audio",
                        399,
                        3499,
                        List.of("WaveBeat", "SonicAir", "EchoLine", "StudioBox", "SoundNest"),
                        List.of("ANC Headphones", "True Wireless Buds", "Portable Speaker", "Soundbar 2.1", "Studio Headset"),
                        List.of("active noise reduction", "balanced sound tuning", "low-latency Bluetooth", "all-day comfort", "clear call microphones"),
                        List.of("commuting", "office calls", "gaming sessions", "home entertainment", "music discovery"),
                        List.of(
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Gaming",
                        299,
                        5999,
                        List.of("Rogue", "HyperGrid", "LevelUp", "Photon", "Arena"),
                        List.of("Mechanical Keyboard", "Precision Mouse", "Quad HD Monitor", "Pro Controller", "RGB Headset"),
                        List.of("fast response times", "ergonomic control", "customizable RGB lighting", "durable switch hardware", "immersive session comfort"),
                        List.of("competitive play", "casual gaming", "stream setups", "late-night sessions", "desk battlestations"),
                        List.of(
                                "https://images.unsplash.com/photo-1593305841991-05c297ba4575?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1603481546579-65d935ba9cdd?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Accessoires",
                        99,
                        1499,
                        List.of("UrbanCarry", "VoltPack", "NorthRoute", "ModLoop", "CableLab"),
                        List.of("Laptop Backpack", "MagSafe Charger", "USB-C Hub", "Power Bank", "Travel Case"),
                        List.of("clean cable management", "compact everyday portability", "fast device top-ups", "smart organization pockets", "robust build quality"),
                        List.of("daily commuting", "short business trips", "campus life", "remote work kits", "mobile productivity"),
                        List.of(
                                "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1523170335258-f5ed11844a49?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1601593346740-925612772716?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Vetements",
                        249,
                        1899,
                        List.of("Northline", "Atelier", "Drift", "UrbanStitch", "Mistral"),
                        List.of("Runner Sneakers", "City Hoodie", "Softshell Jacket", "Daily Tee", "Weekend Tote"),
                        List.of("comfortable materials", "careful stitching", "easy layering", "versatile daily styling", "lightweight wearability"),
                        List.of("city walks", "weekend trips", "casual office outfits", "student life", "everyday errands"),
                        List.of(
                                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Photo",
                        899,
                        12499,
                        List.of("Optix", "SkyFrame", "PixelCraft", "FocusLab", "CaptureOne"),
                        List.of("Mirrorless Camera", "Action Cam", "4K Drone", "Creator Tripod", "Prime Lens Kit"),
                        List.of("sharp optical detail", "stabilized capture", "creator-friendly controls", "reliable outdoor handling", "fast autofocus behavior"),
                        List.of("travel content", "portrait work", "social video", "weekend adventures", "brand shoots"),
                        List.of(
                                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1502982720700-bfff97f2ecac?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1473968512647-3e447244af8f?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1512790182412-b19e6d62bc39?auto=format&fit=crop&w=1200&q=80"
                        )
                ),
                new CategorySeed(
                        "Maison",
                        349,
                        5499,
                        List.of("HomeBrew", "PureAir", "CookNest", "BrightRoom", "CleanWave"),
                        List.of("Coffee Maker", "Air Fryer", "Robot Vacuum", "Desk Lamp", "Smart Humidifier"),
                        List.of("simple daily automation", "energy-conscious operation", "easy maintenance", "clean countertop aesthetics", "practical family sizing"),
                        List.of("small apartments", "family kitchens", "home office corners", "daily routines", "gift-ready setups"),
                        List.of(
                                "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1493666438817-866a91353ca9?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=1200&q=80",
                                "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1200&q=80"
                        )
                )
        );
    }

    private record CategorySeed(
            String category,
            int minPriceMad,
            int maxPriceMad,
            List<String> brands,
            List<String> families,
            List<String> features,
            List<String> useCases,
            List<String> imageUrls
    ) {
    }
}
