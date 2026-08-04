import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchUserCards } from '../../store/slices/cardSlice';

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  
  // Destructure the pagination fields from Redux state
  const { cards, totalPages, currentPage, loading, error } = useSelector((state: any) => state.card);

  // Local state for the search bar
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch cards with debounce whenever searchTerm or currentPage changes
  useEffect(() => {
    // 500ms delay prevents the API from firing on every single keystroke
    const delayDebounceFn = setTimeout(() => {
      dispatch(fetchUserCards({ search: searchTerm, page: currentPage, size: 10 }));
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, currentPage, dispatch]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      dispatch(fetchUserCards({ search: searchTerm, page: currentPage + 1, size: 10 }));
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      dispatch(fetchUserCards({ search: searchTerm, page: currentPage - 1, size: 10 }));
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Card Dashboard</h1>
        <button
          onClick={() => navigate('/create-card')}
          className="px-4 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
        >
          + Create New Card
        </button>
      </div>

      {/* Search Bar */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Search cards by slug..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full max-w-md p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {loading && <p className="text-blue-600 mb-4">Loading your cards...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      {!loading && (!cards || cards.length === 0) && (
        <div className="text-center py-12 bg-white rounded-xl border border-dashed border-gray-300 p-8 shadow-sm">
          <p className="text-gray-600 mb-4 text-lg">No cards found. Create your first digital card!</p>
          <button
            onClick={() => navigate('/create-card')}
            className="px-5 py-2.5 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors shadow"
          >
            Create New Card
          </button>
        </div>
      )}

      {/* Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {cards && cards.map((card: any) => (
          <div key={card.id} className="p-5 bg-white rounded-lg shadow-md border border-gray-200 hover:shadow-lg transition-shadow">
            <h3 className="text-xl font-semibold mb-2">{card.slug || 'Untitled Card'}</h3>
            <p className="text-gray-500 text-sm mb-2">Template: <span className="font-medium text-gray-700">{card.templateId}</span></p>
            <p className={`text-sm font-bold ${card.isActive ? 'text-green-600' : 'text-red-500'}`}>
              {card.isActive ? 'Active' : 'Inactive'}
            </p>
          </div>
        ))}
      </div>

      {/* Pagination Controls */}
      {totalPages > 0 && (
        <div className="mt-8 flex items-center gap-4">
          <button
            onClick={handlePrevPage}
            disabled={currentPage === 0 || loading}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              currentPage === 0 || loading
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700'
            }`}
          >
            Previous
          </button>

          <span className="text-gray-700 font-medium">
            Page {currentPage + 1} of {totalPages}
          </span>

          <button
            onClick={handleNextPage}
            disabled={currentPage >= totalPages - 1 || loading}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              currentPage >= totalPages - 1 || loading
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700'
            }`}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;