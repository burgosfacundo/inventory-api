# Inventory API — API Design

## 1. Purpose

This document defines the HTTP contract for version 1 of Inventory API.

It reflects the implemented controllers and DTOs and should remain consistent with the generated OpenAPI specification.

The API exposes resources for:

- Categories
- Products
- Suppliers
- Product-supplier relationships
- Warehouses
- Stocks
- Inventory movements
- Stock transfers

---

## 2. General Conventions

### Base path

All application REST controllers are exposed under:

```text
/api/v1
```

Example:

```text
GET /api/v1/products
```

### Content types

Successful JSON requests and responses use:

```text
application/json
```

Error responses use Spring `ProblemDetail` and are returned as:

```text
application/problem+json
```

### Identifiers

Database resource identifiers are represented as positive numeric `Long` values.

Business identifiers remain separate:

- Product → `sku`
- Warehouse → `code`
- Supplier → `email`

### Date and time format

Date-time filters use ISO-8601 values.

Example:

```text
2026-08-01T00:00:00
```

### Validation

Request-body validation uses Jakarta Bean Validation.

Path and query parameter validation is also applied where required.

Malformed request bodies, invalid parameters and unsupported sort values produce `400 Bad Request`.

---

## 3. Common HTTP Status Codes

| Status | Usage |
|---|---|
| `200 OK` | Successful read, list or update |
| `201 Created` | Resource successfully created |
| `204 No Content` | Successful physical deletion |
| `400 Bad Request` | Request validation, malformed JSON, invalid pagination/sorting or invalid operation input |
| `404 Not Found` | Requested resource or referenced resource does not exist |
| `409 Conflict` | Unique constraint or business-state conflict, including insufficient stock |
| `422 Unprocessable Content` | Warehouse address cannot be validated/resolved |
| `503 Service Unavailable` | External address provider is temporarily unavailable |
| `500 Internal Server Error` | Unexpected unhandled server failure |

---

## 4. Error Contract

The API uses Spring `ProblemDetail`.

Typical response:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Product with SKU 'NB-LNV-001' already exists.",
  "instance": "/api/v1/products",
  "errorCode": "PRODUCT_SKU_ALREADY_EXISTS"
}
```

The custom extension:

```text
errorCode
```

is included in application-generated error responses.

### Body validation errors

Invalid request DTOs return:

```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/products",
  "errorCode": "VALIDATION_ERROR",
  "fieldErrors": {
    "name": "Name is required"
  }
}
```

### Method/query/path validation errors

Invalid query or path parameters may include:

```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/products",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    "Size cannot be greater than 100"
  ]
}
```

### Malformed JSON

Malformed or unreadable request bodies return:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request body is malformed or unreadable",
  "instance": "/api/v1/products",
  "errorCode": "MALFORMED_REQUEST"
}
```

### Unknown resource

Requests for unmapped resources return:

```text
404 RESOURCE_NOT_FOUND
```

### Unexpected error

Unexpected errors are mapped to:

```text
500 INTERNAL_SERVER_ERROR
```

---

## 5. Pagination and Sorting

Endpoints that may return large result sets use Spring Data's `Page<T>` directly.

There is no application-owned `PageResponse<T>` wrapper in version 1.

### Common pagination parameters

```text
page
size
sortBy
direction
```

Defaults for most paginated endpoints:

```text
page = 0
size = 20
sortBy = id
direction = asc
```

Constraints:

```text
page >= 0
1 <= size <= 100
direction = asc | desc
```

Each controller defines its own allowed `sortBy` values.

Unsupported sort fields or sort directions produce `400 Bad Request`.

### Page response

Paginated endpoints return the Spring `Page` JSON representation containing the resource list and pagination metadata.

Clients should primarily rely on fields such as:

```text
content
number
size
totalElements
totalPages
first
last
numberOfElements
```

---

## 6. Categories

Base resource:

```text
/api/v1/categories
```

Categories are reference data and are returned as a complete list rather than a paginated page.

### 6.1 Create Category

```http
POST /api/v1/categories
```

### Request — `CategoryRequest`

```json
{
  "name": "Notebooks",
  "description": "Portable computers"
}
```

Rules:

- `name` is required
- `name` maximum length is 100
- `description` maximum length is 255
- category name does not need to be unique

### Response — `201 Created`

```json
{
  "id": 1,
  "name": "Notebooks",
  "description": "Portable computers"
}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request |

---

### 6.2 Get Category

```http
GET /api/v1/categories/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Category does not exist |

---

### 6.3 List Categories

```http
GET /api/v1/categories
```

Response:

```text
List<CategoryResponse>
```

---

### 6.4 Update Category

```http
PUT /api/v1/categories/{id}
```

Uses the same `CategoryRequest` structure as creation.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Category does not exist |

Category deletion is not exposed in version 1.

---

## 7. Products

Base resource:

```text
/api/v1/products
```

### 7.1 Create Product

```http
POST /api/v1/products
```

### Request — `ProductRequest`

```json
{
  "sku": "NB-LNV-001",
  "name": "Lenovo ThinkPad E14",
  "description": "14-inch business notebook",
  "salePrice": 1250.00,
  "categoryId": 1
}
```

Rules:

- `sku` is required, maximum 50 characters and unique
- `name` is required, maximum 100 characters
- `salePrice` is required and cannot be negative
- `categoryId` is required, positive and must reference an existing category
- new products are created as active

### Response — `201 Created`

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

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request |
| `404` | Category does not exist |
| `409` | SKU already exists |

---

### 7.2 Get Product

```http
GET /api/v1/products/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Product does not exist |

---

### 7.3 List Products

```http
GET /api/v1/products
```

Optional filters:

```text
active
categoryId
```

Pagination and sorting:

```text
page
size
sortBy
direction
```

Allowed sort fields:

```text
id
sku
name
salePrice
active
```

Defaults:

```text
page=0
size=20
sortBy=id
direction=asc
```

Example:

```http
GET /api/v1/products?categoryId=1&active=true&page=0&size=20&sortBy=name&direction=asc
```

Response:

```text
Page<ProductResponse>
```

---

### 7.4 Update Product

```http
PUT /api/v1/products/{id}
```

Uses the same `ProductRequest` structure as creation.

The active status is not part of `ProductRequest`.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Product or category does not exist |
| `409` | SKU conflicts with another product |

---

### 7.5 Update Product Status

```http
PATCH /api/v1/products/{id}/status?active=false
```

The status is supplied as a required query parameter:

```text
active=true
```

or:

```text
active=false
```

Example:

```http
PATCH /api/v1/products/10/status?active=false
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier or missing/invalid query parameter |
| `404` | Product does not exist |

---

### 7.6 Delete Product

```http
DELETE /api/v1/products/{id}
```

Successful response:

```text
204 No Content
```

Possible documented application errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Product does not exist |

Database foreign-key constraints may reject deletion when the product is referenced by stock, inventory history, transfers or product-supplier data.

---

## 8. Suppliers

Base resource:

```text
/api/v1/suppliers
```

### 8.1 Create Supplier

```http
POST /api/v1/suppliers
```

### Request — `SupplierRequest`

```json
{
  "name": "Tech Distribution S.A.",
  "email": "sales@techdistribution.com",
  "phone": "+54 223 555 0100",
  "description": "Hardware distributor"
}
```

Rules:

- `name` is required, maximum 100 characters
- `email` is required, valid, maximum 100 characters and unique
- `phone` maximum length is 25
- `description` maximum length is 255
- new suppliers are created as active

### Response — `201 Created`

```json
{
  "id": 5,
  "name": "Tech Distribution S.A.",
  "email": "sales@techdistribution.com",
  "phone": "+54 223 555 0100",
  "description": "Hardware distributor",
  "active": true
}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request |
| `409` | Email already exists |

---

### 8.2 Get Supplier

```http
GET /api/v1/suppliers/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Supplier does not exist |

---

### 8.3 List Suppliers

```http
GET /api/v1/suppliers
```

Optional filter:

```text
active
```

Allowed sort fields:

```text
id
name
email
active
```

Defaults:

```text
page=0
size=20
sortBy=id
direction=asc
```

Response:

```text
Page<SupplierResponse>
```

---

### 8.4 Update Supplier

```http
PUT /api/v1/suppliers/{id}
```

Uses the same `SupplierRequest` structure as creation.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Supplier does not exist |
| `409` | Email conflicts with another supplier |

---

### 8.5 Update Supplier Status

```http
PATCH /api/v1/suppliers/{id}/status?active=false
```

The `active` value is supplied as a required query parameter.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier or missing/invalid query parameter |
| `404` | Supplier does not exist |

---

### 8.6 Delete Supplier

```http
DELETE /api/v1/suppliers/{id}
```

Successful response:

```text
204 No Content
```

Possible documented application errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Supplier does not exist |

Database referential integrity may prevent deletion while product-supplier relationships reference the supplier.

---

## 9. Product-Supplier Relationships

Base resource:

```text
/api/v1/product-suppliers
```

The implemented API treats `ProductSupplier` as its own resource.

It is not exposed through the earlier nested design:

```text
/products/{productId}/suppliers
```

### 9.1 Create Product-Supplier Relationship

```http
POST /api/v1/product-suppliers
```

### Request — `ProductSupplierRequest`

```json
{
  "productId": 10,
  "supplierId": 5,
  "purchasePrice": 980.50
}
```

Rules:

- `productId` is required and positive
- `supplierId` is required and positive
- `purchasePrice` is required and cannot be negative
- referenced product and supplier must exist
- product-supplier pair must be unique

### Response — `201 Created`

```json
{
  "id": 42,
  "product": {
    "id": 10,
    "sku": "NB-LNV-001",
    "name": "Lenovo ThinkPad E14"
  },
  "supplier": {
    "id": 5,
    "name": "Tech Distribution S.A.",
    "email": "sales@techdistribution.com"
  },
  "purchasePrice": 980.50
}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request |
| `404` | Product or supplier does not exist |
| `409` | Relationship already exists |

---

### 9.2 Get Product-Supplier Relationship

```http
GET /api/v1/product-suppliers/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Relationship does not exist |

---

### 9.3 List Product-Supplier Relationships

```http
GET /api/v1/product-suppliers
```

Optional filters:

```text
productId
supplierId
```

Allowed sort fields:

```text
id
product.id
supplier.id
```

Defaults:

```text
page=0
size=20
sortBy=id
direction=asc
```

Example:

```http
GET /api/v1/product-suppliers?productId=10&page=0&size=20
```

Response:

```text
Page<ProductSupplierResponse>
```

---

### 9.4 Update Purchase Price

```http
PUT /api/v1/product-suppliers/{id}/purchase-price
```

### Request — `ProductSupplierPriceRequest`

```json
{
  "purchasePrice": 995.00
}
```

Rules:

- `purchasePrice` is required
- `purchasePrice` cannot be negative

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Relationship does not exist |
| `409` | Business conflict |

---

### 9.5 Delete Product-Supplier Relationship

```http
DELETE /api/v1/product-suppliers/{id}
```

Successful response:

```text
204 No Content
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Relationship does not exist |

---

## 10. Warehouses

Base resource:

```text
/api/v1/warehouses
```

Warehouse creation and update both validate the complete submitted address before persistence.

There is no separate version 1 endpoint for updating only the address.

### 10.1 Warehouse Address Input

### `AddressRequest`

```json
{
  "street": "Av. Colón",
  "number": "1234",
  "postalCode": "7600",
  "city": "Mar del Plata",
  "province": "Buenos Aires",
  "countryCode": "AR"
}
```

Rules:

- all fields are required
- `countryCode` must contain exactly two characters
- address data is passed through the configured `AddressValidator`

The resulting persisted address also contains latitude and longitude.

---

### 10.2 Create Warehouse

```http
POST /api/v1/warehouses
```

### Request — `WarehouseRequest`

```json
{
  "code": "MDQ-01",
  "name": "Mar del Plata Main Warehouse",
  "address": {
    "street": "Av. Colón",
    "number": "1234",
    "postalCode": "7600",
    "city": "Mar del Plata",
    "province": "Buenos Aires",
    "countryCode": "AR"
  }
}
```

Rules:

- `code` is required, maximum 50 characters and unique
- `name` is required, maximum 100 characters
- `address` is required and validated before persistence

### Response — `201 Created`

```json
{
  "id": 3,
  "code": "MDQ-01",
  "name": "Mar del Plata Main Warehouse",
  "address": {
    "street": "Avenida Colón",
    "number": "1234",
    "postalCode": "7600",
    "city": "Mar del Plata",
    "province": "Buenos Aires",
    "countryCode": "AR",
    "latitude": -38.0000000,
    "longitude": -57.5500000
  }
}
```

The normalized address fields shown above are illustrative.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request |
| `409` | Warehouse code already exists |
| `422` | Address cannot be resolved |
| `503` | External address provider is unavailable |

No invalid warehouse state is persisted when address validation fails.

---

### 10.3 Get Warehouse

```http
GET /api/v1/warehouses/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Warehouse does not exist |

---

### 10.4 List Warehouses

```http
GET /api/v1/warehouses
```

Allowed sort fields:

```text
id
code
name
```

Defaults:

```text
page=0
size=20
sortBy=id
direction=asc
```

Response:

```text
Page<WarehouseResponse>
```

---

### 10.5 Update Warehouse

```http
PUT /api/v1/warehouses/{id}
```

Uses the complete `WarehouseRequest`, including address.

The address is validated before the updated state is applied.

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Warehouse does not exist |
| `409` | Warehouse code conflicts with another warehouse |
| `422` | Address cannot be resolved |
| `503` | External address provider is unavailable |

---

### 10.6 Delete Warehouse

```http
DELETE /api/v1/warehouses/{id}
```

Successful response:

```text
204 No Content
```

Possible documented application errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Warehouse does not exist |

Database foreign-key constraints may prevent deletion when the warehouse is referenced by stock, inventory movements or stock transfers.

Warehouses do not expose an active/inactive status endpoint in version 1.

---

## 11. Stocks

Base resource:

```text
/api/v1/stocks
```

Stock quantity is not modified through a generic stock create/update operation.

Quantity changes occur through:

```text
InventoryMovement
StockTransfer
```

### 11.1 Get Stock

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

`lowStock` is calculated as:

```text
quantity <= minimumStock
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Stock does not exist |

---

### 11.2 List Stocks

```http
GET /api/v1/stocks
```

Optional filters:

```text
productId
warehouseId
```

Allowed sort fields:

```text
id
quantity
minimumStock
product.id
warehouse.id
```

Defaults:

```text
page=0
size=20
sortBy=id
direction=asc
```

Example:

```http
GET /api/v1/stocks?productId=10&warehouseId=3
```

Response:

```text
Page<StockResponse>
```

---

### 11.3 List Low Stock

```http
GET /api/v1/stocks/low-stock
```

Optional filter:

```text
warehouseId
```

Uses the same pagination and sort fields as the general stock list.

Response:

```text
Page<StockResponse>
```

---

### 11.4 Update Minimum Stock

```http
PATCH /api/v1/stocks/{id}/minimum-stock
```

### Request — `StockMinimumRequest`

```json
{
  "minimumStock": 10
}
```

Rules:

- `minimumStock` is required
- value must be zero or greater
- stock quantity is not modified

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or identifier |
| `404` | Stock does not exist |

When stock is created automatically by an inbound operation:

```text
minimumStock = 0
```

---

## 12. Inventory Movements

Base resource:

```text
/api/v1/inventory-movements
```

Inventory movements represent traceable stock changes.

Supported types:

```text
IN
OUT
```

There are no version 1 `PURCHASE`, `SALE`, `ADJUSTMENT_IN` or `ADJUSTMENT_OUT` movement types.

There is no `reason` field in the final movement contract.

### 12.1 Register Inventory Movement

```http
POST /api/v1/inventory-movements
```

### Request — `InventoryMovementRequest`

```json
{
  "productId": 10,
  "warehouseId": 3,
  "type": "IN",
  "quantity": 20
}
```

Rules:

- `productId` is required and positive
- `warehouseId` is required and positive
- `type` is required and must be `IN` or `OUT`
- `quantity` is required and greater than zero
- `IN` increases stock
- `OUT` decreases stock
- first valid `IN` creates stock automatically when none exists
- automatically created stock starts with `minimumStock = 0`
- `OUT` requires existing stock
- `OUT` cannot exceed available stock
- stock modification and movement persistence are transactional

### Response — `201 Created`

```json
{
  "id": 150,
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
  "type": "IN",
  "quantity": 20,
  "createdAt": "2026-08-20T11:30:00"
}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid movement request |
| `404` | Product, warehouse or required stock does not exist |
| `409` | Business conflict such as insufficient stock |

---

### 12.2 Get Inventory Movement

```http
GET /api/v1/inventory-movements/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Inventory movement does not exist |

---

### 12.3 List Inventory Movements

```http
GET /api/v1/inventory-movements
```

Optional filters:

```text
productId
warehouseId
type
from
to
```

Date filters use ISO-8601 date-time values.

Example:

```http
GET /api/v1/inventory-movements?productId=10&type=OUT&from=2026-08-01T00:00:00&to=2026-08-31T23:59:59
```

Rules:

- if both `from` and `to` are supplied, `from` cannot be after `to`

Allowed sort fields:

```text
id
quantity
createdAt
product.id
warehouse.id
```

Defaults:

```text
page=0
size=20
sortBy=createdAt
direction=desc
```

Response:

```text
Page<InventoryMovementResponse>
```

Inventory movements do not expose update or delete operations.

---

## 13. Stock Transfers

Base resource:

```text
/api/v1/stock-transfers
```

A stock transfer moves one product between two different warehouses and records the operation as a historical transfer plus inventory movements.

### 13.1 Create Stock Transfer

```http
POST /api/v1/stock-transfers
```

### Request — `StockTransferRequest`

```json
{
  "productId": 10,
  "sourceWarehouseId": 3,
  "destinationWarehouseId": 4,
  "quantity": 5
}
```

Rules:

- all three IDs are required and positive
- quantity is required and greater than zero
- source and destination warehouses must be different
- source stock must exist
- source stock must contain sufficient quantity
- destination stock is increased when present
- destination stock is created with `minimumStock = 0` when absent
- the transfer creates an `OUT` movement in the source warehouse
- the transfer creates an `IN` movement in the destination warehouse
- source stock, destination stock, both movements and the transfer record are handled transactionally

### Response — `201 Created`

```json
{
  "id": 30,
  "product": {
    "id": 10,
    "sku": "NB-LNV-001",
    "name": "Lenovo ThinkPad E14"
  },
  "sourceWarehouse": {
    "id": 3,
    "code": "MDQ-01",
    "name": "Mar del Plata Main Warehouse"
  },
  "destinationWarehouse": {
    "id": 4,
    "code": "BA-01",
    "name": "Buenos Aires Warehouse"
  },
  "quantity": 5,
  "createdAt": "2026-08-20T11:35:00"
}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid request or source and destination warehouses are the same |
| `404` | Product, warehouse or source stock does not exist |
| `409` | Insufficient stock |

---

### 13.2 Get Stock Transfer

```http
GET /api/v1/stock-transfers/{id}
```

Possible errors:

| Status | Condition |
|---|---|
| `400` | Invalid identifier |
| `404` | Stock transfer does not exist |

---

### 13.3 List Stock Transfers

```http
GET /api/v1/stock-transfers
```

Optional filters:

```text
productId
sourceWarehouseId
destinationWarehouseId
from
to
```

Example:

```http
GET /api/v1/stock-transfers?productId=10&sourceWarehouseId=3&from=2026-08-01T00:00:00
```

Rules:

- if both `from` and `to` are supplied, `from` cannot be after `to`

Allowed sort fields:

```text
id
quantity
createdAt
product.id
sourceWarehouse.id
destinationWarehouse.id
```

Defaults:

```text
page=0
size=20
sortBy=createdAt
direction=desc
```

Response:

```text
Page<StockTransferResponse>
```

Stock transfers do not expose update or delete operations.

---

## 14. DTO Summary

### Category

```text
CategoryRequest
CategoryResponse
CategorySummaryResponse
```

### Product

```text
ProductRequest
ProductResponse
ProductSummaryResponse
```

### Supplier

```text
SupplierRequest
SupplierResponse
```

### Product Supplier

```text
ProductSupplierRequest
ProductSupplierPriceRequest
ProductSupplierResponse
SupplierSummaryResponse
```

### Warehouse

```text
WarehouseRequest
WarehouseResponse
WarehouseSummaryResponse
AddressRequest
AddressResponse
```

### Stock

```text
StockMinimumRequest
StockResponse
```

### Inventory Movement

```text
InventoryMovementRequest
InventoryMovementResponse
```

### Stock Transfer

```text
StockTransferRequest
StockTransferResponse
```

Persistence entities are not returned directly through controllers.

---

## 15. Endpoint Summary

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/categories` | Create category |
| GET | `/api/v1/categories/{id}` | Get category |
| GET | `/api/v1/categories` | List categories |
| PUT | `/api/v1/categories/{id}` | Update category |
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products/{id}` | Get product |
| GET | `/api/v1/products` | List/filter products |
| PUT | `/api/v1/products/{id}` | Update product |
| PATCH | `/api/v1/products/{id}/status?active={boolean}` | Activate/deactivate product |
| DELETE | `/api/v1/products/{id}` | Delete product |
| POST | `/api/v1/suppliers` | Create supplier |
| GET | `/api/v1/suppliers/{id}` | Get supplier |
| GET | `/api/v1/suppliers` | List/filter suppliers |
| PUT | `/api/v1/suppliers/{id}` | Update supplier |
| PATCH | `/api/v1/suppliers/{id}/status?active={boolean}` | Activate/deactivate supplier |
| DELETE | `/api/v1/suppliers/{id}` | Delete supplier |
| POST | `/api/v1/product-suppliers` | Create product-supplier relationship |
| GET | `/api/v1/product-suppliers/{id}` | Get product-supplier relationship |
| GET | `/api/v1/product-suppliers` | List/filter product-supplier relationships |
| PUT | `/api/v1/product-suppliers/{id}/purchase-price` | Update purchase price |
| DELETE | `/api/v1/product-suppliers/{id}` | Delete product-supplier relationship |
| POST | `/api/v1/warehouses` | Create warehouse |
| GET | `/api/v1/warehouses/{id}` | Get warehouse |
| GET | `/api/v1/warehouses` | List warehouses |
| PUT | `/api/v1/warehouses/{id}` | Update warehouse and validated address |
| DELETE | `/api/v1/warehouses/{id}` | Delete warehouse |
| GET | `/api/v1/stocks/{id}` | Get stock |
| GET | `/api/v1/stocks` | List/filter stocks |
| GET | `/api/v1/stocks/low-stock` | List low-stock records |
| PATCH | `/api/v1/stocks/{id}/minimum-stock` | Update minimum stock |
| POST | `/api/v1/inventory-movements` | Register IN/OUT inventory movement |
| GET | `/api/v1/inventory-movements/{id}` | Get inventory movement |
| GET | `/api/v1/inventory-movements` | List/filter inventory movements |
| POST | `/api/v1/stock-transfers` | Transfer stock between warehouses |
| GET | `/api/v1/stock-transfers/{id}` | Get stock transfer |
| GET | `/api/v1/stock-transfers` | List/filter stock transfers |

---

## 16. OpenAPI and Operational Endpoints

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

OpenAPI YAML:

```text
http://localhost:8080/v3/api-docs.yaml
```

Actuator health:

```text
http://localhost:8080/actuator/health
```

Application info:

```text
http://localhost:8080/actuator/info
```

These infrastructure endpoints are not prefixed with `/api/v1`.

---

## 17. Final API Design Decisions

1. The application API is versioned under `/api/v1`.
2. Springdoc/Swagger and Actuator infrastructure endpoints remain outside the API base path.
3. Persistence entities are never exposed directly by controllers.
4. Errors use Spring `ProblemDetail` with an `errorCode` extension.
5. Categories are returned as a complete list.
6. Paginated application resources return Spring Data `Page<T>` directly.
7. Page size is limited to 100.
8. Sort fields are explicitly whitelisted per controller.
9. Product and supplier activation status are updated through a required `active` query parameter.
10. Warehouse does not expose an active/inactive lifecycle.
11. Warehouse update includes the complete address and validates it before persistence.
12. Product-supplier relationships are exposed through `/product-suppliers`, not a nested product resource.
13. Stock quantity cannot be modified directly through the stock controller.
14. Minimum stock is independently configurable.
15. Inventory movements use only `IN` and `OUT`.
16. Inventory movements have no `reason` field in version 1.
17. Inventory movements support product, warehouse, type and date-range filters.
18. Warehouse stock transfers are a first-class version 1 resource.
19. Every transfer records matching `OUT` and `IN` inventory movements.
20. Inventory movements and stock transfers are historical resources with no update or delete endpoints.
21. OpenAPI generated from the implemented application is the authoritative machine-readable HTTP contract.