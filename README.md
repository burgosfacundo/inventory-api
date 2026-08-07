# Inventory API

REST API for inventory, warehouse and stock management built with Java and Spring Boot.

> 🚧 **Status:** In development

The goal of this project is to model real-world inventory operations while applying production-oriented backend practices such as clean separation of responsibilities, database migrations, automated testing, containerization and API documentation.

---

## 🎯 Project Goals

Inventory API manages:

- Products and categories
- Suppliers and product-supplier relationships
- Multiple warehouses
- Stock per product and warehouse
- Stock movement history
- Low-stock monitoring
- Warehouse address validation through an external geocoding service

The project intentionally goes beyond a basic CRUD API.

Stock quantities cannot be modified directly. Every stock change must be represented by a traceable stock movement.

---

## 🧩 Domain Model

The main domain includes:

- `Product`
- `Category`
- `Supplier`
- `ProductSupplier`
- `Warehouse`
- `Address` — Value Object
- `Stock`
- `StockMovement`
- `MovementType`

### UML

![Inventory Domain Model](docs/diagrams/inventory-domain.svg)

The editable diagram is available at:

[`docs/diagrams/inventory-domain.drawio`](docs/diagrams/inventory-domain.drawio)

---

## 🏗️ Architecture

The application is designed as a **modular monolith** using a feature-oriented package structure.

Main dependency flow:

```text
Controller
    ↓
Application Service
    ↓
Repository
    ↓
MySQL
```

External integrations are accessed through application abstractions:

```text
WarehouseService
       ↓
GeocodingService
       ↑
Geoapify Adapter
```

This keeps business logic independent from the external geocoding provider.

---

## 📦 Main Business Rules

Some of the core rules modeled by the API are:

- Product SKU must be unique.
- Supplier email must be unique.
- Warehouse code must be unique.
- Stock is unique per `Product + Warehouse`.
- Stock can never become negative.
- Stock quantity cannot be modified directly.
- Every stock change generates a `StockMovement`.
- Outbound movements are rejected when stock is insufficient.
- The first inbound movement automatically creates the stock record.
- Manual stock adjustments require a reason.
- Stock update and movement registration occur atomically.
- Warehouse addresses must be validated before persistence.
- Physical deletion is rejected when it would destroy historical integrity.

---

## 🛠️ Planned Tech Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- Maven

### Database

- MySQL 8
- Flyway

### Testing

- JUnit 5
- Mockito
- AssertJ
- Testcontainers
- REST Assured
- JaCoCo

### API & Infrastructure

- OpenAPI / Swagger
- Docker
- Docker Compose
- Spring Boot Actuator
- GitHub Actions

### External Integration

- Geoapify

---

## 📚 Documentation

Project design documentation is available under [`docs/`](docs/).

- [Requirements](docs/requirements.md)
- [API Design](docs/api-design.md)
- [Architecture](docs/architecture.md)
- [Domain Model](docs/diagrams/inventory-domain.svg)

---

## 🌐 API

The API will be versioned from the beginning:

```text
/api/v1
```

Planned main resources:

```text
/api/v1/products
/api/v1/categories
/api/v1/suppliers
/api/v1/warehouses
/api/v1/stocks
/api/v1/stock-movements
```

The complete HTTP contract is documented in:

[API Design](docs/api-design.md)

---

## 🧪 Testing Strategy

The project will include different testing levels:

- **Unit tests** for business rules and services
- **Repository integration tests** using a real MySQL container
- **API integration tests** using REST Assured
- **External service isolation** for Geoapify integration tests

Integration tests will use **Testcontainers with MySQL** instead of an in-memory database.

---

## 🐳 Local Development

Docker Compose support will allow the API and MySQL database to be started in a reproducible environment.

Instructions will be added as the implementation progresses.

---

## 🗺️ Roadmap

- [x] Domain modeling
- [x] Functional and business requirements
- [x] REST API contract
- [x] Architecture definition
- [ ] Spring Boot project setup
- [ ] MySQL + Flyway configuration
- [ ] Product and Category implementation
- [ ] Supplier management
- [ ] Warehouse management
- [ ] Geoapify integration
- [ ] Stock management
- [ ] Stock movements and transactional rules
- [ ] Unit tests
- [ ] Integration tests with Testcontainers
- [ ] REST API tests
- [ ] Docker Compose
- [ ] OpenAPI documentation
- [ ] GitHub Actions CI
- [ ] JaCoCo coverage

---

## 👨‍💻 Author

**Facundo Burgos**

- GitHub: [@burgosfacundo](https://github.com/burgosfacundo)
- LinkedIn: [linkedin.com/in/burgosfacundo](https://www.linkedin.com/in/burgosfacundo/)
