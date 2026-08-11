import React, { useEffect, useState } from 'react';
import { useNotificationStore, IslandType } from '../../store/useNotificationStore';

const ICONS: Record<IslandType, React.ReactNode> = {
  success: (
    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 shadow-lg shadow-emerald-900/50">
      <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
      </svg>
    </span>
  ),
  error: (
    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-rose-500 to-red-600 shadow-lg shadow-rose-900/50">
      <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </span>
  ),
  info: (
    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 shadow-lg shadow-fuchsia-900/50">
      <svg className="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z" />
      </svg>
    </span>
  ),
};

/**
 * Top-center "Dynamic Island" pill notification.
 * Mount once in the app root; drive it via useNotificationStore.
 */
export const DynamicIsland: React.FC = () => {
  const { notification, visible, hide } = useNotificationStore();
  const [leaving, setLeaving] = useState(false);

  useEffect(() => {
    if (!visible) {
      // Play the shrink-away animation before unmounting.
      setLeaving(true);
      const t = setTimeout(() => setLeaving(false), 300);
      return () => clearTimeout(t);
    }
    setLeaving(false);
  }, [visible]);

  if (!notification) return null;

  return (
    <div
      className="pointer-events-none fixed left-1/2 top-4 z-[100] w-full max-w-sm -translate-x-1/2 px-4 sm:top-6"
      aria-live="polite"
    >
      <div
        className={`island-pill ${leaving ? 'opacity-0' : 'opacity-100'}`}
        style={{
          animation: leaving ? 'island-out 0.3s ease-in both' : undefined,
          cursor: 'default',
        }}
        onClick={() => hide()}
      >
        {ICONS[notification.type]}
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold text-white">{notification.title}</p>
          {notification.message && (
            <p className="mt-0.5 truncate text-xs text-white/60">{notification.message}</p>
          )}
        </div>
        <button
          onClick={(e) => {
            e.stopPropagation();
            hide();
          }}
          className="shrink-0 rounded-full p-1 text-white/40 transition-colors hover:bg-white/10 hover:text-white"
          aria-label="Dismiss notification"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  );
};

export default DynamicIsland;
