import React, { useCallback, useEffect, useState } from 'react';
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
  type UserProfile,
  type AdminUser,
} from '../../services/settingsService';

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
 * Settings — profile, security, appearance, notifications, and (for admins)
 * the Super Admin Command Center. Theme preference is purely client-side
 * (localStorage + data-theme on <html>); everything else hits the services
 * through the gateway or user-service directly.
 */
export const SettingsPage: React.FC = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

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
        <ProfileSection email={user?.email} />
        <PasswordSection />
        <NotificationsSection email={user?.email} />
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

function ProfileSection({ email }: { email?: string }) {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    displayName: '',
    phoneNumber: '',
    jobTitle: '',
  });

  const load = useCallback(async () => {
    if (!email) return;
    setLoading(true);
    try {
      const profile = await getMyProfile();
      setForm({
        displayName: profile.displayName || '',
        phoneNumber: profile.phoneNumber || '',
        jobTitle: profile.jobTitle || '',
      });
    } catch (err: any) {
      // 404 (USER_PROFILE_NOT_FOUND) simply means no profile yet — show empty defaults.
      if (err?.response?.status !== 404) {
        notifyError('Could not load profile', extractError(err, 'Failed to load your profile'));
      }
    } finally {
      setLoading(false);
    }
  }, [email]);

  useEffect(() => {
    load();
  }, [load]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateMyProfile({
        displayName: form.displayName.trim(),
        phoneNumber: form.phoneNumber.trim(),
        jobTitle: form.jobTitle.trim(),
      });
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
      {loading ? (
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

function NotificationsSection({ email }: { email?: string }) {
  const [enabled, setEnabled] = useState<boolean | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!email) return;
    getMyProfile()
      .then((profile) => setEnabled(profile.emailNotificationsEnabled !== false))
      .catch(() => setEnabled(true));
  }, [email]);

  const toggle = async (next: boolean) => {
    if (saving) return;
    setSaving(true);
    try {
      const profile = await updateEmailNotifications(next);
      setEnabled(profile.emailNotificationsEnabled !== false);
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
