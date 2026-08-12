import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchMyCards, updateMyCard, deleteMyCard } from '../../store/slices/cardSlice';
import { SkeletonCard } from '../../components/common/Skeleton';
import { notifySuccess, notifyError } from '../../store/useNotificationStore';
import { extractPrimaryColor, buildCardGradient, DEFAULT_CARD_GRADIENT, isLightColor } from '../../utils/colorUtils';
import { ROUTES } from '../../utils/constants';

interface CardItem {
  id?: string;
  userId?: string;
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

/**
 * "My Cards" — full management view for the current user's digital cards.
 * Replaces the placeholder at /dashboard/cards with a frosted-glass grid of
 * card tiles (copy link / open public / edit / activate / delete) plus
 * loading skeletons and a first-run empty state.
 */
export const MyCardsPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { cards, loading, error } = useSelector((state: any) => state.card);

  const [editingCard, setEditingCard] = useState<CardItem | null>(null);
  const [deletingCard, setDeletingCard] = useState<CardItem | null>(null);
  const [busyCardId, setBusyCardId] = useState<string | null>(null);

  useEffect(() => {
    dispatch(fetchMyCards());
  }, [dispatch]);

  const refresh = () => dispatch(fetchMyCards());

  const toggleStatus = async (card: CardItem) => {
    if (busyCardId) return;
    const cardKey = card.id || card.slug || '';
    setBusyCardId(cardKey);
    const result = await dispatch(updateMyCard({ isActive: !card.isActive }));
    if (updateMyCard.fulfilled.match(result)) {
      notifySuccess(
        card.isActive ? 'Card deactivated' : 'Card activated',
        card.isActive
          ? 'Your public card is no longer visible'
          : 'Your card is now live and shareable'
      );
      refresh();
    } else {
      notifyError('Could not update card', (result.payload as string) || 'Failed to update card');
    }
    setBusyCardId(null);
  };

  const confirmDelete = async () => {
    if (!deletingCard) return;
    const result = await dispatch(deleteMyCard());
    if (deleteMyCard.fulfilled.match(result)) {
      notifySuccess('Card deleted', 'Your digital card has been removed');
      setDeletingCard(null);
      refresh();
    } else {
      notifyError('Could not delete card', (result.payload as string) || 'Failed to delete card');
    }
  };

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      {/* Header */}
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">My Cards</h1>
          <p className="mt-1 text-sm text-white/50">
            {cards.length > 0
              ? `${cards.length} digital card${cards.length === 1 ? '' : 's'} in your collection`
              : 'Design, share, and manage your digital presence'}
          </p>
        </div>
        <button onClick={() => navigate('/dashboard/cards/create')} className="btn-primary">
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
          <button onClick={refresh} className="btn-secondary text-xs">
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
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z" />
            </svg>
          </div>
          <h2 className="mt-5 text-xl font-semibold text-white">Create your first card</h2>
          <p className="mt-1 max-w-sm text-sm text-white/50">
            Design a stunning digital card and start collecting leads — it takes less than a minute.
          </p>
          <button
            onClick={() => navigate('/dashboard/cards/create')}
            className="btn-primary mt-6"
          >
            Create Your First Card
          </button>
        </div>
      )}

      {/* Card grid */}
      {!loading && cards.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          {cards.map((card: CardItem) => (
            <CardGridCard
              key={card.id || card.slug}
              card={card}
              busy={busyCardId === (card.id || card.slug || '')}
              onEdit={() => setEditingCard(card)}
              onToggle={() => toggleStatus(card)}
              onDelete={() => setDeletingCard(card)}
            />
          ))}
        </div>
      )}

      {/* Edit modal */}
      {editingCard && (
        <EditCardModal
          card={editingCard}
          onClose={() => setEditingCard(null)}
          onSaved={() => {
            setEditingCard(null);
            refresh();
          }}
        />
      )}

      {/* Delete confirmation modal */}
      {deletingCard && (
        <DeleteCardModal
          card={deletingCard}
          onCancel={() => setDeletingCard(null)}
          onConfirm={confirmDelete}
        />
      )}
    </div>
  );
};

/** One management tile: branded gradient preview + quick-action buttons. */
function CardGridCard({
  card,
  busy,
  onEdit,
  onToggle,
  onDelete,
}: {
  card: CardItem;
  busy: boolean;
  onEdit: () => void;
  onToggle: () => void;
  onDelete: () => void;
}) {
  const profile = useMemo(() => parseProfile(card), [card]);
  const avatar = profile.avatarUrl || card.aiAvatarUrl || '';
  const [gradient, setGradient] = useState<string>(DEFAULT_CARD_GRADIENT);
  const [lightText, setLightText] = useState(false);

  // Smart color coordination: derive the gradient from the uploaded avatar.
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

  const active = card.isActive !== false;
  const name = profile.fullName || card.slug || 'Your Name';
  const title = profile.title || card.templateId || '';

  const copyLink = async () => {
    const url = `${window.location.origin}${ROUTES.PUBLIC_CARD(card.slug || '')}`;
    try {
      await navigator.clipboard.writeText(url);
      notifySuccess('Link copied', 'Your card link is on the clipboard');
    } catch {
      notifyError('Copy failed', 'Could not access the clipboard');
    }
  };

  const openPublic = () => {
    window.open(ROUTES.PUBLIC_CARD(card.slug || ''), '_blank', 'noopener,noreferrer');
  };

  const actionBtn =
    'flex items-center justify-center gap-1.5 rounded-xl border border-white/15 bg-white/5 px-3 py-2 text-xs font-semibold text-white/80 transition-all duration-200 hover:bg-white/15 hover:text-white active:scale-[0.96] disabled:cursor-not-allowed disabled:opacity-50';

  return (
    <div className="glass-card group flex h-full flex-col overflow-hidden">
      {/* Branded preview strip */}
      <div
        className="relative flex h-40 flex-col justify-between overflow-hidden p-5"
        style={{ background: gradient }}
      >
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_10%,rgba(255,255,255,0.28),transparent_55%)]" />
        <div className="pointer-events-none absolute inset-0 opacity-30 mix-blend-overlay bg-[linear-gradient(110deg,transparent_35%,rgba(255,255,255,0.25)_50%,transparent_65%)] transition-transform duration-700 group-hover:translate-x-6" />

        <div className="relative flex items-start justify-between gap-3">
          <div className="flex min-w-0 items-center gap-3">
            {avatar ? (
              <img src={avatar} alt={name} className="h-11 w-11 shrink-0 rounded-xl border border-white/40 object-cover shadow-lg" />
            ) : (
              <div
                className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-white/40 text-lg font-bold shadow-lg ${
                  lightText ? 'text-black/60' : 'text-white/80'
                }`}
              >
                {name.charAt(0).toUpperCase()}
              </div>
            )}
            <div className="min-w-0">
              <p className={`truncate text-base font-bold leading-tight ${lightText ? 'text-black/70' : 'text-white'}`}>{name}</p>
              {title && (
                <p className={`truncate text-xs ${lightText ? 'text-black/50' : 'text-white/70'}`}>{title}</p>
              )}
            </div>
          </div>

          <button
            onClick={onDelete}
            disabled={busy}
            title="Delete card"
            className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg backdrop-blur-sm transition-all duration-200 active:scale-[0.94] ${
              lightText
                ? 'bg-black/15 text-black/60 hover:bg-red-600/90 hover:text-white'
                : 'bg-white/15 text-white/80 hover:bg-red-600/90 hover:text-white'
            }`}
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
            </svg>
          </button>
        </div>

        <div className="relative flex items-center justify-between gap-3">
          <span className={`truncate text-[11px] font-medium ${lightText ? 'text-black/50' : 'text-white/60'}`}>
            /c/{card.slug}
          </span>
          <span
            className={`shrink-0 rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider backdrop-blur-sm ${
              active
                ? lightText
                  ? 'bg-black/15 text-black/70'
                  : 'bg-white/25 text-white'
                : 'bg-black/30 text-white/70'
            }`}
          >
            {active ? 'Active' : 'Inactive'}
          </span>
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-2 gap-2 p-4">
        <button onClick={copyLink} disabled={busy} className={actionBtn}>
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
          </svg>
          Copy Link
        </button>
        <button onClick={openPublic} disabled={busy} className={actionBtn}>
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
          </svg>
          Open Public
        </button>
        <button onClick={onEdit} disabled={busy} className={actionBtn}>
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L6.832 19.82a4.5 4.5 0 01-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 011.13-1.897L16.863 4.487zm0 0L19.5 7.125" />
          </svg>
          Edit
        </button>
        <button onClick={onToggle} disabled={busy} className={actionBtn}>
          {busy ? (
            <svg className="h-3.5 w-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
            </svg>
          ) : (
            <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5.636 5.636a9 9 0 1012.728 0M12 3v9" />
            </svg>
          )}
          {active ? 'Deactivate' : 'Activate'}
        </button>
      </div>
    </div>
  );
}

/** Frosted-glass modal shell used by the edit and delete dialogs. */
function ModalShell({ onClose, children }: { onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div
        role="dialog"
        aria-modal="true"
        className="relative w-full max-w-lg rounded-2xl border border-white/15 bg-slate-900/90 p-6 shadow-2xl shadow-black/40 backdrop-blur-2xl"
      >
        {children}
      </div>
    </div>
  );
}

/** Edit form — PUT /api/v1/cards/me with the core profile fields. */
function EditCardModal({
  card,
  onClose,
  onSaved,
}: {
  card: CardItem;
  onClose: () => void;
  onSaved: () => void;
}) {
  const dispatch = useDispatch<any>();
  const profile = useMemo(() => parseProfile(card), [card]);

  const [form, setForm] = useState({
    slug: card.slug || '',
    fullName: profile.fullName || '',
    title: profile.title || '',
    bio: profile.bio || '',
    phone: profile.phone || '',
    email: profile.email || '',
  });
  const [saving, setSaving] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    const result = await dispatch(
      updateMyCard({
        slug: form.slug.toLowerCase().replace(/[^a-z0-9-]/g, '-'),
        profileData: {
          fullName: form.fullName,
          title: form.title,
          bio: form.bio,
          phone: form.phone,
          email: form.email,
          avatarUrl: profile.avatarUrl,
        },
      })
    );
    if (updateMyCard.fulfilled.match(result)) {
      notifySuccess('Card updated', 'Your changes are now live');
      onSaved();
    } else {
      notifyError('Could not save changes', (result.payload as string) || 'Failed to update card');
      setSaving(false);
    }
  };

  return (
    <ModalShell onClose={onClose}>
      <div className="mb-5 flex items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-white">Edit Card</h2>
          <p className="mt-0.5 text-xs text-white/50">Updates are published to your public card immediately.</p>
        </div>
        <button
          onClick={onClose}
          className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/15 bg-white/5 text-white/60 transition-colors hover:bg-white/15 hover:text-white"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1.5 block text-sm font-medium text-white/70">Card URL Slug</label>
          <input
            type="text"
            name="slug"
            required
            minLength={3}
            maxLength={100}
            value={form.slug}
            onChange={handleChange}
            className="input-field"
          />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-white/70">Full Name</label>
            <input type="text" name="fullName" value={form.fullName} onChange={handleChange} className="input-field" />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-white/70">Professional Title</label>
            <input type="text" name="title" value={form.title} onChange={handleChange} className="input-field" />
          </div>
        </div>
        <div>
          <label className="mb-1.5 block text-sm font-medium text-white/70">Short Bio</label>
          <textarea name="bio" rows={3} value={form.bio} onChange={handleChange} className="input-field resize-none" />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-white/70">Phone</label>
            <input type="text" name="phone" value={form.phone} onChange={handleChange} className="input-field" />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-white/70">Email</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} className="input-field" />
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="btn-secondary px-5 py-2 text-xs">
            Cancel
          </button>
          <button type="submit" disabled={saving} className="btn-primary px-5 py-2 text-xs">
            {saving ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      </form>
    </ModalShell>
  );
}

/** Delete confirmation dialog. */
function DeleteCardModal({
  card,
  onCancel,
  onConfirm,
}: {
  card: CardItem;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  const profile = useMemo(() => parseProfile(card), [card]);
  const name = profile.fullName || card.slug || 'this card';

  return (
    <ModalShell onClose={onCancel}>
      <div className="mb-5 flex items-start gap-4">
        <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-red-500/20 text-red-300">
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
          </svg>
        </span>
        <div>
          <h2 className="text-xl font-semibold text-white">Delete “{name}”?</h2>
          <p className="mt-0.5 text-sm text-white/50">
            This permanently removes your digital card and its public link. This action cannot be undone.
          </p>
        </div>
      </div>
      <div className="flex items-center justify-end gap-3">
        <button onClick={onCancel} className="btn-secondary px-5 py-2 text-xs">
          Cancel
        </button>
        <button onClick={onConfirm} className="btn-danger px-5 py-2 text-xs">
          Delete Card
        </button>
      </div>
    </ModalShell>
  );
}

export default MyCardsPage;
