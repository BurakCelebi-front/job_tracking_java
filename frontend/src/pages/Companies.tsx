import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import AppShell from "../components/AppShell";
import { PlusIcon, ArrowRightIcon, TrashIcon } from "../components/icons";
import { getCompanies, createCompany, deleteCompany } from "../api/companies";
import type { CompanyRequest } from "../api/companies";
import { getApplications } from "../api/applications";
import type { Company } from "../types";

interface FormState {
  name: string;
  website: string;
  location: string;
}

const emptyForm: FormState = { name: "", website: "", location: "" };

export default function Companies() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [applicationCounts, setApplicationCounts] = useState<Record<number, number>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const [showAddModal, setShowAddModal] = useState(false);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [comps, apps] = await Promise.all([getCompanies(), getApplications()]);
      setCompanies(comps);
      const counts: Record<number, number> = {};
      apps.forEach((a) => {
        counts[a.companyId] = (counts[a.companyId] ?? 0) + 1;
      });
      setApplicationCounts(counts);
    } catch {
      setError("Couldn't load companies.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openAddModal = () => {
    setForm(emptyForm);
    setFormError(null);
    setShowAddModal(true);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) {
      setFormError("Company name is required.");
      return;
    }
    setFormError(null);
    setSubmitting(true);
    const data: CompanyRequest = {
      name: form.name.trim(),
      website: form.website.trim(),
      location: form.location.trim(),
    };
    try {
      await createCompany(data);
      setShowAddModal(false);
      loadData();
    } catch {
      setFormError("Couldn't add the company.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Are you sure you want to delete this company?")) return;
    setDeletingId(id);
    try {
      await deleteCompany(id);
      loadData();
    } catch {
      setError("Couldn't delete the company (it may still have applications attached).");
    } finally {
      setDeletingId(null);
    }
  };

  const cards = useMemo(
    () =>
      companies.map((c) => ({
        ...c,
        initial: c.name.charAt(0).toUpperCase(),
        count: applicationCounts[c.id] ?? 0,
      })),
    [companies, applicationCounts]
  );

  return (
    <AppShell>
      <div className="flex flex-col gap-5">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-display text-xl font-semibold text-ink">Companies</h1>
            <p className="mt-1 text-sm text-ink-soft">{companies.length} companies saved</p>
          </div>
          <button
            onClick={openAddModal}
            className="flex h-10 items-center gap-2 rounded-lg bg-brand px-4.5 text-sm font-semibold text-white shadow-elevate-sm hover:bg-brand-hover"
          >
            <PlusIcon />
            Add Company
          </button>
        </div>

        {error && (
          <p className="rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">{error}</p>
        )}

        {loading ? (
          <p className="text-sm text-ink-soft">Loading…</p>
        ) : cards.length === 0 ? (
          <p className="text-sm text-ink-soft">You haven't added any companies yet.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {cards.map((c) => (
              <div
                key={c.id}
                className="flex flex-col gap-2.5 rounded-xl border border-line bg-surface p-5 shadow-elevate-sm transition hover:-translate-y-0.5 hover:shadow-elevate-md"
              >
                <div className="flex items-start justify-between">
                  <div className="flex h-10 w-10 items-center justify-center rounded-[10px] bg-brand-tint font-display text-base font-bold text-brand">
                    {c.initial}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="rounded-full bg-line-soft px-2.5 py-1 text-xs text-ink-soft">
                      {c.count} applications
                    </span>
                    <button
                      onClick={() => handleDelete(c.id)}
                      disabled={deletingId === c.id}
                      aria-label="Delete company"
                      className="flex h-7 w-7 items-center justify-center rounded-md text-ink-soft hover:bg-status-rejected-bg hover:text-status-rejected disabled:opacity-60"
                    >
                      <TrashIcon />
                    </button>
                  </div>
                </div>

                <div>
                  <div className="text-[15px] font-semibold text-ink">{c.name}</div>
                  {c.location && <div className="mt-0.5 text-[13px] text-ink-soft">{c.location}</div>}
                </div>

                <div className="mt-1.5 flex items-center justify-between border-t border-line pt-2.5">
                  <span className="truncate text-xs text-brand">{c.website}</span>
                  <Link
                    to="/applications"
                    className="inline-flex flex-shrink-0 items-center gap-1 text-xs font-semibold text-brand hover:text-brand-hover"
                  >
                    View applications
                    <ArrowRightIcon />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
          <div className="w-full max-w-md rounded-xl bg-surface p-7 shadow-elevate-md">
            <h2 className="font-display text-lg font-semibold text-ink">Add Company</h2>

            <form onSubmit={handleSubmit} className="mt-5 flex flex-col gap-4">
              {formError && (
                <p className="rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">
                  {formError}
                </p>
              )}

              <div>
                <label className="mb-1.5 block text-sm font-medium text-ink" htmlFor="company-name">
                  Company name
                </label>
                <input
                  id="company-name"
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                  className="w-full rounded-lg border border-line bg-paper px-3.5 py-2.5 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand-tint"
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-ink" htmlFor="company-website">
                  Website
                </label>
                <input
                  id="company-website"
                  type="text"
                  value={form.website}
                  onChange={(e) => setForm((p) => ({ ...p, website: e.target.value }))}
                  placeholder="acme.example"
                  className="w-full rounded-lg border border-line bg-paper px-3.5 py-2.5 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand-tint"
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-ink" htmlFor="company-location">
                  Location
                </label>
                <input
                  id="company-location"
                  type="text"
                  value={form.location}
                  onChange={(e) => setForm((p) => ({ ...p, location: e.target.value }))}
                  placeholder="Remote, New York…"
                  className="w-full rounded-lg border border-line bg-paper px-3.5 py-2.5 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand-tint"
                />
              </div>

              <div className="mt-1 flex items-center gap-3">
                <button
                  type="submit"
                  disabled={submitting}
                  className="rounded-lg bg-brand px-4 py-2.5 text-sm font-semibold text-white hover:bg-brand-hover disabled:opacity-60"
                >
                  {submitting ? "Adding…" : "Add"}
                </button>
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="rounded-lg px-4 py-2.5 text-sm font-medium text-ink-soft hover:text-ink"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppShell>
  );
}