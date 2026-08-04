import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProducts } from '../../store/slices/productSlice';

export const ProductListPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  
  const { products, totalPages, currentPage, loading, error } = useSelector((state: any) => state.product);

  const [searchTerm, setSearchTerm] = useState('');

  // Fetch products with debounce whenever searchTerm or currentPage changes
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      dispatch(fetchProducts({ search: searchTerm, page: currentPage, size: 10 }));
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, currentPage, dispatch]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      dispatch(fetchProducts({ search: searchTerm, page: currentPage + 1, size: 10 }));
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      dispatch(fetchProducts({ search: searchTerm, page: currentPage - 1, size: 10 }));
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Product Catalog</h1>

      {/* Search Bar */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Search products by name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full max-w-md p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {loading && <p className="text-blue-600 mb-4">Loading products...</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      {!loading && (!products || products.length === 0) && (
        <p className="text-gray-600">No products found.</p>
      )}

      {/* Products Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {products && products.map((product: any) => (
          <div key={product.id} className="p-5 bg-white rounded-lg shadow-md border border-gray-200 hover:shadow-lg transition-shadow">
            <h3 className="text-xl font-semibold mb-2">{product.name}</h3>
            <p className="text-gray-500 text-sm mb-3 line-clamp-2">{product.description}</p>
            <div className="flex justify-between items-center mt-auto">
              <span className="font-bold text-lg">${product.price?.toFixed(2)}</span>
              <span className={`text-xs font-bold px-2 py-1 rounded ${product.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                {product.active ? 'In Stock' : 'Out of Stock'}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination Controls */}
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
          Page {totalPages === 0 ? 0 : currentPage + 1} of {totalPages}
        </span>

        <button
          onClick={handleNextPage}
          disabled={currentPage >= totalPages - 1 || loading || totalPages === 0}
          className={`px-4 py-2 rounded-lg font-medium transition-colors ${
            currentPage >= totalPages - 1 || loading || totalPages === 0
              ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
              : 'bg-blue-600 text-white hover:bg-blue-700'
          }`}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default ProductListPage;