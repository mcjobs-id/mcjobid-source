import React from 'react';
import { 
  Calendar, 
  DollarSign, 
  Clock, 
  Plus, 
  FileText, 
  Users, 
  Tag, 
  ArrowRight, 
  Sparkles,
  TrendingUp,
  AlertCircle,
  CheckCircle2,
  MapPin
} from 'lucide-react';
import type { Booking } from '../types';

interface HomePageProps {
  bookings: Booking[];
  onNavigateTab: (tab: any) => void;
  onOpenCreateJob: () => void;
  onOpenBookingDetail: (booking: Booking) => void;
  onOpenDayMode: (booking: Booking) => void;
}

export const HomePage: React.FC<HomePageProps> = ({
  bookings,
  onNavigateTab,
  onOpenCreateJob,
  onOpenBookingDetail,
  onOpenDayMode
}) => {
  // Financial metrics calculation
  const totalJobs = bookings.length;
  
  const totalRevenue = bookings.reduce((sum, b) => sum + (b.totalFee || 0), 0);
  const totalPaid = bookings.reduce((sum, b) => sum + (b.dpAmount || 0), 0);
  const totalUnpaid = bookings.reduce((sum, b) => {
    if (b.paymentStatus === 'PAID') return sum;
    return sum + (b.totalFee - (b.dpAmount || 0));
  }, 0);

  // Filter upcoming jobs sorted by date
  const upcomingBookings = bookings
    .filter(b => b.status !== 'CANCELLED')
    .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime())
    .slice(0, 5);

  const nextJob = upcomingBookings[0];

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl mx-auto">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-indigo-700 via-indigo-600 to-indigo-800 text-white p-6 md:p-8 shadow-xl shadow-indigo-600/15">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/15 backdrop-blur-md text-xs font-semibold text-indigo-100 mb-3">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Master of Ceremonies Dashboard</span>
            </div>
            <h2 className="text-2xl md:text-3xl font-extrabold tracking-tight">
              Kelola Jadwal, Honorarium & Event MC
            </h2>
            <p className="text-indigo-100 text-sm mt-1 max-w-2xl">
              Pantau jadwal manggung, catatan DP/pelunasan, cetak invoice profesional, dan rundow acara secara real-time.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={onOpenCreateJob}
              className="py-3 px-5 rounded-2xl bg-white text-indigo-700 font-bold text-sm hover:bg-indigo-50 active:scale-95 shadow-md transition-all flex items-center gap-2 cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Tambah Acara Baru</span>
            </button>
          </div>
        </div>
      </div>

      {/* Financial Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Total Job MC
            </p>
            <p className="text-2xl font-black text-slate-900 dark:text-white mt-1">
              {totalJobs} <span className="text-xs font-normal text-slate-400">Acara</span>
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center justify-center">
            <Calendar className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Estimasi Pemasukan
            </p>
            <p className="text-2xl font-black text-slate-900 dark:text-white mt-1">
              Rp {totalRevenue.toLocaleString('id-ID')}
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
            <TrendingUp className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              DP / Terbayar
            </p>
            <p className="text-2xl font-black text-emerald-600 dark:text-emerald-400 mt-1">
              Rp {totalPaid.toLocaleString('id-ID')}
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
            <CheckCircle2 className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Sisa Piutang Job
            </p>
            <p className="text-2xl font-black text-amber-600 dark:text-amber-400 mt-1">
              Rp {totalUnpaid.toLocaleString('id-ID')}
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center">
            <AlertCircle className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Main Grid Section: Desktop Multi-Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Upcoming Jobs List (2 cols on lg) */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <Calendar className="w-5 h-5 text-indigo-600" />
              <span>Jadwal Manggung Mendatang</span>
            </h3>
            <button
              onClick={() => onNavigateTab('bookings')}
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 dark:text-indigo-400 flex items-center gap-1 cursor-pointer"
            >
              <span>Lihat Semua</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {upcomingBookings.length === 0 ? (
            <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 text-center space-y-3">
              <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-700 mx-auto flex items-center justify-center text-slate-400">
                <Calendar className="w-6 h-6" />
              </div>
              <p className="text-sm font-semibold text-slate-600 dark:text-slate-300">
                Belum ada jadwal acara mendatang
              </p>
              <button
                onClick={onOpenCreateJob}
                className="py-2 px-4 rounded-xl bg-indigo-600 text-white font-semibold text-xs inline-flex items-center gap-1.5 cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Buat Jadwal Pertama</span>
              </button>
            </div>
          ) : (
            <div className="space-y-3">
              {upcomingBookings.map((b) => (
                <div
                  key={b.id}
                  onClick={() => onOpenBookingDetail(b)}
                  className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 shadow-sm hover:shadow-md hover:border-indigo-300 dark:hover:border-indigo-600 transition-all cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                >
                  <div className="space-y-1.5">
                    <div className="flex items-center gap-2">
                      <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-indigo-50 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-400">
                        {b.category || 'Wedding'}
                      </span>
                      <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                        b.paymentStatus === 'PAID'
                          ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400'
                          : b.dpAmount > 0
                          ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400'
                          : 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-400'
                      }`}>
                        {b.paymentStatus === 'PAID' ? 'LUNAS' : b.dpAmount > 0 ? 'DP' : 'BELUM BAYAR'}
                      </span>
                    </div>

                    <h4 className="text-base font-bold text-slate-900 dark:text-white">
                      {b.eventTitle || b.clientName}
                    </h4>

                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500 dark:text-slate-400">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5 text-indigo-500" />
                        {b.eventDate}
                      </span>
                      {b.venue && (
                        <span className="flex items-center gap-1 truncate max-w-xs">
                          <MapPin className="w-3.5 h-3.5 text-rose-500" />
                          {b.venue}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center sm:flex-col sm:items-end justify-between border-t sm:border-t-0 pt-3 sm:pt-0 border-slate-100 dark:border-slate-700">
                    <span className="text-xs text-slate-400">Honorarium</span>
                    <span className="text-base font-black text-indigo-600 dark:text-indigo-400">
                      Rp {(b.totalFee || 0).toLocaleString('id-ID')}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right Column: Quick Tools & Stage Mode Shortcut */}
        <div className="space-y-6">
          {/* Stage Mode Spotlight */}
          {nextJob && (
            <div className="bg-gradient-to-br from-slate-900 to-indigo-950 text-white p-5 rounded-2xl shadow-lg border border-slate-800 relative overflow-hidden space-y-4">
              <div className="flex items-center justify-between">
                <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/30 text-indigo-300 border border-indigo-500/40 flex items-center gap-1">
                  <Clock className="w-3 h-3" /> Mode Hari H
                </span>
                <span className="text-xs text-slate-400">{nextJob.eventDate}</span>
              </div>

              <div>
                <h4 className="text-sm text-slate-400 font-medium">Acara Berikutnya:</h4>
                <h3 className="text-lg font-bold text-white leading-tight mt-0.5">
                  {nextJob.eventTitle}
                </h3>
                <p className="text-xs text-indigo-300 mt-1 flex items-center gap-1">
                  <MapPin className="w-3 h-3" /> {nextJob.venue || 'Lokasi belum diisi'}
                </p>
              </div>

              <button
                onClick={() => onOpenDayMode(nextJob)}
                className="w-full py-2.5 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-md transition-all cursor-pointer"
              >
                <span>Buka Mode Hari H (Stage Control)</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          )}

          {/* Quick Hub Shortcuts */}
          <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700/80 space-y-3">
            <h4 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span>Menu Akses Cepat</span>
            </h4>

            <div className="grid grid-cols-2 gap-2.5">
              <button
                onClick={() => onNavigateTab('finance')}
                className="p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 border border-slate-200/60 dark:border-slate-700 text-left transition-all group cursor-pointer"
              >
                <DollarSign className="w-5 h-5 text-indigo-600 mb-1 group-hover:scale-110 transition-transform" />
                <p className="text-xs font-bold text-slate-800 dark:text-slate-200">Laporan Keuangan</p>
                <p className="text-[10px] text-slate-400">Grafik & Cashflow</p>
              </button>

              <button
                onClick={() => onNavigateTab('clients')}
                className="p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 border border-slate-200/60 dark:border-slate-700 text-left transition-all group cursor-pointer"
              >
                <Users className="w-5 h-5 text-emerald-600 mb-1 group-hover:scale-110 transition-transform" />
                <p className="text-xs font-bold text-slate-800 dark:text-slate-200">Daftar Klien & WO</p>
                <p className="text-[10px] text-slate-400">Kontak & Instansi</p>
              </button>

              <button
                onClick={() => onNavigateTab('price_list')}
                className="p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 border border-slate-200/60 dark:border-slate-700 text-left transition-all group cursor-pointer"
              >
                <Tag className="w-5 h-5 text-amber-600 mb-1 group-hover:scale-110 transition-transform" />
                <p className="text-xs font-bold text-slate-800 dark:text-slate-200">Rate Card MC</p>
                <p className="text-[10px] text-slate-400">Katalog Paket</p>
              </button>

              <button
                onClick={() => onNavigateTab('bookings')}
                className="p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 border border-slate-200/60 dark:border-slate-700 text-left transition-all group cursor-pointer"
              >
                <FileText className="w-5 h-5 text-indigo-600 mb-1 group-hover:scale-110 transition-transform" />
                <p className="text-xs font-bold text-slate-800 dark:text-slate-200">Cetak Invoice</p>
                <p className="text-[10px] text-slate-400">PDF Pelunasan</p>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
