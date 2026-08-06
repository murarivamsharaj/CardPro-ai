import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchUserCards } from '../../store/slices/cardSlice';

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { cards, loading, error, totalPages } = useSelector((state: any) => state.card);

  useEffect(() => {
    dispatch(fetchUserCards({}));
  }, [dispatch]);

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Card Dashboard</h1>
        <button
          onClick={() => navigate('/dashboard/cards/create')}
          className="bg-blue-600 text-white font-semibold px-5 py-2.5 rounded-lg shadow hover:bg-blue-700 transition"
        >
          + Create New Card
        </button>
      </div>

      {loading && <p className="text-gray-500">Loading cards...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && cards.length === 0 && (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-100 shadow-sm">
          <p className="text-gray-500 mb-4">You haven't created any digital cards yet.</p>
          <button
            onClick={() => navigate('/dashboard/cards/create')}
            className="text-blue-600 font-semibold hover:underline"
          >
            Create your first card now
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {cards.map((card: any) => (
          <div key={card.id || card.slug} className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition">
            <h3 className="text-lg font-bold text-gray-900">{card.slug}</h3>
            <p className="text-sm text-gray-500 mt-1">Template: {card.templateId}</p>
            <span className="inline-block mt-4 text-xs font-semibold px-2.5 py-1 bg-green-50 text-green-700 rounded-full">
              Active
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DashboardPage;