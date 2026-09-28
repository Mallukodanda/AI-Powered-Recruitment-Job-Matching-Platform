import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/common/Button';
import { Badge } from '../components/common/Badge';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

describe('Common UI Components Unit Tests', () => {
  describe('Button Component', () => {
    it('renders with label and triggers onClick', () => {
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Submit Application</Button>);

      const btn = screen.getByRole('button', { name: /submit application/i });
      expect(btn).toBeInTheDocument();

      fireEvent.click(btn);
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('disables button when disabled or loading prop is provided', () => {
      render(<Button disabled isLoading>Processing AI Match</Button>);

      const btn = screen.getByRole('button');
      expect(btn).toBeDisabled();
    });

    it('renders secondary and danger variants correctly', () => {
      const { rerender } = render(<Button variant="secondary">Cancel</Button>);
      expect(screen.getByRole('button')).toHaveClass('bg-slate-800');

      rerender(<Button variant="danger">Delete Profile</Button>);
      expect(screen.getByRole('button')).toHaveClass('bg-rose-600');
    });
  });

  describe('Badge Component', () => {
    it('renders text with emerald styling for success', () => {
      render(<Badge variant="emerald">SHORTLISTED</Badge>);
      const badge = screen.getByText('SHORTLISTED');
      expect(badge).toBeInTheDocument();
      expect(badge).toHaveClass('text-emerald-400');
    });

    it('renders amber and indigo variants correctly', () => {
      const { rerender } = render(<Badge variant="amber">UNDER_REVIEW</Badge>);
      expect(screen.getByText('UNDER_REVIEW')).toHaveClass('text-amber-400');

      rerender(<Badge variant="indigo">AI 94% MATCH</Badge>);
      expect(screen.getByText('AI 94% MATCH')).toHaveClass('text-indigo-400');
    });
  });

  describe('LoadingSpinner Component', () => {
    it('renders accessible loading spinner with role status and aria-label', () => {
      render(<LoadingSpinner size="md" />);
      const spinner = screen.getByRole('status', { name: /loading/i });
      expect(spinner).toBeInTheDocument();
      expect(spinner).toHaveAttribute('aria-label', 'Loading');
    });
  });
});
