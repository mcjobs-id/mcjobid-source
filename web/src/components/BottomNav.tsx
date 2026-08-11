import React from 'react';
import { Home, Calendar, Zap, Wallet, User } from 'lucide-react';

export type TabType = 'home' | 'bookings' | 'daymode' | 'finance' | 'profile';

interface BottomNavProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ activeTab, onChangeTab }) => {
  const tabs = [
    { id: 'home' as TabType, label: 'Beranda', icon: Home },
    { id: 'bookings' as TabType, label: 'Acara', icon: Calendar },
    { id: 'daymode' as TabType, label: 'Hari H', icon: Zap, highlight: true },
    { id: 'finance' as TabType, label: 'Keuangan', icon: Wallet },
    { id: 'profile' as TabType, label: 'Profil', icon: User }
  ];

  return (
    <nav className="glass-bottom fixed bottom-0 left-0 right-0 z-40 max-w-[600px] mx-auto flex items-center justify-around px-2 py-2">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;

        if (tab.highlight) {
          return (
            <button
              key={tab.id}
              onClick={() => onChangeTab(tab.id)}
              className={`flex flex-col items-center justify-center -mt-5 transition-transform active:scale-95`}
            >
              <div
                className={`w-12 h-12 rounded-full flex items-center justify-center shadow-lg transition-all ${
                  isActive
                    ? 'bg-gradient-to-tr from-indigo-600 to-indigo-500 text-white ring-4 ring-indigo-100 dark:ring-indigo-900/40 scale-105'
                    : 'bg-indigo-600 text-white shadow-indigo-500/30'
                }`}
              >
                <Icon className="w-6 h-6" />
              </div>
              <span className="text-[11px] font-bold text-indigo-600 dark:text-indigo-400 mt-1">
                {tab.label}
              </span>
            </button>
          );
        }

        return (
          <button
            key={tab.id}
            onClick={() => onChangeTab(tab.id)}
            className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition-all ${
              isActive
                ? 'text-indigo-600 dark:text-indigo-400 font-bold scale-105'
                : 'text-slate-500 dark:text-slate-400 font-medium hover:text-slate-700 dark:hover:text-slate-200'
            }`}
          >
            <Icon className={`w-5 h-5 mb-0.5 ${isActive ? 'stroke-[2.5px]' : 'stroke-2'}`} />
            <span className="text-[10px] leading-tight">{tab.label}</span>
          </button>
        );
      })}
    </nav>
  );
};
