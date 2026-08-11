import { create } from 'zustand';

export type IslandType = 'success' | 'error' | 'info';

export interface IslandNotification {
  id: number;
  type: IslandType;
  title: string;
  message?: string;
}

interface NotificationState {
  notification: IslandNotification | null;
  visible: boolean;
  notify: (type: IslandType, title: string, message?: string) => void;
  hide: () => void;
}

let hideTimer: ReturnType<typeof setTimeout> | null = null;
let idCounter = 0;

const DEFAULT_DURATION = 3200;

/**
 * Global Dynamic-Island style notification store.
 * Replaces toast alerts for copy-link / saved actions with a
 * top-center pill that drops down, expands, and shrinks away.
 */
export const useNotificationStore = create<NotificationState>((set, get) => ({
  notification: null,
  visible: false,

  notify: (type, title, message) => {
    if (hideTimer) {
      clearTimeout(hideTimer);
      hideTimer = null;
    }

    // If a notification is showing, replace it instantly so rapid
    // actions (e.g. repeated copy-link clicks) feel responsive.
    set({ notification: { id: ++idCounter, type, title, message }, visible: true });

    hideTimer = setTimeout(() => {
      get().hide();
    }, DEFAULT_DURATION);
  },

  hide: () => {
    set({ visible: false });
    // Clear the message shortly after the exit animation completes.
    setTimeout(() => {
      if (!get().visible) {
        set({ notification: null });
      }
    }, 350);
  },
}));

/** Convenience helpers */
export const notifySuccess = (title: string, message?: string) =>
  useNotificationStore.getState().notify('success', title, message);
export const notifyError = (title: string, message?: string) =>
  useNotificationStore.getState().notify('error', title, message);
export const notifyInfo = (title: string, message?: string) =>
  useNotificationStore.getState().notify('info', title, message);
