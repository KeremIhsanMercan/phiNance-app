import { create } from 'zustand';
import { accountsApi } from '../services/api';

export const useAccountsStore = create((set, _get) => ({
  accounts: [],
  loading: false,
  error: null,

  fetchAccounts: async () => {
    set({ loading: true, error: null });
    try {
      const response = await accountsApi.getAll();
      set({ accounts: response.data, loading: false });
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
