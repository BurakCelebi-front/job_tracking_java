// Single source of truth for shared domain types.
// (Previously split between src/types/index.ts and src/api/types.ts —
// merged here, keeping the optional companyName/notes shape since the
// rest of the app already treats those fields as possibly absent.)

export type ApplicationStatus = "APPLIED" | "INTERVIEW" | "OFFER" | "REJECTED";

export interface Company {
  id: number;
  name: string;
  website: string;
  location: string;
}

export interface Application {
  id: number;
  companyId: number;
  companyName?: string;
  positionTitle: string;
  appliedDate: string;
  status: ApplicationStatus;
  notes?: string;
}

export interface StatusHistoryEntry {
  id: number;
  status: ApplicationStatus;
  changedAt: string;
  note?: string;
}