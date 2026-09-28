import { apiClient } from './apiClient';
import { Job } from '../types';

export const jobService = {
  async getJobs(search?: string): Promise<Job[]> {
    const response = await apiClient.get<Job[]>('/jobs', {
      params: search && search.trim() ? { search: search.trim() } : undefined,
    });
    return response.data;
  },

  async getJobById(id: number): Promise<Job> {
    const response = await apiClient.get<Job>(`/jobs/${id}`);
    return response.data;
  },

  async createJob(job: Partial<Job>): Promise<Job> {
    const response = await apiClient.post<Job>('/jobs', job);
    return response.data;
  },

  async updateJob(id: number, job: Partial<Job>): Promise<Job> {
    const response = await apiClient.put<Job>(`/jobs/${id}`, job);
    return response.data;
  },

  async deleteJob(id: number): Promise<void> {
    await apiClient.delete(`/jobs/${id}`);
  },
};
