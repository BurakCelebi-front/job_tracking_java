// pages/Dashboard.tsx

import { useEffect, useState } from "react";
import { getApplications } from "../api/applications";
import type { Application, ApplicationStatus } from "../api/types";

const STATUS_LABELS: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  INTERVIEW: "Interview",
  OFFER: "Offer",
  REJECTED: "Rejected",
};

export default function Dashboard() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    getApplications()
      .then(setApplications)
      .catch(() => setError("Failed to load data"));
  }, []);

  const total = applications.length;
  const counts: Record<ApplicationStatus, number> = {
    APPLIED: 0,
    INTERVIEW: 0,
    OFFER: 0,
    REJECTED: 0,
  };
  applications.forEach((app) => {
    counts[app.status] = (counts[app.status] ?? 0) + 1;
  });

  const recent = applications
    .slice()
    .sort((a, b) => new Date(b.appliedDate).getTime() - new Date(a.appliedDate).getTime())
    .slice(0, 5);

  return (
    <div>
      <h1>Dashboard</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div style={{ display: "flex", gap: 16, marginBottom: 24 }}>
        <div style={{ border: "1px solid #ccc", padding: 16, borderRadius: 8 }}>
          <div style={{ fontSize: 24, fontWeight: 700 }}>{total}</div>
          <div>Total applications</div>
        </div>
        {Object.entries(STATUS_LABELS).map(([status, label]) => (
          <div key={status} style={{ border: "1px solid #ccc", padding: 16, borderRadius: 8 }}>
            <div style={{ fontSize: 24, fontWeight: 700 }}>{counts[status as ApplicationStatus]}</div>
            <div>{label}</div>
          </div>
        ))}
      </div>

      <h2>Recent applications</h2>
      <ul>
        {recent.map((app) => (
          <li key={app.id}>
            {app.companyName ?? app.companyId} — {app.positionTitle} — {STATUS_LABELS[app.status]}
          </li>
        ))}
      </ul>
    </div>
  );
}