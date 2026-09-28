import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RootLayout } from './layouts/RootLayout';
import { LandingPage } from './pages/LandingPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { AdminDashboard } from './pages/AdminDashboard';

// Configure TanStack Query client with sensible retry and stale defaults
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30000,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<RootLayout />}>
            {/* Step 1: Landing Page */}
            <Route index element={<LandingPage />} />

            {/* Placeholders for subsequent steps */}
            <Route
              path="/jobs"
              element={
                <div className="py-12 text-center text-slate-400">
                  <h2 className="text-xl font-bold text-white mb-2">Job Requisitions Explorer</h2>
                  <p>Step 8 will implement the full Job Listing &amp; Filtering page.</p>
                </div>
              }
            />
            <Route
              path="/login"
              element={
                <div className="py-12 text-center text-slate-400">
                  <h2 className="text-xl font-bold text-white mb-2">Authentication: Login</h2>
                  <p>Step 3 will implement the Login page.</p>
                </div>
              }
            />
            <Route
              path="/register"
              element={
                <div className="py-12 text-center text-slate-400">
                  <h2 className="text-xl font-bold text-white mb-2">Authentication: Registration</h2>
                  <p>Step 2 will implement the Registration page.</p>
                </div>
              }
            />

            {/* Admin Management & Telemetry */}
            <Route path="/admin" element={<AdminDashboard />} />

            {/* Catch-all 404 */}
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
