import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import { resetPassword } from "../api/auth";
import { LockIcon } from "../components/icons";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");

    if (newPassword !== confirmPassword) {
      setError("Passwords don't match.");
      return;
    }

    setSubmitting(true);
    try {
      await resetPassword({ token, newPassword });
      setDone(true);
      setTimeout(() => navigate("/login"), 2500);
    } catch {
      setError("This reset link is invalid or has expired. Request a new one.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#0c0d11] px-4">
      <div className="pointer-events-none absolute -left-36 -top-52 h-[560px] w-[560px] rounded-full bg-[#ff7a59] opacity-30 blur-[115px]" />
      <div className="pointer-events-none absolute -bottom-56 -right-40 h-[600px] w-[600px] rounded-full bg-[#3355d8] opacity-55 blur-[110px]" />

      <Link to="/" className="absolute left-12 top-9 flex items-center gap-2.5 transition hover:opacity-70">
        <div className="flex h-[30px] w-[30px] items-center justify-center rounded-lg border border-white/15 bg-white/10 font-display text-[15px] font-bold text-white">
          J
        </div>
        <span className="font-display text-base font-semibold text-white/90">Job Tracker</span>
      </Link>

      <div className="relative flex w-[420px] max-w-full flex-col gap-[18px] rounded-[20px] bg-white p-10 shadow-[0_30px_80px_-16px_rgba(0,0,0,0.55),0_8px_24px_rgba(0,0,0,0.22)]">
        {!token ? (
          <div className="flex flex-col gap-1.5">
            <h1 className="font-display text-[27px] font-semibold text-[#171717]">Invalid link</h1>
            <p className="text-sm text-[#6b6560]">
              This reset link is missing its token. Request a new one from the{" "}
              <Link to="/forgot-password" className="font-semibold text-[#3355d8] hover:underline">
                forgot password
              </Link>{" "}
              page.
            </p>
          </div>
        ) : done ? (
          <div className="flex flex-col gap-1.5">
            <h1 className="font-display text-[27px] font-semibold text-[#171717]">Password updated</h1>
            <p className="text-sm text-[#6b6560]">Taking you to sign in…</p>
          </div>
        ) : (
          <>
            <div className="flex flex-col gap-1.5">
              <h1 className="font-display text-[27px] font-semibold text-[#171717]">Set a new password</h1>
              <p className="text-sm text-[#6b6560]">Choose a new password for your account.</p>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-[18px]">
              {error && (
                <p className="rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">{error}</p>
              )}

              <label className="flex flex-col gap-1.5 text-[13px] font-medium text-[#6b6560]">
                New Password
                <div className="relative">
                  <LockIcon className="pointer-events-none absolute left-[14px] top-1/2 -translate-y-1/2 text-[#aca79f]" />
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    minLength={8}
                    placeholder="At least 8 characters"
                    className="h-[46px] w-full rounded-[10px] border border-[#e8e5e0] bg-white pl-[42px] pr-3.5 text-sm text-[#171717] outline-none transition placeholder:text-[#aca79f] focus:border-[#3355d8] focus:ring-[3px] focus:ring-[#eef2fd]"
                  />
                </div>
              </label>

              <label className="flex flex-col gap-1.5 text-[13px] font-medium text-[#6b6560]">
                Confirm Password
                <div className="relative">
                  <LockIcon className="pointer-events-none absolute left-[14px] top-1/2 -translate-y-1/2 text-[#aca79f]" />
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    minLength={8}
                    placeholder="Repeat your new password"
                    className="h-[46px] w-full rounded-[10px] border border-[#e8e5e0] bg-white pl-[42px] pr-3.5 text-sm text-[#171717] outline-none transition placeholder:text-[#aca79f] focus:border-[#3355d8] focus:ring-[3px] focus:ring-[#eef2fd]"
                  />
                </div>
              </label>

              <button
                type="submit"
                disabled={submitting}
                className="h-12 w-full rounded-[10px] bg-[#3355d8] text-[15px] font-semibold text-white shadow-[0_8px_20px_-6px_rgba(51,85,216,0.55)] transition hover:-translate-y-px hover:bg-[#24409e] disabled:opacity-60 disabled:hover:translate-y-0"
              >
                {submitting ? "Saving…" : "Reset Password"}
              </button>
            </form>
          </>
        )}
      </div>

      <div className="absolute bottom-7 text-xs text-white/40">© {new Date().getFullYear()} Job Tracker</div>
    </div>
  );
}