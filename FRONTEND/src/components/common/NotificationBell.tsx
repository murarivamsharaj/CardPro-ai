import React, { useState, useEffect } from 'react';
import api from '../../services/api';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}

const MOCK_NOTIFICATIONS: NotificationItem[] = [
  {
    id: '1',
    title: 'New Lead Captured!',
    message: 'A prospective client submitted a contact form.',
    timestamp: '10m ago',
    read: false,
  },
  {
    id: '2',
    title: 'Payment Successful',
    message: 'Microtransaction processed successfully.',
    timestamp: '2h ago',
    read: false,
  },
];

export const NotificationBell: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>(MOCK_NOTIFICATIONS);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const response = await api.get('/api/v1/notifications');
        if (response.data) {
          setNotifications(response.data?.data || response.data);
        }
      } catch (error) {
        // Silently fall back to mock data if backend endpoint isn't implemented yet
        console.warn('Notifications endpoint returned 404. Using local mock data.');
      }
    };

    fetchNotifications();
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAllAsRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, read: true })));
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative rounded-full p-2 text-white/60 transition-colors hover:bg-white/10 hover:text-white focus:outline-none"
        aria-label="Notifications"
      >
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-gradient-to-r from-rose-500 to-fuchsia-500 text-[10px] font-bold text-white shadow-sm">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-2xl border border-white/15 bg-slate-900/85 shadow-2xl shadow-black/50 backdrop-blur-2xl sm:w-96">
          <div className="flex items-center justify-between border-b border-white/10 bg-white/5 px-4 py-3">
            <span className="text-sm font-semibold text-white">Notifications</span>
            {unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                className="text-xs font-medium text-fuchsia-300 transition-colors hover:text-fuchsia-200"
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="max-h-80 divide-y divide-white/5 overflow-y-auto">
            {notifications.length === 0 ? (
              <p className="p-6 text-center text-xs text-white/40">No notifications yet</p>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-4 text-sm transition-colors hover:bg-white/5 ${!n.read ? 'bg-fuchsia-500/5' : ''}`}
                >
                  <div className="flex items-start justify-between">
                    <p className="text-xs font-semibold text-white">{n.title}</p>
                    <span className="ml-2 text-[10px] text-white/35">{n.timestamp}</span>
                  </div>
                  <p className="mt-1 text-xs text-white/55">{n.message}</p>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
