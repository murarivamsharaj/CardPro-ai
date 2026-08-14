import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  BarChart3,
  Menu,
  Palette,
  PenLine,
  QrCode,
  Send,
  Share2,
  ShieldCheck,
  Sparkles,
  TrendingUp,
  UserPlus,
  Users,
  X,
  Zap,
} from 'lucide-react';
import { BackgroundFX } from '../../components/common/BackgroundFX';

/* ─────────────────────────────────────────────
   CardPro AI — Landing Page
   Premium dark SaaS aesthetic (violet → fuchsia accents)
   ───────────────────────────────────────────── */

const NAV_LINKS = [
  { label: 'Features', href: '#features' },
  { label: 'How It Works', href: '#how-it-works' },
];

const FEATURES = [
  {
    icon: Zap,
    title: 'Instant Sharing',
    description:
      'Share your card with a tap, QR code, or one link. No apps, no friction — your contact info travels at the speed of a text message.',
    accent: 'from-violet-500 to-purple-600',
  },
  {
    icon: Users,
    title: 'Lead Capture',
    description:
      'Every tap on your card becomes a lead. Capture names, emails, and context automatically so you never lose a connection again.',
    accent: 'from-fuchsia-500 to-pink-600',
  },
  {
    icon: BarChart3,
    title: 'Analytics',
    description:
      'See exactly who viewed and saved your card. Track profile visits and lead volume with a clean, real-time analytics dashboard.',
    accent: 'from-purple-500 to-fuchsia-600',
  },
  {
    icon: Palette,
    title: 'Custom Themes',
    description:
      'Make it unmistakably yours with AI-generated bios, custom gradients, and a card that matches your personal brand perfectly.',
    accent: 'from-indigo-500 to-violet-600',
  },
];

const STEPS = [
  {
    icon: UserPlus,
    step: '01',
    title: 'Sign Up',
    description: 'Create your free CardPro AI account in under 30 seconds. No credit card required.',
  },
  {
    icon: PenLine,
    step: '02',
    title: 'Customize Card',
    description: 'Add your details, photo, and branding. Our AI even writes your bio for you.',
  },
  {
    icon: Send,
    step: '03',
    title: 'Share & Collect Leads',
    description: 'Share your link or QR code anywhere. Watch leads roll into your dashboard.',
  },
];

export default function LandingPage() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="relative min-h-screen overflow-x-clip bg-[#0b0a1f] text-white">
      <BackgroundFX />

      {/* ── Navigation ── */}
      <header className="sticky top-0 z-40 border-b border-white/10 bg-[#0b0a1f]/70 backdrop-blur-xl">
        <nav className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          {/* Logo */}
          <a href="#" className="flex items-center gap-2.5" aria-label="CardPro AI home">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-lg font-black text-white shadow-lg shadow-fuchsia-900/50">
              C
            </span>
            <span className="text-lg font-bold tracking-tight">
              CardPro <span className="bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent">AI</span>
            </span>
          </a>

          {/* Desktop links */}
          <div className="hidden items-center gap-8 md:flex">
            {NAV_LINKS.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-sm font-medium text-white/60 transition-colors hover:text-white"
              >
                {link.label}
              </a>
            ))}
          </div>

          {/* Desktop CTAs */}
          <div className="hidden items-center gap-3 md:flex">
            <Link to="/login" className="btn-secondary px-5 py-2 text-sm">
              Sign In
            </Link>
            <Link to="/signup" className="btn-primary px-5 py-2 text-sm">
              Get Started
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMenuOpen((open) => !open)}
            className="rounded-lg p-2 text-white/60 transition-colors hover:bg-white/10 hover:text-white md:hidden"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
          >
            {menuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </nav>

        {/* Mobile menu */}
        {menuOpen && (
          <div className="border-t border-white/10 bg-[#0b0a1f]/95 px-4 pb-6 pt-3 backdrop-blur-xl md:hidden">
            <div className="flex flex-col gap-1">
              {NAV_LINKS.map((link) => (
                <a
                  key={link.href}
                  href={link.href}
                  onClick={() => setMenuOpen(false)}
                  className="rounded-xl px-4 py-3 text-sm font-medium text-white/70 transition-colors hover:bg-white/10 hover:text-white"
                >
                  {link.label}
                </a>
              ))}
            </div>
            <div className="mt-4 flex flex-col gap-3">
              <Link to="/login" className="btn-secondary w-full justify-center">
                Sign In
              </Link>
              <Link to="/signup" className="btn-primary w-full justify-center">
                Get Started
                <ArrowRight className="h-4 w-4" />
              </Link>
            </div>
          </div>
        )}
      </header>

      {/* ── Hero ── */}
      <section className="relative mx-auto max-w-7xl px-4 pb-24 pt-16 sm:px-6 sm:pt-24 lg:px-8 lg:pb-32 lg:pt-32">
        <div className="mx-auto max-w-3xl text-center">
          {/* Badge */}
          <span className="glass-chip mb-6 border-fuchsia-400/30 bg-fuchsia-500/10 px-4 py-1.5 text-xs font-semibold uppercase tracking-widest text-fuchsia-200">
            <Sparkles className="h-3.5 w-3.5" />
            Powered by AI
          </span>

          <h1 className="text-balance text-4xl font-black leading-[1.08] tracking-tight sm:text-6xl lg:text-7xl">
            The Ultimate{' '}
            <span className="bg-gradient-to-r from-violet-400 via-fuchsia-400 to-pink-400 bg-clip-text text-transparent">
              AI-Powered
            </span>{' '}
            Digital Business Card
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-balance text-base text-white/60 sm:text-lg lg:text-xl">
            Capture leads, share instantly, and stand out everywhere. Turn every handshake and QR scan into a
            connection your business actually remembers.
          </p>

          {/* CTAs */}
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link to="/signup" className="btn-primary w-full px-8 py-3.5 text-base sm:w-auto">
              Create Your Free Card
              <ArrowRight className="h-5 w-5" />
            </Link>
            <a
              href="#how-it-works"
              className="btn-secondary w-full px-8 py-3.5 text-base sm:w-auto"
            >
              See How It Works
            </a>
          </div>

          <p className="mt-6 flex items-center justify-center gap-2 text-xs text-white/40 sm:text-sm">
            <ShieldCheck className="h-4 w-4 text-emerald-400" />
            Free forever plan · No credit card required · Set up in 30 seconds
          </p>
        </div>

        {/* Hero visual — mock digital card */}
        <div className="relative mx-auto mt-16 max-w-3xl lg:mt-20">
          <div
            aria-hidden="true"
            className="absolute -inset-x-10 -top-10 bottom-0 -z-10 rounded-full bg-gradient-to-r from-violet-600/30 via-fuchsia-600/25 to-purple-600/30 blur-3xl"
          />
          <div className="glass-card group relative overflow-hidden p-6 sm:p-8">
            {/* Card gradient surface */}
            <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-violet-600 via-purple-600 to-fuchsia-600 p-6 shadow-2xl shadow-fuchsia-900/40 sm:p-8">
              <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_10%,rgba(255,255,255,0.28),transparent_55%)]" />
              <div className="pointer-events-none absolute inset-0 opacity-25 mix-blend-overlay bg-[linear-gradient(110deg,transparent_35%,rgba(255,255,255,0.25)_50%,transparent_65%)]" />

              {/* Card header */}
              <div className="relative flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-white/40 bg-white/15 text-lg font-bold shadow-lg backdrop-blur-sm sm:h-14 sm:w-14 sm:text-xl">
                    A
                  </div>
                  <div className="text-left">
                    <p className="text-base font-bold sm:text-lg">Alex Morgan</p>
                    <p className="text-xs text-white/80 sm:text-sm">Founder · CardPro AI</p>
                  </div>
                </div>
                <span className="rounded-full bg-white/20 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider backdrop-blur-sm sm:text-xs">
                  Live
                </span>
              </div>

              {/* Card chip + link */}
              <div className="relative mt-8 flex items-end justify-between sm:mt-12">
                <div>
                  <div className="h-7 w-9 rounded-md bg-white/25 backdrop-blur-sm sm:h-8 sm:w-11">
                    <div className="mx-auto mt-2 h-2.5 w-5 rounded-sm bg-gradient-to-br from-yellow-200/90 to-yellow-400/70" />
                  </div>
                  <p className="mt-3 font-mono text-sm tracking-widest text-white/90 sm:text-base">
                    cardpro.ai/c/alex
                  </p>
                </div>
                <div className="flex h-14 w-14 items-center justify-center rounded-xl border border-white/40 bg-white/15 backdrop-blur-sm sm:h-16 sm:w-16">
                  <QrCode className="h-8 w-8 text-white sm:h-9 sm:w-9" />
                </div>
              </div>
            </div>

            {/* Floating stat chips */}
            <div className="mt-6 grid grid-cols-3 gap-3">
              {[
                { icon: Users, label: 'New Leads', value: '128' },
                { icon: TrendingUp, label: 'Profile Views', value: '1.4k' },
                { icon: Zap, label: 'Shares', value: '342' },
              ].map((stat) => (
                <div key={stat.label} className="rounded-xl border border-white/10 bg-white/5 p-3 text-center sm:p-4">
                  <stat.icon className="mx-auto h-4 w-4 text-fuchsia-300 sm:h-5 sm:w-5" />
                  <p className="mt-1.5 text-base font-bold sm:text-lg">{stat.value}</p>
                  <p className="text-[10px] text-white/50 sm:text-xs">{stat.label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ── Features Grid ── */}
      <section id="features" className="relative mx-auto max-w-7xl scroll-mt-20 px-4 py-20 sm:px-6 lg:px-8 lg:py-28">
        <div className="mx-auto max-w-2xl text-center">
          <span className="glass-chip mb-5 uppercase tracking-widest text-fuchsia-200">Features</span>
          <h2 className="text-balance text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">
            Everything you need to <span className="bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent">network smarter</span>
          </h2>
          <p className="mt-4 text-base text-white/60 sm:text-lg">
            A pocket-sized powerhouse that replaces paper cards, stale contact lists, and guesswork.
          </p>
        </div>

        <div className="mt-14 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((feature) => (
            <div
              key={feature.title}
              className="glass-card group flex flex-col p-6 transition-all duration-300 hover:-translate-y-1 hover:border-fuchsia-400/30"
            >
              <div
                className={`mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${feature.accent} text-white shadow-lg shadow-fuchsia-900/30 transition-transform duration-300 group-hover:scale-110`}
              >
                <feature.icon className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-semibold text-white">{feature.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-white/55">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── How It Works ── */}
      <section id="how-it-works" className="relative mx-auto max-w-7xl scroll-mt-20 px-4 py-20 sm:px-6 lg:px-8 lg:py-28">
        <div className="mx-auto max-w-2xl text-center">
          <span className="glass-chip mb-5 uppercase tracking-widest text-fuchsia-200">How It Works</span>
          <h2 className="text-balance text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">
            Live in <span className="bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent">three simple steps</span>
          </h2>
          <p className="mt-4 text-base text-white/60 sm:text-lg">
            From sign-up to your first captured lead in minutes — not days.
          </p>
        </div>

        <div className="relative mt-16 grid grid-cols-1 gap-10 md:grid-cols-3 md:gap-8">
          {/* Connecting line (desktop) */}
          <div
            aria-hidden="true"
            className="absolute left-[16.66%] right-[16.66%] top-8 hidden h-px bg-gradient-to-r from-violet-500/50 via-fuchsia-500/50 to-violet-500/50 md:block"
          />

          {STEPS.map((step) => (
            <div key={step.step} className="relative flex flex-col items-center text-center">
              <div className="relative z-10 flex h-16 w-16 items-center justify-center rounded-2xl border border-fuchsia-400/40 bg-[#15122b] text-fuchsia-300 shadow-xl shadow-fuchsia-900/30">
                <step.icon className="h-7 w-7" />
                <span className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-fuchsia-600 text-[10px] font-bold text-white shadow-lg">
                  {step.step}
                </span>
              </div>
              <h3 className="mt-6 text-lg font-semibold text-white">{step.title}</h3>
              <p className="mt-2 max-w-xs text-sm leading-relaxed text-white/55">{step.description}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA Banner ── */}
      <section className="mx-auto max-w-7xl px-4 pb-20 sm:px-6 lg:px-8 lg:pb-28">
        <div className="glass-panel relative overflow-hidden px-6 py-14 text-center sm:px-12 lg:py-20">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -left-20 -top-24 h-72 w-72 rounded-full bg-violet-600/30 blur-3xl"
          />
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -bottom-24 -right-20 h-72 w-72 rounded-full bg-fuchsia-600/30 blur-3xl"
          />
          <div className="relative">
            <h2 className="text-balance text-3xl font-bold tracking-tight sm:text-4xl lg:text-5xl">
              Your next connection is <span className="bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent">one tap away</span>
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-base text-white/60 sm:text-lg">
              Join thousands of professionals who swapped paper for pixel. Your free card is waiting.
            </p>
            <Link to="/signup" className="btn-primary mt-8 px-8 py-3.5 text-base">
              Create Your Free Card
              <ArrowRight className="h-5 w-5" />
            </Link>
          </div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="border-t border-white/10">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-6 px-4 py-10 sm:px-6 md:flex-row lg:px-8">
          <div className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-violet-600 to-fuchsia-600 text-sm font-black text-white">
              C
            </span>
            <span className="text-sm font-semibold text-white/70">CardPro AI</span>
          </div>

          <p className="text-center text-xs text-white/40 sm:text-sm">
            © {new Date().getFullYear()} CardPro AI. All rights reserved.
          </p>

          <div className="flex items-center gap-6">
            {['Privacy', 'Terms', 'Contact'].map((label) => (
              <a
                key={label}
                href="#"
                onClick={(e) => e.preventDefault()}
                className="text-xs text-white/50 transition-colors hover:text-white sm:text-sm"
              >
                {label}
              </a>
            ))}
          </div>
        </div>
      </footer>
    </div>
  );
}
