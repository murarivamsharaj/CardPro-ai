import api from './api';

/**
 * Analytics API client.
 *
 * All requests go through the shared `api` axios instance (see ./api.ts),
 * whose request interceptor attaches the JWT from localStorage as a Bearer
 * token on every call and whose response interceptor redirects to /login on
 * 401. The gateway validates that token and injects the caller's identity, so
 * the endpoints are scoped to the authenticated user.
 */

export interface AnalyticsResponse {
  totalViews: number;
  uniqueVisitors: number;
  totalLeads: number;
  /** Percentage (0–100): clicks ÷ views. */
  clickThroughRate: number;
  /** ISO date (YYYY-MM-DD) → view count, zero-filled for charting. */
  viewsByDate: Record<string, number>;
  /** Link label (e.g. "LinkedIn") → click count. */
  clicksByLink: Record<string, number>;
}

export interface TotalCardsResponse {
  totalCards: number;
}

/** Platform-wide card metrics, visible to admins only. */
export interface AdminAnalyticsResponse {
  totalCards: number;
  activeCards: number;
  totalViews: number;
  viewsLast7Days: number;
}

export const analyticsService = {
  /**
   * Engagement metrics for the logged-in user across all of their cards.
   * @param days time-series window for `viewsByDate` (default 30, backend clamps 1–365)
   */
  async getUserAnalytics(days = 30): Promise<AnalyticsResponse> {
    const { data } = await api.get<AnalyticsResponse>('/api/v1/analytics/summary', {
      params: { days },
    });
    return data;
  },

  /** Admin-only: absolute total number of cards in the system. */
  async getTotalCards(): Promise<TotalCardsResponse> {
    const { data } = await api.get<TotalCardsResponse>('/api/v1/analytics/admin/total-cards');
    return data;
  },

  /** Admin-only: platform-wide card health metrics. */
  async getAdminOverview(): Promise<AdminAnalyticsResponse> {
    const { data } = await api.get<AdminAnalyticsResponse>('/api/v1/analytics/admin/overview');
    return data;
  },
};

export default analyticsService;
