import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import AppShell from "../components/AppShell";
import { getApplications } from "../api/applications";
import { getCompanies } from "../api/companies";
import { ArrowRightIcon, CheckIcon } from "../components/icons";
import PipelineBackground from "../components/PipelineBackground";

// This is the site's home page ("/"). Logged out, it's a public marketing
// page. Logged in, it becomes a "getting started" guide. Content here is
// deliberately in English (unlike the rest of the app) — the home page is
// the part a wider/English-speaking audience will see first.
export default function Dashboard() {
  const { token, email } = useAuth();

  if (token) {
    return <AuthenticatedHome email={email} />;
  }
  return <PublicHome />;
}

const FEATURES = [
  {
    title: "Save your companies",
    description: "Keep a record of every company you're applying to, with location and website.",
  },
  {
    title: "Log applications",
    description: "Track the position, applied date, and notes for every application you submit.",
  },
  {
    title: "Follow the status",
    description: "Move each application through Applied, Interview, Offer, or Rejected as it progresses.",
  },
];

const PIPELINE = [
  {
    label: "Applied",
    description: "You submit your application and log it in Job Tracker.",
    classes: "bg-status-applied-bg text-status-applied",
  },
  {
    label: "Interview",
    description: "The company reaches out and the process moves forward.",
    classes: "bg-status-interview-bg text-status-interview",
  },
  {
    label: "Offer or Rejected",
    description: "You reach a final decision — either way, it's recorded.",
    classes: "bg-status-offer-bg text-status-offer",
  },
];

function PublicHome() {
  return (
    <div className="flex min-h-screen flex-col bg-paper text-ink">
      <header className="flex h-16 flex-shrink-0 items-center justify-between border-b border-line bg-surface px-6 sm:px-10">
        <Link to="/" className="flex items-center gap-2.5 transition hover:opacity-70">
          <div className="flex h-7 w-7 items-center justify-center rounded-md bg-brand font-display text-sm font-bold text-white">
            J
          </div>
          <span className="font-display text-base font-semibold text-ink">Job Tracker</span>
        </Link>
        <div className="flex items-center gap-5">
          <Link to="/login" className="text-sm font-semibold text-ink-soft hover:text-ink">
            Sign In
          </Link>
          <Link
            to="/register"
            className="rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-hover"
          >
            Sign Up
          </Link>
        </div>
      </header>

      <main className="flex flex-1 flex-col items-center px-6 py-20 text-center sm:px-10">
        <div className="relative flex w-full flex-col items-center pb-44 sm:pb-56">
          <h1 className="max-w-2xl font-display text-4xl font-semibold leading-tight text-ink sm:text-5xl">
            Track every job application in one place
          </h1>
          <p className="mt-5 max-w-xl text-base text-ink-soft">
            Job Tracker keeps your companies, applications, and interview stages organized — so you always
            know what to follow up on next.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/register"
              className="rounded-lg bg-brand px-6 py-3 text-sm font-semibold text-white shadow-elevate-sm hover:bg-brand-hover"
            >
              Get Started — It's Free
            </Link>
            <Link
              to="/login"
              className="rounded-lg border border-line px-6 py-3 text-sm font-semibold text-ink hover:bg-surface"
            >
              Sign In
            </Link>
          </div>

          {/* the user -> company -> offer/rejected story, centered at the bottom of the hero */}
          <PipelineBackground />
        </div>

        <div className="mt-6 grid w-full max-w-4xl grid-cols-1 gap-5 text-left sm:grid-cols-3">
          {FEATURES.map((f, i) => (
            <div key={f.title} className="rounded-xl border border-line bg-surface p-6 shadow-elevate-sm">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-tint font-display text-sm font-semibold text-brand">
                {i + 1}
              </div>
              <h3 className="mt-4 text-[15px] font-semibold text-ink">{f.title}</h3>
              <p className="mt-1.5 text-sm text-ink-soft">{f.description}</p>
            </div>
          ))}
        </div>

        {/* Why Job Tracker — problem/solution section */}
        <div className="mt-28 grid w-full max-w-4xl grid-cols-1 items-center gap-12 text-left lg:grid-cols-2">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wide text-brand">Why Job Tracker</span>
            <h2 className="mt-3 font-display text-3xl font-semibold leading-snug text-ink">
              Job hunting means juggling dozens of applications at once
            </h2>
            <p className="mt-4 text-sm leading-relaxed text-ink-soft">
              A spreadsheet works for the first five applications. After that, it's hard to remember which
              company you applied to last week, what stage each one is at, or which one you were supposed to
              follow up on. Job Tracker replaces the scattered notes and forgotten tabs with one simple,
              organized view.
            </p>
            <ul className="mt-6 flex flex-col gap-3 text-sm text-ink-soft">
              <li className="flex items-start gap-2.5">
                <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-status-offer-bg text-status-offer">
                  <CheckIcon className="h-3 w-3" />
                </span>
                Never lose track of a company or a position you applied to.
              </li>
              <li className="flex items-start gap-2.5">
                <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-status-offer-bg text-status-offer">
                  <CheckIcon className="h-3 w-3" />
                </span>
                See exactly where every application stands, at a glance.
              </li>
              <li className="flex items-start gap-2.5">
                <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-status-offer-bg text-status-offer">
                  <CheckIcon className="h-3 w-3" />
                </span>
                Keep notes and interview history attached to each application.
              </li>
            </ul>
          </div>

          <div className="rounded-2xl border border-line bg-surface p-7 shadow-elevate-sm">
            <p className="text-xs font-semibold uppercase tracking-wide text-ink-soft">Every application follows a path</p>
            <div className="mt-5 flex flex-col gap-4">
              {PIPELINE.map((stage, i) => (
                <div key={stage.label} className="flex items-center gap-3">
                  <span
                    className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full font-display text-xs font-semibold ${stage.classes}`}
                  >
                    {i + 1}
                  </span>
                  <div className="flex-1">
                    <p className="text-sm font-semibold text-ink">{stage.label}</p>
                    <p className="text-xs text-ink-soft">{stage.description}</p>
                  </div>
                  {i < PIPELINE.length - 1 && <span className="text-ink-soft/40">↓</span>}
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>

      <footer className="border-t border-line px-6 py-6 text-center text-xs text-ink-soft sm:px-10">
        © {new Date().getFullYear()} Job Tracker
      </footer>
    </div>
  );
}

function AuthenticatedHome({ email }: { email: string | null }) {
  const [companyCount, setCompanyCount] = useState<number | null>(null);
  const [applicationCount, setApplicationCount] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getCompanies(), getApplications()])
      .then(([companies, applications]) => {
        if (cancelled) return;
        setCompanyCount(companies.length);
        setApplicationCount(applications.length);
      })
      .catch(() => {
        if (!cancelled) {
          setCompanyCount(0);
          setApplicationCount(0);
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const hasCompany = (companyCount ?? 0) > 0;
  const hasApplication = (applicationCount ?? 0) > 0;

  const steps = [
    {
      title: "Add a company",
      description: "Save the companies you're applying to so you can link applications to them.",
      href: "/companies",
      cta: "Go to Companies",
      done: hasCompany,
    },
    {
      title: "Log an application",
      description: "Record a job application with the position, applied date, and any notes.",
      href: "/applications/new",
      cta: "Add an Application",
      done: hasApplication,
    },
    {
      title: "Track its status",
      description: "Move applications through Applied, Interview, Offer, or Rejected as things progress.",
      href: "/applications",
      cta: "View Applications",
      done: hasApplication,
    },
  ];

  return (
    <AppShell>
      <div className="mx-auto max-w-3xl">
        <div className="relative pb-36 sm:pb-44">
          <h1 className="font-display text-2xl font-semibold text-ink">
            Welcome back{email ? `, ${email}` : ""}
          </h1>
          <p className="mt-1.5 text-sm text-ink-soft">Here's how to get the most out of Job Tracker.</p>

          {/* the user -> company -> offer/rejected story */}
          <PipelineBackground />
        </div>

        <div className="mt-6 flex flex-col gap-4">
          {steps.map((step, i) => (
            <div
              key={step.title}
              className="flex flex-col items-start gap-4 rounded-xl border border-line bg-surface p-5 shadow-elevate-sm sm:flex-row sm:items-center"
            >
              <div
                className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full font-display text-sm font-semibold ${
                  step.done ? "bg-status-offer-bg text-status-offer" : "bg-brand-tint text-brand"
                }`}
              >
                {step.done ? <CheckIcon /> : i + 1}
              </div>
              <div className="flex-1">
                <h2 className="text-[15px] font-semibold text-ink">{step.title}</h2>
                <p className="mt-1 text-sm text-ink-soft">{step.description}</p>
              </div>
              <Link
                to={step.href}
                className="inline-flex flex-shrink-0 items-center gap-1 text-sm font-semibold text-brand hover:text-brand-hover"
              >
                {step.cta}
                <ArrowRightIcon />
              </Link>
            </div>
          ))}
        </div>
      </div>
    </AppShell>
  );
}