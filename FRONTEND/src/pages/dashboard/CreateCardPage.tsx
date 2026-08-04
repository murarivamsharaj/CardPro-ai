import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { createCard } from '../../store/slices/cardSlice'; // Adjust import path if needed

export const CreateCardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  
  const [slug, setSlug] = useState('');
  const [templateId, setTemplateId] = useState('1');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // 👇 Now we are sending exactly what your Java backend demands
      await dispatch(createCard({ 
        slug, 
        templateId,
        profileData: {
          name: "Chittipoola Murari",
          title: "Software Developer",
          bio: "Learning Java and React!"
        }
      })).unwrap();
      
      // Navigate back to dashboard once successfully saved
      navigate('/dashboard');
    } catch (err: any) {
      setError(err || 'Failed to create card. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="p-8 max-w-xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Create New Digital Card</h1>
      
      {error && <p className="mb-4 text-red-500 bg-red-50 p-3 rounded-lg border border-red-200">{error}</p>}

      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow border border-gray-200">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Card Slug</label>
          <input
            type="text"
            required
            value={slug}
            onChange={(e) => setSlug(e.target.value)}
            placeholder="e.g. murari-card"
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Template ID</label>
          <input
            type="text"
            required
            value={templateId}
            onChange={(e) => setTemplateId(e.target.value)}
            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>

        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={loading}
            className={`px-5 py-2.5 font-medium rounded-lg text-white transition ${
              loading ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'
            }`}
          >
            {loading ? 'Saving...' : 'Save Card'}
          </button>
          
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="px-5 py-2.5 bg-gray-200 text-gray-700 font-medium rounded-lg hover:bg-gray-300 transition"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateCardPage;