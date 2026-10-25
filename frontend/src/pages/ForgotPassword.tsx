import { useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../api/auth";
import { MailIcon } from "../components/icons";

// Same visual shell as Login/Register — dark bg, white card, independent of
// the theme toggle.

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await forgotPassword({ email });
    } catch {
      // Swallowed on purpose — the backend never reveals whether an email
      // is registered, so this screen always shows the same outcome.
    } finally {
      setSubmitting(false);
      setSent(true);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#0c0d11] px-4">
      <div className="pointer-events-none absolute -right-40 -top-56 h-[620px] w-[620px] rounded-full bg-[#3355d8] opacity-55 blur-[110px]" />
      <div className="pointer-events-none absolute -bottom-52 -left-36 h-[520px] w-[520px] rounded-full bg-[#ff7a59] opacity-30 blur-[120px]" />

      <Link to="/" className="absolute left-12 top-9 flex items-center gap-2.5 transition hover:opacity-70">
        <div className="flex h-[30px] w-[30px] items-center justify-center rounded-lg border border-white/15 bg-white/10 font-display text-[15px] font-bold text-white">
          J
        </div>
        <span className="font-display text-base font-semibold text-white/90">Job Tracker</span>
      </Link>

      <div className="relative flex w-[420px] max-w-full flex-col gap-[22px] rounded-[20px] bg-white px-10 py-11 shadow-[0_30px_80px_-16px_rgba(0,0,0,0.55),0_8px_24px_rgba(0,0,0,0.22)]">
        {sent ? (
          <>
            <div className="flex flex-col gap-1.5">
              <h1 className="font-display text-[27px] font-semibold text-[#171717]">Check your email</h1>
              <p className="text-sm text-[#6b6560]">
                If an account exists for <span className="font-medium text-[#171717]">{email}</span>, we've
                sent a link to reset your password. It's valid for 1 hour.
              </p>
            </div>
            <Link
              to="/login"
              className="mt-1 flex h-[50px] w-full items-center justify-center rounded-[10px] bg-[#3355d8] text-[15px] font-semibold text-white shadow-[0_8px_20px_-6px_rgba(51,85,216,0.55)] transition hover:-translate-y-px hover:bg-[#24409e]"
            >
              Back to Sign In
            </Link>
          </>
        ) : (
          <>
            <div className="flex flex-col gap-1.5">
              <h1 className="font-display text-[27px] font-semibold text-[#171717]">Forgot password?</h1>
              <p className="text-sm text-[#6b6560]">Enter your email and we'll send you a link to reset it.</p>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-[18px]">
              <label className="flex flex-col gap-1.5 text-[13px] font-medium text-[#6b6560]">
                Email
                <div className="relative">
                  <MailIcon className="pointer-events-none absolute left-[14px] top-1/2 -translate-y-1/2 text-[#aca79f]" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    placeholder="you@company.com"
                    className="h-12 w-full rounded-[10px] border border-[#e8e5e0] bg-white pl-[42px] pr-3.5 text-sm text-[#171717] outline-none transition placeholder:text-[#aca79f] focus:border-[#3355d8] focus:ring-[3px] focus:ring-[#eef2fd]"
                  />
                </div>
              </label>

              <button
                type="submit"
                disabled={submitting}
                className="h-[50px] w-full rounded-[10px] bg-[#3355d8] text-[15px] font-semibold text-white shadow-[0_8px_20px_-6px_rgba(51,85,216,0.55)] transition hover:-translate-y-px hover:bg-[#24409e] disabled:opacity-60 disabled:hover:translate-y-0"
              >
                {submitting ? "Sending…" : "Send Reset Link"}
              </button>
            </form>

            <div className="border-t border-[#e8e5e0] pt-4 text-center text-[13px] text-[#6b6560]">
              Remembered it?{" "}
              <Link to="/login" className="font-semibold text-[#3355d8] hover:text-[#24409e] hover:underline">
                Sign in
              </Link>
            </div>
          </>
        )}
      </div>

      <div className="absolute bottom-7 text-xs text-white/40">© {new Date().getFullYear()} Job Tracker</div>
    </div>
  );
}