import React, { useRef, useState } from 'react';
import { uploadFile, resolveAvatarUrl } from '../../services/fileService';
import { notifyError, notifyInfo } from '../../store/useNotificationStore';

interface ImageUploadProps {
  label: string;
  currentImage?: string;
  onUploadSuccess: (url: string) => void;
}

/**
 * Avatar / logo uploader with a simulated "AI Enhance Photo Quality"
 * toggle: flipping it runs a scanning animation over the image to mimic
 * an AI restoration pipeline bringing the photo to stunning 4K clarity.
 */
export const ImageUpload: React.FC<ImageUploadProps> = ({ label, currentImage, onUploadSuccess }) => {
  const [uploading, setUploading] = useState(false);
  // Stale blob: URLs from a previous session can never load again, so treat
  // them as "no image" rather than rendering a permanently broken preview.
  // Server-relative /api/... paths are resolved against the API Gateway so
  // the <img> never 404s on the Vite dev server / static host port.
  const [preview, setPreview] = useState(
    currentImage && !currentImage.startsWith('blob:') ? resolveAvatarUrl(currentImage) : ''
  );
  const [enhanceEnabled, setEnhanceEnabled] = useState(false);
  const [enhancing, setEnhancing] = useState(false);
  const [enhanced, setEnhanced] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Show the image instantly via a local object URL, then swap it for the
    // persistent URL (server path or base64 data URL) once the upload settles.
    const objectUrl = URL.createObjectURL(file);
    setPreview(objectUrl);
    setEnhanced(false);
    setUploading(true);
    try {
      const url = await uploadFile(file);
      URL.revokeObjectURL(objectUrl);
      setPreview(resolveAvatarUrl(url));
      onUploadSuccess(url);
    } catch (error) {
      console.error('Failed to upload image:', error);
      // Keep the local preview so the user still sees their image, but do not
      // persist a blob: URL — the next page load would render a broken avatar.
      notifyError('Upload failed', 'The preview stays local — the image was not saved. Please try again.');
    } finally {
      setUploading(false);
    }
  };

  const handleToggleEnhance = () => {
    const next = !enhanceEnabled;
    setEnhanceEnabled(next);
    if (!next) return;
    if (!preview) {
      notifyInfo('No image yet', 'Upload an avatar first, then enhance it.');
      setEnhanceEnabled(false);
      return;
    }
    // Simulate the AI restoration pass with a scanning animation.
    setEnhancing(true);
    setEnhanced(false);
    setTimeout(() => {
      setEnhancing(false);
      setEnhanced(true);
      notifyInfo('Photo enhanced', 'AI restoration complete — 4K clarity applied.');
    }, 2600);
  };

  return (
    <div className="space-y-3">
      <label className="block text-sm font-medium text-white/80">{label}</label>

      <div className="flex flex-wrap items-center gap-5">
        {/* Preview with scanning overlay */}
        <div className="relative h-20 w-20">
          {preview ? (
            <img
              src={preview}
              alt="Avatar Preview"
              className={`h-20 w-20 rounded-2xl border border-white/20 object-cover shadow-lg shadow-black/30 backdrop-blur-sm transition-all duration-500 ${
                enhanced ? 'ring-2 ring-fuchsia-400/80 shadow-fuchsia-900/40' : ''
              }`}
            />
          ) : (
            <div className="flex h-20 w-20 items-center justify-center rounded-2xl border border-dashed border-white/25 bg-white/5 text-white/40">
              <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
              </svg>
            </div>
          )}

          {/* AI scanning overlay */}
          {enhancing && preview && (
            <div className="scan-overlay">
              <div className="scan-line" />
              <div className="absolute inset-x-0 top-1/2 -translate-y-1/2 text-center">
                <span className="rounded-full bg-slate-900/80 px-3 py-1 text-[10px] font-semibold uppercase tracking-widest text-fuchsia-200 backdrop-blur-sm">
                  AI Restoring…
                </span>
              </div>
            </div>
          )}

          {enhanced && !enhancing && (
            <span className="absolute -bottom-2 -right-2 rounded-full bg-gradient-to-r from-violet-600 to-fuchsia-600 px-2 py-0.5 text-[10px] font-bold text-white shadow-lg shadow-fuchsia-900/50">
              4K ✨
            </span>
          )}
        </div>

        <div className="min-w-0 flex-1 space-y-2.5">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            disabled={uploading}
            className="block w-full text-sm text-white/50 file:mr-4 file:rounded-xl file:border-0 file:bg-white/10 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white file:backdrop-blur-sm file:transition-colors hover:file:bg-white/20"
          />
          {uploading && <p className="text-xs text-fuchsia-300">Uploading image…</p>}
        </div>
      </div>

      {/* AI Enhance Photo Quality toggle */}
      <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 backdrop-blur-sm">
        <button
          type="button"
          role="switch"
          aria-checked={enhanceEnabled}
          onClick={handleToggleEnhance}
          className="toggle-track"
          data-on={enhanceEnabled}
        >
          <span className="toggle-thumb" />
        </button>
        <div className="flex-1">
          <p className="text-sm font-semibold text-white/90">Enhance Photo Quality</p>
          <p className="text-xs text-white/50">
            AI restoration to stunning 4K clarity
          </p>
        </div>
        {enhancing ? (
          <span className="text-xs font-medium text-fuchsia-300">Scanning…</span>
        ) : enhanced ? (
          <span className="glass-chip text-fuchsia-200">Enhanced</span>
        ) : null}
      </div>
    </div>
  );
};

export default ImageUpload;
