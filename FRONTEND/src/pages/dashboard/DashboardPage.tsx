import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchUserCards } from '../../store/slices/cardSlice';

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const { cards, loading, error } = useSelector((state: any) => state.card);

  useEffect(() => {
    dispatch(fetchUserCards());
  }, [dispatch]);

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold mb-6">Card Dashboard</h1>

      {loading && <p className="text-blue-600">Loading your cards...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      {!loading && cards.length === 0 && (
        <p className="text-gray-600">No cards found. Create your first digital card!</p>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {cards.map((card: any) => (
          <div key={card.id} className="p-5 bg-white rounded-lg shadow-md border border-gray-200">
            <h3 className="text-xl font-semibold mb-2">{card.title || 'Untitled Card'}</h3>
            <p className="text-gray-500 text-sm">{card.description || 'No description provided.'}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DashboardPage;