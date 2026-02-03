# Personal Finance Tracker Application - Homework Assignment

## Overview
You are expected to develop a comprehensive personal finance tracking application with Java for back-end and JavaScript, HTML, CSS for front-end. This application will help users manage their income, expenses, budgets, and financial goals.

## Architecture Requirements

- **Back-end and front-end should be separated applications** and connected to each other with REST APIs.
- You may use any well-known open source framework or library both in your front-end project and backend project.
- **Using Spring Boot and React or Vue.js is a plus.**
- Database integration is required (MongoDB).

---

## Core Features

### 1. User Management
- **User login** with JWT token-based authentication
- **User profile management** (update personal information, change password)
- Support for multiple currency preferences (USD, EUR, TRY, etc.)

### 2. Account Management
- **Create financial accounts** (Bank Account, Credit Card, Cash, Investment Account)
- Each account should have:
  - Account name
  - Account type
  - Initial balance
  - Current balance
  - Currency
  - Creation date
- **List all accounts** with current balances
- **Update account** information
- **Archive/Delete account** (soft delete with transaction history preservation)

### 3. Transaction Management
- **Add transaction** to an account with following properties:
  - Transaction type (Income, Expense, Transfer)
  - Amount
  - Category
  - Description
  - Date
  - Recurring flag (one-time or recurring)
  - Attachments/receipts (optional file upload)
- **Transfer between accounts** (automatically creates paired transactions)
- **Recurring transactions** (daily, weekly, monthly, yearly)
- **List transactions** with pagination
- **Edit transaction** details
- **Delete transaction** (with balance recalculation)

### 4. Category Management
- **Create custom categories** for income and expenses
- Each category should have:
  - Name
  - Type (Income/Expense)
  - Icon/Color
  - Parent category (for subcategories)
- **Predefined categories** (Salary, Food, Transportation, Entertainment, Healthcare, etc.)
- **Budget allocation** per category

### 5. Budget Planning
- **Create monthly budgets** for specific categories
- Each budget should track:
  - Category
  - Allocated amount
  - Spent amount
  - Remaining amount
  - Period (month/year)
- **Budget alerts** when spending exceeds 80% and 100%
- **Compare budgets** across different months

### 6. Financial Goals
- **Create savings goals** with:
  - Goal name
  - Target amount
  - Current amount
  - Deadline
  - Priority level (Low, Medium, High)
  - Associated account
- **Track goal progress** with visual indicators
- **Add contributions** to goals
- **Goal dependencies**: Some goals can be dependent on others (e.g., "Buy a car" depends on "Save emergency fund")
- Goals with dependencies cannot be marked as completed until dependent goals are completed

### 7. Reports and Analytics
- **Dashboard** with:
  - Total net worth
  - Monthly income vs expenses chart
  - Category-wise expense breakdown (pie chart)
  - Account balances overview
  - Recent transactions
- **Filter transactions** by:
  - Date range
  - Account
  - Category
  - Transaction type
  - Amount range
- **Sort transactions** by:
  - Date
  - Amount
  - Category
  - Account
- **Export reports** to CSV

### 8. Search and Filter
- **Advanced search** across all transactions
- **Filter by multiple criteria** simultaneously
- **Save favorite filters** for quick access

---

## Technical Requirements

### Back-End
- RESTful API design
- Input validation and error handling
- Secure password storage (bcrypt or similar)
- JWT-based authentication
- Database migrations/schema management
- Logging mechanism
- Transaction management (ACID properties for financial operations)

### Front-End
- Responsive design (mobile-friendly)
- Form validation
- Loading states and error messages
- Data visualization (charts and graphs)
- File upload functionality
- Date pickers and form components
- Confirmation dialogs for critical actions

### Testing
- **Implement at least 5 unit tests** covering:
  - User authentication
  - Transaction creation and balance calculation
  - Budget calculation logic
  - Goal dependency validation
  - Transfer between accounts

### Documentation
- **README.md** with:
  - Project description
  - Technology stack
  - Installation instructions
  - How to run the application
  - API documentation
  - Database schema diagram
  - Screenshots (optional but recommended)

---

## General Expectations

1. **Easy to run**: Your project must be easy to run without any external dependency configuration. Provide clear instructions.
2. **Package managers**: Use Maven for back-end, npm for front-end.
3. **UI Libraries**: You may use HTML, CSS, and JS libraries such as Bootstrap, Material-UI, Ant Design, Tailwind CSS.
4. **Code quality**: Follow clean code principles, use meaningful variable names, and add comments where necessary.
5. **Git practices**: Use meaningful commit messages and maintain a clean git history.
6. **Security**: Implement proper authentication, authorization, and input sanitization.
7. **Error handling**: Graceful error handling both on client and server side.

---
