import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { createCard } from '../../store/slices/cardSlice';
import { ImageUpload } from '../../components/common/ImageUpload';

export const CreateCardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state: any) => state.card);

  const [formData, setFormData] = useState({
    slug: '',
    templateId: 'default',
    profileData: {
      fullName: '',
      title: '',
      bio: '',
      avatarUrl: '',
      phone: '',
      email: '',
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      profileData: {
        ...formData.profileData,
        [name]: value,
      },
    });
  };

  const handleSlugChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      slug: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const resultAction = await dispatch(createCard(formData));
    
    if (createCard.fulfilled.match(resultAction)) {
      toast.success('Digital card created successfully! 🎉');
      navigate('/dashboard/cards');
    } else {
      toast.error((resultAction.payload as string) || 'Failed to create card');
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-8 bg-white shadow rounded-lg mt-8">
      <h1 className="text-2xl font-bold mb-6">Create New Digital Card</h1>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-700">Card URL Slug (Unique Identifier)</label>
          <input
            type="text"
            required
            placeholder="e.g. john-doe"
            value={formData.slug}
            onChange={handleSlugChange}
            className="mt-1 block w-full rounded-md border border-gray-300 p-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>

        <ImageUpload
          label="Profile Avatar / Logo"
          currentImage={formData.profileData.avatarUrl}
          onUploadSuccess={(url) => {
            setFormData({
              ...formData,
              profileData: { ...formData.profileData, avatarUrl: url },
            });
            toast.success('Avatar uploaded successfully!');
          }}
        />

        <div>
          <label className="block text-sm font-medium text-gray-700">Full Name</label>
          <input
            type="text"
            name="fullName"
            required
            value={formData.profileData.fullName}
            onChange={handleChange}
            className="mt-1 block w-full rounded-md border border-gray-300 p-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700">Professional Title</label>
          <input
            type="text"
            name="title"
            placeholder="e.g. Senior Software Engineer"
            value={formData.profileData.title}
            onChange={handleChange}
            className="mt-1 block w-full rounded-md border border-gray-300 p-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700">Short Bio</label>
          <textarea
            name="bio"
            rows={3}
            value={formData.profileData.bio}
            onChange={handleChange}
            className="mt-1 block w-full rounded-md border border-gray-300 p-2 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white font-semibold py-2 px-4 rounded-md hover:bg-blue-700 transition disabled:opacity-50"
        >
          {loading ? 'Creating Card...' : 'Create Card'}
        </button>
      </form>
    </div>
  );
};

export default CreateCardPage;