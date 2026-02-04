import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

const API_URL = '/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // Don't retry for login/register endpoints
    const isAuthEndpoint = originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/register');

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        const response = await axios.post(`${API_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = response.data;
        useAuthStore.getState().setTokens(accessToken, newRefreshToken);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  // Email verification removed: no-op for compatibility
  verifyEmail: (token) => Promise.resolve({ data: { message: 'Email verification disabled' } }),
  changePassword: (data) => api.post('/auth/change-password', data),
  updateProfile: (data) => api.put('/auth/update-profile', data),
  deleteAccount: (data) => api.post('/auth/delete-account', data),
};

// Accounts API
export const accountsApi = {
  getAll: (params) => api.get('/accounts', { params }),
  getById: (id) => api.get(`/accounts/${id}`),
  create: (data) => api.post('/accounts', data),
  update: (id, data) => api.put(`/accounts/${id}`, data),
  archive: (id) => api.delete(`/accounts/${id}`),
};

// Transactions API
export const transactionsApi = {
  getAll: (params) => api.get('/transactions', { params }),
  getAllForExport: (params) => api.get('/transactions/export', { params }),
  getById: (id) => api.get(`/transactions/${id}`),
  getByDateRange: (startDate, endDate) =>
    api.get('/transactions/date-range', { params: { startDate, endDate } }),
  create: (data) => api.post('/transactions', data),
  update: (id, data) => api.put(`/transactions/${id}`, data),
  delete: (id) => api.delete(`/transactions/${id}`),
};

// Categories API
export const categoriesApi = {
  getAll: (params) => api.get('/categories', { params }),
  getByType: (type) => api.get(`/categories/type/${type}`),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`),
};

// Budgets API
export const budgetsApi = {
  getAll: (params) => api.get('/budgets', { params }),
  getByMonth: (year, month) => api.get('/budgets', { params: { year, month } }),
  getById: (id) => api.get(`/budgets/${id}`),
  compare: (year1, month1, year2, month2) =>
    api.get('/budgets/compare', { params: { year1, month1, year2, month2 } }),
  create: (data) => api.post('/budgets', data),
  update: (id, data) => api.put(`/budgets/${id}`, data),
  delete: (id) => api.delete(`/budgets/${id}`),
};

// Goals API
export const goalsApi = {
  getAll: (params) => api.get('/goals', { params }),
  getActive: () => api.get('/goals/active'),
  getById: (id) => api.get(`/goals/${id}`),
  validateDependencies: (id) => api.get(`/goals/${id}/validate-dependencies`),
  create: (data) => api.post('/goals', data),
  addContribution: (id, data) => api.post('/goals/contribution', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  markComplete: (id) => api.put(`/goals/${id}/complete`),
  addDependency: (id, dependencyId) => api.post(`/goals/${id}/dependencies/${dependencyId}`),
  removeDependency: (id, dependencyId) => api.delete(`/goals/${id}/dependencies/${dependencyId}`),
  delete: (id) => api.delete(`/goals/${id}`),
};

// Dashboard API
export const dashboardApi = {
  getData: () => api.get('/dashboard'),
};

// Files API
export const filesApi = {
  upload: (files) => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });
    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  delete: (userId, filename) => api.delete(`/files/${userId}/${filename}`),
};

export default api;
