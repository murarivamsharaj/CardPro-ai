import React from 'react';

interface SkeletonProps {
  className?: string;
}

/** Shimmering skeleton placeholder. Combine with h-/w- utilities. */
export const Skeleton: React.FC<SkeletonProps> = ({ className = '' }) => {
  return <div aria-hidden="true" className={`skeleton ${className}`} />;
};

/** Convenience: a card-shaped skeleton with avatar + lines. */
export const SkeletonCard: React.FC = () => (
  <div className="glass-card p-6">
    <div className="flex items-center gap-4">
      <Skeleton className="h-12 w-12 rounded-full" />
      <div className="flex-1 space-y-2">
        <Skeleton className="h-4 w-2/3" />
        <Skeleton className="h-3 w-1/2" />
      </div>
    </div>
    <div className="mt-6 space-y-3">
      <Skeleton className="h-3 w-full" />
      <Skeleton className="h-3 w-5/6" />
      <Skeleton className="h-3 w-4/6" />
    </div>
    <Skeleton className="mt-6 h-6 w-20 rounded-full" />
  </div>
);

export default Skeleton;
