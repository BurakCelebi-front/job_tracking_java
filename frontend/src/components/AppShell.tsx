import type { ReactNode } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { LogOutIcon, MoonIcon, SunIcon } from "./icons";

interface AppShellProps {
  children: ReactNode;
}

const navItems = [
  { to: "/applications", label: "Applications" },
  { to: "/companies", label: "Companies" },
];

// Shared layout for every authenticated screen: top nav, theme toggle, logout.
// Wrap each page's content with <AppShell>...</AppShell>.
export default function AppShell({ children }: AppShellProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout, email, fullName } = useAuth();
  const { theme, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="flex min-h-screen flex-col bg-paper text-ink">
      <header className="flex h-16 flex-shrink-0 items-center justify-between border-b border-line bg-surface px-8">
        <div className="flex items-center gap-9">
          <Link to="/" className="flex items-center gap-2.5 transition hover:opacity-70">
            <div className="flex h-7 w-7 items-center justify-center rounded-md bg-brand font-display text-sm font-bold text-white">
              J
            </div>
            <span className="font-display text-base font-semibold">Job Tracker</span>
          </Link>
          <nav className="flex gap-1.5">
            {navItems.map((item) => {
              const active = location.pathname.startsWith(item.to);
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={
                    active
                      ? "flex h-9 items-center rounded-lg bg-brand-tint px-3.5 text-sm font-semibold text-brand"
                      : "flex h-9 items-center rounded-lg px-3.5 text-sm font-medium text-ink-soft hover:text-ink"
                  }
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>
        <div className="flex items-center gap-3.5">
          <button
            onClick={toggleTheme}
            aria-label="Toggle theme"
            className="flex h-9 w-9 items-center justify-center rounded-lg border border-line bg-surface text-ink-soft hover:bg-paper"
          >
            {theme === "dark" ? <SunIcon /> : <MoonIcon />}
          </button>
          {email && (
            <>
              <div className="hidden flex-col items-end leading-tight sm:flex">
                <span className="text-sm font-semibold text-ink">{fullName ?? email}</span>
                {fullName && <span className="text-xs text-ink-soft">{email}</span>}
              </div>
              <span className="hidden h-8 w-px bg-line sm:inline" />
            </>
          )}
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 text-sm font-semibold text-ink-soft hover:text-ink"
          >
            <LogOutIcon />
            Log Out
          </button>
        </div>
      </header>
      <main className="flex-1 overflow-y-auto p-8">{children}</main>
    </div>
  );
}