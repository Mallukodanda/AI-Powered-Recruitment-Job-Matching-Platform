import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { Briefcase, Shield, CheckCircle } from 'lucide-react';

export const RootLayout: React.FC = () => {
  return (
    <div className="min-h-screen flex flex-col bg-[#090d16] text-slate-100 selection:bg-brand-500 selection:text-white">
      {/* Top Navbar */}
      <Navbar />

      {/* Main Content Area */}
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="border-t border-white/5 bg-slate-950/40 backdrop-blur-md py-12 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center">
                  <Briefcase className="w-4 h-4 text-white" />
                </div>
                <span className="font-heading font-bold text-lg text-white">TalentPulse</span>
              </div>
              <p className="text-sm text-slate-400 leading-relaxed">
                Enterprise talent acquisition and hiring pipeline platform designed for high-growth engineering teams.
              </p>
            </div>

            <div>
              <h4 className="text-sm font-semibold text-slate-200 mb-3 tracking-wider uppercase">Candidates</h4>
              <ul className="space-y-2 text-sm text-slate-400">
                <li><Link to="/jobs" className="hover:text-white transition-colors">Browse Job Openings</Link></li>
                <li><Link to="/candidate/dashboard" className="hover:text-white transition-colors">Application Tracker</Link></li>
                <li><Link to="/candidate/profile" className="hover:text-white transition-colors">Profile & Resume</Link></li>
              </ul>
            </div>

            <div>
              <h4 className="text-sm font-semibold text-slate-200 mb-3 tracking-wider uppercase">Employers</h4>
              <ul className="space-y-2 text-sm text-slate-400">
                <li><Link to="/recruiter/dashboard" className="hover:text-white transition-colors">Recruiter Dashboard</Link></li>
                <li><Link to="/recruiter/jobs/new" className="hover:text-white transition-colors">Post Requisitions</Link></li>
                <li><Link to="/login" className="hover:text-white transition-colors">Employer Login</Link></li>
              </ul>
            </div>

            <div>
              <h4 className="text-sm font-semibold text-slate-200 mb-3 tracking-wider uppercase">Security & Compliance</h4>
              <ul className="space-y-2 text-sm text-slate-400">
                <li className="flex items-center gap-1.5"><Shield className="w-4 h-4 text-emerald-400" /> RBAC Protected</li>
                <li className="flex items-center gap-1.5"><CheckCircle className="w-4 h-4 text-emerald-400" /> Stateless JWT Sessions</li>
              </ul>
            </div>
          </div>

          <div className="pt-8 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-400 gap-4">
            <p>&copy; {new Date().getFullYear()} TalentPulse Platform. All rights reserved.</p>
            <p className="flex items-center gap-1">
              Built with Spring Boot &amp; React
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};
