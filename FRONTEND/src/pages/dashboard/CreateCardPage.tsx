import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { createCard, fetchMyCards } from '../../store/slices/cardSlice';
import { ImageUpload } from '../../components/common/ImageUpload';
import { resolveAvatarUrl } from '../../services/fileService';
import { TiltCard } from '../../components/common/TiltCard';
import { notifySuccess, notifyError } from '../../store/useNotificationStore';
import { extractPrimaryColor, buildCardGradient, DEFAULT_CARD_GRADIENT, isLightColor } from '../../utils/colorUtils';
import { generateCardDetails } from '../../services/aiService';
import { TemplateGallery } from '../../components/common/TemplateGallery';
import { templateGradient } from '../../utils/templateConstants';

interface FormState {
  slug: string;
  templateId: string;
  address: string;
  gender: string;
  socialLinks: Record<string, string>;
  profileData: {
    fullName: string;
    title: string;
    tagline: string;
    bio: string;
    avatarUrl: string;
    phone: string;
    email: string;
  };
}

const INITIAL_FORM: FormState = {
  slug: '',
  templateId: 'default',
  address: '',
  gender: '',
  socialLinks: {
    linkedin: '',
    github: '',
    twitter: '',
    instagram: '',
    youtube: '',
    website: '',
    whatsapp: '',
  },
  profileData: {
    fullName: '',
    title: '',
    tagline: '',
    bio: '',
    avatarUrl: '',
    phone: '',
    email: '',
  },
};

/** Social platforms editable in the form (key → label + placeholder). */
const SOCIAL_FIELDS: { key: string; label: string; placeholder: string }[] = [
  { key: 'linkedin', label: 'LinkedIn', placeholder: 'https://linkedin.com/in/username' },
  { key: 'github', label: 'GitHub', placeholder: 'https://github.com/username' },
  { key: 'twitter', label: 'Twitter / X', placeholder: 'https://x.com/username' },
  { key: 'instagram', label: 'Instagram', placeholder: 'https://instagram.com/username' },
  { key: 'youtube', label: 'YouTube', placeholder: 'https://youtube.com/@channel' },
  { key: 'website', label: 'Website / Portfolio', placeholder: 'https://yourwebsite.com' },
  { key: 'whatsapp', label: 'WhatsApp', placeholder: 'https://wa.me/15550102030' },
];

const FIELD_LABELS: Record<string, string> = {
  fullName: 'Full Name',
  title: 'Professional Title',
  tagline: 'Tagline',
  bio: 'Short Bio',
  phone: 'Phone Number',
  email: 'Email',
};


export const CreateCardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state: any) => state.card);
  // The user's own card carries the purchased-entitlement flags (one card per
  // user is normalized into an array by fetchMyCards).
  const currentCard = useSelector((state: any) => state.card.cards?.[0] || null);
  const premiumTemplatesUnlocked = !!currentCard?.premiumTemplatesUnlocked;

  useEffect(() => {
    dispatch(fetchMyCards());
  }, [dispatch]);

  const [formData, setFormData] = useState<FormState>(INITIAL_FORM);
  const [gradient, setGradient] = useState<string>(DEFAULT_CARD_GRADIENT);
  const [lightText, setLightText] = useState(false);

  // ✨ Magic Autofill
  const [aiPrompt, setAiPrompt] = useState('');
  const [aiGenerating, setAiGenerating] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);

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

  const handleSocialChange = (key: string, value: string) => {
    setFormData({
      ...formData,
      socialLinks: { ...formData.socialLinks, [key]: value.trim() },
    });
  };

  const handleSlugChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      slug: e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, '-'),
    });
  };

  const avatarUrl = formData.profileData.avatarUrl;

  /** Calls the ai-service and drops the suggestions into the editable fields. */
  const handleMagicAutofill = async () => {
    if (!aiPrompt.trim()) {
      notifyError('Add keywords first', 'Describe your role or industry so the AI has something to work with.');
      return;
    }
    setAiGenerating(true);
    setAiError(null);
    try {
      const suggestions = await generateCardDetails(aiPrompt.trim());
      setFormData((prev) => ({
        ...prev,
        profileData: {
          ...prev.profileData,
          title: suggestions.suggestedJobTitle,
          tagline: suggestions.suggestedTagline,
          bio: suggestions.suggestedBio,
        },
      }));
      notifySuccess(
        'Magic Autofill applied',
        'Review the suggested title, tagline, and bio — edit them freely before saving.'
      );
    } catch (err) {
      console.error('Magic Autofill failed:', err);
      setAiError('Could not reach the AI service. Please try again.');
      notifyError('Autofill failed', 'Could not reach the AI service. Please try again.');
    } finally {
      setAiGenerating(false);
    }
  };

  // Smart Color Coordination: derive the card gradient from the uploaded logo/avatar.
  useEffect(() => {
    let cancelled = false;
    const apply = async () => {
      if (!avatarUrl) {
        if (!cancelled) {
          // No avatar → the selected template's own gradient styles the preview.
          setGradient(templateGradient(formData.templateId));
          setLightText(false);
        }
        return;
      }
      const hex = await extractPrimaryColor(resolveAvatarUrl(avatarUrl));
      if (cancelled) return;
      setGradient(hex.startsWith('#') ? buildCardGradient(hex) : hex);
      setLightText(hex.startsWith('#') ? isLightColor(hex) : false);
    };
    apply();
    return () => {
      cancelled = true;
    };
  }, [avatarUrl, formData.templateId]);

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
          {/* ✨ Magic Autofill — AI-drafted title, tagline, and bio */}
          <div className="rounded-2xl border border-fuchsia-400/25 bg-gradient-to-br from-violet-600/10 to-fuchsia-600/10 p-5">
            <div className="mb-3 flex items-center gap-2.5">
              <span className="text-xl">✨</span>
              <div>
                <p className="text-sm font-semibold text-white">Magic Autofill</p>
                <p className="text-xs text-white/50">
                  Drop in a few keywords and AI drafts your job title, tagline, and bio — keep or edit them.
                </p>
              </div>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row">
              <input
                type="text"
                value={aiPrompt}
                onChange={(e) => setAiPrompt(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleMagicAutofill();
                  }
                }}
                placeholder="e.g. sales manager, fintech, SaaS, 8 years experience"
                disabled={aiGenerating}
                className="input-field flex-1"
              />
              <button type="button" onClick={handleMagicAutofill} disabled={aiGenerating} className="btn-primary shrink-0">
                {aiGenerating ? (
                  <>
                    <svg className="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
                    </svg>
                    Generating…
                  </>
                ) : (
                  '✨ Generate'
                )}
              </button>
            </div>
            {aiError && <p className="mt-2 text-xs text-rose-300">{aiError}</p>}
          </div>

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

          {/* Card template picker — premium designs gate behind the Store purchase */}
          <div>
            <p className="mb-1.5 block text-sm font-medium text-white/70">Card Template</p>
            <TemplateGallery
              selectedTemplateId={formData.templateId}
              premiumTemplatesUnlocked={premiumTemplatesUnlocked}
              onSelect={(templateId) => setFormData({ ...formData, templateId })}
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

          <div className="border-t border-white/10 pt-6">
            <label className="mb-1.5 block text-sm font-medium text-white/70">Gender</label>
            <select
              name="gender"
              value={formData.gender}
              onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
              className="input-field appearance-none"
            >
              <option value="">Prefer not to say</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Custom">Custom</option>
            </select>
            <p className="mt-1 text-xs text-white/40">Optional — included on your digital card profile.</p>
          </div>

          <div className="border-t border-white/10 pt-6">
            <label className="mb-1.5 block text-sm font-medium text-white/70">Address / Location</label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              placeholder="123 Main Street, Bengaluru, India"
              className="input-field"
            />
            <p className="mt-1 text-xs text-white/40">
              Shown on your public card with a quick-open Google Maps link.
            </p>
          </div>

          <div className="border-t border-white/10 pt-6">
            <p className="mb-1.5 block text-sm font-medium text-white/70">Social Media Profiles</p>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              {SOCIAL_FIELDS.map((social) => (
                <div key={social.key}>
                  <label className="mb-1.5 block text-xs font-medium text-white/60">{social.label}</label>
                  <input
                    type="url"
                    value={formData.socialLinks[social.key] || ''}
                    onChange={(e) => handleSocialChange(social.key, e.target.value)}
                    placeholder={social.placeholder}
                    className="input-field"
                  />
                </div>
              ))}
            </div>
          </div>

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
                      <img src={resolveAvatarUrl(avatarUrl)} alt="Avatar" className="h-12 w-12 rounded-xl border border-white/40 object-cover shadow-lg" />
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
