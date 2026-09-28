import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Briefcase, User, LogOut, ArrowRight, ShieldCheck } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import { Button } from './common/Button';
import { Badge } from './common/Badge';

export const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 glass-panel border-b border-white/5 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-purple-600 flex items-center justify-center shadow-lg shadow-brand-500/25 group-hover:scale-105 transition-transform">
            <Briefcase className="w-5 h-5 text-white" />
          </div>
          <div>
            <span className="font-heading font-bold text-xl tracking-tight text-white flex items-center gap-1.5">
              TalentPulse
              <span className="inline-block w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
            </span>
            <span className="text-[10px] text-slate-400 block -mt-1 tracking-wider uppercase font-mono">
              Enterprise Recruitment
            </span>
          </div>
        </Link>

        {/* Center Nav Links */}
        <nav className="hidden md:flex items-center gap-1 text-sm font-medium text-slate-300">
          <Link
            to="/jobs"
            className="px-3 py-2 rounded-lg hover:text-white hover:bg-white/5 transition-colors"
          >
            Find Jobs
          </Link>

          {isAuthenticated && user?.role === 'CANDIDATE' && (
            <>
              <Link
                to="/candidate/dashboard"
                className="px-3 py-2 rounded-lg hover:text-white hover:bg-white/5 transition-colors"
              >
                Candidate Dashboard
              </Link>
              <Link
                to="/candidate/profile"
                className="px-3 py-2 rounded-lg hover:text-white hover:bg-white/5 transition-colors"
              >
                My Profile
              </Link>
            </>
          )}

          {isAuthenticated && (user?.role === 'RECRUITER' || user?.role === 'ADMIN') && (
            <>
              <Link
                to="/recruiter/dashboard"
                className="px-3 py-2 rounded-lg hover:text-white hover:bg-white/5 transition-colors"
              >
                Recruiter Console
              </Link>
              <Link
                to="/recruiter/jobs/new"
                className="px-3 py-2 rounded-lg hover:text-white hover:bg-white/5 transition-colors"
              >
                Post Job
              </Link>
              {user?.role === 'ADMIN' && (
                <Link
                  to="/admin"
                  className="px-3 py-2 rounded-lg text-indigo-400 hover:text-indigo-300 hover:bg-indigo-500/10 transition-colors font-semibold flex items-center gap-1.5"
                >
                  <ShieldCheck className="w-4 h-4" />
                  Admin Console
                </Link>
              )}
            </>
          )}
        </nav>

        {/* Right CTA / Auth Status */}
        <div className="flex items-center gap-3">
          {isAuthenticated && user ? (
            <div className="flex items-center gap-3">
              <div className="hidden sm:flex flex-col items-end">
                <span className="text-sm font-medium text-slate-200">{user.fullName}</span>
                <Badge
                  variant={
                    user.role === 'ADMIN' ? 'rose' : user.role === 'RECRUITER' ? 'amber' : 'emerald'
                  }
                  size="sm"
                >
                  {user.role}
                </Badge>
              </div>

              <button
                onClick={handleLogout}
                className="p-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/20 transition-all"
                title="Sign out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login">
                <Button variant="ghost" size="sm">
                  Sign In
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="sm" icon={<ArrowRight className="w-4 h-4" />}>
                  Get Started
                </Button>
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
