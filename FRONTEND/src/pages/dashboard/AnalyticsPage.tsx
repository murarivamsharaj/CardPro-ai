import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAnalytics } from '../../store/slices/cardSlice';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export const AnalyticsPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { analytics, loading, error } = useSelector((state: any) => state.card);

  useEffect(() => {
    dispatch(fetchAnalytics());
  }, [dispatch]);

  const chartData = analytics.viewsByDate 
    ? Object.keys(analytics.viewsByDate).map((date) => ({
        date,
        views: analytics.viewsByDate[date],
      }))
    : [];

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Analytics & Reports</h1>

      {loading && <p className="text-blue-600 mb-4">Loading analytics...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      {/* Summary Widgets */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow border border-gray-200">
          <p className="text-sm font-medium text-gray-500">Total Views</p>
          <p className="text-3xl font-bold text-blue-600 mt-2">{analytics.totalViews || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border border-gray-200">
          <p className="text-sm font-medium text-gray-500">Unique Visitors</p>
          <p className="text-3xl font-bold text-green-600 mt-2">{analytics.uniqueVisitors || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border border-gray-200">
          <p className="text-sm font-medium text-gray-500">Total Leads Captured</p>
          <p className="text-3xl font-bold text-purple-600 mt-2">{analytics.totalLeads || 0}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow border border-gray-200">
          <p className="text-sm font-medium text-gray-500">Click-Through Rate</p>
          <p className="text-3xl font-bold text-orange-500 mt-2">{(analytics.clickThroughRate || 0).toFixed(1)}%</p>
        </div>
      </div>

      {/* Time-Series Chart */}
      <div className="bg-white p-6 rounded-lg shadow border border-gray-200 mb-8">
        <h2 className="text-xl font-semibold mb-4">Profile Views Over Time</h2>
        <div className="h-80 w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="views" stroke="#2563eb" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-gray-400">
              No view data available yet.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AnalyticsPage;