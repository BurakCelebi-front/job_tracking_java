// pages/Applications.tsx

import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import {
  getApplications,
  createApplication,
  updateApplicationStatus,
  deleteApplication,
  getStatusHistory,
} from "../api/applications";
import { getCompanies } from "../api/companies";
import type { Application, ApplicationStatus, StatusHistoryEntry } from "../api/types";
import type { Company } from "../types";

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  INTERVIEW: "Interview",
  OFFER: "Offer",
  REJECTED: "Rejected",
};

export default function Applications() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [companyId, setCompanyId] = useState("");
  const [position, setPosition] = useState("");
  const [appliedDate, setAppliedDate] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [historyMap, setHistoryMap] = useState<Record<number, StatusHistoryEntry[]>>({});
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const loadData = async () => {
    try {
      const [apps, comps] = await Promise.all([getApplications(), getCompanies()]);
      setApplications(apps);
      setCompanies(comps);
    } catch (err) {
      setError("Failed to load data");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await createApplication({
        companyId: Number(companyId),
        positionTitle: position,
        appliedDate,
        status: "APPLIED",
        note,
      });
      setCompanyId("");
      setPosition("");
      setAppliedDate("");
      setNote("");
      loadData();
    } catch (err) {
      setError("Failed to add application");
    }
  };

  const handleStatusChange = async (id: number, status: ApplicationStatus) => {
    try {
      await updateApplicationStatus(id, status);
      loadData();
      if (expandedId === id) {
        const history = await getStatusHistory(id);
        setHistoryMap((prev) => ({ ...prev, [id]: history }));
      }
    } catch (err) {
      setError("Failed to update status");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteApplication(id);
      loadData();
    } catch (err) {
      setError("Failed to delete");
    }
  };

  const handleToggleHistory = async (id: number) => {
    if (expandedId === id) {
      setExpandedId(null);
      return;
    }
    try {
      const history = await getStatusHistory(id);
      setHistoryMap((prev) => ({ ...prev, [id]: history }));
      setExpandedId(id);
    } catch (err) {
      setError("Failed to load history");
    }
  };

  return (
    <div>
      <h1>Applications</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <form onSubmit={handleSubmit}>
        <select value={companyId} onChange={(e) => setCompanyId(e.target.value)} required>
          <option value="">Select company</option>
          {companies.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <input
          placeholder="Position"
          value={position}
          onChange={(e) => setPosition(e.target.value)}
          required
        />
        <input
          type="date"
          value={appliedDate}
          onChange={(e) => setAppliedDate(e.target.value)}
          required
        />
        <input
          placeholder="Note"
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
        <button type="submit">Add</button>
      </form>

      <ul>
        {applications.map((app) => (
          <li key={app.id}>
            <div>
              {app.companyName ?? app.companyId} — {app.positionTitle} —{" "}
              <select
                value={app.status}
                onChange={(e) => handleStatusChange(app.id, e.target.value as ApplicationStatus)}
              >
                {Object.entries(STATUS_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
              <button onClick={() => handleToggleHistory(app.id)}>
                {expandedId === app.id ? "Hide history" : "History"}
              </button>
              <button onClick={() => handleDelete(app.id)}>Delete</button>
            </div>

            {expandedId === app.id && (
              <ul style={{ marginTop: 8, marginLeft: 16, fontSize: 13 }}>
                {(historyMap[app.id] ?? []).map((entry) => (
                  <li key={entry.id}>
                    {STATUS_LABELS[entry.status]} — {new Date(entry.changedAt).toLocaleString()}
                  </li>
                ))}
                {(historyMap[app.id]?.length ?? 0) === 0 && <li>No history yet</li>}
              </ul>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}