import { create } from 'zustand';
import { accountsApi } from '../services/api';

export const useAccountsStore = create((set, _get) => ({
  accounts: [],
  loading: false,
  error: null,
  totalPages: 0,
  totalElements: 0,
  currentPage: 0,
  pageSize: 10,

  fetchAccounts: async (page = 0, size = 10, sortBy = 'name', sortDirection = 'asc') => {
    set({ loading: true, error: null });
    try {
      const response = await accountsApi.getAll({ page, size, sortBy, sortDirection });
      set({ 
        accounts: response.data.content, 
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements,
        currentPage: response.data.number,
        pageSize: response.data.size,
        loading: false 
      });
    } catch (error) {
      set({ error: error.message, loading: false });
    }
  },

  createAccount: async (data) => {
    const response = await accountsApi.create(data);
    set((state) => ({ accounts: [...state.accounts, response.data] }));
    return response.data;
  },

  updateAccount: async (id, data) => {
    const response = await accountsApi.update(id, data);
    set((state) => ({
      accounts: state.accounts.map((acc) =>
        acc.id === id ? response.data : acc
      ),
    }));
    return response.data;
  },

  archiveAccount: async (id) => {
    await accountsApi.archive(id);
    set((state) => ({
      accounts: state.accounts.filter((acc) => acc.id !== id),
    }));
  },
}));
