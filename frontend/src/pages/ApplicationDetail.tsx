import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import { ArrowLeftIcon, PencilIcon, TrashIcon } from "../components/icons";
import {
  deleteApplication,
  getApplicationById,
  getStatusHistory,
  updateApplicationStatus,
} from "../api/applications";
import type { Application, ApplicationStatus, StatusHistoryEntry } from "../api/types";
import { STATUS_LABELS, STATUS_OPTIONS, statusBadgeClasses, statusDotClasses } from "../lib/status";

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" });
}

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString("en-US", { dateStyle: "medium", timeStyle: "short" });
}

export default function ApplicationDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [application, setApplication] = useState<Application | null>(null);
  const [history, setHistory] = useState<StatusHistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [nextStatus, setNextStatus] = useState<ApplicationStatus | "">("");
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async (appId: number) => {
    setLoading(true);
    setError(null);
    try {
      const [app, hist] = await Promise.all([getApplicationById(appId), getStatusHistory(appId)]);
      setApplication(app);
      setHistory(hist);
    } catch {
      setError("Application not found.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (id) load(Number(id));
  }, [id, load]);

  const handleStatusUpdate = async () => {
    if (!id || !nextStatus || !application) return;
    setStatusUpdating(true);
    try {
      const updated = await updateApplicationStatus(Number(id), nextStatus);
      setApplication(updated);
      setNextStatus("");
      const hist = await getStatusHistory(Number(id));
      setHistory(hist);
    } catch {
      setError("Couldn't update the status.");
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    if (!window.confirm("Are you sure you want to delete this application?")) return;
    setDeleting(true);
    try {
      await deleteApplication(Number(id));
      navigate("/applications");
    } catch {
      setError("Couldn't delete — want to try again?");
      setDeleting(false);
    }
  };

  return (
    <AppShell>
      <div className="mx-auto max-w-2xl">
        <Link
          to="/applications"
          className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-ink-soft hover:text-ink"
        >
          <ArrowLeftIcon />
          Back to Applications
        </Link>

        {loading ? (
          <p className="text-sm text-ink-soft">Loading…</p>
        ) : error && !application ? (
          <p className="rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">{error}</p>
        ) : application ? (
          <>
            <div className="rounded-xl border border-line bg-surface p-8 shadow-elevate-sm">
              {error && (
                <p className="mb-4 rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">
                  {error}
                </p>
              )}

              <div className="flex items-start justify-between gap-4">
                <div>
                  <h1 className="font-display text-2xl font-semibold text-ink">{application.companyName}</h1>
                  <p className="mt-1 text-sm text-ink-soft">{application.positionTitle}</p>
                </div>
                <span
                  className={`whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold ${statusBadgeClasses(
                    application.status
                  )}`}
                >
                  {STATUS_LABELS[application.status]}
                </span>
              </div>

              <dl className="mt-6 grid grid-cols-2 gap-4 border-t border-line pt-6 text-sm">
                <div>
                  <dt className="text-ink-soft">Applied Date</dt>
                  <dd className="mt-1 font-medium text-ink">{formatDate(application.appliedDate)}</dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-ink-soft">Notes</dt>
                  <dd className="mt-1 whitespace-pre-wrap text-ink">
                    {application.notes || <span className="text-ink-soft">No notes added</span>}
                  </dd>
                </div>
              </dl>

              <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-line pt-6">
                <Link
                  to={`/applications/${application.id}/edit`}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-line px-3.5 py-2 text-sm font-medium text-ink hover:bg-paper"
                >
                  <PencilIcon />
                  Edit
                </Link>
                <button
                  onClick={handleDelete}
                  disabled={deleting}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-line px-3.5 py-2 text-sm font-medium text-status-rejected hover:bg-status-rejected-bg disabled:opacity-60"
                >
                  <TrashIcon />
                  {deleting ? "Deleting…" : "Delete"}
                </button>

                <div className="ml-auto flex items-center gap-2">
                  <select
                    value={nextStatus}
                    onChange={(e) => setNextStatus(e.target.value as ApplicationStatus)}
                    className="rounded-lg border border-line bg-paper px-3 py-2 text-sm text-ink outline-none focus:border-brand"
                  >
                    <option value="">Change status</option>
                    {STATUS_OPTIONS.filter((s) => s !== application.status).map((s) => (
                      <option key={s} value={s}>
                        {STATUS_LABELS[s]}
                      </option>
                    ))}
                  </select>
                  <button
                    onClick={handleStatusUpdate}
                    disabled={!nextStatus || statusUpdating}
                    className="rounded-lg bg-brand px-3.5 py-2 text-sm font-semibold text-white hover:bg-brand-hover disabled:opacity-60"
                  >
                    {statusUpdating ? "Updating…" : "Update"}
                  </button>
                </div>
              </div>
            </div>

            <div className="mt-6 rounded-xl border border-line bg-surface p-8 shadow-elevate-sm">
              <h2 className="mb-5 font-display text-base font-semibold text-ink">Status History</h2>
              {history.length === 0 ? (
                <p className="text-sm text-ink-soft">No history yet.</p>
              ) : (
                <ol className="flex flex-col gap-5">
                  {[...history]
                    .sort((a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime())
                    .map((entry) => (
                      <li key={entry.id} className="relative border-l border-line pl-5">
                        <span
                          className={`absolute -left-[5px] top-1 h-2.5 w-2.5 rounded-full ${statusDotClasses(
                            entry.status
                          )}`}
                        />
                        <p className="text-sm font-medium text-ink">{STATUS_LABELS[entry.status]}</p>
                        <p className="text-xs text-ink-soft">{formatDateTime(entry.changedAt)}</p>
                        {entry.note && <p className="mt-1 text-sm text-ink">{entry.note}</p>}
                      </li>
                    ))}
                </ol>
              )}
            </div>
          </>
        ) : null}
      </div>
    </AppShell>
  );
}