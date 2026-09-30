import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import { ArrowLeftIcon } from "../components/icons";
import { createApplication, getApplicationById, updateApplication } from "../api/applications";
import { getCompanies } from "../api/companies";
import type { Company } from "../types";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

interface FormState {
  companyId: string;
  positionTitle: string;
  appliedDate: string;
  notes: string;
}

const emptyForm: FormState = {
  companyId: "",
  positionTitle: "",
  appliedDate: today(),
  notes: "",
};

// Used for both /applications/new and /applications/:id/edit.
export default function ApplicationForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [companies, setCompanies] = useState<Company[]>([]);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof FormState, string>>>({});
  const [serverError, setServerError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    getCompanies()
      .then((data) => {
        if (!cancelled) setCompanies(data);
      })
      .catch(() => {
        if (!cancelled) setServerError("The companies could not be loaded.");
      });

    if (isEdit && id) {
      getApplicationById(Number(id))
        .then((app) => {
          if (cancelled) return;
          setForm({
            companyId: String(app.companyId),
            positionTitle: app.positionTitle,
            appliedDate: app.appliedDate.slice(0, 10),
            notes: app.notes ?? "",
          });
        })
        .catch(() => {
          if (!cancelled) setServerError("The application could not be uploaded.");
        })
        .finally(() => {
          if (!cancelled) setLoading(false);
        });
    }

    return () => {
      cancelled = true;
    };
  }, [id, isEdit]);

  const updateField = <K extends keyof FormState>(field: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const validate = (): boolean => {
    const nextErrors: Partial<Record<keyof FormState, string>> = {};
    if (!form.companyId) nextErrors.companyId = "You need to choose a company.";
    if (!form.positionTitle.trim()) nextErrors.positionTitle = "A position title is required.";
    if (!form.appliedDate) nextErrors.appliedDate = "The application date is required.";
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setServerError(null);
    if (!validate()) return;

    setSubmitting(true);
    const payload = {
      companyId: Number(form.companyId),
      positionTitle: form.positionTitle.trim(),
      appliedDate: form.appliedDate,
      notes: form.notes.trim() || undefined,
    };

    try {
      const result =
        isEdit && id ? await updateApplication(Number(id), payload) : await createApplication(payload);
      navigate(`/applications/${result.id}`);
    } catch {
      setServerError("Couldn't save it. Would you like to try again?");
    } finally {
      setSubmitting(false);
    }
  };

  const inputClass =
    "w-full rounded-lg border border-line bg-paper px-3.5 py-2.5 text-sm text-ink outline-none focus:border-brand focus:ring-2 focus:ring-brand-tint";
  const errorClass = "border-status-rejected focus:border-status-rejected focus:ring-status-rejected-bg";
  const labelClass = "mb-1.5 block text-sm font-medium text-ink";

  return (
    <AppShell>
      <div className="mx-auto max-w-xl">
        <Link
          to="/applications"
          className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-ink-soft hover:text-ink"
        >
          <ArrowLeftIcon />
          Back to Applications
        </Link>

        <div className="rounded-xl border border-line bg-surface p-8 shadow-elevate-sm">
          <h1 className="mb-6 font-display text-xl font-semibold text-ink">
            {isEdit ? "Edit Application" : "New Application"}
          </h1>

          {loading ? (
            <p className="text-sm text-ink-soft">Loading...</p>
          ) : (
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              {serverError && (
                <p className="rounded-lg bg-status-rejected-bg px-3.5 py-2.5 text-sm text-status-rejected">
                  {serverError}
                </p>
              )}

              <div>
                <label className={labelClass} htmlFor="companyId">
                  Company
                </label>
                <select
                  id="companyId"
                  value={form.companyId}
                  onChange={(e) => updateField("companyId", e.target.value)}
                  className={`${inputClass} ${errors.companyId ? errorClass : ""}`}
                >
                  <option value="">Choose a Company</option>
                  {companies.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
                {errors.companyId && <p className="mt-1 text-xs text-status-rejected">{errors.companyId}</p>}
                {companies.length === 0 && (
                  <p className="mt-1 text-xs text-ink-soft">
                    You haven't added a company yet — first{" "}
                    <Link to="/companies" className="text-brand hover:underline">
                      Companies
                    </Link>{" "}
                    add it from the page.
                  </p>
                )}
              </div>

              <div>
                <label className={labelClass} htmlFor="positionTitle">
                  Position
                </label>
                <input
                  id="positionTitle"
                  type="text"
                  value={form.positionTitle}
                  onChange={(e) => updateField("positionTitle", e.target.value)}
                  placeholder="Backend Developer"
                  className={`${inputClass} ${errors.positionTitle ? errorClass : ""}`}
                />
                {errors.positionTitle && (
                  <p className="mt-1 text-xs text-status-rejected">{errors.positionTitle}</p>
                )}
              </div>

              <div>
                <label className={labelClass} htmlFor="appliedDate">
                  Application Date
                </label>
                <input
                  id="appliedDate"
                  type="date"
                  value={form.appliedDate}
                  onChange={(e) => updateField("appliedDate", e.target.value)}
                  className={`${inputClass} ${errors.appliedDate ? errorClass : ""}`}
                />
                {errors.appliedDate && (
                  <p className="mt-1 text-xs text-status-rejected">{errors.appliedDate}</p>
                )}
              </div>

              <div>
                <label className={labelClass} htmlFor="notes">
                  Note
                </label>
                <textarea
                  id="notes"
                  rows={4}
                  value={form.notes}
                  onChange={(e) => updateField("notes", e.target.value)}
                  placeholder="References, contact person, interview details..."
                  className={`${inputClass} resize-none`}
                />
              </div>

              <div className="mt-2 flex items-center gap-3">
                <button
                  type="submit"
                  disabled={submitting}
                  className="rounded-lg bg-brand px-4 py-2.5 text-sm font-semibold text-white hover:bg-brand-hover disabled:opacity-60"
                >
                  {submitting ? "Saving" : isEdit ? "Save Changes" : "Add Application"}
                </button>
                <Link
                  to="/applications"
                  className="rounded-lg px-4 py-2.5 text-sm font-medium text-ink-soft hover:text-ink"
                >
                  Cancel
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </AppShell>
  );
}