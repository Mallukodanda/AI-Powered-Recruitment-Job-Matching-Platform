import React, { useState } from 'react';
import { Briefcase, MapPin, DollarSign, Search, PlusCircle, ArrowRight, Zap } from 'lucide-react';

export default function JobManagement({ jobs, onOpenNewJobModal, onSelectJobForRanking }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDept, setSelectedDept] = useState('ALL');

  const departments = ['ALL', ...new Set(jobs.map(j => j.department))];

  const filteredJobs = jobs.filter(job => {
    const matchesSearch = job.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          job.skills.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          job.location.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesDept = selectedDept === 'ALL' || job.department === selectedDept;
    return matchesSearch && matchesDept;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Job Requisitions &amp; Role Definitions</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Manage requisitions, configure required skills, and run candidate ranking algorithms.
          </p>
        </div>

        <button className="btn btn-primary" onClick={onOpenNewJobModal}>
          <PlusCircle size={18} />
          Create New Requisition
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-card" style={{ padding: '1.25rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ flex: 1, minWidth: '260px', position: 'relative' }}>
          <Search size={18} color="var(--text-dim)" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            className="input-field"
            placeholder="Search by role title, skill, or location..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ paddingLeft: '40px' }}
          />
        </div>

        <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
          {departments.map(dept => (
            <button
              key={dept}
              onClick={() => setSelectedDept(dept)}
              style={{
                padding: '8px 16px',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.825rem',
                fontWeight: 600,
                border: 'none',
                cursor: 'pointer',
                background: selectedDept === dept ? 'var(--primary)' : 'rgba(255,255,255,0.05)',
                color: selectedDept === dept ? '#ffffff' : 'var(--text-muted)',
                transition: 'var(--transition)'
              }}
            >
              {dept === 'ALL' ? 'All Departments' : dept}
            </button>
          ))}
        </div>
      </div>

      {/* Jobs Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.5rem' }}>
        {filteredJobs.map(job => (
          <div
            key={job.id}
            className="glass-card"
            style={{
              padding: '1.75rem',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              gap: '1.25rem'
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.85rem' }}>
                <span className="badge badge-indigo">{job.department}</span>
                <span className="badge badge-cyan">{job.experienceLevel}</span>
              </div>

              <h2 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#ffffff', marginBottom: '0.5rem' }}>
                {job.title}
              </h2>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', fontSize: '0.825rem', color: 'var(--text-dim)', marginBottom: '1rem' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <MapPin size={14} /> {job.location}
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#38bdf8' }}>
                  <DollarSign size={14} /> {job.salaryRange || 'Competitive'}
                </span>
              </div>

              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.25rem', lineHeight: 1.5 }}>
                {job.description}
              </p>

              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)', fontWeight: 600, display: 'block', marginBottom: '6px' }}>
                  Target Technical Stack:
                </span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {job.skills.split(',').map((skill, idx) => (
                    <span key={idx} className="badge badge-indigo" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              paddingTop: '1rem',
              borderTop: '1px solid var(--border-subtle)'
            }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                Minimum {job.minExperienceYears}+ yrs required
              </span>
              <button
                className="btn btn-primary btn-sm"
                onClick={() => onSelectJobForRanking(job.id)}
              >
                <Zap size={14} />
                Rank Candidates
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
