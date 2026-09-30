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


// Shape your backend's UserResponse actually serializes to — note "fullname"
// (lowercase n), matching that DTO's field name.
interface CurrentUserResponse {
  id: number;
  fullname: string;
  email: string;
}
 
export interface CurrentUser {
  fullName: string;
  email: string;
}
 
export async function getCurrentUser(): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUserResponse>("/api/auth/me");
  return { fullName: response.data.fullname, email: response.data.email };
}
 