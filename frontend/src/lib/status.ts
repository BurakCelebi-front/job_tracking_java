import type { ApplicationStatus } from "../api/types";

export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  INTERVIEW: "Interview",
  OFFER: "Offer",
  REJECTED: "Rejected",
};
 
export const STATUS_OPTIONS: ApplicationStatus[] = ["APPLIED", "INTERVIEW", "OFFER", "REJECTED"];
 
export function statusBadgeClasses(status: ApplicationStatus): string {
  switch (status) {
    case "APPLIED":
      return "bg-status-applied-bg text-status-applied";
    case "INTERVIEW":
      return "bg-status-interview-bg text-status-interview";
    case "OFFER":
      return "bg-status-offer-bg text-status-offer";
    case "REJECTED":
      return "bg-status-rejected-bg text-status-rejected";
  }
}
 

export function statusDotClasses(status: ApplicationStatus): string {
  switch (status) {
    case "APPLIED":
      return "bg-status-applied";
    case "INTERVIEW":
      return "bg-status-interview";
    case "OFFER":
      return "bg-status-offer";
    case "REJECTED":
      return "bg-status-rejected";
  }
}
 
