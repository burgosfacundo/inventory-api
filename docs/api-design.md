# Inventory API — API Design

## 1. Purpose

This document defines the HTTP contract for version 1 of the Inventory API.

It translates the functional and business requirements defined in `requirements.md` into:

- Resource URLs
- HTTP methods
- Request DTOs
- Response DTOs
- Pagination and filtering rules
- HTTP status codes
- Error responses

The implementation and generated OpenAPI specification should remain consistent with this document.

---

## 2. General Conventions

### Base URL

```text
/api/v1
```

API versioning is included in the URL from the first version.

### Content Types

Successful requests and responses use:

```text
application/json
```

Error responses use:

```text
application/problem+json
```

### Resource Naming

Resource names are plural nouns:

```text
/products
/categories
/suppliers
/warehouses
/stocks
/stock-movements
```

### Identifiers

Database identifiers are represented as numeric `Long` values.

Business identifiers remain separate:

- Product → `sku`
- Warehouse → `code`
- Supplier → `email`

---

## 3. Common HTTP Status Codes

| Status | Usage |
|---|---|
| `200 OK` | Successful read or update |
| `201 Created` | Resource successfully created |
| `204 No Content` | Successful physical deletion or relationship removal |
| `400 Bad Request` | Malformed request or DTO validation failure |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | Unique constraint, business-state conflict, insufficient stock or restricted physical deletion |
| `422 Unprocessable Entity` | Address data is syntactically valid but cannot be validated/resolved |
| `503 Service Unavailable` | External address provider is temporarily unavailable |

---

## 4. Error Contract

The API will use Spring's `ProblemDetail` model following the Problem Details standard.

Example:

```json
{
  "type": "about:blank",
  "title": "Resource conflict",
  "status": 409,
  "detail": "A product with SKU NB-LNV-001 already exists.",
  "instance": "/api/v1/products",
  "errorCode": "PRODUCT_SKU_ALREADY_EXISTS"
}
```

Validation errors may include a `fieldErrors` extension:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/products",
  "errorCode": "VALIDATION_ERROR",
  "fieldErrors": {
    "name": "must not be blank",
    "salePrice": "must be greater than or equal to 0"
  }
}
```

### Initial Error Codes

```text
VALIDATION_ERROR
RESOURCE_NOT_FOUND

PRODUCT_SKU_ALREADY_EXISTS
WAREHOUSE_CODE_ALREADY_EXISTS
SUPPLIER_EMAIL_ALREADY_EXISTS
PRODUCT_SUPPLIER_ALREADY_EXISTS

INSUFFICIENT_STOCK
PHYSICAL_DELETION_NOT_ALLOWED

ADDRESS_NOT_RESOLVED
ADDRESS_PROVIDER_UNAVAILABLE
```

The list may grow as implementation reveals additional domain-specific errors.

---

# 5. Pagination and Sorting

List endpoints that may grow significantly should support pagination.

### Query Parameters

```text
page=0
size=20
sort=name,asc
```

Defaults:

```text
page = 0
size = 20
```

Maximum page size:

```text
size = 100
```

### Page Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

The API will expose its own page response DTO instead of returning Spring's internal `Page` representation directly.

---

# 6. Products

Base resource:

```text
/api/v1/products
```

## 6.1 Create Product

```http
POST /api/v1/products
```

### Request — `CreateProductRequest`

```json
{
  "sku": "NB-LNV-001",
  "name": "Lenovo ThinkPad E14",
  "description": "14-inch business notebook",
  "salePrice": 1250.00,
  "categoryId": 1
}
```

### Rules

- `sku` is required and unique.
- `name` is required.
- `salePrice` is required and cannot be negative.
- `categoryId` must reference an existing category.
- New products are created with `active = true`.

### Response — `ProductResponse`

```json
{
  "id": 10,
  "sku": "NB-LNV-001",
  "name": "Lenovo ThinkPad E14",
  "description": "14-inch business notebook",
  "salePrice": 1250.00,
  "active": true,
  "category": {
    "id": 1,
    "name": "Notebooks"
  }
}
```

### Responses

| Status | Condition |
|---|---|
| `201` | Product created |
| `400` | Invalid request |
| `404` | Category does not exist |
| `409` | SKU already exists |

---

## 6.2 Get Product

```http
GET /api/v1/products/{id}
```

### Responses

| Status | Condition |
|---|---|
| `200` | Product returned |
| `404` | Product does not exist |

---

## 6.3 List Products

```http
GET /api/v1/products
```

### Supported Filters

```text
categoryId
active
page
size
sort
```

Example:

```http
GET /api/v1/products?categoryId=1&active=true&page=0&size=20&sort=name,asc
```

### Response

```text
PageResponse<ProductResponse>
```

---

## 6.4 Update Product

```http
PUT /api/v1/products/{id}
```

### Request — `UpdateProductRequest`

```json
{
  "sku": "NB-LNV-001",
  "name": "Lenovo ThinkPad E14 Gen 2",
  "description": "Updated description",
  "salePrice": 1299.99,
  "categoryId": 1
}
```

`active` is intentionally excluded from this DTO and managed through the status endpoint.

### Responses

| Status | Condition |
|---|---|
| `200` | Product updated |
| `400` | Invalid request |
| `404` | Product or category does not exist |
| `409` | Updated SKU conflicts with another product |

---

## 6.5 Change Product Status

```http
PATCH /api/v1/products/{id}/status
```

### Request — `UpdateStatusRequest`

```json
{
  "active": false
}
```

### Responses

| Status | Condition |
|---|---|
| `200` | Status updated |
| `404` | Product does not exist |

This endpoint represents logical deletion/reactivation.

---

## 6.6 Physically Delete Product

```http
DELETE /api/v1/products/{id}
```

### Rules

Physical deletion is rejected when deleting the product would break historical or referential integrity.

A product with stock movement history must not be physically deleted.

### Responses

| Status | Condition |
|---|---|
| `204` | Product physically deleted |
| `404` | Product does not exist |
| `409` | Physical deletion is not allowed |

---

# 7. Categories

Base resource:

```text
/api/v1/categories
```

## 7.1 Create Category

```http
POST /api/v1/categories
```

### Request — `CreateCategoryRequest`

```json
{
  "name": "Notebooks",
  "description": "Portable computers"
}
```

Category names are not required to be unique.

### Responses

| Status | Condition |
|---|---|
| `201` | Category created |
| `400` | Invalid request |

---

## 7.2 Get Category

```http
GET /api/v1/categories/{id}
```

---

## 7.3 List Categories

```http
GET /api/v1/categories
```

The first version may return the complete category list without pagination because categories are treated as reference/master data.

---

## 7.4 Update Category

```http
PUT /api/v1/categories/{id}
```

### Request — `UpdateCategoryRequest`

```json
{
  "name": "Portable Computers",
  "description": "Notebooks and laptops"
}
```

### Responses

| Status | Condition |
|---|---|
| `200` | Category updated |
| `400` | Invalid request |
| `404` | Category does not exist |

Physical deletion of categories is intentionally not exposed in version 1.

---

# 8. Suppliers

Base resource:

```text
/api/v1/suppliers
```

## 8.1 Create Supplier

```http
POST /api/v1/suppliers
```

### Request — `CreateSupplierRequest`

```json
{
  "name": "Tech Distribution S.A.",
  "email": "sales@techdistribution.com",
  "phone": "+54 223 555 0100"
}
```

New suppliers are created with `active = true`.

### Responses

| Status | Condition |
|---|---|
| `201` | Supplier created |
| `400` | Invalid request |
| `409` | Email already exists |

---

## 8.2 Get Supplier

```http
GET /api/v1/suppliers/{id}
```

---

## 8.3 List Suppliers

```http
GET /api/v1/suppliers
```

### Supported Filters

```text
active
page
size
sort
```

---

## 8.4 Update Supplier

```http
PUT /api/v1/suppliers/{id}
```

### Request — `UpdateSupplierRequest`

```json
{
  "name": "Tech Distribution Argentina S.A.",
  "email": "ventas@techdistribution.com",
  "phone": "+54 223 555 0100"
}
```

### Responses

| Status | Condition |
|---|---|
| `200` | Supplier updated |
| `400` | Invalid request |
| `404` | Supplier does not exist |
| `409` | Email conflicts with another supplier |

---

## 8.5 Change Supplier Status

```http
PATCH /api/v1/suppliers/{id}/status
```

### Request

```json
{
  "active": false
}
```

This endpoint represents logical deletion/reactivation.

---

## 8.6 Physically Delete Supplier

```http
DELETE /api/v1/suppliers/{id}
```

Physical deletion is rejected while relationships that must be preserved still reference the supplier.

### Responses

| Status | Condition |
|---|---|
| `204` | Supplier deleted |
| `404` | Supplier does not exist |
| `409` | Physical deletion is not allowed |

---

# 9. Product-Supplier Relationships

Product-supplier relationships are exposed as a nested resource because they exist in the context of a product.

Base resource:

```text
/api/v1/products/{productId}/suppliers
```

## 9.1 Associate Supplier with Product

```http
POST /api/v1/products/{productId}/suppliers
```

### Request — `CreateProductSupplierRequest`

```json
{
  "supplierId": 5,
  "purchasePrice": 980.50
}
```

### Response — `ProductSupplierResponse`

```json
{
  "id": 42,
  "productId": 10,
  "supplier": {
    "id": 5,
    "name": "Tech Distribution S.A."
  },
  "purchasePrice": 980.50
}
```

### Responses

| Status | Condition |
|---|---|
| `201` | Relationship created |
| `400` | Invalid purchase price |
| `404` | Product or supplier does not exist |
| `409` | Product-supplier relationship already exists |

---

## 9.2 List Suppliers for Product

```http
GET /api/v1/products/{productId}/suppliers
```

---

## 9.3 Update Supplier Purchase Price

```http
PUT /api/v1/products/{productId}/suppliers/{supplierId}
```

### Request — `UpdateProductSupplierRequest`

```json
{
  "purchasePrice": 995.00
}
```

---

## 9.4 Remove Supplier from Product

```http
DELETE /api/v1/products/{productId}/suppliers/{supplierId}
```

### Responses

| Status | Condition |
|---|---|
| `204` | Relationship removed |
| `404` | Relationship does not exist |
| `409` | Removal would violate required historical integrity |

---

# 10. Warehouses

Base resource:

```text
/api/v1/warehouses
```

## 10.1 Address Input

Clients submit address information using an `AddressInput` DTO.

Example:

```json
{
  "street": "Av. Colón",
  "number": "1234",
  "postalCode": "7600",
  "city": "Mar del Plata",
  "region": "Buenos Aires",
  "countryCode": "AR"
}
```

The submitted values are treated as input for validation and geocoding.

The API persists the normalized address returned from the configured address service.

---

## 10.2 Create Warehouse

```http
POST /api/v1/warehouses
```

### Request — `CreateWarehouseRequest`

```json
{
  "code": "MDQ-01",
  "name": "Mar del Plata Main Warehouse",
  "address": {
    "street": "Av. Colón",
    "number": "1234",
    "postalCode": "7600",
    "city": "Mar del Plata",
    "region": "Buenos Aires",
    "countryCode": "AR"
  }
}
```

### Response — `WarehouseResponse`

```json
{
  "id": 3,
  "code": "MDQ-01",
  "name": "Mar del Plata Main Warehouse",
  "active": true,
  "address": {
    "street": "Avenida Colón",
    "number": "1234",
    "postalCode": "B7600",
    "city": "Mar del Plata",
    "region": "Buenos Aires",
    "countryCode": "AR",
    "latitude": -38.0000,
    "longitude": -57.5500
  }
}
```

The address shown above is illustrative. Actual normalized values come from the configured geocoding provider.

### Responses

| Status | Condition |
|---|---|
| `201` | Warehouse created and address validated |
| `400` | Invalid request |
| `409` | Warehouse code already exists |
| `422` | Address cannot be resolved |
| `503` | External address provider unavailable |

No warehouse is persisted if address validation fails.

---

## 10.3 Get Warehouse

```http
GET /api/v1/warehouses/{id}
```

---

## 10.4 List Warehouses

```http
GET /api/v1/warehouses
```

### Supported Filters

```text
active
page
size
sort
```

---

## 10.5 Update Warehouse Data

```http
PUT /api/v1/warehouses/{id}
```

### Request — `UpdateWarehouseRequest`

```json
{
  "code": "MDQ-01",
  "name": "Mar del Plata Central Warehouse"
}
```

Address updates are intentionally separated from general warehouse updates.

---

## 10.6 Update Warehouse Address

```http
PUT /api/v1/warehouses/{id}/address
```

### Request

```json
{
  "street": "Av. Independencia",
  "number": "2500",
  "postalCode": "7600",
  "city": "Mar del Plata",
  "region": "Buenos Aires",
  "countryCode": "AR"
}
```

### Rules

- The new address is validated before persistence.
- If validation fails, the existing address remains unchanged.

### Responses

| Status | Condition |
|---|---|
| `200` | Address validated and updated |
| `400` | Invalid request |
| `404` | Warehouse does not exist |
| `422` | Address cannot be resolved |
| `503` | Address provider unavailable |

---

## 10.7 Change Warehouse Status

```http
PATCH /api/v1/warehouses/{id}/status
```

### Request

```json
{
  "active": false
}
```

This endpoint represents logical deletion/reactivation.

---

## 10.8 Physically Delete Warehouse

```http
DELETE /api/v1/warehouses/{id}
```

### Responses

| Status | Condition |
|---|---|
| `204` | Warehouse physically deleted |
| `404` | Warehouse does not exist |
| `409` | Historical inventory data prevents deletion |

---

# 11. Stocks

Stock quantity is never modified through a generic `POST`, `PUT` or `PATCH` operation.

Quantity changes are exclusively performed by creating stock movements.

Base resource:

```text
/api/v1/stocks
```

## 11.1 Get Stock

```http
GET /api/v1/stocks/{id}
```

### Response — `StockResponse`

```json
{
  "id": 25,
  "product": {
    "id": 10,
    "sku": "NB-LNV-001",
    "name": "Lenovo ThinkPad E14"
  },
  "warehouse": {
    "id": 3,
    "code": "MDQ-01",
    "name": "Mar del Plata Main Warehouse"
  },
  "quantity": 20,
  "minimumStock": 5,
  "lowStock": false
}
```

`lowStock` is a calculated response field:

```text
quantity <= minimumStock
```

---

## 11.2 List Stock by Product

```http
GET /api/v1/products/{productId}/stocks
```

Returns the stock of the product across all warehouses.

---

## 11.3 List Stock by Warehouse

```http
GET /api/v1/warehouses/{warehouseId}/stocks
```

Returns all stock records for the selected warehouse.

Supports pagination.

---

## 11.4 List Low Stock

```http
GET /api/v1/stocks/low-stock
```

Optional filter:

```text
warehouseId
```

Example:

```http
GET /api/v1/stocks/low-stock?warehouseId=3&page=0&size=20
```

---

## 11.5 Update Minimum Stock

```http
PATCH /api/v1/stocks/{id}/minimum-stock
```

### Request — `UpdateMinimumStockRequest`

```json
{
  "minimumStock": 10
}
```

### Rules

- `minimumStock` must be zero or greater.
- Stock quantity is not modified.

### Responses

| Status | Condition |
|---|---|
| `200` | Minimum stock updated |
| `400` | Invalid value |
| `404` | Stock record does not exist |

### Initial Minimum Stock

When a stock record is automatically created by its first inbound movement:

```text
minimumStock = 0
```

It can later be configured using this endpoint.

---

# 12. Stock Movements

Base resource:

```text
/api/v1/stock-movements
```

## 12.1 Register Stock Movement

```http
POST /api/v1/stock-movements
```

### Request — `CreateStockMovementRequest`

```json
{
  "productId": 10,
  "warehouseId": 3,
  "type": "PURCHASE",
  "quantity": 20,
  "reason": "Purchase order PO-2026-001"
}
```

### Rules

- `quantity` must be greater than zero.
- `PURCHASE` increases stock.
- `ADJUSTMENT_IN` increases stock.
- `SALE` decreases stock.
- `ADJUSTMENT_OUT` decreases stock.
- `reason` is mandatory for both adjustment types.
- An inbound movement automatically creates the stock record if none exists.
- An outbound movement with no stock record is rejected.
- An outbound movement that exceeds available stock is rejected.
- Stock modification and movement creation occur in the same transaction.

### Response — `StockMovementResponse`

```json
{
  "id": 150,
  "stockId": 25,
  "productId": 10,
  "warehouseId": 3,
  "type": "PURCHASE",
  "quantity": 20,
  "reason": "Purchase order PO-2026-001",
  "createdAt": "2026-08-07T15:30:00",
  "resultingStockQuantity": 20
}
```

### Responses

| Status | Condition |
|---|---|
| `201` | Movement registered |
| `400` | Invalid movement request |
| `404` | Product or warehouse does not exist |
| `409` | Insufficient stock |

---

## 12.2 Get Stock Movement

```http
GET /api/v1/stock-movements/{id}
```

---

## 12.3 Get Movement History for Stock

```http
GET /api/v1/stocks/{stockId}/movements
```

### Optional Filters

```text
type
from
to
page
size
sort
```

Example:

```http
GET /api/v1/stocks/25/movements?type=SALE&from=2026-08-01T00:00:00&to=2026-08-31T23:59:59&page=0&size=20&sort=createdAt,desc
```

Default movement sorting:

```text
createdAt,desc
```

---

# 13. DTO Summary

## Product

```text
CreateProductRequest
UpdateProductRequest
ProductResponse
ProductSummaryResponse
```

## Category

```text
CreateCategoryRequest
UpdateCategoryRequest
CategoryResponse
CategorySummaryResponse
```

## Supplier

```text
CreateSupplierRequest
UpdateSupplierRequest
SupplierResponse
SupplierSummaryResponse
```

## Product Supplier

```text
CreateProductSupplierRequest
UpdateProductSupplierRequest
ProductSupplierResponse
```

## Warehouse

```text
CreateWarehouseRequest
UpdateWarehouseRequest
WarehouseResponse
WarehouseSummaryResponse
AddressInput
AddressResponse
```

## Stock

```text
StockResponse
UpdateMinimumStockRequest
```

## Stock Movement

```text
CreateStockMovementRequest
StockMovementResponse
```

## Common

```text
UpdateStatusRequest
PageResponse<T>
```

DTOs must be separate from persistence entities. JPA entities are not exposed directly through the HTTP API.

---

# 14. Endpoint Summary

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products/{id}` | Get product |
| GET | `/api/v1/products` | List/filter products |
| PUT | `/api/v1/products/{id}` | Update product |
| PATCH | `/api/v1/products/{id}/status` | Activate/deactivate product |
| DELETE | `/api/v1/products/{id}` | Physically delete product |
| POST | `/api/v1/categories` | Create category |
| GET | `/api/v1/categories/{id}` | Get category |
| GET | `/api/v1/categories` | List categories |
| PUT | `/api/v1/categories/{id}` | Update category |
| POST | `/api/v1/suppliers` | Create supplier |
| GET | `/api/v1/suppliers/{id}` | Get supplier |
| GET | `/api/v1/suppliers` | List suppliers |
| PUT | `/api/v1/suppliers/{id}` | Update supplier |
| PATCH | `/api/v1/suppliers/{id}/status` | Activate/deactivate supplier |
| DELETE | `/api/v1/suppliers/{id}` | Physically delete supplier |
| POST | `/api/v1/products/{productId}/suppliers` | Associate supplier |
| GET | `/api/v1/products/{productId}/suppliers` | List product suppliers |
| PUT | `/api/v1/products/{productId}/suppliers/{supplierId}` | Update purchase price |
| DELETE | `/api/v1/products/{productId}/suppliers/{supplierId}` | Remove relationship |
| POST | `/api/v1/warehouses` | Create warehouse |
| GET | `/api/v1/warehouses/{id}` | Get warehouse |
| GET | `/api/v1/warehouses` | List warehouses |
| PUT | `/api/v1/warehouses/{id}` | Update warehouse |
| PUT | `/api/v1/warehouses/{id}/address` | Validate and update address |
| PATCH | `/api/v1/warehouses/{id}/status` | Activate/deactivate warehouse |
| DELETE | `/api/v1/warehouses/{id}` | Physically delete warehouse |
| GET | `/api/v1/stocks/{id}` | Get stock |
| GET | `/api/v1/products/{productId}/stocks` | Stock by product |
| GET | `/api/v1/warehouses/{warehouseId}/stocks` | Stock by warehouse |
| GET | `/api/v1/stocks/low-stock` | List low stock |
| PATCH | `/api/v1/stocks/{id}/minimum-stock` | Configure minimum stock |
| POST | `/api/v1/stock-movements` | Register stock movement |
| GET | `/api/v1/stock-movements/{id}` | Get movement |
| GET | `/api/v1/stocks/{stockId}/movements` | Stock movement history |

---

# 15. Important API Design Decisions

1. The API is versioned from the beginning using `/api/v1`.
2. Persistence entities are never returned directly by controllers.
3. Logical deletion is modeled as an explicit status change.
4. `DELETE` is reserved for actual physical deletion.
5. Stock quantity cannot be modified directly.
6. Every stock quantity change must pass through the stock movement endpoint.
7. Product-supplier relationships are modeled as nested resources.
8. Warehouse address updates are separated from general warehouse updates because they require an external service call.
9. Address validation failure never leaves a warehouse in a partially updated state.
10. Stock is created automatically by the first valid inbound movement.
11. Automatically created stock begins with `minimumStock = 0`.
12. API errors use `ProblemDetail` instead of a custom ad-hoc error structure.
13. Pagination responses use an application-owned DTO instead of exposing Spring's `Page` structure.
14. Authentication and Spring Security are intentionally outside version 1.
