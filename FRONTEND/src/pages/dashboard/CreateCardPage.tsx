import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { createCard } from '../../store/slices/cardSlice';
import { ImageUpload } from '../../components/common/ImageUpload';
import { TiltCard } from '../../components/common/TiltCard';
import { notifySuccess, notifyError } from '../../store/useNotificationStore';
import { extractPrimaryColor, buildCardGradient, DEFAULT_CARD_GRADIENT, isLightColor } from '../../utils/colorUtils';

interface FormState {
  slug: string;
  templateId: string;
  profileData: {
    fullName: string;
    title: string;
    bio: string;
    avatarUrl: string;
    phone: string;
    email: string;
  };
}

const INITIAL_FORM: FormState = {
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
};

const FIELD_LABELS: Record<string, string> = {
  fullName: 'Full Name',
  title: 'Professional Title',
  bio: 'Short Bio',
  phone: 'Phone Number',
  email: 'Email',
};

export const CreateCardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state: any) => state.card);

  const [formData, setFormData] = useState<FormState>(INITIAL_FORM);
  const [gradient, setGradient] = useState<string>(DEFAULT_CARD_GRADIENT);
  const [lightText, setLightText] = useState(false);

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
      slug: e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '-'),
    });
  };

  const avatarUrl = formData.profileData.avatarUrl;

  // Smart Color Coordination: derive the card gradient from the uploaded logo/avatar.
  useEffect(() => {
    let cancelled = false;
    const apply = async () => {
      if (!avatarUrl) {
        if (!cancelled) {
          setGradient(DEFAULT_CARD_GRADIENT);
          setLightText(false);
        }
        return;
      }
      const hex = await extractPrimaryColor(avatarUrl);
      if (cancelled) return;
      setGradient(hex.startsWith('#') ? buildCardGradient(hex) : hex);
      setLightText(hex.startsWith('#') ? isLightColor(hex) : false);
    };
    apply();
    return () => {
      cancelled = true;
    };
  }, [avatarUrl]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const resultAction = await dispatch(createCard(formData));

    if (createCard.fulfilled.match(resultAction)) {
      notifySuccess('Card saved', 'Your digital card is ready to share');
      navigate('/dashboard/cards');
    } else {
      notifyError('Could not save card', (resultAction.payload as string) || 'Failed to create card');
    }
  };

  const preview = useMemo(
    () => ({
      name: formData.profileData.fullName || 'Your Name',
      title: formData.profileData.title || 'Professional Title',
      email: formData.profileData.email,
      phone: formData.profileData.phone,
      slug: formData.slug || 'your-slug',
    }),
    [formData]
  );

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-white">Create New Digital Card</h1>
        <p className="mt-1 text-sm text-white/50">Design a card as distinctive as your signature</p>
      </div>

      {error && !loading && (
        <div className="glass-panel mb-6 border-rose-400/30 p-4 text-sm text-rose-200">{error}</div>
      )}

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-5">
        {/* Form */}
        <form onSubmit={handleSubmit} className="glass-panel space-y-6 p-6 lg:col-span-3 lg:p-8">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-white/70">Card URL Slug (Unique Identifier)</label>
            <div className="flex items-center overflow-hidden rounded-xl border border-white/15 bg-white/5 focus-within:border-fuchsia-400/60 focus-within:ring-2 focus-within:ring-fuchsia-400/25">
              <span className="border-r border-white/10 px-3 py-2.5 text-sm text-white/35">cardpro.ai/c/</span>
              <input
                type="text"
                required
                placeholder="john-doe"
                value={formData.slug}
                onChange={handleSlugChange}
                className="w-full bg-transparent px-4 py-2.5 text-sm text-white placeholder-white/35 outline-none"
              />
            </div>
          </div>

          <ImageUpload
            label="Profile Avatar / Logo"
            currentImage={formData.profileData.avatarUrl}
            onUploadSuccess={(url) => {
              setFormData({
                ...formData,
                profileData: { ...formData.profileData, avatarUrl: url },
              });
              notifySuccess('Avatar uploaded', 'Your brand color was detected automatically');
            }}
          />

          {(Object.keys(FIELD_LABELS) as Array<keyof FormState['profileData']>).map((field) => (
            <div key={field}>
              <label className="mb-1.5 block text-sm font-medium text-white/70">{FIELD_LABELS[field]}</label>
              {field === 'bio' ? (
                <textarea
                  name={field}
                  rows={3}
                  value={formData.profileData[field]}
                  onChange={handleChange}
                  className="input-field resize-none"
                />
              ) : (
                <input
                  type={field === 'email' ? 'email' : 'text'}
                  name={field}
                  required={field === 'fullName'}
                  value={formData.profileData[field]}
                  onChange={handleChange}
                  placeholder={field === 'phone' ? '+1 555 010 2030' : undefined}
                  className="input-field"
                />
              )}
            </div>
          ))}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? (
              <>
                <svg className="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
                </svg>
                Creating Card…
              </>
            ) : (
              'Create Card'
            )}
          </button>
        </form>

        {/* Live preview */}
        <div className="lg:col-span-2">
          <div className="sticky top-24">
            <p className="mb-3 text-sm font-medium text-white/60">Live Preview</p>
            <TiltCard className="h-full">
              <div
                className="relative flex h-[17rem] flex-col justify-between overflow-hidden rounded-2xl p-6"
                style={{ background: gradient }}
              >
                <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_10%,rgba(255,255,255,0.28),transparent_55%)]" />
                <div className="tilt-card-inner relative">
                  <div className="flex items-center gap-3">
                    {avatarUrl ? (
                      <img src={avatarUrl} alt="Avatar" className="h-12 w-12 rounded-xl border border-white/40 object-cover shadow-lg" />
                    ) : (
                      <div className={`flex h-12 w-12 items-center justify-center rounded-xl border border-white/40 text-xl font-bold ${lightText ? 'text-black/60' : 'text-white/80'}`}>
                        {preview.name.charAt(0).toUpperCase()}
                      </div>
                    )}
                    <div>
                      <p className={`text-lg font-bold leading-tight ${lightText ? 'text-black/70' : 'text-white'}`}>{preview.name}</p>
                      <p className={`text-sm ${lightText ? 'text-black/50' : 'text-white/70'}`}>{preview.title}</p>
                    </div>
                  </div>
                </div>

                <div className="tilt-card-inner relative">
                  <div className={`h-7 w-9 rounded-md ${lightText ? 'bg-black/25' : 'bg-white/30'} backdrop-blur-sm`}>
                    <div className="mx-auto mt-2 h-2.5 w-5 rounded-sm bg-gradient-to-br from-yellow-200/90 to-yellow-400/70" />
                  </div>
                </div>

                <div className="tilt-card-inner relative space-y-1.5">
                  {(preview.email || preview.phone) && (
                    <div className={`space-y-1 text-sm ${lightText ? 'text-black/60' : 'text-white/80'}`}>
                      {preview.email && <p className="truncate">{preview.email}</p>}
                      {preview.phone && <p className="truncate">{preview.phone}</p>}
                    </div>
                  )}
                  <p className={`text-[11px] font-medium ${lightText ? 'text-black/45' : 'text-white/55'}`}>
                    cardpro.ai/c/{preview.slug}
                  </p>
                </div>
              </div>
            </TiltCard>

            <p className="mt-4 text-xs leading-relaxed text-white/40">
              Hover the preview to inspect the card's physical tilt. The gradient is derived from your uploaded logo's dominant color.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateCardPage;
