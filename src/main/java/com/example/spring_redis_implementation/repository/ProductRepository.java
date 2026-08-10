package com.example.spring_redis_implementation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_redis_implementation.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    
}
