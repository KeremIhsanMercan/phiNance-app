import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-hot-toast';
import { useAuthStore } from '../stores/authStore';
import { authApi } from '../services/api';
import {
  UserCircleIcon,
  KeyIcon,
  EyeIcon,
  EyeSlashIcon,
} from '@heroicons/react/24/outline';

const currencies = ['USD', 'EUR', 'TRY', 'GBP', 'JPY', 'CAD', 'AUD'];

export default function Settings() {
  const { user, logout } = useAuthStore();
  const [activeTab, setActiveTab] = useState('profile');
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);

  const tabs = [
    { id: 'profile', label: 'Profile', icon: UserCircleIcon },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Settings</h1>

      <div className="flex gap-6">

        {/* Content */}
        <div className="flex-1 space-y-6">
          <ProfileSettings user={user} />
          <SecuritySettings
            showCurrentPassword={showCurrentPassword}
            setShowCurrentPassword={setShowCurrentPassword}
            showNewPassword={showNewPassword}
            setShowNewPassword={setShowNewPassword}
            logout={logout}
          />
        </div>
      </div>
    </div>
  );
}

function ProfileSettings({ user }) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      email: user?.email || '',
      preferredCurrency: user?.preferredCurrency || 'USD',
    },
  });

  const onSubmit = async (data) => {
    try {
      const res = await authApi.updateProfile(data);
      // Update local auth store with returned user info if available
      if (res?.data?.user) {
        useAuthStore.getState().updateUser(res.data.user);
        // reset form values to the updated user
        reset({
          firstName: res.data.user.firstName,
          lastName: res.data.user.lastName,
          email: res.data.user.email,
          preferredCurrency: res.data.user.preferredCurrency,
        });
      } else {
        // fallback: merge updated fields
        useAuthStore.getState().updateUser({
          firstName: data.firstName,
          lastName: data.lastName,
          preferredCurrency: data.preferredCurrency,
        });
        reset({
          firstName: data.firstName,
          lastName: data.lastName,
          email: user?.email || '',
          preferredCurrency: data.preferredCurrency,
        });
      }
      toast.success('Profile updated successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    }
  };

  return (
    <div className="card">
      <h2 className="text-lg font-semibold text-gray-900 mb-6">Profile Information</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 max-w-lg">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              First Name
            </label>
            <input
              {...register('firstName', { required: 'First name is required' })}
              className="input-field"
            />
            {errors.firstName && (
              <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Last Name
            </label>
            <input
              {...register('lastName', { required: 'Last name is required' })}
              className="input-field"
            />
            {errors.lastName && (
              <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>
            )}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            {...register('email')}
            className="input-field bg-gray-50"
            disabled
          />
          <p className="mt-1 text-xs text-gray-500">Email cannot be changed</p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Preferred Currency
          </label>
          <select {...register('preferredCurrency')} className="input-field">
            {currencies.map((currency) => (
              <option key={currency} value={currency}>
                {currency}
              </option>
            ))}
          </select>
        </div>

        <button type="submit" className="btn-primary">
          Save Changes
        </button>
      </form>
    </div>
  );
}

function SecuritySettings({
  showCurrentPassword,
  setShowCurrentPassword,
  showNewPassword,
  setShowNewPassword,
  logout,
}) {
  const { register, handleSubmit, reset, watch, formState: { errors } } = useForm();
  const newPassword = watch('newPassword');
  const { register: delRegister, handleSubmit: handleDeleteSubmit, formState: { errors: delErrors }, reset: resetDelete } = useForm();
  const [showDeletePassword, setShowDeletePassword] = useState(false);

  const onSubmit = async (data) => {
    try {
      await authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      toast.success('Password changed successfully');
      reset();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to change password');
    }
  };

  const onDelete = async (data) => {
    try {
      await authApi.deleteAccount({ password: data.password });
      toast.success('Account deleted');
      // logout and redirect to login
      logout();
      window.location.href = '/login';
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete account');
    } finally {
      resetDelete();
    }
  };

  return (
    <div className="space-y-6">
      <div className="card">
        <h2 className="text-lg font-semibold text-gray-900 mb-6">Change Password</h2>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 max-w-lg">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Current Password
            </label>
            <div className="relative">
              <input
                type={showCurrentPassword ? 'text' : 'password'}
                {...register('currentPassword', { required: 'Current password is required' })}
                className="input-field pr-10"
              />
              <button
                type="button"
                onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showCurrentPassword ? (
                  <EyeSlashIcon className="h-5 w-5" />
                ) : (
                  <EyeIcon className="h-5 w-5" />
                )}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="mt-1 text-sm text-red-600">{errors.currentPassword.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              New Password
            </label>
            <div className="relative">
              <input
                type={showNewPassword ? 'text' : 'password'}
                {...register('newPassword', {
                  required: 'New password is required',
                  minLength: { value: 8, message: 'Password must be at least 8 characters' },
                })}
                className="input-field pr-10"
              />
              <button
                type="button"
                onClick={() => setShowNewPassword(!showNewPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showNewPassword ? (
                  <EyeSlashIcon className="h-5 w-5" />
                ) : (
                  <EyeIcon className="h-5 w-5" />
                )}
              </button>
            </div>
            {errors.newPassword && (
              <p className="mt-1 text-sm text-red-600">{errors.newPassword.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Confirm New Password
            </label>
            <input
              type="password"
              {...register('confirmPassword', {
                required: 'Please confirm your password',
                validate: (value) => value === newPassword || 'Passwords do not match',
              })}
              className="input-field"
            />
            {errors.confirmPassword && (
              <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
            )}
          </div>

          <button type="submit" className="btn-primary">
            Update Password
          </button>
        </form>
      </div>

      <div className="card border-red-200">
        <h2 className="text-lg font-semibold text-red-600 mb-4">Delete Account</h2>
        <p className="text-gray-600 mb-4">This will deactivate your account. Your data will be retained but your account will be disabled.</p>
        <form onSubmit={handleDeleteSubmit(onDelete)} className="max-w-lg space-y-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
            <div className="relative">
              <input
                type={showDeletePassword ? 'text' : 'password'}
                {...delRegister('password', { required: 'Password is required' })}
                className="input-field pr-10"
              />
              <button
                type="button"
                onClick={() => setShowDeletePassword(!showDeletePassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showDeletePassword ? <EyeSlashIcon className="h-5 w-5" /> : <EyeIcon className="h-5 w-5" />}
              </button>
            </div>
            {delErrors.password && (
              <p className="mt-1 text-sm text-red-600">{delErrors.password.message}</p>
            )}
          </div>

          <div className="flex gap-3">
            <button type="submit" className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">Delete Account</button>
            <button type="button" onClick={() => resetDelete()} className="px-4 py-2 bg-gray-100 rounded-lg">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
 
