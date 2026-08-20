# Inventory API

Production-oriented REST API for managing products, suppliers, warehouses and inventory across multiple physical locations.

[![CI](https://github.com/burgosfacundo/inventory-api/actions/workflows/ci.yml/badge.svg)](https://github.com/burgosfacundo/inventory-api/actions/workflows/ci.yml)

> ✅ **Status: Version 1 complete**

Inventory API is a portfolio backend built with **Java 25 and Spring Boot 4.1**. It goes beyond CRUD by modeling transactional stock movements, warehouse transfers, concurrency control, external address validation, database migrations, integration testing with a real MySQL instance, containerized demo execution, OpenAPI documentation and automated CI quality checks.

---

## ✨ Highlights

- Product and category management
- Supplier management
- Product-supplier relationships with purchase prices
- Multiple warehouses with validated addresses
- Stock tracked independently per product and warehouse
- Traceable `IN` and `OUT` inventory movements
- Low-stock monitoring
- Transactional stock transfers between warehouses
- Pessimistic locking for concurrent stock-changing operations
- Flyway-managed MySQL schema
- Geoapify address validation behind an application abstraction
- Zero-configuration Docker demo without Geoapify credentials
- OpenAPI / Swagger UI
- Spring Boot Actuator health and info endpoints
- Unit, controller, repository and API integration tests
- Testcontainers with real MySQL
- JaCoCo coverage enforcement
- GitHub Actions CI

---

## 🧩 Domain Model

Main domain concepts:

- `Category`
- `Product`
- `Supplier`
- `ProductSupplier`
- `Warehouse`
- `Address` — Value Object
- `Stock`
- `InventoryMovement`
- `MovementType`
- `StockTransfer`

### UML

![Inventory Domain Model](docs/diagrams/inventory-domain.jpg)

---

## 🏗️ Architecture

The application is a **modular monolith** organized by feature under:

```text
com.burgosfacundo.inventory
```

Main request flow:

```text
Controller
    ↓
Application Service
    ↓
Repository
    ↓
MySQL
```

Each feature groups its own controllers, DTOs, models, mappers, repositories, services and exceptions.

External address validation follows the same dependency direction:

```text
WarehouseService
       ↓
AddressValidator
       ↑
GeoapifyAddressValidator
DemoAddressValidator
```

`WarehouseService` depends on the application abstraction, not directly on Geoapify.

Stock-changing operations are handled transactionally. Inventory movements use pessimistic row locking, while stock transfers acquire stock locks in deterministic warehouse-ID order to reduce concurrency issues and deadlock risk.

For the complete design, see [Architecture](docs/architecture.md).

---

## 📦 Core Business Rules

- Product SKU must be unique.
- Supplier email must be unique.
- Warehouse code must be unique.
- A product-supplier pair may exist only once.
- Stock is unique per `Product + Warehouse`.
- Stock quantity and minimum stock cannot be negative.
- Stock quantity is not modified through a generic stock update endpoint.
- `IN` movements increase stock.
- The first valid `IN` movement creates the stock record automatically when necessary with `minimumStock = 0`.
- `OUT` movements require existing and sufficient stock.
- Inventory movement creation and stock modification occur atomically.
- Stock transfers require different source and destination warehouses.
- A successful transfer decreases source stock, increases destination stock, stores a `StockTransfer` and creates matching `OUT` and `IN` inventory movements in one transaction.
- Warehouse addresses are validated through `AddressValidator` before persistence.
- Database unique constraints, foreign keys and check constraints provide the final integrity layer.
- Physical deletion is subject to database referential integrity.

---

## 🛠️ Tech Stack

### Backend

- Java 25
- Spring Boot 4.1
- Spring MVC
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- Maven
- Lombok

### Database

- MySQL 8.4
- Flyway

### Testing & Quality

- JUnit
- Mockito
- Spring MVC Test
- Testcontainers
- REST Assured
- JaCoCo
- Maven Surefire
- Maven Failsafe

### API & Infrastructure

- OpenAPI / Swagger UI
- Spring Boot Actuator
- Docker
- Docker Compose
- GitHub Actions

### External Integration

- Geoapify

---

## 🌐 API

All application endpoints are versioned under:

```text
/api/v1
```

Main resources:

```text
/api/v1/categories
/api/v1/products
/api/v1/suppliers
/api/v1/product-suppliers
/api/v1/warehouses
/api/v1/stocks
/api/v1/inventory-movements
/api/v1/stock-transfers
```

### API documentation

After starting the application:

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| OpenAPI YAML | http://localhost:8080/v3/api-docs.yaml |
| Health | http://localhost:8080/actuator/health |
| Application info | http://localhost:8080/actuator/info |

Swagger UI is the recommended way to explore and execute requests against the API.

The complete HTTP contract is also documented in [API Design](docs/api-design.md).

---

## 🔄 Inventory Operations

### Register an inbound movement

```http
POST /api/v1/inventory-movements
Content-Type: application/json
```

```json
{
  "productId": 1,
  "warehouseId": 1,
  "type": "IN",
  "quantity": 20
}
```

### Register an outbound movement

```json
{
  "productId": 1,
  "warehouseId": 1,
  "type": "OUT",
  "quantity": 5
}
```

If available stock is insufficient, the operation is rejected and stock remains unchanged.

### Transfer stock between warehouses

```http
POST /api/v1/stock-transfers
Content-Type: application/json
```

```json
{
  "productId": 1,
  "sourceWarehouseId": 1,
  "destinationWarehouseId": 2,
  "quantity": 5
}
```

A successful transfer atomically:

1. decreases source stock
2. increases or creates destination stock
3. stores an `OUT` inventory movement
4. stores an `IN` inventory movement
5. stores the `StockTransfer`

---

## ⚠️ Error Handling

The API uses Spring `ProblemDetail` and returns errors as:

```text
application/problem+json
```

Application error responses include an `errorCode` extension.

Example:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Insufficient stock. Available: 3, requested: 5",
  "instance": "/api/v1/inventory-movements",
  "errorCode": "INSUFFICIENT_STOCK"
}
```

The centralized exception layer handles validation errors, malformed requests, missing resources, conflicts, address-validation failures, external-service failures and unexpected server errors.

---

## 🐳 Quick Start — Docker Demo

### Requirements

- Docker
- Docker Compose

Clone the repository and start the complete environment:

```bash
git clone https://github.com/burgosfacundo/inventory-api.git
cd inventory-api
docker compose up --build
```

The Docker environment starts:

- Inventory API
- MySQL 8.4

The application becomes available at:

```text
http://localhost:8080
```

### Zero-configuration demo

Docker activates the Spring `demo` profile.

No Geoapify API key or `.env` file is required.

In demo mode:

- warehouse address validation runs locally through `DemoAddressValidator`
- no external Geoapify HTTP request is made
- submitted address data is preserved
- placeholder coordinates are used
- Flyway loads representative demo data

The demo dataset includes:

- Categories
- Products
- Suppliers
- Product-supplier relationships
- Warehouses
- Stocks
- Inventory movements
- Stock transfers

This makes the API immediately explorable through Swagger UI.

### Stop the environment

```bash
docker compose down
```

To also remove the MySQL volume and recreate the database from scratch on the next start:

```bash
docker compose down -v
```

---

## 🌍 Running with Geoapify

Outside the `demo` profile, warehouse creation and updates use the real Geoapify-backed `AddressValidator`.

Use `.env.example` as the reference for the required configuration.

The integration validates Geoapify results before creating the application's `Address` Value Object and rejects unresolved or insufficient-confidence addresses.

---

## 🧪 Testing Strategy

The test suite is divided by responsibility.

- **Domain and unit tests** verify local business rules.
- **Service tests** verify application orchestration and business behavior.
- **Controller tests** verify HTTP contracts, validation and error responses.
- **Repository integration tests** verify JPA behavior and database constraints against MySQL.
- **Transaction integration tests** verify stock consistency and rollback behavior.
- **API integration tests** use REST Assured against the running application.
- **Infrastructure integration tests** verify OpenAPI, Swagger and Actuator endpoints.

Integration tests use **Testcontainers with MySQL**, not an in-memory substitute.

### Maven lifecycle

Run the regular `*Test` suite through Surefire:

```bash
mvn test
```

Run the complete project verification:

```bash
mvn verify
```

`mvn verify` includes:

- regular tests through Maven Surefire
- `*IT` integration tests through Maven Failsafe
- Testcontainers-based MySQL integration tests
- combined JaCoCo coverage
- coverage threshold validation

Docker must be running for the complete integration test suite locally.

---

## 📊 Code Coverage

JaCoCo combines coverage from regular tests and integration tests.

HTML report:

```text
target/site/jacoco/index.html
```

The build enforces these project-wide minimums:

- **Line coverage:** 90%
- **Branch coverage:** 80%

If either threshold is not met, `mvn verify` fails.

Coverage is treated as a quality guard, not as a substitute for meaningful tests.

---

## 🔁 Continuous Integration

GitHub Actions automatically validates:

```text
pull requests → main
pushes → main
```

The CI pipeline:

```text
Checkout repository
        ↓
Set up Eclipse Temurin Java 25
        ↓
Cache Maven dependencies
        ↓
mvn --batch-mode verify
        ↓
Upload JaCoCo HTML report
```

The workflow runs on Ubuntu and verifies the project independently from the developer environment.

The JaCoCo HTML report is uploaded as a workflow artifact after the run.

---

## 📚 Documentation

Detailed project documentation is available under [`docs/`](docs):

- [Requirements](docs/requirements.md)
- [API Design](docs/api-design.md)
- [Architecture](docs/architecture.md)
- [Domain Model](docs/diagrams/inventory-domain.jpg)

The generated OpenAPI specification is the authoritative machine-readable HTTP contract.

---

## 🗺️ Version 1 Roadmap

- [x] Domain modeling
- [x] Functional and business requirements
- [x] REST API contract
- [x] Architecture definition
- [x] Spring Boot project setup
- [x] MySQL + Flyway configuration
- [x] Product and Category management
- [x] Supplier management
- [x] Product-Supplier management
- [x] Warehouse management
- [x] Geoapify integration
- [x] Stock management
- [x] Inventory movements and transactional rules
- [x] Warehouse stock transfers
- [x] Unit and controller tests
- [x] Integration tests with Testcontainers
- [x] REST API tests with REST Assured
- [x] Docker Compose
- [x] Zero-configuration Docker demo
- [x] OpenAPI / Swagger documentation
- [x] Spring Boot Actuator
- [x] GitHub Actions CI
- [x] JaCoCo coverage enforcement
- [x] Final documentation review

---

## 👨‍💻 Author

**Facundo Burgos**

- GitHub: [@burgosfacundo](https://github.com/burgosfacundo)
- LinkedIn: [linkedin.com/in/burgosfacundo](https://www.linkedin.com/in/burgosfacundo/)