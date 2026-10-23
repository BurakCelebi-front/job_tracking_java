import axios, { isAxiosError } from "axios";

// Vite only exposes env vars prefixed with VITE_. Falls back to localhost
// so local development (`pnpm dev`) keeps working untouched.
const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
});

const AUTH_STORAGE_KEYS = ["token", "email", "fullName"] as const;

apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// A 401 on an authenticated request means the token is missing, invalid or expired:
// clear the stored session and send the user back to the login page.
// Login/register are skipped so a wrong password just shows the form error.
apiClient.interceptors.response.use(
    (response) => response,
    (error: unknown) => {
        const url = isAxiosError(error) ? (error.config?.url ?? "") : "";
        const isAuthEndpoint = url.includes("/api/auth/login") || url.includes("/api/auth/register");

        if (isAxiosError(error) && error.response?.status === 401 && !isAuthEndpoint && localStorage.getItem("token")) {
            AUTH_STORAGE_KEYS.forEach((key) => localStorage.removeItem(key));
            if (window.location.pathname !== "/login") {
                window.location.replace("/login");
            }
        }
        return Promise.reject(error);
    },
);

/** Returns the backend's error message (e.g. "An account with this email already exists."), or the fallback. */
export function getErrorMessage(error: unknown, fallback: string): string {
    if (isAxiosError<{ message?: string; errors?: Record<string, string> }>(error)) {
        const data = error.response?.data;
        // Validation errors: show the first field message (e.g. "size must be between 8 and ...").
        const firstFieldError = data?.errors ? Object.values(data.errors)[0] : undefined;
        if (firstFieldError) return firstFieldError;
        if (typeof data?.message === "string" && data.message.length > 0) return data.message;
    }
    return fallback;
}

export default apiClient;
