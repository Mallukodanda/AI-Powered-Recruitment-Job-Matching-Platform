import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import RecruiterDashboard from './components/RecruiterDashboard';
import CandidateRankingArena from './components/CandidateRankingArena';
import ResumeScanner from './components/ResumeScanner';
import WorkflowKanban from './components/WorkflowKanban';
import JobManagement from './components/JobManagement';
import CandidateDetailModal from './components/CandidateDetailModal';
import NewJobModal from './components/NewJobModal';
import { api, MOCK_JOBS } from './services/api';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [jobs, setJobs] = useState(MOCK_JOBS);
  const [stats, setStats] = useState({
    totalJobs: 4,
    totalCandidates: 4,
    totalApplications: 4,
    shortlistedCount: 2,
    interviewCount: 1,
    averageMatchScore: 92.8
  });
  const [selectedJobId, setSelectedJobId] = useState(1);
  const [backendConnected, setBackendConnected] = useState(false);

  // Modals state
  const [activeCandidateDetail, setActiveCandidateDetail] = useState(null);
  const [isNewJobModalOpen, setIsNewJobModalOpen] = useState(false);

  // Initialize data and test backend connection
  useEffect(() => {
    async function initData() {
      const isLive = await api.checkBackend();
      setBackendConnected(isLive);

      try {
        const fetchedJobs = await api.getJobs();
        if (fetchedJobs && fetchedJobs.length > 0) {
          setJobs(fetchedJobs);
          setSelectedJobId(fetchedJobs[0].id);
        }
        const fetchedStats = await api.getDashboardStats();
        if (fetchedStats) {
          setStats(fetchedStats);
        }
      } catch (err) {
        console.warn('Using default demo data');
      }
    }
    initData();
  }, []);

  const handleSelectJobForRanking = (jobId) => {
    setSelectedJobId(jobId);
    setActiveTab('matching');
  };

  const handleOpenDetailModal = (candidate, matchDetails, job) => {
    setActiveCandidateDetail({ candidate, matchDetails, job });
  };

  const handleCreateJob = async (newJobData) => {
    const created = await api.createJob(newJobData);
    setJobs(prev => [created, ...prev]);
    setStats(prev => ({ ...prev, totalJobs: prev.totalJobs + 1 }));
  };

  const handleScheduleInterview = async (candidateId, dateTimeStr, notes) => {
    setStats(prev => ({ ...prev, interviewCount: prev.interviewCount + 1 }));
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navigation Bar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onOpenNewJobModal={() => setIsNewJobModalOpen(true)}
        backendConnected={backendConnected}
      />

      {/* Main View Container */}
      <main style={{ flex: 1, maxWidth: '1440px', width: '100%', margin: '0 auto', padding: '2.5rem 2rem' }}>
        {activeTab === 'dashboard' && (
          <RecruiterDashboard
            stats={stats}
            jobs={jobs}
            onSelectJobForRanking={handleSelectJobForRanking}
            onOpenScanner={() => setActiveTab('scanner')}
            onSelectCandidate={(candidate, match) => handleOpenDetailModal(candidate, match, jobs[0])}
          />
        )}

        {activeTab === 'matching' && (
          <CandidateRankingArena
            jobs={jobs}
            selectedJobId={selectedJobId}
            onSelectJob={setSelectedJobId}
            onOpenDetailModal={handleOpenDetailModal}
          />
        )}

        {activeTab === 'scanner' && (
          <ResumeScanner jobs={jobs} />
        )}

        {activeTab === 'pipeline' && (
          <WorkflowKanban onOpenDetailModal={handleOpenDetailModal} />
        )}

        {activeTab === 'jobs' && (
          <JobManagement
            jobs={jobs}
            onOpenNewJobModal={() => setIsNewJobModalOpen(true)}
            onSelectJobForRanking={handleSelectJobForRanking}
          />
        )}
      </main>

      {/* Modals */}
      {activeCandidateDetail && (
        <CandidateDetailModal
          candidate={activeCandidateDetail.candidate}
          matchDetails={activeCandidateDetail.matchDetails}
          job={activeCandidateDetail.job}
          onClose={() => setActiveCandidateDetail(null)}
          onScheduleInterview={handleScheduleInterview}
        />
      )}

      {isNewJobModalOpen && (
        <NewJobModal
          onClose={() => setIsNewJobModalOpen(false)}
          onSubmit={handleCreateJob}
        />
      )}

      {/* Modern Platform Footer */}
      <footer style={{
        borderTop: '1px solid var(--border-subtle)',
        padding: '2rem',
        backgroundColor: 'rgba(9, 13, 22, 0.95)',
        marginTop: 'auto'
      }}>
        <div style={{
          maxWidth: '1440px',
          margin: '0 auto',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          fontSize: '0.825rem',
          color: 'var(--text-dim)'
        }}>
          <div>
            <strong style={{ color: 'var(--text-muted)' }}>TalentPulse AI Platform</strong> — Built with Java, Spring Boot 3, React, and NLP Matching Algorithms.
          </div>
          <div style={{ display: 'flex', gap: '1.5rem' }}>
            <span>REST API: <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer" style={{ color: '#818cf8', textDecoration: 'none' }}>/swagger-ui.html</a></span>
            <span>H2 Console: <a href="http://localhost:8080/h2-console" target="_blank" rel="noreferrer" style={{ color: '#818cf8', textDecoration: 'none' }}>/h2-console</a></span>
            <span>Status: <strong style={{ color: backendConnected ? '#6ee7b7' : '#93c5fd' }}>{backendConnected ? 'Live Backend Connected' : 'Hybrid Client Mode'}</strong></span>
          </div>
        </div>
      </footer>
    </div>
  );
}
