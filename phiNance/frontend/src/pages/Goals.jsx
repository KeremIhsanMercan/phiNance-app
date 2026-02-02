import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { format } from 'date-fns';
import { goalsApi, accountsApi } from '../services/api';
import { useCurrencyFormatter } from '../utils/currency';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  PlusIcon,
  FlagIcon,
  PencilIcon,
  TrashIcon,
  CheckCircleIcon,
  CurrencyDollarIcon,
} from '@heroicons/react/24/outline';

const priorities = [
  { value: 'LOW', label: 'Low', color: 'bg-gray-100 text-gray-700' },
  { value: 'MEDIUM', label: 'Medium', color: 'bg-yellow-100 text-yellow-700' },
  { value: 'HIGH', label: 'High', color: 'bg-red-100 text-red-700' },
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

export default function Goals() {
  const [goals, setGoals] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isContributionModalOpen, setIsContributionModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingGoal, setEditingGoal] = useState(null);
  const [contributingGoal, setContributingGoal] = useState(null);
  const [deletingGoalId, setDeletingGoalId] = useState(null);
  const [selectedColor, setSelectedColor] = useState(colors[0]);

  const { register, handleSubmit, reset, formState: { errors } } = useForm();
  const {
    register: registerContribution,
    handleSubmit: handleContribution,
    reset: resetContribution,
  } = useForm();

  useEffect(() => {
    fetchGoals();
    fetchAccounts();
  }, []);

  const fetchGoals = async () => {
    try {
      const response = await goalsApi.getAll();
      setGoals(response.data);
    } catch (error) {
      toast.error('Failed to fetch goals');
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    try {
      const response = await accountsApi.getAll();
      setAccounts(response.data);
    } catch (error) {
      console.error('Failed to fetch accounts:', error);
    }
  };

  const openCreateModal = () => {
    setEditingGoal(null);
    setSelectedColor(colors[0]);
    reset({
      name: '',
      targetAmount: '',
      deadline: '',
      priority: 'MEDIUM',
      description: '',
      color: colors[0],
    });
    setIsModalOpen(true);
  };

  const openEditModal = (goal) => {
    setEditingGoal(goal);
    setSelectedColor(goal.color || colors[0]);
    reset({
      name: goal.name,
      targetAmount: goal.targetAmount,
      deadline: goal.deadline,
      priority: goal.priority,
      description: goal.description || '',
      color: goal.color || colors[0],
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingGoal(null);
    reset();
  };

  const openContributionModal = (goal) => {
    setContributingGoal(goal);
    resetContribution({ 
      amount: '',
      accountId: accounts[0]?.id || ''
    });
    setIsContributionModalOpen(true);
  };

  const closeContributionModal = () => {
    setIsContributionModalOpen(false);
    setContributingGoal(null);
    resetContribution();
  };

  const onSubmit = async (data) => {
    try {
      const payload = {
        ...data,
        targetAmount: parseFloat(data.targetAmount),
        color: selectedColor,
      };

      if (editingGoal) {
        await goalsApi.update(editingGoal.id, payload);
        toast.success('Goal updated successfully');
      } else {
        await goalsApi.create(payload);
        toast.success('Goal created successfully');
      }
      closeModal();
      fetchGoals();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const onContribute = async (data) => {
    try {
      await goalsApi.addContribution(contributingGoal.id, {
        amount: parseFloat(data.amount),
        accountId: data.accountId,
        goalId: contributingGoal.id,
      });
      toast.success('Contribution added successfully');
      closeContributionModal();
      fetchGoals();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add contribution');
    }
  };

  const handleDelete = async () => {
    try {
      await goalsApi.delete(deletingGoalId);
      toast.success('Goal deleted successfully');
      setIsDeleteDialogOpen(false);
      setDeletingGoalId(null);
      fetchGoals();
    } catch (error) {
      toast.error('Failed to delete goal');
    }
  };

  const formatCurrency = useCurrencyFormatter();

  const getPriorityStyle = (priority) => {
    return priorities.find((p) => p.value === priority) || priorities[1];
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const activeGoals = goals.filter((g) => !g.completed);
  const completedGoals = goals.filter((g) => g.completed);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Savings Goals</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <PlusIcon className="h-5 w-5" />
          Add Goal
        </button>
      </div>

      {goals.length === 0 ? (
        <EmptyState
          icon={FlagIcon}
          title="No goals yet"
          description="Set savings goals to track your progress towards financial objectives."
          action={
            <button onClick={openCreateModal} className="btn-primary">
              Add Goal
            </button>
          }
        />
      ) : (
        <div className="space-y-8">
          {/* Active Goals */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Active Goals</h2>
            {activeGoals.length === 0 ? (
              <p className="text-gray-500">All goals completed! Create a new one.</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {activeGoals.map((goal) => {
                  const progress = (goal.currentAmount / goal.targetAmount) * 100;
                  const priorityStyle = getPriorityStyle(goal.priority);

                  return (
                    <div key={goal.id} className="card" style={{ borderLeftColor: goal.color, borderLeftWidth: '4px' }}>
                      <div className="flex items-start justify-between mb-4">
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <h3 className="font-semibold text-gray-900">{goal.name}</h3>
                            <span
                              className={`text-xs px-2 py-0.5 rounded-full ${priorityStyle.color}`}
                            >
                              {priorityStyle.label}
                            </span>
                          </div>
                          {goal.deadline && (
                            <p className="text-sm text-gray-500">
                              Target: {format(new Date(goal.deadline), 'MMM dd, yyyy')}
                            </p>
                          )}
                        </div>
                        <div className="flex gap-1">
                          <button
                            onClick={() => openEditModal(goal)}
                            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
                          >
                            <PencilIcon className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => {
                              setDeletingGoalId(goal.id);
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
                          <span className="text-gray-500">Progress</span>
                          <span className="text-gray-900">{progress.toFixed(0)}%</span>
                        </div>

                        <div className="w-full bg-gray-200 rounded-full h-3">
                          <div
                            className="h-3 rounded-full transition-all"
                            style={{ width: `${Math.min(progress, 100)}%`, 
                              backgroundColor: goal.color}}
                          />
                        </div>

                        <div className="flex justify-between text-sm">
                          <span className="text-gray-500">
                            {formatCurrency(goal.currentAmount)} of{' '}
                            {formatCurrency(goal.targetAmount)}
                          </span>
                        </div>

                        <button
                          onClick={() => openContributionModal(goal)}
                          className="btn-secondary w-full flex items-center justify-center gap-2"
                        >
                          <CurrencyDollarIcon className="h-4 w-4" />
                          Add Contribution
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Completed Goals */}
          {completedGoals.length > 0 && (
            <div>
              <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <CheckCircleIcon className="h-5 w-5 text-primary-600" />
                Completed Goals
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {completedGoals.map((goal) => (
                  <div key={goal.id} className="card bg-primary-50 border-primary-200">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                        <CheckCircleIcon className="h-5 w-5 text-primary-600" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900">{goal.name}</h3>
                        <p className="text-sm text-gray-600">
                          {formatCurrency(goal.targetAmount)} saved
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingGoal ? 'Edit Goal' : 'Create Goal'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Goal Name
            </label>
            <input
              {...register('name', { required: 'Goal name is required' })}
              className="input-field"
              placeholder="e.g., Emergency Fund"
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Target Amount
              </label>
              <input
                type="number"
                step="0.01"
                {...register('targetAmount', {
                  required: 'Target amount is required',
                  min: { value: 0.01, message: 'Must be positive' },
                })}
                className="input-field"
                placeholder="0.00"
              />
              {errors.targetAmount && (
                <p className="mt-1 text-sm text-red-600">{errors.targetAmount.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Target Date
              </label>
              <input
                type="date"
                {...register('deadline')}
                className="input-field"
                required={true}
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Priority
            </label>
            <select {...register('priority')} className="input-field">
              {priorities.map((priority) => (
                <option key={priority.value} value={priority.value}>
                  {priority.label}
                </option>
              ))}
            </select>
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
                  onClick={() => setSelectedColor(color)}
                  className={`w-8 h-8 rounded-full border-2 transition-all ${
                    selectedColor === color ? 'border-gray-900 scale-110' : 'border-gray-300'
                  }`}
                  style={{ backgroundColor: color }}
                />
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description (Optional)
            </label>
            <textarea
              {...register('description')}
              className="input-field"
              rows={3}
              placeholder="What are you saving for?"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={closeModal} className="btn-secondary flex-1">
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              {editingGoal ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Contribution Modal */}
      <Modal
        isOpen={isContributionModalOpen}
        onClose={closeContributionModal}
        title="Add Contribution"
      >
        <form onSubmit={handleContribution(onContribute)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Account
            </label>
            <select
              {...registerContribution('accountId', { required: 'Account is required' })}
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

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Amount
            </label>
            <input
              type="number"
              step="0.01"
              {...registerContribution('amount', {
                required: 'Amount is required',
                min: { value: 0.01, message: 'Must be positive' },
              })}
              className="input-field"
              placeholder="0.00"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={closeContributionModal}
              className="btn-secondary flex-1"
            >
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              Add
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Goal"
        message="Are you sure you want to delete this goal? Your contribution history will also be removed."
      />
    </div>
  );
}
