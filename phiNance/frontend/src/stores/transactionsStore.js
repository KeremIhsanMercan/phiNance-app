import { create } from 'zustand';
import { transactionsApi } from '../services/api';

export const useTransactionsStore = create((set, get) => ({
  transactions: [],
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  },
  filters: {
    startDate: null,
    endDate: null,
    accountId: null,
    categoryId: null,
    type: null,
    sortBy: 'date',
    sortDirection: 'desc',
  },
  loading: false,
  error: null,

  setFilters: (newFilters) => {
    set((state) => ({
      filters: { ...state.filters, ...newFilters },
    }));
  },

  fetchTransactions: async (page = 0) => {
    const { filters } = get();
    set({ loading: true, error: null });
    try {
      const response = await transactionsApi.getAll({
        ...filters,
        page,
        size: 20,
      });
      set({
        transactions: response.data.content,
        pagination: {
          page: response.data.number,
          size: response.data.size,
          totalElements: response.data.totalElements,
          totalPages: response.data.totalPages,
        },
        loading: false,
      });
    } catch (error) {
      set({ error: error.message, loading: false });
    }
  },

  createTransaction: async (data) => {
    const response = await transactionsApi.create(data);
    set((state) => ({
      transactions: [response.data, ...state.transactions],
    }));
    return response.data;
  },

  updateTransaction: async (id, data) => {
    const response = await transactionsApi.update(id, data);
    set((state) => ({
      transactions: state.transactions.map((t) =>
        t.id === id ? response.data : t
      ),
    }));
    return response.data;
  },

  deleteTransaction: async (id) => {
    await transactionsApi.delete(id);
    set((state) => ({
      transactions: state.transactions.filter((t) => t.id !== id),
    }));
  },
}));
