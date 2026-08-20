# Inventory API — Requirements

## 1. Purpose

Inventory API is a backend REST service for managing products, suppliers, warehouses and stock across multiple physical locations.

The project models real inventory operations rather than a simple CRUD application. Stock changes are traceable through inventory movements, warehouse transfers are recorded explicitly, and stock-changing operations preserve transactional consistency.

---

## 2. Scope

The API manages:

- Products
- Categories
- Suppliers
- Product-supplier relationships
- Warehouses
- Warehouse addresses
- Stock by product and warehouse
- Inventory movements
- Low-stock monitoring
- Stock transfers between warehouses

Authentication and authorization are intentionally out of scope for version 1.

---

## 3. Domain Model

The domain model is documented in:

- `docs/diagrams/inventory-domain.jpg`

Main domain elements:

- `Product`
- `Category`
- `Supplier`
- `ProductSupplier`
- `Warehouse`
- `Address` (`Value Object`)
- `Stock`
- `InventoryMovement`
- `MovementType`
- `StockTransfer`

---

## 4. Functional Requirements

### 4.1 Products

**FR-PROD-001**  
The system shall allow creating a product.

**FR-PROD-002**  
Each product shall have a unique SKU.

**FR-PROD-003**  
Each product shall belong to one existing category.

**FR-PROD-004**  
The system shall allow retrieving a product by its identifier.

**FR-PROD-005**  
The system shall allow listing products with pagination and sorting.

**FR-PROD-006**  
The system shall allow filtering products by category and active status.

**FR-PROD-007**  
The system shall allow updating product information.

**FR-PROD-008**  
The system shall allow activating or deactivating a product.

**FR-PROD-009**  
The system shall allow physically deleting a product when database referential integrity permits the operation.

---

### 4.2 Categories

**FR-CAT-001**  
The system shall allow creating categories.

**FR-CAT-002**  
The system shall allow retrieving a category by its identifier.

**FR-CAT-003**  
The system shall allow listing categories.

**FR-CAT-004**  
The system shall allow updating categories.

**FR-CAT-005**  
The system shall prevent assigning a product to a non-existing category.

**FR-CAT-006**  
Category names are not required to be unique.

**FR-CAT-007**  
Physical deletion of categories is not exposed in version 1.

---

### 4.3 Suppliers

**FR-SUP-001**  
The system shall allow creating suppliers.

**FR-SUP-002**  
The system shall allow retrieving suppliers by identifier.

**FR-SUP-003**  
The system shall allow listing suppliers with pagination and sorting.

**FR-SUP-004**  
The system shall allow filtering suppliers by active status.

**FR-SUP-005**  
The system shall allow updating supplier information.

**FR-SUP-006**  
The system shall allow activating or deactivating suppliers.

**FR-SUP-007**  
Supplier email shall be unique.

**FR-SUP-008**  
The system shall allow physically deleting a supplier when database referential integrity permits the operation.

---

### 4.4 Product-Supplier Relationships

**FR-PS-001**  
A product may be associated with multiple suppliers.

**FR-PS-002**  
A supplier may supply multiple products.

**FR-PS-003**  
The product-supplier relationship shall store the purchase price for that supplier.

**FR-PS-004**  
The same product-supplier pair shall not be duplicated.

**FR-PS-005**  
The system shall allow retrieving a product-supplier relationship by its identifier.

**FR-PS-006**  
The system shall allow listing product-supplier relationships with pagination and sorting.

**FR-PS-007**  
The system shall allow filtering product-supplier relationships by product and supplier.

**FR-PS-008**  
The system shall allow updating the purchase price of an existing product-supplier relationship.

**FR-PS-009**  
The system shall allow deleting a product-supplier relationship.

---

### 4.5 Warehouses

**FR-WH-001**  
The system shall allow creating warehouses.

**FR-WH-002**  
Each warehouse shall have a unique code.

**FR-WH-003**  
Each warehouse shall have exactly one address.

**FR-WH-004**  
`Address` shall be modeled as a Value Object owned by `Warehouse`.

**FR-WH-005**  
Warehouse address data shall be validated through the configured address-validation abstraction before being persisted.

**FR-WH-006**  
Geoapify is the external address provider used by the normal application profile.

**FR-WH-007**  
If the submitted address cannot be validated, warehouse creation or update shall fail without persisting an invalid warehouse state.

**FR-WH-008**  
If the external address provider is temporarily unavailable, warehouse creation or update shall fail without partially updating persisted warehouse data.

**FR-WH-009**  
The system shall allow retrieving a warehouse by its identifier.

**FR-WH-010**  
The system shall allow listing warehouses with pagination and sorting.

**FR-WH-011**  
The system shall allow updating warehouse code, name and address in one operation.

**FR-WH-012**  
Warehouse updates shall validate the submitted address before applying the new warehouse state.

**FR-WH-013**  
The system shall allow physically deleting a warehouse when database referential integrity permits the operation.

**FR-WH-014**  
Logical warehouse activation/deactivation is not part of version 1.

---

### 4.6 Stock

**FR-STOCK-001**  
Stock shall be tracked independently for each product and warehouse combination.

**FR-STOCK-002**  
Only one stock record may exist for the same product and warehouse combination.

**FR-STOCK-003**  
The system shall allow retrieving a stock record by its identifier.

**FR-STOCK-004**  
The system shall allow listing stock records with pagination and sorting.

**FR-STOCK-005**  
The system shall allow filtering stock records by product and warehouse.

**FR-STOCK-006**  
The system shall allow listing low-stock records, optionally filtered by warehouse.

**FR-STOCK-007**  
A stock record is considered low stock when:

```text
quantity <= minimumStock
```

**FR-STOCK-008**  
Stock quantity shall not be modified directly through a generic stock update endpoint.

**FR-STOCK-009**  
The system shall allow updating a stock record's `minimumStock` value independently from its quantity.

**FR-STOCK-010**  
A stock record shall be created automatically when the first valid `IN` inventory movement is registered for a product and warehouse combination that does not yet have stock.

**FR-STOCK-011**  
Automatically created stock shall start with:

```text
minimumStock = 0
```

**FR-STOCK-012**  
An `OUT` movement for a product and warehouse combination with no existing stock record shall be rejected.

**FR-STOCK-013**  
Stock quantity and minimum stock shall never be negative.

---

### 4.7 Inventory Movements

**FR-MOV-001**  
Every direct inventory quantity change shall be represented by an `InventoryMovement`.

**FR-MOV-002**  
An inventory movement shall contain:

- Product
- Warehouse
- Movement type
- Quantity
- Creation timestamp

**FR-MOV-003**  
The supported movement types are:

```text
IN
OUT
```

**FR-MOV-004**  
An `IN` movement shall increase stock.

**FR-MOV-005**  
An `OUT` movement shall decrease stock.

**FR-MOV-006**  
Movement quantity shall always be greater than zero.

**FR-MOV-007**  
An `IN` movement shall create the stock record automatically when the product and warehouse combination does not yet have stock.

**FR-MOV-008**  
An `OUT` movement shall require an existing stock record.

**FR-MOV-009**  
The system shall reject an `OUT` movement when the requested quantity exceeds available stock.

**FR-MOV-010**  
Stock modification and inventory movement persistence shall occur atomically.

**FR-MOV-011**  
The system shall allow retrieving an inventory movement by its identifier.

**FR-MOV-012**  
The system shall allow listing inventory movements with pagination and sorting.

**FR-MOV-013**  
The system shall allow filtering inventory movements by:

- Product
- Warehouse
- Movement type
- Creation date range

**FR-MOV-014**  
When both date-range boundaries are supplied, the start date shall not be after the end date.

**FR-MOV-015**  
Inventory movements are historical records and are not updated or deleted through the version 1 API.

---

### 4.8 Stock Transfers

**FR-TRANSFER-001**  
The system shall allow transferring stock for a product from one warehouse to another.

**FR-TRANSFER-002**  
A stock transfer shall contain:

- Product
- Source warehouse
- Destination warehouse
- Quantity
- Creation timestamp

**FR-TRANSFER-003**  
Source and destination warehouses shall be different.

**FR-TRANSFER-004**  
Transfer quantity shall be greater than zero.

**FR-TRANSFER-005**  
The source warehouse shall have an existing stock record for the selected product.

**FR-TRANSFER-006**  
The system shall reject a transfer when the requested quantity exceeds available stock at the source warehouse.

**FR-TRANSFER-007**  
A successful transfer shall decrease source stock and increase destination stock by the same quantity.

**FR-TRANSFER-008**  
If the destination stock record does not exist, it shall be created automatically with:

```text
minimumStock = 0
```

**FR-TRANSFER-009**  
Every successful transfer shall generate:

- one `OUT` `InventoryMovement` for the source warehouse
- one `IN` `InventoryMovement` for the destination warehouse

**FR-TRANSFER-010**  
Source stock modification, destination stock modification, both generated inventory movements and the `StockTransfer` record shall be persisted atomically.

**FR-TRANSFER-011**  
The system shall allow retrieving a stock transfer by its identifier.

**FR-TRANSFER-012**  
The system shall allow listing stock transfers with pagination and sorting.

**FR-TRANSFER-013**  
The system shall allow filtering stock transfers by:

- Product
- Source warehouse
- Destination warehouse
- Creation date range

**FR-TRANSFER-014**  
When both transfer date-range boundaries are supplied, the start date shall not be after the end date.

**FR-TRANSFER-015**  
Stock transfers are historical records and are not updated or deleted through the version 1 API.

---

## 5. Business Rules

**BR-001 — Unique SKU**  
A product SKU must be unique across the system.

**BR-002 — Unique warehouse code**  
A warehouse code must be unique across the system.

**BR-003 — Unique supplier email**  
A supplier email must be unique across the system.

**BR-004 — Category name**  
Category names are not required to be unique.

**BR-005 — Product category**  
Every product must reference an existing category.

**BR-006 — Product-supplier identity**  
The same product and supplier may have only one product-supplier relationship.

**BR-007 — Monetary values**  
Product sale price and product-supplier purchase price must use decimal monetary values and must not be negative.

**BR-008 — Stock identity**  
A stock record is uniquely identified by the combination of product and warehouse.

**BR-009 — Non-negative stock**  
Stock quantity and minimum stock must never be negative.

**BR-010 — Positive stock-changing quantity**  
Inventory movement and stock transfer quantities must always be greater than zero.

**BR-011 — Movement traceability**  
Direct stock changes must be represented by inventory movements.

**BR-012 — Movement direction**  
`IN` increases stock and `OUT` decreases stock.

**BR-013 — Automatic stock creation**  
The first valid inbound operation for a product and warehouse combination may create the stock record automatically with `minimumStock = 0`.

**BR-014 — Insufficient stock**  
An operation that removes more stock than is available must be rejected.

**BR-015 — Atomic inventory movement**  
Updating stock and persisting the corresponding inventory movement must happen in the same transaction.

**BR-016 — Valid transfer route**  
A stock transfer source and destination warehouse must be different.

**BR-017 — Transfer traceability**  
A successful stock transfer must be represented by one `StockTransfer` record plus an `OUT` movement at the source and an `IN` movement at the destination.

**BR-018 — Atomic stock transfer**  
All stock changes, generated movements and the transfer record must be part of the same transaction.

**BR-019 — Concurrency protection**  
Stock-changing operations must use database concurrency protection so concurrent requests cannot produce negative or inconsistent stock.

**BR-020 — Address validation**  
A warehouse address must be validated through the configured `AddressValidator` before the warehouse is persisted or updated.

**BR-021 — Address consistency**  
A failed address validation must not leave a warehouse partially updated.

**BR-022 — Valid references**  
Referenced products, categories, suppliers and warehouses must exist before dependent operations are completed.

**BR-023 — Database integrity**  
Database unique constraints, foreign keys and check constraints remain authoritative even when the application performs validation before persistence.

---

## 6. Deletion and Status Rules

The API distinguishes between status changes and physical deletion only for resources that actually implement those concepts.

### 6.1 Product and Supplier Status

Products and suppliers support an `active` state.

The system shall allow:

- activation
- deactivation
- later reactivation

Changing active status does not physically remove the record.

### 6.2 Warehouse Status

Warehouses do not have an `active` state in version 1.

No warehouse activation/deactivation operation is exposed.

### 6.3 Physical Deletion

Physical deletion is exposed for:

- Products
- Suppliers
- Warehouses
- Product-supplier relationships

Database foreign-key constraints protect referential and historical integrity.

A referenced product, supplier or warehouse must not be removed when doing so would violate persisted relationships or historical inventory data.

Examples include references from:

- Product-supplier relationships
- Stock records
- Inventory movements
- Stock transfers

Category physical deletion is not exposed through the version 1 API.

Inventory movements and stock transfers are treated as historical records and do not expose delete operations.

---

## 7. Validation Requirements

### Product

- SKU: required and unique
- Name: required
- Sale price: required and greater than or equal to zero
- Category: required and must exist
- Active status: managed by the application

### Category

- Name: required
- Name does not need to be unique

### Supplier

- Name: required
- Email: required
- Email: valid format
- Email: unique
- Active status: managed by the application

### ProductSupplier

- Product: required and must exist
- Supplier: required and must exist
- Purchase price: required and greater than or equal to zero
- Product-supplier pair: unique

### Warehouse

- Code: required and unique
- Name: required
- Address: required
- Address: validated before persistence

### Address Input

The submitted warehouse address requires:

- Street
- Street number
- Postal code
- City
- Province
- Two-character country code

### Persisted Address

A successfully validated persisted address contains:

- Street
- Street number
- Postal code
- City
- Province
- Country code
- Latitude
- Longitude

### Stock

- Product: required
- Warehouse: required
- Quantity: greater than or equal to zero
- Minimum stock: greater than or equal to zero
- Product-warehouse pair: unique

### InventoryMovement

- Product ID: required and positive
- Warehouse ID: required and positive
- Movement type: required and limited to `IN` or `OUT`
- Quantity: required and greater than zero
- Date filter: `from` must not be after `to` when both are supplied

### StockTransfer

- Product ID: required and positive
- Source warehouse ID: required and positive
- Destination warehouse ID: required and positive
- Source and destination warehouse: different
- Quantity: required and greater than zero
- Date filter: `from` must not be after `to` when both are supplied

### Pagination

Paginated endpoints use zero-based page numbering.

- `page`: zero or greater
- `size`: greater than zero
- maximum `size`: 100
- sorting is restricted to explicitly allowed fields
- sort direction is limited to ascending or descending

---

## 8. External Address Validation

The application integrates with an external geocoding/address provider behind an application abstraction.

### Application Abstraction

Warehouse use cases depend on:

```text
AddressValidator
```

rather than directly on provider-specific classes.

### External Provider

The normal non-demo application profile uses:

```text
Geoapify
```

### Responsibilities

The external address integration is responsible for:

- validating the submitted address
- resolving geographic information
- mapping provider data into the application's `Address` Value Object
- returning coordinates
- translating provider failures into application-level failures

### Failure Behavior

If an address cannot be validated:

- warehouse creation or update fails
- no invalid warehouse state is persisted
- an existing warehouse remains unchanged after a failed update

If the external provider is unavailable:

- warehouse creation or update fails
- no partial database change is committed
- the client may retry later

### Demo Profile

The Docker demo profile replaces the external Geoapify dependency with a local `AddressValidator` implementation.

This allows the repository to be evaluated without external credentials or network access to Geoapify.

---

## 9. Non-Functional Requirements

**NFR-001 — REST API**  
The application shall expose a versioned JSON REST API under `/api/v1`.

**NFR-002 — Database**  
Persistent data shall be stored in MySQL 8.

**NFR-003 — Database migrations**  
Database schema changes shall be managed with Flyway migrations.

**NFR-004 — Schema ownership**  
Flyway shall own schema evolution and Hibernate shall validate the mapped schema rather than generate it.

**NFR-005 — API documentation**  
The HTTP API shall be documented through OpenAPI and Swagger UI.

**NFR-006 — Operational metadata**  
The application shall expose Spring Boot Actuator health and application information endpoints.

**NFR-007 — Error responses**  
API errors shall use a consistent `ProblemDetail`-based response structure.

**NFR-008 — Testing**  
The project shall contain unit, controller, repository integration, transaction and API-level automated tests.

**NFR-009 — Real database integration tests**  
Integration tests shall run against MySQL using Testcontainers rather than an in-memory database.

**NFR-010 — External integration isolation**  
External address validation shall be isolated behind an application interface so it can be replaced or mocked in automated tests and demo environments.

**NFR-011 — Transactional consistency**  
Operations that modify stock and create inventory history shall preserve transactional consistency.

**NFR-012 — Concurrency safety**  
Concurrent stock-changing operations shall use database locking where required to avoid lost updates, inconsistent quantities or negative stock.

**NFR-013 — Containerization**  
The API and MySQL database shall be runnable locally using Docker Compose.

**NFR-014 — Reproducible demo**  
The Docker demo environment shall be runnable without Geoapify credentials and shall include representative demo data.

**NFR-015 — Continuous Integration**  
GitHub Actions shall run the complete Maven verification lifecycle on pull requests targeting `main` and pushes to `main`.

**NFR-016 — Test lifecycle**  
Regular `*Test` tests shall run through Maven Surefire and `*IT` integration tests shall run through Maven Failsafe during `verify`.

**NFR-017 — Code coverage**  
JaCoCo shall combine regular-test and integration-test coverage and enforce minimum project-wide coverage of:

- 90% line coverage
- 80% branch coverage

**NFR-018 — CI artifacts**  
The CI workflow shall publish the generated JaCoCo HTML report as a workflow artifact.

---

## 10. Out of Scope — Version 1

The following features are intentionally excluded from version 1:

- Authentication and authorization
- Spring Security
- User management
- Purchase orders
- Sales orders
- Billing or invoicing
- Stock reservations
- Event-driven messaging
- Redis caching
- Multi-tenancy
- Microservices

These capabilities may be introduced in future versions or separate portfolio projects.

---

## 11. Final Design Decisions

The following decisions define the implemented version 1:

1. The API is a modular monolith built with Spring Boot.
2. Product SKU is unique.
3. Category name is not required to be unique.
4. Supplier email is unique.
5. Products and suppliers support activation and deactivation.
6. Warehouses do not use an active/inactive lifecycle in version 1.
7. Warehouse addresses are modeled as an embedded `Address` Value Object.
8. Warehouse use cases depend on an `AddressValidator` abstraction.
9. Geoapify is used as the external address provider outside the demo profile.
10. Warehouse persistence/update does not proceed with an invalid or unresolved address.
11. Stock is unique per product and warehouse.
12. Stock quantity cannot be edited directly through a generic stock update.
13. `minimumStock` is configurable independently and defaults to `0` for automatically created stock.
14. Inventory movements use only `IN` and `OUT`.
15. Inventory movements do not contain the earlier planned adjustment `reason` field.
16. The first valid `IN` operation can create a stock record automatically.
17. `OUT` operations require existing and sufficient stock.
18. Inventory movement creation and stock modification are transactional.
19. Warehouse stock transfers are part of version 1.
20. Stock transfers require different source and destination warehouses.
21. Successful transfers create an `OUT` movement at the source and an `IN` movement at the destination.
22. Stock transfers use deterministic pessimistic stock locking to protect concurrent updates.
23. Persistence entities are not exposed directly through the API.
24. Spring Security is intentionally excluded from version 1.
25. MySQL integration testing uses Testcontainers.
26. The project is runnable as a zero-configuration Docker demo.
27. OpenAPI/Swagger and Actuator provide API and operational visibility.
28. GitHub Actions runs the complete Maven verification lifecycle.
29. JaCoCo combines regular and integration test coverage and enforces the configured quality floor.