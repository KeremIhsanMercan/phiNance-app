# PhiNance Backend API

Personal Finance Tracker Application - Spring Boot Backend

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.2**
- **Spring Security** with JWT Authentication
- **Spring Data MongoDB**
- **MongoDB** - Database
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Build tool

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB 6.0+ (running on localhost:27017)

## Project Structure

```
src/main/java/com/kerem/phinance/
??? config/           # Configuration classes
??? controller/       # REST API controllers
??? dto/              # Data Transfer Objects
??? exception/        # Exception handling
??? model/            # MongoDB document entities
??? repository/       # MongoDB repositories
??? security/         # JWT and security components
??? service/          # Business logic services
```

## Installation & Running

### 1. Start MongoDB

Make sure MongoDB is running on `localhost:27017`. You can use Docker:

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 2. Configure Application

Update `src/main/resources/application.properties` with your settings:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/phinance

# JWT Secret (change in production!)
jwt.secret=your-secret-key-here

# Email Configuration (for email verification)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 3. Build and Run

```bash
# Navigate to backend directory
cd phinance

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

Once the application is running, access the Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login user |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/verify-email` | Verify email address |
| POST | `/api/auth/change-password` | Change password |

### Accounts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | Get all accounts |
| GET | `/api/accounts/{id}` | Get account by ID |
| POST | `/api/accounts` | Create new account |
| PUT | `/api/accounts/{id}` | Update account |
| DELETE | `/api/accounts/{id}` | Archive account |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | Get transactions (paginated) |
| GET | `/api/transactions/{id}` | Get transaction by ID |
| GET | `/api/transactions/date-range` | Get by date range |
| POST | `/api/transactions` | Create transaction |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/type/{type}` | Get by type |
| POST | `/api/categories` | Create category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/budgets` | Get budgets by month |
| GET | `/api/budgets/compare` | Compare two months |
| POST | `/api/budgets` | Create budget |
| PUT | `/api/budgets/{id}` | Update budget |
| DELETE | `/api/budgets/{id}` | Delete budget |

### Goals
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/goals` | Get all goals |
| GET | `/api/goals/active` | Get active goals |
| POST | `/api/goals` | Create goal |
| POST | `/api/goals/contribution` | Add contribution |
| PUT | `/api/goals/{id}/complete` | Mark complete |
| DELETE | `/api/goals/{id}` | Delete goal |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard` | Get dashboard data |

## Running Tests

```bash
./mvnw test
```

## Database Schema

### Collections

- **users** - User accounts and authentication
- **accounts** - Financial accounts (bank, credit card, etc.)
- **transactions** - Income, expense, and transfer records
- **categories** - Transaction categories
- **budgets** - Monthly budget allocations
- **goals** - Savings goals
- **goal_contributions** - Goal contribution history
- **saved_filters** - User's saved search filters

## Security

- Passwords are hashed using BCrypt
- JWT tokens for stateless authentication
- CORS configured for frontend origins
- Input validation on all endpoints
