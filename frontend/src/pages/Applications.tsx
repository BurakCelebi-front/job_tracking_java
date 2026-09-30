import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { PlusIcon, SearchIcon, ArrowRightIcon } from "../components/icons";
import { getApplications } from "../api/applications";
import type { Application, ApplicationStatus } from "../api/types";
import { STATUS_LABELS, STATUS_OPTIONS, statusBadgeClasses } from "../lib/status";

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" });
}

export default function Applications() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<ApplicationStatus | "">("");

  useEffect(() => {
    let cancelled = false;
    getApplications()
      .then((data) => {
        if (!cancelled) setApplications(data);
      })
      .catch(() => {
        if (!cancelled) setError("Couldn't load applications.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // Stats are always based on the FULL list — search/filter don't affect them.
  const stats = useMemo(
    () => ({
      total: applications.length,
      interview: applications.filter((a) => a.status === "INTERVIEW").length,
      offer: applications.filter((a) => a.status === "OFFER").length,
    }),
    [applications]
  );

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    return applications.filter((a) => {
      const matchesTerm =
        !term ||
        (a.companyName ?? "").toLowerCase().includes(term) ||
        a.positionTitle.toLowerCase().includes(term);
      const matchesStatus = !statusFilter || a.status === statusFilter;
      return matchesTerm && matchesStatus;
    });
  }, [applications, search, statusFilter]);

  return (
    <AppShell>
      <div className="flex flex-col gap-5">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-display text-xl font-semibold text-ink">My Applications</h1>
            <p className="mt-1 text-sm text-ink-soft">{applications.length} applications</p>
          </div>
          <Link
            to="/applications/new"
            className="flex h-10 items-center gap-2 rounded-lg bg-brand px-4.5 text-sm font-semibold text-white shadow-elevate-sm hover:bg-brand-hover"
          >
            <PlusIcon />
            New Application
          </Link>
        </div>

        <div className="flex gap-3.5">
          <div className="flex flex-1 flex-col gap-1 rounded-xl border border-line bg-surface p-4.5 shadow-elevate-sm">
            <span className="text-xs font-medium text-ink-soft">Total Applications</span>
            <span className="font-display text-2xl font-semibold text-ink">{stats.total}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 rounded-xl border border-line bg-surface p-4.5 shadow-elevate-sm">
            <span className="text-xs font-medium text-ink-soft">In Interview</span>
            <span className="font-display text-2xl font-semibold text-status-interview">{stats.interview}</span>
          </div>
          <div className="flex flex-1 flex-col gap-1 rounded-xl border border-line bg-surface p-4.5 shadow-elevate-sm">
            <span className="text-xs font-medium text-ink-soft">Offers Received</span>
            <span className="font-display text-2xl font-semibold text-status-offer">{stats.offer}</span>
          </div>
        </div>

        <div className="flex gap-3">
          <div className="relative flex-1">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink-soft" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by company or position"
              className="h-10 w-full rounded-lg border border-line bg-surface pl-9 pr-3.5 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand-tint"
            />
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as ApplicationStatus | "")}
            className="h-10 rounded-lg border border-line bg-surface px-3.5 text-sm text-ink-soft outline-none focus:border-brand"
          >
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {STATUS_LABELS[s]}
              </option>
            ))}
          </select>
        </div>

        <div className="overflow-hidden rounded-xl border border-line bg-surface shadow-elevate-sm">
          <div className="grid grid-cols-[2fr_2fr_1fr_1fr_110px] px-5 py-3 text-[11px] font-semibold uppercase tracking-wide text-ink-soft bg-line-soft">
            <div>Company</div>
            <div>Position</div>
            <div>Status</div>
            <div>Date</div>
            <div />
          </div>

          {loading ? (
            <p className="px-5 py-6 text-sm text-ink-soft">Loading…</p>
          ) : error ? (
            <p className="px-5 py-6 text-sm text-status-rejected">{error}</p>
          ) : filtered.length === 0 ? (
            <p className="px-5 py-6 text-sm text-ink-soft">
              {applications.length === 0 ? "You don't have any applications yet." : "No applications match your search."}
            </p>
          ) : (
            filtered.map((app) => (
              <div
                key={app.id}
                className="grid grid-cols-[2fr_2fr_1fr_1fr_110px] items-center border-t border-line px-5 py-4 hover:bg-paper"
              >
                <div className="text-sm font-semibold text-ink">{app.companyName}</div>
                <div className="text-sm text-ink-soft">{app.positionTitle}</div>
                <div>
                  <span
                    className={`inline-block rounded-full px-2.5 py-1 text-xs font-semibold ${statusBadgeClasses(
                      app.status
                    )}`}
                  >
                    {STATUS_LABELS[app.status]}
                  </span>
                </div>
                <div className="text-[13px] text-ink-soft">{formatDate(app.appliedDate)}</div>
                <div>
                  <Link
                    to={`/applications/${app.id}`}
                    className="inline-flex items-center gap-1 text-[13px] font-semibold text-brand hover:text-brand-hover"
                  >
                    View
                    <ArrowRightIcon />
                  </Link>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </AppShell>
  );
}