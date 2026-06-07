# Online Book Store 📚✨

A state-of-the-art, premium online bookstore web application built using **Spring Boot**, **Thymeleaf**, and **Spring Security** with **JWT authentication**. The user interface is designed with a sleek, premium, responsive layout featuring modern aesthetics, subtle micro-animations, and dynamic shopping features.

---

## 🚀 Key Features

* **Sleek Catalog Dashboard**: Browse books categorised under *Technology & Systems*, *Science Fiction*, and *Education & Academics* with full sorting (Price, Title) and search filters.
* **Modern Star Ratings & Reviews**: Real-time customer reviews and star rating submission widgets on every book's detail page.
* **Premium Shopping Cart**: Smooth, dynamic shopping cart interface supporting real-time calculations.
* **Dynamic Checkout Flow**: Automatic tax (5%) and shipping calculations with a free shipping threshold for orders above ₹50.
* **Order Tracking & History**: Track order status and view complete historical order summaries with localized Indian Rupees (`₹`) currency presentation.
* **Interactive AI Support Bot**: Front-end chat widget ready for real-time customer support simulated interactions.
* **JWT-Secured Rest APIs**: Secure endpoint token generation protecting checkout requests and user context.

---

## 🛠️ Technology Stack

* **Back-End**: Java 17+, Spring Boot, Spring Security, JPA Hibernate, MySQL.
* **Front-End**: HTML5, Vanilla CSS3 (custom layouts, glassmorphism, responsive grid), Thymeleaf Templating.
* **Authentication**: JWT (JSON Web Tokens) with Secure Cookie Storage.

---

## ⚙️ How to Run Locally

### 1. Prerequisites
* **Java Development Kit (JDK 17 or above)**
* **Apache Maven**
* **MySQL Database Server**

### 2. Configure Database Connection
Update the `src/main/resources/application.properties` database properties to match your local MySQL configuration:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Build & Run Application
From the project root directory, compile and run the application:
```bash
mvn clean compile
mvn spring-boot:run
```
Once started, open your browser and navigate to `http://localhost:8080/`.

---

## 📁 Project Structure

* `src/main/java`: Backend controllers, models, security setups, and repository layers.
* `src/main/resources/templates`: Thymeleaf HTML views (`index.html`, `details.html`, `cart.html`, `checkout.html`, `orders.html`, etc.).
* `src/main/resources/static`: Client-side stylesheets (`styles.css`), icons, and static assets.
