import React from 'react';
import { Briefcase, Users, Award, TrendingUp, Sparkles, ArrowRight, Zap, CheckCircle2, Clock } from 'lucide-react';

export default function RecruiterDashboard({ stats, jobs, onSelectJobForRanking, onOpenScanner, onSelectCandidate }) {
  const kpis = [
    {
      title: 'Active Requisitions',
      value: stats.totalJobs || 4,
      trend: '+2 added this week',
      icon: Briefcase,
      color: 'indigo',
      gradient: 'var(--primary-gradient)'
    },
    {
      title: 'Candidate Talent Pool',
      value: stats.totalCandidates || 4,
      trend: 'NLP parsed & indexed',
      icon: Users,
      color: 'cyan',
      gradient: 'var(--cyan-gradient)'
    },
    {
      title: 'AI Fast-Track Shortlist',
      value: stats.shortlistedCount || 2,
      trend: 'Match Score ≥ 75%',
      icon: Award,
      color: 'emerald',
      gradient: 'var(--emerald-gradient)'
    },
    {
      title: 'Average Match Accuracy',
      value: `${stats.averageMatchScore || 92.8}%`,
      trend: 'Cosine + TF-IDF semantic',
      icon: TrendingUp,
      color: 'amber',
      gradient: 'var(--amber-gradient)'
    }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Hero Welcome Banner */}
      <div className="glass-card" style={{
        padding: '2.5rem',
        borderRadius: 'var(--radius-lg)',
        background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.7) 0%, rgba(15, 23, 42, 0.9) 100%)',
        border: '1px solid var(--border-glow)',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{
          position: 'absolute',
          top: '-50px',
          right: '-50px',
          width: '250px',
          height: '250px',
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(99, 102, 241, 0.25) 0%, transparent 70%)',
          pointerEvents: 'none'
        }} />

        <div style={{ maxWidth: '800px', position: 'relative', zIndex: 1 }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', padding: '6px 14px', borderRadius: 'var(--radius-full)', background: 'rgba(99, 102, 241, 0.15)', border: '1px solid rgba(99, 102, 241, 0.3)', marginBottom: '1.25rem' }}>
            <Sparkles size={16} color="#818cf8" />
            <span style={{ fontSize: '0.825rem', fontWeight: 600, color: '#c7d2fe' }}>Next-Gen Recruitment Intelligence</span>
          </div>

          <h1 style={{ fontSize: '2.4rem', lineHeight: 1.2, marginBottom: '0.85rem' }}>
            AI-Powered Candidate Ranking &amp; Resume Matching
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.05rem', lineHeight: 1.6, marginBottom: '1.75rem' }}>
            Screen resumes instantly, uncover semantic skill matches, identify critical skill gaps, and accelerate your recruitment pipeline with high-precision scoring algorithms.
          </p>

          <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap' }}>
            <button className="btn btn-primary" onClick={() => onSelectJobForRanking(jobs[0]?.id || 1)}>
              <Zap size={18} />
              Open AI Ranking Arena
            </button>
            <button className="btn btn-secondary" onClick={onOpenScanner}>
              <Sparkles size={18} />
              Test Resume Scanner &amp; Skill Gap Tool
            </button>
          </div>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '1.5rem'
      }}>
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div key={idx} className="glass-card" style={{ padding: '1.5rem', position: 'relative' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem', fontWeight: 600 }}>{kpi.title}</span>
                <div style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '10px',
                  background: kpi.gradient,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.3)'
                }}>
                  <Icon size={19} color="#ffffff" />
                </div>
              </div>
              <div style={{ fontSize: '2.2rem', fontWeight: 800, fontFamily: 'var(--font-display)', marginBottom: '0.35rem', letterSpacing: '-0.02em' }}>
                {kpi.value}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                <CheckCircle2 size={13} color="#10b981" />
                <span>{kpi.trend}</span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Active Roles & Quick Matching Grid */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Active Requisitions &amp; Candidate Pools</h2>
            <p style={{ color: 'var(--text-dim)', fontSize: '0.875rem' }}>Select any role to view real-time AI ranked candidates</p>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          {jobs.map(job => (
            <div
              key={job.id}
              className="glass-card"
              style={{
                padding: '1.6rem',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
              onClick={() => onSelectJobForRanking(job.id)}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                  <span className="badge badge-indigo">{job.department}</span>
                  <span className="badge badge-emerald">{job.experienceLevel}</span>
                </div>
                <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#ffffff' }}>{job.title}</h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {job.description}
                </p>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '1.25rem' }}>
                  {job.skills.split(',').slice(0, 4).map((skill, sIdx) => (
                    <span key={sIdx} style={{
                      fontSize: '0.75rem',
                      padding: '3px 8px',
                      borderRadius: '6px',
                      background: 'rgba(255,255,255,0.05)',
                      color: 'var(--text-muted)',
                      border: '1px solid var(--border-subtle)'
                    }}>
                      {skill.trim()}
                    </span>
                  ))}
                  {job.skills.split(',').length > 4 && (
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', alignSelf: 'center' }}>
                      +{job.skills.split(',').length - 4} more
                    </span>
                  )}
                </div>
              </div>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingTop: '1rem',
                borderTop: '1px solid var(--border-subtle)',
                marginTop: '0.5rem'
              }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: '#38bdf8' }}>
                  {job.salaryRange || 'Competitive'}
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.85rem', color: '#a5b4fc', fontWeight: 600 }}>
                  View AI Rank <ArrowRight size={15} />
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
