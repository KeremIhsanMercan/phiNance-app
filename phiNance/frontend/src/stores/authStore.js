import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set, _get) => ({
      token: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,

      setTokens: (token, refreshToken) => {
        set({ token, refreshToken, isAuthenticated: true });
      },

      setUser: (user) => {
        set({ user });
      },

      login: (authResponse) => {
        set({
          token: authResponse.accessToken,
          refreshToken: authResponse.refreshToken,
          user: authResponse.user,
          isAuthenticated: true,
        });
      },

      logout: () => {
        set({
          token: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        });
      },

      updateUser: (userData) => {
        set((state) => ({
          user: { ...state.user, ...userData },
        }));
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
