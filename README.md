# Book Worm E-Commerce Bookstore Platform (Spring Boot 3 + Java 21/26 + PostgreSQL)

A complete RESTful API implementation for the **Book Worm** online bookstore based on the customer journey wireframe.

---

## 🛠️ Tech Stack & Requirements
- **Language / Runtime:** Java 26
- **Framework:** Spring Boot 3.4.3
- **Database:** PostgreSQL (running on port 3306)
- **Security:** Spring Security + Stateless JWT (`jjwt` 0.12.6) + BCrypt
- **API Documentation:** SpringDoc OpenAPI 3 / Swagger UI (`v2.8.5`)
- **Build Tool:** Maven

---

## 🏗️ Architecture & Project Structure

```
├── pom.xml                               # Project dependencies & build config
├── openapi.yaml                          # Full OpenAPI 3.0 specification contract
└── src/main/java/com/bookworm/
    ├── BookwormApplication.java          # Spring Boot main runner
    ├── config/
    │   ├── OpenApiConfig.java            # Swagger / OpenAPI custom bean & JWT scheme
    │   └── DataSeeder.java               # Auto-seeds categories, authors & books from wireframe
    ├── controller/
    │   ├── AuthController.java           # Step 1: Login, Register, Logout
    │   ├── RecommendationController.java # Step 2: Order History, "Buy In Again", Recommendations
    │   ├── CatalogController.java        # Steps 3, 5, 6, 7: Categories, Brands, Delivery Dates, Related
    │   ├── BasketController.java         # Step 8, 9, 10: Basket, Address selection, Gift Points
    │   ├── AddressAndLoyaltyController.java # Saved addresses & Loyalty balance
    │   ├── PaymentController.java        # Step 10, 11: Payment initiation & verification
    │   └── OrderController.java          # Step 11, 12: Order placement, receipt & 48-hr cancellation
    ├── dto/
    │   └── DTOs.java                     # Strongly typed Java Record DTOs
    ├── exception/
    │   └── GlobalExceptionHandler.java   # Centralized error response handling
    ├── model/                            # JPA Entities (MySQL schema mapping)
    │   ├── User.java
    │   ├── Category.java
    │   ├── Brand.java
    │   ├── Product.java
    │   ├── Address.java
    │   ├── Basket.java & BasketItem.java
    │   ├── GiftPoints.java
    │   ├── Payment.java
    │   └── Order.java & OrderItem.java
    ├── repository/                       # Spring Data JPA Repositories
    ├── security/                         # JWT Filter, Token Provider & SecurityFilterChain
    └── service/                          # Business logic layer
```

---

## 🚀 Getting Started

### 1. Configure PostgreSQL Database
Update `src/main/resources/application.properties` if your PostgreSQL password is not `admin`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:3306/postgres
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```

### 2. Build & Run Application
```powershell
mvn clean install
mvn spring-boot:run
```

---

## 📖 Interactive Swagger UI & Seed Data

Once started, navigate to:
- **Swagger UI:** `http://localhost:8085/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8085/v3/api-docs`

### Pre-seeded Demo Account:
- **Email:** `customer@example.com`
- **Password:** `Password123!`
- **Gift Points:** `150` points pre-credited
