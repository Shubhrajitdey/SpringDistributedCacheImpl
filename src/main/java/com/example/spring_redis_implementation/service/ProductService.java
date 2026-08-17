package com.example.spring_redis_implementation.service;

import org.springframework.stereotype.Service;

import com.example.spring_redis_implementation.repository.ProductRepository;
import com.example.spring_redis_implementation.dto.ProductRequest;
import com.example.spring_redis_implementation.entity.Product;
import com.example.spring_redis_implementation.exception.ProductNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    @Cacheable(value = "products_all", key = "#root.method.name")
    public List<Product> getAllProducts(){
        log.info("Fetching all products from MySQL database ......");
        simulateSlowDbCall();
        return productRepository.findAll();
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id){
        log.info("Fetching product by id from MySQL database ......");
        simulateSlowDbCall();
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }
    
    public Product createProduct(ProductRequest request) {
        log.info("Saving new product to MySQL database...");
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
        return productRepository.save(product);
    }

    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "products_all",key ="getAllProducts")
    public Product updateProduct(Long id, ProductRequest request) {
        log.info("Updating product ID {} in MySQL database...", id);
        Product product = getProductById(id);
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != 0.0) {
            product.setPrice(request.getPrice());
        }
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product ID {} from MySQL database...", id);
        productRepository.deleteById(id);
    }


    private void simulateSlowDbCall(){
        try{
            Thread.sleep(5000);
        }catch(InterruptedException e){
            log.error("Error while simulating slow DB call", e);
        }
    }
}
