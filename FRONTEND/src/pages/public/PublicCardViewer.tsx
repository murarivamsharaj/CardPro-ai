import React, { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchPublicCard, parseProfileData, PublicCardProfile, PublicCardResponse } from '../../services/publicCardService';

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

function CardContent({ card }: { card: PublicCardResponse }) {
  const profile = useMemo(() => parseProfileData(card.profileData), [card.profileData]);
  // blob: URLs only live in the browser session that created them — a public
  // visitor on another device can never load them, so fall back to initials.
  const avatar = profile.avatarUrl && !profile.avatarUrl.startsWith('blob:') ? profile.avatarUrl : card.aiAvatarUrl || '';
  const name = profile.fullName || card.slug || 'Your Name';
  const title = profile.title || '';

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

  const links = useMemo(
    () =>
      [
        { label: 'LinkedIn', href: profile.linkedin, icon: LinkIcon },
        { label: 'GitHub', href: profile.github, icon: LinkIcon },
        { label: 'Website', href: profile.website || profile.portfolio, icon: LinkIcon },
        { label: 'YouTube', href: profile.youtube, icon: LinkIcon },
        { label: 'Instagram', href: profile.instagram, icon: LinkIcon },
      ].filter((l) => l.href),
    [profile]
  );

  return (
    <div className="flex flex-col gap-4">
      {/* Profile header */}
      <div className="glass-panel flex flex-col items-center px-6 py-8 text-center">
        {avatar ? (
          <img src={avatar} alt={name} className="h-24 w-24 rounded-full border-2 border-white/20 object-cover shadow-xl" />
        ) : (
          <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-fuchsia-600 text-3xl font-bold text-white shadow-xl shadow-fuchsia-900/40">
            {name.charAt(0).toUpperCase()}
          </div>
        )}
        <h1 className="mt-4 text-2xl font-bold tracking-tight text-white">{name}</h1>
        {title && <p className="mt-1 text-sm font-medium text-fuchsia-300">{title}</p>}
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
              className="glass-card group flex flex-col items-center gap-2 px-2 py-4 text-center"
            >
              <action.icon className="h-6 w-6 text-fuchsia-300 transition-transform duration-200 group-hover:scale-110" />
              <span className="text-xs font-semibold text-white/80">{action.label}</span>
            </a>
          ))}
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
      {links.length > 0 && (
        <div className="glass-panel p-5">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-white/50">Connect</p>
          <div className="flex flex-col gap-2">
            {links.map((link) => (
              <a
                key={link.label}
                href={link.href!}
                target="_blank"
                rel="noreferrer"
                className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-sm font-medium text-white/80 transition-all duration-200 hover:border-white/25 hover:bg-white/10 hover:text-white"
              >
                <link.icon className="h-4 w-4 text-fuchsia-300" />
                {link.label}
                <svg className="ml-auto h-4 w-4 text-white/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
                </svg>
              </a>
            ))}
          </div>
        </div>
      )}

      {/* Save contact (vCard) */}
      <SaveContactButton profile={profile} name={name} title={title} />

      <p className="mt-2 text-center text-[11px] text-white/30">
        Powered by CardPro — {card.slug ? `cardpro.ai/c/${card.slug}` : ''}
      </p>
    </div>
  );
}

function SaveContactButton({ profile, name, title }: { profile: PublicCardProfile; name: string; title: string }) {
  const [saved, setSaved] = useState(false);

  const handleSave = () => {
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

function LinkIcon({ className }: IconProps) {
  return (
    <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
    </svg>
  );
}
