# Inventory API — Architecture

## 1. Purpose

This document describes the software architecture for version 1 of the Inventory API.

The architecture is designed to keep the project:

- Easy to understand
- Easy to test
- Explicit about business rules
- Decoupled from infrastructure details
- Suitable for incremental evolution
- Representative of professional Java / Spring Boot backend development

The project intentionally avoids unnecessary complexity such as microservices, event brokers, distributed transactions and Spring Security in version 1.

---

# 2. Architectural Style

The application will be implemented as a **modular monolith** using Spring Boot.

The codebase will follow a layered dependency flow:

```text
HTTP Client
    │
    ▼
Controller
    │
    ▼
Application Service
    │
    ├──────────────► Repository
    │
    └──────────────► External Service Interface
                           │
                           ▼
                    Geoapify Adapter
```

The application remains a single deployable unit, but responsibilities are separated by feature and layer.

This gives the project clear boundaries without introducing the operational complexity of microservices.

---

# 3. Main Architectural Principles

## 3.1 Separation of Responsibilities

Each layer has a specific responsibility:

### Controller

Responsible for:

- Receiving HTTP requests
- Validating request DTOs
- Delegating operations to application services
- Returning response DTOs
- Selecting the appropriate HTTP status code

Controllers must not contain business logic.

---

### Application Service

Responsible for:

- Implementing use cases
- Enforcing business rules
- Coordinating repositories
- Coordinating external integrations
- Defining transaction boundaries
- Mapping domain errors to application exceptions when necessary

Examples:

```text
Create product
Update warehouse address
Register stock movement
Deactivate supplier
Physically delete product
```

---

### Repository

Responsible for:

- Persisting and retrieving domain entities
- Executing database queries
- Applying locking strategies when required

Repositories will be Spring Data JPA interfaces.

---

### Domain Model

Responsible for representing the core business concepts:

```text
Product
Category
Supplier
ProductSupplier
Warehouse
Address
Stock
StockMovement
MovementType
```

Business invariants that naturally belong to an object should be kept close to that object.

---

### External Integration Adapter

Responsible for communicating with external systems.

Version 1 contains one external integration:

```text
Geoapify
```

The rest of the application must not depend directly on Geoapify-specific classes or response formats.

---

# 4. Dependency Direction

Dependencies should point toward application/domain abstractions rather than toward concrete infrastructure implementations.

Example:

```text
WarehouseController
        │
        ▼
WarehouseService
   <<interface>>
        ▲
        │ implements
WarehouseServiceImpl
        │
        ├────────► WarehouseRepository
        │
        ▼
GeocodingService
   <<interface>>
        ▲
        │ implements
GeoapifyGeocodingService
```

`WarehouseServiceImpl` knows that an address must be validated and normalized.

It does **not** need to know how Geoapify's HTTP API works.

This allows the external provider to be replaced without modifying the warehouse use-case logic.

---

# 5. Package Structure

The project will use **package by feature**, with internal separation of responsibilities.

Proposed structure:

```text
com.example.inventory
│
├── common
│   ├── exception
│   ├── web
│   └── config
│
├── product
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── category
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── supplier
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── warehouse
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── inventory
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
└── geocoding
    ├── client
    ├── dto
    ├── config
    └── service
```

This structure is preferred over a single global:

```text
controller/
service/
repository/
entity/
```

because each business capability remains grouped together as the project grows.

---

# 6. Application Services

Interfaces will be used when they define meaningful application contracts.

They are not introduced simply to create an `Impl` class for every Java class.

Initial service contracts:

```text
ProductService
CategoryService
SupplierService
ProductSupplierService
WarehouseService
StockQueryService
InventoryService
GeocodingService
```

---

## 6.1 ProductService

Responsibilities:

- Create products
- Retrieve products
- List/filter products
- Update products
- Activate/deactivate products
- Validate physical deletion rules
- Physically delete products when allowed

Implementation:

```text
ProductServiceImpl
```

Dependencies:

```text
ProductRepository
CategoryRepository
ProductMapper
```

---

## 6.2 CategoryService

Responsibilities:

- Create categories
- Retrieve categories
- List categories
- Update categories

Implementation:

```text
CategoryServiceImpl
```

Dependencies:

```text
CategoryRepository
CategoryMapper
```

---

## 6.3 SupplierService

Responsibilities:

- Create suppliers
- Retrieve/list suppliers
- Update suppliers
- Activate/deactivate suppliers
- Validate physical deletion rules
- Physically delete suppliers when allowed

Implementation:

```text
SupplierServiceImpl
```

Dependencies:

```text
SupplierRepository
SupplierMapper
```

---

## 6.4 ProductSupplierService

Responsibilities:

- Associate suppliers with products
- Validate duplicate relationships
- Update supplier-specific purchase price
- List suppliers for a product
- Remove product-supplier relationships when allowed

Implementation:

```text
ProductSupplierServiceImpl
```

Dependencies:

```text
ProductRepository
SupplierRepository
ProductSupplierRepository
```

---

## 6.5 WarehouseService

Responsibilities:

- Create warehouses
- Retrieve/list warehouses
- Update warehouse metadata
- Validate and update warehouse addresses
- Activate/deactivate warehouses
- Validate physical deletion rules
- Physically delete warehouses when allowed

Implementation:

```text
WarehouseServiceImpl
```

Dependencies:

```text
WarehouseRepository
WarehouseMapper
GeocodingService
```

---

## 6.6 StockQueryService

`StockQueryService` is read-oriented.

Responsibilities:

- Retrieve stock by ID
- Retrieve stock across warehouses for a product
- Retrieve stock inside a warehouse
- Retrieve low-stock records
- Update minimum stock configuration

Implementation:

```text
StockQueryServiceImpl
```

Dependencies:

```text
StockRepository
StockMapper
```

Stock quantity itself is never directly changed by this service.

---

## 6.7 InventoryService

`InventoryService` contains the main inventory business logic.

Responsibilities:

- Register stock movements
- Create stock automatically on first inbound movement
- Validate available quantity for outbound movements
- Modify stock quantity
- Persist stock movement history
- Guarantee transactional consistency

Implementation:

```text
InventoryServiceImpl
```

Dependencies:

```text
ProductRepository
WarehouseRepository
StockRepository
StockMovementRepository
StockMovementMapper
```

This is one of the most important services in the application.

---

# 7. Repository Layer

Repositories will extend Spring Data JPA repository abstractions.

Initial repositories:

```text
ProductRepository
CategoryRepository
SupplierRepository
ProductSupplierRepository
WarehouseRepository
StockRepository
StockMovementRepository
```

Typical examples:

```java
interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
}
```

```java
interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
```

Database constraints remain authoritative even when the application performs pre-checks.

For example:

```text
UNIQUE product.sku
UNIQUE supplier.email
UNIQUE warehouse.code
UNIQUE (product_id, supplier_id)
UNIQUE (product_id, warehouse_id)
```

Application-level checks improve error messages.

Database constraints guarantee integrity.

---

# 8. Stock Transaction Model

Registering a stock movement is an atomic operation.

Conceptually:

```text
BEGIN TRANSACTION

1. Validate Product
2. Validate Warehouse
3. Find Stock
4. Create Stock if required and movement is inbound
5. Validate quantity
6. Modify Stock
7. Save StockMovement

COMMIT
```

If any step fails:

```text
ROLLBACK
```

The stock quantity and movement history must never become inconsistent.

The application service method responsible for this use case will define the transaction boundary.

Example:

```java
@Transactional
public StockMovementResponse registerMovement(CreateStockMovementRequest request) {
    ...
}
```

---

# 9. Concurrent Stock Updates

Inventory operations can occur concurrently.

Example:

```text
Current stock = 5

Request A → SALE 4
Request B → SALE 4
```

Without concurrency control, both requests could read `5` and both succeed.

That would violate:

```text
stock >= 0
```

Version 1 will therefore use database-level concurrency protection when loading a stock record for modification.

Recommended strategy:

```text
PESSIMISTIC_WRITE
```

Conceptually:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Stock> findForUpdate(...);
```

The lock is acquired inside the inventory transaction.

The unique database constraint on:

```text
(product_id, warehouse_id)
```

also protects against duplicate stock records.

Concurrency behavior must be covered by integration tests where practical.

---

# 10. Warehouse Address Validation

`Address` is a Value Object owned by `Warehouse`.

It has no independent database identity.

Conceptually:

```java
@Entity
class Warehouse {

    @Embedded
    private Address address;
}
```

```java
@Embeddable
class Address {
    ...
}
```

The application does not trust free-form geographic data directly.

Address creation/update flow:

```text
HTTP Request
    │
    ▼
WarehouseController
    │
    ▼
WarehouseService
    │
    ▼
GeocodingService
    │
    ▼
GeoapifyGeocodingService
    │
    ▼
Geoapify API
```

Geoapify returns normalized geographic information.

That information is converted into the application's own `Address` Value Object.

---

# 11. External Geocoding Abstraction

The application-level abstraction:

```text
GeocodingService
<<interface>>
```

Example contract:

```java
Address validateAndNormalize(AddressInput input);
```

Infrastructure implementation:

```text
GeoapifyGeocodingService
```

The implementation is responsible for:

- Building HTTP requests
- Sending requests to Geoapify
- Parsing provider-specific responses
- Mapping those responses to application objects
- Translating provider errors into application exceptions

Possible exceptions:

```text
AddressNotResolvedException
AddressProviderUnavailableException
```

No controller or business service should depend on Geoapify-specific DTOs.

---

# 12. External Call and Transaction Boundaries

External HTTP calls should not keep a database transaction open unnecessarily.

Warehouse creation flow:

```text
1. Validate input
2. Call Geoapify
3. Obtain normalized Address
4. Persist Warehouse
```

Warehouse address update flow:

```text
1. Validate input
2. Call Geoapify
3. Obtain normalized Address
4. Update existing Warehouse
```

If Geoapify fails:

```text
No database update occurs.
```

This avoids holding database locks while waiting for an external network service.

Stock operations are different: they require a database transaction because multiple persistence operations must remain atomic.

---

# 13. HTTP Client

Geoapify communication will use Spring's HTTP client facilities.

The provider URL and API key must be externalized:

```text
GEOAPIFY_BASE_URL
GEOAPIFY_API_KEY
```

Secrets must never be committed to Git.

The repository will include:

```text
.env.example
```

with placeholder values only.

---

# 14. DTO Boundary

Persistence entities must never be exposed directly through controllers.

Flow:

```text
Request JSON
    │
    ▼
Request DTO
    │
    ▼
Application Service
    │
    ▼
Domain Entity
    │
    ▼
Repository
```

Response flow:

```text
Domain Entity
    │
    ▼
Mapper
    │
    ▼
Response DTO
    │
    ▼
JSON
```

Benefits:

- Persistence implementation is hidden from API clients
- API contracts can evolve independently from entities
- Sensitive/internal fields are not accidentally exposed
- Validation belongs at the API boundary
- Serialization problems with JPA relationships are avoided

---

# 15. Mapping Strategy

Version 1 will use explicit mapper classes rather than exposing entities directly.

Examples:

```text
ProductMapper
CategoryMapper
SupplierMapper
WarehouseMapper
StockMapper
StockMovementMapper
```

Initial implementation can use manual mapping.

This keeps the project transparent while reviewing Java fundamentals.

A mapping library such as MapStruct may be evaluated in a later project.

---

# 16. Validation Strategy

Validation is divided into two categories.

## 16.1 Structural Validation

Handled at DTO level using Jakarta Bean Validation.

Examples:

```text
@NotBlank
@NotNull
@Email
@Positive
@PositiveOrZero
@Size
```

Examples:

```text
Product name cannot be blank.
Movement quantity must be greater than zero.
Supplier email must have a valid format.
```

---

## 16.2 Business Validation

Handled by application services.

Examples:

```text
SKU must be unique.
Supplier email must be unique.
Product must exist.
Warehouse must exist.
Stock must be sufficient.
Adjustment reason is mandatory.
Physical deletion must preserve history.
Address must be externally validated.
```

Database constraints provide a final integrity layer.

---

# 17. Exception Handling

Controllers should not individually construct error responses.

A global exception handler will centralize HTTP error mapping.

Proposed component:

```text
GlobalExceptionHandler
@RestControllerAdvice
```

It will convert application exceptions into `ProblemDetail`.

Examples:

```text
ProductNotFoundException
        ↓
404 Not Found
```

```text
DuplicateSkuException
        ↓
409 Conflict
```

```text
InsufficientStockException
        ↓
409 Conflict
```

```text
AddressNotResolvedException
        ↓
422 Unprocessable Entity
```

```text
AddressProviderUnavailableException
        ↓
503 Service Unavailable
```

Bean Validation failures:

```text
MethodArgumentNotValidException
        ↓
400 Bad Request
```

---

# 18. Database Architecture

Database:

```text
MySQL 8
```

ORM:

```text
Spring Data JPA / Hibernate
```

Schema migrations:

```text
Flyway
```

Hibernate must not be responsible for production schema evolution.

Recommended configuration:

```text
ddl-auto = validate
```

Flyway migrations define the schema.

Example migration structure:

```text
src/main/resources/db/migration/

V1__create_categories.sql
V2__create_products.sql
V3__create_suppliers.sql
V4__create_product_suppliers.sql
V5__create_warehouses.sql
V6__create_stocks.sql
V7__create_stock_movements.sql
V8__add_indexes.sql
```

The final migration order may change during implementation.

---

# 19. Database Indexes

Indexes should support both integrity and common query patterns.

Initial candidates:

```text
products.sku                          UNIQUE
suppliers.email                      UNIQUE
warehouses.code                      UNIQUE

product_suppliers(product_id,
                  supplier_id)       UNIQUE

stocks(product_id,
       warehouse_id)                 UNIQUE

products.category_id                 INDEX

stocks.warehouse_id                  INDEX
stocks.product_id                    INDEX

stock_movements.stock_id             INDEX
stock_movements.created_at           INDEX
stock_movements.type                 INDEX
```

Indexes will be reviewed against actual repository queries.

---

# 20. Testing Architecture

The test suite will contain multiple levels.

## 20.1 Unit Tests

Tools:

```text
JUnit 5
Mockito
AssertJ
```

Focus:

```text
Application service business rules
Movement calculations
Deletion rules
Validation behavior
Mapper behavior when useful
```

Unit tests do not start the Spring context unless necessary.

---

## 20.2 Repository Integration Tests

Tools:

```text
Spring Boot Test
Testcontainers
MySQL container
```

Focus:

```text
JPA mappings
Queries
Constraints
Indexes when behavior depends on them
Locking behavior
```

Tests must use MySQL rather than H2 so the test database behaves like the production database engine.

---

## 20.3 API Integration Tests

Tools:

```text
Spring Boot Test
Testcontainers
REST Assured
```

Focus:

```text
HTTP contract
Serialization
Validation
Status codes
ProblemDetail responses
Transactional behavior
```

---

## 20.4 External Provider Tests

`WarehouseService` tests should mock:

```text
GeocodingService
```

They should not call Geoapify over the internet.

Separate adapter tests may verify mapping of representative Geoapify responses using local fixtures or a mocked HTTP server.

CI must not depend on Geoapify availability.

---

# 21. Container Architecture

Local execution:

```text
Docker Compose
```

Initial services:

```text
inventory-api
mysql
```

Conceptually:

```text
┌───────────────────┐
│   Inventory API   │
│   Spring Boot     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│      MySQL 8      │
└───────────────────┘

Inventory API ──────► Geoapify API
```

The MySQL container will include health checks.

The application should wait for the database to become healthy before startup when managed by Docker Compose.

---

# 22. Configuration

Configuration should be environment-based.

Examples:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD

GEOAPIFY_BASE_URL
GEOAPIFY_API_KEY
```

Spring profiles may separate:

```text
local
test
```

Production secrets are never stored in the repository.

---

# 23. API Documentation

OpenAPI documentation will be generated using Springdoc OpenAPI.

Development environment:

```text
/swagger-ui.html
```

The documentation must describe:

- Endpoints
- Request DTOs
- Response DTOs
- Validation rules
- Status codes
- Error responses

The OpenAPI contract should remain consistent with `api-design.md`.

---

# 24. Observability

The API should expose basic operational information through Spring Boot Actuator.

Initial endpoints may include:

```text
/actuator/health
/actuator/info
```

Sensitive actuator endpoints must not be unnecessarily exposed.

Application logs should include enough context to diagnose failures without logging secrets or API keys.

---

# 25. CI Architecture

GitHub Actions will execute the automated quality pipeline.

Initial workflow:

```text
Checkout
   ↓
Set up Java
   ↓
Build
   ↓
Run tests
   ↓
Generate coverage report
```

Testcontainers will start MySQL automatically during integration tests.

The CI environment must not require a locally installed MySQL server.

---

# 26. Coverage

JaCoCo will generate test coverage reports.

Coverage is used as a quality signal, not as a target to maximize artificially.

Business-critical paths should receive priority:

```text
Stock movement rules
Insufficient stock
Automatic stock creation
Deletion restrictions
Address validation behavior
Unique business identifiers
```

---

# 27. Main Components

## Controllers

```text
ProductController
CategoryController
SupplierController
ProductSupplierController
WarehouseController
StockController
StockMovementController
```

## Service Interfaces

```text
ProductService
CategoryService
SupplierService
ProductSupplierService
WarehouseService
StockQueryService
InventoryService
GeocodingService
```

## Service Implementations

```text
ProductServiceImpl
CategoryServiceImpl
SupplierServiceImpl
ProductSupplierServiceImpl
WarehouseServiceImpl
StockQueryServiceImpl
InventoryServiceImpl
GeoapifyGeocodingService
```

## Repositories

```text
ProductRepository
CategoryRepository
SupplierRepository
ProductSupplierRepository
WarehouseRepository
StockRepository
StockMovementRepository
```

## Domain

```text
Product
Category
Supplier
ProductSupplier
Warehouse
Address
Stock
StockMovement
MovementType
```

## Cross-Cutting Components

```text
GlobalExceptionHandler
OpenApiConfig
GeoapifyConfig
```

---

# 28. Architecture Diagram

A visual architecture diagram should be stored in:

```text
docs/diagrams/application-architecture.drawio
docs/diagrams/application-architecture.svg
```

Recommended elements for the draw.io diagram:

```text
Client

ProductController
CategoryController
SupplierController
WarehouseController
StockController
StockMovementController

ProductService
CategoryService
SupplierService
WarehouseService
StockQueryService
InventoryService

Repositories

MySQL

GeocodingService
GeoapifyGeocodingService
Geoapify API
```

Recommended dependency direction:

```text
Client
  ↓
Controllers
  ↓
Service Interfaces
  ↓
Service Implementations
  ↓
Repositories
  ↓
MySQL

WarehouseServiceImpl
  ↓
GeocodingService
  △
  │ implements
GeoapifyGeocodingService
  ↓
Geoapify API
```

This diagram should represent software dependencies, not domain cardinalities. Domain relationships remain documented in `inventory-domain.drawio`.

---

# 29. Key Architecture Decisions

## ADR-001 — Modular Monolith

Version 1 is implemented as a modular monolith.

Reason:

- Simple deployment
- Clear module boundaries
- No current requirement for independent scaling
- Avoids unnecessary distributed-system complexity

---

## ADR-002 — Package by Feature

Code is grouped primarily by business feature.

Reason:

- Better cohesion
- Easier navigation
- Features remain easier to extract or evolve later

---

## ADR-003 — DTOs Separate from Entities

JPA entities are never exposed directly through the API.

Reason:

- Protect persistence boundaries
- Avoid accidental serialization
- Allow independent API evolution

---

## ADR-004 — MySQL + Flyway

MySQL is the persistence engine and Flyway owns schema migrations.

Reason:

- Explicit, version-controlled database evolution
- Reproducible environments
- Production-style schema management

---

## ADR-005 — Address as Value Object

`Address` is embedded in `Warehouse`.

Reason:

- Address has no identity independent from the warehouse
- Its meaning is defined by its values
- Its lifecycle belongs to the warehouse

---

## ADR-006 — External Geocoding Behind Interface

Geoapify is accessed through `GeocodingService`.

Reason:

- Dependency Inversion
- Provider replaceability
- Easier automated testing
- No Geoapify DTO leakage into the domain/application layers

---

## ADR-007 — Stock Changes Only Through Movements

Direct stock quantity updates are forbidden.

Reason:

- Auditability
- Business-rule centralization
- Historical traceability

---

## ADR-008 — Transactional Inventory Operations

Stock updates and movement persistence share one database transaction.

Reason:

- Prevent inconsistent stock and history

---

## ADR-009 — Pessimistic Locking for Stock Mutation

Stock records used by outbound/inbound concurrent operations are loaded with write locking.

Reason:

- Prevent lost updates
- Prevent concurrent operations from producing negative stock

---

## ADR-010 — Manual Mapping in Version 1

DTO/entity mapping begins with explicit Java mappers.

Reason:

- Keeps behavior visible
- Reinforces Java fundamentals
- Avoids introducing another abstraction before it is needed

---

# 30. Out of Scope Architectural Concerns

Version 1 intentionally excludes:

```text
Spring Security
JWT
OAuth2
Redis
Kafka
RabbitMQ
Microservices
Distributed tracing
Kubernetes
Multi-tenancy
CQRS
Event sourcing
```

These topics are better demonstrated in later portfolio projects where they solve an actual architectural need.
