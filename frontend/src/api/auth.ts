import apiClient from "./client";

export interface LoginRequest {
    email: string;
    password: string;
}
export interface RegisterRequest {
  fullname: string;
  email: string;
  password: string;
}

export interface AuthResponse {
    token: string;
}


export const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/api/auth/login", data);
    return response.data;
}

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>("/api/auth/register", data);
  return response.data;
};


interface CurrentUserResponse {
  id: number;
  fullName: string;
  email: string;
}

export interface CurrentUser {
  fullName: string;
  email: string;
}

export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUserResponse>("/api/auth/me");
  return { fullName: response.data.fullName, email: response.data.email };
}

export async function deleteAccount(): Promise<void> {
  await apiClient.delete("/api/auth/me");
}

export interface ForgotPasswordRequest {
  email: string;
}

export const forgotPassword = async (data: ForgotPasswordRequest): Promise<void> => {
  await apiClient.post("/api/auth/forgot-password", data);
};

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export const resetPassword = async (data: ResetPasswordRequest): Promise<void> => {
  await apiClient.post("/api/auth/reset-password", data);
};