import React from 'react';
import { 
  Home, 
  Calendar, 
  Users, 
  DollarSign, 
  Grid, 
  Plus, 
  Sparkles,
  LogOut,
  Moon,
  Sun,
  Tag,
  FileText,
  Bell,
  TrendingUp
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export type TabType = 
  | 'home' 
  | 'agenda' 
  | 'clients' 
  | 'finance' 
  | 'more' 
  | 'daymode' 
  | 'price_list' 
  | 'profile' 
  | 'testimonial' 
  | 'analytics' 
  | 'followup' 
  | 'todo' 
  | 'notifications' 
  | 'quick_action_settings';

interface SidebarProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
  onOpenCreateJob: () => void;
  isDarkMode: boolean;
  onToggleDarkMode: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  onChangeTab,
  onOpenCreateJob,
  isDarkMode,
  onToggleDarkMode
}) => {
  const { userProfile, logout } = useAuth();

  const mainNavItems = [
    { id: 'home', label: 'Beranda', icon: Home },
    { id: 'agenda', label: 'Agenda Acara', icon: Calendar },
    { id: 'clients', label: 'Klien & WO', icon: Users },
    { id: 'finance', label: 'Keuangan', icon: DollarSign },
    { id: 'more', label: 'Lainnya & Hub Bisnis', icon: Grid },
  ];

  return (
    <aside className="hidden md:flex flex-col w-64 bg-white dark:bg-slate-900 border-r border-slate-200/80 dark:border-slate-800 min-h-screen sticky top-0 z-30 transition-colors">
      {/* Brand Header */}
      <div className="p-5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-md shadow-indigo-600/30 text-white font-black text-lg tracking-tighter">
            MC
          </div>
          <div>
            <h1 className="font-extrabold text-indigo-600 dark:text-indigo-400 text-base tracking-tight leading-none">
              mcjob.id
            </h1>
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
              powered by career mc academy
            </span>
          </div>
        </div>
      </div>

      {/* Quick Action Button */}
      <div className="p-4">
        <button
          onClick={onOpenCreateJob}
          className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-[0.98] text-white font-extrabold text-xs flex items-center justify-center gap-2 shadow-md shadow-indigo-600/20 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Tambah Job Baru</span>
        </button>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
        <div className="px-3 py-1.5 text-[10px] font-extrabold uppercase tracking-wider text-slate-400 dark:text-slate-500">
          Menu Utama (5 Tab)
        </div>
        {mainNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onChangeTab(item.id as TabType)}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-bold text-xs transition-all text-left cursor-pointer ${
                isActive
                  ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-400'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800/50 dark:hover:text-slate-200'
              }`}
            >
              <Icon className={`w-4 h-4 ${isActive ? 'text-indigo-600 dark:text-indigo-400' : 'text-slate-400 dark:text-slate-500'}`} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Bottom Profile Widget & Theme Toggle */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800 space-y-3">
        <div className="flex items-center justify-between bg-slate-50 dark:bg-slate-800/50 p-2.5 rounded-xl border border-slate-200/60 dark:border-slate-800">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-full bg-indigo-100 dark:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300 font-extrabold text-xs flex items-center justify-center flex-shrink-0 border border-indigo-500">
              {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'M'}
            </div>
            <div className="truncate">
              <p className="text-xs font-extrabold text-slate-800 dark:text-slate-200 truncate">
                {userProfile?.displayName || 'MC Professional'}
              </p>
              <p className="text-[10px] text-slate-400 truncate">
                {userProfile?.city || 'Jakarta'}
              </p>
            </div>
          </div>

          <button
            onClick={onToggleDarkMode}
            className="p-1.5 rounded-lg text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors cursor-pointer"
            title="Ganti Tema"
          >
            {isDarkMode ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-600" />}
          </button>
        </div>

        <button
          onClick={logout}
          className="w-full flex items-center justify-center gap-2 py-2 text-xs font-bold text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 rounded-xl transition-colors cursor-pointer"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Keluar Sesi</span>
        </button>
      </div>
    </aside>
  );
};
