package com.cart.ecom_proj.controller;

import com.cart.ecom_proj.dto.UpdateStockRequest;
import com.cart.ecom_proj.model.Product;
import com.cart.ecom_proj.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(){
        return new ResponseEntity<>(service.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id){

        Product product = service.getProductById(id);

        if(product != null)
            return new ResponseEntity<>(product, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product")
    public ResponseEntity<?> addProduct(
            @RequestPart Product product,
            @RequestPart(required = false) MultipartFile imageFile
    ) {
        try {
            Product product1 = service.addProduct(product, imageFile);
            return new ResponseEntity<>(product1, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("product/{productId}/image")
    public ResponseEntity<?> getImageByProductId(@PathVariable int productId){

        Product product = service.getProductById(productId);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, URI.create(product.getImageUrl()).toString())
                    .build();
        }

        if (product.getImageDate() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] imageFile = product.getImageDate();

        return ResponseEntity.ok()
            .contentType(product.getImageType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.valueOf(product.getImageType()))
                .body(imageFile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/product/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable int id,
                                                @RequestPart Product product,
                                                @RequestPart(required = false) MultipartFile imageFile){

        Product product1 = null;
        try {
            product1 = service.updateProduct(id, product, imageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (product1 != null)
            return new ResponseEntity<>("Updated", HttpStatus.OK);
        else
            return new ResponseEntity<>("Failed to update", HttpStatus.BAD_REQUEST);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/product/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id,
                                         @RequestBody UpdateStockRequest request) {
        if (request == null) {
            return new ResponseEntity<>("Stock payload is required", HttpStatus.BAD_REQUEST);
        }

        Product updated = service.updateStock(id, request.stockQuantity());
        if (updated == null) {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id){
        Product product = service.getProductById(id);

        if (product != null) {
            service.deleteProduct(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword){
        List<Product> products = service.searchProducts(keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}
