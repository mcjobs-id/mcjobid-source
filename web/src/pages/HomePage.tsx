import React, { useState } from 'react';
import { 
  Calendar, 
  Search, 
  Bell, 
  Cloud,
  CloudOff, 
  Mic, 
  MessageSquare, 
  HelpCircle, 
  Plus, 
  ArrowRight, 
  CheckCircle2, 
  AlertCircle,
  TrendingUp,
  MapPin,
  Clock,
  Sparkles
} from 'lucide-react';
import type { Booking } from '../types';
import { useAuth } from '../context/AuthContext';

interface HomePageProps {
  bookings: Booking[];
  onNavigateTab: (tab: any) => void;
  onOpenCreateJob: () => void;
  onOpenBookingDetail: (booking: Booking) => void;
  onOpenDayMode: (booking: Booking) => void;
}

type TimeFilter = 'TODAY' | 'THIS_MONTH' | 'THIS_YEAR' | 'CUSTOM';

export const HomePage: React.FC<HomePageProps> = ({
  bookings,
  onNavigateTab,
  onOpenCreateJob,
  onOpenBookingDetail,
  onOpenDayMode
}) => {
  const { userProfile } = useAuth();
  const [selectedFilter, setSelectedFilter] = useState<TimeFilter>('THIS_MONTH');

  // Today booking check
  const todayStr = new Date().toISOString().split('T')[0];
  const todayBooking = bookings.find(b => b.eventDate === todayStr && b.status !== 'CANCELLED');

  // Next booking calculation
  const upcomingBookings = bookings
    .filter(b => b.status !== 'CANCELLED' && b.eventDate >= todayStr)
    .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
  
  const nextBooking = upcomingBookings[0];

  // Financial calculations based on selected filter
  const filteredBookings = bookings.filter(b => {
    if (b.status === 'CANCELLED') return false;
    const bDate = new Date(b.eventDate);
    const now = new Date();

    if (selectedFilter === 'TODAY') {
      return b.eventDate === todayStr;
    }
    if (selectedFilter === 'THIS_MONTH') {
      return bDate.getMonth() === now.getMonth() && bDate.getFullYear() === now.getFullYear();
    }
    if (selectedFilter === 'THIS_YEAR') {
      return bDate.getFullYear() === now.getFullYear();
    }
    return true;
  });

  const totalHonor = filteredBookings.reduce((sum, b) => sum + (b.totalFee || 0), 0);
  const totalPaid = filteredBookings.reduce((sum, b) => sum + (b.dpAmount || 0), 0);
  const totalUnpaid = filteredBookings.reduce((sum, b) => {
    if (b.paymentStatus === 'PAID') return sum;
    return sum + (b.totalFee - (b.dpAmount || 0));
  }, 0);

  return (
    <div className="space-y-5 animate-fade-in max-w-7xl mx-auto pb-10">
      {/* HomeHeader - Android Presisi */}
      <div className="bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm flex items-center justify-between">
        <div className="flex items-center gap-3">
          {/* MC Avatar */}
          <div className="w-10 h-10 rounded-full bg-indigo-100 dark:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300 font-extrabold text-sm flex items-center justify-center border-2 border-indigo-600 flex-shrink-0">
            {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'M'}
          </div>

          <div>
            <div className="flex items-center gap-1.5">
              <span className="text-base font-black text-indigo-600 dark:text-indigo-400 leading-none">
                mcjob.id
              </span>
              <Cloud className="w-3.5 h-3.5 text-emerald-500" />
            </div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
              powered by career mc academy
            </p>
            <h3 className="text-sm font-extrabold text-slate-900 dark:text-white mt-0.5">
              Halo, {userProfile?.displayName || 'MC Professional'} 👋
            </h3>
          </div>
        </div>

        <div className="flex items-center gap-1">
          <button
            onClick={() => onNavigateTab('agenda')}
            className="p-2 rounded-xl text-indigo-600 dark:text-indigo-400 hover:bg-slate-100 dark:hover:bg-slate-700 cursor-pointer"
            title="Cari Job"
          >
            <Search className="w-5 h-5" />
          </button>
          <button
            onClick={() => onNavigateTab('notifications')}
            className="p-2 rounded-xl text-indigo-600 dark:text-indigo-400 hover:bg-slate-100 dark:hover:bg-slate-700 relative cursor-pointer"
            title="Pengingat"
          >
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500" />
          </button>
        </div>
      </div>

      {/* HeroFinancialSurface - Primary Indigo Card with Filter Chips */}
      <div className="bg-indigo-600 text-white p-5 sm:p-6 rounded-3xl shadow-xl shadow-indigo-600/20 space-y-4">
        {/* Filter Chips Bar */}
        <div className="bg-black/20 p-1 rounded-xl flex items-center justify-between gap-1 text-xs font-bold">
          {(['TODAY', 'THIS_MONTH', 'THIS_YEAR', 'CUSTOM'] as TimeFilter[]).map((filter) => {
            const labels: Record<TimeFilter, string> = {
              TODAY: 'Hari Ini',
              THIS_MONTH: 'Bulan Ini',
              THIS_YEAR: 'Tahun Ini',
              CUSTOM: 'Rentang 📅'
            };
            const isSelected = selectedFilter === filter;
            return (
              <button
                key={filter}
                onClick={() => setSelectedFilter(filter)}
                className={`flex-1 py-1.5 px-2 rounded-lg transition-all text-center cursor-pointer ${
                  isSelected
                    ? 'bg-white text-indigo-700 font-extrabold shadow-sm'
                    : 'text-white/80 hover:text-white'
                }`}
              >
                {labels[filter]}
              </button>
            );
          })}
        </div>

        {/* Total Omset */}
        <div className="flex items-center justify-between pt-1">
          <span className="text-xs font-semibold text-white/80">Total Omset</span>
          <span className="text-2xl sm:text-3xl font-black text-white">
            Rp {totalHonor.toLocaleString('id-ID')}
          </span>
        </div>

        <div className="h-[1px] bg-white/20" />

        {/* Breakdown Rows */}
        <div className="grid grid-cols-2 gap-4 pt-1 text-xs">
          <div>
            <span className="text-white/70">Terbayar / DP</span>
            <p className="text-base font-bold text-white mt-0.5">
              Rp {totalPaid.toLocaleString('id-ID')}
            </p>
          </div>

          <div className="text-right">
            <span className="text-white/70">Sisa Piutang Job</span>
            <p className="text-base font-bold text-amber-300 mt-0.5">
              Rp {totalUnpaid.toLocaleString('id-ID')}
            </p>
          </div>
        </div>
      </div>

      {/* TopQuickFilterChipsSurface: Testimoni & Bantuan Kendala */}
      <div className="grid grid-cols-2 gap-3">
        <button
          onClick={() => onNavigateTab('testimonial')}
          className="p-3.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700 flex items-center gap-3 shadow-sm hover:shadow-md transition-all cursor-pointer text-left"
        >
          <div className="w-9 h-9 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center justify-center flex-shrink-0">
            <MessageSquare className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-extrabold text-slate-900 dark:text-white">Testimoni Klien</p>
            <p className="text-[10px] text-slate-400">Review & Feedback</p>
          </div>
        </button>

        <a
          href="https://wa.link/rxug92"
          target="_blank"
          rel="noopener noreferrer"
          className="p-3.5 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700 flex items-center gap-3 shadow-sm hover:shadow-md transition-all cursor-pointer text-left"
        >
          <div className="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center flex-shrink-0">
            <HelpCircle className="w-5 h-5" />
          </div>
          <div>
            <p className="text-xs font-extrabold text-slate-900 dark:text-white">Bantuan Kendala</p>
            <p className="text-[10px] text-slate-400">Support WA Official</p>
          </div>
        </a>
      </div>

      {/* ACARA HARI INI Banner (Shown strictly if there's a job today) */}
      {todayBooking && (
        <div className="bg-indigo-600 text-white p-4 sm:p-5 rounded-2xl shadow-lg flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <span className="px-2.5 py-0.5 rounded-md text-[10px] font-black bg-white/20 uppercase tracking-wider text-white">
              ACARA HARI INI
            </span>
            <h4 className="text-base font-extrabold text-white mt-1">
              {todayBooking.eventTitle || todayBooking.clientName}
            </h4>
            <p className="text-xs text-white/80 mt-0.5 flex items-center gap-2">
              <span>{todayBooking.eventTime || '19:00'}</span>
              <span>•</span>
              <span className="truncate">{todayBooking.venue || 'Venue'}</span>
            </p>
          </div>

          <button
            onClick={() => onOpenDayMode(todayBooking)}
            className="py-2.5 px-4 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs flex items-center justify-center gap-2 shadow-md cursor-pointer flex-shrink-0"
          >
            <Mic className="w-4 h-4" />
            <span>MC Day Mode</span>
          </button>
        </div>
      )}

      {/* Main Grid: Agenda Berikutnya & Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Left Column: Agenda Berikutnya (lg:col-span-8) */}
        <div className="lg:col-span-8 space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-600" />
              <span>Agenda Berikutnya</span>
            </h3>
            <button
              onClick={() => onNavigateTab('agenda')}
              className="text-xs font-bold text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1 cursor-pointer"
            >
              <span>Lihat Semua</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {nextBooking ? (
            <div
              onClick={() => onOpenBookingDetail(nextBooking)}
              className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm hover:shadow-md hover:border-indigo-300 dark:hover:border-indigo-600 transition-all cursor-pointer space-y-3"
            >
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-indigo-50 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-400">
                  {nextBooking.category || 'Wedding'}
                </span>
                <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                  nextBooking.paymentStatus === 'PAID'
                    ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400'
                    : nextBooking.dpAmount > 0
                    ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400'
                    : 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-400'
                }`}>
                  {nextBooking.paymentStatus === 'PAID' ? 'LUNAS' : nextBooking.dpAmount > 0 ? 'DP' : 'BELUM BAYAR'}
                </span>
              </div>

              <div>
                <h4 className="text-base font-extrabold text-slate-900 dark:text-white">
                  {nextBooking.eventTitle || nextBooking.clientName}
                </h4>
                <div className="flex items-center gap-3 text-xs text-slate-500 dark:text-slate-400 mt-1">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-indigo-500" />
                    {nextBooking.eventDate}
                  </span>
                  {nextBooking.venue && (
                    <span className="flex items-center gap-1 truncate">
                      <MapPin className="w-3.5 h-3.5 text-rose-500" />
                      {nextBooking.venue}
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-2 border-t border-slate-100 dark:border-slate-700 flex items-center justify-between text-xs">
                <span className="text-slate-400">Honorarium MC</span>
                <span className="text-base font-black text-indigo-600 dark:text-indigo-400">
                  Rp {(nextBooking.totalFee || 0).toLocaleString('id-ID')}
                </span>
              </div>
            </div>
          ) : (
            <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200/80 dark:border-slate-700 text-center space-y-3">
              <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-700 mx-auto flex items-center justify-center text-slate-400">
                <Calendar className="w-5 h-5" />
              </div>
              <p className="text-sm font-extrabold text-slate-800 dark:text-slate-200">
                Belum ada agenda terdekat
              </p>
              <p className="text-xs text-slate-400">
                Catat job acara pertama Anda untuk mulai mengelola jadwal profesional.
              </p>
              <button
                onClick={onOpenCreateJob}
                className="py-2.5 px-4 rounded-xl bg-indigo-600 text-white font-bold text-xs inline-flex items-center gap-1.5 shadow-md cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Catat Job Pertama</span>
              </button>
            </div>
          )}
        </div>

        {/* Right Column: Quick Tools (lg:col-span-4) */}
        <div className="lg:col-span-4 space-y-3">
          <h3 className="text-sm font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-indigo-600" />
            <span>Pintasan Akses</span>
          </h3>

          <div className="bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200/80 dark:border-slate-700 space-y-2">
            <button
              onClick={onOpenCreateJob}
              className="w-full p-3 rounded-xl bg-indigo-600 text-white font-bold text-xs flex items-center justify-between shadow-md cursor-pointer"
            >
              <div className="flex items-center gap-2">
                <Plus className="w-4 h-4" />
                <span>Tambah Job Baru</span>
              </div>
              <ArrowRight className="w-4 h-4" />
            </button>

            <button
              onClick={() => onNavigateTab('more')}
              className="w-full p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 font-bold text-xs flex items-center justify-between border border-slate-200/60 dark:border-slate-700 cursor-pointer"
            >
              <span>Hub Bisnis MC (Invoice & Rate Card)</span>
              <ArrowRight className="w-4 h-4 text-slate-400" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
