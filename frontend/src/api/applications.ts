import apiClient from "./client";
import type { Application, ApplicationStatus, StatusHistoryEntry } from "./types";

export interface ApplicationRequest {
  companyId: number;
  positionTitle: string;
  appliedDate: string;
  status: ApplicationStatus;
  note?: string;
}

export const getApplications = async (): Promise<Application[]> => {
  const response = await apiClient.get<Application[]>("/api/applications");
  return response.data;
};

export const createApplication = async (data: ApplicationRequest): Promise<Application> => {
  const response = await apiClient.post<Application>("/api/applications", data);
  return response.data;
};

export const updateApplication = async (id: number, data: ApplicationRequest): Promise<Application> => {
  const response = await apiClient.put<Application>(`/api/applications/${id}`, data);
  return response.data;
};

export const updateApplicationStatus = async (id: number, status: ApplicationStatus): Promise<Application> => {
  const response = await apiClient.patch<Application>(`/api/applications/${id}/status`, { status });
  return response.data;
};

export const deleteApplication = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/applications/${id}`);
};

export const getStatusHistory = async (id: number): Promise<StatusHistoryEntry[]> => {
  const response = await apiClient.get<StatusHistoryEntry[]>(`/api/applications/${id}/status-history`);
  return response.data;
};