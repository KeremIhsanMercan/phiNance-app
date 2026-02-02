import { useEffect, useState } from 'react';
import { dashboardApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';
import { useCurrencyFormatter } from '../utils/currency';
import LoadingSpinner from '../components/LoadingSpinner';
import {
  BanknotesIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  CreditCardIcon,
  BuildingLibraryIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';
import { FaPiggyBank } from 'react-icons/fa';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line, Doughnut } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuthStore();
  const formatCurrency = useCurrencyFormatter();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await dashboardApi.getData();
        setData(response.data);
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const lineChartData = {
    labels: data?.monthlyData?.map((m) => m.month) || [],
    datasets: [
      {
        label: 'Income',
        data: data?.monthlyData?.map((m) => m.income) || [],
        borderColor: 'rgb(16, 185, 129)', // emerald-600
        backgroundColor: 'rgba(16, 185, 129, 0.1)',
        tension: 0.3,
      },
      {
        label: 'Expenses',
        data: data?.monthlyData?.map((m) => m.expenses) || [],
        borderColor: 'rgb(220, 38, 38)', // red-600
        backgroundColor: 'rgba(220, 38, 38, 0.1)',
        tension: 0.3,
      },
    ],
  };

  const doughnutChartData = {
    labels: data?.categoryExpenses?.map((c) => c.categoryName) || [],
    datasets: [
      {
        data: data?.categoryExpenses?.map((c) => c.amount) || [],
        backgroundColor: data?.categoryExpenses?.map((c) => c.color) || [],
        borderWidth: 0,
      },
    ],
  };

  const accountTypes = {
    BANK_ACCOUNT: 'Bank Account',
    CREDIT_CARD: 'Credit Card',
    CASH: 'Cash',
    INVESTMENT_ACCOUNT: 'Investment',
    SAVINGS: 'Savings',
  };

  const getAccountIcon = (type) => {
    const iconMap = {
      BANK_ACCOUNT: BuildingLibraryIcon,
      CREDIT_CARD: CreditCardIcon,
      CASH: BanknotesIcon,
      INVESTMENT_ACCOUNT: ChartBarIcon,
      SAVINGS: FaPiggyBank,
    };
    return iconMap[type] || CreditCardIcon;
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
              <BanknotesIcon className="h-6 w-6 text-primary-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Net Worth</p>
              <p className="text-xl font-bold text-gray-900">
                {formatCurrency(data?.totalNetWorth)}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center">
              <ArrowTrendingUpIcon className="h-6 w-6 text-emerald-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Monthly Income</p>
              <p className="text-xl font-bold text-emerald-600">
                {formatCurrency(data?.totalIncome)}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
              <ArrowTrendingDownIcon className="h-6 w-6 text-red-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Monthly Expenses</p>
              <p className="text-xl font-bold text-red-600">
                {formatCurrency(data?.totalExpenses)}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <CreditCardIcon className="h-6 w-6 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Accounts</p>
              <p className="text-xl font-bold text-gray-900">
                {data?.accountSummaries?.length || 0}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Income vs Expenses
          </h3>
          <div className="h-64">
            <Line
              data={lineChartData}
              options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: {
                    position: 'bottom',
                  },
                },
              }}
            />
          </div>
        </div>

        <div className="card">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Expenses by Category
          </h3>
          <div className="h-64 flex items-center justify-center">
            {data?.categoryExpenses?.length > 0 ? (
              <Doughnut
                data={doughnutChartData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      position: 'right',
                    },
                  },
                }}
              />
            ) : (
              <p className="text-gray-500">No expense data available</p>
            )}
          </div>
        </div>
      </div>

      {/* Accounts Overview */}
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          Account Balances
        </h3>
        {data?.accountSummaries?.length > 0 ? (
          <div className="space-y-3">
            {data.accountSummaries.map((account) => {
              const Icon = getAccountIcon(account.type);
              return (
              <div
                key={account.id}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
              >
                <div className="flex items-center gap-3">
                  <div 
                    className="w-10 h-10 rounded-lg flex items-center justify-center"
                    style={{ backgroundColor: `${account.color}20` }}
                  >
                    <Icon className="h-5 w-5" style={{ color: account.color }} />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{account.name}</p>
                    <p className="text-sm text-gray-500">{accountTypes[account.type]}</p>
                  </div>
                </div>
                <p className="font-semibold text-gray-900">
                  {formatCurrency(account.balance)}
                </p>
              </div>
            );
            })}
          </div>
        ) : (
          <p className="text-gray-500 text-center py-4">
            No accounts yet. Create your first account to get started.
          </p>
        )}
      </div>
    </div>
  );
}
