import React, { useState, useEffect } from 'react';
import { Sparkles, Trophy, Award, CheckCircle, AlertTriangle, ArrowUpRight, Filter, ChevronRight, UserCheck, Calendar } from 'lucide-react';
import { api } from '../services/api';

export default function CandidateRankingArena({ jobs, selectedJobId, onSelectJob, onOpenDetailModal }) {
  const [rankedCandidates, setRankedCandidates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterLevel, setFilterLevel] = useState('ALL'); // ALL, EXCELLENT, STRONG

  const currentJob = jobs.find(j => j.id === Number(selectedJobId)) || jobs[0];

  useEffect(() => {
    if (!currentJob) return;
    async function fetchRanked() {
      setLoading(true);
      try {
        const data = await api.rankCandidatesForJob(currentJob.id);
        setRankedCandidates(data);
      } catch (err) {
        console.error('Error fetching rankings', err);
      } finally {
        setLoading(false);
      }
    }
    fetchRanked();
  }, [currentJob]);

  const filteredCandidates = rankedCandidates.filter(item => {
    if (filterLevel === 'ALL') return true;
    return item.matchDetails.suitabilityLevel === filterLevel;
  });

  const getRankBadge = (rank) => {
    if (rank === 1) return { bg: 'linear-gradient(135deg, #fbbf24 0%, #d97706 100%)', text: '#78350f', label: '1st' };
    if (rank === 2) return { bg: 'linear-gradient(135deg, #e2e8f0 0%, #94a3b8 100%)', text: '#1e293b', label: '2nd' };
    if (rank === 3) return { bg: 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)', text: '#431407', label: '3rd' };
    return { bg: 'rgba(255, 255, 255, 0.1)', text: '#cbd5e1', label: `#${rank}` };
  };

  const getScoreColor = (score) => {
    if (score >= 85) return '#10b981';
    if (score >= 70) return '#6366f1';
    if (score >= 50) return '#f59e0b';
    return '#f43f5e';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Top Requisition Selector Header */}
      <div className="glass-card" style={{ padding: '1.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <Trophy size={20} color="#fbbf24" />
              <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>AI Candidate Match Leaderboard</h1>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Multi-factor semantic ranking calculated across skills, seniority, education, and NLP ontology.
            </p>
          </div>

          {/* Job Select Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Target Role:</span>
            <select
              value={currentJob?.id || ''}
              onChange={(e) => onSelectJob(Number(e.target.value))}
              style={{
                background: 'var(--bg-input)',
                border: '1px solid var(--border-glow)',
                color: '#ffffff',
                padding: '10px 18px',
                borderRadius: 'var(--radius-md)',
                fontSize: '0.925rem',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer'
              }}
            >
              {jobs.map(job => (
                <option key={job.id} value={job.id} style={{ background: '#0f172a' }}>
                  {job.title} ({job.department})
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Selected Job Requirements Summary */}
        {currentJob && (
          <div style={{
            background: 'rgba(255, 255, 255, 0.03)',
            borderRadius: 'var(--radius-md)',
            padding: '1.25rem',
            border: '1px solid var(--border-subtle)',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.75rem'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.5rem', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <span style={{ fontWeight: 700, fontSize: '1rem', color: '#ffffff' }}>{currentJob.title}</span>
                <span className="badge badge-indigo">{currentJob.location}</span>
                <span className="badge badge-cyan">{currentJob.experienceLevel} ({currentJob.minExperienceYears}+ yrs min)</span>
              </div>
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#38bdf8' }}>{currentJob.salaryRange}</span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600 }}>Required Skills:</span>
              {currentJob.skills.split(',').map((skill, sIdx) => (
                <span key={sIdx} className="badge badge-indigo" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                  {skill.trim()}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', gap: '8px' }}>
          {['ALL', 'EXCELLENT', 'STRONG'].map(tab => (
            <button
              key={tab}
              onClick={() => setFilterLevel(tab)}
              style={{
                padding: '6px 14px',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.825rem',
                fontWeight: 600,
                border: 'none',
                cursor: 'pointer',
                background: filterLevel === tab ? 'var(--primary)' : 'rgba(255,255,255,0.05)',
                color: filterLevel === tab ? '#ffffff' : 'var(--text-muted)',
                transition: 'var(--transition)'
              }}
            >
              {tab === 'ALL' ? 'All Ranked Profiles' : `${tab} Match Only`}
            </button>
          ))}
        </div>
        <span style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
          Showing <strong>{filteredCandidates.length}</strong> evaluated candidates
        </span>
      </div>

      {/* Ranked Candidate Leaderboard */}
      {loading ? (
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          <Sparkles className="animate-spin" size={32} color="#6366f1" style={{ margin: '0 auto 1rem' }} />
          <p>Running semantic vector analysis and scoring candidates...</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {filteredCandidates.map(item => {
            const { rank, candidate, matchDetails } = item;
            const rankStyle = getRankBadge(rank);
            const scoreColor = getScoreColor(matchDetails.overallScore);

            return (
              <div
                key={candidate.id}
                className="glass-card"
                style={{
                  padding: '1.75rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '1.25rem',
                  borderLeft: rank === 1 ? '4px solid #fbbf24' : '1px solid var(--border-subtle)'
                }}
              >
                {/* Header Row */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    {/* Rank Badge */}
                    <div style={{
                      width: '44px',
                      height: '44px',
                      borderRadius: '12px',
                      background: rankStyle.bg,
                      color: rankStyle.text,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 800,
                      fontSize: '1.1rem',
                      fontFamily: 'var(--font-mono)',
                      boxShadow: 'var(--shadow-sm)'
                    }}>
                      {rankStyle.label}
                    </div>

                    {/* Candidate Info */}
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <h3 style={{ fontSize: '1.3rem', fontWeight: 700, color: '#ffffff' }}>
                          {candidate.fullName}
                        </h3>
                        <span className={`badge ${matchDetails.suitabilityLevel === 'EXCELLENT' ? 'badge-emerald' : 'badge-indigo'}`}>
                          {matchDetails.suitabilityLevel} FIT
                        </span>
                      </div>
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                        {candidate.currentTitle} • {candidate.yearsExperience} yrs exp • {candidate.highestEducation}
                      </p>
                    </div>
                  </div>

                  {/* Overall Score Badge */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '2rem', fontWeight: 800, color: scoreColor, fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                        {matchDetails.overallScore}%
                      </div>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600 }}>MATCH ACCURACY</span>
                    </div>

                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => onOpenDetailModal(candidate, matchDetails, currentJob)}
                    >
                      Deep Analysis
                      <ArrowUpRight size={15} />
                    </button>
                  </div>
                </div>

                {/* Sub-Score Bars */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                  gap: '12px',
                  background: 'rgba(255, 255, 255, 0.02)',
                  padding: '12px 16px',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-subtle)'
                }}>
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.775rem', marginBottom: '4px' }}>
                      <span style={{ color: 'var(--text-dim)' }}>Skills Alignment</span>
                      <span style={{ fontWeight: 600, color: '#6ee7b7' }}>{matchDetails.skillScore}%</span>
                    </div>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.06)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${matchDetails.skillScore}%`, height: '100%', background: 'var(--emerald-gradient)' }} />
                    </div>
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.775rem', marginBottom: '4px' }}>
                      <span style={{ color: 'var(--text-dim)' }}>Experience Match</span>
                      <span style={{ fontWeight: 600, color: '#93c5fd' }}>{matchDetails.experienceScore}%</span>
                    </div>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.06)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${Math.min(100, matchDetails.experienceScore)}%`, height: '100%', background: 'var(--cyan-gradient)' }} />
                    </div>
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.775rem', marginBottom: '4px' }}>
                      <span style={{ color: 'var(--text-dim)' }}>Education Level</span>
                      <span style={{ fontWeight: 600, color: '#c4b5fd' }}>{matchDetails.educationScore}%</span>
                    </div>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.06)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${matchDetails.educationScore}%`, height: '100%', background: 'var(--primary-gradient)' }} />
                    </div>
                  </div>

                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.775rem', marginBottom: '4px' }}>
                      <span style={{ color: 'var(--text-dim)' }}>NLP Semantic Sim</span>
                      <span style={{ fontWeight: 600, color: '#fde047' }}>{matchDetails.semanticScore}%</span>
                    </div>
                    <div style={{ height: '6px', background: 'rgba(255,255,255,0.06)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${matchDetails.semanticScore}%`, height: '100%', background: 'var(--amber-gradient)' }} />
                    </div>
                  </div>
                </div>

                {/* Skill Match Breakdown */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {/* Matched Skills */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '0.775rem', color: '#6ee7b7', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <CheckCircle size={13} /> Matched:
                    </span>
                    {matchDetails.matchedSkills.map((skill, idx) => (
                      <span key={idx} className="badge badge-emerald" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                        {skill}
                      </span>
                    ))}
                    {matchDetails.matchedSkills.length === 0 && (
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>None matched</span>
                    )}
                  </div>

                  {/* Missing Skills */}
                  {matchDetails.missingSkills.length > 0 && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                      <span style={{ fontSize: '0.775rem', color: '#fcd34d', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <AlertTriangle size={13} /> Skill Gaps:
                      </span>
                      {matchDetails.missingSkills.map((skill, idx) => (
                        <span key={idx} className="badge badge-amber" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                          {skill}
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                {/* AI Rationale Snippet */}
                <div style={{
                  fontSize: '0.85rem',
                  color: 'var(--text-muted)',
                  borderLeft: '2px solid rgba(99, 102, 241, 0.5)',
                  paddingLeft: '12px',
                  fontStyle: 'italic'
                }}>
                  "{matchDetails.recommendationRationale}"
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
