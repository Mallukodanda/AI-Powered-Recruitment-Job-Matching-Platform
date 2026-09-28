import React, { useState, useEffect } from 'react';
import { GitPullRequest, ArrowRight, UserCheck, Calendar, CheckCircle2, ChevronRight, Clock } from 'lucide-react';
import { api } from '../services/api';

const PIPELINE_COLUMNS = [
  { id: 'APPLIED', label: 'Applied', color: 'indigo', badge: 'badge-indigo' },
  { id: 'AI_SCREENED', label: 'AI Screened', color: 'cyan', badge: 'badge-cyan' },
  { id: 'SHORTLISTED', label: 'Shortlisted', color: 'emerald', badge: 'badge-emerald' },
  { id: 'INTERVIEW_SCHEDULED', label: 'Interviewing', color: 'amber', badge: 'badge-amber' },
  { id: 'OFFERED', label: 'Offer Extended', color: 'emerald', badge: 'badge-emerald' },
];

export default function WorkflowKanban({ onOpenDetailModal }) {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadApps() {
      try {
        const data = await api.getApplications();
        setApplications(data);
      } catch (err) {
        console.error('Failed to load applications', err);
      } finally {
        setLoading(false);
      }
    }
    loadApps();
  }, []);

  const moveApplication = async (appId, nextStatus) => {
    try {
      await api.updateApplicationStatus(appId, nextStatus);
      setApplications(prev => prev.map(a => a.id === appId ? { ...a, status: nextStatus } : a));
    } catch (err) {
      console.error('Failed to update stage', err);
    }
  };

  const getNextStage = (currentStatus) => {
    const sequence = ['APPLIED', 'AI_SCREENED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED'];
    const idx = sequence.indexOf(currentStatus);
    if (idx >= 0 && idx < sequence.length - 1) {
      return sequence[idx + 1];
    }
    return null;
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
      {/* Title */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
          <GitPullRequest size={22} color="#818cf8" />
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Recruiter Workflow Pipeline</h1>
        </div>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Manage and track candidate progression through AI automated screening, shortlisting, and hiring stages.
        </p>
      </div>

      {/* Kanban Board Columns Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '1.25rem',
        alignItems: 'start'
      }}>
        {PIPELINE_COLUMNS.map(col => {
          const colApps = applications.filter(a => a.status === col.id);

          return (
            <div
              key={col.id}
              style={{
                background: 'rgba(15, 23, 42, 0.75)',
                backdropFilter: 'blur(12px)',
                borderRadius: 'var(--radius-lg)',
                border: '1px solid var(--border-subtle)',
                padding: '1.25rem',
                minHeight: '520px',
                display: 'flex',
                flexDirection: 'column',
                gap: '1rem'
              }}
            >
              {/* Column Header */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-subtle)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.95rem', color: '#ffffff' }}>{col.label}</span>
                  <span className={`badge ${col.badge}`} style={{ padding: '2px 7px', fontSize: '0.7rem' }}>
                    {colApps.length}
                  </span>
                </div>
              </div>

              {/* Cards List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem', flex: 1 }}>
                {colApps.map(app => {
                  const nextStage = getNextStage(app.status);

                  return (
                    <div
                      key={app.id}
                      className="glass-card"
                      style={{
                        padding: '1.1rem',
                        background: 'rgba(255, 255, 255, 0.03)',
                        borderRadius: 'var(--radius-md)',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '0.75rem'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <h4 style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '2px' }}>
                            {app.candidate.fullName}
                          </h4>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                            {app.candidate.currentTitle}
                          </span>
                        </div>

                        <span style={{
                          fontSize: '0.85rem',
                          fontWeight: 800,
                          fontFamily: 'var(--font-mono)',
                          color: app.aiMatchScore >= 85 ? '#10b981' : '#6366f1'
                        }}>
                          {app.aiMatchScore}%
                        </span>
                      </div>

                      <div style={{
                        fontSize: '0.775rem',
                        color: 'var(--text-muted)',
                        background: 'rgba(0,0,0,0.25)',
                        padding: '6px 10px',
                        borderRadius: '6px'
                      }}>
                        <strong>Role:</strong> {app.job.title}
                      </div>

                      {app.interviewDate && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.75rem', color: '#fcd34d' }}>
                          <Calendar size={13} />
                          <span>Interview: {new Date(app.interviewDate).toLocaleDateString()}</span>
                        </div>
                      )}

                      {/* Advance Stage Button */}
                      {nextStage && (
                        <button
                          onClick={() => moveApplication(app.id, nextStage)}
                          className="btn btn-secondary btn-sm"
                          style={{
                            width: '100%',
                            fontSize: '0.75rem',
                            padding: '6px',
                            justifyContent: 'space-between'
                          }}
                        >
                          <span>Advance Stage</span>
                          <ChevronRight size={14} />
                        </button>
                      )}
                    </div>
                  );
                })}

                {colApps.length === 0 && (
                  <div style={{
                    textAlign: 'center',
                    color: 'var(--text-dim)',
                    fontSize: '0.8rem',
                    padding: '2.5rem 1rem',
                    border: '1px dashed rgba(255,255,255,0.06)',
                    borderRadius: 'var(--radius-md)'
                  }}>
                    No candidates in this stage
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
