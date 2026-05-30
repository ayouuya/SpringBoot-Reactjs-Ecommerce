package com.cart.ecom_proj.service;

import com.cart.ecom_proj.model.Product;
import com.cart.ecom_proj.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo repo;


    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById(int id){
        return repo.findById(id).orElse(null);
    }

    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        product.setImageName(imageFile.getOriginalFilename());
        product.setImageType(imageFile.getContentType());
        product.setImageDate(imageFile.getBytes());
        return repo.save(product);
    }

    public Product updateProduct(int id, Product product, MultipartFile imageFile) throws IOException {
        Product existing = repo.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        product.setId(id);
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageDate(imageFile.getBytes());
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
        } else {
            product.setImageDate(existing.getImageDate());
            product.setImageName(existing.getImageName());
            product.setImageType(existing.getImageType());
        }
        return repo.save(product);
    }

    public void deleteProduct(int id) {
        repo.deleteById(id);
    }

    public Product updateStock(int id, int stockQuantity) {
        Product existing = repo.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setStockQuantity(stockQuantity);
        existing.setProductAvailable(stockQuantity > 0);
        return repo.save(existing);
    }

    public List<Product> searchProducts(String keyword) {
        return repo.searchProducts(keyword);
    }
}
