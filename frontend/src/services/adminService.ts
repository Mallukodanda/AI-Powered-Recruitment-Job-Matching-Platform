import { apiClient } from './apiClient';
import {
  AuditLog,
  Company,
  Job,
  PlatformAnalytics,
  PlatformConfig,
  Role,
  UserResponse,
} from '../types';

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminService = {
  // Analytics
  getPlatformAnalytics: async (): Promise<PlatformAnalytics> => {
    const response = await apiClient.get<PlatformAnalytics>('/analytics/platform');
    return response.data;
  },

  // Users
  getUsers: async (page = 0, size = 15, role?: Role): Promise<PagedResponse<UserResponse>> => {
    const params: Record<string, any> = { page, size };
    if (role) params.role = role;
    const response = await apiClient.get<PagedResponse<UserResponse>>('/admin/users', { params });
    return response.data;
  },

  updateUserRole: async (userId: number, role: Role): Promise<UserResponse> => {
    const response = await apiClient.patch<UserResponse>(`/admin/users/${userId}/role`, { role });
    return response.data;
  },

  updateUserStatus: async (userId: number, active: boolean): Promise<UserResponse> => {
    const response = await apiClient.patch<UserResponse>(`/admin/users/${userId}/status`, { active });
    return response.data;
  },

  deleteUser: async (userId: number): Promise<void> => {
    await apiClient.delete(`/admin/users/${userId}`);
  },

  // Companies
  getCompanies: async (page = 0, size = 15): Promise<PagedResponse<Company>> => {
    const response = await apiClient.get<PagedResponse<Company>>('/companies', {
      params: { page, size },
    });
    return response.data;
  },

  createCompany: async (company: Partial<Company>): Promise<Company> => {
    const response = await apiClient.post<Company>('/companies', company);
    return response.data;
  },

  verifyCompany: async (id: number, verified: boolean): Promise<Company> => {
    const response = await apiClient.patch<Company>(`/companies/${id}/verify`, { verified });
    return response.data;
  },

  // Jobs
  getJobs: async (page = 0, size = 15): Promise<PagedResponse<Job>> => {
    const response = await apiClient.get<PagedResponse<Job>>('/admin/jobs', {
      params: { page, size },
    });
    return response.data;
  },

  updateJobStatus: async (jobId: number, status: string): Promise<Job> => {
    const response = await apiClient.patch<Job>(`/admin/jobs/${jobId}/status`, { status });
    return response.data;
  },

  // Audit Logs
  getAuditLogs: async (
    page = 0,
    size = 20,
    actor?: string,
    action?: string,
    resourceType?: string
  ): Promise<PagedResponse<AuditLog>> => {
    const params: Record<string, any> = { page, size };
    if (actor) params.actor = actor;
    if (action) params.action = action;
    if (resourceType) params.resourceType = resourceType;
    const response = await apiClient.get<PagedResponse<AuditLog>>('/admin/audit-logs', { params });
    return response.data;
  },

  // Platform Configs
  getConfigs: async (): Promise<PlatformConfig[]> => {
    const response = await apiClient.get<PlatformConfig[]>('/admin/configs');
    return response.data;
  },

  updateConfig: async (key: string, value: string): Promise<PlatformConfig> => {
    const response = await apiClient.patch<PlatformConfig>(`/admin/configs/${key}`, { value });
    return response.data;
  },
};
