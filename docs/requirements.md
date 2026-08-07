# Inventory API — Requirements

## 1. Purpose

Inventory API is a backend REST service for managing products, suppliers, warehouses and stock across multiple physical locations.

The project is intended to model real inventory operations rather than a simple CRUD application. Every stock change must be traceable through stock movements and must respect the business rules defined below.

---

## 2. Scope

The API will manage:

- Products
- Categories
- Suppliers
- Product-supplier relationships
- Warehouses
- Warehouse addresses
- Stock by product and warehouse
- Stock movements

Authentication and authorization are intentionally out of scope for this first project.

---

## 3. Domain Model

The domain model is documented separately in:

- `docs/diagrams/inventory-domain.drawio`
- `docs/diagrams/inventory-domain.svg`

Main domain elements:

- `Product`
- `Category`
- `Supplier`
- `ProductSupplier`
- `Warehouse`
- `Address` (`Value Object`)
- `Stock`
- `StockMovement`
- `MovementType`

---

## 4. Functional Requirements

### 4.1 Products

**FR-PROD-001**  
The system shall allow creating a product.

**FR-PROD-002**  
Each product shall have a unique SKU.

**FR-PROD-003**  
Each product shall belong to one category.

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
The system shall support both logical deletion/deactivation and physical deletion, subject to the deletion rules defined in this document.

---

### 4.2 Categories

**FR-CAT-001**  
The system shall allow creating categories.

**FR-CAT-002**  
The system shall allow listing categories.

**FR-CAT-003**  
The system shall allow updating categories.

**FR-CAT-004**  
The system shall prevent assigning a product to a non-existing category.

**FR-CAT-005**  
Category names are not required to be unique.

---

### 4.3 Suppliers

**FR-SUP-001**  
The system shall allow creating suppliers.

**FR-SUP-002**  
The system shall allow retrieving and listing suppliers.

**FR-SUP-003**  
The system shall allow updating supplier information.

**FR-SUP-004**  
The system shall allow activating or deactivating suppliers.

**FR-SUP-005**  
Supplier email shall be unique.

---

### 4.4 Product-Supplier Relationships

**FR-PS-001**  
A product may be associated with multiple suppliers.

**FR-PS-002**  
A supplier may supply multiple products.

**FR-PS-003**  
The product-supplier relationship shall store the purchase price for that supplier.

**FR-PS-004**  
The same product-supplier relationship shall not be duplicated.

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
Warehouse address data shall be validated and normalized using an external geocoding/address provider before being persisted.

**FR-WH-006**  
The initially selected external address provider is Geoapify.

**FR-WH-007**  
If the external address provider is unavailable or the address cannot be validated, the warehouse creation or address update shall fail and no partial warehouse data shall be persisted.

**FR-WH-008**  
If an address update fails validation, the previously stored valid address shall remain unchanged.

**FR-WH-009**  
The system shall allow retrieving and listing warehouses.

**FR-WH-010**  
The system shall allow updating warehouse information.

**FR-WH-011**  
The system shall support both logical deletion/deactivation and physical deletion, subject to the deletion rules defined in this document.

---

### 4.6 Stock

**FR-STOCK-001**  
Stock shall be tracked independently for each product and warehouse combination.

**FR-STOCK-002**  
Only one stock record may exist for the same product and warehouse combination.

**FR-STOCK-003**  
The system shall allow retrieving stock for a specific product.

**FR-STOCK-004**  
The system shall allow retrieving stock for a specific warehouse.

**FR-STOCK-005**  
The system shall allow listing products whose stock is at or below their configured minimum stock level.

**FR-STOCK-006**  
Stock quantities shall not be modified directly through a generic update operation.

**FR-STOCK-007**  
A stock record shall be created automatically when the first valid inbound stock movement is registered for a product and warehouse combination that does not yet have stock.

**FR-STOCK-008**  
An outbound stock movement for a product and warehouse combination with no existing stock record shall be rejected as insufficient stock.

---

### 4.7 Stock Movements

**FR-MOV-001**  
Every stock quantity change shall generate a stock movement.

**FR-MOV-002**  
A stock movement shall contain:

- Product and warehouse context through its stock record
- Movement type
- Quantity
- Reason
- Creation timestamp

**FR-MOV-003**  
The supported movement types shall initially be:

- `PURCHASE`
- `SALE`
- `ADJUSTMENT_IN`
- `ADJUSTMENT_OUT`

**FR-MOV-004**  
`PURCHASE` and `ADJUSTMENT_IN` shall increase stock.

**FR-MOV-005**  
`SALE` and `ADJUSTMENT_OUT` shall decrease stock.

**FR-MOV-006**  
The system shall reject outbound movements when available stock is insufficient.

**FR-MOV-007**  
The stock update and its corresponding stock movement shall be persisted atomically.

**FR-MOV-008**  
The system shall allow retrieving the movement history for a stock record.

**FR-MOV-009**  
`reason` shall be mandatory for `ADJUSTMENT_IN` and `ADJUSTMENT_OUT`.

**FR-MOV-010**  
`reason` may be optional for `PURCHASE` and `SALE`.

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

**BR-005 — Non-negative stock**  
A stock quantity must never become negative.

**BR-006 — Stock identity**  
A stock record is uniquely identified by the combination of product and warehouse.

**BR-007 — Movement traceability**  
Stock cannot be changed without creating a corresponding stock movement.

**BR-008 — Positive movement quantity**  
The quantity submitted for a stock movement must always be greater than zero. The movement type determines whether it increases or decreases stock.

**BR-009 — Atomic stock operation**  
Updating stock and recording its movement must happen in the same transaction.

**BR-010 — Valid references**  
Products, categories, suppliers and warehouses referenced by an operation must exist.

**BR-011 — Address validation**  
A warehouse address must be validated and normalized through the configured external address provider before being persisted.

**BR-012 — Address consistency**  
Warehouse creation or address modification is atomic from the application's perspective. The application shall not persist a warehouse with an unvalidated or partially resolved address.

**BR-013 — Monetary values**  
Purchase and sale prices must use decimal monetary types and must not be negative.

**BR-014 — Automatic stock creation**  
When the first inbound movement is registered for a product and warehouse combination, the system shall create the corresponding stock record if it does not already exist.

**BR-015 — Missing stock on outbound movement**  
If an outbound movement is requested and no stock record exists for the selected product and warehouse, the operation shall be rejected as insufficient stock.

**BR-016 — Adjustment auditability**  
Manual stock adjustments must always include a reason.

---

## 6. Deletion Rules

The API shall distinguish between logical deletion/deactivation and physical deletion.

### 6.1 Logical Deletion / Deactivation

Logical deletion or deactivation shall be the preferred operation for records that may have historical relationships.

Examples include:

- Products
- Suppliers
- Warehouses

Deactivated records remain stored for historical consistency but may be excluded from normal active listings and operations.

### 6.2 Physical Deletion

Physical deletion shall only be allowed when removing the record does not destroy historical or referential integrity.

The system shall reject physical deletion when the target record is referenced by historical inventory data or other records that must be preserved.

Examples:

- A product with stock movements must not be physically deleted.
- A warehouse with stock movement history must not be physically deleted.
- A supplier relationship may only be physically removed when doing so does not violate required historical data.

Detailed HTTP behavior for logical and physical deletion will be defined in `api-design.md`.

---

## 7. Validation Requirements

### Product

- SKU: required and unique
- Name: required
- Sale price: required and greater than or equal to zero
- Category: required
- Active: required

### Category

- Name: required
- Name does not need to be unique

### Supplier

- Name: required
- Email: required
- Email: valid email format
- Email: unique
- Active: required

### Warehouse

- Code: required and unique
- Name: required
- Address: required
- Address: externally validated and normalized before persistence

### Address

- Street: required
- Number: required
- Postal code: stored as text
- City: required after external validation
- Province / state / region: required when returned by the external provider
- Country code: required
- Latitude: required after successful geocoding
- Longitude: required after successful geocoding

### ProductSupplier

- Product: required
- Supplier: required
- Purchase price: required and greater than or equal to zero
- Product-supplier pair: unique

### Stock

- Product: required
- Warehouse: required
- Quantity: greater than or equal to zero
- Minimum stock: greater than or equal to zero
- Product-warehouse pair: unique

### StockMovement

- Movement type: required
- Quantity: required and greater than zero
- Reason: mandatory for `ADJUSTMENT_IN` and `ADJUSTMENT_OUT`
- Reason: optional for `PURCHASE` and `SALE`

---

## 8. External Address Validation

The application shall integrate with an external geocoding/address provider.

### Initial Provider

The initially selected provider is:

- Geoapify

### Responsibilities of the Integration

The integration shall:

- Validate that the provided address can be resolved.
- Normalize available address components.
- Return geographic coordinates.
- Return country and locality information when available.

### Failure Behavior

If the provider is unavailable or the submitted address cannot be validated:

- The requested warehouse creation or address update shall fail.
- No partially valid warehouse state shall be persisted.
- Existing warehouse data shall remain unchanged during failed updates.
- The API shall return an error indicating that address validation could not be completed.
- The client may retry the operation later.

The concrete HTTP status and error response will be defined in `api-design.md`.

---

## 9. Non-Functional Requirements

**NFR-001 — REST**  
The application shall expose a versioned REST API using JSON.

**NFR-002 — Database**  
Persistent data shall be stored in MySQL.

**NFR-003 — Database migrations**  
Database schema changes shall be managed with Flyway migrations.

**NFR-004 — API documentation**  
Endpoints shall be documented using OpenAPI / Swagger.

**NFR-005 — Error responses**  
API errors shall use a consistent response structure.

**NFR-006 — Testing**  
The project shall contain unit, integration and API-level automated tests.

**NFR-007 — Real database integration tests**  
Integration tests shall run against MySQL using Testcontainers.

**NFR-008 — External integration testing**  
The external address provider shall be isolated behind an application interface so it can be mocked or replaced during automated tests.

**NFR-009 — Containerization**  
The API and MySQL database shall be runnable locally using Docker Compose.

**NFR-010 — Reproducibility**  
The repository shall include sample/test data so the API can be evaluated quickly after startup.

**NFR-011 — Continuous Integration**  
GitHub Actions shall build the application and execute the automated test suite on repository changes.

**NFR-012 — Transactional consistency**  
Operations that modify stock and create stock movements shall preserve transactional consistency.

---

## 10. Out of Scope — Version 1

The following features are intentionally excluded from the first version:

- Authentication and authorization
- Spring Security
- User management
- Purchase orders
- Sales orders
- Billing or invoicing
- Reservations of stock
- Transfers between warehouses
- Event-driven messaging
- Redis caching
- Multi-tenancy

These capabilities may be introduced in future versions or separate portfolio projects.

---

## 11. Final Design Decisions

The following decisions are considered closed for version 1:

1. The API supports both logical and physical deletion, with physical deletion restricted when historical or referential integrity would be affected.
2. Product SKU is unique.
3. Category name is not required to be unique.
4. Supplier email is unique.
5. Warehouse addresses are validated using an external provider.
6. Geoapify is the initially selected external address provider.
7. Warehouse creation and address updates fail atomically if address validation cannot be completed.
8. `reason` is mandatory for stock adjustments.
9. Stock records are created automatically on the first valid inbound movement.
10. Outbound movements against non-existing stock are rejected as insufficient stock.
11. Spring Security is intentionally excluded from this project.
