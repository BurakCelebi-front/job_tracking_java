import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { getCurrentUser } from "../api/auth";

interface AuthContextType {
  token: string | null;
  email: string | null;
  fullName: string | null;
  login: (token: string, email: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(localStorage.getItem("token"));
  const [email, setEmailState] = useState<string | null>(localStorage.getItem("email"));
  const [fullName, setFullNameState] = useState<string | null>(localStorage.getItem("fullName"));

  const login = (newToken: string, newEmail: string) => {
    localStorage.setItem("token", newToken);
    localStorage.setItem("email", newEmail);
    localStorage.removeItem("fullName");
    setTokenState(newToken);
    setEmailState(newEmail);
    setFullNameState(null);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    localStorage.removeItem("fullName");
    setTokenState(null);
    setEmailState(null);
    setFullNameState(null);
  };

  // Whenever there's a token but no fullName cached yet — right after a fresh
  // login/register, or an existing session from before we started tracking
  // fullName — fetch it once from /api/auth/me and cache it.
  useEffect(() => {
    if (!token || fullName) return;
    let cancelled = false;

    getCurrentUser()
      .then((user) => {
        if (cancelled) return;
        localStorage.setItem("fullName", user.fullName);
        setFullNameState(user.fullName);
      })
      .catch(() => {
        // token expired/invalid, or /me not reachable — fall back to email in the UI
      });

    return () => {
      cancelled = true;
    };
  }, [token, fullName]);

  return (
    <AuthContext.Provider value={{ token, email, fullName, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}