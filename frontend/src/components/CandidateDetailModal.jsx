import React, { useState } from 'react';
import { X, CheckCircle2, AlertTriangle, Calendar, Mail, Phone, MapPin, Award, Send } from 'lucide-react';

export default function CandidateDetailModal({ candidate, matchDetails, job, onClose, onScheduleInterview }) {
  const [interviewDate, setInterviewDate] = useState('');
  const [interviewTime, setInterviewTime] = useState('10:00');
  const [notes, setNotes] = useState('');
  const [scheduled, setScheduled] = useState(false);

  if (!candidate) return null;

  const handleSchedule = (e) => {
    e.preventDefault();
    if (!interviewDate) return;
    if (onScheduleInterview) {
      onScheduleInterview(candidate.id, `${interviewDate}T${interviewTime}:00`, notes);
    }
    setScheduled(true);
    setTimeout(() => {
      onClose();
    }, 1500);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ padding: '2rem' }}>
        
        {/* Modal Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
              <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{candidate.fullName}</h2>
              {matchDetails && (
                <span className="badge badge-emerald">
                  {matchDetails.overallScore}% MATCH
                </span>
              )}
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              {candidate.currentTitle} • {candidate.yearsExperience} yrs experience • {candidate.highestEducation}
            </p>
          </div>

          <button
            onClick={onClose}
            style={{
              background: 'rgba(255,255,255,0.06)',
              border: 'none',
              borderRadius: '50%',
              width: '34px',
              height: '34px',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Contact Strip */}
        <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '1.5rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Mail size={14} color="#818cf8" /> {candidate.email}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Phone size={14} color="#818cf8" /> {candidate.phone || '+1 (555) 012-3456'}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <MapPin size={14} color="#818cf8" /> {candidate.location || 'Remote'}
          </span>
        </div>

        {/* AI Match Explanation */}
        {matchDetails && (
          <div style={{
            background: 'rgba(99, 102, 241, 0.08)',
            border: '1px solid rgba(99, 102, 241, 0.25)',
            borderRadius: 'var(--radius-md)',
            padding: '1.25rem',
            marginBottom: '1.5rem'
          }}>
            <h4 style={{ fontSize: '0.9rem', color: '#c7d2fe', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Award size={16} /> AI Suitability Assessment for {job?.title}
            </h4>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-main)', lineHeight: 1.5 }}>
              {matchDetails.recommendationRationale}
            </p>
          </div>
        )}

        {/* Skills Matched and Gaps */}
        {matchDetails && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
            <div style={{ background: 'rgba(16, 185, 129, 0.05)', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: '0.8rem', color: '#6ee7b7', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                <CheckCircle2 size={14} /> Confirmed Skills
              </span>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                {matchDetails.matchedSkills.map((s, i) => (
                  <span key={i} className="badge badge-emerald" style={{ fontSize: '0.7rem' }}>{s}</span>
                ))}
              </div>
            </div>

            <div style={{ background: 'rgba(245, 158, 11, 0.05)', border: '1px solid rgba(245, 158, 11, 0.2)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <span style={{ fontSize: '0.8rem', color: '#fcd34d', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                <AlertTriangle size={14} /> Missing Skills
              </span>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                {matchDetails.missingSkills.map((s, i) => (
                  <span key={i} className="badge badge-amber" style={{ fontSize: '0.7rem' }}>{s}</span>
                ))}
                {matchDetails.missingSkills.length === 0 && (
                  <span style={{ fontSize: '0.75rem', color: '#6ee7b7' }}>None (Full match)</span>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Bio / Summary */}
        <div style={{ marginBottom: '1.75rem' }}>
          <h4 style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Candidate Background</h4>
          <p style={{ fontSize: '0.875rem', color: '#cbd5e1', lineHeight: 1.6, background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
            {candidate.bio || candidate.resumeText || 'No bio provided'}
          </p>
        </div>

        {/* Interview Scheduler Form */}
        <div style={{
          background: 'rgba(255, 255, 255, 0.03)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          padding: '1.25rem'
        }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Calendar size={17} color="#6366f1" /> Schedule Interview &amp; Advance Candidate
          </h4>

          {scheduled ? (
            <div style={{ padding: '1rem', textAlign: 'center', color: '#6ee7b7', fontWeight: 600 }}>
              Interview successfully scheduled! Updating pipeline...
            </div>
          ) : (
            <form onSubmit={handleSchedule} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-dim)', display: 'block', marginBottom: '4px' }}>Date</label>
                  <input
                    type="date"
                    required
                    value={interviewDate}
                    onChange={(e) => setInterviewDate(e.target.value)}
                    className="input-field"
                    style={{ fontSize: '0.85rem' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-dim)', display: 'block', marginBottom: '4px' }}>Time</label>
                  <input
                    type="time"
                    required
                    value={interviewTime}
                    onChange={(e) => setInterviewTime(e.target.value)}
                    className="input-field"
                    style={{ fontSize: '0.85rem' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-dim)', display: 'block', marginBottom: '4px' }}>Interviewer Notes</label>
                <input
                  type="text"
                  placeholder="e.g. Focus on Spring Boot microservice design and system architecture"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  className="input-field"
                  style={{ fontSize: '0.85rem' }}
                />
              </div>

              <button type="submit" className="btn btn-emerald" style={{ marginTop: '6px' }}>
                <Send size={15} /> Confirm &amp; Send Calendar Invite
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
