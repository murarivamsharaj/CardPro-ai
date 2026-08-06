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
    read: false 
  },
  { 
    id: '2', 
    title: 'Payment Successful', 
    message: 'Microtransaction processed successfully.', 
    timestamp: '2h ago', 
    read: false 
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
        className="relative p-2 text-gray-600 hover:text-gray-900 focus:outline-none rounded-full hover:bg-gray-100 transition"
        aria-label="Notifications"
      >
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white shadow-sm">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 rounded-2xl bg-white shadow-xl border border-gray-100 z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 bg-gray-50">
            <span className="font-semibold text-sm text-gray-900">Notifications</span>
            {unreadCount > 0 && (
              <button 
                onClick={markAllAsRead} 
                className="text-xs text-blue-600 hover:text-blue-700 font-medium"
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-gray-100">
            {notifications.length === 0 ? (
              <p className="p-6 text-center text-xs text-gray-500">No notifications yet</p>
            ) : (
              notifications.map((n) => (
                <div 
                  key={n.id} 
                  className={`p-4 text-sm transition hover:bg-gray-50 ${!n.read ? 'bg-blue-50/40' : ''}`}
                >
                  <div className="flex justify-between items-start">
                    <p className="font-semibold text-gray-900 text-xs">{n.title}</p>
                    <span className="text-[10px] text-gray-400">{n.timestamp}</span>
                  </div>
                  <p className="text-gray-600 text-xs mt-1">{n.message}</p>
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