import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { useAccountsStore } from '../stores/accountsStore';
import { useAuthStore } from '../stores/authStore';
import { useCurrencyFormatter } from '../utils/currency';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  PlusIcon,
  CreditCardIcon,
  PencilIcon,
  TrashIcon,
  BanknotesIcon,
  BuildingLibraryIcon,
  CurrencyDollarIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';
import { FaPiggyBank } from 'react-icons/fa';

const accountTypes = [
  { value: 'BANK_ACCOUNT', label: 'Bank Account', icon: BuildingLibraryIcon },
  { value: 'CREDIT_CARD', label: 'Credit Card', icon: CreditCardIcon },
  { value: 'CASH', label: 'Cash', icon: BanknotesIcon },
  { value: 'INVESTMENT_ACCOUNT', label: 'Investment', icon: ChartBarIcon },
  { value: 'SAVINGS', label: 'Savings', icon: FaPiggyBank },
];

const colors = [
  '#f59e0b',
  '#C57F08',
  '#c5b23a',
  '#8ed334',
  '#6ee772',
  '#60f0fa',
  '#3B82F6',
  '#A78BFA',
  '#F472B6',
  '#EF4444',
];

export default function Accounts() {
  const { accounts, loading, fetchAccounts, createAccount, updateAccount, archiveAccount } =
    useAccountsStore();
  const { user } = useAuthStore();
  const formatCurrency = useCurrencyFormatter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState(null);
  const [deletingAccountId, setDeletingAccountId] = useState(null);
  
  const { register, handleSubmit, reset, setValue, watch, formState: { errors } } = useForm();
  const selectedColor = watch('color', colors[0]);

  useEffect(() => {
    fetchAccounts();
  }, [fetchAccounts]);

  const openCreateModal = () => {
    setEditingAccount(null);
    reset({
      name: '',
      type: 'BANK_ACCOUNT',
      currency: 'USD',
      description: '',
      color: '#f59e0b',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (account) => {
    setEditingAccount(account);
    reset({
      name: account.name,
      type: account.type,
      initialBalance: account.initialBalance,
      description: account.description || '',
      color: account.color || '#22c55e',
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingAccount(null);
    reset();
  };

  const onSubmit = async (data) => {
    try {
      let payload;
      
      if (editingAccount) {
        // When editing, exclude initialBalance and currency (they can't be changed)
        payload = {
          name: data.name,
          type: data.type,
          description: data.description,
          color: data.color,
        };
        await updateAccount(editingAccount.id, payload);
        toast.success('Account updated successfully');
      } else {
        // When creating, include all fields
        payload = {
          ...data,
          initialBalance: parseFloat(data.initialBalance),
        };
        await createAccount(payload);
        toast.success('Account created successfully');
      }
      closeModal();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async () => {
    try {
      await archiveAccount(deletingAccountId);
      toast.success('Account archived successfully');
      setIsDeleteDialogOpen(false);
      setDeletingAccountId(null);
    } catch (error) {
      toast.error('Failed to archive account');
    }
  };

  const getAccountIcon = (type) => {
    const accountType = accountTypes.find((t) => t.value === type);
    return accountType?.icon || CreditCardIcon;
  };

  if (loading && accounts.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Accounts</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <PlusIcon className="h-5 w-5" />
          Add Account
        </button>
      </div>

      {accounts.length === 0 ? (
        <EmptyState
          icon={CreditCardIcon}
          title="No accounts yet"
          description="Create your first financial account to start tracking your money."
          action={
            <button onClick={openCreateModal} className="btn-primary">
              Add Account
            </button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {accounts.map((account) => {
            const Icon = getAccountIcon(account.type);
            return (
              <div
                key={account.id}
                className="card hover:shadow-md transition-shadow"
                style={{ borderLeftColor: account.color, borderLeftWidth: '4px' }}
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div
                      className="w-12 h-12 rounded-lg flex items-center justify-center"
                      style={{ backgroundColor: `${account.color}20` }}
                    >
                      <Icon className="h-6 w-6" style={{ color: account.color }} />
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-900">{account.name}</h3>
                      <p className="text-sm text-gray-500">
                        {accountTypes.find((t) => t.value === account.type)?.label}
                      </p>
                    </div>
                  </div>
                  {account.type !== 'SAVINGS' && (
                    <div className="flex gap-1">
                      <button
                        onClick={() => openEditModal(account)}
                        className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
                      >
                        <PencilIcon className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => {
                          setDeletingAccountId(account.id);
                          setIsDeleteDialogOpen(true);
                        }}
                        className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        <TrashIcon className="h-4 w-4" />
                      </button>
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Balance</span>
                    <span className="font-semibold text-gray-900">
                      {formatCurrency(account.currentBalance)}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">Description</span>
                    <span className="text-gray-600">
                      {account.description || '-'}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingAccount ? 'Edit Account' : 'Create Account'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Account Name
            </label>
            <input
              {...register('name', { required: 'Account name is required' })}
              className="input-field"
              placeholder="e.g., Main Checking"
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Account Type
            </label>
            <select {...register('type')} className="input-field">
              {accountTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Initial Balance
            </label>
            <input
              type="number"
              step="0.01"
              {...register('initialBalance', { required: 'Initial balance is required' })}
              className="input-field"
              placeholder="0.00"
              disabled={!!editingAccount}
            />
            {errors.initialBalance && (
              <p className="mt-1 text-sm text-red-600">{errors.initialBalance.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description (Optional)
            </label>
            <input
              {...register('description')}
              className="input-field"
              placeholder="Account description"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Color
            </label>
            <div className="flex gap-2 flex-wrap">
              {colors.map((color) => (
                <button
                  key={color}
                  type="button"
                  onClick={() => setValue('color', color)}
                  className={`w-8 h-8 rounded-full border-2 transition-all ${
                    selectedColor === color
                      ? 'border-gray-900 scale-110'
                      : 'border-transparent hover:border-gray-300'
                  }`}
                  style={{ backgroundColor: color }}
                />
              ))}
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={closeModal} className="btn-secondary flex-1">
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              {editingAccount ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Archive Account"
        message="Are you sure you want to archive this account? The transaction history will be preserved."
      />
    </div>
  );
}
