import React, { useState, useEffect } from 'react';
import { adminService, PagedResponse } from '../services/adminService';
import {
  AuditLog,
  Company,
  Job,
  PlatformAnalytics,
  PlatformConfig,
  Role,
  UserResponse,
} from '../types';

export const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<
    'analytics' | 'users' | 'companies' | 'jobs' | 'audit' | 'configs'
  >('analytics');

  // Analytics state
  const [analytics, setAnalytics] = useState<PlatformAnalytics | null>(null);
  const [loadingAnalytics, setLoadingAnalytics] = useState(true);

  // Users state
  const [users, setUsers] = useState<PagedResponse<UserResponse> | null>(null);
  const [selectedRole, setSelectedRole] = useState<Role | undefined>(undefined);
  const [userPage, setUserPage] = useState(0);

  // Companies state
  const [companies, setCompanies] = useState<PagedResponse<Company> | null>(null);
  const [newCompanyName, setNewCompanyName] = useState('');
  const [newCompanyIndustry, setNewCompanyIndustry] = useState('');

  // Jobs state
  const [jobs, setJobs] = useState<PagedResponse<Job> | null>(null);
  const [jobPage, setJobPage] = useState(0);

  // Audit state
  const [auditLogs, setAuditLogs] = useState<PagedResponse<AuditLog> | null>(null);
  const [auditPage, setAuditPage] = useState(0);

  // Configs state
  const [configs, setConfigs] = useState<PlatformConfig[]>([]);
  const [editingConfigKey, setEditingConfigKey] = useState<string | null>(null);
  const [editingConfigVal, setEditingConfigVal] = useState<string>('');

  const [notificationMsg, setNotificationMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const showNotification = (text: string, type: 'success' | 'error' = 'success') => {
    setNotificationMsg({ text, type });
    setTimeout(() => setNotificationMsg(null), 4000);
  };

  // Initial Load
  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    setLoadingAnalytics(true);
    try {
      const data = await adminService.getPlatformAnalytics();
      setAnalytics(data);
    } catch (err: any) {
      showNotification(err?.response?.data?.message || 'Failed to load platform analytics', 'error');
    } finally {
      setLoadingAnalytics(false);
    }
  };

  const loadUsers = async () => {
    try {
      const data = await adminService.getUsers(userPage, 10, selectedRole);
      setUsers(data);
    } catch (err: any) {
      showNotification('Failed to load user list', 'error');
    }
  };

  const loadCompanies = async () => {
    try {
      const data = await adminService.getCompanies(0, 15);
      setCompanies(data);
    } catch (err: any) {
      showNotification('Failed to load companies', 'error');
    }
  };

  const loadJobs = async () => {
    try {
      const data = await adminService.getJobs(jobPage, 10);
      setJobs(data);
    } catch (err: any) {
      showNotification('Failed to load jobs', 'error');
    }
  };

  const loadAuditLogs = async () => {
    try {
      const data = await adminService.getAuditLogs(auditPage, 15);
      setAuditLogs(data);
    } catch (err: any) {
      showNotification('Failed to load audit logs', 'error');
    }
  };

  const loadConfigs = async () => {
    try {
      const data = await adminService.getConfigs();
      setConfigs(data);
    } catch (err: any) {
      showNotification('Failed to load platform configs', 'error');
    }
  };

  useEffect(() => {
    if (activeTab === 'analytics') loadAnalytics();
    if (activeTab === 'users') loadUsers();
    if (activeTab === 'companies') loadCompanies();
    if (activeTab === 'jobs') loadJobs();
    if (activeTab === 'audit') loadAuditLogs();
    if (activeTab === 'configs') loadConfigs();
  }, [activeTab, userPage, selectedRole, jobPage, auditPage]);

  // Actions
  const handleRoleChange = async (userId: number, role: Role) => {
    try {
      await adminService.updateUserRole(userId, role);
      showNotification('User role successfully updated');
      loadUsers();
    } catch (err: any) {
      showNotification(err?.response?.data?.message || 'Failed to update user role', 'error');
    }
  };

  const handleStatusToggle = async (userId: number, currentActive: boolean) => {
    try {
      await adminService.updateUserStatus(userId, !currentActive);
      showNotification(`User account ${!currentActive ? 'activated' : 'deactivated'}`);
      loadUsers();
    } catch (err: any) {
      showNotification(err?.response?.data?.message || 'Failed to update status', 'error');
    }
  };

  const handleCreateCompany = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCompanyName.trim()) return;
    try {
      await adminService.createCompany({
        name: newCompanyName.trim(),
        industry: newCompanyIndustry.trim() || 'Technology',
      });
      setNewCompanyName('');
      setNewCompanyIndustry('');
      showNotification('Company registered successfully');
      loadCompanies();
    } catch (err: any) {
      showNotification(err?.response?.data?.message || 'Failed to create company', 'error');
    }
  };

  const handleVerifyCompany = async (companyId: number, currentVerified: boolean) => {
    try {
      await adminService.verifyCompany(companyId, !currentVerified);
      showNotification(`Company verification set to ${!currentVerified}`);
      loadCompanies();
    } catch (err: any) {
      showNotification('Failed to update company verification', 'error');
    }
  };

  const handleJobStatusChange = async (jobId: number, newStatus: string) => {
    try {
      await adminService.updateJobStatus(jobId, newStatus);
      showNotification(`Job requisition status updated to ${newStatus}`);
      loadJobs();
    } catch (err: any) {
      showNotification('Failed to update job status', 'error');
    }
  };

  const handleSaveConfig = async (key: string) => {
    try {
      await adminService.updateConfig(key, editingConfigVal);
      showNotification(`Configuration '${key}' updated to '${editingConfigVal}'`);
      setEditingConfigKey(null);
      loadConfigs();
    } catch (err: any) {
      showNotification(err?.response?.data?.message || 'Failed to update configuration', 'error');
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-8">
      {/* Toast Notification */}
      {notificationMsg && (
        <div
          className={`fixed top-5 right-5 z-50 px-4 py-3 rounded-lg shadow-xl text-sm font-medium transition-all ${
            notificationMsg.type === 'success'
              ? 'bg-emerald-600 text-white'
              : 'bg-rose-600 text-white'
          }`}
        >
          {notificationMsg.text}
        </div>
      )}

      {/* Header */}
      <div className="max-w-7xl mx-auto mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="flex items-center gap-3">
            <span className="inline-block p-2 rounded-lg bg-indigo-500/10 text-indigo-400 font-bold border border-indigo-500/20">
              SYS-ADMIN
            </span>
            <h1 className="text-3xl font-extrabold tracking-tight text-white">
              Platform Administration &amp; Telemetry
            </h1>
          </div>
          <p className="text-slate-400 text-sm mt-1">
            Real-time observability, security audit logs, organizational governance, and AI intelligence controls.
          </p>
        </div>
        <button
          onClick={() => {
            if (activeTab === 'analytics') loadAnalytics();
            if (activeTab === 'users') loadUsers();
            if (activeTab === 'companies') loadCompanies();
            if (activeTab === 'jobs') loadJobs();
            if (activeTab === 'audit') loadAuditLogs();
            if (activeTab === 'configs') loadConfigs();
            showNotification('Dashboard refreshed');
          }}
          className="self-start md:self-auto px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-sm font-medium transition-colors flex items-center gap-2"
        >
          <svg className="w-4 h-4 animate-spin-reverse" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Refresh Live Data
        </button>
      </div>

      <div className="max-w-7xl mx-auto">
        {/* Navigation Tabs */}
        <div className="flex flex-wrap gap-2 border-b border-slate-800 pb-4 mb-6">
          {[
            { id: 'analytics', label: 'Platform Analytics' },
            { id: 'users', label: 'User Management' },
            { id: 'companies', label: 'Companies' },
            { id: 'jobs', label: 'Job Governance' },
            { id: 'audit', label: 'Security & Audit Trail' },
            { id: 'configs', label: 'System Configuration' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeTab === tab.id
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/30'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800/80'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* TAB 1: ANALYTICS OVERVIEW */}
        {activeTab === 'analytics' && (
          <div className="space-y-6">
            {loadingAnalytics ? (
              <div className="p-12 text-center text-slate-400">Loading platform metrics...</div>
            ) : analytics ? (
              <>
                {/* KPI Grid */}
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                  {[
                    { label: 'Total Users', value: analytics.totalUsers, color: 'text-indigo-400' },
                    { label: 'Candidates', value: analytics.totalCandidates, color: 'text-sky-400' },
                    { label: 'Recruiters', value: analytics.totalRecruiters, color: 'text-emerald-400' },
                    { label: 'Active Jobs', value: analytics.activeJobs, color: 'text-amber-400' },
                    { label: 'Applications', value: analytics.totalApplications, color: 'text-purple-400' },
                    { label: 'Interviews', value: analytics.totalInterviews, color: 'text-rose-400' },
                  ].map((card, idx) => (
                    <div
                      key={idx}
                      className="p-5 rounded-xl bg-slate-900/80 border border-slate-800/80 shadow-sm"
                    >
                      <span className="text-xs uppercase tracking-wider text-slate-400 font-medium">
                        {card.label}
                      </span>
                      <div className={`text-2xl font-black mt-2 ${card.color}`}>
                        {card.value.toLocaleString()}
                      </div>
                    </div>
                  ))}
                </div>

                {/* AI Processing & Intelligence Card */}
                <div className="p-6 rounded-xl bg-gradient-to-r from-indigo-950/40 via-purple-950/20 to-slate-900/60 border border-indigo-800/30">
                  <div className="flex items-center gap-2 mb-4">
                    <span className="px-2 py-0.5 rounded text-xs font-semibold bg-indigo-500/20 text-indigo-300">
                      AI ENGINE
                    </span>
                    <h2 className="text-lg font-bold text-white">AI Intelligence &amp; Matching Telemetry</h2>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                    <div>
                      <span className="text-xs text-slate-400">Avg Candidate Match Score</span>
                      <div className="text-3xl font-extrabold text-indigo-300 mt-1">
                        {analytics.averageMatchScore}%
                      </div>
                    </div>
                    <div>
                      <span className="text-xs text-slate-400">Auto-Shortlisted Applications</span>
                      <div className="text-3xl font-extrabold text-emerald-400 mt-1">
                        {analytics.autoShortlistedCount}
                      </div>
                    </div>
                    <div>
                      <span className="text-xs text-slate-400">Total Resumes AI-Parsed</span>
                      <div className="text-3xl font-extrabold text-sky-400 mt-1">
                        {analytics.totalResumesParsed}
                      </div>
                    </div>
                    <div>
                      <span className="text-xs text-slate-400">Dense Semantic Vectors</span>
                      <div className="text-3xl font-extrabold text-purple-400 mt-1">
                        {analytics.totalEmbeddings}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Distributions */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* Application Statuses */}
                  <div className="p-6 rounded-xl bg-slate-900/80 border border-slate-800">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300 mb-4">
                      Recruitment Funnel By Stage
                    </h3>
                    <div className="space-y-3">
                      {Object.keys(analytics.applicationsByStatus).length === 0 ? (
                        <p className="text-slate-500 text-sm">No applications recorded yet.</p>
                      ) : (
                        Object.entries(analytics.applicationsByStatus).map(([status, count]) => (
                          <div key={status} className="flex justify-between items-center text-sm">
                            <span className="font-mono text-slate-300">{status}</span>
                            <span className="font-semibold text-white px-2 py-0.5 rounded bg-slate-800 border border-slate-700">
                              {count}
                            </span>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  {/* Interview Statuses */}
                  <div className="p-6 rounded-xl bg-slate-900/80 border border-slate-800">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-300 mb-4">
                      Interview Execution By Status
                    </h3>
                    <div className="space-y-3">
                      {Object.keys(analytics.interviewsByStatus).length === 0 ? (
                        <p className="text-slate-500 text-sm">No interviews scheduled yet.</p>
                      ) : (
                        Object.entries(analytics.interviewsByStatus).map(([status, count]) => (
                          <div key={status} className="flex justify-between items-center text-sm">
                            <span className="font-mono text-slate-300">{status}</span>
                            <span className="font-semibold text-white px-2 py-0.5 rounded bg-slate-800 border border-slate-700">
                              {count}
                            </span>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </div>
              </>
            ) : null}
          </div>
        )}

        {/* TAB 2: USER MANAGEMENT */}
        {activeTab === 'users' && (
          <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-xl bg-slate-900/80 border border-slate-800">
              <div className="flex items-center gap-2">
                <span className="text-xs text-slate-400 font-semibold uppercase">Filter Role:</span>
                {(['ALL', 'CANDIDATE', 'RECRUITER', 'ADMIN'] as const).map((r) => (
                  <button
                    key={r}
                    onClick={() => {
                      setSelectedRole(r === 'ALL' ? undefined : r);
                      setUserPage(0);
                    }}
                    className={`px-3 py-1 rounded text-xs font-semibold ${
                      (r === 'ALL' && selectedRole === undefined) || selectedRole === r
                        ? 'bg-indigo-600 text-white'
                        : 'bg-slate-800 text-slate-400 hover:text-white'
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
              <div className="text-xs text-slate-400">
                Total Users: {users?.totalElements || 0}
              </div>
            </div>

            <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900/60">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4">User</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Created</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {users?.content.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-800/30">
                      <td className="py-3 px-4">
                        <div className="font-semibold text-white">{u.fullName}</div>
                        <div className="text-xs text-slate-400 font-mono">{u.email}</div>
                      </td>
                      <td className="py-3 px-4">
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value as Role)}
                          className="text-xs font-semibold px-2 py-1 rounded bg-slate-800 border border-slate-700 text-indigo-300"
                        >
                          <option value="CANDIDATE">CANDIDATE</option>
                          <option value="RECRUITER">RECRUITER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                            u.active !== false
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          }`}
                        >
                          {u.active !== false ? 'ACTIVE' : 'INACTIVE'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-xs text-slate-400">
                        {new Date(u.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4 text-right space-x-2">
                        <button
                          onClick={() => handleStatusToggle(u.id, u.active !== false)}
                          className="px-2 py-1 rounded text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700"
                        >
                          {u.active !== false ? 'Deactivate' : 'Activate'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 3: COMPANIES */}
        {activeTab === 'companies' && (
          <div className="space-y-6">
            {/* Create Company Form */}
            <form
              onSubmit={handleCreateCompany}
              className="p-5 rounded-xl bg-slate-900/80 border border-slate-800 flex flex-wrap gap-4 items-end"
            >
              <div className="flex-1 min-w-[200px]">
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Company Name
                </label>
                <input
                  type="text"
                  placeholder="e.g. Acme Cloud Corp"
                  value={newCompanyName}
                  onChange={(e) => setNewCompanyName(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-sm text-white focus:outline-none focus:border-indigo-500"
                />
              </div>
              <div className="flex-1 min-w-[180px]">
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Industry
                </label>
                <input
                  type="text"
                  placeholder="e.g. Enterprise Software"
                  value={newCompanyIndustry}
                  onChange={(e) => setNewCompanyIndustry(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg bg-slate-950 border border-slate-700 text-sm text-white focus:outline-none focus:border-indigo-500"
                />
              </div>
              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm transition-colors"
              >
                Register Company
              </button>
            </form>

            {/* Companies Table */}
            <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900/60">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Company</th>
                    <th className="py-3 px-4">Industry</th>
                    <th className="py-3 px-4">Verification</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {companies?.content.map((c) => (
                    <tr key={c.id} className="hover:bg-slate-800/30">
                      <td className="py-3 px-4 font-bold text-white">{c.name}</td>
                      <td className="py-3 px-4 text-slate-300">{c.industry || 'General'}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                            c.verified
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          }`}
                        >
                          {c.verified ? 'VERIFIED' : 'PENDING'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => handleVerifyCompany(c.id, c.verified)}
                          className="px-2 py-1 rounded text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700"
                        >
                          {c.verified ? 'Revoke Verification' : 'Verify Organization'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 4: JOB GOVERNANCE */}
        {activeTab === 'jobs' && (
          <div className="space-y-4">
            <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900/60">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-900 text-slate-400 text-xs uppercase border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Job Requisition</th>
                    <th className="py-3 px-4">Department</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Created</th>
                    <th className="py-3 px-4 text-right">Administrative Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {jobs?.content.map((j) => (
                    <tr key={j.id} className="hover:bg-slate-800/30">
                      <td className="py-3 px-4">
                        <div className="font-bold text-white">{j.title}</div>
                        <div className="text-xs text-slate-400">{j.location || 'Remote'}</div>
                      </td>
                      <td className="py-3 px-4 text-slate-300">{j.department || 'Engineering'}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                            j.status === 'ACTIVE'
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : 'bg-slate-800 text-slate-400 border border-slate-700'
                          }`}
                        >
                          {j.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-xs text-slate-400">
                        {new Date(j.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-3 px-4 text-right space-x-2">
                        {j.status === 'ACTIVE' ? (
                          <button
                            onClick={() => handleJobStatusChange(j.id, 'CLOSED')}
                            className="px-2 py-1 rounded text-xs font-medium bg-rose-950/40 text-rose-300 border border-rose-800 hover:bg-rose-900/50"
                          >
                            Close Position
                          </button>
                        ) : (
                          <button
                            onClick={() => handleJobStatusChange(j.id, 'ACTIVE')}
                            className="px-2 py-1 rounded text-xs font-medium bg-emerald-950/40 text-emerald-300 border border-emerald-800 hover:bg-emerald-900/50"
                          >
                            Re-activate
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 5: AUDIT LOGS */}
        {activeTab === 'audit' && (
          <div className="space-y-4">
            <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-900/60">
              <table className="w-full text-left text-xs font-mono">
                <thead className="bg-slate-900 text-slate-400 uppercase border-b border-slate-800">
                  <tr>
                    <th className="py-3 px-4">Timestamp</th>
                    <th className="py-3 px-4">Actor</th>
                    <th className="py-3 px-4">Action</th>
                    <th className="py-3 px-4">Resource</th>
                    <th className="py-3 px-4">Result</th>
                    <th className="py-3 px-4">Audit Metadata</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {auditLogs?.content.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-800/30">
                      <td className="py-3 px-4 text-slate-400">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className="py-3 px-4 text-indigo-300 font-semibold">{log.actorEmail}</td>
                      <td className="py-3 px-4 text-white font-bold">{log.action}</td>
                      <td className="py-3 px-4 text-slate-300">
                        {log.resourceType}
                        {log.resourceId ? `:${log.resourceId}` : ''}
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-block px-1.5 py-0.5 rounded text-[10px] font-bold ${
                            log.result === 'SUCCESS'
                              ? 'bg-emerald-500/10 text-emerald-400'
                              : 'bg-rose-500/10 text-rose-400'
                          }`}
                        >
                          {log.result}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-400 truncate max-w-xs">{log.metadata}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* TAB 6: PLATFORM CONFIGURATION */}
        {activeTab === 'configs' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {configs.map((cfg) => (
              <div
                key={cfg.configKey}
                className="p-5 rounded-xl bg-slate-900/80 border border-slate-800 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-mono font-bold text-indigo-400">
                      {cfg.configKey}
                    </span>
                    <span className="text-[10px] uppercase font-semibold text-slate-400 px-2 py-0.5 rounded bg-slate-800">
                      {cfg.category}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mb-4">{cfg.description}</p>
                </div>

                {editingConfigKey === cfg.configKey ? (
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={editingConfigVal}
                      onChange={(e) => setEditingConfigVal(e.target.value)}
                      className="flex-1 px-3 py-1.5 rounded bg-slate-950 border border-indigo-500 text-xs text-white focus:outline-none"
                    />
                    <button
                      onClick={() => handleSaveConfig(cfg.configKey)}
                      className="px-3 py-1.5 rounded bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold"
                    >
                      Save
                    </button>
                    <button
                      onClick={() => setEditingConfigKey(null)}
                      className="px-2 py-1.5 rounded bg-slate-800 text-slate-400 text-xs"
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <div className="flex items-center justify-between pt-3 border-t border-slate-800/80">
                    <span className="font-mono text-sm font-bold text-white">
                      {cfg.configValue}
                    </span>
                    <button
                      onClick={() => {
                        setEditingConfigKey(cfg.configKey);
                        setEditingConfigVal(cfg.configValue);
                      }}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs text-indigo-300 border border-slate-700 font-medium"
                    >
                      Edit Value
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
