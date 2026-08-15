import React, { useCallback, useEffect, useState } from 'react';
import {
  analyticsService,
  AnalyticsResponse,
  AdminAnalyticsResponse,
} from '../../services/analyticsService';
import { useAuth } from '../../context/AuthContext';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { Skeleton } from '../../components/common/Skeleton';

const EMPTY_ANALYTICS: AnalyticsResponse = {
  totalViews: 0,
  uniqueVisitors: 0,
  totalLeads: 0,
  clickThroughRate: 0,
  viewsByDate: {},
  clicksByLink: {},
};

const PERIODS: { label: string; days: number }[] = [
  { label: 'Last 7 days', days: 7 },
  { label: 'Last 30 days', days: 30 },
  { label: 'Last 90 days', days: 90 },
];

const STATS: { key: keyof AnalyticsResponse; label: string; icon: React.ReactNode; accent: string; format?: (v: number) => string }[] = [
  {
    key: 'totalViews',
    label: 'Total Views',
    accent: 'from-sky-500/30 to-blue-600/20 text-sky-300',
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    ),
  },
  {
    key: 'uniqueVisitors',
    label: 'Unique Visitors',
    accent: 'from-emerald-500/30 to-teal-600/20 text-emerald-300',
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
      </svg>
    ),
  },
  {
    key: 'totalLeads',
    label: 'Total Leads Captured',
    accent: 'from-violet-500/30 to-purple-600/20 text-violet-300',
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 8.511c.884.284 1.5 1.128 1.5 2.097v4.286c0 1.136-.847 2.1-1.98 2.193-.34.027-.68.052-1.02.072v3.091l-3-3c-1.354 0-2.694-.055-4.02-.163a2.115 2.115 0 01-.825-.242m9.345-8.334a2.126 2.126 0 00-.476-.095 48.64 48.64 0 00-8.048 0c-1.131.094-1.976 1.057-1.976 2.192v4.286c0 .837.46 1.58 1.155 1.951m9.345-8.334V6.637c0-1.621-1.152-3.026-2.76-3.235A48.455 48.455 0 0011.25 3c-2.115 0-4.198.137-6.24.402-1.608.209-2.76 1.614-2.76 3.235v6.226c0 1.621 1.152 3.026 2.76 3.235.577.075 1.157.14 1.74.194V21l4.155-4.155" />
      </svg>
    ),
  },
  {
    key: 'clickThroughRate',
    label: 'Click-Through Rate',
    accent: 'from-orange-500/30 to-amber-600/20 text-orange-300',
    icon: (
      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
      </svg>
    ),
    format: (v: number) => `${(v || 0).toFixed(1)}%`,
  },
];

export const AnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [data, setData] = useState<AnalyticsResponse>(EMPTY_ANALYTICS);
  const [adminData, setAdminData] = useState<AdminAnalyticsResponse | null>(null);
  const [days, setDays] = useState(30);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (windowDays: number) => {
    setLoading(true);
    setError(null);
    try {
      const response = await analyticsService.getUserAnalytics(windowDays);
      setData(response);
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || err?.message || 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(days);
  }, [days, load]);

  // Platform-wide card stats — admins only, non-fatal if it fails.
  useEffect(() => {
    if (!isAdmin) return;
    analyticsService
      .getAdminOverview()
      .then(setAdminData)
      .catch(() => setAdminData(null));
  }, [isAdmin]);

  const chartData = data.viewsByDate
    ? Object.entries(data.viewsByDate).map(([date, views]) => ({ date, views: Number(views) || 0 }))
    : [];
  const hasChartData = chartData.some((d) => d.views > 0);

  const linkData = data.clicksByLink
    ? Object.entries(data.clicksByLink).map(([name, clicks]) => ({ name, clicks: Number(clicks) || 0 }))
    : [];

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">Analytics & Reports</h1>
          <p className="mt-1 text-sm text-white/50">How the world discovers your digital cards</p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 rounded-xl border border-white/10 bg-white/5 p-1">
            {PERIODS.map((period) => (
              <button
                key={period.days}
                onClick={() => setDays(period.days)}
                disabled={loading}
                className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all duration-200 ${
                  days === period.days
                    ? 'bg-gradient-to-r from-violet-600 to-fuchsia-600 text-white shadow-lg shadow-fuchsia-900/40'
                    : 'text-white/60 hover:bg-white/10 hover:text-white'
                }`}
              >
                {period.label}
              </button>
            ))}
          </div>
          <button onClick={() => load(days)} className="btn-secondary text-xs" disabled={loading}>
            {loading ? 'Refreshing…' : 'Refresh'}
          </button>
        </div>
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
              <p className="text-sm font-semibold text-white">Analytics unavailable</p>
              <p className="text-xs text-white/50">{error}</p>
            </div>
          </div>
          <button onClick={() => load(days)} className="btn-secondary text-xs">
            Retry
          </button>
        </div>
      )}

      {/* Summary widgets */}
      {loading ? (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {[0, 1, 2, 3].map((i) => (
            <div key={i} className="glass-panel p-6">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="mt-3 h-8 w-16" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {STATS.map((stat) => (
            <div key={stat.key} className="stat-card group">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-white/60">{stat.label}</p>
                <span className={`flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br ${stat.accent} transition-transform duration-300 group-hover:scale-110`}>
                  {stat.icon}
                </span>
              </div>
              <p className="mt-3 text-3xl font-bold tracking-tight text-white">
                {stat.format
                  ? stat.format(Number(data[stat.key]) || 0)
                  : (Number(data[stat.key as keyof AnalyticsResponse]) || 0).toLocaleString()}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Admin platform overview */}
      {isAdmin && (
        <div className="glass-panel mt-8 p-6">
          <div className="mb-1 flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white shadow-lg shadow-fuchsia-900/40">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
              </svg>
            </span>
            <div>
              <h2 className="text-lg font-semibold text-white">Platform Overview</h2>
              <p className="text-xs text-white/40">Card health metrics across the whole CardPro AI platform</p>
            </div>
          </div>
          {adminData ? (
            <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {[
                { label: 'Total Cards', value: adminData.totalCards },
                { label: 'Active Cards', value: adminData.activeCards },
                { label: 'Total Views', value: adminData.totalViews },
                { label: 'Views (Last 7 Days)', value: adminData.viewsLast7Days },
              ].map((tile) => (
                <div key={tile.label} className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <p className="text-xs font-medium uppercase tracking-wider text-white/50">{tile.label}</p>
                  <p className="mt-2 text-2xl font-bold tracking-tight text-white">{tile.value.toLocaleString()}</p>
                </div>
              ))}
            </div>
          ) : (
            <Skeleton className="mt-4 h-24 w-full" />
          )}
        </div>
      )}

      {/* Time-series chart */}
      <div className="glass-panel mt-8 p-6">
        <h2 className="text-xl font-semibold text-white">Profile Views Over Time</h2>
        <p className="mt-0.5 text-xs text-white/40">Daily impressions across all your cards</p>
        <div className="mt-4 h-80 w-full">
          {loading ? (
            <Skeleton className="h-full w-full" />
          ) : hasChartData ? (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" />
                <XAxis dataKey="date" stroke="rgba(255,255,255,0.35)" tick={{ fill: 'rgba(255,255,255,0.5)', fontSize: 12 }} />
                <YAxis stroke="rgba(255,255,255,0.35)" tick={{ fill: 'rgba(255,255,255,0.5)', fontSize: 12 }} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    background: 'rgba(15, 15, 35, 0.9)',
                    border: '1px solid rgba(255,255,255,0.15)',
                    borderRadius: '0.75rem',
                    backdropFilter: 'blur(12px)',
                    color: '#fff',
                  }}
                  labelStyle={{ color: 'rgba(255,255,255,0.7)' }}
                />
                <Line
                  type="monotone"
                  dataKey="views"
                  stroke="#a855f7"
                  strokeWidth={2.5}
                  dot={{ fill: '#e879f9', r: 3 }}
                  activeDot={{ r: 5, fill: '#e879f9' }}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-white/40">
              No view data available yet — share your card link to get started.
            </div>
          )}
        </div>
      </div>

      {/* Link performance */}
      {!loading && linkData.length > 0 && (
        <div className="glass-panel mt-8 p-6">
          <h2 className="text-xl font-semibold text-white">Link Performance</h2>
          <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
            {linkData.map(({ name, clicks }) => {
              const max = Math.max(...linkData.map((l) => l.clicks), 1);
              const pct = Math.max(6, Math.round((clicks / max) * 100));
              return (
                <div key={name} className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium text-white/80">{name}</span>
                    <span className="font-bold text-fuchsia-300">{clicks}</span>
                  </div>
                  <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-white/10">
                    <div
                      className="h-full rounded-full bg-gradient-to-r from-violet-500 to-fuchsia-500 transition-all duration-700"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default AnalyticsPage;
