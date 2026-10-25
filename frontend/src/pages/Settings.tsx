import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import { useAuth } from "../context/AuthContext";
import { deleteAccount } from "../api/auth";

export default function Settings() {
  const { email, fullName, logout } = useAuth();
  const navigate = useNavigate();
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState("");

  const handleDelete = async () => {
    setDeleting(true);
    setError("");
    try {
      await deleteAccount();
      logout();
      navigate("/");
    } catch {
      setError("Something went wrong while deleting your account. Please try again.");
      setDeleting(false);
    }
  };

  return (
    <AppShell>
      <div className="mx-auto max-w-2xl">
        <h1 className="font-display text-2xl font-semibold text-ink">Settings</h1>

        <div className="mt-6 rounded-xl border border-line bg-surface p-5 shadow-elevate-sm">
          <h2 className="text-[15px] font-semibold text-ink">Account</h2>
          <dl className="mt-3 flex flex-col gap-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-ink-soft">Name</dt>
              <dd className="font-medium text-ink">{fullName ?? "—"}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-ink-soft">Email</dt>
              <dd className="font-medium text-ink">{email}</dd>
            </div>
          </dl>
        </div>

        <div className="mt-6 rounded-xl border border-status-rejected/30 bg-status-rejected-bg p-5">
          <h2 className="text-[15px] font-semibold text-status-rejected">Danger zone</h2>
          <p className="mt-1.5 text-sm text-ink-soft">
            Deleting your account permanently removes your companies, applications, and their status
            history. This cannot be undone.
          </p>

          {error && <p className="mt-3 text-sm text-status-rejected">{error}</p>}

          {!confirming ? (
            <button
              type="button"
              onClick={() => setConfirming(true)}
              className="mt-4 rounded-lg border border-status-rejected bg-surface px-4 py-2 text-sm font-semibold text-status-rejected hover:bg-status-rejected-bg"
            >
              Delete account
            </button>
          ) : (
            <div className="mt-4 flex flex-wrap items-center gap-3">
              <span className="text-sm font-medium text-ink">Are you sure? This can't be undone.</span>
              <button
                type="button"
                onClick={handleDelete}
                disabled={deleting}
                className="rounded-lg bg-status-rejected px-4 py-2 text-sm font-semibold text-white hover:bg-status-rejected/90 disabled:opacity-60"
              >
                {deleting ? "Deleting…" : "Yes, delete my account"}
              </button>
              <button
                type="button"
                onClick={() => setConfirming(false)}
                disabled={deleting}
                className="rounded-lg border border-line px-4 py-2 text-sm font-semibold text-ink-soft hover:bg-paper"
              >
                Cancel
              </button>
            </div>
          )}
        </div>
      </div>
    </AppShell>
  );
}