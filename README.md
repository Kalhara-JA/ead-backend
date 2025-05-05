# EAD Microservices Backend

This repository contains the backend implementation for an e-commerce platform using a microservices architecture. The system is built with Java (Spring Boot), and each service is responsible for a specific domain. The services communicate via REST and Kafka (with Avro serialization).

## Services Overview

- **Order Service**: Handles order placement, status updates, and order history.
- **Product Service**: Manages product catalog, including CRUD operations.
- **Inventory Service**: Manages inventory stock, warehouses, and stock checks.
- **Notification Service**: Listens to Kafka events (e.g., order placed/cancelled) and sends notifications.
- **API Gateway**: Aggregates APIs and provides a single entry point.

## Technologies Used

- Java 17+
- Spring Boot
- Spring Data JPA & MongoDB
- Kafka (with Avro serialization)
- Testcontainers (for integration testing)
- Resilience4j (circuit breaker, retry, timeout)
- OpenAPI/Swagger (API documentation)
- MySQL & MongoDB
- Docker (for local development)

## Project Structure

```
ead-backend/
├── api-gateway/
├── inventory-service/
├── notification-service/
├── order-service/
├── product-service/
```

Each service is a standalone Spring Boot application.

## Key Features

- **Order Placement**: Orders are placed via `/api/v1/orders` (Order Service), which checks inventory and emits Kafka events.
- **Product Management**: Products can be created, updated, listed, and deleted (Product Service).
- **Inventory Management**: Inventory is tracked per SKU and warehouse, with endpoints for stock checks and restocking.
- **Warehouse Management**: CRUD operations for warehouses (Inventory Service).
- **Notifications**: Kafka events (e.g., `OrderPlacedEvent`, `OrderCancelEvent`) are consumed for sending notifications.
- **API Gateway**: Unified API entry point with Swagger aggregation.

## Running Locally

1. **Prerequisites**:  
   - Java 17+  
   - Docker (for MySQL, MongoDB, Kafka, Schema Registry)  
   - Maven

2. **Start Dependencies** (example using Docker Compose):

   ```sh
   docker-compose up -d
   ```

3. **Build and Run Each Service**:

   ```sh
   cd order-service
   ./mvnw spring-boot:run
   # Repeat for other services
   ```

4. **Access API Documentation**:  
   - Each service exposes Swagger UI at `/swagger-ui.html` (e.g., `http://localhost:8081/swagger-ui.html`).

## Testing

- Integration tests use Testcontainers for isolated database and Kafka environments.
- Example test: `OrderServiceApplicationTests` in the order-service module.

## Configuration

- Each service has its own `application.properties` for DB, Kafka, and other settings.
- Kafka Avro schemas are in `order-service/target/classes/avro/`.

## Example API Endpoints

- **Order Service**:  
  - `POST /api/v1/orders` – Place an order  
  - `GET /api/v1/orders/{id}` – Get order details

- **Product Service**:  
  - `POST /api/v1/products` – Add a product  
  - `GET /api/v1/products` – List products

- **Inventory Service**:  
  - `POST /api/v1/inventory/check-stock` – Check stock for SKUs  
  - `POST /api/v1/inventory/increment-stock` – Restock items  
  - `GET /api/v1/inventory/warehouse` – List warehouses

## Avro Event Schemas

- **OrderPlacedEvent**:  
  - Fields: `orderNumber`, `email`, `firstName`, `lastName`
- **OrderCancelEvent**:  
  - Fields: `orderNumber`, `email`

## Contributing

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the Apache 2.0 License.

---

*For details, see each service's README or Swagger UI.*
