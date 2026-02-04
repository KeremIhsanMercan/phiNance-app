import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { format } from 'date-fns';
import { budgetsApi, categoriesApi } from '../services/api';
import { useCurrencyFormatter } from '../utils/currency';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  PlusIcon,
  CalculatorIcon,
  PencilIcon,
  TrashIcon,
  ExclamationTriangleIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
} from '@heroicons/react/24/outline';

export default function Budgets() {
  const [budgets, setBudgets] = useState([]);
  const [pastBudgets, setPastBudgets] = useState([]);
  const [futureBudgets, setFutureBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingBudget, setEditingBudget] = useState(null);
  const [deletingBudgetId, setDeletingBudgetId] = useState(null);
  
  // Current month pagination
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(9);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  
  // Past budgets pagination
  const [pastPage, setPastPage] = useState(0);
  const [pastSize] = useState(9);
  const [pastTotalPages, setPastTotalPages] = useState(0);
  const [pastTotalElements, setPastTotalElements] = useState(0);
  const [pastCurrentPage, setPastCurrentPage] = useState(0);
  
  // Future budgets pagination
  const [futurePage, setFuturePage] = useState(0);
  const [futureSize] = useState(9);
  const [futureTotalPages, setFutureTotalPages] = useState(0);
  const [futureTotalElements, setFutureTotalElements] = useState(0);
  const [futureCurrentPage, setFutureCurrentPage] = useState(0);

  const { register, handleSubmit, reset, formState: { errors } } = useForm();

  useEffect(() => {
    fetchData();
  }, [page, size, pastPage, futurePage]);

  const fetchData = async () => {
    try {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1; // JavaScript months are 0-indexed
      
      const [currentBudgetsRes, allBudgetsRes, categoriesRes] = await Promise.all([
        budgetsApi.getAll({ year: currentYear, month: currentMonth, page, size, sortBy: 'allocatedAmount', sortDirection: 'desc' }),
        budgetsApi.getAll({ page: 0, size: 1000, sortBy: 'year,month', sortDirection: 'desc' }), // Get all budgets for filtering
        categoriesApi.getAll({ page: 0, size: 100 }),
      ]);
      
      // Current month budgets
      setBudgets(currentBudgetsRes.data.content);
      setTotalPages(currentBudgetsRes.data.totalPages);
      setTotalElements(currentBudgetsRes.data.totalElements);
      setCurrentPage(currentBudgetsRes.data.number);
      
      // Filter all budgets into past and future
      const allBudgets = allBudgetsRes.data.content || [];
      
      // Filter past budgets (before current month)
      const pastBudgetsList = allBudgets.filter(budget => {
        return budget.year < currentYear || (budget.year === currentYear && budget.month < currentMonth);
      });
      
      // Filter future budgets (after current month)
      const futureBudgetsList = allBudgets.filter(budget => {
        return budget.year > currentYear || (budget.year === currentYear && budget.month > currentMonth);
      });
      
      // Sort future budgets ascending (nearest first)
      futureBudgetsList.sort((a, b) => {
        if (a.year !== b.year) return a.year - b.year;
        return a.month - b.month;
      });
      
      // Apply pagination to past budgets
      const pastStart = pastPage * pastSize;
      const pastEnd = pastStart + pastSize;
      setPastBudgets(pastBudgetsList.slice(pastStart, pastEnd));
      setPastTotalPages(Math.ceil(pastBudgetsList.length / pastSize));
      setPastTotalElements(pastBudgetsList.length);
      setPastCurrentPage(pastPage);
      
      // Apply pagination to future budgets
      const futureStart = futurePage * futureSize;
      const futureEnd = futureStart + futureSize;
      setFutureBudgets(futureBudgetsList.slice(futureStart, futureEnd));
      setFutureTotalPages(Math.ceil(futureBudgetsList.length / futureSize));
      setFutureTotalElements(futureBudgetsList.length);
      setFutureCurrentPage(futurePage);
      
      setCategories((categoriesRes.data.content || []).filter((c) => c.type === 'EXPENSE'));
    } catch (error) {
      toast.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingBudget(null);
    const currentMonth = format(new Date(), 'yyyy-MM');
    reset({
      categoryId: categories[0]?.id || '',
      monthYear: currentMonth,
      budgetAmount: '',
      alertThreshold: 80,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (budget) => {
    setEditingBudget(budget);
    // Convert year and month to monthYear format for the input
    const monthYear = `${budget.year}-${String(budget.month).padStart(2, '0')}`;
    reset({
      categoryId: budget.categoryId,
      monthYear: monthYear,
      budgetAmount: budget.allocatedAmount,
      alertThreshold: budget.alertThreshold || 80,
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingBudget(null);
    reset();
  };

  const onSubmit = async (data) => {
    try {
      // Parse monthYear (e.g., "2026-01") into separate year and month
      const [year, month] = data.monthYear.split('-').map(Number);
      
      const payload = {
        categoryId: data.categoryId,
        allocatedAmount: parseFloat(data.budgetAmount),
        year: year,
        month: month,
        alertThreshold: data.alertThreshold ? parseInt(data.alertThreshold) : 80,
      };

      if (editingBudget) {
        await budgetsApi.update(editingBudget.id, payload);
        toast.success('Budget updated successfully');
      } else {
        await budgetsApi.create(payload);
        toast.success('Budget created successfully');
      }
      closeModal();
      fetchData();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async () => {
    try {
      await budgetsApi.delete(deletingBudgetId);
      toast.success('Budget deleted successfully');
      setIsDeleteDialogOpen(false);
      setDeletingBudgetId(null);
      fetchData();
    } catch (error) {
      toast.error('Failed to delete budget');
    }
  };

  const formatCurrency = useCurrencyFormatter();

  const getCategoryName = (categoryId) => {
    return categories.find((c) => c.id === categoryId)?.name || 'Unknown';
  };

  const getCategoryColor = (categoryId) => {
    return categories.find((c) => c.id === categoryId)?.color || '#6366f1';
  };

  const getProgressColor = (percentage) => {
    if (percentage >= 100) return 'bg-red-500';
    if (percentage >= 80) return 'bg-yellow-500';
    return 'bg-primary-500';
  };

  const renderBudgetCard = (budget) => {
    const spentPercentage = Math.min(
      (budget.spentAmount / (budget.allocatedAmount || 1)) * 100,
      100
    );
    const isOverBudget = budget.spentAmount > budget.allocatedAmount;
    const isNearLimit = spentPercentage >= budget.alertThreshold;
    const categoryColor = getCategoryColor(budget.categoryId);

    return (
      <div 
        key={budget.id} 
        className="card"
        style={{
          border: isOverBudget 
            ? '2px solid #ef4444' 
            : isNearLimit 
            ? '2px solid #eab308' 
            : undefined
        }}
      >
        <div className="flex items-start justify-between mb-4">
          <div>
            <h3 className="font-semibold text-gray-900">
              {getCategoryName(budget.categoryId)}
            </h3>
            <p className="text-sm text-gray-500">
              {new Date(budget.year, budget.month - 1).toLocaleDateString('en-US', { year: 'numeric', month: 'long' })}
            </p>
          </div>
          <div className="flex gap-1">
            {(isOverBudget || isNearLimit) && (
              <div className="p-2" style={{ color: isOverBudget ? '#ef4444' : '#eab308' }}>
                <ExclamationTriangleIcon className="h-5 w-5" />
              </div>
            )}
            <button
              onClick={() => openEditModal(budget)}
              className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
            >
              <PencilIcon className="h-4 w-4" />
            </button>
            <button
              onClick={() => {
                setDeletingBudgetId(budget.id);
                setIsDeleteDialogOpen(true);
              }}
              className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
            >
              <TrashIcon className="h-4 w-4" />
            </button>
          </div>
        </div>

        <div className="space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Spent</span>
            <span style={{ color: categoryColor }}>
              {formatCurrency(budget.spentAmount)}
            </span>
          </div>

          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className={`h-2 rounded-full transition-all`}
              style={{ 
                width: `${Math.min(spentPercentage, 100)}%`,
                backgroundColor: categoryColor
              }}
            />
          </div>

          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Budget</span>
            <span className="text-gray-900 font-semibold">
              {formatCurrency(budget.allocatedAmount)}
            </span>
          </div>

          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Remaining</span>
            <span className={isOverBudget ? 'font-semibold' : ''}
              style={{
                color: isOverBudget ? '#ef4444' : categoryColor
              }}
            >
              {formatCurrency(budget.allocatedAmount - budget.spentAmount)}
            </span>
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Budgets</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2 text-sm sm:text-base">
          <PlusIcon className="h-5 w-5" />
          Add Budget
        </button>
      </div>

      {/* Current Month Budgets */}
      <div className="mb-8">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Current Month</h2>
        {budgets.length === 0 ? (
          <EmptyState
            icon={CalculatorIcon}
            title="No budgets for this month"
            description="Set monthly budgets for your expense categories to stay on track."
            action={
              <button onClick={openCreateModal} className="btn-primary">
                Add Budget
              </button>
            }
          />
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {budgets.map(renderBudgetCard)}
            </div>

            {/* Current Month Pagination */}
            {totalPages > 1 && (
              <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
                <div className="text-sm text-gray-700 text-center sm:text-left">
                  Showing {currentPage * size + 1} to{' '}
                  {Math.min((currentPage + 1) * size, totalElements)} of {totalElements} budgets
                </div>
                <div className="flex items-center gap-2 w-full sm:w-auto justify-center">
                  <button
                    onClick={() => setPage(page - 1)}
                    disabled={page === 0}
                    className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                  >
                    <ChevronLeftIcon className="h-5 w-5" />
                  </button>
                  <div className="flex gap-1 overflow-x-auto max-w-[200px] sm:max-w-md hide-scrollbar">
                    {[...Array(totalPages)].map((_, index) => (
                      <button
                        key={index}
                        onClick={() => setPage(index)}
                        className={`px-3 py-2 text-sm font-medium rounded-lg border flex-shrink-0 ${
                          currentPage === index
                            ? 'bg-primary-600 text-white border-primary-600'
                            : 'border-gray-300 hover:bg-gray-50'
                        }`}
                      >
                        {index + 1}
                      </button>
                    ))}
                  </div>
                  <button
                    onClick={() => setPage(page + 1)}
                    disabled={page >= totalPages - 1}
                    className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                  >
                    <ChevronRightIcon className="h-5 w-5" />
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Future Budgets */}
      {futureBudgets.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Future Budgets</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {futureBudgets.map(renderBudgetCard)}
          </div>

          {/* Future Budgets Pagination */}
          {futureTotalPages > 1 && (
            <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
              <div className="text-sm text-gray-700 text-center sm:text-left">
                Showing {futureCurrentPage * futureSize + 1} to{' '}
                {Math.min((futureCurrentPage + 1) * futureSize, futureTotalElements)} of {futureTotalElements} budgets
              </div>
              <div className="flex items-center gap-2 w-full sm:w-auto justify-center">
                <button
                  onClick={() => setFuturePage(futurePage - 1)}
                  disabled={futurePage === 0}
                  className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                >
                  <ChevronLeftIcon className="h-5 w-5" />
                </button>
                <div className="flex gap-1 overflow-x-auto max-w-[200px] sm:max-w-md hide-scrollbar">
                  {[...Array(futureTotalPages)].map((_, index) => (
                    <button
                      key={index}
                      onClick={() => setFuturePage(index)}
                      className={`px-3 py-2 text-sm font-medium rounded-lg border flex-shrink-0 ${
                        futureCurrentPage === index
                          ? 'bg-primary-600 text-white border-primary-600'
                          : 'border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {index + 1}
                    </button>
                  ))}
                </div>
                <button
                  onClick={() => setFuturePage(futurePage + 1)}
                  disabled={futurePage >= futureTotalPages - 1}
                  className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                >
                  <ChevronRightIcon className="h-5 w-5" />
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Past Budgets */}
      {pastBudgets.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Past Budgets</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {pastBudgets.map(renderBudgetCard)}
          </div>

          {/* Past Budgets Pagination */}
          {pastTotalPages > 1 && (
            <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
              <div className="text-sm text-gray-700 text-center sm:text-left">
                Showing {pastCurrentPage * pastSize + 1} to{' '}
                {Math.min((pastCurrentPage + 1) * pastSize, pastTotalElements)} of {pastTotalElements} budgets
              </div>
              <div className="flex items-center gap-2 w-full sm:w-auto justify-center">
                <button
                  onClick={() => setPastPage(pastPage - 1)}
                  disabled={pastPage === 0}
                  className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                >
                  <ChevronLeftIcon className="h-5 w-5" />
                </button>
                <div className="flex gap-1 overflow-x-auto max-w-[200px] sm:max-w-md hide-scrollbar">
                  {[...Array(pastTotalPages)].map((_, index) => (
                    <button
                      key={index}
                      onClick={() => setPastPage(index)}
                      className={`px-3 py-2 text-sm font-medium rounded-lg border flex-shrink-0 ${
                        pastCurrentPage === index
                          ? 'bg-primary-600 text-white border-primary-600'
                          : 'border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      {index + 1}
                    </button>
                  ))}
                </div>
                <button
                  onClick={() => setPastPage(pastPage + 1)}
                  disabled={pastPage >= pastTotalPages - 1}
                  className="px-3 py-2 text-sm font-medium rounded-lg border border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 flex-shrink-0"
                >
                  <ChevronRightIcon className="h-5 w-5" />
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingBudget ? 'Edit Budget' : 'Create Budget'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category
            </label>
            <select
              {...register('categoryId', { required: 'Category is required' })}
              className="input-field"
            >
              <option value="">Select category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            {errors.categoryId && (
              <p className="mt-1 text-sm text-red-600">{errors.categoryId.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Month
            </label>
            <input
              type="month"
              {...register('monthYear', { required: 'Month is required' })}
              className="input-field"
              placeholder="YYYY-MM"
            />
            {errors.monthYear && (
              <p className="mt-1 text-sm text-red-600">{errors.monthYear.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Budget Amount
            </label>
            <input
              type="number"
              step="0.01"
              {...register('budgetAmount', {
                required: 'Budget amount is required',
                min: { value: 0.01, message: 'Amount must be positive' },
              })}
              className="input-field"
              placeholder="0.00"
            />
            {errors.budgetAmount && (
              <p className="mt-1 text-sm text-red-600">{errors.budgetAmount.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Alert Threshold (%)
            </label>
            <input
              type="number"
              {...register('alertThreshold', {
                min: { value: 1, message: 'Must be at least 1%' },
                max: { value: 100, message: 'Cannot exceed 100%' },
              })}
              className="input-field"
              placeholder="80"
            />
            <p className="mt-1 text-xs text-gray-500">
              You'll be warned when spending reaches this percentage of the budget.
            </p>
            {errors.alertThreshold && (
              <p className="mt-1 text-sm text-red-600">{errors.alertThreshold.message}</p>
            )}
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={closeModal} className="btn-secondary flex-1">
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              {editingBudget ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Budget"
        message="Are you sure you want to delete this budget? Your transaction history will not be affected."
      />
    </div>
  );
}
