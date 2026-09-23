export interface Company {
  id: number;
  name: string;
  website: string;
  location: string;
}

export interface Application {
  id: number;
  companyId: number;
  companyName: string;
  positionTitle: string;
  status: "APPLIED" | "INTERVIEW" | "OFFER" | "REJECTED";
  appliedDate: string;
  notes: string;
}