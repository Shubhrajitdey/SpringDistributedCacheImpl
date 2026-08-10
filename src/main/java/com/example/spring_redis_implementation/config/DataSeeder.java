package com.example.spring_redis_implementation.config;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.spring_redis_implementation.repository.ProductRepository;
import com.example.spring_redis_implementation.entity.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner{
    private final ProductRepository productRepository;

    @Override
    public void run(String...args){
        if (productRepository.count() == 0) {
            log.info("Seeding initial product data into MySQL...");
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                products.add(Product.builder()
                        .name("Product " + i)
                        .description("Description for Product " + i)
                        .price(99.99 * i)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            productRepository.saveAll(products);
            log.info("Successfully seeded 10 products!");
        } else {
            log.info("Product database already seeded.");
        }
    }
}
