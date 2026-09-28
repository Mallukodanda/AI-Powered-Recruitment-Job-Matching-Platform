import { apiClient } from './apiClient';
import { DashboardStats } from '../types';

export const analyticsService = {
  async getDashboardStats(): Promise<DashboardStats> {
    const response = await apiClient.get<DashboardStats>('/analytics/dashboard');
    return response.data;
  },
};
