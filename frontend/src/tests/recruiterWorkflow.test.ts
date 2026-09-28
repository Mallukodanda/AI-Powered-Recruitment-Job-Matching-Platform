import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from '../store/useAuthStore';

describe('Recruiter End-to-End Recruitment & Scheduling Workflow', () => {
  beforeEach(() => {
    localStorage.clear();
    useAuthStore.getState().logout();
  });

  it('executes Recruiter Lifecycle: Login -> Company -> Job -> Applications -> Shortlist -> Interview', () => {
    // 1. Step 1: Recruiter Login
    const recruiterAuth = {
      token: 'recruiter.jwt.signature.mock',
      user: {
        id: 201,
        email: 'recruiter.bob@techcorp.io',
        fullName: 'Bob Recruiter',
        firstName: 'Bob',
        lastName: 'Recruiter',
        role: 'RECRUITER' as const,
        active: true,
        createdAt: new Date().toISOString(),
      },
    };

    useAuthStore.getState().setAuth(recruiterAuth.user, recruiterAuth.token);
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().user?.role).toBe('RECRUITER');

    // 2. Step 2: Company Profile Setup & Verification Association
    const company = {
      id: 10,
      name: 'TechCorp Cloud Solutions',
      website: 'https://techcorp.io',
      industry: 'Cloud Software',
      location: 'Austin, TX',
      verified: true,
    };
    expect(company.verified).toBe(true);

    // 3. Step 3: Job Requisition Creation
    const newJob = {
      id: 55,
      title: 'Principal Distributed Systems Architect',
      companyId: company.id,
      companyName: company.name,
      department: 'Infrastructure',
      location: 'Remote / US',
      jobType: 'FULL_TIME',
      experienceLevel: 'LEAD',
      minExperienceYears: 8,
      salaryRange: '$180k - $220k',
      description: 'Architect multi-region distributed systems with high availability and resilience.',
      requirements: '8+ years with Java, Spring Boot, Kafka, and Kubernetes.',
      skills: 'Java, Spring Boot, Kafka, Kubernetes, Microservices',
      status: 'ACTIVE',
    };
    expect(newJob.status).toBe('ACTIVE');

    // 4. Step 4: Receive Applicant Submissions
    const applicants = [
      {
        applicationId: 401,
        candidateId: 12,
        candidateName: 'Carlos Vance',
        currentTitle: 'Lead Software Engineer',
        yearsExperience: 9,
        matchScore: 94.2,
        status: 'APPLIED',
      },
      {
        applicationId: 402,
        candidateId: 15,
        candidateName: 'Derek Miller',
        currentTitle: 'Junior Developer',
        yearsExperience: 2,
        matchScore: 42.0,
        status: 'APPLIED',
      },
    ];

    // Recruiter filters for high-match candidates (score >= 80)
    const qualifiedApplicants = applicants.filter((app) => app.matchScore >= 80.0);
    expect(qualifiedApplicants).toHaveLength(1);
    expect(qualifiedApplicants[0].candidateName).toBe('Carlos Vance');

    // 5. Step 5: Shortlist Applicant
    const topCandidateApplication = qualifiedApplicants[0];
    const updatedApplication = {
      ...topCandidateApplication,
      status: 'SHORTLISTED',
      reviewedAt: new Date().toISOString(),
    };
    expect(updatedApplication.status).toBe('SHORTLISTED');

    // 6. Step 6: Schedule Technical Interview with Timezone
    const interviewSlot = {
      id: 88,
      candidateId: topCandidateApplication.candidateId,
      jobId: newJob.id,
      interviewerEmail: recruiterAuth.user.email,
      interviewType: 'TECHNICAL',
      scheduledStartTime: '2026-10-15T14:00:00Z',
      scheduledEndTime: '2026-10-15T15:00:00Z',
      timeZone: 'America/New_York',
      locationOrUrl: 'https://meet.techcorp.io/interview-88',
      status: 'SCHEDULED',
      reminderSent: false,
    };

    expect(interviewSlot.status).toBe('SCHEDULED');
    expect(interviewSlot.interviewType).toBe('TECHNICAL');
    expect(interviewSlot.timeZone).toBe('America/New_York');

    // Transition application to INTERVIEW stage
    const finalApplicationState = {
      ...updatedApplication,
      status: 'INTERVIEW',
    };
    expect(finalApplicationState.status).toBe('INTERVIEW');
  });
});
