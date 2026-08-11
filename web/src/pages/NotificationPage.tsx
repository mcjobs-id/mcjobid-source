import React from 'react';
import { Bell, ArrowLeft, CheckCircle2 } from 'lucide-react';

interface NotificationPageProps {
  onBack: () => void;
}

export const NotificationPage: React.FC<NotificationPageProps> = ({ onBack }) => {
  return (
    <div className="space-y-5 animate-fade-in max-w-4xl mx-auto pb-10">
      <div className="flex items-center gap-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tight flex items-center gap-2">
            <Bell className="w-5 h-5 text-rose-600" />
            <span>Pusat Pengingat & Notifikasi</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Pengingat otomatis jadwal acara H-1, brief WO, dan pelunasan.
          </p>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200/80 dark:border-slate-700 text-center space-y-2">
        <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto" />
        <h4 className="text-base font-extrabold text-slate-900 dark:text-white">Semua Pengingat Aman!</h4>
        <p className="text-xs text-slate-400">Tidak ada pengingat mendesak yang memerlukan tindakan Anda saat ini.</p>
      </div>
    </div>
  );
};
