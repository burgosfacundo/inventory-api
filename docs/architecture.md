# Inventory API — Architecture

## 1. Purpose

This document describes the implemented software architecture of version 1 of Inventory API.

The project is designed as a production-oriented portfolio backend that keeps business rules explicit while remaining understandable and testable.

The architecture favors:

- clear separation of responsibilities
- feature-oriented organization
- explicit transactional boundaries
- database-backed integrity
- replaceable external integrations
- automated testing against real infrastructure
- reproducible local execution with Docker
- automated verification through CI

The project intentionally remains a single deployable application rather than introducing microservices or distributed infrastructure.

---

## 2. Architectural Style

Inventory API is implemented as a **modular monolith** with Spring Boot.

The application is deployed as one process, but code is grouped by business feature and then internally separated by responsibility.

Typical request flow:

```text
HTTP Client
    │
    ▼
Controller
    │
    ▼
Application Service
    │
    ├────────────► Repository
    │
    ├────────────► Domain Model
    │
    └────────────► External Integration Abstraction
                         │
                         ▼
                  External Adapter
```

Persistence flow:

```text
Application Service
        │
        ▼
Spring Data JPA Repository
        │
        ▼
Hibernate
        │
        ▼
MySQL
```

Database schema evolution is owned by Flyway rather than Hibernate.

---

## 3. Technology Baseline

The implemented application uses:

### Runtime

- Java 25
- Spring Boot 4.1
- Spring MVC
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- Maven

### Persistence

- MySQL 8.4
- Flyway

### External integration

- Spring REST client facilities
- Geoapify

### API documentation and operations

- Springdoc OpenAPI
- Swagger UI
- Spring Boot Actuator

### Testing

- JUnit
- Mockito
- Spring MVC test support
- Testcontainers
- MySQL Testcontainer
- REST Assured
- JaCoCo

### Delivery

- Docker
- Docker Compose
- GitHub Actions

---

## 4. Root Package and Feature Structure

The root Java package is:

```text
com.burgosfacundo.inventory
```

The application uses **package by feature**.

Current top-level feature structure:

```text
com.burgosfacundo.inventory
│
├── category
├── common
├── inventory_movement
├── product
├── product_supplier
├── stock
├── stock_transfer
├── supplier
└── warehouse
```

Business features generally contain responsibilities such as:

```text
controller
dto
exception
mapper
model
repository
service
```

For example:

```text
product
├── controller
├── dto
├── exception
├── mapper
├── model
├── repository
└── service
```

Warehouse additionally contains integration-specific code:

```text
warehouse
├── controller
├── dto
├── exception
├── integration
│   ├── demo
│   └── geoapify
├── mapper
├── model
├── repository
└── service
```

Shared cross-cutting concerns are grouped under:

```text
common
├── config
├── exception
└── web
```

This structure keeps each business capability cohesive while still making technical responsibilities easy to identify.

---

## 5. Layer Responsibilities

## 5.1 Controllers

Controllers define the HTTP boundary.

Responsibilities include:

- receiving HTTP requests
- validating request DTOs
- validating path/query parameters
- constructing pagination and sorting inputs
- delegating use cases to services
- selecting HTTP status codes
- returning response DTOs

Controllers do not contain persistence logic.

Examples:

```text
ProductController
InventoryMovementController
StockTransferController
```

The API base path is not repeated in every controller.

Controllers declare feature-relative mappings such as:

```text
/products
/inventory-movements
/stock-transfers
```

`WebConfig` applies the configured:

```text
/api/v1
```

prefix only to application `@RestController` classes inside:

```text
com.burgosfacundo.inventory
```

This allows application endpoints to be versioned without accidentally prefixing Springdoc or Actuator infrastructure endpoints.

---

## 5.2 Application Services

Services implement use cases and transaction boundaries.

Responsibilities include:

- loading required domain objects
- enforcing cross-entity business rules
- checking uniqueness when useful for application-level error messages
- coordinating repositories
- invoking external abstractions
- creating and updating domain objects
- defining read-only and read-write transactions
- mapping entities to response DTOs

Examples:

```text
ProductService
SupplierService
WarehouseService
StockService
InventoryMovementService
StockTransferService
ProductSupplierService
```

Service interfaces are used as application contracts and implemented by Spring-managed service classes.

---

## 5.3 Domain Models

Domain models represent persisted business concepts and protect local invariants.

Main models:

```text
Category
Product
Supplier
ProductSupplier
Warehouse
Address
Stock
InventoryMovement
StockTransfer
MovementType
```

Examples of invariants enforced close to the model:

- Product SKU cannot be blank.
- Product sale price cannot be negative.
- Supplier email must be present and valid.
- Product-supplier purchase price cannot be negative.
- Stock quantity cannot be negative.
- Stock minimum cannot be negative.
- Inventory movement quantity must be positive.
- Inventory movement type is required.
- Stock transfer quantity must be positive.
- Transfer source and destination warehouses must differ.

`Address` is an embedded Value Object owned by `Warehouse`.

---

## 5.4 Repositories

Repositories use Spring Data JPA.

Responsibilities include:

- entity persistence
- entity retrieval
- filtered queries
- relationship fetching
- database locking where required

Database constraints remain the final integrity layer even when services perform pre-checks.

Important database constraints include:

```text
UNIQUE product.sku
UNIQUE supplier.email
UNIQUE warehouse.code
UNIQUE (product_id, supplier_id)
UNIQUE (product_id, warehouse_id)
CHECK stock.quantity >= 0
CHECK stock.minimum_stock >= 0
CHECK inventory_movement.quantity > 0
CHECK inventory_movement.type IN ('IN', 'OUT')
CHECK stock_transfer.quantity > 0
CHECK source_warehouse_id <> destination_warehouse_id
```

Foreign keys use restrictive deletion behavior for historical and referential integrity.

---

## 5.5 DTOs and Mappers

Persistence entities are not exposed directly through HTTP controllers.

Request flow:

```text
JSON
  │
  ▼
Request DTO
  │
  ▼
Application Service
  │
  ▼
Domain Model
```

Response flow:

```text
Domain Model
  │
  ▼
Manual Mapper
  │
  ▼
Response DTO
  │
  ▼
JSON
```

The project intentionally uses explicit manual mappers.

Examples:

```text
ProductMapper
WarehouseMapper
InventoryMovementMapper
StockTransferMapper
```

Feature summary DTOs are owned by the feature they summarize, such as:

```text
ProductSummaryResponse
WarehouseSummaryResponse
CategorySummaryResponse
SupplierSummaryResponse
```

---

## 6. API Base Path Architecture

Application endpoints are versioned through:

```yaml
api:
  base-path: /api/v1
```

`WebConfig` applies the prefix programmatically to application REST controllers.

Conceptually:

```text
ProductController mapping:
    /products

WebConfig prefix:
    /api/v1

Final endpoint:
    /api/v1/products
```

Infrastructure endpoints remain outside the application base path:

```text
/swagger-ui.html
/v3/api-docs
/v3/api-docs.yaml
/actuator/health
/actuator/info
```

Springdoc is configured to document only:

```text
/api/v1/**
```

---

## 7. Persistence Architecture

Database:

```text
MySQL 8.4
```

ORM:

```text
Spring Data JPA + Hibernate
```

Schema migration:

```text
Flyway
```

Hibernate configuration:

```text
ddl-auto = validate
open-in-view = false
```

This means:

- Flyway creates and evolves the schema.
- Hibernate verifies that mappings match the schema.
- HTTP serialization does not depend on an open persistence context.

Current migration sequence includes tables for:

```text
categories
products
suppliers
product_suppliers
warehouses
stocks
inventory_movements
stock_transfers
```

plus later schema evolution such as product-supplier purchase pricing.

---

## 8. Stock Model

`Stock` represents the quantity of one product at one warehouse.

Its identity is protected by:

```text
UNIQUE (product_id, warehouse_id)
```

Core mutable state:

```text
quantity
minimumStock
```

Domain operations:

```text
increase(amount)
decrease(amount)
updateMinimumStock(value)
```

`increase` and `decrease` require a positive amount.

`decrease` rejects requests greater than available stock.

Low stock is derived as:

```text
quantity <= minimumStock
```

Stock quantity is not modified directly by a generic stock HTTP update.

Quantity changes occur through:

```text
InventoryMovement
StockTransfer
```

---

## 9. Inventory Movement Transaction Model

`InventoryMovement` represents a traceable stock change.

Movement types:

```text
IN
OUT
```

An inventory movement stores:

```text
Product
Warehouse
MovementType
quantity
createdAt
```

There is no `reason` field in the final version 1 model.

### IN operation

Conceptual flow:

```text
BEGIN TRANSACTION

1. Load Product
2. Load Warehouse
3. Lock existing Stock if present
4. If Stock exists:
      increase quantity
   Else:
      create Stock(quantity, minimumStock = 0)
5. Persist InventoryMovement(IN)

COMMIT
```

### OUT operation

Conceptual flow:

```text
BEGIN TRANSACTION

1. Load Product
2. Load Warehouse
3. Lock Stock
4. Reject if Stock does not exist
5. Reject if requested quantity exceeds available quantity
6. Decrease Stock
7. Persist InventoryMovement(OUT)

COMMIT
```

Because the stock entity is managed inside the transaction, quantity changes use JPA dirty checking rather than unnecessary explicit saves.

---

## 10. Concurrency Control

Stock-changing operations use pessimistic database locking.

`StockRepository` exposes a query using:

```text
PESSIMISTIC_WRITE
```

for the pair:

```text
productId + warehouseId
```

This protects operations from concurrent lost updates.

Example problem prevented:

```text
Current quantity = 5

Request A -> OUT 4
Request B -> OUT 4
```

Without serialized access, both requests could observe the same original quantity.

With the locked stock row, operations are coordinated by the database transaction.

The database constraint:

```text
quantity >= 0
```

provides an additional integrity layer.

---

## 11. Stock Transfer Architecture

`StockTransfer` is a first-class historical entity.

It records:

```text
Product
sourceWarehouse
destinationWarehouse
quantity
createdAt
```

The transfer service coordinates multiple stock records and inventory movements inside one transaction.

Conceptual flow:

```text
BEGIN TRANSACTION

1. Load Product
2. Load source Warehouse
3. Load destination Warehouse
4. Reject same source/destination
5. Determine warehouse lock order
6. Lock both stock positions in deterministic warehouse-id order
7. Require source Stock
8. Decrease source Stock
9. Increase destination Stock
   or create destination Stock with minimumStock = 0
10. Persist InventoryMovement(OUT, source)
11. Persist InventoryMovement(IN, destination)
12. Persist StockTransfer

COMMIT
```

If any step fails:

```text
ROLLBACK
```

This guarantees that a transfer cannot partially update one warehouse without updating the other or without recording its audit history.

---

## 12. Deterministic Lock Ordering

Transfers may touch two warehouse stock records.

To reduce deadlock risk, the implementation determines lock order using warehouse IDs.

Conceptually:

```text
firstWarehouseId  = min(sourceId, destinationId)
secondWarehouseId = max(sourceId, destinationId)
```

The service locks in that order regardless of transfer direction.

It then maps those locked records back to:

```text
source
destination
```

This gives transfers a consistent lock acquisition order across concurrent requests.

---

## 13. Warehouse and Address Architecture

`Warehouse` owns an embedded `Address`.

Conceptually:

```java
@Entity
class Warehouse {
    @Embedded
    private Address address;
}
```

The stored address includes:

```text
street
number
postalCode
city
province
countryCode
latitude
longitude
```

Warehouse creation and update do not construct persisted addresses directly from untrusted request data.

Instead they depend on:

```text
AddressValidator
```

The application contract is:

```java
Address validate(AddressRequest request);
```

`WarehouseServiceImpl` depends on this abstraction rather than on Geoapify-specific classes.

---

## 14. External Address Validation

The external integration is located inside the warehouse feature.

Structure:

```text
warehouse.integration
├── geoapify
└── demo
```

### Normal profile

For profiles other than `demo`:

```text
GeoapifyAddressValidator
```

implements:

```text
AddressValidator
```

Flow:

```text
WarehouseService
      │
      ▼
AddressValidator
      │
      ▼
GeoapifyAddressValidator
      │
      ▼
GeoapifyClient
      │
      ▼
Geoapify HTTP API
```

The adapter:

- calls Geoapify
- validates that a result exists
- validates required provider fields
- validates minimum confidence
- normalizes available address information
- falls back to submitted postal code/province when appropriate
- converts the provider result into the application's `Address`

Geoapify-specific DTOs remain inside the integration package.

The rest of the application does not depend on them.

---

## 15. Demo Address Adapter

Docker execution activates:

```text
demo
```

In this profile:

```text
DemoAddressValidator
```

implements the same:

```text
AddressValidator
```

contract.

The demo implementation:

- uses submitted address fields
- normalizes country code to uppercase
- uses placeholder coordinates
- performs no external HTTP request

This allows the complete application to run without a Geoapify API key.

The architectural point is that Warehouse does not know which validator implementation is active.

Spring profile selection chooses the adapter.

---

## 16. External Call and Transaction Considerations

Warehouse creation/update performs address validation before applying the persisted warehouse state.

Conceptual flow:

```text
1. Validate request DTO
2. Check warehouse business constraints
3. Invoke AddressValidator
4. Obtain validated Address
5. Create/update Warehouse
6. Persist transaction
```

If validation fails:

```text
no valid warehouse change is committed
```

Stock operations differ because they require transactional coordination and locking across database state.

---

## 17. Validation Architecture

Validation occurs at multiple levels.

### HTTP boundary

Jakarta Bean Validation handles structural input requirements.

Examples:

```text
@NotBlank
@NotNull
@Positive
@PositiveOrZero
@Size
@Email
@Max
```

### Domain model

Entities validate invariants that must hold regardless of caller.

Examples:

```text
quantity > 0
price >= 0
required Product
required Warehouse
different transfer warehouses
```

### Application service

Services enforce cross-entity rules.

Examples:

```text
SKU uniqueness
supplier email uniqueness
referenced category existence
referenced warehouse existence
stock availability
valid date ranges
```

### Database

MySQL enforces final integrity through:

```text
PRIMARY KEY
FOREIGN KEY
UNIQUE
CHECK
NOT NULL
```

The layers complement each other rather than replacing each other.

---

## 18. Exception Architecture

Domain- and feature-specific exceptions are translated centrally.

The application uses a shared exception hierarchy with categories corresponding to HTTP semantics, including:

```text
BadRequestException
ResourceNotFoundException
ConflictException
UnprocessableContentException
ServiceUnavailableException
```

`GlobalExceptionHandler` is implemented with:

```text
@RestControllerAdvice
```

and maps application failures to Spring `ProblemDetail`.

Common mappings:

```text
400 -> validation / malformed request / bad request
404 -> missing resources
409 -> conflicts and insufficient stock
422 -> unresolved/invalid external address result
503 -> external service unavailable
500 -> unexpected server error
```

The response includes:

```text
errorCode
```

as a custom extension.

Request-body field validation errors also include:

```text
fieldErrors
```

and method/query/path validation errors may include:

```text
errors
```

Controllers therefore do not duplicate error response construction.

---

## 19. OpenAPI Architecture

Springdoc generates the machine-readable API contract from the application.

Available endpoints:

```text
/swagger-ui.html
/v3/api-docs
/v3/api-docs.yaml
```

Controllers contain OpenAPI metadata such as:

```text
@Tag
@Operation
@ApiResponse
@Parameter
@Schema
```

Reusable error response components keep documented errors consistent.

The generated OpenAPI specification is considered the authoritative machine-readable HTTP contract.

---

## 20. Actuator Architecture

Spring Boot Actuator provides operational endpoints.

Exposed endpoints:

```text
/actuator/health
/actuator/info
```

Only health and info are exposed by the current configuration.

The Docker API container health check uses:

```text
/actuator/health
```

so container orchestration can determine whether the application is ready.

---

## 21. Docker Architecture

The project provides:

```text
Dockerfile
compose.yml
```

### Application image

The Dockerfile is multi-stage.

Build stage:

```text
Maven + Eclipse Temurin 25
```

Runtime stage:

```text
Eclipse Temurin 25 JRE Alpine
```

The build stage packages the Spring Boot application.

The runtime stage contains only what is required to execute the resulting JAR plus `curl` for health checks.

### Compose environment

Docker Compose starts:

```text
MySQL 8.4
Inventory API
```

MySQL uses a persistent named volume.

The API depends on MySQL becoming healthy before startup.

The application container runs with:

```text
SPRING_PROFILES_ACTIVE=demo
```

Therefore the Docker demo does not require Geoapify credentials.

---

## 22. Demo Data Architecture

Flyway owns both schema migration and demo bootstrap behavior.

Versioned migrations create the schema.

The demo profile additionally loads representative data through demo-specific Flyway migration configuration.

The demo dataset makes it possible to inspect:

```text
categories
products
suppliers
product-supplier relationships
warehouses
stocks
inventory movements
stock transfers
```

without manually constructing the complete domain before evaluating the API.

---

## 23. Testing Architecture

The project separates regular tests and integration tests through the Maven lifecycle.

### Regular tests

Files following:

```text
*Test
```

run through Maven Surefire.

They include areas such as:

- domain behavior
- services
- mappers
- controller HTTP contracts

### Integration tests

Files following:

```text
*IT
```

run through Maven Failsafe during:

```text
mvn verify
```

Integration coverage includes:

- repository/JPA behavior
- MySQL constraints
- transactional operations
- complete HTTP API workflows
- infrastructure endpoints

### Real database testing

Integration tests use:

```text
Testcontainers + MySQL
```

rather than replacing MySQL with an in-memory database.

This verifies behavior against the same database engine used by the application.

### API integration testing

REST Assured is used for representative cross-layer HTTP workflows.

The application starts on a random port and requests exercise the real HTTP stack.

---

## 24. Testcontainers Lifecycle

The MySQL Testcontainer is provided through Spring test configuration and imported by integration test base classes.

The container lifecycle is therefore aligned with the Spring ApplicationContext lifecycle.

This avoids cached application contexts retaining datasource connections to a Testcontainer that has already been stopped.

The integration test architecture favors one compatible shared Spring-managed MySQL container lifecycle across the suite.

---

## 25. Database Test Isolation

API-level integration tests do not rely on test transaction rollback because HTTP requests are executed by the running server in separate transactions.

Instead, database state is explicitly cleaned between API integration tests.

Repository integration tests may use transactional rollback where appropriate.

This distinction keeps test isolation aligned with the actual execution model.

---

## 26. Coverage Architecture

JaCoCo collects coverage separately for:

```text
regular tests
integration tests
```

The generated execution data is merged into:

```text
target/jacoco.exec
```

The HTML report is generated at:

```text
target/site/jacoco/
```

The build enforces project-wide minimum coverage of:

```text
90% lines
80% branches
```

If either minimum is not met:

```text
mvn verify
```

fails.

Coverage is used as a quality guard, not as a substitute for meaningful test assertions.

---

## 27. Maven Verification Lifecycle

The project distinguishes:

```text
mvn test
```

from:

```text
mvn verify
```

`mvn test` runs the regular Surefire suite.

`mvn verify` performs the complete verification lifecycle:

```text
regular tests
      ↓
integration tests
      ↓
Testcontainers / MySQL
      ↓
JaCoCo merge
      ↓
JaCoCo report
      ↓
coverage threshold check
```

For complete local or CI validation, `verify` is the authoritative build command.

---

## 28. Continuous Integration

GitHub Actions runs CI on:

```text
pull requests -> main
pushes -> main
```

The workflow:

```text
Checkout
   ↓
Set up Eclipse Temurin Java 25
   ↓
Restore/cache Maven dependencies
   ↓
mvn --batch-mode verify
   ↓
Upload JaCoCo HTML artifact
```

The workflow runs on Ubuntu and therefore validates the repository independently from the developer's local IntelliJ environment.

A change is considered technically validated only when the complete Maven verification lifecycle succeeds in CI.

---

## 29. Architectural Dependency Summary

The core dependency direction can be summarized as:

```text
Controller
    │
    ▼
Service Interface
    │
    ▼
Service Implementation
    │
    ├────────► Domain Model
    ├────────► Repository
    └────────► Application Abstraction
                     │
                     ▼
              Integration Adapter
```

Database infrastructure sits behind repositories:

```text
Repository
    │
    ▼
Spring Data JPA
    │
    ▼
Hibernate
    │
    ▼
MySQL
```

External address infrastructure sits behind:

```text
AddressValidator
```

which has different implementations by profile.

---

## 30. Final Architecture Decisions

The following architectural decisions define version 1:

1. The application is a modular monolith.
2. The root package is `com.burgosfacundo.inventory`.
3. Source code is organized by feature rather than by one global technical layer.
4. Feature packages internally separate controller, DTO, model, mapper, repository and service responsibilities.
5. Controllers remain thin and delegate use cases to services.
6. Persistence entities are never exposed directly through the HTTP API.
7. Mapping is explicit and manual.
8. Spring Data JPA repositories isolate persistence access.
9. Flyway owns schema evolution.
10. Hibernate uses `ddl-auto=validate`.
11. Open Session in View is disabled.
12. Application REST endpoints receive the global `/api/v1` prefix through `WebConfig`.
13. Springdoc and Actuator infrastructure endpoints remain outside `/api/v1`.
14. Stock quantity is modified only through inventory movement and transfer use cases.
15. Inventory movements use `IN` and `OUT`.
16. Inventory movement creation and stock modification are transactional.
17. Stock transfers are first-class historical entities.
18. Transfers modify both warehouse stocks and create matching `OUT`/`IN` movements in one transaction.
19. Pessimistic database locking protects stock-changing operations.
20. Stock transfers acquire locks in deterministic warehouse-id order.
21. Database constraints remain the authoritative integrity layer.
22. Warehouse addresses are embedded Value Objects.
23. Warehouse services depend on the `AddressValidator` abstraction.
24. Geoapify is the normal external address-validation adapter.
25. The `demo` profile replaces Geoapify with a local offline adapter.
26. Errors are centralized through `GlobalExceptionHandler` and Spring `ProblemDetail`.
27. OpenAPI/Swagger documents the implemented HTTP contract.
28. Actuator exposes health and application information.
29. Docker Compose provides a zero-configuration portfolio demo with MySQL.
30. Integration tests use a real MySQL Testcontainer.
31. Maven Failsafe runs `*IT` integration tests during `verify`.
32. JaCoCo merges regular and integration test coverage.
33. GitHub Actions executes the complete `mvn verify` lifecycle.
34. Authentication, distributed messaging, Redis, multi-tenancy and microservices remain outside version 1.