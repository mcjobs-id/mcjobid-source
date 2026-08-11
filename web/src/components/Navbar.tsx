import React from 'react';
import { Sun, Moon, Bell, Sparkles } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface NavbarProps {
  title?: string;
  isDarkMode: boolean;
  onToggleDarkMode: () => void;
  onOpenNotifications?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  title = "MCJobId",
  isDarkMode,
  onToggleDarkMode,
  onOpenNotifications
}) => {
  const { userProfile } = useAuth();

  return (
    <header className="glass-bar sticky top-0 z-40 px-4 py-3 flex items-center justify-between">
      <div className="flex items-center gap-2.5">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-500 flex items-center justify-center text-white shadow-md font-bold text-lg">
          <Sparkles className="w-5 h-5 text-indigo-100" />
        </div>
        <div>
          <h1 className="text-base font-extrabold tracking-tight leading-none text-slate-900 dark:text-white">
            {title}
          </h1>
          {userProfile?.stageName && (
            <p className="text-[11px] font-medium text-indigo-600 dark:text-indigo-400 leading-tight">
              {userProfile.stageName}
            </p>
          )}
        </div>
      </div>

      <div className="flex items-center gap-2">
        {onOpenNotifications && (
          <button
            onClick={onOpenNotifications}
            className="w-9 h-9 rounded-xl flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors relative"
            aria-label="Notifications"
          >
            <Bell className="w-5 h-5" />
            <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-rose-500" />
          </button>
        )}

        <button
          onClick={onToggleDarkMode}
          className="w-9 h-9 rounded-xl flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          aria-label="Toggle Dark Mode"
        >
          {isDarkMode ? <Sun className="w-5 h-5 text-amber-400" /> : <Moon className="w-5 h-5 text-slate-700" />}
        </button>
      </div>
    </header>
  );
};
