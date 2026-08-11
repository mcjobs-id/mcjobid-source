import React, { useState } from 'react';
import { 
  Tag, 
  TrendingUp, 
  MessageSquare, 
  CheckSquare, 
  Zap, 
  FileText, 
  Bell, 
  Settings, 
  LogOut, 
  ChevronRight,
  User,
  Sparkles,
  AlertTriangle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface MorePageProps {
  onNavigateTab: (tab: any) => void;
}

export const MorePage: React.FC<MorePageProps> = ({ onNavigateTab }) => {
  const { userProfile, logout } = useAuth();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const menuItems = [
    {
      id: 'price_list',
      title: 'Rate Card & Price List',
      subtitle: 'Kelola paket harga MC, bagikan ke klien & buat job instan',
      icon: Tag,
      color: 'text-indigo-600 dark:text-indigo-400',
      bgColor: 'bg-indigo-50 dark:bg-indigo-950/60'
    },
    {
      id: 'analytics',
      title: 'Analisis Performa',
      subtitle: 'Pantau omset, pengeluaran, dan laba bersih secara visual',
      icon: TrendingUp,
      color: 'text-emerald-600 dark:text-emerald-400',
      bgColor: 'bg-emerald-50 dark:bg-emerald-950/60'
    },
    {
      id: 'followup',
      title: 'Pusat Follow Up',
      subtitle: 'Konfirmasi agenda dan penagihan piutang pelunasan',
      icon: MessageSquare,
      color: 'text-blue-600 dark:text-blue-400',
      bgColor: 'bg-blue-50 dark:bg-blue-950/60'
    },
    {
      id: 'todo',
      title: 'Daftar Tugas & To-Do MC',
      subtitle: 'Checklist persiapan perform, gladi resik, & karier',
      icon: CheckSquare,
      color: 'text-purple-600 dark:text-purple-400',
      bgColor: 'bg-purple-50 dark:bg-purple-950/60'
    },
    {
      id: 'quick_action_settings',
      title: 'Pintasan Cepat Dasbor',
      subtitle: 'Kustomisasi tombol melayang & hak akses fitur cepat',
      icon: Zap,
      color: 'text-amber-600 dark:text-amber-400',
      bgColor: 'bg-amber-50 dark:bg-amber-950/60'
    },
    {
      id: 'invoice',
      title: 'Generator Invoice',
      subtitle: 'Buat dan bagikan invoice PDF profesional',
      icon: FileText,
      color: 'text-indigo-600 dark:text-indigo-400',
      bgColor: 'bg-indigo-50 dark:bg-indigo-950/60'
    },
    {
      id: 'notifications',
      title: 'Pusat Pengingat',
      subtitle: 'Notifikasi otomatis agenda dan pelunasan H-1',
      icon: Bell,
      color: 'text-rose-600 dark:text-rose-400',
      bgColor: 'bg-rose-50 dark:bg-rose-950/60'
    },
  ];

  return (
    <div className="space-y-5 animate-fade-in max-w-4xl mx-auto pb-10">
      {/* Header */}
      <div>
        <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tight">
          Lainnya & Hub Bisnis MC
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          Pusat kendali fitur profesional, Rate Card, Invoice, & Pengaturan Akun.
        </p>
      </div>

      {/* Profile Header Card */}
      <div 
        onClick={() => onNavigateTab('profile')}
        className="bg-indigo-600 text-white p-5 rounded-3xl shadow-lg shadow-indigo-600/20 flex items-center justify-between cursor-pointer hover:bg-indigo-700 transition-all"
      >
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-full bg-white/20 text-white font-black text-xl flex items-center justify-center border-2 border-white flex-shrink-0">
            {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'M'}
          </div>

          <div>
            <h3 className="text-base font-extrabold text-white">
              {userProfile?.displayName || 'MC Professional'}
            </h3>
            <p className="text-xs text-white/80">
              {userProfile?.city || 'Jakarta'} • Professional MC
            </p>
            <p className="text-[11px] font-bold text-indigo-200 mt-1 flex items-center gap-1">
              <span>Lihat & Edit Profil MC Studio</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </p>
          </div>
        </div>
      </div>

      {/* Menu Hub Card */}
      <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200/80 dark:border-slate-700 shadow-sm overflow-hidden divide-y divide-slate-100 dark:divide-slate-700/60">
        {menuItems.map((item) => {
          const Icon = item.icon;
          return (
            <div
              key={item.id}
              onClick={() => onNavigateTab(item.id)}
              className="p-4 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-700/40 transition-colors cursor-pointer"
            >
              <div className="flex items-center gap-3.5">
                <div className={`w-10 h-10 rounded-2xl ${item.bgColor} ${item.color} flex items-center justify-center flex-shrink-0`}>
                  <Icon className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-sm font-extrabold text-slate-900 dark:text-white">
                    {item.title}
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    {item.subtitle}
                  </p>
                </div>
              </div>

              <ChevronRight className="w-4 h-4 text-slate-400 flex-shrink-0" />
            </div>
          );
        })}

        {/* Destructive Logout Menu Item */}
        <div
          onClick={() => setShowLogoutModal(true)}
          className="p-4 flex items-center justify-between hover:bg-rose-50/50 dark:hover:bg-rose-950/20 transition-colors cursor-pointer"
        >
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-2xl bg-rose-50 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400 flex items-center justify-center flex-shrink-0">
              <LogOut className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-extrabold text-rose-600 dark:text-rose-400">
                Keluar Sesi
              </h4>
              <p className="text-xs text-slate-400">
                Selesaikan sesi akses mcjob.id Anda
              </p>
            </div>
          </div>

          <ChevronRight className="w-4 h-4 text-rose-400 flex-shrink-0" />
        </div>
      </div>

      {/* Destructive Logout Modal */}
      {showLogoutModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-white dark:bg-slate-800 rounded-3xl p-6 max-w-sm w-full border border-slate-200 dark:border-slate-700 shadow-2xl space-y-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 dark:bg-rose-950/60 dark:text-rose-400 mx-auto flex items-center justify-center">
              <AlertTriangle className="w-6 h-6" />
            </div>

            <div>
              <h3 className="text-lg font-black text-slate-900 dark:text-white">
                Konfirmasi Keluar Sesi
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 leading-relaxed">
                Apakah Anda yakin ingin keluar dari akun MCJOB.id? Data Anda tetap aman tersinkronisasi di server cloud.
              </p>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={() => setShowLogoutModal(false)}
                className="flex-1 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold text-xs cursor-pointer"
              >
                Batal
              </button>
              <button
                onClick={logout}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 text-white font-extrabold text-xs shadow-md cursor-pointer"
              >
                Ya, Keluar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
