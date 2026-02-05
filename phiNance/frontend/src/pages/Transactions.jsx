import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { format } from 'date-fns';
import { useTransactionsStore } from '../stores/transactionsStore';
import { useAccountsStore } from '../stores/accountsStore';
import { categoriesApi, filesApi, transactionsApi, favoriteFiltersApi } from '../services/api';
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
  PaperClipIcon,
  XMarkIcon,
  ArrowDownTrayIcon,
  MagnifyingGlassIcon,
  ChevronUpIcon,
  ChevronDownIcon,
  StarIcon,
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
  const [deletingTransaction, setDeletingTransaction] = useState(null);
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [newlyUploadedFiles, setNewlyUploadedFiles] = useState([]); // Track files uploaded in current session
  const [uploadingFiles, setUploadingFiles] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    search: '',
    type: '',
    accountId: '',
    categoryId: '',
    dateFrom: '',
    dateTo: '',
    amountMin: '',
    amountMax: '',
  });
  const [savedFilters, setSavedFilters] = useState([]);
  const [sortBy, setSortBy] = useState('date');
  const [sortOrder, setSortOrder] = useState('desc');

  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm({
    defaultValues: {
      accountId: '',
    }
  });
  const watchType = watch('type', 'EXPENSE');
  const watchRecurring = watch('recurring', false);

  useEffect(() => {
    fetchAccounts();
    fetchCategories();
    // Load saved filters from backend
    loadSavedFiltersFromBackend();
  }, []);

  const loadSavedFiltersFromBackend = async () => {
    try {
      const response = await favoriteFiltersApi.getAll();
      setSavedFilters(response.data || []);
    } catch (error) {
      console.error('Failed to load favorite filters:', error);
    }
  };

  useEffect(() => {
    if (!isModalOpen || editingTransaction) return;
    if (accounts.length > 0 && !watch('accountId')) {
      setValue('accountId', accounts[0].id);
    }
  }, [accounts, isModalOpen, editingTransaction, setValue, watch]);

  // Fetch transactions when filters or sorting changes (with debounce for text inputs)
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      fetchTransactions(buildQueryParams());
    }, 500); // 500ms debounce

    return () => clearTimeout(timeoutId);
  }, [filters, sortBy, sortOrder]);

  const fetchCategories = async () => {
    try {
      const response = await categoriesApi.getAll({ page: 0, size: 100 });
      // first list income categories, then expense categories
      const sortedCategories = response.data.content.sort((a, b) => {
        if (a.type === b.type) return 0;
        return a.type === 'INCOME' ? -1 : 1;
      });
      setCategories(sortedCategories || []);
    } catch (error) {
      console.error('Failed to fetch categories:', error);
    }
  };

  const buildQueryParams = (page = 0) => {
    const params = {
      searchQuery: filters.search || undefined,
      type: filters.type || undefined,
      accountId: filters.accountId || undefined,
      categoryId: filters.categoryId || undefined,
      startDate: filters.dateFrom || undefined,
      endDate: filters.dateTo || undefined,
      minAmount: filters.amountMin ? parseFloat(filters.amountMin) : undefined,
      maxAmount: filters.amountMax ? parseFloat(filters.amountMax) : undefined,
      sortBy: sortBy,
      sortDirection: sortOrder,
      page: page,
      size: 20,
    };
    
    // Remove undefined values
    Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
    return params;
  };

  const openCreateModal = () => {
    setEditingTransaction(null);
    setUploadedFiles([]);
    setNewlyUploadedFiles([]); // Reset tracker for new session
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
    setUploadedFiles(transaction.attachmentUrls || []);
    setNewlyUploadedFiles([]); // Reset tracker for edit session
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

  const closeModal = async (shouldCleanupFiles = true) => {
    // If we were creating a new transaction (not editing) and uploaded files, clean them up
    // Only cleanup if shouldCleanupFiles is true (i.e., user cancelled without saving)
    if (shouldCleanupFiles && !editingTransaction && newlyUploadedFiles.length > 0) {
      // Delete all newly uploaded files since transaction wasn't created
      const deletePromises = newlyUploadedFiles.map(async (fileUrl) => {
        try {
          const urlMatch = fileUrl.match(/\/api\/files\/([^\\/]+)\/([^?]+)/);
          if (urlMatch) {
            const userId = urlMatch[1];
            const filename = decodeURIComponent(urlMatch[2]);
            await filesApi.delete(userId, filename);
          }
        } catch (error) {
          console.error('Failed to clean up file:', error);
        }
      });
      await Promise.all(deletePromises);
    }
    
    setIsModalOpen(false);
    setEditingTransaction(null);
    setUploadedFiles([]);
    setNewlyUploadedFiles([]);
    reset();
  };

  const handleFileUpload = async (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;

    // Client-side validation
    const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    const MAX_ATTACHMENTS = 5;
    
    // Check attachment limit
    if (uploadedFiles.length + files.length > MAX_ATTACHMENTS) {
      toast.error(`Maximum ${MAX_ATTACHMENTS} attachments allowed. You currently have ${uploadedFiles.length} attachment(s).`);
      e.target.value = ''; // Reset file input
      return;
    }
    
    const oversizedFiles = files.filter(file => file.size > MAX_FILE_SIZE);
    
    if (oversizedFiles.length > 0) {
      const fileNames = oversizedFiles.map(f => f.name).join(', ');
      toast.error(`File(s) too large: ${fileNames}. Maximum size is 5MB per file.`);
      e.target.value = ''; // Reset file input
      return;
    }

    setUploadingFiles(true);
    try {
      const response = await filesApi.upload(files);
      const newUploadedFiles = [...uploadedFiles, ...response.data];
      setUploadedFiles(newUploadedFiles);
      
      // Track newly uploaded files only when creating (not editing)
      if (!editingTransaction) {
        setNewlyUploadedFiles(prev => [...prev, ...response.data]);
      }
      
      toast.success('Files uploaded successfully');
      e.target.value = ''; // Reset file input after success
      
      // If editing a transaction, automatically update it
      if (editingTransaction) {
        const currentFormData = watch();
        const payload = {
          ...currentFormData,
          amount: parseFloat(currentFormData.amount),
          categoryId: currentFormData.categoryId || null,
          transferToAccountId: currentFormData.type === 'TRANSFER' ? (currentFormData.transferToAccountId || editingTransaction?.transferToAccountId) : null,
          recurrencePattern: currentFormData.recurring ? currentFormData.recurrencePattern : null,
          attachmentUrls: newUploadedFiles,
        };
        await updateTransaction(editingTransaction.id, payload);
        toast.success('Transaction updated with new attachments');
        fetchTransactions(buildQueryParams());
      }
    } catch (error) {
      console.log('file upload error', error);
      toast.error(error.response?.data?.message || 'Failed to upload files');
      e.target.value = ''; // Reset file input
    } finally {
      setUploadingFiles(false);
    }
  };

  const removeFile = async (fileUrl) => {
    try {
      // Extract userId and filename from URL
      // URL format: /api/files/{userId}/{filename}?token=xxx
      const urlMatch = fileUrl.match(/\/api\/files\/([^\\/]+)\/([^?]+)/);
      if (urlMatch) {
        const userId = urlMatch[1];
        const filename = decodeURIComponent(urlMatch[2]);
        
        await filesApi.delete(userId, filename);
        const newUploadedFiles = uploadedFiles.filter(url => url !== fileUrl);
        setUploadedFiles(newUploadedFiles);
        
        // Also remove from newly uploaded files tracker
        setNewlyUploadedFiles(newlyUploadedFiles.filter(url => url !== fileUrl));
        
        toast.success('File deleted successfully');
        
        // If editing a transaction, automatically update it
        if (editingTransaction) {
          const currentFormData = watch();
          const payload = {
            ...currentFormData,
            amount: parseFloat(currentFormData.amount),
            categoryId: currentFormData.categoryId || null,
            transferToAccountId: currentFormData.type === 'TRANSFER' ? (currentFormData.transferToAccountId || editingTransaction?.transferToAccountId) : null,
            recurrencePattern: currentFormData.recurring ? currentFormData.recurrencePattern : null,
            attachmentUrls: newUploadedFiles,
          };
          await updateTransaction(editingTransaction.id, payload);
          toast.success('Transaction updated');
          fetchTransactions(buildQueryParams());
        }
      } else {
        // If URL doesn't match expected format, just remove from state
        setUploadedFiles(uploadedFiles.filter(url => url !== fileUrl));
      }
    } catch (error) {
      console.error('File deletion error:', error);
      toast.error('Failed to delete file');
    }
  };

  const onSubmit = async (data) => {
    try {
      const payload = {
        ...data,
        amount: parseFloat(data.amount),
        categoryId: data.categoryId || null,
        transferToAccountId: data.type === 'TRANSFER' ? (data.transferToAccountId || editingTransaction?.transferToAccountId) : null,
        recurrencePattern: data.recurring ? data.recurrencePattern : null,
        attachmentUrls: uploadedFiles,
      };

      if (editingTransaction) {
        await updateTransaction(editingTransaction.id, payload);
        toast.success('Transaction updated successfully');
      } else {
        await createTransaction(payload);
        toast.success('Transaction created successfully');
      }
      closeModal(false); // Don't cleanup files since transaction was created successfully
      fetchTransactions(buildQueryParams());
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async () => {
    try {
      // Delete attachments first if any exist
      if (deletingTransaction?.attachmentUrls && deletingTransaction.attachmentUrls.length > 0) {
        const deletePromises = deletingTransaction.attachmentUrls.map(async (fileUrl) => {
          try {
            const urlMatch = fileUrl.match(/\/api\/files\/([^\\/]+)\/([^?]+)/);
            if (urlMatch) {
              const userId = urlMatch[1];
              const filename = decodeURIComponent(urlMatch[2]);
              await filesApi.delete(userId, filename);
            }
          } catch (error) {
            console.error('Failed to delete attachment:', error);
          }
        });
        await Promise.all(deletePromises);
      }
      
      // Then delete the transaction
      await deleteTransaction(deletingTransaction.id);
      toast.success('Transaction deleted successfully');
      setIsDeleteDialogOpen(false);
      setDeletingTransaction(null);
    } catch (error) {
      toast.error('Failed to delete transaction');
    }
  };

  const generateFilterName = () => {
    const parts = [];
    if (filters.search) parts.push(`Search: "${filters.search}"`);
    if (filters.type) parts.push(`Type: ${filters.type}`);
    if (filters.accountId) {
      const account = accounts.find(a => a.id === filters.accountId);
      if (account) parts.push(`Account: ${account.name}`);
    }
    if (filters.categoryId) {
      const category = categories.find(c => c.id === filters.categoryId);
      if (category) parts.push(`Category: ${category.name}`);
    }
    if (filters.dateFrom) parts.push(`From: ${filters.dateFrom}`);
    if (filters.dateTo) parts.push(`To: ${filters.dateTo}`);
    if (filters.amountMin) parts.push(`Min: ${filters.amountMin}`);
    if (filters.amountMax) parts.push(`Max: ${filters.amountMax}`);
    
    return parts.length > 0 ? parts.join(' - ') : 'Unnamed Filter';
  };

  const isFilterActive = () => {
    return Object.values(filters).some(value => value !== '');
  };

  const saveCurrentFilter = async () => {
    try {
      const filterName = generateFilterName();
      const response = await favoriteFiltersApi.save(filterName, filters);
      setSavedFilters([...savedFilters, response.data]);
      toast.success('Filter saved successfully');
    } catch (error) {
      console.error('Failed to save filter:', error);
      toast.error('Failed to save filter');
    }
  };

  const loadSavedFilter = (savedFilterData) => {
    setFilters(savedFilterData.filters);
    toast.success(`Loaded filter: ${savedFilterData.name}`);
  };

  const deleteSavedFilter = async (filterId) => {
    try {
      await favoriteFiltersApi.delete(filterId);
      setSavedFilters(savedFilters.filter(f => f.id !== filterId));
      toast.success('Filter deleted');
    } catch (error) {
      console.error('Failed to delete filter:', error);
      toast.error('Failed to delete filter');
    }
  };

  const getMatchingSavedFilter = () => {
    return savedFilters.find(savedFilter => 
      JSON.stringify(savedFilter.filters) === JSON.stringify(filters)
    );
  };

  const handleStarClick = () => {
    const matchingFilter = getMatchingSavedFilter();
    if (matchingFilter) {
      // If current filters match a saved filter, remove it
      deleteSavedFilter(matchingFilter.id);
    } else {
      // Otherwise, save current filters
      saveCurrentFilter();
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const clearFilters = () => {
    setFilters({
      search: '',
      type: '',
      accountId: '',
      categoryId: '',
      dateFrom: '',
      dateTo: '',
      amountMin: '',
      amountMax: '',
    });
  };

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
  };

  const exportToCSV = async () => {
    try {
      // Fetch all transactions matching current filters (no pagination)
      const params = buildQueryParams(0);
      delete params.page; // Remove pagination params for export
      delete params.size;
      
      const response = await transactionsApi.getAllForExport(params);
      const allTransactions = response.data;
      
      if (allTransactions.length === 0) {
        toast.error('No transactions to export');
        return;
      }

      const headers = ['Date', 'Type', 'Account', 'Category', 'Description', 'Amount'];
      const csvData = allTransactions.map(t => [
        format(new Date(t.date), 'yyyy-MM-dd'),
        t.type,
        getAccountName(t.accountId),
        getCategoryName(t.categoryId, t),
        t.description || '',
        t.amount
      ]);

      const csvContent = [
        headers.join(','),
        ...csvData.map(row => row.map(cell => `"${cell}"`).join(','))
      ].join('\n');

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `transactions_${format(new Date(), 'yyyy-MM-dd')}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      toast.success(`Exported ${allTransactions.length} transactions successfully`);
    } catch (error) {
      toast.error('Failed to export transactions');
      console.error('Export error:', error);
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

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <div className="flex flex-wrap gap-2">
          <button 
            onClick={() => setShowFilters(!showFilters)} 
            className="btn-secondary flex items-center gap-2 text-sm sm:text-base"
          >
            <FunnelIcon className="h-5 w-5" />
            <span className="hidden sm:inline">Filters</span>
          </button>
          <button 
            onClick={exportToCSV} 
            className="btn-secondary flex items-center gap-2 text-sm sm:text-base"
            disabled={transactions.length === 0}
          >
            <ArrowDownTrayIcon className="h-5 w-5" />
            <span className="hidden sm:inline">Export</span>
          </button>
          <button onClick={openCreateModal} className="btn-primary flex items-center gap-2 text-sm sm:text-base">
            <PlusIcon className="h-5 w-5" />
            <span className="hidden sm:inline">Add Transaction</span>
          </button>
        </div>
      </div>

      {/* Filters Panel */}
      {showFilters && (
        <div className="card mb-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <h3 className="font-semibold text-gray-900">Filters</h3>
              {isFilterActive() && (
                <button
                  onClick={handleStarClick}
                  className={`flex items-center gap-1 text-sm transition-colors ${
                    getMatchingSavedFilter() 
                      ? 'text-yellow-500 hover:text-yellow-600' 
                      : 'text-gray-600 hover:text-yellow-500'
                  }`}
                  title={getMatchingSavedFilter() ? 'Remove favorite filter' : 'Save current filters'}
                >
                  {getMatchingSavedFilter() ? (
                    <StarIcon className="h-5 w-5 fill-current" />
                  ) : (
                    <StarIcon className="h-5 w-5" />
                  )}
                </button>
              )}
              {savedFilters.length > 0 && (
                <div className="relative group pb-1">
                  <button className="text-sm text-primary-600 hover:text-primary-700 px-2 py-1 rounded hover:bg-primary-50">
                    My Filters ({savedFilters.length})
                  </button>
                  <div className="hidden group-hover:block absolute left-0 top-full -mt-1 w-64 bg-white border border-gray-200 rounded-lg shadow-lg z-10 pt-2">
                    <div className="max-h-64 overflow-y-auto">
                      {savedFilters.map((savedFilter) => (
                        <div
                          key={savedFilter.id}
                          className="flex items-center justify-between gap-2 p-3 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 group/item relative"
                        >
                          <button
                            onClick={() => loadSavedFilter(savedFilter)}
                            className="flex-1 min-w-0 text-left hover:text-primary-600 relative group/button"
                            title={savedFilter.name}
                          >
                            <p className="text-sm font-medium text-gray-900 truncate">
                              {savedFilter.name}
                            </p>
                          </button>
                          <button
                            onClick={() => deleteSavedFilter(savedFilter.id)}
                            className="text-gray-400 hover:text-red-600 flex-shrink-0 opacity-0 group-hover/item:opacity-100 transition-opacity"
                          >
                            <XMarkIcon className="h-4 w-4" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
            <button onClick={clearFilters} className="text-sm text-primary-600 hover:text-primary-700">
              Clear All
            </button>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Search */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Search</label>
              <div className="relative">
                <input
                  type="text"
                  value={filters.search}
                  onChange={(e) => handleFilterChange('search', e.target.value)}
                  className="input-field pr-10"
                  placeholder="Search description..."
                />
                <MagnifyingGlassIcon className="h-5 w-5 absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              </div>
            </div>

            {/* Type Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
              <select
                value={filters.type}
                onChange={(e) => handleFilterChange('type', e.target.value)}
                className="input-field"
              >
                <option value="">All Types</option>
                {transactionTypes.map(type => (
                  <option key={type.value} value={type.value}>{type.label}</option>
                ))}
              </select>
            </div>

            {/* Account Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Account</label>
              <select
                value={filters.accountId}
                onChange={(e) => handleFilterChange('accountId', e.target.value)}
                className="input-field"
              >
                <option value="">All Accounts</option>
                {accounts.filter(account => account.type !== 'SAVINGS').map(account => ( // other than saving accounts
                  <option key={account.id} value={account.id}>{account.name}</option>
                ))}
              </select>
            </div>

            {/* Category Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <select
                value={filters.categoryId}
                onChange={(e) => handleFilterChange('categoryId', e.target.value)}
                className="input-field"
              >
                <option value="">All Categories</option>
                {categories.map(category => (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
            </div>

            {/* Date From */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date From</label>
              <input
                type="date"
                value={filters.dateFrom}
                onChange={(e) => handleFilterChange('dateFrom', e.target.value)}
                className="input-field"
              />
            </div>

            {/* Date To */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date To</label>
              <input
                type="date"
                value={filters.dateTo}
                onChange={(e) => handleFilterChange('dateTo', e.target.value)}
                className="input-field"
              />
            </div>

            {/* Amount Min */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount Min</label>
              <input
                type="number"
                step="0.01"
                value={filters.amountMin}
                onChange={(e) => handleFilterChange('amountMin', e.target.value)}
                className="input-field"
                placeholder="0.00"
              />
            </div>

            {/* Amount Max */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount Max</label>
              <input
                type="number"
                step="0.01"
                value={filters.amountMax}
                onChange={(e) => handleFilterChange('amountMax', e.target.value)}
                className="input-field"
                placeholder="0.00"
              />
            </div>
          </div>

          {/* Active Filters Summary */}
          {(filters.search || filters.type || filters.accountId || filters.categoryId || 
            filters.dateFrom || filters.dateTo || filters.amountMin || filters.amountMax) && (
            <div className="mt-4 pt-4 border-t border-gray-200">
              <p className="text-sm text-gray-600">
                Showing {transactions.length} of {pagination.totalElements} transactions
              </p>
            </div>
          )}
        </div>
      )}

      {loading && transactions.length === 0 ? (
        <div className="flex items-center justify-center h-64">
          <LoadingSpinner size="lg" />
        </div>
      ) : transactions.length === 0 ? (
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
        <div className="card relative">
          {loading && (
            <div className="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center z-10 rounded-lg">
              <LoadingSpinner size="md" />
            </div>
          )}
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th 
                    className="text-left py-3 px-4 text-sm font-medium text-gray-500 cursor-pointer hover:text-gray-700 select-none"
                    onClick={() => handleSort('date')}
                  >
                    <div className="flex items-center gap-1">
                      Date
                      {sortBy === 'date' && (
                        sortOrder === 'asc' ? <ChevronUpIcon className="h-4 w-4" /> : <ChevronDownIcon className="h-4 w-4" />
                      )}
                    </div>
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500 cursor-pointer hover:text-gray-700 select-none"
                    onClick={() => handleSort('type')}
                  >
                    <div className="flex items-center gap-1">
                      Type
                      {sortBy === 'type' && (
                        sortOrder === 'asc' ? <ChevronUpIcon className="h-4 w-4" /> : <ChevronDownIcon className="h-4 w-4" />
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-left py-3 px-4 text-sm font-medium text-gray-500 cursor-pointer hover:text-gray-700 select-none"
                    onClick={() => handleSort('account')}
                  >
                    <div className="flex items-center gap-1">
                      Account
                      {sortBy === 'account' && (
                        sortOrder === 'asc' ? <ChevronUpIcon className="h-4 w-4" /> : <ChevronDownIcon className="h-4 w-4" />
                      )}
                    </div>
                  </th>
                  <th 
                    className="text-left py-3 px-4 text-sm font-medium text-gray-500 cursor-pointer hover:text-gray-700 select-none"
                    onClick={() => handleSort('category')}
                  >
                    <div className="flex items-center gap-1">
                      Category
                      {sortBy === 'category' && (
                        sortOrder === 'asc' ? <ChevronUpIcon className="h-4 w-4" /> : <ChevronDownIcon className="h-4 w-4" />
                      )}
                    </div>
                  </th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Description</th>
                  <th 
                    className="text-right py-3 px-4 text-sm font-medium text-gray-500 cursor-pointer hover:text-gray-700 select-none"
                    onClick={() => handleSort('amount')}
                  >
                    <div className="flex items-center justify-end gap-1">
                      Amount
                      {sortBy === 'amount' && (
                        sortOrder === 'asc' ? <ChevronUpIcon className="h-4 w-4" /> : <ChevronDownIcon className="h-4 w-4" />
                      )}
                    </div>
                  </th>
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
                      <td className="py-3 px-4 text-sm text-gray-600 max-w-xs">
                        <div className="flex items-center gap-2">
                          <span className="truncate">{transaction.description || '-'}</span>
                          {transaction.attachmentUrls && transaction.attachmentUrls.length > 0 && (
                            <span className="flex items-center gap-1 text-primary-600 flex-shrink-0">
                              <PaperClipIcon className="h-4 w-4" />
                              <span className="text-xs">{transaction.attachmentUrls.length}</span>
                            </span>
                          )}
                        </div>
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
                              setDeletingTransaction(transaction);
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
            <div className="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-200">
              <p className="text-sm text-gray-500 text-center sm:text-left">
                Showing {pagination.page * pagination.size + 1} to{' '}
                {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
                {pagination.totalElements} results
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => fetchTransactions(buildQueryParams(pagination.page - 1))}
                  disabled={pagination.page === 0}
                  className="btn-secondary disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => fetchTransactions(buildQueryParams(pagination.page + 1))}
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
                // if changed to INCOME or EXPENSE, set categories to their respective types
                if (value === 'INCOME' || value === 'EXPENSE') {
                  reset({
                    ...watch(),
                    type: value,
                    categoryId: '',
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
                {accounts
                  .filter((account) => {
                    // For editing: include SAVINGS accounts if this transaction originally had a SAVINGS destination
                    if (editingTransaction && editingTransaction.transferToAccountId) {
                      return account.type !== 'SAVINGS' || account.id === editingTransaction.transferToAccountId;
                    }
                    // For creating: exclude all SAVINGS accounts
                    return account.type !== 'SAVINGS';
                  })
                  .map((account) => (
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
              Description {watchRecurring ? '(Required for recurring transactions)' : '(Optional)'}
            </label>
            <input
              {...register('description')}
              className="input-field"
              placeholder="Transaction description"
              // required if recurring is true
              {...(watchRecurring ? { required: 'Description is required for recurring transactions' } : {})}
            />
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="recurring"
              {...register('recurring')}
              className="h-4 w-4 text-primary-600 rounded disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={watchType === 'TRANSFER'}
            />
            <label htmlFor="recurring" className={`text-sm ${watchType === 'TRANSFER' ? 'text-gray-400' : 'text-gray-700'}`}>
              Recurring transaction
            </label>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Attachments (Optional) - Max 5 files
            </label>
            <div className="space-y-2">
              <label className={`flex items-center justify-center px-4 py-3 border-2 border-dashed border-gray-300 rounded-lg transition-colors ${uploadedFiles.length >= 5 ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:border-primary-500 hover:bg-gray-50'}`}>
                <PaperClipIcon className="h-5 w-5 text-gray-400 mr-2" />
                <span className="text-sm text-gray-600">
                  {uploadingFiles ? 'Uploading...' : `Upload receipts (${uploadedFiles.length}/5) - JPG, PNG, PDF`}
                </span>
                <input
                  type="file"
                  multiple
                  accept=".jpg,.jpeg,.png,.pdf,.gif"
                  onChange={handleFileUpload}
                  disabled={uploadingFiles || uploadedFiles.length >= 5}
                  className="hidden"
                />
              </label>
              
              {uploadedFiles.length > 0 && (
                <div className="space-y-2">
                  {uploadedFiles.map((fileUrl, index) => (
                    <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                      <div className="flex items-center gap-2">
                        <PaperClipIcon className="h-4 w-4 text-gray-400" />
                        <a
                          href={fileUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm text-primary-600 hover:text-primary-700"
                        >
                          Attachment {index + 1}
                        </a>
                      </div>
                      <button
                        type="button"
                        onClick={() => removeFile(fileUrl)}
                        className="p-1 text-gray-400 hover:text-red-600 rounded"
                      >
                        <XMarkIcon className="h-4 w-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
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
