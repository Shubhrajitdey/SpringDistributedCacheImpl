# 🚀 Spring Boot Redis Distributed Caching & Rate Limiting

A production-grade Spring Boot application demonstrating **Distributed Caching**, **Rate Limiting (Fixed Window & Sliding Window)**, and **Per-Cache Custom TTLs** using Redis and MySQL.

---

## 📚 What You Will Learn (Step-by-Step Breakdown)

### 1️⃣ Distributed Caching with Redis & JSON Serialization
* **Spring Cache Annotations**: `@Cacheable`, `@CachePut`, and `@CacheEvict`.
* **JSON Serialization**: Configured `RedisSerializer.json()` with Jackson `ObjectMapper` instead of raw JDK binary serialization (`\xac\xed...`), making cached values human-readable in Redis CLI.
* **Cache Eviction Strategy**: Evicting list caches (`products_all`) when single items are updated or deleted to prevent stale data.

### 2️⃣ Fixed Window Rate Limiting (`INCR` + `EXPIRE`)
* **Atomic Counters**: Using `StringRedisTemplate.opsForValue().increment(key)` to count requests per client IP.
* **Expiration Handling**: Setting key TTL on the 1st request to automatically reset the rate-limit window.
* **Distributed Safety**: Enforcing rate limits across scaled application nodes via a centralized Redis store instead of local Java memory.

### 3️⃣ Sliding Window Rate Limiting using Redis Sorted Sets (`ZSET`)
* **Edge-Burst Prevention**: Using timestamps as scores in Redis `ZSET` (`removeRangeByScore`, `zCard`, `add`) to solve fixed-window burst spikes.
* **Production Memory Spam Protection**: Checking request counts **before** calling `zSet.add(...)` so spam/bot attacks do not bloat Redis RAM during active bursts.

### 4️⃣ Per-Method & Per-Cache Custom TTL Configuration
* **Flexible Cache Durations**: Using `RedisCacheManager.builder().withInitialCacheConfigurations(...)`.
* **Custom vs Default TTLs**:
  * Default Fallback TTL: **10 minutes** for general caches (`product_detail`).
  * Custom TTL: **1 minute** for dynamic list caches (`products_all`).

### 5️⃣ Global Exception Handling & REST Best Practices
* Custom exceptions (`ProductNotFoundException`, `RateLimitExceededException`).
* `@RestControllerAdvice` mapping rate limit breaches to **HTTP 429 Too Many Requests**.

---

## 🛠️ Low-Level Design (LLD) Patterns Used

| Design Pattern | Where It Is Used | Purpose |
| :--- | :--- | :--- |
| **Strategy Pattern** | `RedisSerializer` (`StringRedisSerializer`, `RedisSerializer.json()`) | Interchangeable serialization algorithms for keys and values. |
| **Builder Pattern** | `RedisCacheConfiguration.defaultCacheConfig().entryTtl(...)` | Clean, fluent object construction without complex constructors. |
| **Static Factory Method** | `RedisSerializer.json()`, `defaultCacheConfig()` | Encapsulates complex Jackson serializer initialization. |
| **Immutable Value Object** | `RedisCacheConfiguration` | Thread-safe configuration state across Spring beans. |

---

## ⚙️ Prerequisites

Before running the application, make sure you have installed:
* **Java 17+**
* **Maven 3.8+**
* **MySQL 8.0+**
* **Docker** (for running Redis)

---

## 🏃 How to Run the Application

### Step 1: Clone the Repository
```bash
git clone https://github.com/Shubhrajitdey/SpringDistributedCacheImpl.git
cd SpringDistributedCacheImpl
```

### Step 2: Start Redis Container
Run a Redis container mapped to port `6379`:
```bash
docker run -d --name redis-container -p 6379:6379 redis:latest
```

### Step 3: Configure Database
Ensure MySQL is running on `localhost:3306` with a database named `root` (or update `src/main/resources/application.yml` with your credentials):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/root?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: password
```

### Step 4: Build and Run
```bash
mvn clean package
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

---

## 🧪 Testing the Application

### 1️⃣ Test Rate Limiting (Fixed or Sliding Window)
Send requests rapidly using `curl`:
```bash
for i in {1..5}; do curl -i http://localhost:8080/api/products; done
```
* **Requests 1–3**: Returns `200 OK` with JSON products.
* **4th Request onwards**: Returns `429 Too Many Requests`:
```json
{
  "timestamp": "2026-08-17T13:00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Sliding window rate limit exceeded! Max 3 requests per 60 seconds."
}
```

### 2️⃣ Inspect Redis Keys via Redis CLI
Connect to your running Redis container:
```bash
docker exec -it redis-container redis-cli
```

* **Inspect Rate Limit Keys & TTL**:
  ```shell
  KEYS rate_limit_sliding:*
  TTL rate_limit_sliding:0:0:0:0:0:0:0:1
  ZRANGE rate_limit_sliding:0:0:0:0:0:0:0:1 0 -1 WITHSCORES
  ```

* **Inspect Cache Keys & TTL**:
  ```shell
  KEYS products*
  TTL products_all::getAllProducts
  ```

---

## 📌 Project Structure

```
src/main/java/com/example/spring_redis_implementation/
├── config/
│   ├── DataSeeder.java            # Initial DB seed data
│   └── RedisConfig.java           # Custom CacheManager, JSON Serializer & Per-Cache TTLs
├── controller/
│   └── ProductController.java     # REST API endpoints
├── dto/
│   └── ProductRequest.java        # Request DTO
├── entity/
│   └── Product.java               # JPA Entity
├── exception/
│   ├── GlobalExceptionHandler.java# Centralized REST Exception Handler (404, 429)
│   ├── ProductNotFoundException.java
│   └── RateLimitExceededException.java
├── repository/
│   └── ProductRepository.java     # Spring Data JPA Repository
└── service/
    ├── ProductService.java        # Cacheable product logic (@Cacheable, @CachePut, @CacheEvict)
    └── RateLimiterService.java     # Fixed & Sliding Window Rate Limiting logic
```
