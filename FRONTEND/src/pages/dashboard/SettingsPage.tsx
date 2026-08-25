import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { notifySuccess, notifyError } from '../../store/useNotificationStore';
import { THEME_STORAGE_KEY } from '../../utils/constants';
import {
  getMyProfile,
  updateMyProfile,
  updateEmailNotifications,
  changePassword,
  getAllUsers,
  updateUserRole,
  updateUserStatus,
  softDeleteUser,
  getRegistrationConfig,
  setRegistrationConfig,
  getTotalCardCount,
  createProOrder,
  verifyProPayment,
  regenerateApiKey,
  updateWebhookUrl,
  deleteMyAccount,
  type UserProfile,
  type AdminUser,
} from '../../services/settingsService';
import { openRazorpayCheckout } from '../../services/razorpayService';

type ThemeMode = 'dark' | 'light';

function extractError(err: unknown, fallback: string): string {
  if (err && typeof err === 'object' && 'response' in err) {
    const axiosError = err as { response?: { data?: { error?: { message?: string }; message?: string } } };
    return (
      axiosError.response?.data?.error?.message ||
      axiosError.response?.data?.message ||
      fallback
    );
  }
  return err instanceof Error ? err.message : fallback;
}

/**
 * Props shared by the profile-aware sections. The profile is fetched once by
 * SettingsPage and distributed here; sections never fetch user-service
 * themselves — they render from this shared state and report writes back via
 * {@code onProfileChange}.
 */
interface ProfileAwareSectionProps {
  email?: string;
  /** Authoritative profile from user-service (null while loading / on failure). */
  profile: UserProfile | null;
  /** True while the initial profile fetch is still in flight. */
  profileLoading: boolean;
  /** Replace the shared profile after a successful write (also syncs AuthContext). */
  onProfileChange: (profile: UserProfile) => void;
}

/**
 * Settings — profile, security, appearance, notifications, and (for admins)
 * the Super Admin Command Center. Theme preference is purely client-side
 * (localStorage + data-theme on <html>); everything else hits the services
 * through the gateway or user-service directly.
 *
 * The authenticated user's profile is fetched from user-service EXACTLY ONCE
 * here and distributed to every section via props. That single source of truth
 * keeps Pro status, Profile Details, watermark, notifications, API key and
 * webhook in agreement with each other AND with the AuthContext (Navbar).
 */
export const SettingsPage: React.FC = () => {
  const { user, updateUser } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const email = user?.email;

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [profileLoading, setProfileLoading] = useState(true);

  /**
   * Fetches the authoritative profile from user-service and shares it with
   * every child section. Also heals any drift between the database and the
   * cached auth user so the Navbar PRO badge and ProSection can never disagree.
   */
  const refreshProfile = useCallback(async () => {
    if (!email) {
      setProfileLoading(false);
      return null;
    }
    setProfileLoading(true);
    try {
      const p = await getMyProfile();
      setProfile(p);
      if (typeof p.pro === 'boolean' && p.pro !== user?.pro) {
        updateUser({ pro: p.pro });
      }
      return p;
    } catch (err) {
      // Surface the failure once instead of each section silently guessing.
      console.error('Settings: could not load profile from user-service', err);
      setProfile(null);
      return null;
    } finally {
      setProfileLoading(false);
    }
  }, [email, user?.pro, updateUser]);

  // FIX 1: Run only once on mount to prevent infinite fetching loop
  useEffect(() => {
    refreshProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /**
   * After any successful write, replace the shared profile AND merge the
   * server-confirmed fields into the AuthContext user so the Navbar and all
   * consumers re-render instantly.
   */
  const handleProfileChange = useCallback(
    (p: UserProfile) => {
      setProfile(p);
      updateUser({
        pro: p.pro === true,
        removeWatermark: p.removeWatermark === true,
        displayName: p.displayName || '',
        phoneNumber: p.phoneNumber || '',
        jobTitle: p.jobTitle || '',
      });
    },
    [updateUser]
  );

  const sectionProps: ProfileAwareSectionProps = {
    email,
    profile,
    profileLoading,
    onProfileChange: handleProfileChange,
  };

  return (
    <div className="mx-auto max-w-5xl p-6 lg:p-8">
      {/* Header */}
      <div className="mb-8">
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="text-3xl font-bold tracking-tight text-white">Settings</h1>
          {isAdmin && (
            <span className="inline-flex items-center gap-1.5 rounded-full border border-fuchsia-400/40 bg-fuchsia-500/15 px-3 py-1 text-xs font-bold uppercase tracking-wider text-fuchsia-300">
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
              </svg>
              Super Admin
            </span>
          )}
        </div>
        <p className="mt-1 text-sm text-white/50">
          Manage your profile, security, and preferences{isAdmin ? ' — plus platform-wide controls' : ''}.
        </p>
      </div>

      <div className="space-y-6">
        <AppearanceSection />
        <ProSection {...sectionProps} />
        {/* React Key prop ensures clean initialization of form states instead of double rendering with useEffect */}
        <ProfileSection key={`profile-${profile?.id || 'loading'}`} {...sectionProps} />
        <PasswordSection />
        <NotificationsSection {...sectionProps} />
        <CardPreferencesSection {...sectionProps} />
        <DeveloperIntegrationsSection key={`dev-${profile?.id || 'loading'}`} {...sectionProps} />
        <DangerZoneSection email={email} />
        {isAdmin && <AdminCommandCenter currentUserId={user?.id} />}
      </div>
    </div>
  );
};

/* ────────────────────────── UI helpers ────────────────────────── */

function SectionCard({
  title,
  description,
  children,
  icon,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
  icon?: React.ReactNode;
}) {
  return (
    <section className="glass-panel overflow-hidden">
      <div className="border-b border-white/10 px-6 py-5">
        <div className="flex items-center gap-3">
          {icon && (
            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600/30 to-fuchsia-600/30 text-fuchsia-300">
              {icon}
            </span>
          )}
          <div>
            <h2 className="text-lg font-semibold text-white">{title}</h2>
            <p className="text-xs text-white/50">{description}</p>
          </div>
        </div>
      </div>
      <div className="px-6 py-5">{children}</div>
    </section>
  );
}

function ToggleSwitch({
  checked,
  onChange,
  disabled,
  label,
}: {
  checked: boolean;
  onChange: (next: boolean) => void;
  disabled?: boolean;
  label: string;
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className="toggle-track"
      data-on={checked}
    >
      <span className="toggle-thumb" />
    </button>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="mb-1.5 block text-sm font-medium text-white/70">{label}</label>
      {children}
    </div>
  );
}

/* ────────────────────────── Appearance ────────────────────────── */

function AppearanceSection() {
  const [theme, setTheme] = useState<ThemeMode>(() => {
    const saved = localStorage.getItem(THEME_STORAGE_KEY);
    return saved === 'light' ? 'light' : 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  return (
    <SectionCard
      title="Appearance"
      description="Dark / Light mode — saved locally on this device, no backend involved."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v2.25m6.364.386l-1.591 1.591M21 12h-2.25m-.386 6.364l-1.591-1.591M12 18.75V21m-4.773-4.227l-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z" />
        </svg>
      }
    >
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-white">UI Theme</p>
          <p className="text-xs text-white/50">Applies instantly across the whole application.</p>
        </div>
        <div className="inline-flex rounded-xl border border-white/15 bg-white/5 p-1 backdrop-blur-md">
          {(['dark', 'light'] as ThemeMode[]).map((mode) => (
            <button
              key={mode}
              onClick={() => setTheme(mode)}
              className={`rounded-lg px-4 py-1.5 text-sm font-semibold capitalize transition-all duration-200 ${
                theme === mode
                  ? 'bg-gradient-to-r from-violet-600 to-fuchsia-600 text-white shadow-lg shadow-fuchsia-900/40'
                  : 'text-white/60 hover:text-white'
              }`}
            >
              {mode === 'dark' ? '🌙 Dark' : '☀️ Light'}
            </button>
          ))}
        </div>
      </div>
    </SectionCard>
  );
}

/* ─────────────────────────── Profile ─────────────────────────── */

function ProfileSection({ email, profile, profileLoading, onProfileChange }: ProfileAwareSectionProps) {
  const [saving, setSaving] = useState(false);
  
  // Directly initializing with prop data rather than using useEffect
  const [form, setForm] = useState({
    displayName: profile?.displayName || '',
    phoneNumber: profile?.phoneNumber || '',
    jobTitle: profile?.jobTitle || '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const updated = await updateMyProfile({
        displayName: form.displayName.trim(),
        phoneNumber: form.phoneNumber.trim(),
        jobTitle: form.jobTitle.trim(),
      });
      // Distribute the authoritative response to the shared state (which also
      // syncs AuthContext), so the form, Navbar and Sidebar all re-render
      // instantly with the saved values.
      onProfileChange(updated);
      notifySuccess('Profile updated', 'Your profile details have been saved');
    } catch (err) {
      notifyError('Could not save profile', extractError(err, 'Failed to update your profile'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionCard
      title="Profile Details"
      description="How your identity appears across CardPro AI."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
        </svg>
      }
    >
      {profileLoading && !profile ? (
        <div className="space-y-3">
          <div className="skeleton h-10 w-full" />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="skeleton h-10 w-full" />
            <div className="skeleton h-10 w-full" />
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <Field label="Display Name">
            <input
              type="text"
              name="displayName"
              value={form.displayName}
              onChange={handleChange}
              maxLength={120}
              placeholder="e.g. Alex Morgan"
              className="input-field"
              autoComplete="name"
            />
          </Field>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Field label="Phone Number">
              <input
                type="tel"
                name="phoneNumber"
                value={form.phoneNumber}
                onChange={handleChange}
                maxLength={30}
                placeholder="+1 (555) 000-0000"
                className="input-field"
                autoComplete="tel"
              />
            </Field>
            <Field label="Job Title">
              <input
                type="text"
                name="jobTitle"
                value={form.jobTitle}
                onChange={handleChange}
                maxLength={120}
                placeholder="e.g. Product Designer"
                className="input-field"
                autoComplete="organization-title"
              />
            </Field>
          </div>
          <div className="flex items-center justify-end gap-3 pt-1">
            <p className="mr-auto text-xs text-white/40">Account email: {email}</p>
            <button type="submit" className="btn-primary px-5 py-2 text-xs" disabled={saving}>
              {saving ? 'Saving…' : 'Save Profile'}
            </button>
          </div>
        </form>
      )}
    </SectionCard>
  );
}

/* ─────────────────────────── Password ─────────────────────────── */

function PasswordSection() {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [saving, setSaving] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!form.currentPassword) errors.currentPassword = 'Current password is required';
    if (form.newPassword.length < 8) errors.newPassword = 'New password must be at least 8 characters';
    if (form.newPassword !== form.confirmPassword) errors.confirmPassword = 'Passwords do not match';
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSaving(true);
    try {
      await changePassword(form.currentPassword, form.newPassword);
      notifySuccess('Password changed', 'Your password has been updated successfully');
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      notifyError('Could not change password', extractError(err, 'Failed to change your password'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionCard
      title="Password"
      description="Your current password is verified before the new one is hashed and saved."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
        </svg>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <Field label="Current Password">
          <input
            type="password"
            name="currentPassword"
            value={form.currentPassword}
            onChange={handleChange}
            className={`input-field ${fieldErrors.currentPassword ? 'input-error' : ''}`}
            autoComplete="current-password"
          />
          {fieldErrors.currentPassword && (
            <p className="mt-1 text-xs text-rose-300">{fieldErrors.currentPassword}</p>
          )}
        </Field>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="New Password">
            <input
              type="password"
              name="newPassword"
              value={form.newPassword}
              onChange={handleChange}
              className={`input-field ${fieldErrors.newPassword ? 'input-error' : ''}`}
              autoComplete="new-password"
            />
            {fieldErrors.newPassword && <p className="mt-1 text-xs text-rose-300">{fieldErrors.newPassword}</p>}
          </Field>
          <Field label="Confirm New Password">
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              className={`input-field ${fieldErrors.confirmPassword ? 'input-error' : ''}`}
              autoComplete="new-password"
            />
            {fieldErrors.confirmPassword && (
              <p className="mt-1 text-xs text-rose-300">{fieldErrors.confirmPassword}</p>
            )}
          </Field>
        </div>
        <div className="flex items-center justify-end pt-1">
          <button type="submit" className="btn-primary px-5 py-2 text-xs" disabled={saving}>
            {saving ? 'Updating…' : 'Update Password'}
          </button>
        </div>
      </form>
    </SectionCard>
  );
}

/* ───────────────────────── Notifications ───────────────────────── */

function NotificationsSection({ profile, onProfileChange }: ProfileAwareSectionProps) {
  const [saving, setSaving] = useState(false);

  // Read from the shared profile (fetched once by SettingsPage). Until it
  // arrives we stay disabled and say we're loading, rather than guessing.
  const enabled = profile ? profile.emailNotificationsEnabled !== false : null;

  const toggle = async (next: boolean) => {
    if (saving) return;
    setSaving(true);
    try {
      const updated = await updateEmailNotifications(next);
      onProfileChange(updated);
      notifySuccess(
        next ? 'Notifications enabled' : 'Notifications disabled',
        next ? "You'll get an email when a new lead comes in" : 'New-lead emails are turned off'
      );
    } catch (err) {
      notifyError('Could not update preference', extractError(err, 'Failed to save notification preference'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionCard
      title="Email Notifications"
      description="Receive an email whenever someone submits your card and becomes a lead."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
        </svg>
      }
    >
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-white">New-lead email alerts</p>
          <p className="text-xs text-white/50">
            {enabled === null
              ? 'Loading your preference…'
              : enabled
              ? 'On — you will be notified about new leads'
              : 'Off — you will not receive lead emails'}
          </p>
        </div>
        <ToggleSwitch
          checked={enabled === true}
          onChange={toggle}
          disabled={enabled === null || saving}
          label="Email notifications"
        />
      </div>
    </SectionCard>
  );
}

/* ─────────────────────── Global Card Preferences ─────────────────────── */

/**
 * Pro-only card preferences that apply to every card the user publishes.
 * The {@code removeWatermark} flag is stored on the user-service profile and
 * honored by the public card render (card-service resolves it per owner);
 * non-Pro users see the toggle disabled with a "Requires Pro" badge.
 */
function CardPreferencesSection({ profile, onProfileChange }: ProfileAwareSectionProps) {
  const { user } = useAuth();
  const [saving, setSaving] = useState(false);

  // Pro gate uses the SAME source the Navbar badge reads (AuthContext user,
  // hydrated from user-service) with the shared profile as cross-check — so
  // this section can never show the toggle as locked while the Navbar shows
  // the user as PRO.
  const isPro = user?.pro === true || profile?.pro === true;
  const removeWatermark = profile?.removeWatermark === true;

  const toggleWatermark = async (next: boolean) => {
    if (saving || !isPro) return;
    setSaving(true);
    try {
      const updated = await updateMyProfile({ removeWatermark: next });
      onProfileChange(updated);
      notifySuccess(
        next ? 'Watermark removed' : 'Watermark restored',
        next
          ? 'Your public cards will no longer show the CardPro watermark'
          : 'The CardPro watermark is back on your public cards'
      );
    } catch (err) {
      notifyError('Could not update preference', extractError(err, 'Failed to save your card preference'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <SectionCard
      title="Global Card Preferences"
      description="Settings that apply across every card you publish."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456z" />
        </svg>
      }
    >
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-sm font-medium text-white">Remove "Powered by CardPro" watermark</p>
            {!isPro && (
              <span className="inline-flex items-center rounded-full border border-amber-400/40 bg-amber-500/15 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-amber-300">
                Requires Pro
              </span>
            )}
          </div>
          <p className="mt-1 max-w-md text-xs text-white/50">
            Hide the CardPro branding footer from visitors viewing your public cards.
          </p>
        </div>
        <div className="flex items-center gap-3">
          {isPro && (
            <span className={`text-xs font-medium ${removeWatermark ? 'text-emerald-300' : 'text-white/40'}`}>
              {removeWatermark ? 'Watermark hidden' : 'Watermark visible'}
            </span>
          )}
          <ToggleSwitch
            checked={removeWatermark}
            onChange={toggleWatermark}
            disabled={!isPro || !profile || saving}
            label="Remove CardPro watermark"
          />
        </div>
      </div>
    </SectionCard>
  );
}

/* ──────────────────── Developer & Integrations ──────────────────── */

/**
 * Developer integrations: the user's secret API key (UUID, regenerable) and
 * the webhook URL where future CRM lead-forwarding integrations can POST new
 * leads. Both live on the user-service profile, which SettingsPage fetches
 * once and shares here — the API key is always present because the backend
 * persists a UUID for every profile on first read.
 */
function DeveloperIntegrationsSection({ profile, onProfileChange }: ProfileAwareSectionProps) {
  // Directly initializing with prop data rather than using useEffect
  const [webhook, setWebhook] = useState(profile?.webhookUrl || '');
  const [regenerating, setRegenerating] = useState(false);
  const [savingWebhook, setSavingWebhook] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleRegenerate = async () => {
    if (regenerating) return;
    setRegenerating(true);
    try {
      const updated = await regenerateApiKey();
      onProfileChange(updated);
      notifySuccess('API key regenerated', 'Your previous key is now invalid');
    } catch (err) {
      notifyError('Could not regenerate key', extractError(err, 'Failed to regenerate your API key'));
    } finally {
      setRegenerating(false);
    }
  };

  const handleSaveWebhook = async (e: React.FormEvent) => {
    e.preventDefault();
    if (savingWebhook) return;
    setSavingWebhook(true);
    try {
      const updated = await updateWebhookUrl(webhook.trim());
      onProfileChange(updated);
      setWebhook(updated.webhookUrl || '');
      notifySuccess(
        'Webhook saved',
        updated.webhookUrl ? 'New leads will be forwarded to your webhook URL' : 'Webhook URL cleared'
      );
    } catch (err) {
      notifyError('Could not save webhook', extractError(err, 'Failed to save your webhook URL'));
    } finally {
      setSavingWebhook(false);
    }
  };

  const copyKey = async () => {
    if (!profile?.apiKey) return;
    try {
      await navigator.clipboard.writeText(profile.apiKey);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      // Clipboard unavailable (e.g. insecure context) — the key stays visible.
    }
  };

  return (
    <SectionCard
      title="Developer & Integrations"
      description="Programmatic access to your CardPro account."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M17.25 6.75L22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3l-4.5 16.5" />
        </svg>
      }
    >
      <div className="space-y-5">
        {/* API Key */}
        <div>
          <div className="mb-1.5 flex items-center justify-between gap-3">
            <label className="text-sm font-medium text-white/70">API Key</label>
            <button
              type="button"
              onClick={handleRegenerate}
              disabled={regenerating}
              className="rounded-lg border border-white/15 bg-white/5 px-3 py-1.5 text-xs font-semibold text-white/70 transition-all hover:bg-white/15 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
            >
              {regenerating ? 'Regenerating…' : 'Regenerate'}
            </button>
          </div>
          <div className="flex items-center gap-2">
            <input
              type="text"
              readOnly
              value={profile?.apiKey || ''}
              onFocus={(e) => e.currentTarget.select()}
              placeholder="Your API key will appear here"
              className="input-field font-mono text-xs"
            />
            <button
              type="button"
              onClick={copyKey}
              disabled={!profile?.apiKey}
              title="Copy API key"
              className="shrink-0 rounded-xl border border-white/15 bg-white/5 px-3 py-2.5 text-xs font-semibold text-white/70 transition-all hover:bg-white/15 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
            >
              {copied ? '✓' : 'Copy'}
            </button>
          </div>
          <p className="mt-1.5 text-xs text-white/40">
            Authenticate external integrations with this secret key. Regenerating invalidates the previous key
            immediately.
          </p>
        </div>

        {/* Webhook URL */}
        <form onSubmit={handleSaveWebhook} className="border-t border-white/10 pt-5">
          <label htmlFor="webhook-url" className="mb-1.5 block text-sm font-medium text-white/70">
            Webhook URL
          </label>
          <div className="flex items-center gap-2">
            <input
              id="webhook-url"
              type="url"
              value={webhook}
              onChange={(e) => setWebhook(e.target.value)}
              placeholder="https://your-crm.example.com/hooks/cardpro"
              className="input-field"
              autoComplete="off"
            />
            <button
              type="submit"
              disabled={savingWebhook}
              className="btn-primary shrink-0 px-4 py-2.5 text-xs"
            >
              {savingWebhook ? 'Saving…' : 'Save'}
            </button>
          </div>
          <p className="mt-1.5 text-xs text-white/40">
            Reserve the URL where future CRM integrations will receive new leads. Leave empty to clear.
          </p>
        </form>
      </div>
    </SectionCard>
  );
}

/* ─────────────────────────── Danger Zone ─────────────────────────── */

/**
 * Account deletion with a strict confirmation gate: the user must type their
 * own email address into the modal before the red button becomes active, and
 * only then is DELETE /users/me called. On success the local session is
 * cleared and the app redirects to /login.
 */
function DangerZoneSection({ email }: { email?: string }) {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [confirmText, setConfirmText] = useState('');
  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    if (deleting || !email || confirmText.trim() !== email) return;
    setDeleting(true);
    try {
      await deleteMyAccount();
      // Clear the local session (token + cached user) and leave the app.
      await logout();
      navigate('/login', { replace: true });
    } catch (err) {
      notifyError('Could not delete account', extractError(err, 'Failed to delete your account'));
      setDeleting(false);
    }
  };

  const closeModal = () => {
    if (deleting) return;
    setConfirmOpen(false);
    setConfirmText('');
  };

  const matches = !!email && confirmText.trim() === email;

  return (
    <SectionCard
      title="Danger Zone"
      description="Irreversible actions on your account."
      icon={
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
        </svg>
      }
    >
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-white">Delete Account</p>
          <p className="mt-1 max-w-md text-xs text-white/50">
            Permanently disables your account, your cards stop working, and you can no longer sign in. This cannot be
            undone.
          </p>
        </div>
        <button
          onClick={() => setConfirmOpen(true)}
          className="rounded-xl border border-rose-400/40 bg-rose-500/10 px-5 py-2.5 text-xs font-bold text-rose-300 transition-all hover:bg-rose-500/20 hover:text-rose-200 active:scale-[0.97]"
        >
          Delete Account
        </button>
      </div>

      {confirmOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">
          <div className="w-full max-w-md overflow-hidden rounded-2xl border border-rose-400/30 bg-slate-900 shadow-2xl shadow-rose-950/40">
            <div className="border-b border-rose-400/20 bg-rose-500/10 px-6 py-4">
              <h3 className="flex items-center gap-2 text-base font-bold text-rose-200">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
                </svg>
                Are you absolutely sure?
              </h3>
            </div>
            <div className="space-y-4 px-6 py-5">
              <p className="text-sm leading-relaxed text-white/70">
                This will permanently disable your account. Your digital cards will stop resolving and you will lose
                access to your leads and analytics. This action <span className="font-semibold text-rose-300">cannot be
                undone</span>.
              </p>
              <div>
                <label htmlFor="delete-confirm" className="mb-1.5 block text-sm font-medium text-white/70">
                  Type <span className="font-mono font-semibold text-rose-300">{email}</span> to confirm
                </label>
                <input
                  id="delete-confirm"
                  type="text"
                  value={confirmText}
                  onChange={(e) => setConfirmText(e.target.value)}
                  placeholder={email}
                  autoFocus
                  className={`input-field font-mono text-sm ${matches ? 'border-emerald-400/50' : ''}`}
                />
              </div>
              <div className="flex items-center justify-end gap-3 pt-1">
                <button
                  onClick={closeModal}
                  disabled={deleting}
                  className="rounded-xl border border-white/15 bg-white/5 px-4 py-2.5 text-xs font-semibold text-white/70 transition-all hover:bg-white/15 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  onClick={handleDelete}
                  disabled={!matches || deleting}
                  className="rounded-xl bg-gradient-to-r from-rose-600 to-red-600 px-5 py-2.5 text-xs font-bold text-white shadow-lg shadow-rose-950/50 transition-all hover:brightness-110 active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-40"
                >
                  {deleting ? 'Deleting…' : 'Delete my account'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </SectionCard>
  );
}

/* ─────────────────────────── CardPro Pro ─────────────────────────── */

const PRO_FEATURES = [
  'Unlimited digital cards',
  'AI-powered bios & photo upscaling',
  'Lead capture with analytics',
  'Priority email support',
  'Exclusive PRO badge',
];

/**
 * Razorpay-backed CardPro Pro upgrade card.
 *
 * Loads the real Pro status from user-service ({@code /users/me → pro}); a
 * non-Pro user gets the ₹999 upgrade CTA, which creates a Razorpay order,
 * opens the Checkout modal, and only flips the UI to PRO after the server has
 * cryptographically verified the payment signature.
 */
function ProSection({ email, profile, onProfileChange }: ProfileAwareSectionProps) {
  const { user, updateUser } = useAuth();
  const [checkingOut, setCheckingOut] = useState(false);

  // The Navbar PRO badge reads user?.pro (hydrated from user-service); the
  // shared profile is the server cross-check. EITHER saying PRO means PRO —
  // an active member must never see the ₹999 upgrade CTA, no matter which
  // fetch answered first (or whether one failed).
  const isPro = user?.pro === true || profile?.pro === true;

  // FIX 2: Removed updateUser from the dependency array to prevent looping
  useEffect(() => {
    if (isPro && user?.pro !== true) {
      updateUser({ pro: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPro, user?.pro]);

  const handleUpgrade = async () => {
    if (checkingOut) return;
    setCheckingOut(true);
    try {
      const order = await createProOrder();
      await openRazorpayCheckout({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        orderId: order.orderId,
        name: 'CardPro AI',
        description: 'CardPro Pro — one-time ₹999',
        email,
        themeColor: '#7c3aed',
        onSuccess: async (payment) => {
          try {
            const result = await verifyProPayment({
              razorpayOrderId: payment.razorpayOrderId,
              razorpayPaymentId: payment.razorpayPaymentId,
              signature: payment.razorpaySignature,
            });
            if (result.success) {
              // Re-read the authoritative profile so the shared section state
              // AND the auth context (navbar PRO badge) reflect exactly what
              // the database holds — not just the verify reply.
              const updated = await getMyProfile();
              onProfileChange(updated);
              notifySuccess('Welcome to Pro!', 'Your account is now on CardPro Pro');
            } else {
              notifyError('Payment not verified', result.message || 'Could not verify your payment');
            }
          } catch (err) {
            notifyError(
              'Payment not verified',
              extractError(err, 'Something went wrong while verifying your payment. Contact support with your payment ID.')
            );
          }
        },
        onDismiss: () => {
          // Modal closed without paying — nothing to update.
        },
      });
    } catch (err) {
      notifyError('Could not start checkout', extractError(err, 'Failed to create the payment order'));
    } finally {
      setCheckingOut(false);
    }
  };

  return (
    <section className="overflow-hidden rounded-2xl border border-amber-400/30 bg-gradient-to-br from-amber-500/[0.08] via-transparent to-fuchsia-500/[0.06] shadow-xl shadow-amber-950/20 backdrop-blur-xl">
      {/* Header */}
      <div className="border-b border-amber-400/20 px-6 py-5">
        <div className="flex flex-wrap items-center gap-3">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-amber-400 to-yellow-500 text-slate-900 shadow-lg shadow-amber-900/40">
            <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
            </svg>
          </span>
          <div>
            <h2 className="flex items-center gap-2 text-lg font-semibold text-white">
              CardPro Pro
              {isPro && (
                <span className="inline-flex items-center gap-1 rounded-full bg-gradient-to-r from-amber-400 to-yellow-500 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider text-slate-900">
                  PRO
                </span>
              )}
            </h2>
            <p className="text-xs text-white/50">
              {isPro ? 'Your Pro membership is active' : 'One-time payment, lifetime access'}
            </p>
          </div>
        </div>
      </div>

      <div className="px-6 py-5">
        {isPro ? (
          <div className="flex items-start gap-3">
            <span className="mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-emerald-500/20 text-emerald-300">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
              </svg>
            </span>
            <div>
              <p className="text-sm font-semibold text-white">You're on CardPro Pro</p>
              <p className="mt-0.5 max-w-md text-xs text-white/50">
                All Pro features are unlocked on your account. Thanks for supporting CardPro AI!
              </p>
            </div>
          </div>
        ) : (
          <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <div className="flex items-baseline gap-2">
                <span className="text-4xl font-bold text-white">₹999</span>
                <span className="text-sm text-white/50">one-time</span>
              </div>
              <ul className="mt-4 space-y-2">
                {PRO_FEATURES.map((feature) => (
                  <li key={feature} className="flex items-center gap-2 text-sm text-white/70">
                    <svg className="h-4 w-4 shrink-0 text-amber-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                    </svg>
                    {feature}
                  </li>
                ))}
              </ul>
            </div>
            <div className="flex flex-col items-start gap-3 lg:items-end">
              <button
                onClick={handleUpgrade}
                disabled={checkingOut}
                className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-amber-400 to-yellow-500 px-6 py-3 text-sm font-bold text-slate-900 shadow-lg shadow-amber-900/40 transition-all duration-200 hover:brightness-110 active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-60"
              >
                {checkingOut ? (
                  <>
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-900/30 border-t-slate-900" />
                    Starting checkout…
                  </>
                ) : (
                  <>
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                    </svg>
                    Upgrade to Pro
                  </>
                )}
              </button>
              <p className="text-xs text-white/40">
                Secure payments powered by <span className="font-semibold text-white/60">Razorpay</span> · UPI, cards
                & netbanking
              </p>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

/* ────────────────────── Admin Command Center ───────────────────── */

function AdminCommandCenter({ currentUserId }: { currentUserId?: string }) {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [totalCards, setTotalCards] = useState<number | null>(null);
  const [registrationEnabled, setRegistrationEnabled] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);
  const [busyUserId, setBusyUserId] = useState<string | null>(null);
  const [regSaving, setRegSaving] = useState(false);

  const refreshUsers = useCallback(async () => {
    const list = await getAllUsers();
    setUsers(list);
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const [list, cards, reg] = await Promise.all([
          getAllUsers(),
          getTotalCardCount(),
          getRegistrationConfig(),
        ]);
        setUsers(list);
        setTotalCards(cards.totalCards);
        setRegistrationEnabled(reg.enabled);
      } catch (err) {
        notifyError('Admin data unavailable', extractError(err, 'Could not load admin metrics'));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const runAction = async (action: () => Promise<unknown>, successTitle: string, successMsg: string, userId: string) => {
    if (busyUserId) return;
    setBusyUserId(userId);
    try {
      await action();
      await refreshUsers();
      notifySuccess(successTitle, successMsg);
    } catch (err) {
      notifyError(successTitle, extractError(err, 'Action failed'));
    } finally {
      setBusyUserId(null);
    }
  };

  const onPromote = (u: AdminUser) =>
    runAction(() => updateUserRole(u.id, 'ADMIN'), 'User promoted', `${u.email} is now an admin`, u.id);

  const onDemote = (u: AdminUser) =>
    runAction(() => updateUserRole(u.id, 'USER'), 'User demoted', `${u.email} is now a standard user`, u.id);

  const onDisable = (u: AdminUser) =>
    runAction(() => softDeleteUser(u.id), 'Account disabled', `${u.email} can no longer sign in`, u.id);

  const onRestore = (u: AdminUser) =>
    runAction(() => updateUserStatus(u.id, true), 'Account restored', `${u.email} can sign in again`, u.id);

  const toggleRegistration = async (next: boolean) => {
    if (regSaving) return;
    setRegSaving(true);
    try {
      const res = await setRegistrationConfig(next);
      setRegistrationEnabled(res.enabled);
      notifySuccess(
        next ? 'Registration enabled' : 'Registration disabled',
        next ? 'New users can sign up again' : 'New self-registration is now blocked'
      );
    } catch (err) {
      notifyError('Could not update registration', extractError(err, 'Failed to update registration flag'));
    } finally {
      setRegSaving(false);
    }
  };

  const self = (u: AdminUser) => !!currentUserId && u.id === currentUserId;

  return (
    <section className="overflow-hidden rounded-2xl border border-fuchsia-400/30 bg-fuchsia-500/[0.06] shadow-xl shadow-fuchsia-950/30 backdrop-blur-xl">
      <div className="border-b border-fuchsia-400/20 px-6 py-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-white shadow-lg shadow-fuchsia-900/40">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 6h9.75M10.5 6a1.5 1.5 0 11-3 0m3 0a1.5 1.5 0 10-3 0M3.75 6H7.5m3 12h9.75m-9.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-3.75 0H7.5m9-6h3.75m-3.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-9.75 0h9.75" />
              </svg>
            </span>
            <div>
              <h2 className="text-lg font-semibold text-white">Super Admin Command Center</h2>
              <p className="text-xs text-white/50">Platform-wide users, metrics, and access controls.</p>
            </div>
          </div>
        </div>
      </div>

      <div className="space-y-6 px-6 py-5">
        {loading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="skeleton h-24 w-full" />
            <div className="skeleton h-24 w-full" />
            <div className="skeleton h-24 w-full" />
          </div>
        ) : (
          <>
            {/* Metric cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div className="glass-card p-5">
                <p className="text-xs font-semibold uppercase tracking-wider text-white/50">Total Digital Cards</p>
                <p className="mt-2 text-4xl font-bold text-white">{totalCards ?? '—'}</p>
                <p className="mt-1 text-xs text-white/40">All cards across every user</p>
              </div>

              <div className="glass-card p-5">
                <p className="text-xs font-semibold uppercase tracking-wider text-white/50">Registered Users</p>
                <p className="mt-2 text-4xl font-bold text-white">{users.length}</p>
                <p className="mt-1 text-xs text-white/40">Including disabled accounts</p>
              </div>

              <div className="glass-card flex flex-col justify-between p-5">
                <p className="text-xs font-semibold uppercase tracking-wider text-white/50">Public Registration</p>
                <div className="mt-2 flex items-center justify-between">
                  <p className="text-sm font-medium text-white">
                    {registrationEnabled === null ? '…' : registrationEnabled ? 'Open' : 'Closed'}
                  </p>
                  <ToggleSwitch
                    checked={registrationEnabled === true}
                    onChange={toggleRegistration}
                    disabled={regSaving || registrationEnabled === null}
                    label="Public registration"
                  />
                </div>
                <p className="mt-1 text-xs text-white/40">
                  When closed, POST /register rejects new sign-ups
                </p>
              </div>
            </div>

            {/* Users table */}
            <div className="glass-panel overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead>
                    <tr className="border-b border-white/10 text-xs uppercase tracking-wider text-white/50">
                      <th className="px-4 py-3 font-semibold">Email</th>
                      <th className="px-4 py-3 font-semibold">Role</th>
                      <th className="px-4 py-3 font-semibold">Status</th>
                      <th className="px-4 py-3 font-semibold">Joined</th>
                      <th className="px-4 py-3 text-right font-semibold">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => {
                      const isSelf = self(u);
                      const disabled = !!busyUserId || isSelf;
                      return (
                        <tr key={u.id} className="border-b border-white/5 last:border-0 hover:bg-white/5">
                          <td className="px-4 py-3">
                            <div className="flex items-center gap-3">
                              <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500/50 to-fuchsia-500/50 text-xs font-bold text-white">
                                {u.email.charAt(0).toUpperCase()}
                              </span>
                              <div className="min-w-0">
                                <p className="truncate font-medium text-white">{u.email}</p>
                                {isSelf && <p className="text-[10px] font-semibold uppercase tracking-wide text-fuchsia-300">You</p>}
                              </div>
                            </div>
                          </td>
                          <td className="px-4 py-3">
                            <span
                              className={`inline-flex rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider ${
                                u.role === 'ADMIN'
                                  ? 'bg-fuchsia-500/20 text-fuchsia-300'
                                  : 'bg-white/10 text-white/60'
                              }`}
                            >
                              {u.role}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <span
                              className={`inline-flex items-center gap-1.5 text-xs font-medium ${
                                u.enabled === false ? 'text-rose-300' : 'text-emerald-300'
                              }`}
                            >
                              <span
                                className={`h-1.5 w-1.5 rounded-full ${
                                  u.enabled === false ? 'bg-rose-400' : 'bg-emerald-400'
                                }`}
                              />
                              {u.enabled === false ? 'Disabled' : 'Active'}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-xs text-white/50">
                            {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex items-center justify-end gap-2">
                              {u.enabled === false ? (
                                <button
                                  onClick={() => onRestore(u)}
                                  disabled={disabled}
                                  className="rounded-lg border border-emerald-400/30 bg-emerald-500/10 px-3 py-1.5 text-xs font-semibold text-emerald-300 transition-all hover:bg-emerald-500/20 disabled:cursor-not-allowed disabled:opacity-50"
                                >
                                  Restore
                                </button>
                              ) : (
                                <>
                                  {u.role === 'ADMIN' ? (
                                    <button
                                      onClick={() => onDemote(u)}
                                      disabled={disabled}
                                      className="rounded-lg border border-white/15 bg-white/5 px-3 py-1.5 text-xs font-semibold text-white/70 transition-all hover:bg-white/15 hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                                    >
                                      Demote
                                    </button>
                                  ) : (
                                    <button
                                      onClick={() => onPromote(u)}
                                      disabled={disabled}
                                      className="rounded-lg border border-fuchsia-400/30 bg-fuchsia-500/10 px-3 py-1.5 text-xs font-semibold text-fuchsia-300 transition-all hover:bg-fuchsia-500/20 disabled:cursor-not-allowed disabled:opacity-50"
                                    >
                                      Promote
                                    </button>
                                  )}
                                  <button
                                    onClick={() => onDisable(u)}
                                    disabled={disabled}
                                    className="rounded-lg border border-rose-400/30 bg-rose-500/10 px-3 py-1.5 text-xs font-semibold text-rose-300 transition-all hover:bg-rose-500/20 disabled:cursor-not-allowed disabled:opacity-50"
                                  >
                                    {busyUserId === u.id ? '…' : 'Disable'}
                                  </button>
                                </>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
              {users.length === 0 && (
                <p className="px-4 py-8 text-center text-sm text-white/40">No users found.</p>
              )}
            </div>
          </>
        )}
      </div>
    </section>
  );
}

export default SettingsPage;