# PhiNance - Personal Finance Tracker

A full-stack personal finance management application built with Spring Boot and React.

## Overview

PhiNance helps users manage their personal finances by tracking accounts, transactions, budgets, and savings goals. The application provides comprehensive financial insights through interactive dashboards and detailed reports.

## Features

- **User Management**: Registration, authentication, and profile management with JWT security
- **Account Management**: Multiple account types (Bank, Credit Card, Cash, Investment)
- **Transaction Tracking**: Income, expenses, and transfers with categorization
- **Budget Management**: Monthly budgets with alerts and progress tracking
- **Savings Goals**: Set and track financial goals with contributions
- **Category System**: Hierarchical categories for income and expenses
- **Dashboard**: Visual overview with charts and summaries
- **Reports**: Filter and export transaction data

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.2
- Spring Security with JWT
- Spring Data MongoDB
- SpringDoc OpenAPI (Swagger)
- Lombok

### Frontend
- React 18
- Vite
- TailwindCSS
- Zustand (State Management)
- Chart.js
- React Router DOM

### Database
- MongoDB

## Project Structure

```
phiNance/
??? frontend/              # React frontend application
?   ??? src/
?   ?   ??? components/   # Reusable UI components
?   ?   ??? pages/        # Page components
?   ?   ??? services/     # API services
?   ?   ??? stores/       # State management
?   ??? README.md
?
??? phinance/              # Spring Boot backend
?   ??? src/main/java/com/kerem/phinance/
?   ?   ??? config/       # Security, CORS, OpenAPI configs
?   ?   ??? controller/   # REST controllers
?   ?   ??? dto/          # Data transfer objects
?   ?   ??? exception/    # Exception handling
?   ?   ??? model/        # Entity classes
?   ?   ??? repository/   # MongoDB repositories
?   ?   ??? security/     # JWT authentication
?   ?   ??? service/      # Business logic
?   ??? README.md
?
??? HOMEWORK_ASSIGNMENT.md # Project requirements
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- MongoDB 6.0 or higher
- Maven 3.8+

## Quick Start

### 1. Start MongoDB

Make sure MongoDB is running on `localhost:27017`

### 2. Start the Backend

```bash
cd phinance
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000`

### 4. Access the Application

- Frontend: http://localhost:3000
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | User registration |
| POST | /api/auth/login | User login |
| POST | /api/auth/refresh | Refresh access token |
| GET | /api/accounts | Get user accounts |
| POST | /api/accounts | Create account |
| GET | /api/transactions | Get transactions |
| POST | /api/transactions | Create transaction |
| GET | /api/categories | Get categories |
| POST | /api/categories | Create category |
| GET | /api/budgets | Get budgets |
| POST | /api/budgets | Create budget |
| GET | /api/goals | Get goals |
| POST | /api/goals | Create goal |
| GET | /api/dashboard | Get dashboard data |

## Configuration

### Backend (application.properties)

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/phinance

# JWT
app.jwt.secret=your-secret-key
app.jwt.expiration=3600000
app.jwt.refresh-expiration=604800000

# Server
server.port=8080
```

### Frontend (vite.config.js)

The frontend is configured to proxy API requests to the backend:

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': 'http://localhost:8080'
  }
}
```

## Testing

### Backend Tests

```bash
cd phinance
./mvnw test
```

The project includes 5 unit test classes:
- AuthServiceTest
- TransactionServiceTest
- BudgetServiceTest
- GoalServiceTest
- AccountTransferTest

### Frontend Tests

```bash
cd frontend
npm run test
```

## Building for Production

### Backend

```bash
cd phinance
./mvnw clean package -DskipTests
java -jar target/phinance-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
```

The build output will be in `frontend/dist`

## License

This project is created for educational purposes as part of a homework assignment.

## Author

Kerem
