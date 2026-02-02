import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { categoriesApi } from '../services/api';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import LoadingSpinner from '../components/LoadingSpinner';
import EmptyState from '../components/EmptyState';
import {
  PlusIcon,
  TagIcon,
  PencilIcon,
  TrashIcon,
  ArrowUpIcon,
  ArrowDownIcon,
} from '@heroicons/react/24/outline';

const categoryTypes = [
  { value: 'INCOME', label: 'Income', icon: ArrowUpIcon, color: 'text-amber-600', bg: 'bg-amber-100' },
  { value: 'EXPENSE', label: 'Expense', icon: ArrowDownIcon, color: 'text-red-600', bg: 'bg-red-100' },
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

const icons = [
  'ShoppingCart', 'Home', 'Car', 'Utensils', 'Film',
  'Heart', 'Book', 'Plane', 'Gift', 'Briefcase',
];

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [deletingCategoryId, setDeletingCategoryId] = useState(null);

  const { register, handleSubmit, reset, setValue, watch, formState: { errors } } = useForm();
  const selectedColor = watch('color', colors[0]);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const response = await categoriesApi.getAll();
      setCategories(response.data);
    } catch (error) {
      toast.error('Failed to fetch categories');
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingCategory(null);
    reset({
      name: '',
      type: 'EXPENSE',
      color: colors[0],
      icon: icons[0],
      description: '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (category) => {
    setEditingCategory(category);
    reset({
      name: category.name,
      type: category.type,
      color: category.color || colors[0],
      icon: category.icon || icons[0],
      description: category.description || '',
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingCategory(null);
    reset();
  };

  const onSubmit = async (data) => {
    try {
      if (editingCategory) {
        await categoriesApi.update(editingCategory.id, data);
        toast.success('Category updated successfully');
      } else {
        await categoriesApi.create(data);
        toast.success('Category created successfully');
      }
      closeModal();
      fetchCategories();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async () => {
    try {
      await categoriesApi.delete(deletingCategoryId);
      toast.success('Category deleted successfully');
      setIsDeleteDialogOpen(false);
      setDeletingCategoryId(null);
      fetchCategories();
    } catch (error) {
      toast.error('Failed to delete category');
    }
  };

  const getCategoryStyle = (type) => {
    return categoryTypes.find((t) => t.value === type) || categoryTypes[1];
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const incomeCategories = categories.filter((c) => c.type === 'INCOME');
  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Categories</h1>
        <button onClick={openCreateModal} className="btn-primary flex items-center gap-2">
          <PlusIcon className="h-5 w-5" />
          Add Category
        </button>
      </div>

      {categories.length === 0 ? (
        <EmptyState
          icon={TagIcon}
          title="No categories yet"
          description="Create categories to organize your income and expenses."
          action={
            <button onClick={openCreateModal} className="btn-primary">
              Add Category
            </button>
          }
        />
      ) : (
        <div className="space-y-8">
          {/* Income Categories */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <ArrowUpIcon className="h-5 w-5 text-amber-600" />
              Income Categories
            </h2>
            {incomeCategories.length === 0 ? (
              <p className="text-gray-500 text-sm">No income categories yet.</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {incomeCategories.map((category) => (
                  <CategoryCard
                    key={category.id}
                    category={category}
                    onEdit={() => openEditModal(category)}
                    onDelete={() => {
                      setDeletingCategoryId(category.id);
                      setIsDeleteDialogOpen(true);
                    }}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Expense Categories */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <ArrowDownIcon className="h-5 w-5 text-red-600" />
              Expense Categories
            </h2>
            {expenseCategories.length === 0 ? (
              <p className="text-gray-500 text-sm">No expense categories yet.</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {expenseCategories.map((category) => (
                  <CategoryCard
                    key={category.id}
                    category={category}
                    onEdit={() => openEditModal(category)}
                    onDelete={() => {
                      setDeletingCategoryId(category.id);
                      setIsDeleteDialogOpen(true);
                    }}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingCategory ? 'Edit Category' : 'Create Category'}
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category Name
            </label>
            <input
              {...register('name', { required: 'Category name is required' })}
              className="input-field"
              placeholder="e.g., Groceries"
            />
            {errors.name && (
              <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type
            </label>
            <select {...register('type')} className="input-field">
              {categoryTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
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

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description (Optional)
            </label>
            <input
              {...register('description')}
              className="input-field"
              placeholder="Category description"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button type="button" onClick={closeModal} className="btn-secondary flex-1">
              Cancel
            </button>
            <button type="submit" className="btn-primary flex-1">
              {editingCategory ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={handleDelete}
        title="Delete Category"
        message="Are you sure you want to delete this category? Transactions using this category will remain but won't be categorized."
      />
    </div>
  );
}

function CategoryCard({ category, onEdit, onDelete }) {
  return (
    <div
      className="card hover:shadow-md transition-shadow"
      style={{ borderLeftColor: category.color, borderLeftWidth: '4px' }}
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: `${category.color}20` }}
          >
            <TagIcon className="h-5 w-5" style={{ color: category.color }} />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900">{category.name}</h3>
            {category.description && (
              <p className="text-sm text-gray-500 truncate max-w-[150px]">
                {category.description}
              </p>
            )}
          </div>
        </div>
        <div className="flex gap-1">
          <button
            onClick={onEdit}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg"
          >
            <PencilIcon className="h-4 w-4" />
          </button>
          <button
            onClick={onDelete}
            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
          >
            <TrashIcon className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
