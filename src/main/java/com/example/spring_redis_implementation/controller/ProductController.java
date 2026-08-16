package com.example.spring_redis_implementation.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.spring_redis_implementation.dto.ProductRequest;
import com.example.spring_redis_implementation.entity.Product;
import com.example.spring_redis_implementation.service.ProductService;
import com.example.spring_redis_implementation.service.RateLimiterService;
import com.example.spring_redis_implementation.exception.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final RateLimiterService rateLimiterService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        boolean allowed = rateLimiterService.isAllowed(clientIp, 2, 60);
        if (!allowed) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        boolean allowed = rateLimiterService.isAllowedSlidingWindow(clientIp, 2, 60);
        if (!allowed) {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }

        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Deleted product ID: " + id);
    }
}
