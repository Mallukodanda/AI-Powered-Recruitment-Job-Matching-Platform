import React from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, Home } from 'lucide-react';
import { Button } from '../components/common/Button';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4 space-y-4">
      <div className="w-16 h-16 rounded-2xl bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-400">
        <AlertCircle className="w-8 h-8" />
      </div>
      <h1 className="text-3xl font-bold font-heading text-white">Page Not Found</h1>
      <p className="text-slate-400 max-w-md text-sm">
        The page you are looking for does not exist or may have been moved.
      </p>
      <Link to="/" className="pt-2">
        <Button variant="primary" size="md" icon={<Home className="w-4 h-4" />}>
          Back to Homepage
        </Button>
      </Link>
    </div>
  );
};
