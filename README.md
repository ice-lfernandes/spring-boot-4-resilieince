# Spring Boot Resilience API

A comprehensive demonstration of resilience patterns in Spring Boot 3 using Resilience4j, implementing Circuit Breaker, Rate Limiter, Retry, Bulkhead, and Time Limiter patterns for a robust e-commerce API.

## Overview

This project demonstrates how to build resilient microservices using Spring Boot and Resilience4j. It implements a complete e-commerce API with various resilience patterns to handle failures gracefully.

## Features

### Resilience Patterns Implemented

1. **Circuit Breaker** - Prevents cascading failures by opening circuit when failure threshold is reached
   - Payment Service: Protects against payment gateway failures
   - Inventory Service: Handles inventory service outages

2. **Rate Limiter** - Controls request rate to prevent system overload
   - Product Search: 100 requests per second
   - Product Detail: 500 requests per second
   - SMS Notifications: 10 requests per minute

3. **Retry** - Automatically retries failed operations with exponential backoff
   - Inventory Service: 4 attempts with exponential backoff
   - Payment Service: 2 attempts

4. **Bulkhead** - Isolates resources to prevent thread pool exhaustion
   - Email Notifications: Thread pool bulkhead with 5 max threads
   - SMS Notifications: Semaphore bulkhead with 5 concurrent calls

5. **Time Limiter** - Sets timeout for asynchronous operations
   - Payment Service: 5 seconds timeout
   - Inventory Service: 3 seconds timeout

## Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Resilience4j 2.2.0** - Resilience patterns implementation
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Spring WebFlux** - Reactive programming support
- **Micrometer** - Metrics and observability
- **Prometheus** - Metrics export
- **Lombok** - Reduce boilerplate code

## Project Structure

```
src/main/java/com/ecommerce/
├── ResilienceApplication.java       # Main application class
├── config/
│   ├── ApplicationConfig.java       # WebClient and JPA configuration
│   └── MetricsConfig.java          # Metrics configuration
├── controller/
│   ├── CheckoutController.java     # Checkout endpoint
│   ├── ProductController.java      # Product endpoints with rate limiting
│   └── ResilienceHealthController.java # Health check endpoint
├── dto/
│   ├── CheckoutRequest.java
│   ├── CheckoutResponse.java
│   ├── ErrorResponse.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── Product.java
│   ├── RateLimiterStatus.java
│   ├── ResilienceHealth.java
│   └── StockResponse.java
├── entity/
│   └── Order.java                  # Order entity
├── exception/
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java # Global exception handling
│   ├── InsufficientStockException.java
│   ├── InventoryServiceException.java
│   └── ProductNotFoundException.java
├── listener/
│   └── CircuitBreakerEventListener.java # Circuit breaker monitoring
├── repository/
│   └── OrderRepository.java
└── service/
    ├── CheckoutService.java        # Orchestrates checkout flow
    ├── InventoryService.java       # Stock checking with retry
    ├── NotificationService.java    # Email/SMS with bulkhead
    ├── PaymentService.java         # Payment with circuit breaker
    └── ProductService.java         # Product search and retrieval
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Product APIs

```bash
# Search products (Rate limited: 100 req/s)
GET /api/products/search?query=laptop&page=0

# Get product by ID (Rate limited: 500 req/s)
GET /api/products/{id}
```

### Checkout API

```bash
# Process checkout
POST /api/checkout
Content-Type: application/json

{
  "cartId": "cart-123",
  "customerId": 1,
  "productId": 1,
  "quantity": 2,
  "totalAmount": 2599.98,
  "paymentMethod": "CREDIT_CARD"
}
```

### Health and Monitoring

```bash
# Resilience health check
GET /api/health/resilience

# Actuator endpoints
GET /actuator/health
GET /actuator/metrics
GET /actuator/prometheus
GET /actuator/circuitbreakers
GET /actuator/ratelimiters
```

## Configuration

All resilience configurations are in `src/main/resources/application.yml`:

### Circuit Breaker Configuration
- `slidingWindowSize`: 10 calls
- `minimumNumberOfCalls`: 5 calls
- `failureRateThreshold`: 50%
- `waitDurationInOpenState`: 10-15 seconds

### Retry Configuration
- `maxAttempts`: 3-4 attempts
- `waitDuration`: 500ms-1s
- `exponentialBackoff`: 2x multiplier

### Rate Limiter Configuration
- Product Search: 100 requests/second
- Product Detail: 500 requests/second
- SMS: 10 requests/minute

### Bulkhead Configuration
- Email: 5 max threads, queue capacity 20
- SMS: 5 max concurrent calls

## Monitoring with Prometheus

The application exposes Prometheus metrics at `/actuator/prometheus`. You can visualize them using Grafana.

### Sample Prometheus Queries

```promql
# Circuit breaker state
resilience4j_circuitbreaker_state{name="paymentService"}

# Success rate
rate(resilience4j_circuitbreaker_calls_total{kind="successful"}[5m])
/ rate(resilience4j_circuitbreaker_calls_total[5m])

# Rate limiter available permissions
resilience4j_ratelimiter_available_permissions{name="productSearch"}

# Retry attempts
rate(resilience4j_retry_calls_total{kind="successful_with_retry"}[5m])
```

## Testing the Resilience Patterns

### Test Circuit Breaker

The circuit breaker will open after 5 failed calls with 50% failure rate:

```bash
# Make multiple requests to trigger circuit breaker
for i in {1..10}; do
  curl http://localhost:8080/api/checkout -X POST \
    -H "Content-Type: application/json" \
    -d '{"cartId":"test","customerId":1,"productId":1,"quantity":1,"totalAmount":100}'
done
```

### Test Rate Limiter

Make rapid requests to hit rate limit:

```bash
# This should trigger rate limiting
for i in {1..150}; do
  curl http://localhost:8080/api/products/search?query=laptop
done
```

### Check Resilience Status

```bash
curl http://localhost:8080/api/health/resilience | jq
```

## Key Features

### Fallback Mechanisms

- **Payment Service**: Returns "PENDING_RETRY" status when circuit is open
- **Inventory Service**: Returns estimated stock based on cache
- **Product Service**: Returns cached product data on rate limit

### Event Listeners

Circuit breaker state changes are logged and can trigger alerts:
- State transitions (CLOSED → OPEN → HALF_OPEN)
- Error events
- Success events

### Global Exception Handling

Centralized exception handling for:
- `CallNotPermittedException` - Circuit breaker open (503)
- `RequestNotPermitted` - Rate limit exceeded (429)
- `InsufficientStockException` - Out of stock (409)
- `BusinessException` - Business errors (400)

## Best Practices Implemented

1. **Timeout Configuration**: Realistic timeouts based on service SLAs
2. **Minimum Calls**: Circuit breaker needs sufficient data (5 calls)
3. **Meaningful Fallbacks**: User-friendly messages, not errors
4. **Comprehensive Monitoring**: Prometheus metrics for all patterns
5. **Gradual Rollout**: Configuration supports feature flags

## Lessons Learned

- Don't set timeouts too aggressively
- Circuit breakers need minimum calls to make decisions
- Always implement meaningful fallback methods
- Monitor metrics to tune configurations
- Test resilience patterns in production with feature flags

## Database

The application uses H2 in-memory database. Access the console at:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Contributing

This is a demonstration project based on the article "Spring Boot and Resilience: How I Made My APIs More Robust with the New Features".

## License

This project is for educational purposes.

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Micrometer Documentation](https://micrometer.io/)
