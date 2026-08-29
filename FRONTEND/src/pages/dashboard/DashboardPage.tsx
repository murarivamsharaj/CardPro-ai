import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchUserCards } from '../../store/slices/cardSlice';
import { CreditCard, Users, BarChart3, Plus, Sparkles } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch<any>();
  const navigate = useNavigate();
  const { cards, loading } = useSelector((state: any) => state.card);
  const { user } = useSelector((state: any) => state.auth || {});

  useEffect(() => {
    // Fetch cards to get the real count for the dashboard summary
    dispatch(fetchUserCards({}));
  }, [dispatch]);

  const activeCards = cards?.length || 0;

  const QUICK_ACTIONS = [
    {
      title: 'Create New Card',
      description: 'Design a new interactive digital business card.',
      icon: Plus,
      color: 'text-fuchsia-400',
      bg: 'bg-fuchsia-500/10',
      onClick: () => navigate('/dashboard/cards/create'),
    },
    {
      title: 'Manage Cards',
      description: `You have ${loading ? '...' : activeCards} active card${activeCards === 1 ? '' : 's'}.`,
      icon: CreditCard,
      color: 'text-blue-400',
      bg: 'bg-blue-500/10',
      onClick: () => navigate('/dashboard/cards'),
    },
    {
      title: 'View Leads',
      description: 'Check your latest contact requests and messages.',
      icon: Users,
      color: 'text-emerald-400',
      bg: 'bg-emerald-500/10',
      onClick: () => navigate('/dashboard/leads'),
    },
    {
      title: 'Analytics',
      description: 'Track your page views and link clicks.',
      icon: BarChart3,
      color: 'text-amber-400',
      bg: 'bg-amber-500/10',
      onClick: () => navigate('/dashboard/analytics'),
    },
  ];

  return (
    <div className="mx-auto max-w-7xl p-6 lg:p-8">
      {/* Welcome Hero */}
      <div className="mb-10 rounded-3xl bg-gradient-to-br from-violet-600/20 to-fuchsia-600/20 p-8 border border-white/10 relative overflow-hidden">
        <div className="pointer-events-none absolute -right-10 -top-10 opacity-30 mix-blend-screen">
          <Sparkles className="w-64 h-64 text-fuchsia-500" />
        </div>
        <div className="relative z-10">
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Welcome back{user?.firstName ? `, ${user.firstName}` : ''}!
          </h1>
          <p className="mt-2 max-w-xl text-sm text-white/70 leading-relaxed">
            Your CardPro AI ecosystem is ready. Manage your digital identities, connect with new leads, and track your networking performance all in one place.
          </p>
        </div>
      </div>

      <h2 className="text-xl font-bold tracking-tight text-white mb-4">Quick Actions</h2>
      
      {/* Quick Action Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {QUICK_ACTIONS.map((action, idx) => (
          <button
            key={idx}
            onClick={action.onClick}
            className="group glass-panel p-6 rounded-2xl border border-white/10 flex items-start gap-4 text-left hover:bg-white/[0.03] transition-all active:scale-[0.98]"
          >
            <div className={`p-3 rounded-xl ${action.bg} ${action.color} shrink-0 group-hover:scale-110 transition-transform`}>
              <action.icon className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white group-hover:text-fuchsia-300 transition-colors">
                {action.title}
              </h3>
              <p className="mt-1 text-sm text-white/50">
                {action.description}
              </p>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};

export default DashboardPage;