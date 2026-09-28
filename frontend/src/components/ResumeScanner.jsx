import React, { useState } from 'react';
import { FileText, Sparkles, Upload, CheckCircle2, AlertTriangle, Lightbulb, ArrowRight, Play, RefreshCw } from 'lucide-react';
import { api } from '../services/api';

const SAMPLE_RESUMES = {
  java: `Alex Rivera
Senior Software Engineer
Email: alex.rivera@example.com | Phone: +1 555-0192 | New York, NY
Education: M.S. in Computer Science

SUMMARY:
Results-driven Senior Full-Stack Software Engineer with 6+ years of experience architecting resilient distributed systems, Spring Boot microservices, and reactive user interfaces with React and TypeScript. Proven track record in high-traffic fintech environments.

TECHNICAL SKILLS:
- Languages & Frameworks: Java 17, Spring Boot, React, TypeScript, JavaScript, HTML5, CSS3, REST API
- Cloud & Infrastructure: Docker, Kubernetes, AWS, PostgreSQL, Redis, Microservices, Git, CI/CD
- Architecture: Clean Architecture, Event-Driven Systems, Unit & Integration Testing

EXPERIENCE:
Senior Full Stack Engineer | Apex Cloud Solutions (2020 - Present)
- Engineered scalable microservices with Spring Boot processing 20M+ API requests daily.
- Built reusable component library in React & TypeScript, boosting team development velocity by 35%.
- Migrated legacy database to PostgreSQL with zero-downtime replication.`,

  ai: `Dr. Priya Sharma
Lead AI & NLP Research Engineer
Email: priya.sharma@example.com | San Francisco, CA
Education: Ph.D. in Artificial Intelligence

SUMMARY:
Artificial Intelligence Specialist with 5+ years of experience in Natural Language Processing, transformer fine-tuning, large language models (LLMs), and semantic retrieval systems.

TECHNICAL SKILLS:
- AI / ML: Python, PyTorch, Transformers, LLMs, Vector Databases, Semantic Search, NLP, TensorFlow, LangChain
- Backend & Cloud: FastAPI, Docker, PostgreSQL, REST API, Git
- Research: Author of 4 published papers on sparse attention in large language models.`,

  devops: `Marcus Vance
Senior Cloud Infrastructure & DevOps Architect
Email: marcus.vance@example.com | Austin, TX
Education: B.S. in Software Engineering

SUMMARY:
DevOps Architect with 7+ years optimizing enterprise cloud infrastructures. Expert in Kubernetes orchestration, Terraform infrastructure-as-code, and continuous integration pipelines.

TECHNICAL SKILLS:
- Cloud & DevOps: AWS, Kubernetes, Docker, Terraform, CI/CD, Prometheus, Grafana, Linux, Helm
- Scripting & Languages: Python, Bash, Java, Git, Microservices`
};

export default function ResumeScanner({ jobs }) {
  const [selectedJobId, setSelectedJobId] = useState(jobs[0]?.id || 1);
  const [resumeText, setResumeText] = useState(SAMPLE_RESUMES.java);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);

  const currentJob = jobs.find(j => j.id === Number(selectedJobId)) || jobs[0];

  const handleRunAnalysis = async () => {
    if (!resumeText.trim()) return;
    setAnalyzing(true);
    try {
      const result = await api.analyzeMatch(currentJob.id, resumeText);
      setAnalysisResult(result);
    } catch (err) {
      console.error('Failed to run AI analysis', err);
    } finally {
      setAnalyzing(false);
    }
  };

  const loadPreset = (key) => {
    setResumeText(SAMPLE_RESUMES[key]);
    setAnalysisResult(null);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Header */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
          <Sparkles size={22} color="#818cf8" />
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>AI Resume Scanner &amp; Skill Gap Analyzer</h1>
        </div>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          Paste or upload any candidate resume against any open requisition to generate instant fit scores, semantic breakdown, and upskilling guidance.
        </p>
      </div>

      {/* Main Grid: Input on Left, Results on Right */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '2rem' }}>
        
        {/* Left Column: Input Form */}
        <div className="glass-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          
          {/* Target Role Selector */}
          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '6px' }}>
              Target Job Requisition
            </label>
            <select
              value={selectedJobId}
              onChange={(e) => {
                setSelectedJobId(Number(e.target.value));
                setAnalysisResult(null);
              }}
              className="input-field"
              style={{ fontWeight: 600 }}
            >
              {jobs.map(job => (
                <option key={job.id} value={job.id}>
                  {job.title} — {job.department} ({job.salaryRange || 'Competitive'})
                </option>
              ))}
            </select>
          </div>

          {/* Quick Presets */}
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)', fontWeight: 600, display: 'block', marginBottom: '6px' }}>
              Or Load Sample Profile:
            </span>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <button className="btn btn-secondary btn-sm" onClick={() => loadPreset('java')}>
                Full-Stack Java Lead
              </button>
              <button className="btn btn-secondary btn-sm" onClick={() => loadPreset('ai')}>
                AI / NLP Researcher
              </button>
              <button className="btn btn-secondary btn-sm" onClick={() => loadPreset('devops')}>
                DevOps / AWS Architect
              </button>
            </div>
          </div>

          {/* Resume Text Input Area */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>
                Candidate Resume Text
              </label>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                {resumeText.length} characters
              </span>
            </div>
            <textarea
              className="input-field textarea-field"
              rows={14}
              value={resumeText}
              onChange={(e) => setResumeText(e.target.value)}
              placeholder="Paste candidate resume or raw CV text here..."
              style={{ fontFamily: 'var(--font-mono)', fontSize: '0.825rem' }}
            />
          </div>

          {/* Action Button */}
          <button
            className="btn btn-primary"
            onClick={handleRunAnalysis}
            disabled={analyzing || !resumeText.trim()}
            style={{ width: '100%', padding: '14px' }}
          >
            {analyzing ? (
              <>
                <RefreshCw className="animate-spin" size={18} />
                Executing NLP Semantic Model...
              </>
            ) : (
              <>
                <Play size={18} />
                Run AI Deep Match Analysis
              </>
            )}
          </button>
        </div>

        {/* Right Column: Analysis Results */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {analysisResult ? (
            <div className="glass-card animate-fade-in" style={{ padding: '2rem', display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
              
              {/* Overall Score Header */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: '1.5rem',
                borderBottom: '1px solid var(--border-subtle)'
              }}>
                <div>
                  <span className={`badge ${analysisResult.overallScore >= 80 ? 'badge-emerald' : 'badge-indigo'}`} style={{ marginBottom: '8px' }}>
                    {analysisResult.suitabilityLevel} FIT
                  </span>
                  <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>Evaluation Summary</h2>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-dim)' }}>
                    Targeted against: <strong>{currentJob.title}</strong>
                  </p>
                </div>

                <div style={{ textAlign: 'center' }}>
                  <div style={{
                    width: '84px',
                    height: '84px',
                    borderRadius: '50%',
                    background: 'radial-gradient(circle, rgba(99,102,241,0.2) 0%, rgba(15,23,42,1) 80%)',
                    border: '3px solid var(--primary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: 'var(--shadow-glow)'
                  }}>
                    <span style={{ fontSize: '1.8rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: '#ffffff' }}>
                      {analysisResult.overallScore}%
                    </span>
                  </div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', fontWeight: 600, display: 'block', marginTop: '4px' }}>
                    OVERALL MATCH
                  </span>
                </div>
              </div>

              {/* Sub-Metric Score Bars */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <h4 style={{ fontSize: '0.925rem', color: 'var(--text-muted)' }}>Multi-Factor Score Vector</h4>
                
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.825rem', marginBottom: '4px' }}>
                    <span>Technical Skills Relevance (45% wt)</span>
                    <span style={{ fontWeight: 700, color: '#6ee7b7' }}>{analysisResult.skillScore}%</span>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.06)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${analysisResult.skillScore}%`, height: '100%', background: 'var(--emerald-gradient)' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.825rem', marginBottom: '4px' }}>
                    <span>Seniority &amp; Experience (25% wt)</span>
                    <span style={{ fontWeight: 700, color: '#93c5fd' }}>{analysisResult.experienceScore}%</span>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.06)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${analysisResult.experienceScore}%`, height: '100%', background: 'var(--cyan-gradient)' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.825rem', marginBottom: '4px' }}>
                    <span>Education &amp; Credentials (15% wt)</span>
                    <span style={{ fontWeight: 700, color: '#c4b5fd' }}>{analysisResult.educationScore}%</span>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.06)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${analysisResult.educationScore}%`, height: '100%', background: 'var(--primary-gradient)' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.825rem', marginBottom: '4px' }}>
                    <span>NLP Semantic Proximity (15% wt)</span>
                    <span style={{ fontWeight: 700, color: '#fde047' }}>{analysisResult.semanticScore}%</span>
                  </div>
                  <div style={{ height: '8px', background: 'rgba(255,255,255,0.06)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${analysisResult.semanticScore}%`, height: '100%', background: 'var(--amber-gradient)' }} />
                  </div>
                </div>
              </div>

              {/* Matched vs Missing Skills Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div style={{
                  background: 'rgba(16, 185, 129, 0.05)',
                  border: '1px solid rgba(16, 185, 129, 0.2)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1rem'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#6ee7b7', fontWeight: 700, fontSize: '0.85rem', marginBottom: '8px' }}>
                    <CheckCircle2 size={16} />
                    <span>Skills Matched ({analysisResult.matchedSkills.length})</span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                    {analysisResult.matchedSkills.map((s, i) => (
                      <span key={i} className="badge badge-emerald" style={{ fontSize: '0.725rem', padding: '2px 8px' }}>
                        {s}
                      </span>
                    ))}
                    {analysisResult.matchedSkills.length === 0 && (
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>None identified</span>
                    )}
                  </div>
                </div>

                <div style={{
                  background: 'rgba(245, 158, 11, 0.05)',
                  border: '1px solid rgba(245, 158, 11, 0.2)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1rem'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#fcd34d', fontWeight: 700, fontSize: '0.85rem', marginBottom: '8px' }}>
                    <AlertTriangle size={16} />
                    <span>Skill Gaps ({analysisResult.missingSkills.length})</span>
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                    {analysisResult.missingSkills.map((s, i) => (
                      <span key={i} className="badge badge-amber" style={{ fontSize: '0.725rem', padding: '2px 8px' }}>
                        {s}
                      </span>
                    ))}
                    {analysisResult.missingSkills.length === 0 && (
                      <span style={{ fontSize: '0.8rem', color: '#6ee7b7' }}>All required skills present!</span>
                    )}
                  </div>
                </div>
              </div>

              {/* AI Narrative Rationale */}
              <div style={{
                background: 'rgba(99, 102, 241, 0.08)',
                border: '1px solid rgba(99, 102, 241, 0.25)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#a5b4fc', fontWeight: 700, fontSize: '0.9rem', marginBottom: '6px' }}>
                  <Lightbulb size={18} />
                  <span>AI Recruiter Analysis Rationale</span>
                </div>
                <p style={{ color: 'var(--text-main)', fontSize: '0.9rem', lineHeight: 1.6 }}>
                  {analysisResult.recommendationRationale}
                </p>
              </div>

              {/* Recommended Next Steps */}
              {analysisResult.recommendedNextSteps?.length > 0 && (
                <div>
                  <h4 style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '8px' }}>Recommended Actions</h4>
                  <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    {analysisResult.recommendedNextSteps.map((step, idx) => (
                      <li key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem', color: '#cbd5e1' }}>
                        <ArrowRight size={14} color="#6366f1" />
                        <span>{step}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <div className="glass-card" style={{
              padding: '3rem 2rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: '400px',
              border: '2px dashed var(--border-subtle)'
            }}>
              <div style={{
                width: '60px',
                height: '60px',
                borderRadius: '50%',
                background: 'rgba(99, 102, 241, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: '1rem'
              }}>
                <Sparkles size={28} color="#818cf8" />
              </div>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Ready for Analysis</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', maxWidth: '340px' }}>
                Select a target role on the left and click "Run AI Deep Match Analysis" to generate the multi-factor report.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
