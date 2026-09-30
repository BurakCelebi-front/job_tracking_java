export type ApplicationStatus = "APPLIED" | "INTERVIEW" | "OFFER" | "REJECTED";

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