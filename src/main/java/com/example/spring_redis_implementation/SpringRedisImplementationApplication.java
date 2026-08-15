package com.example.spring_redis_implementation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringRedisImplementationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRedisImplementationApplication.class, args);
	}

}
