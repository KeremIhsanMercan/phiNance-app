# phiNance - Personal Finance Management System

A comprehensive full-stack web application for managing personal finances, tracking expenses, setting budgets, and achieving financial goals.

## Project Description

phiNance is a modern personal finance management application that helps users take control of their financial life. The application provides intuitive tools for:

- **Account Management**: Track multiple bank accounts, credit cards, and cash accounts
- **Transaction Tracking**: Record and categorize income and expenses with detailed metadata
- **Budget Planning**: Set monthly budgets by category and monitor spending
- **Goal Setting**: Define financial goals with target amounts and track progress with contributions
- **Recurring Transactions**: Automate regular income and expenses
- **Category Organization**: Organize transactions with customizable categories
- **File Attachments**: Upload receipts and documents for transactions
- **Dashboard Analytics**: Visualize financial data with comprehensive statistics and insights

## Technology Stack

### Backend
- **Java 18** - Core programming language
- **Spring Boot 3.2.2** - Application framework
- **Spring Security** - Authentication and authorization with JWT
- **Spring Data MongoDB** - Database integration with transaction support
- **MongoDB 8.2** - NoSQL database with replica set for transactions
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management and build tool

### Frontend
- **React 18** - UI library
- **Vite** - Fast build tool and dev server
- **React Router** - Client-side routing
- **Zustand** - State management
- **TailwindCSS** - Utility-first CSS framework
- **Lucide React** - Icon library
- **Recharts** - Data visualization

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **SecurityUtils** - Secure user context management

## Installation Instructions

### Prerequisites
1. **Java 18 or higher** - [Download JDK](https://www.oracle.com/java/technologies/downloads/)
2. **Node.js 18+** and **npm** - [Download Node.js](https://nodejs.org/)
3. **MongoDB 8.2** - [Download MongoDB](https://www.mongodb.com/try/download/community)
4. **MongoDB Shell (mongosh)** - [Download mongosh](https://www.mongodb.com/try/download/shell)
5. **Git** - [Download Git](https://git-scm.com/downloads)

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd phiNance-app
```

### Step 2: Setup MongoDB Replica Set

MongoDB replica set is required for transaction support.

#### Windows
```powershell
# Create data directory
mkdir C:\data\db

# Start MongoDB with replica set (care for your mongod.exe path)
& "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe" --dbpath C:\data\db --replSet rs0

# In a new terminal, initialize replica set
& "path\to\mongosh.exe" --eval "rs.initiate()"

# Verify replica set status
& "path\to\mongosh.exe" --eval "rs.status()"
```

#### Linux/Mac
```bash
# Create data directory
sudo mkdir -p /data/db
sudo chown -R $USER /data/db

# Start MongoDB with replica set
mongod --dbpath /data/db --replSet rs0

# In a new terminal, initialize replica set
mongosh --eval "rs.initiate()"

# Verify replica set status
mongosh --eval "rs.status()"
```

### Step 3: Configure Backend

No additional configuration needed. The application uses default MongoDB connection:
- **Host**: localhost
- **Port**: 27017
- **Database**: phinance (created automatically)

### Step 4: Install Backend Dependencies
```bash
cd phiNance/phinance
# Dependencies are automatically downloaded by Maven
```

### Step 5: Install Frontend Dependencies
```bash
cd ../frontend
npm install
```

## How to Run the Application

### Option 1: Development Mode (Recommended)

#### Terminal 1: Start MongoDB (if not running)
```bash
# Windows
& "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe" --dbpath C:\data\db --replSet rs0

# Linux/Mac
mongod --dbpath /data/db --replSet rs0
```

#### Terminal 2: Start Backend
```bash
cd phiNance/phinance
./mvnw spring-boot:run     # Linux/Mac
.\mvnw.cmd spring-boot:run # Windows
```

Backend will start on: **http://localhost:8080**

#### Terminal 3: Start Frontend
```bash
cd phiNance/frontend
npm run dev
```

Frontend will start on: **http://localhost:3000**

### Option 2: Production Build

#### Build Backend
```bash
cd phiNance/phinance
./mvnw clean package     # Linux/Mac
.\mvnw.cmd clean package # Windows

# Run the JAR
java -jar target/phinance-0.0.1-SNAPSHOT.jar
```

#### Build Frontend
```bash
cd phiNance/frontend
npm run build

# Serve with a static file server
npm install -g serve
serve -s dist
```

## Default Access

After starting the application:
1. Navigate to **http://localhost:3000**
2. Click **Register** to create a new account
3. Login with your credentials

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}

Response: 200 OK
{
  "token": "eyJhbGc...",
  "email": "user@example.com",
  "name": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "token": "eyJhbGc...",
  "email": "user@example.com",
  "name": "John Doe"
}
```

### Account Endpoints

#### Get All Accounts
```http
GET /api/accounts
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "Checking Account",
    "type": "CHECKING",
    "balance": 5000.00,
    "currency": "USD",
    "institution": "Bank of America",
    "createdAt": "2026-01-15T10:30:00Z",
    "updatedAt": "2026-02-04T15:20:00Z"
  }
]
```

#### Create Account
```http
POST /api/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Savings Account",
  "type": "SAVINGS",
  "balance": 10000.00,
  "currency": "USD",
  "institution": "Chase Bank"
}

Response: 201 CREATED
```

#### Update Account
```http
PUT /api/accounts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Account Name",
  "balance": 5500.00
}

Response: 200 OK
```

#### Delete Account
```http
DELETE /api/accounts/{id}
Authorization: Bearer {token}

Response: 204 NO CONTENT
```

### Transaction Endpoints

#### Get All Transactions
```http
GET /api/transactions?page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 100,
  "size": 20,
  "number": 0
}
```

#### Create Transaction
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "accountId": "507f1f77bcf86cd799439011",
  "categoryId": "507f1f77bcf86cd799439012",
  "type": "EXPENSE",
  "amount": 50.00,
  "description": "Grocery shopping",
  "date": "2026-02-04",
  "tags": ["food", "essentials"]
}

Response: 201 CREATED
```

#### Get Transaction by ID
```http
GET /api/transactions/{id}
Authorization: Bearer {token}

Response: 200 OK
```

#### Update Transaction
```http
PUT /api/transactions/{id}
Authorization: Bearer {token}
Content-Type: application/json

Response: 200 OK
```

#### Delete Transaction
```http
DELETE /api/transactions/{id}
Authorization: Bearer {token}

Response: 204 NO CONTENT
```

### Budget Endpoints

#### Get All Budgets
```http
GET /api/budgets
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": "507f1f77bcf86cd799439013",
    "categoryId": "507f1f77bcf86cd799439012",
    "amount": 500.00,
    "spent": 250.00,
    "period": "MONTHLY",
    "startDate": "2026-02-01",
    "endDate": "2026-02-29"
  }
]
```

#### Create Budget
```http
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": "507f1f77bcf86cd799439012",
  "amount": 500.00,
  "period": "MONTHLY",
  "startDate": "2026-02-01",
  "endDate": "2026-02-29"
}

Response: 201 CREATED
```

### Goal Endpoints

#### Get All Goals
```http
GET /api/goals
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": "507f1f77bcf86cd799439014",
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currentAmount": 3000.00,
    "deadline": "2026-12-31",
    "status": "IN_PROGRESS"
  }
]
```

#### Add Contribution to Goal
```http
POST /api/goals/{id}/contributions
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 500.00,
  "note": "Monthly savings"
}

Response: 200 OK
```

### Category Endpoints

#### Get All Categories
```http
GET /api/categories
Authorization: Bearer {token}

Response: 200 OK
[
  {
    "id": "507f1f77bcf86cd799439012",
    "name": "Groceries",
    "type": "EXPENSE",
    "color": "#4CAF50",
    "icon": "shopping-cart"
  }
]
```

#### Create Category
```http
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Entertainment",
  "type": "EXPENSE",
  "color": "#FF5722",
  "icon": "film"
}

Response: 201 CREATED
```

### Dashboard Endpoints

#### Get Dashboard Statistics
```http
GET /api/dashboard/stats
Authorization: Bearer {token}

Response: 200 OK
{
  "totalIncome": 5000.00,
  "totalExpenses": 3000.00,
  "netSavings": 2000.00,
  "accountsCount": 3,
  "transactionsCount": 45,
  "budgetsCount": 5,
  "goalsCount": 2
}
```

### File Upload Endpoint

#### Upload Transaction Receipt
```http
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [binary data]
transactionId: "507f1f77bcf86cd799439015"

Response: 200 OK
{
  "fileUrl": "/uploads/user@example.com/receipt_123.pdf",
  "fileName": "receipt_123.pdf"
}
```

## Database Schema

### Collections

#### Users
```javascript
{
  _id: ObjectId,
  email: String (unique, indexed),
  password: String (hashed),
  name: String,
  createdAt: Date,
  updatedAt: Date
}
```

#### Accounts
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  name: String,
  type: String, // SAVINGS, CREDIT_CARD, CASH, INVESTMENT
  balance: Double,
  currency: String,
  institution: String,
  accountNumber: String,
  notes: String,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

#### Transactions
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  accountId: String (indexed),
  categoryId: String (indexed),
  type: String, // INCOME, EXPENSE
  amount: Double,
  description: String,
  date: Date (indexed),
  payee: String,
  notes: String,
  tags: [String],
  attachments: [String],
  recurringTransactionId: String,
  createdAt: Date,
  updatedAt: Date
}
```

#### Categories
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  name: String,
  type: String, // INCOME, EXPENSE
  color: String,
  icon: String,
  parentCategoryId: String,
  isDefault: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

#### Budgets
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  categoryId: String (indexed),
  amount: Double,
  spent: Double,
  period: String, // WEEKLY, MONTHLY, YEARLY
  startDate: Date,
  endDate: Date,
  rollover: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

#### Goals
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  name: String,
  description: String,
  targetAmount: Double,
  currentAmount: Double,
  deadline: Date,
  status: String, // IN_PROGRESS, COMPLETED
  contributions: [
    {
      amount: Double,
      date: Date,
      note: String
    }
  ],
  createdAt: Date,
  updatedAt: Date
}
```

#### Recurring Transactions
```javascript
{
  _id: ObjectId,
  userEmail: String (indexed),
  accountId: String,
  categoryId: String,
  type: String, // INCOME, EXPENSE
  amount: Double,
  description: String,
  startDate: Date,
  endDate: Date,
  nextOccurrence: Date (indexed),
  lastProcessed: Date,
  isActive: Boolean,
  createdAt: Date,
  updatedAt: Date
}
```

### Database Relationships Diagram

```
+-------------+
|    Users    |
|             |
+------+------+
       |
       +------------------+------------------+----------------+----------------+
       |                  |                  |                |                |
       v                  v                  v                v                v
+-------------+    +-------------+    +-------------+  +-------------+   +----------------+
|  Accounts   |    | Categories  |    |   Budgets   |  |    Goals    |   |  Goal          |
|             |    |             |    |             |  |             |   |  Contributions | 
+------+------+    +------+------+    +------+------+  +-------------+   +------+---------+
       |                  |                  |                ^                |
       |                  |                  |                |                |  
       +----------+-------+----------+-------+                +----------------+       
                  |                  |                                         
                  v                  v     
           +------------------------------+
           |       Transactions           |
           |                              |
           +------------------------------+
```

## Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Password Encryption**: BCrypt hashing for secure password storage
- **SecurityUtils**: Centralized user context management preventing userId manipulation
- **CORS Configuration**: Controlled cross-origin resource sharing
- **Input Validation**: Request validation using Spring annotations
- **SQL Injection Prevention**: NoSQL injection protection via Spring Data MongoDB
- **File Upload Security**: Validated file types and size limits

## Project Structure

```
phiNance-app/
+-- phiNance/phinance/          # Backend (Spring Boot)
|   +-- src/main/java/
|   |   +-- com/kerem/phinance/
|   |       +-- config/         # Configuration classes
|   |       +-- controller/     # REST API controllers
|   |       +-- model/          # MongoDB entities
|   |       +-- repository/     # Data access layer
|   |       +-- security/       # Security & JWT
|   |       +-- service/        # Business logic
|   |       +-- util/           # Utility classes
|   +-- src/main/resources/
|       +-- application.properties
|
+-- phiNance/frontend/          # Frontend (React + Vite)
|   +-- src/
|   |   +-- components/         # Reusable UI components
|   |   +-- pages/              # Page components
|   |   +-- services/           # API client
|   |   +-- stores/             # Zustand state management
|   |   +-- utils/              # Utility functions
|   +-- public/
|   +-- package.json
|
+-- README.md
```

## Troubleshooting

### MongoDB Connection Issues
- Ensure MongoDB is running: Check if the process is active
- Verify replica set initialization: Run `mongosh --eval "rs.status()"`
- Check port availability: MongoDB should be on port 27017

### Backend Won't Start
- Check Java version: `java -version` (should be 18+)
- Clean Maven cache: `./mvnw clean install`
- Verify MongoDB connection in logs

### Frontend Won't Start
- Check Node version: `node --version` (should be 18+)
- Clear node_modules: `rm -rf node_modules && npm install`
- Check port 3000 is available

### Transaction Errors
- Verify MongoDB is running in replica set mode
- Check replica set status: Must show "PRIMARY" state
- Ensure replica set was initialized: `rs.initiate()`

## License

This project is created for educational purposes.

## Author

Kerem - Full Stack Developer Intern

## Acknowledgments

- Spring Boot Documentation
- React Documentation
- MongoDB Documentation
- TailwindCSS
- VS Code
