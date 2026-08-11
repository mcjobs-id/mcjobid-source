import React from 'react';
import { 
  Home, 
  Calendar, 
  Clock, 
  DollarSign, 
  Users, 
  Tag, 
  User, 
  Plus, 
  Sparkles,
  LogOut,
  Moon,
  Sun
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export type TabType = 'home' | 'bookings' | 'daymode' | 'finance' | 'clients' | 'price_list' | 'profile';

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

  const navItems = [
    { id: 'home', label: 'Beranda / Overview', icon: Home },
    { id: 'bookings', label: 'Jadwal Acara (Jobs)', icon: Calendar },
    { id: 'daymode', label: 'Mode Hari H (Panggung)', icon: Clock },
    { id: 'finance', label: 'Keuangan & Invoice', icon: DollarSign },
    { id: 'clients', label: 'Daftar Klien & WO', icon: Users },
    { id: 'price_list', label: 'Katalog Rate Card', icon: Tag },
    { id: 'profile', label: 'Profil Saya & MC Studio', icon: User },
  ];

  return (
    <aside className="hidden md:flex flex-col w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 min-h-screen sticky top-0 z-30 transition-colors">
      {/* Brand Header */}
      <div className="p-6 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-500 flex items-center justify-center shadow-md shadow-indigo-500/20 text-white">
            <Sparkles className="w-6 h-6" />
          </div>
          <div>
            <h1 className="font-extrabold text-slate-900 dark:text-white text-lg tracking-tight leading-none">
              MCJobId
            </h1>
            <span className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400">
              MC Hub & Finance
            </span>
          </div>
        </div>
      </div>

      {/* Quick Action Button */}
      <div className="p-4">
        <button
          onClick={onOpenCreateJob}
          className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-[0.98] text-white font-semibold text-sm flex items-center justify-center gap-2 shadow-md shadow-indigo-600/20 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Tambah Job Baru</span>
        </button>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
        <div className="px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500">
          Menu Utama
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onChangeTab(item.id as TabType)}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all text-left cursor-pointer ${
                isActive
                  ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950/50 dark:text-indigo-400 font-semibold'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800/50 dark:hover:text-slate-200'
              }`}
            >
              <Icon className={`w-5 h-5 ${isActive ? 'text-indigo-600 dark:text-indigo-400' : 'text-slate-400 dark:text-slate-500'}`} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Bottom Profile Widget & Theme Toggle */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800 space-y-3">
        <div className="flex items-center justify-between bg-slate-50 dark:bg-slate-800/50 p-2.5 rounded-xl border border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-full bg-indigo-100 dark:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300 font-bold text-xs flex items-center justify-center flex-shrink-0">
              {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'MC'}
            </div>
            <div className="truncate">
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">
                {userProfile?.displayName || 'Master of Ceremony'}
              </p>
              <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate">
                {userProfile?.city || 'Professional MC'}
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
          className="w-full flex items-center justify-center gap-2 py-2 text-xs font-semibold text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 rounded-lg transition-colors cursor-pointer"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Keluar Akun</span>
        </button>
      </div>
    </aside>
  );
};
