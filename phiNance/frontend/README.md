# PhiNance Frontend

A modern React-based frontend for the Personal Finance Tracker application.

## Tech Stack

- **React 18** - UI Library
- **Vite** - Build tool and dev server
- **TailwindCSS** - Utility-first CSS framework
- **React Router DOM 6** - Client-side routing
- **Zustand** - State management
- **React Hook Form** - Form handling
- **Axios** - HTTP client
- **Chart.js + react-chartjs-2** - Data visualization
- **Heroicons** - Icon library
- **date-fns** - Date formatting
- **react-hot-toast** - Toast notifications

## Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend server running on port 8080

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server on port 3000 |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |

## Project Structure

```
frontend/
??? public/                 # Static assets
??? src/
?   ??? components/        # Reusable UI components
?   ?   ??? ConfirmDialog.jsx
?   ?   ??? EmptyState.jsx
?   ?   ??? Layout.jsx
?   ?   ??? LoadingSpinner.jsx
?   ?   ??? Modal.jsx
?   ??? pages/             # Page components
?   ?   ??? Accounts.jsx
?   ?   ??? Budgets.jsx
?   ?   ??? Categories.jsx
?   ?   ??? Dashboard.jsx
?   ?   ??? Goals.jsx
?   ?   ??? Login.jsx
?   ?   ??? Register.jsx
?   ?   ??? Settings.jsx
?   ?   ??? Transactions.jsx
?   ??? services/          # API services
?   ?   ??? api.js
?   ??? stores/            # Zustand stores
?   ?   ??? accountsStore.js
?   ?   ??? authStore.js
?   ?   ??? transactionsStore.js
?   ??? App.jsx            # Main app with routing
?   ??? index.css          # Global styles + Tailwind
?   ??? main.jsx           # Entry point
??? index.html
??? package.json
??? postcss.config.js
??? tailwind.config.js
??? vite.config.js
```

## Features

### Authentication
- User registration with email validation
- Login with JWT authentication
- Automatic token refresh
- Protected routes

### Dashboard
- Net worth overview
- Monthly income vs expenses chart
- Expense breakdown by category
- Account balances summary

### Accounts Management
- Create bank accounts, credit cards, cash, and investment accounts
- Track current balances
- Color-coded account cards
- Archive accounts

### Transaction Tracking
- Record income, expenses, and transfers
- Categorize transactions
- Filter and paginate transactions
- Recurring transaction support

### Categories
- Create income and expense categories
- Custom colors for categories
- Organize with parent categories

### Budget Management
- Set monthly budgets per category
- Visual progress bars
- Alert thresholds
- Over-budget warnings

### Savings Goals
- Create financial goals
- Track progress with contributions
- Priority levels (Low, Medium, High)
- Target dates

### Settings
- Profile management
- Password change
- Notification preferences

## API Integration

The frontend communicates with the backend through RESTful APIs. The base URL is configured to proxy `/api` requests to `http://localhost:8080`.

### Authentication Flow

1. User logs in with email/password
2. Backend returns access token and refresh token
3. Access token stored in localStorage via Zustand persist
4. Axios interceptor attaches token to all requests
5. On 401 response, automatic token refresh is attempted

## Styling

This project uses TailwindCSS with a custom green color palette as the primary theme. Custom utility classes are defined in `index.css`:

- `.btn-primary` - Primary action buttons
- `.btn-secondary` - Secondary action buttons
- `.input-field` - Form input styling
- `.card` - Card container styling
- `.sidebar-link` - Sidebar navigation links

## Environment Variables

Create a `.env` file for custom configuration:

```env
VITE_API_URL=http://localhost:8080/api
```

## Building for Production

```bash
npm run build
```

The build output will be in the `dist` directory, ready for deployment.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
