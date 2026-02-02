import { useAuthStore } from '../stores/authStore';

/**
 * Formats a number as currency using the user's preferred currency
 * @param {number} amount - The amount to format
 * @returns {string} Formatted currency string
 */
export const formatCurrency = (amount) => {
  const user = useAuthStore.getState().user;
  const currency = user?.preferredCurrency || 'USD';
  
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
  }).format(amount || 0);
};

/**
 * Hook to get currency formatter function
 * Updates when user changes their preferred currency
 */
export const useCurrencyFormatter = () => {
  const { user } = useAuthStore();
  
  return (amount) => {
    const currency = user?.preferredCurrency || 'USD';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount || 0);
  };
};
