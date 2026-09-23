import apiClient from "./client";
import type { Company } from "../types";

export interface CompanyRequest {
  name: string;
  website: string;
  location: string;
}

export const getCompanies = async (): Promise<Company[]> => {
  const response = await apiClient.get<Company[]>("/api/companies");
  return response.data;
};

export const createCompany = async (data: CompanyRequest): Promise<Company> => {
  const response = await apiClient.post<Company>("/api/companies", data);
  return response.data;
};

export const deleteCompany = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/companies/${id}`);
};