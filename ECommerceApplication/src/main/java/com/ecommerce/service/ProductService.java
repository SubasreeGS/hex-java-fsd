package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(Product product) {
        productRepository.save(product);

    }

    public Product getProductById(long id)
    {
         return productRepository.getProductById(id);
    }

    public boolean updateStock(Long productId, int newQuantity) {
        int rowsUpdated = productRepository.updateStock(productId, newQuantity);
        return rowsUpdated > 0;
    }

    public Map<String, Integer> countProductsByVendor() {
        return productRepository.countProductsByVendor();
    }
}
