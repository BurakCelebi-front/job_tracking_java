import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";

interface AuthContextType {
  token: string | null;
  email: string | null;
  login: (token: string, email: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(localStorage.getItem("token"));
  const [email, setEmailState] = useState<string | null>(localStorage.getItem("email"));

  const login = (newToken: string, newEmail: string) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("email", newEmail);
    setTokenState(newToken);
    setEmailState(newEmail);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    setTokenState(null);
    setEmailState(null);
  };

  return (
    <AuthContext.Provider value={{ token, email, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}