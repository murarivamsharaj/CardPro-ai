import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchLeads, Lead } from '../../store/slices/leadsSlice';
import { Skeleton } from '../../components/common/Skeleton';
import { ROUTES } from '../../utils/constants';

function formatDate(value?: string): string {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(value?: string): string {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
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

/**
 * "My Leads" — the logged-in user's captured leads, rendered as a responsive
 * grid of frosted-glass message cards (name, email, message, submitted date,
 * plus phone and AI follow-up). The API call is scoped to the caller via the
 * X-User-Id header the gateway injects from the JWT.
 */
export const LeadsPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
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
          <h1 className="text-3xl font-bold tracking-tight text-white">My Leads</h1>
          <p className="mt-1 text-sm text-white/50">
            {totalElements > 0
              ? `${totalElements.toLocaleString()} lead${totalElements === 1 ? '' : 's'} captured`
              : 'Everyone who reaches out through your cards'}
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

      {/* Loading skeletons */}
      {loading && leads.length === 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
          {[0, 1, 2, 3, 4, 5].map((i) => (
            <LeadCardSkeleton key={i} />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && leads.length === 0 && (
        <div className="glass-panel flex flex-col items-center justify-center px-6 py-20 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white shadow-xl shadow-fuchsia-900/40">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
            </svg>
          </div>
          <h2 className="mt-5 text-xl font-semibold text-white">No leads yet</h2>
          <p className="mt-1 max-w-sm text-sm text-white/50">
            Share your card to start collecting contacts. Every submission from your public card lands here instantly.
          </p>
          <button onClick={() => navigate(ROUTES.CARDS)} className="btn-primary mt-6">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M7.217 10.907a2.25 2.25 0 100 2.186m0-2.186c.18.324.283.696.283 1.093s-.103.77-.283 1.093m0-2.186l9.566-5.314m-9.566 7.5l9.566 5.314m0 0a2.25 2.25 0 103.935 2.186 2.25 2.25 0 00-3.935-2.186zm0-12.814a2.25 2.25 0 103.933-2.185 2.25 2.25 0 00-3.933 2.185z" />
            </svg>
            View My Cards
          </button>
        </div>
      )}

      {/* Leads grid — message cards */}
      {!loading && leads.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
          {leads.map((lead: Lead) => (
            <LeadCard key={lead.id} lead={lead} />
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

/** A single lead presented as a frosted-glass "message card". */
function LeadCard({ lead }: { lead: Lead }) {
  const name = lead.visitorName || 'Anonymous visitor';

  return (
    <div className="glass-card flex h-full flex-col p-5">
      {/* Header: avatar + name + email, submitted date */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500/40 to-fuchsia-500/40 text-sm font-bold text-white">
            {initials(lead.visitorName)}
          </span>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-white">{name}</p>
            {lead.visitorEmail ? (
              <a
                href={`mailto:${lead.visitorEmail}`}
                className="block truncate text-xs text-white/50 transition-colors hover:text-fuchsia-300"
                title={lead.visitorEmail}
              >
                {lead.visitorEmail}
              </a>
            ) : (
              <p className="text-xs text-white/35">No email provided</p>
            )}
          </div>
        </div>
        <span className="glass-chip shrink-0" title={formatDateTime(lead.capturedAt)}>
          {formatDate(lead.capturedAt)}
        </span>
      </div>

      {/* Message body */}
      <div className="mt-4 flex-1">
        {lead.message ? (
          <p className="line-clamp-4 text-sm leading-relaxed text-white/75">{lead.message}</p>
        ) : (
          <p className="text-sm italic text-white/35">No message</p>
        )}
      </div>

      {/* Footer: phone + AI follow-up */}
      <div className="mt-5 flex flex-wrap items-center justify-between gap-2 border-t border-white/10 pt-4">
        {lead.visitorPhone ? (
          <a
            href={`tel:${lead.visitorPhone}`}
            className="flex items-center gap-1.5 text-xs font-medium text-white/60 transition-colors hover:text-fuchsia-300"
          >
            <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
            </svg>
            {lead.visitorPhone}
          </a>
        ) : (
          <span className="text-xs text-white/35">—</span>
        )}

        {lead.aiFollowup ? (
          <span className="glass-chip" title={lead.aiFollowup}>
            <svg className="h-3 w-3 text-fuchsia-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456z" />
            </svg>
            AI follow-up
          </span>
        ) : (
          <span className="text-xs text-white/35">No follow-up yet</span>
        )}
      </div>
    </div>
  );
}

/** Card-shaped skeleton matching the message-card layout. */
function LeadCardSkeleton() {
  return (
    <div className="glass-card p-5">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-center gap-3">
          <Skeleton className="h-11 w-11 shrink-0 rounded-full" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-3 w-40" />
          </div>
        </div>
        <Skeleton className="h-6 w-20 shrink-0 rounded-full" />
      </div>
      <div className="mt-4 space-y-2.5">
        <Skeleton className="h-3 w-full" />
        <Skeleton className="h-3 w-5/6" />
        <Skeleton className="h-3 w-2/3" />
      </div>
      <div className="mt-5 flex items-center justify-between border-t border-white/10 pt-4">
        <Skeleton className="h-3 w-24" />
        <Skeleton className="h-6 w-24 rounded-full" />
      </div>
    </div>
  );
}

export default LeadsPage;
