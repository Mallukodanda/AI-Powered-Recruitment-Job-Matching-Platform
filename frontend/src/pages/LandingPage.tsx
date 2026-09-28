import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Search,
  Briefcase,
  Users,
  FileCheck2,
  TrendingUp,
  MapPin,
  DollarSign,
  ArrowRight,
  Sparkles,
  CheckCircle2,
  Building2,
} from 'lucide-react';
import { jobService } from '../services/jobService';
import { analyticsService } from '../services/analyticsService';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');

  // 1. Fetch live metrics from backend
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['analytics', 'dashboard'],
    queryFn: () => analyticsService.getDashboardStats(),
    staleTime: 60000,
  });

  // 2. Fetch featured active jobs from backend
  const { data: jobs, isLoading: jobsLoading } = useQuery({
    queryKey: ['jobs', 'featured'],
    queryFn: () => jobService.getJobs(),
    staleTime: 30000,
  });

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/jobs?search=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/jobs');
    }
  };

  const featuredJobs = jobs?.slice(0, 3) || [];

  return (
    <div className="space-y-20 pb-12">
      {/* Hero Section */}
      <section className="relative text-center max-w-4xl mx-auto pt-8 sm:pt-14 space-y-6">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full border border-brand-500/30 bg-brand-500/10 text-brand-300 text-xs font-medium">
          <Sparkles className="w-3.5 h-3.5 text-brand-400" />
          <span>Intelligent Talent Acquisition Platform</span>
        </div>

        <h1 className="font-heading text-4xl sm:text-6xl font-extrabold tracking-tight text-white leading-tight sm:leading-none">
          Connect Exceptional Talent With{' '}
          <span className="gradient-text">High-Impact Roles</span>
        </h1>

        <p className="text-base sm:text-xl text-slate-400 max-w-2xl mx-auto leading-relaxed">
          Streamline hiring workflows with structured candidate evaluation, transparent pipeline tracking, and instant role matching.
        </p>

        {/* Search Bar */}
        <form
          onSubmit={handleSearchSubmit}
          className="max-w-2xl mx-auto flex flex-col sm:flex-row items-center gap-2.5 p-2 rounded-2xl glass-card border border-white/10 shadow-2xl bg-slate-900/80"
        >
          <div className="flex-1 flex items-center gap-3 px-3 w-full">
            <Search className="w-5 h-5 text-slate-400 shrink-0" />
            <input
              type="text"
              placeholder="Search by job title, skill (e.g. Spring Boot, React), or department..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-transparent border-none text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:ring-0 py-2"
            />
          </div>
          <Button type="submit" variant="primary" size="md" className="w-full sm:w-auto shrink-0">
            Search Jobs
          </Button>
        </form>

        <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
          <Link to="/jobs">
            <Button variant="outline" size="sm">
              Explore All Requisitions
            </Button>
          </Link>
          <Link to="/register?role=RECRUITER">
            <Button variant="ghost" size="sm" icon={<ArrowRight className="w-4 h-4" />}>
              Post a Requisition as Employer
            </Button>
          </Link>
        </div>
      </section>

      {/* Live Platform KPI Stats Bar */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-5xl mx-auto">
        <div className="glass-card p-5 rounded-2xl border border-white/5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-400 shrink-0">
            <Briefcase className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold font-heading text-white">
              {statsLoading ? <LoadingSpinner size="sm" /> : stats?.totalJobs ?? 0}
            </div>
            <p className="text-xs text-slate-400">Active Openings</p>
          </div>
        </div>

        <div className="glass-card p-5 rounded-2xl border border-white/5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 shrink-0">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold font-heading text-white">
              {statsLoading ? <LoadingSpinner size="sm" /> : stats?.totalCandidates ?? 0}
            </div>
            <p className="text-xs text-slate-400">Registered Candidates</p>
          </div>
        </div>

        <div className="glass-card p-5 rounded-2xl border border-white/5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 shrink-0">
            <FileCheck2 className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold font-heading text-white">
              {statsLoading ? <LoadingSpinner size="sm" /> : stats?.totalApplications ?? 0}
            </div>
            <p className="text-xs text-slate-400">Applications Handled</p>
          </div>
        </div>

        <div className="glass-card p-5 rounded-2xl border border-white/5 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-bold font-heading text-white">
              {statsLoading ? (
                <LoadingSpinner size="sm" />
              ) : stats?.shortlistedCount ? (
                `${stats.shortlistedCount}`
              ) : (
                'Active'
              )}
            </div>
            <p className="text-xs text-slate-400">Candidates Shortlisted</p>
          </div>
        </div>
      </section>

      {/* Featured Jobs Section */}
      <section className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
          <div>
            <span className="text-xs font-semibold text-brand-400 uppercase tracking-wider font-mono">
              Live Requisitions
            </span>
            <h2 className="text-2xl sm:text-3xl font-bold font-heading text-white mt-1">
              Featured Engineering Opportunities
            </h2>
          </div>
          <Link to="/jobs">
            <Button variant="outline" size="sm" icon={<ArrowRight className="w-4 h-4" />}>
              View All Openings
            </Button>
          </Link>
        </div>

        {jobsLoading ? (
          <div className="p-12 text-center glass-card">
            <LoadingSpinner size="lg" />
            <p className="text-sm text-slate-400 mt-3">Loading live opportunities...</p>
          </div>
        ) : featuredJobs.length === 0 ? (
          <div className="p-12 text-center glass-card border border-white/5 rounded-2xl">
            <Building2 className="w-10 h-10 text-slate-600 mx-auto mb-3" />
            <h3 className="text-base font-semibold text-slate-200">No Active Jobs Right Now</h3>
            <p className="text-sm text-slate-500 mt-1">Check back shortly or post a new job opening.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {featuredJobs.map((job) => (
              <div
                key={job.id}
                className="glass-card p-6 rounded-2xl border border-white/5 flex flex-col justify-between hover:border-brand-500/30 transition-all group"
              >
                <div className="space-y-4">
                  <div className="flex items-start justify-between gap-2">
                    <Badge variant="indigo" size="sm">
                      {job.department || 'Engineering'}
                    </Badge>
                    <Badge variant="emerald" size="sm">
                      {job.jobType || 'FULL_TIME'}
                    </Badge>
                  </div>

                  <div>
                    <h3 className="text-lg font-bold text-white group-hover:text-brand-300 transition-colors line-clamp-1">
                      {job.title}
                    </h3>
                    <p className="text-xs text-slate-400 mt-1 line-clamp-2 leading-relaxed">
                      {job.description}
                    </p>
                  </div>

                  <div className="space-y-1.5 text-xs text-slate-400 pt-2 border-t border-white/5">
                    <div className="flex items-center gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-slate-500" />
                      <span>{job.location || 'Remote'}</span>
                    </div>
                    {job.salaryRange && (
                      <div className="flex items-center gap-1.5 text-emerald-400 font-medium">
                        <DollarSign className="w-3.5 h-3.5" />
                        <span>{job.salaryRange}</span>
                      </div>
                    )}
                  </div>

                  {job.skills && (
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {job.skills.split(',').slice(0, 3).map((skill, index) => (
                        <span
                          key={index}
                          className="text-[11px] px-2 py-0.5 rounded-md bg-slate-800/80 text-slate-300 border border-slate-700/50"
                        >
                          {skill.trim()}
                        </span>
                      ))}
                    </div>
                  )}
                </div>

                <div className="pt-6 mt-4 border-t border-white/5">
                  <Link to={`/jobs/${job.id}`} className="block">
                    <Button variant="secondary" size="sm" className="w-full">
                      View Position
                    </Button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Role Segmentation Cards */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-5xl mx-auto pt-6">
        <div className="glass-card p-8 rounded-3xl border border-brand-500/20 bg-gradient-to-br from-brand-950/30 to-slate-900/60 relative overflow-hidden space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-brand-500/20 text-brand-400 flex items-center justify-center">
            <Users className="w-6 h-6" />
          </div>
          <h3 className="text-xl font-bold font-heading text-white">For Candidates</h3>
          <p className="text-sm text-slate-300 leading-relaxed">
            Create an end-to-end professional profile, showcase certified skills and projects, and monitor every stage of your job applications with total transparency.
          </p>
          <ul className="space-y-2 text-xs text-slate-400">
            <li className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              Direct application submission with zero intermediaries
            </li>
            <li className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              Real-time pipeline stage visibility (Screening, Interview, Offer)
            </li>
          </ul>
          <div className="pt-3">
            <Link to="/register?role=CANDIDATE">
              <Button variant="primary" size="sm" icon={<ArrowRight className="w-4 h-4" />}>
                Create Candidate Account
              </Button>
            </Link>
          </div>
        </div>

        <div className="glass-card p-8 rounded-3xl border border-purple-500/20 bg-gradient-to-br from-purple-950/30 to-slate-900/60 relative overflow-hidden space-y-4">
          <div className="w-12 h-12 rounded-2xl bg-purple-500/20 text-purple-400 flex items-center justify-center">
            <Building2 className="w-6 h-6" />
          </div>
          <h3 className="text-xl font-bold font-heading text-white">For Recruiters &amp; Employers</h3>
          <p className="text-sm text-slate-300 leading-relaxed">
            Publish comprehensive job requisitions, review qualified applicants with standardized credential cards, and drive fast interview scheduling decisions.
          </p>
          <ul className="space-y-2 text-xs text-slate-400">
            <li className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-purple-400 shrink-0" />
              Centralized requisition and candidate review dashboard
            </li>
            <li className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-purple-400 shrink-0" />
              Role-based access control protecting company resources
            </li>
          </ul>
          <div className="pt-3">
            <Link to="/register?role=RECRUITER">
              <Button variant="secondary" size="sm" icon={<ArrowRight className="w-4 h-4" />}>
                Register Company Account
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};
