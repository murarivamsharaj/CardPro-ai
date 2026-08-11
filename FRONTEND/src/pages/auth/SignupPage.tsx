import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { BackgroundFX } from '../../components/common/BackgroundFX';

export default function SignupPage() {
  const navigate = useNavigate();
  const { signup, isLoading, error, clearError } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  function validate(): boolean {
    const errors: Record<string, string> = {};
    if (!email.trim()) errors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Invalid email format';
    if (!password) errors.password = 'Password is required';
    else if (password.length < 8) errors.password = 'Password must be at least 8 characters';
    if (password !== confirmPassword) errors.confirmPassword = 'Passwords do not match';
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    clearError();
    if (!validate()) return;

    try {
      await signup({ email: email.trim(), password });
      navigate('/dashboard', { replace: true });
    } catch {
      // Error handled by AuthContext
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center p-4">
      <BackgroundFX />
      <div className="auth-card">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-violet-600 to-fuchsia-600 text-2xl font-bold text-white shadow-xl shadow-fuchsia-900/50">
            C
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Create your account</h1>
          <p className="mt-1 text-sm text-white/50">Get started with CardPro AI for free</p>
        </div>

        {error && (
          <div className="mb-4 rounded-xl border border-rose-400/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200 backdrop-blur-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label htmlFor="email" className="mb-1.5 block text-sm font-medium text-white/70">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              className={`input-field ${validationErrors.email ? 'input-error' : ''}`}
              autoComplete="email"
              disabled={isLoading}
            />
            {validationErrors.email && <p className="mt-1 text-xs text-rose-300">{validationErrors.email}</p>}
          </div>

          <div>
            <label htmlFor="password" className="mb-1.5 block text-sm font-medium text-white/70">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="At least 8 characters"
              className={`input-field ${validationErrors.password ? 'input-error' : ''}`}
              autoComplete="new-password"
              disabled={isLoading}
            />
            {validationErrors.password && <p className="mt-1 text-xs text-rose-300">{validationErrors.password}</p>}
          </div>

          <div>
            <label htmlFor="confirmPassword" className="mb-1.5 block text-sm font-medium text-white/70">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="••••••••"
              className={`input-field ${validationErrors.confirmPassword ? 'input-error' : ''}`}
              autoComplete="new-password"
              disabled={isLoading}
            />
            {validationErrors.confirmPassword && <p className="mt-1 text-xs text-rose-300">{validationErrors.confirmPassword}</p>}
          </div>

          <button type="submit" className="btn-primary w-full" disabled={isLoading}>
            {isLoading ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-white/50">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-fuchsia-300 transition-colors hover:text-fuchsia-200">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
