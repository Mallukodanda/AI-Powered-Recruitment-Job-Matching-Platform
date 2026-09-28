import React from 'react';
import { Sparkles, Briefcase, Users, FileSearch, GitPullRequest, PlusCircle, CheckCircle2, AlertCircle } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onOpenNewJobModal, backendConnected }) {
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: Briefcase },
    { id: 'matching', label: 'AI Matching Arena', icon: Sparkles },
    { id: 'scanner', label: 'Resume & Gap Analyzer', icon: FileSearch },
    { id: 'pipeline', label: 'Hiring Pipeline', icon: GitPullRequest },
    { id: 'jobs', label: 'Job Postings', icon: Users },
  ];

  return (
    <header style={{
      position: 'sticky',
      top: 0,
      zIndex: 50,
      backgroundColor: 'rgba(9, 13, 22, 0.85)',
      backdropFilter: 'blur(16px)',
      borderBottom: '1px solid var(--border-subtle)',
      padding: '0 2rem'
    }}>
      <div style={{
        maxWidth: '1440px',
        margin: '0 auto',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '74px'
      }}>
        {/* Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', cursor: 'pointer' }} onClick={() => setActiveTab('dashboard')}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: 'var(--primary-gradient)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)'
          }}>
            <Sparkles size={22} color="#ffffff" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontSize: '1.25rem', fontWeight: 800, letterSpacing: '-0.02em', background: 'linear-gradient(90deg, #fff 0%, #cbd5e1 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                TalentPulse
              </span>
              <span className="badge badge-indigo" style={{ padding: '2px 8px', fontSize: '0.7rem' }}>AI 3.0</span>
            </div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 500 }}>Recruitment & Job Match Engine</p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          {navItems.map(item => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '9px 16px',
                  borderRadius: '10px',
                  fontSize: '0.9rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  border: 'none',
                  background: isActive ? 'rgba(99, 102, 241, 0.15)' : 'transparent',
                  color: isActive ? '#a5b4fc' : 'var(--text-muted)',
                  borderBottom: isActive ? '2px solid #6366f1' : '2px solid transparent',
                  transition: 'var(--transition)'
                }}
              >
                <Icon size={17} color={isActive ? '#818cf8' : 'currentColor'} />
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* Right Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          {/* Backend Status indicator */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontSize: '0.775rem',
            padding: '5px 10px',
            borderRadius: 'var(--radius-full)',
            background: backendConnected ? 'rgba(16, 185, 129, 0.1)' : 'rgba(99, 102, 241, 0.1)',
            color: backendConnected ? '#6ee7b7' : '#a5b4fc',
            border: `1px solid ${backendConnected ? 'rgba(16, 185, 129, 0.3)' : 'rgba(99, 102, 241, 0.3)'}`
          }}>
            {backendConnected ? <CheckCircle2 size={13} /> : <AlertCircle size={13} />}
            <span>{backendConnected ? 'Spring Boot Active' : 'Hybrid Demo Mode'}</span>
          </div>

          <button
            className="btn btn-primary btn-sm"
            onClick={onOpenNewJobModal}
            style={{ padding: '8px 16px' }}
          >
            <PlusCircle size={16} />
            Post Requisition
          </button>
        </div>
      </div>
    </header>
  );
}
