import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { format } from 'date-fns';
import { useTransactionsStore } from '../stores/transactionsStore';
import { useAccountsStore } from '../stores/accountsStore';
import { categoriesApi } from '../services/api';
import { useCurrencyFormatter } from '../utils/currency';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  PlusIcon,
  ArrowsRightLeftIcon,
  PencilIcon,
  TrashIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  FunnelIcon,
} from '@heroicons/react/24/outline';

const transactionTypes = [
  { value: 'INCOME', label: 'Income', color: 'text-emerald-600', bg: 'bg-emerald-100' },
  { value: 'EXPENSE', label: 'Expense', color: 'text-red-600', bg: 'bg-red-100' },
  { value: 'TRANSFER', label: 'Transfer', color: 'text-primary-600', bg: 'bg-primary-100' },
];

export default function Transactions() {
  const {
    transactions,
    pagination,
    loading,
    fetchTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
  } = useTransactionsStore();
  const { accounts, fetchAccounts } = useAccountsStore();
  const [categories, setCategories] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [deletingTransactionId, setDeletingTransactionId] = useState(null);

  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm();
  const watchType = watch('type', 'EXPENSE');

  useEffect(() => {
    fetchTransactions();
    fetchAccounts();
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const response = await categoriesApi.getAll();
      setCategories(response.data);
    } catch (error) {
      console.error('Failed to fetch categories:', error);
    }
  };

  const openCreateModal = () => {
    setEditingTransaction(null);
    reset({
      type: 'EXPENSE',
      amount: '',
      accountId: accounts[0]?.id || '',
      categoryId: '',
      description: '',
      date: format(new Date(), 'yyyy-MM-dd'),
      recurring: false,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (transaction) => {
    setEditingTransaction(transaction);
    reset({
      type: transaction.type,
      amount: transaction.amount,
      accountId: transaction.accountId,
      categoryId: transaction.categoryId || '',
      description: transaction.description || '',
      date: transaction.date,
      recurring: transaction.recurring,
      recurrencePattern: transaction.recurrencePattern || '',
      transferToAccountId: transaction.transferToAccountId || '',
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingTransaction(null);
    reset();
  };

  const onSubmit = async (data) => {
    try {
      const payload = {
        ...data,
        amount: parseFloat(data.amount),
        categoryId: data.categoryId || null,
        transferToAccountId: data.type === 'TRANSFER' ? data.transferToAccountId : null,
        recurrencePattern: data.recurring ? data.recurrencePattern : null,
      };

      if (editingTransaction) {
        await updateTransaction(editingTransaction.id, payload);
        toast.success('Transaction updated successfully');
      } else {
        await createTransaction(payload);
        toast.success('Transaction created successfully');
      }
      closeModal();
      fetchTransactions();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async () => {
    try {
      await deleteTransaction(deletingTransactionId);
      toast.success('Transaction deleted successfully');
      setIsDeleteDialogOpen(false);
      setDeletingTransactionId(null);
    } catch (error) {
      toast.error('Failed to delete transaction');
    }
  };

  const formatCurrency = useCurrencyFormatter();

  const getTransactionStyle = (type) => {
    return transactionTypes.find((t) => t.value === type) || transactionTypes[1];
  };

  const getAccountName = (accountId) => {
    return accounts.find((a) => a.id === accountId)?.name || 'Unknown';
  };

  const getCategoryName = (categoryId, transaction) => {
    // For transfers, show "to {destination account name}"
    if (transaction?.type === 'TRANSFER' && transaction?.transferToAccountId) {
      const destinationAccount = accounts.find((a) => a.id === transaction.transferToAccountId);
      return destinationAccount ? `to ${destinationAccount.name}` : 'to Unknown';
    }
    return categories.find((c) => c.id === categoryId)?.name || '-';
  };

  if (loading && transactions.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <PlusIcon className="h-5 w-5" />
          Add Transaction
        </button>
      </div>

      {transactions.length === 0 ? (
        <EmptyState
          icon={ArrowsRightLeftIcon}
          title="No transactions yet"
          description="Start tracking your income and expenses by adding your first transaction."
          action={
            <button onClick={openCreateModal} className="btn-primary">
              Add Transaction
            </button>
          }
        />
      ) : (
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Date</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Type</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Account</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Category</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Description</th>
                  <th className="text-right py-3 px-4 text-sm font-medium text-gray-500">Amount</th>
                  <th className="text-right py-3 px-4 text-sm font-medium text-gray-500">Actions</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((transaction) => {
                  const style = getTransactionStyle(transaction.type);
                  return (
                    <tr key={transaction.id} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-3 px-4 text-sm text-gray-600">
                        {format(new Date(transaction.date), 'MMM dd, yyyy')}
                      </td>
                      <td className="py-3 px-4">
                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${style.bg} ${style.color}`}>
                          {transaction.type === 'INCOME' && <ArrowUpIcon className="h-3 w-3" />}
                          {transaction.type === 'EXPENSE' && <ArrowDownIcon className="h-3 w-3" />}
                          {transaction.type === 'TRANSFER' && <ArrowsRightLeftIcon className="h-3 w-3" />}
                          {style.label}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-900">
                        {getAccountName(transaction.accountId)}
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-600">
                        {getCategoryName(transaction.categoryId, transaction)}
                      </td>
                      <td className="py-3 px-4 text-sm text-gray-600 max-w-xs truncate">{transaction.description || '-'}
                      </td>
                      <td className={`py-3 px-4 text-sm font-medium text-right ${style.color}`}>
                        {transaction.type === 'EXPENSE' ? '-' : ''}
                        {formatCurrency(transaction.amount)}
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex justify-end gap-1">
                          <button
                            onClick={() => openEditModal(transaction)}
                            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
                          >
                            <PencilIcon className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => {
                              setDeletingTransactionId(transaction.id);
                              setIsDeleteDialogOpen(true);
                            }}
                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                          >
                            <TrashIcon className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200">
              <p className="text-sm text-gray-500">
                Showing {pagination.page * pagination.size + 1} to{' '}
                {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
                {pagination.totalElements} results
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => fetchTransactions(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="btn-secondary disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => fetchTransactions(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="btn-secondary disabled:opacity-50"
                >
                  Next
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
        title={editingTransaction ? 'Edit Transaction' : 'Add Transaction'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
            <select disabled={editingTransaction !== null} 
            {...register('type')
              // if changed to TRANSFER, clear categoryId, clear recurring
              } onChange={(e) => {
                const value = e.target.value;
                if (value === 'TRANSFER') {
                  reset({
                    ...watch(),
                    type: 'TRANSFER',
                    categoryId: '',
                    recurring: false,
                  });
                } else if (watchType === 'TRANSFER') {
                  reset({
                    ...watch(),
                    type: value,
                  });
                }
              }
            } className="input-field">
              {transactionTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount</label>
              <input
                type="number"
                step="0.01"
                {...register('amount', { required: 'Amount is required', min: 0.01 })}
                className="input-field"
                placeholder="0.00"
              />
              {errors.amount && (
                <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
              <input
                type="date"
                {...register('date', { required: 'Date is required' })}
                className="input-field"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Account</label>
            <select
              {...register('accountId', { required: 'Account is required' })} disabled={editingTransaction !== null}
              className="input-field"
            >
              <option value="">Select account</option>
              {accounts.filter((account) => account.type !== 'SAVINGS').map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name}
                </option>
              ))}
            </select>
          </div>

          {watchType === 'TRANSFER' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Transfer To
              </label>
              <select
                {...register('transferToAccountId', {
                  required: watchType === 'TRANSFER' ? 'Destination account is required' : false,
                })}
                disabled={editingTransaction !== null}
                className="input-field"
              >
                <option value="">Select destination</option>
                {accounts.filter((account) => account.type !== 'SAVINGS').map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          {watchType !== 'TRANSFER' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Category (Optional)
              </label>
              <select {...register('categoryId')} className="input-field">
                <option value="">Select category</option>
                {categories
                  .filter((c) => c.type === watchType)
                  .map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
              </select>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description (Optional)
            </label>
            <input
              {...register('description')}
              className="input-field"
              placeholder="Transaction description"
            />
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="recurring"
              {...register('recurring')}
              className="h-4 w-4 text-primary-600 rounded disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={editingTransaction !== null || watchType === 'TRANSFER'}
            />
            <label htmlFor="recurring" className={`text-sm ${editingTransaction ? 'text-gray-400' : 'text-gray-700'}`}>
              Recurring transaction
            </label>
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={closeModal} className="btn-secondary flex-1">
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              {editingTransaction ? 'Update' : 'Add'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Transaction"
        message="Are you sure you want to delete this transaction? This action cannot be undone and will recalculate your account balance."
      />
    </div>
  );
}
