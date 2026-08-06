import React, { useState } from 'react';
import { uploadFile } from '../../services/fileService';

interface ImageUploadProps {
  label: string;
  currentImage?: string;
  onUploadSuccess: (url: string) => void;
}

export const ImageUpload: React.FC<ImageUploadProps> = ({ label, currentImage, onUploadSuccess }) => {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(currentImage || '');

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      const url = await uploadFile(file);
      setPreview(url);
      onUploadSuccess(url);
    } catch (error) {
      console.error('Failed to upload image:', error);
      alert('Failed to upload image. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-2">
      <label className="block text-sm font-medium text-gray-700">{label}</label>
      <div className="flex items-center gap-4">
        {preview ? (
          <img
            src={preview}
            alt="Avatar Preview"
            className="h-16 w-16 rounded-full object-cover border border-gray-300 shadow-sm"
          />
        ) : (
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gray-100 border border-gray-300 text-gray-400">
            No Image
          </div>
        )}
        <input
          type="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={uploading}
          className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-primary-50 file:text-primary-700 hover:file:bg-primary-100"
        />
      </div>
      {uploading && <p className="text-xs text-blue-600">Uploading image...</p>}
    </div>
  );
};