// API Client with Live Backend Integration and Seamless Fallback Mock Mode

const BASE_URL = 'http://localhost:8080/api/v1';

// Initial Mock Seed Data for Instant Experience
export const MOCK_JOBS = [
  {
    id: 1,
    title: 'Senior Full-Stack Java & React Engineer',
    department: 'Engineering',
    location: 'Remote / New York',
    jobType: 'FULL_TIME',
    experienceLevel: 'SENIOR',
    minExperienceYears: 5,
    salaryRange: '$140,000 - $175,000',
    description: 'Lead the development of scalable microservices and high-performance React frontends. You will architect event-driven architectures, design resilient REST APIs, and collaborate closely with product and AI teams.',
    requirements: '5+ years building distributed Java applications with Spring Boot. Deep proficiency in modern React, TypeScript, and cloud deployment pipelines.',
    skills: 'Java, Spring Boot, React, TypeScript, REST API, Docker, PostgreSQL, Microservices, Git',
    status: 'ACTIVE'
  },
  {
    id: 2,
    title: 'Machine Learning & NLP Specialist',
    department: 'AI Research',
    location: 'San Francisco, CA (Hybrid)',
    jobType: 'FULL_TIME',
    experienceLevel: 'MID_SENIOR',
    minExperienceYears: 3,
    salaryRange: '$150,000 - $190,000',
    description: 'Design and train transformer models, LLM retrieval-augmented generation (RAG) pipelines, and intelligent semantic ranking algorithms for our enterprise talent platform.',
    requirements: 'Strong foundation in Natural Language Processing (NLP), semantic search, vector embeddings, and Python/PyTorch. Familiarity with Java or Spring integration is a strong plus.',
    skills: 'Python, NLP, PyTorch, Transformers, LLMs, Vector Databases, Semantic Search, FastAPI, Docker, Java',
    status: 'ACTIVE'
  },
  {
    id: 3,
    title: 'Cloud Infrastructure & DevOps Architect',
    department: 'DevOps',
    location: 'Austin, TX / Remote',
    jobType: 'FULL_TIME',
    experienceLevel: 'LEAD',
    minExperienceYears: 6,
    salaryRange: '$160,000 - $200,000',
    description: 'Own the reliability, security, and scalability of multi-region cloud infrastructure on AWS and Kubernetes. Automate CI/CD pipelines and implement Terraform infrastructure as code.',
    requirements: 'Proven track record managing Kubernetes clusters in production. Expert knowledge of AWS, Terraform, Docker, monitoring stacks (Prometheus/Grafana), and zero-downtime deployment strategies.',
    skills: 'AWS, Kubernetes, Docker, Terraform, CI/CD, Linux, Prometheus, Grafana, Java, Python',
    status: 'ACTIVE'
  },
  {
    id: 4,
    title: 'Frontend UI/UX Engineer',
    department: 'Design & Product',
    location: 'Seattle, WA / Remote',
    jobType: 'FULL_TIME',
    experienceLevel: 'MID',
    minExperienceYears: 3,
    salaryRange: '$115,000 - $145,000',
    description: 'Craft engaging, ultra-fast web experiences and reusable design component libraries using React, modern CSS, and Vite. Champion accessibility, responsive design, and fluid animations.',
    requirements: '3+ years experience building interactive web applications with React. Deep mastery of modern CSS/SCSS, performance profiling, responsive layouts, and state management.',
    skills: 'React, JavaScript, TypeScript, CSS3, HTML5, Vite, Redux, UI/UX, Web Accessibility',
    status: 'ACTIVE'
  }
];

export const MOCK_CANDIDATES = [
  {
    id: 1,
    fullName: 'Alex Rivera',
    email: 'alex.rivera@example.com',
    phone: '+1 (555) 234-5678',
    location: 'New York, NY',
    currentTitle: 'Senior Software Engineer',
    yearsExperience: 6,
    highestEducation: 'M.S. in Computer Science',
    skillsSummary: 'Java, Spring Boot, React, TypeScript, PostgreSQL, Docker, Microservices, REST API, Git, AWS',
    bio: 'Experienced full-stack engineer with 6 years leading cloud-native product development using Java Spring Boot and React. Passionate about clean code, unit testing, and distributed systems.',
    resumeText: 'Alex Rivera - Senior Full Stack Engineer. 6+ years experience with Java, Spring Boot, React, TypeScript, REST API, Docker, PostgreSQL, Microservices. Built cloud-native microservices on AWS.'
  },
  {
    id: 2,
    fullName: 'Dr. Priya Sharma',
    email: 'priya.sharma@example.com',
    phone: '+1 (555) 876-5432',
    location: 'San Francisco, CA',
    currentTitle: 'Lead AI Research Engineer',
    yearsExperience: 5,
    highestEducation: 'Ph.D. in Artificial Intelligence',
    skillsSummary: 'Python, NLP, PyTorch, Transformers, LLMs, Vector Databases, Semantic Search, FastAPI, Docker',
    bio: 'AI researcher with a Ph.D. specializing in Natural Language Processing and information retrieval. Authored papers on transformer fine-tuning and semantic document ranking.',
    resumeText: 'Dr. Priya Sharma - Lead AI Research Engineer. 5 years specializing in NLP, Transformers, LLMs, Vector Databases, Semantic Search, PyTorch, and Python. PhD from Stanford.'
  },
  {
    id: 3,
    fullName: 'Marcus Vance',
    email: 'marcus.vance@example.com',
    phone: '+1 (555) 345-6789',
    location: 'Austin, TX',
    currentTitle: 'Senior Cloud/DevOps Engineer',
    yearsExperience: 7,
    highestEducation: 'B.S. in Software Engineering',
    skillsSummary: 'AWS, Kubernetes, Docker, Terraform, CI/CD, Linux, Prometheus, Grafana, Bash, Python',
    bio: 'DevOps engineer passionate about GitOps, Kubernetes cluster scaling, and zero-trust cloud infrastructure on AWS. Spearheaded migration of 50+ microservices.',
    resumeText: 'Marcus Vance - Cloud DevOps Engineer. 7 years experience with Kubernetes, AWS, Docker, Terraform, CI/CD pipelines, Prometheus, Grafana, Linux.'
  },
  {
    id: 4,
    fullName: 'Elena Rostova',
    email: 'elena.rostova@example.com',
    phone: '+1 (555) 456-7890',
    location: 'Boston, MA',
    currentTitle: 'Frontend Developer',
    yearsExperience: 3,
    highestEducation: 'B.S. in Computer Science',
    skillsSummary: 'React, JavaScript, TypeScript, CSS3, HTML5, Vite, Redux, TailwindCSS, Figma',
    bio: 'Frontend specialist focused on design systems, accessible UI interactions, and snappy micro-frontends with React and modern CSS.',
    resumeText: 'Elena Rostova - Frontend Engineer. 3+ years experience building web apps with React, Vite, JavaScript, TypeScript, CSS3, Redux, and modern UI libraries.'
  }
];

export const MOCK_APPLICATIONS = [
  {
    id: 101,
    job: MOCK_JOBS[0],
    candidate: MOCK_CANDIDATES[0],
    status: 'SHORTLISTED',
    aiMatchScore: 94.2,
    aiRationale: 'Exceptional candidate fit for Senior Full-Stack Java & React Engineer. Matches 8/8 core skills with 6 years of experience.',
    recruiterNotes: 'Auto-shortlisted by AI intelligence. Strong portfolio.',
    appliedAt: '2026-09-20T10:15:00'
  },
  {
    id: 102,
    job: MOCK_JOBS[1],
    candidate: MOCK_CANDIDATES[1],
    status: 'INTERVIEW_SCHEDULED',
    aiMatchScore: 96.8,
    aiRationale: 'World-class alignment for ML & NLP Specialist role. Ph.D. in AI and deep transformer research.',
    recruiterNotes: 'Technical screen passed with honors. Panel scheduled.',
    interviewDate: '2026-09-29T14:00:00',
    appliedAt: '2026-09-21T11:30:00'
  },
  {
    id: 103,
    job: MOCK_JOBS[2],
    candidate: MOCK_CANDIDATES[2],
    status: 'AI_SCREENED',
    aiMatchScore: 89.5,
    aiRationale: 'Strong DevOps profile with 7 years in Kubernetes and AWS. Ready for recruiter review.',
    recruiterNotes: 'Pending team lead review.',
    appliedAt: '2026-09-22T09:00:00'
  },
  {
    id: 104,
    job: MOCK_JOBS[3],
    candidate: MOCK_CANDIDATES[3],
    status: 'OFFERED',
    aiMatchScore: 91.0,
    aiRationale: 'Complete match for Frontend UI/UX role. React, Vite, and CSS3 mastery.',
    recruiterNotes: 'Offer extended at $135k.',
    appliedAt: '2026-09-18T16:00:00'
  }
];

// Helper to determine mock match score
export function computeClientMatchScore(job, candidate) {
  const jobSkills = job.skills.toLowerCase().split(',').map(s => s.trim());
  const candSkills = (candidate.skillsSummary || candidate.resumeText || '').toLowerCase().split(',').map(s => s.trim());
  
  const matched = [];
  const missing = [];
  jobSkills.forEach(js => {
    if (candSkills.some(cs => cs.includes(js) || js.includes(cs)) || (candidate.resumeText || '').toLowerCase().includes(js)) {
      matched.push(js.toUpperCase());
    } else {
      missing.push(js.toUpperCase());
    }
  });

  const skillScore = jobSkills.length ? (matched.length / jobSkills.length) * 100 : 80;
  const expScore = candidate.yearsExperience >= (job.minExperienceYears || 2) ? 100 : (candidate.yearsExperience / job.minExperienceYears) * 100;
  const eduScore = 90;
  const semanticScore = 85;

  const overall = Math.round((skillScore * 0.45 + expScore * 0.25 + eduScore * 0.15 + semanticScore * 0.15) * 10) / 10;

  return {
    overallScore: Math.min(99, Math.max(45, overall)),
    skillScore: Math.round(skillScore),
    experienceScore: Math.round(expScore),
    educationScore: Math.round(eduScore),
    semanticScore: Math.round(semanticScore),
    matchedSkills: matched,
    missingSkills: missing,
    bonusSkills: ['Docker', 'Git', 'Agile', 'System Design'],
    suitabilityLevel: overall >= 85 ? 'EXCELLENT' : overall >= 70 ? 'STRONG' : 'MODERATE',
    recommendationRationale: `Profile matches ${matched.length} of ${jobSkills.length} core competencies. Experience (${candidate.yearsExperience} yrs) fulfills the requirement.`,
    recommendedNextSteps: ['Review technical code samples', 'Conduct initial hiring screen']
  };
}

export const api = {
  // Check backend health
  async checkBackend() {
    try {
      const res = await fetch(`${BASE_URL}/jobs`, { signal: AbortSignal.timeout(1500) });
      return res.ok;
    } catch {
      return false;
    }
  },

  // Jobs
  async getJobs(search = '') {
    try {
      const url = search ? `${BASE_URL}/jobs?search=${encodeURIComponent(search)}` : `${BASE_URL}/jobs`;
      const res = await fetch(url);
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, using mock jobs data');
    }
    if (!search) return MOCK_JOBS;
    return MOCK_JOBS.filter(j => 
      j.title.toLowerCase().includes(search.toLowerCase()) || 
      j.skills.toLowerCase().includes(search.toLowerCase())
    );
  },

  async createJob(jobData) {
    try {
      const res = await fetch(`${BASE_URL}/jobs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(jobData)
      });
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, mocking job creation');
    }
    const newJob = { id: Date.now(), ...jobData, status: 'ACTIVE' };
    MOCK_JOBS.unshift(newJob);
    return newJob;
  },

  // Candidates
  async getCandidates(search = '') {
    try {
      const url = search ? `${BASE_URL}/candidates?search=${encodeURIComponent(search)}` : `${BASE_URL}/candidates`;
      const res = await fetch(url);
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, using mock candidates');
    }
    if (!search) return MOCK_CANDIDATES;
    return MOCK_CANDIDATES.filter(c => 
      c.fullName.toLowerCase().includes(search.toLowerCase()) || 
      c.skillsSummary.toLowerCase().includes(search.toLowerCase())
    );
  },

  // AI Matching
  async rankCandidatesForJob(jobId) {
    try {
      const res = await fetch(`${BASE_URL}/matching/rank-candidates/${jobId}`);
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, mocking ranking');
    }
    const targetJob = MOCK_JOBS.find(j => j.id === Number(jobId)) || MOCK_JOBS[0];
    const ranked = MOCK_CANDIDATES.map(c => ({
      rank: 0,
      candidate: c,
      matchDetails: computeClientMatchScore(targetJob, c)
    }));
    ranked.sort((a, b) => b.matchDetails.overallScore - a.matchDetails.overallScore);
    ranked.forEach((item, index) => { item.rank = index + 1; });
    return ranked;
  },

  async analyzeMatch(jobId, customResumeText = '', candidateId = null) {
    try {
      const res = await fetch(`${BASE_URL}/matching/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ jobId, candidateId, customResumeText })
      });
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, computing AI match client-side');
    }
    const targetJob = MOCK_JOBS.find(j => j.id === Number(jobId)) || MOCK_JOBS[0];
    const tempCandidate = candidateId 
      ? MOCK_CANDIDATES.find(c => c.id === Number(candidateId)) 
      : {
          fullName: 'Custom Candidate',
          yearsExperience: 4,
          skillsSummary: customResumeText,
          resumeText: customResumeText
        };
    return computeClientMatchScore(targetJob, tempCandidate);
  },

  // Applications & Workflow
  async getApplications() {
    try {
      const res = await fetch(`${BASE_URL}/applications`);
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, using mock applications');
    }
    return MOCK_APPLICATIONS;
  },

  async updateApplicationStatus(id, status, notes = '') {
    try {
      const res = await fetch(`${BASE_URL}/applications/${id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, notes })
      });
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, updating local mock state');
    }
    const app = MOCK_APPLICATIONS.find(a => a.id === Number(id));
    if (app) {
      app.status = status;
      if (notes) app.recruiterNotes = notes;
    }
    return app;
  },

  // Analytics
  async getDashboardStats() {
    try {
      const res = await fetch(`${BASE_URL}/analytics/dashboard`);
      if (res.ok) return await res.json();
    } catch (e) {
      console.warn('Backend unavailable, using mock analytics');
    }
    return {
      totalJobs: MOCK_JOBS.length,
      totalCandidates: MOCK_CANDIDATES.length,
      totalApplications: MOCK_APPLICATIONS.length,
      shortlistedCount: MOCK_APPLICATIONS.filter(a => a.status === 'SHORTLISTED').length,
      interviewCount: MOCK_APPLICATIONS.filter(a => a.status === 'INTERVIEW_SCHEDULED').length,
      averageMatchScore: 92.8
    };
  }
};
