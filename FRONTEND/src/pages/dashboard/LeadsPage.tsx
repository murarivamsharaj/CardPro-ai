import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchLeads, Lead } from '../../store/slices/leadsSlice';
import { Skeleton } from '../../components/common/Skeleton';

function formatDate(value?: string): string {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

function initials(name?: string): string {
  if (!name) return '?';
  return name
    .split(/\s+/)
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();
}

export const LeadsPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { leads, loading, error, totalElements, totalPages, currentPage } = useSelector((state: any) => state.leads);

  useEffect(() => {
    dispatch(fetchLeads({ page: 0, size: 20 }));
  }, [dispatch]);

  const loadPage = (page: number) => {
    dispatch(fetchLeads({ page, size: 20 }));
  };

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">Leads</h1>
          <p className="mt-1 text-sm text-white/50">
            {totalElements > 0 ? `${totalElements.toLocaleString()} lead${totalElements === 1 ? '' : 's'} captured` : 'Everyone who reached out through your cards'}
          </p>
        </div>
        <button onClick={() => dispatch(fetchLeads({ page: 0, size: 20 }))} className="btn-secondary text-xs" disabled={loading}>
          {loading ? 'Refreshing…' : 'Refresh'}
        </button>
      </div>

      {/* Error state with retry */}
      {error && (
        <div className="glass-panel mb-8 flex flex-wrap items-center justify-between gap-4 border-rose-400/30 p-5">
          <div className="flex items-center gap-3">
            <span className="flex h-10 w-10 items-center justify-center rounded-full bg-rose-500/20 text-rose-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
              </svg>
            </span>
            <div>
              <p className="text-sm font-semibold text-white">Couldn't load your leads</p>
              <p className="text-xs text-white/50">{error}</p>
            </div>
          </div>
          <button onClick={() => dispatch(fetchLeads({ page: 0, size: 20 }))} className="btn-secondary text-xs">
            Retry
          </button>
        </div>
      )}

      {/* Skeleton table */}
      {loading && leads.length === 0 && (
        <div className="glass-panel overflow-hidden">
          <div className="hidden grid-cols-4 gap-4 border-b border-white/10 px-6 py-4 text-xs font-semibold uppercase tracking-wider text-white/40 md:grid">
            <span>Visitor</span>
            <span>Phone</span>
            <span>Captured</span>
            <span>AI Follow-up</span>
          </div>
          {[0, 1, 2, 3, 4].map((i) => (
            <div key={i} className="grid grid-cols-1 gap-3 border-b border-white/5 px-6 py-5 md:grid-cols-4 md:items-center">
              <div className="flex items-center gap-3">
                <Skeleton className="h-10 w-10 rounded-full" />
                <Skeleton className="h-4 w-32" />
              </div>
              <Skeleton className="h-4 w-28" />
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-4 w-40" />
            </div>
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && leads.length === 0 && (
        <div className="glass-panel flex flex-col items-center justify-center px-6 py-20 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white shadow-xl shadow-fuchsia-900/40">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
          </div>
          <h2 className="mt-5 text-xl font-semibold text-white">No leads yet</h2>
          <p className="mt-1 max-w-sm text-sm text-white/50">
            When someone submits a contact form on your card, they'll appear here instantly.
          </p>
        </div>
      )}

      {/* Leads table */}
      {!loading && leads.length > 0 && (
        <div className="glass-panel overflow-hidden">
          <div className="hidden grid-cols-4 gap-4 border-b border-white/10 px-6 py-4 text-xs font-semibold uppercase tracking-wider text-white/40 md:grid">
            <span>Visitor</span>
            <span>Phone</span>
            <span>Captured</span>
            <span>AI Follow-up</span>
          </div>
          {leads.map((lead: Lead) => (
            <div
              key={lead.id}
              className="grid grid-cols-1 gap-3 border-b border-white/5 px-6 py-5 transition-colors last:border-0 hover:bg-white/5 md:grid-cols-4 md:items-center"
            >
              <div className="flex items-center gap-3">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500/40 to-fuchsia-500/40 text-sm font-bold text-white">
                  {initials(lead.visitorName)}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-white">{lead.visitorName || 'Anonymous visitor'}</p>
                  <p className="truncate text-xs text-white/40">{lead.profileId}</p>
                </div>
              </div>
              <div className="flex items-center gap-2 md:block">
                <span className="text-xs text-white/40 md:hidden">Phone: </span>
                <a href={`tel:${lead.visitorPhone}`} className="text-sm text-white/80 transition-colors hover:text-fuchsia-300">
                  {lead.visitorPhone || '—'}
                </a>
              </div>
              <div className="flex items-center gap-2 md:block">
                <span className="text-xs text-white/40 md:hidden">Captured: </span>
                <span className="text-sm text-white/60">{formatDate(lead.capturedAt)}</span>
              </div>
              <div className="min-w-0">
                {lead.aiFollowup ? (
                  <p className="truncate text-sm text-white/60" title={lead.aiFollowup}>
                    {lead.aiFollowup}
                  </p>
                ) : (
                  <span className="glass-chip">No follow-up yet</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-2">
          <button
            onClick={() => loadPage(Math.max(0, currentPage - 1))}
            disabled={currentPage <= 0}
            className="btn-secondary px-4 py-2 text-xs"
          >
            Previous
          </button>
          <span className="px-3 text-xs text-white/50">
            Page {currentPage + 1} of {totalPages}
          </span>
          <button
            onClick={() => loadPage(Math.min(totalPages - 1, currentPage + 1))}
            disabled={currentPage >= totalPages - 1}
            className="btn-secondary px-4 py-2 text-xs"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default LeadsPage;
