import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchUserCards } from '../../store/slices/cardSlice';
import { TiltCard } from '../../components/common/TiltCard';
import { SkeletonCard } from '../../components/common/Skeleton';
import { notifySuccess, notifyError } from '../../store/useNotificationStore';
import { extractPrimaryColor, buildCardGradient, DEFAULT_CARD_GRADIENT, isLightColor } from '../../utils/colorUtils';

interface CardItem {
  id?: string;
  slug?: string;
  templateId?: string;
  profileData?: string;
  aiAvatarUrl?: string;
  isActive?: boolean;
}

interface ProfileData {
  fullName?: string;
  title?: string;
  bio?: string;
  avatarUrl?: string;
  phone?: string;
  email?: string;
}

function parseProfile(card: CardItem): ProfileData {
  if (!card.profileData) return {};
  try {
    const parsed = JSON.parse(card.profileData);
    return typeof parsed === 'object' && parsed !== null ? parsed : {};
  } catch {
    return {};
  }
}

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { cards, loading, error, totalPages } = useSelector((state: any) => state.card);

  useEffect(() => {
    dispatch(fetchUserCards({}));
  }, [dispatch]);

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">Card Dashboard</h1>
          <p className="mt-1 text-sm text-white/50">
            {cards.length > 0 ? `${cards.length} active digital card${cards.length === 1 ? '' : 's'}` : 'Craft your professional identity'}
          </p>
        </div>
        <button
          onClick={() => navigate('/dashboard/cards/create')}
          className="btn-primary"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
          </svg>
          Create New Card
        </button>
      </div>

      {/* Error state with retry */}
      {error && (
        <div className="glass-panel mb-8 flex flex-wrap items-center justify-between gap-4 border-rose-400/30 p-5">
          <div className="flex items-center gap-3">
            <span className="flex h-10 w-10 items-center justify-center rounded-full bg-rose-500/20 text-rose-300">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
              </svg>
            </span>
            <div>
              <p className="text-sm font-semibold text-white">Couldn't load your cards</p>
              <p className="text-xs text-white/50">{error}</p>
            </div>
          </div>
          <button onClick={() => dispatch(fetchUserCards({}))} className="btn-secondary text-xs">
            Retry
          </button>
        </div>
      )}

      {/* Loading skeletons */}
      {loading && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && cards.length === 0 && (
        <div className="glass-panel flex flex-col items-center justify-center px-6 py-20 text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white shadow-xl shadow-fuchsia-900/40">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 11.25v8.25a1.5 1.5 0 01-1.5 1.5H4.5A1.5 1.5 0 013 19.5v-8.25m18 0L12 4.5 3 11.25m18 0l-9-6.75m-9 6.75l9-6.75" />
            </svg>
          </div>
          <h2 className="mt-5 text-xl font-semibold text-white">No digital cards yet</h2>
          <p className="mt-1 max-w-sm text-sm text-white/50">
            Create your first card and share a stunning, tailored pocket square of your professional self.
          </p>
          <button
            onClick={() => navigate('/dashboard/cards/create')}
            className="btn-primary mt-6"
          >
            Create your first card
          </button>
        </div>
      )}

      {/* Card grid */}
      {!loading && cards.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {cards.map((card: CardItem) => (
            <CardPreview key={card.id || card.slug} card={card} />
          ))}
        </div>
      )}

      {/* Pagination hint */}
      {!loading && totalPages > 1 && (
        <p className="mt-6 text-center text-xs text-white/40">
          {totalPages} page{totalPages === 1 ? '' : 's'} of cards
        </p>
      )}
    </div>
  );
};

/** A digital card preview with 3D tilt, brand-color gradient, and copy-link. */
function CardPreview({ card }: { card: CardItem }) {
  const profile = useMemo(() => parseProfile(card), [card]);
  const avatar = profile.avatarUrl || card.aiAvatarUrl || '';
  const [gradient, setGradient] = useState<string>(DEFAULT_CARD_GRADIENT);
  const [lightText, setLightText] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const apply = async () => {
      const hex = avatar ? await extractPrimaryColor(avatar) : DEFAULT_CARD_GRADIENT;
      if (cancelled) return;
      const grad = hex.startsWith('#') ? buildCardGradient(hex) : hex;
      setGradient(grad);
      setLightText(hex.startsWith('#') ? isLightColor(hex) : false);
    };
    apply();
    return () => {
      cancelled = true;
    };
  }, [avatar]);

  const copyLink = async () => {
    const url = `${window.location.origin}/c/${card.slug}`;
    try {
      await navigator.clipboard.writeText(url);
      notifySuccess('Link copied', 'Your card link is on the clipboard');
    } catch {
      notifyError('Copy failed', 'Could not access the clipboard');
    }
  };

  const name = profile.fullName || card.slug || 'Your Name';
  const title = profile.title || card.templateId || '';

  return (
    <TiltCard className="group h-full">
      <div
        className="relative flex h-full min-h-[13rem] flex-col justify-between overflow-hidden p-6"
        style={{ background: gradient }}
      >
        {/* Sheen + noise for a premium physical feel */}
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_10%,rgba(255,255,255,0.28),transparent_55%)]" />
        <div className="pointer-events-none absolute inset-0 opacity-30 mix-blend-overlay bg-[linear-gradient(110deg,transparent_35%,rgba(255,255,255,0.25)_50%,transparent_65%)] transition-transform duration-700 group-hover:translate-x-6" />

        <div className="tilt-card-inner relative">
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-center gap-3">
              {avatar ? (
                <img src={avatar} alt={name} className="h-11 w-11 rounded-xl border border-white/40 object-cover shadow-lg" />
              ) : (
                <div className={`flex h-11 w-11 items-center justify-center rounded-xl border border-white/40 text-lg font-bold shadow-lg ${lightText ? 'text-black/60' : 'text-white/80'}`}>
                  {name.charAt(0).toUpperCase()}
                </div>
              )}
              <div>
                <p className={`text-base font-bold leading-tight ${lightText ? 'text-black/70' : 'text-white'}`}>{name}</p>
                {title && (
                  <p className={`text-xs ${lightText ? 'text-black/50' : 'text-white/70'}`}>{title}</p>
                )}
              </div>
            </div>
            <span
              className={`rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider backdrop-blur-sm ${
                card.isActive === false
                  ? 'bg-black/30 text-white/70'
                  : lightText
                  ? 'bg-black/15 text-black/70'
                  : 'bg-white/25 text-white'
              }`}
            >
              {card.isActive === false ? 'Inactive' : 'Active'}
            </span>
          </div>
        </div>

        {/* Simulated card chip + number for realism */}
        <div className="tilt-card-inner relative mt-8">
          <div className={`h-7 w-9 rounded-md ${lightText ? 'bg-black/25' : 'bg-white/30'} backdrop-blur-sm`}>
            <div className="mx-auto mt-2 h-2.5 w-5 rounded-sm bg-gradient-to-br from-yellow-200/90 to-yellow-400/70" />
          </div>
        </div>

        {/* Actions */}
        <div className="relative mt-6 flex items-center justify-between gap-3">
          <button
            onClick={copyLink}
            className={`flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-semibold backdrop-blur-sm transition-all duration-200 active:scale-[0.96] ${
              lightText ? 'bg-black/15 text-black/70 hover:bg-black/25' : 'bg-white/20 text-white hover:bg-white/30'
            }`}
          >
            <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
            </svg>
            Copy Link
          </button>
          <span className={`text-[11px] font-medium ${lightText ? 'text-black/50' : 'text-white/60'}`}>
            /c/{card.slug}
          </span>
        </div>
      </div>
    </TiltCard>
  );
}

export default DashboardPage;
