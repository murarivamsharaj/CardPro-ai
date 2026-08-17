import React, { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  fetchPublicCard,
  parseProfileData,
  submitLead,
  trackCardEvent,
  PublicCardProfile,
  PublicCardResponse,
} from '../../services/publicCardService';
import { resolveAvatarUrl } from '../../services/fileService';

type ViewState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; card: PublicCardResponse };

/**
 * Public digital card viewer (route: /c/:slug).
 *
 * This page is rendered OUTSIDE any auth guard and intentionally never
 * redirects unauthenticated visitors to /login — a public profile must be
 * viewable by anyone with the link.
 */
export default function PublicCardViewer() {
  const { slug = '' } = useParams<{ slug: string }>();
  const [state, setState] = useState<ViewState>({ status: 'loading' });

  useEffect(() => {
    let cancelled = false;
    setState({ status: 'loading' });

    fetchPublicCard(slug)
      .then((card) => {
        if (!cancelled) setState({ status: 'ready', card });
      })
      .catch(() => {
        if (!cancelled) {
          setState({ status: 'error', message: 'This card could not be found or is no longer available.' });
        }
      });

    return () => {
      cancelled = true;
    };
  }, [slug]);

  // Real-time analytics: record a PAGE_VIEW every time the public card loads.
  // Fire-and-forget, unauthenticated, attributed to the card via its profile id.
  useEffect(() => {
    if (state.status === 'ready' && state.card.id) {
      trackCardEvent(state.card.id, 'PAGE_VIEW');
    }
  }, [state]);

  if (state.status === 'loading') {
    return <ViewerShell>{<LoadingSkeleton />}</ViewerShell>;
  }

  if (state.status === 'error') {
    return <ViewerShell>{<NotFound message={state.message} />}</ViewerShell>;
  }

  return <ViewerShell>{<CardContent card={state.card} />}</ViewerShell>;
}

/** Centered, mobile-first wrapper with the ambient aurora backdrop. */
function ViewerShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative min-h-screen bg-[#0b0a1f] text-white">
      <div className="app-backdrop" />
      <div className="relative mx-auto w-full max-w-md px-5 pb-14 pt-12">
        <div className="mb-8 flex items-center justify-center">
          <span className="flex items-center gap-2 text-sm font-semibold tracking-wide text-white/60">
            <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-violet-600 to-fuchsia-600 text-xs font-bold text-white">
              C
            </span>
            CardPro
          </span>
        </div>
        {children}
      </div>
    </div>
  );
}

function LoadingSkeleton() {
  return (
    <div className="flex flex-col items-center gap-5">
      <div className="skeleton h-24 w-24 rounded-full" />
      <div className="skeleton h-5 w-40" />
      <div className="skeleton h-3 w-56" />
      <div className="skeleton mt-2 h-20 w-full" />
      <div className="grid w-full grid-cols-3 gap-3">
        <div className="skeleton h-16 w-full" />
        <div className="skeleton h-16 w-full" />
        <div className="skeleton h-16 w-full" />
      </div>
    </div>
  );
}

function NotFound({ message }: { message: string }) {
  return (
    <div className="glass-panel flex flex-col items-center px-6 py-14 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-500/15 text-rose-300">
        <svg className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
      </div>
      <h2 className="mt-4 text-lg font-semibold text-white">Card not found</h2>
      <p className="mt-1 max-w-xs text-sm text-white/50">{message}</p>
      <Link to="/" className="btn-primary mt-6">
        Go to Homepage
      </Link>
    </div>
  );
}

/** Social platforms rendered in the Connect section, in display order. */
const SOCIAL_PLATFORMS: { key: string; label: string; icon: React.FC<IconProps> }[] = [
  { key: 'linkedin', label: 'LinkedIn', icon: LinkedInIcon },
  { key: 'github', label: 'GitHub', icon: GitHubIcon },
  { key: 'twitter', label: 'Twitter / X', icon: TwitterIcon },
  { key: 'instagram', label: 'Instagram', icon: InstagramIcon },
  { key: 'youtube', label: 'YouTube', icon: YouTubeIcon },
  { key: 'website', label: 'Website', icon: GlobeIcon },
  { key: 'whatsapp', label: 'WhatsApp', icon: WhatsAppIcon },
];

/** Legacy profileData fields that feed each platform before the schema expansion. */
const LEGACY_SOCIAL_FIELDS: Record<string, string[]> = {
  linkedin: ['linkedin'],
  github: ['github'],
  twitter: ['twitter'],
  instagram: ['instagram'],
  youtube: ['youtube'],
  website: ['website', 'portfolio'],
  whatsapp: ['whatsapp'],
};

/**
 * Maps a card's templateId to the CSS classes styling the hero card panel.
 * Matches the template gradient palette from the Create Card editor:
 * Classic / Minimal / Bold are free; Aurora / Neon / Gold are premium designs.
 * Unknown or missing ids fall back to the default glass-panel look (empty string).
 */
function getTemplateStyles(templateId?: string): string {
  switch (templateId) {
    case 'default':
      return 'bg-[linear-gradient(135deg,#312e81,#7c3aed)] border-white/20 shadow-xl shadow-indigo-900/40';
    case 'minimal':
      return 'bg-[linear-gradient(135deg,#1e293b,#475569)] border-white/20 shadow-xl shadow-slate-900/40';
    case 'bold':
      return 'bg-[linear-gradient(135deg,#be123c,#f97316)] border-white/20 shadow-xl shadow-rose-900/40';
    case 'aurora':
      return 'bg-[linear-gradient(135deg,#6d28d9,#d946ef)] border-white/25 shadow-xl shadow-fuchsia-900/50';
    case 'neon':
      return 'bg-[linear-gradient(135deg,#0f172a,#22d3ee)] border-white/25 shadow-xl shadow-cyan-900/50';
    case 'gold':
      return 'bg-[linear-gradient(135deg,#78350f,#d97706)] border-amber-300/30 shadow-xl shadow-amber-900/50';
    default:
      return '';
  }
}

function CardContent({ card }: { card: PublicCardResponse }) {
  const profile = useMemo(() => parseProfileData(card.profileData), [card.profileData]);
  // blob: URLs only live in the browser session that created them — a public
  // visitor on another device can never load them, so fall back to initials.
  // Server-relative /api/... paths are resolved against the API Gateway so
  // the avatar renders even when the card is served from a different origin.
  const avatar = resolveAvatarUrl(
    profile.avatarUrl && !profile.avatarUrl.startsWith('blob:') ? profile.avatarUrl : card.aiAvatarUrl || ''
  );
  const name = profile.fullName || card.slug || 'Your Name';
  const title = profile.title || '';

  // The selected template styles the hero card panel; the template gradient
  // takes priority over any avatar influence so premium designs stay visible.
  const templateClasses = getTemplateStyles(card.templateId ?? undefined);

  const skills = useMemo(
    () => (Array.isArray(profile.skills) ? (profile.skills as string[]).filter(Boolean).slice(0, 12) : []),
    [profile.skills]
  );

  const actions = useMemo(
    () =>
      [
        { key: 'call', label: 'Call', href: profile.phone ? `tel:${profile.phone}` : '', icon: PhoneIcon, hidden: !profile.phone },
        { key: 'whatsapp', label: 'WhatsApp', href: whatsAppHref(profile.phone), icon: WhatsAppIcon, hidden: !profile.phone },
        { key: 'email', label: 'Email', href: profile.email ? `mailto:${profile.email}` : '', icon: MailIcon, hidden: !profile.email },
      ].filter((a) => !a.hidden),
    [profile]
  );

  const address = card.address || profile.address || '';
  const mapsUrl = address ? `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}` : '';

  // Social links: the entity's social_links map is authoritative; legacy
  // profileData fields fill in the gaps for cards created before the expansion.
  const socials = useMemo(() => {
    const merged: Record<string, string> = { ...(card.socialLinks || {}) };
    for (const [key, legacyFields] of Object.entries(LEGACY_SOCIAL_FIELDS)) {
      if (!merged[key]) {
        for (const field of legacyFields) {
          const value = profile[field];
          if (typeof value === 'string' && value.trim()) {
            merged[key] = value.trim();
            break;
          }
        }
      }
    }
    return SOCIAL_PLATFORMS.filter((platform) => merged[platform.key]).map((platform) => ({
      ...platform,
      href: merged[platform.key]!,
    }));
  }, [card.socialLinks, profile]);

  /** Fire an interaction event before the browser acts on the click. */
  const fireClick = (eventType: 'BUTTON_CLICK' | 'SOCIAL_CLICK', linkLabel: string) => {
    if (card.id) trackCardEvent(card.id, eventType, linkLabel);
  };

  return (
    <div className="flex flex-col gap-4">
      {/* Profile header — styled by the selected template */}
      <div
        className={`relative isolate overflow-hidden flex flex-col items-center rounded-2xl px-6 py-8 text-center ${
          templateClasses || 'glass-panel'
        }`}
      >
        {/* Depth overlays so the template gradient reads as a designed card */}
        {templateClasses && (
          <>
            <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_20%_10%,rgba(255,255,255,0.25),transparent_55%)]" />
            <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(circle_at_80%_100%,rgba(0,0,0,0.35),transparent_60%)]" />
          </>
        )}
        {avatar ? (
          <img src={avatar} alt={name} className="h-24 w-24 rounded-full border-2 border-white/20 object-cover shadow-xl" />
        ) : (
          <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-fuchsia-600 text-3xl font-bold text-white shadow-xl shadow-fuchsia-900/40">
            {name.charAt(0).toUpperCase()}
          </div>
        )}
        <h1 className="mt-4 text-2xl font-bold tracking-tight text-white">{name}</h1>
        {title && <p className="mt-1 text-sm font-medium text-fuchsia-300">{title}</p>}
        {profile.tagline && <p className="mt-1 text-xs text-white/50">{profile.tagline}</p>}
        {profile.bio && <p className="mt-3 max-w-sm text-sm leading-relaxed text-white/60">{profile.bio}</p>}
      </div>

      {/* Quick actions */}
      {actions.length > 0 && (
        <div className="grid grid-cols-3 gap-3">
          {actions.map((action) => (
            <a
              key={action.key}
              href={action.href}
              target={action.href.startsWith('http') ? '_blank' : undefined}
              rel="noreferrer"
              onClick={() => fireClick('BUTTON_CLICK', action.label)}
              className="glass-card group flex flex-col items-center gap-2 px-2 py-4 text-center"
            >
              <action.icon className="h-6 w-6 text-fuchsia-300 transition-transform duration-200 group-hover:scale-110" />
              <span className="text-xs font-semibold text-white/80">{action.label}</span>
            </a>
          ))}
        </div>
      )}

      {/* Address / location with quick-open map link */}
      {address && (
        <div className="glass-panel p-5">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-white/50">Location</p>
          <a
            href={mapsUrl}
            target="_blank"
            rel="noreferrer"
            onClick={() => fireClick('BUTTON_CLICK', 'Location / Maps')}
            className="flex items-start gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-medium text-white/80 transition-all duration-200 hover:border-white/25 hover:bg-white/10 hover:text-white"
          >
            <MapPinIcon className="mt-0.5 h-4 w-4 shrink-0 text-fuchsia-300" />
            <span className="min-w-0 flex-1">{address}</span>
            <svg className="mt-0.5 h-4 w-4 shrink-0 text-white/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
            </svg>
          </a>
        </div>
      )}

      {/* Skills */}
      {skills.length > 0 && (
        <div className="glass-panel p-5">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-white/50">Skills</p>
          <div className="flex flex-wrap gap-2">
            {skills.map((skill) => (
              <span key={skill} className="glass-chip">
                {skill}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Social / portfolio links */}
      {socials.length > 0 && (
        <div className="glass-panel p-5">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-white/50">Connect</p>
          <div className="flex flex-col gap-2">
            {socials.map((social) => (
              <a
                key={social.key}
                href={social.href}
                target="_blank"
                rel="noreferrer"
                onClick={() => fireClick('SOCIAL_CLICK', social.label)}
                className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-medium text-white/80 transition-all duration-200 hover:border-white/25 hover:bg-white/10 hover:text-white"
              >
                <social.icon className="h-4 w-4 text-fuchsia-300" />
                {social.label}
                <svg className="ml-auto h-4 w-4 text-white/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
                </svg>
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Save contact (vCard) */}
      <SaveContactButton profileId={card.id ?? null} profile={profile} name={name} title={title} />

      {/* Contact Me — lets visitors send their info to the card owner */}
      <ContactMeForm profileId={card.id ?? null} />

      {/* Watermark footer — hidden when the owner (Pro) opted out of it */}
      {!card.removeWatermark && (
        <p className="mt-2 text-center text-[11px] text-white/30">
          Powered by CardPro — {card.slug ? `cardpro.ai/c/${card.slug}` : ''}
        </p>
      )}
    </div>
  );
}

function SaveContactButton({
  profileId,
  profile,
  name,
  title,
}: {
  profileId: string | null;
  profile: PublicCardProfile;
  name: string;
  title: string;
}) {
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    if (profileId) trackCardEvent(profileId, 'VCF_DOWNLOAD', 'vCard');
    const vcard = buildVCard(profile, name, title);
    const blob = new Blob([vcard], { type: 'text/vcard' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${sanitizeFileName(name)}.vcf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    setSaved(true);
    window.setTimeout(() => setSaved(false), 2500);
  };

  return (
    <button onClick={handleSave} className="btn-primary sticky bottom-4 w-full justify-center">
      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3" />
      </svg>
      {saved ? 'Contact Saved!' : 'Save Contact'}
    </button>
  );
}

/* ── Contact Me form ──────────────────────────────── */

type ContactFormStatus = 'idle' | 'sending' | 'success' | 'error';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function ContactMeForm({ profileId }: { profileId: string | null }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [message, setMessage] = useState('');
  const [status, setStatus] = useState<ContactFormStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const sending = status === 'sending';

  function validate(): boolean {
    const errors: Record<string, string> = {};
    if (!name.trim()) errors.name = 'Please enter your name';
    if (!email.trim()) errors.email = 'Please enter your email';
    else if (!EMAIL_PATTERN.test(email)) errors.email = 'Please enter a valid email address';
    if (!message.trim()) errors.message = 'Please enter a message';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setStatus('idle');
    setErrorMessage('');
    if (!validate()) return;

    if (!profileId) {
      setStatus('error');
      setErrorMessage("This card isn't accepting messages right now. Please try again later.");
      return;
    }

    setStatus('sending');
    try {
      await submitLead({
        profileId,
        visitorName: name.trim(),
        visitorEmail: email.trim(),
        visitorPhone: phone.trim() || undefined,
        message: message.trim(),
      });
      // Clear the form so the visitor can send another message if they like.
      setName('');
      setEmail('');
      setPhone('');
      setMessage('');
      setFieldErrors({});
      setStatus('success');
    } catch {
      setStatus('error');
      setErrorMessage('Something went wrong sending your message. Please try again.');
    }
  }

  return (
    <div className="glass-panel p-5">
      <div className="mb-4 flex items-center gap-3">
        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-fuchsia-600 shadow-lg shadow-fuchsia-900/40">
          <MailIcon className="h-5 w-5 text-white" />
        </span>
        <div>
          <h2 className="text-base font-semibold text-white">Contact Me</h2>
          <p className="text-xs text-white/50">Send a message — I'll get back to you soon.</p>
        </div>
      </div>

      {status === 'success' && (
        <div className="mb-4 flex items-start gap-2.5 rounded-xl border border-emerald-400/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200 backdrop-blur-sm">
          <svg className="mt-0.5 h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Message sent! I'll get back to you soon.
        </div>
      )}

      {status === 'error' && (
        <div className="mb-4 flex items-start gap-2.5 rounded-xl border border-rose-400/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200 backdrop-blur-sm">
          <svg className="mt-0.5 h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
          </svg>
          {errorMessage}
        </div>
      )}

      {!profileId && (
        <p className="mb-4 rounded-xl border border-amber-400/30 bg-amber-500/10 px-4 py-3 text-xs text-amber-200 backdrop-blur-sm">
          This card isn't accepting messages right now. Please try again later.
        </p>
      )}

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <div>
          <label htmlFor="contact-name" className="mb-1.5 block text-sm font-medium text-white/70">
            Name
          </label>
          <input
            id="contact-name"
            type="text"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              setStatus('idle');
            }}
            placeholder="Your name"
            className={`input-field ${fieldErrors.name ? 'input-error' : ''}`}
            autoComplete="name"
            disabled={sending}
          />
          {fieldErrors.name && <p className="mt-1 text-xs text-rose-300">{fieldErrors.name}</p>}
        </div>

        <div>
          <label htmlFor="contact-email" className="mb-1.5 block text-sm font-medium text-white/70">
            Email
          </label>
          <input
            id="contact-email"
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              setStatus('idle');
            }}
            placeholder="you@example.com"
            className={`input-field ${fieldErrors.email ? 'input-error' : ''}`}
            autoComplete="email"
            disabled={sending}
          />
          {fieldErrors.email && <p className="mt-1 text-xs text-rose-300">{fieldErrors.email}</p>}
        </div>

        <div>
          <label htmlFor="contact-phone" className="mb-1.5 block text-sm font-medium text-white/70">
            Phone <span className="text-white/35">(optional)</span>
          </label>
          <input
            id="contact-phone"
            type="tel"
            value={phone}
            onChange={(e) => {
              setPhone(e.target.value);
              setStatus('idle');
            }}
            placeholder="+1 555 010 2030"
            className="input-field"
            autoComplete="tel"
            disabled={sending}
          />
        </div>

        <div>
          <label htmlFor="contact-message" className="mb-1.5 block text-sm font-medium text-white/70">
            Note
          </label>
          <textarea
            id="contact-message"
            value={message}
            onChange={(e) => {
              setMessage(e.target.value);
              setStatus('idle');
            }}
            placeholder="What would you like to talk about?"
            rows={4}
            className={`input-field resize-none ${fieldErrors.message ? 'input-error' : ''}`}
            disabled={sending}
          />
          {fieldErrors.message && <p className="mt-1 text-xs text-rose-300">{fieldErrors.message}</p>}
        </div>

        <button type="submit" className="btn-primary w-full" disabled={sending || !profileId}>
          {sending ? (
            <>
              <svg className="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
              </svg>
              Sending...
            </>
          ) : (
            'Send Message'
          )}
        </button>
      </form>
    </div>
  );
}

/* ── Helpers ─────────────────────────────────────── */

function whatsAppHref(phone?: string): string {
  if (!phone) return '';
  const digits = phone.replace(/\D/g, '');
  return digits ? `https://wa.me/${digits}` : '';
}

function sanitizeFileName(value: string): string {
  return value.replace(/[^a-z0-9-_]/gi, '-').replace(/-+/g, '-').toLowerCase() || 'contact';
}

/** Builds a minimal RFC 2426 vCard from the profile fields. */
function buildVCard(profile: PublicCardProfile, name: string, title: string): string {
  const esc = (value?: string) =>
    (value || '').replace(/\\/g, '\\\\').replace(/,/g, '\\,').replace(/;/g, '\\;').replace(/\r?\n/g, '\\n');

  const lines = [
    'BEGIN:VCARD',
    'VERSION:3.0',
    `FN:${esc(name)}`,
    `N:${esc(name.split(' ').slice(1).join(' '))};${esc(name.split(' ')[0] || '')};;;`,
    title ? `TITLE:${esc(title)}` : '',
    profile.phone ? `TEL;TYPE=CELL:${esc(profile.phone)}` : '',
    profile.email ? `EMAIL;TYPE=INTERNET:${esc(profile.email)}` : '',
    profile.website || profile.portfolio ? `URL:${esc(profile.website || profile.portfolio)}` : '',
    'END:VCARD',
  ].filter(Boolean);

  return lines.join('\r\n') + '\r\n';
}

/* ── Icons (inline, dependency-free) ─────────────── */

type IconProps = { className?: string };

function PhoneIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 6.75c0 8.284 6.716 15 15 15h2.25a2.25 2.25 0 002.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106c-.44-.11-.902.055-1.173.417l-.97 1.293c-.282.376-.769.542-1.21.38a12.035 12.035 0 01-7.143-7.143c-.162-.441.004-.928.38-1.21l1.293-.97c.363-.271.527-.734.417-1.173L6.963 3.102a1.125 1.125 0 00-1.091-.852H4.5A2.25 2.25 0 002.25 4.5v2.25z" />
    </svg>
  );
}

function MailIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
    </svg>
  );
}

function WhatsAppIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
    </svg>
  );
}

function LinkedInIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.064 2.064 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z" />
    </svg>
  );
}

function GitHubIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12" />
    </svg>
  );
}

function TwitterIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z" />
    </svg>
  );
}

function InstagramIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 8.25V6a2.25 2.25 0 00-2.25-2.25H6A2.25 2.25 0 003.75 6v8.25A2.25 2.25 0 006 16.5h2.25m8.25-8.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-7.5A2.25 2.25 0 018.25 18v-1.5m8.25-8.25h-3.375a1.5 1.5 0 01-1.5-1.5V3.375a1.5 1.5 0 011.5-1.5h3.375a1.5 1.5 0 011.5 1.5v3.375a1.5 1.5 0 01-1.5 1.5zM12 15.75a3.75 3.75 0 100-7.5 3.75 3.75 0 000 7.5z" />
    </svg>
  );
}

function YouTubeIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M23.498 6.186a3.016 3.016 0 00-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 00.502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 002.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 002.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z" />
    </svg>
  );
}

function GlobeIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 21a9.004 9.004 0 008.716-6.747M12 21a9.004 9.004 0 01-8.716-6.747M12 21c2.485 0 4.5-4.03 4.5-9S14.485 3 12 3m0 18c-2.485 0-4.5-4.03-4.5-9S9.515 3 12 3m0 0a8.997 8.997 0 017.843 4.582M12 3a8.997 8.997 0 00-7.843 4.582m15.686 0A11.953 11.953 0 0112 10.5c-2.998 0-5.74-1.1-7.843-2.918m15.686 0A8.959 8.959 0 0121 12c0 .778-.099 1.533-.284 2.253m0 0A17.919 17.919 0 0112 16.5c-3.162 0-6.133-.815-8.716-2.247m0 0A9.015 9.015 0 013 12c0-1.605.42-3.113 1.157-4.418" />
    </svg>
  );
}

function MapPinIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
    </svg>
  );
}
