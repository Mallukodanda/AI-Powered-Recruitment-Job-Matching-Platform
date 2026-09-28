export type Role = 'CANDIDATE' | 'RECRUITER' | 'ADMIN';

export interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active?: boolean;
  createdAt: string;
}

export interface AuthState {
  user: UserResponse | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface RegisterRequest {
  email: string;
  fullName: string;
  password: string;
  role?: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: UserResponse;
}

export interface Job {
  id: number;
  title: string;
  department?: string;
  location?: string;
  jobType?: string;
  experienceLevel?: string;
  minExperienceYears?: number;
  salaryRange?: string;
  description?: string;
  requirements?: string;
  skills?: string;
  status: 'ACTIVE' | 'ARCHIVED' | 'CLOSED';
  createdAt: string;
}

export interface DashboardStats {
  totalJobs: number;
  totalCandidates: number;
  totalApplications: number;
  shortlistedCount: number;
  interviewCount: number;
  averageMatchScore: number;
}

export interface Education {
  id?: number;
  institution: string;
  degree: string;
  fieldOfStudy?: string;
  startDate?: string;
  endDate?: string;
  isCurrent?: boolean;
  grade?: string;
  description?: string;
}

export interface Experience {
  id?: number;
  company: string;
  title: string;
  location?: string;
  employmentType?: string;
  startDate: string;
  endDate?: string;
  isCurrent?: boolean;
  description?: string;
}

export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface CandidateSkill {
  id?: number;
  name: string;
  category?: string;
  proficiencyLevel?: ProficiencyLevel;
  yearsExperience?: number;
}

export interface Certification {
  id?: number;
  name: string;
  issuingOrganization: string;
  issueDate?: string;
  expirationDate?: string;
  credentialId?: string;
  credentialUrl?: string;
}

export interface Project {
  id?: number;
  title: string;
  description?: string;
  technologies?: string;
  projectUrl?: string;
  repoUrl?: string;
  startDate?: string;
  endDate?: string;
}

export interface CandidateProfile {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  location?: string;
  headline?: string;
  currentTitle?: string;
  yearsExperience?: number;
  highestEducation?: string;
  skillsSummary?: string;
  bio?: string;
  resumeText?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
  createdAt: string;
  educations?: Education[];
  experiences?: Experience[];
  skills?: CandidateSkill[];
  certifications?: Certification[];
  projects?: Project[];
}

export type ApplicationStatus = 'APPLIED' | 'SCREENING' | 'INTERVIEW_SCHEDULED' | 'OFFER_EXTENDED' | 'REJECTED';

export interface Application {
  id: number;
  job: Job;
  candidate: CandidateProfile;
  status: ApplicationStatus;
  appliedAt: string;
  aiMatchScore?: number;
  aiRationale?: string;
}

export interface Company {
  id: number;
  name: string;
  industry?: string;
  website?: string;
  location?: string;
  contactEmail?: string;
  description?: string;
  verified: boolean;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  actorEmail: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  result: string;
  metadata?: string;
  ipAddress?: string;
  timestamp: string;
}

export interface PlatformConfig {
  id: number;
  configKey: string;
  configValue: string;
  description?: string;
  category: string;
  updatedBy?: string;
  updatedAt: string;
}

export interface PlatformAnalytics {
  totalUsers: number;
  totalCandidates: number;
  totalRecruiters: number;
  totalAdmins: number;
  totalCompanies: number;
  totalJobs: number;
  activeJobs: number;
  totalApplications: number;
  totalInterviews: number;
  applicationsByStatus: Record<string, number>;
  interviewsByStatus: Record<string, number>;
  usersByRole: Record<string, number>;
  averageMatchScore: number;
  totalScreened: number;
  autoShortlistedCount: number;
  totalResumesParsed: number;
  totalEmbeddings: number;
}
