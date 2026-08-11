import React from 'react';
import { Sparkles, Moon, Sun, Bell, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface NavbarProps {
  isDarkMode: boolean;
  onToggleDarkMode: () => void;
  title?: string;
}

export const Navbar: React.FC<NavbarProps> = ({
  isDarkMode,
  onToggleDarkMode,
  title = 'MCJobId'
}) => {
  const { userProfile } = useAuth();

  return (
    <header className="sticky top-0 z-20 glass-header px-4 py-3 border-b border-slate-200/80 dark:border-slate-800 transition-colors">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        {/* Left Mobile Logo / Page Title */}
        <div className="flex items-center gap-3">
          <div className="md:hidden w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-500 flex items-center justify-center text-white shadow-sm">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base md:text-lg font-extrabold text-slate-900 dark:text-white tracking-tight">
              {title}
            </h1>
            <p className="text-[11px] text-slate-500 dark:text-slate-400 hidden md:block">
              Platform Manajemen Jadwal, Financial & Invoice Master of Ceremonies
            </p>
          </div>
        </div>

        {/* Right Actions Header */}
        <div className="flex items-center gap-2">
          <button
            onClick={onToggleDarkMode}
            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
            aria-label="Toggle Theme"
          >
            {isDarkMode ? <Sun className="w-5 h-5 text-amber-400" /> : <Moon className="w-5 h-5" />}
          </button>

          <div className="h-4 w-[1px] bg-slate-200 dark:bg-slate-800 mx-1 hidden md:block" />

          {/* User Profile Quick Badge for Mobile & Desktop */}
          <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 py-1.5 px-3 rounded-full border border-slate-200/60 dark:border-slate-700">
            <div className="w-6 h-6 rounded-full bg-indigo-600 text-white font-bold text-[11px] flex items-center justify-center">
              {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'M'}
            </div>
            <span className="text-xs font-bold text-slate-700 dark:text-slate-200 hidden sm:inline">
              {userProfile?.displayName || 'MC Studio'}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};
