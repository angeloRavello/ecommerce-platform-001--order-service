# order-service

Microservice responsible for order management within the ecommerce-platform. Exposes a reactive REST API, persists orders in an in-memory H2 database via R2DBC, and communicates with an external `product-service` over HTTP to look up products and reserve stock.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 + Spring WebFlux |
| Database | H2 (in-memory) via R2DBC |
| HTTP client | WebClient |
| Mapping | MapStruct 1.6 |
| Boilerplate | Lombok |
| Testing | JUnit 5 · Mockito · AssertJ · Reactor Test · MockWebServer |

---

## Architecture

Hexagonal architecture (Ports & Adapters):

```
interfaces/rest          → driving adapter  (HTTP in)
application              → use cases + ports (interfaces)
domain                   → business rules, aggregates, value objects
infrastructure/rest      → driven adapter   (HTTP out → product-service)
infrastructure/persistence → driven adapter (DB out → R2DBC H2)
```

### Package layout

```
pe.angeloravello.orderservice
├── domain
│   ├── model               Order, OrderItem, Money, OrderStatus
│   └── exception           OrderDomainException, ProductNotFoundException,
│                           InsufficientStockException
├── application
│   ├── command             CreateOrderCommand
│   ├── dto                 OrderItemDto, ProductDto
│   ├── repository          OrderRepository, ProductRepository,
│                           StockReservationRepository  ← ports
│   ├── usecase             CreateOrderUseCase, ListOrdersUseCase
│   │   └── impl            CreateOrderUseCaseImpl, ListOrdersUseCaseImpl
│   └── validator           OrderCommandValidator
├── infrastructure
│   ├── config              WebClientConfig
│   ├── persistence         OrderAdapter, OrderWithItemsDto,
│   │   ├── entity          OrderEntity, OrderItemEntity
│   │   ├── mapper          MapStruct mappers (entity ↔ domain)
│   │   └── repository      R2dbcOrderRepository, R2dbcOrderItemRepository
│   └── rest                ProductAdapter, StockReservationAdapter,
│       ├── dto             ProductReservationDto
│       ├── request         ReservationRequest
│       └── response        ProductResponse
└── interfaces/rest         OrderController, GlobalExceptionHandler,
    ├── mapper              MapStruct mappers (request/response ↔ command/domain)
    ├── request             OrderRequest, OrderItemRequest
    └── response            OrderResponse, OrderItemResponse, ErrorResponse
```

---

## API

Base path: `/api/orders`

### POST `/api/orders` — Create order

**Request**
```json
{
  "customerEmail": "customer@example.com",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

**Response `200 OK`**
```json
{
  "id": 1,
  "customerEmail": "customer@example.com",
  "totalAmount": 1999.98,
  "status": "PENDING",
  "createdAt": "2026-03-17T10:30:00",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 999.99,
      "itemPrice": 1999.98
    }
  ]
}
```

**Flow**
1. Validate command (email not blank, items not empty)
2. Fetch product details from `product-service` for each item
3. Reserve stock via `product-service`
4. Place and persist the order

### GET `/api/orders` — List orders

**Response `200 OK`** — array of the same `OrderResponse` shape above.

---

### Error responses

All errors follow this structure:

```json
{
  "status": 404,
  "message": "Product not found with id: 99",
  "timestamp": "2026-03-17T10:30:00"
}
```

| Exception | HTTP status |
|---|---|
| `ProductNotFoundException` | 404 Not Found |
| `InsufficientStockException` | 422 Unprocessable Entity |
| `OrderDomainException` | 400 Bad Request |
| Any other | 500 Internal Server Error |

---

## Domain model

### `Order` (aggregate root)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | null before persistence |
| `customerEmail` | `String` | validated on creation |
| `totalAmount` | `Money` | computed from items |
| `status` | `OrderStatus` | `PENDING` on creation |
| `createdAt` | `LocalDateTime` | set on `place()` |
| `items` | `List<OrderItem>` | at least one required |

Factory methods: `Order.place(email, items)` — enforces invariants · `Order.reconstitute(...)` — rehydrates from persistence.

### `Money` (value object / record)

Immutable wrapper over `BigDecimal`. Rejects null and negative values. Supports `add()` and `multiply(int)`.

### `OrderStatus`

`PENDING` · `CONFIRMED` · `CANCELLED`

---

## Database schema

```sql
CREATE TABLE orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_email VARCHAR(255)  NOT NULL,
    total_amount   DECIMAL(10,2) NOT NULL,
    status         VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT        NOT NULL,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    unit_price   DECIMAL(10,2) NOT NULL,
    quantity     INT           NOT NULL
);
```

Schema is auto-created on startup via `schema.sql` (`spring.sql.init.mode: always`).

---

## Configuration

`src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: order-service
  r2dbc:
    url: r2dbc:h2:mem:///orderdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
  sql:
    init:
      mode: always

product-service:
  base-url: http://localhost:8081   # URL of the product-service

logging:
  level:
    pe.angeloravello: DEBUG
```

The `product-service.base-url` property is injected into `WebClientConfig` and used by both `ProductAdapter` and `StockReservationAdapter`.

---

## External service dependency — `product-service`

| Call | Method | Path | Purpose |
|---|---|---|---|
| Look up product | `GET` | `/api/products/{id}` | Fetch name and price per item |
| Reserve stock | `POST` | `/api/products/reserve` | Reserve quantities before persisting the order |

The service must be running on `product-service.base-url` before order creation works. Listing orders (`GET /api/orders`) does not call the product-service.

---

## Running locally

**Prerequisites**: Java 17, Maven 3.8+, `product-service` running on port 8081.

```bash
./mvnw spring-boot:run
```

The application starts on port `8080` by default. H2 is in-memory — data is reset on each restart.

---

## Tests

```bash
./mvnw test
```

### Test coverage by layer

| Layer | Test class | Framework |
|---|---|---|
| REST controller | `OrderControllerTest` | WebTestClient + Mockito |
| Use case — create | `CreateOrderUseCaseImplTest` | StepVerifier + Mockito |
| Use case — list | `ListOrdersUseCaseImplTest` | StepVerifier + Mockito |
| Persistence adapter | `OrderAdapterTest` | StepVerifier + Mockito |
| HTTP client — products | `ProductAdapterTest` | MockWebServer + StepVerifier |
| HTTP client — stock | `StockReservationAdapterTest` | MockWebServer + StepVerifier |

`ProductAdapterTest` and `StockReservationAdapterTest` spin up a real `MockWebServer` instance per test, so `WebClient` makes actual HTTP calls against a local mock server — no Mockito involved at the HTTP layer.
