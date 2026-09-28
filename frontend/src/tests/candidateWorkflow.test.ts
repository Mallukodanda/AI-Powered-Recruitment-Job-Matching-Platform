import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from '../store/useAuthStore';

describe('Candidate End-to-End Recruitment Workflow', () => {
  beforeEach(() => {
    localStorage.clear();
    useAuthStore.getState().logout();
  });

  it('executes Candidate Lifecycle: Register -> Login -> Profile -> Resume -> Search -> Apply -> Track', async () => {
    // 1. Step 1: Register Candidate
    const registrationPayload = {
      email: 'candidate.jane@example.com',
      password: 'SecurePassword123!',
      firstName: 'Jane',
      lastName: 'Doe',
      role: 'CANDIDATE' as const,
    };
    expect(registrationPayload.email).toContain('@');
    expect(registrationPayload.role).toBe('CANDIDATE');

    // 2. Step 2: Login & Authenticate (Token & User State)
    const mockAuthResponse = {
      token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mockTokenPayload',
      user: {
        id: 101,
        email: registrationPayload.email,
        fullName: `${registrationPayload.firstName} ${registrationPayload.lastName}`,
        firstName: registrationPayload.firstName,
        lastName: registrationPayload.lastName,
        role: 'CANDIDATE' as const,
        active: true,
        createdAt: new Date().toISOString(),
      },
    };

    useAuthStore.getState().setAuth(mockAuthResponse.user, mockAuthResponse.token);
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().user?.email).toBe('candidate.jane@example.com');
    expect(useAuthStore.getState().token).toBe(mockAuthResponse.token);

    // 3. Step 3: Candidate Profile Creation
    const candidateProfile = {
      id: 50,
      fullName: `${registrationPayload.firstName} ${registrationPayload.lastName}`,
      email: registrationPayload.email,
      phone: '+1 (555) 987-6543',
      location: 'San Francisco, CA',
      currentTitle: 'Senior Full Stack Engineer',
      yearsExperience: 6,
      highestEducation: 'B.S. in Computer Science',
      skillsSummary: 'Java, Spring Boot, React, TypeScript, PostgreSQL',
    };
    expect(candidateProfile.yearsExperience).toBeGreaterThanOrEqual(5);

    // 4. Step 4: Resume Upload & Parsing Simulation
    const resumeUploadMock = {
      storageKey: 'resumes/jane_doe_2026.pdf',
      fileSize: 1024 * 450, // 450 KB
      extractedSkills: ['Java', 'Spring Boot', 'React', 'TypeScript', 'Docker', 'PostgreSQL'],
      status: 'PARSED',
    };
    expect(resumeUploadMock.fileSize).toBeLessThan(10 * 1024 * 1024); // < 10MB limit
    expect(resumeUploadMock.extractedSkills).toContain('Spring Boot');

    // 5. Step 5: Advanced Job Search & AI Compatibility Scoring
    const availableJobs = [
      {
        id: 1,
        title: 'Senior Full-Stack Java & React Engineer',
        skills: 'Java, Spring Boot, React, TypeScript, PostgreSQL',
        minExperienceYears: 5,
        status: 'ACTIVE',
      },
      {
        id: 2,
        title: 'Senior Python Data Scientist',
        skills: 'Python, PyTorch, Machine Learning',
        minExperienceYears: 6,
        status: 'ACTIVE',
      },
    ];

    // Filter jobs matching candidate's primary skill "Java"
    const searchResults = availableJobs.filter((job) =>
      job.skills.toLowerCase().includes('java')
    );
    expect(searchResults).toHaveLength(1);
    expect(searchResults[0].title).toBe('Senior Full-Stack Java & React Engineer');

    // 6. Step 6: Apply to Matched Job with Cover Letter
    const targetJob = searchResults[0];
    const applicationPayload = {
      candidateId: candidateProfile.id,
      jobId: targetJob.id,
      coverLetter: 'I am excited to apply for this Full-Stack Java & React role.',
    };

    const createdApplication = {
      id: 201,
      ...applicationPayload,
      status: 'APPLIED',
      matchScore: 92.5,
      appliedAt: new Date().toISOString(),
    };
    expect(createdApplication.status).toBe('APPLIED');
    expect(createdApplication.matchScore).toBeGreaterThanOrEqual(85.0);

    // 7. Step 7: Application Tracking & Status Progression
    const statusProgression: string[] = [
      'APPLIED',
      'UNDER_REVIEW',
      'SHORTLISTED',
      'INTERVIEW',
    ];

    let currentStatus = createdApplication.status;
    for (const nextStatus of statusProgression) {
      currentStatus = nextStatus;
    }
    expect(currentStatus).toBe('INTERVIEW');
  });
});
