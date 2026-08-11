import React, { useMemo } from 'react';
import { Calendar, Plus, Zap, FileText, TrendingUp, ChevronRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
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
  const { userProfile } = useAuth();

  // Financial summary
  const summary = useMemo(() => {
    let totalFee = 0;
    let totalDp = 0;
    bookings.forEach((b) => {
      totalFee += b.fee || 0;
      totalDp += b.dp || 0;
    });
    return {
      totalFee,
      totalDp,
      remaining: totalFee - totalDp,
      totalJobs: bookings.length
    };
  }, [bookings]);

  // Next upcoming job
  const upcomingJobs = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    return bookings
      .filter((b) => b.date >= today && b.status !== 'completed')
      .sort((a, b) => a.date.localeCompare(b.date));
  }, [bookings]);

  const todayJob = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    return bookings.find((b) => b.date === today && b.status !== 'cancelled');
  }, [bookings]);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  return (
    <div className="space-y-5 pb-24 animate-fade-in">
      {/* Banner / Header */}
      <div className="bg-gradient-to-r from-indigo-600 to-indigo-700 rounded-3xl p-5 text-white shadow-lg relative overflow-hidden">
        <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-white/10 rounded-full blur-2xl pointer-events-none" />
        <p className="text-indigo-200 text-xs font-semibold uppercase tracking-wider">Selamat Datang</p>
        <h2 className="text-2xl font-extrabold mt-0.5">{userProfile?.stageName || userProfile?.name || 'MC Talent'}</h2>
        <p className="text-indigo-100 text-xs mt-1 font-medium">Siap memandu acara spektakuler hari ini!</p>

        {/* Quick Stats Pill */}
        <div className="mt-4 pt-4 border-t border-indigo-500/40 grid grid-cols-3 gap-2 text-center">
          <div>
            <span className="text-[10px] text-indigo-200 block">Total Job</span>
            <span className="text-base font-extrabold">{summary.totalJobs}</span>
          </div>
          <div className="border-x border-indigo-500/40">
            <span className="text-[10px] text-indigo-200 block">Total Pendapatan</span>
            <span className="text-xs font-bold truncate block">{formatCurrency(summary.totalFee)}</span>
          </div>
          <div>
            <span className="text-[10px] text-indigo-200 block">Sisa Piutang</span>
            <span className="text-xs font-bold text-amber-300 truncate block">{formatCurrency(summary.remaining)}</span>
          </div>
        </div>
      </div>

      {/* TODAY JOB HIGHLIGHT (If Any) */}
      {todayJob && (
        <div className="bg-amber-500/10 dark:bg-amber-500/20 border-2 border-amber-500/40 rounded-3xl p-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-amber-500 text-slate-900 flex items-center justify-center font-black animate-pulse">
              <Zap className="w-6 h-6 fill-slate-900" />
            </div>
            <div>
              <span className="text-[10px] font-extrabold uppercase tracking-wide text-amber-600 dark:text-amber-400">
                EVENT HARI INI!
              </span>
              <h4 className="text-sm font-bold text-slate-900 dark:text-white leading-tight">{todayJob.name}</h4>
              <p className="text-xs text-slate-500 dark:text-slate-400">{todayJob.start ? `${todayJob.start} WIB` : 'Jadwal Hari Ini'} • {todayJob.loc || 'Lokasi belum diset'}</p>
            </div>
          </div>
          <button
            onClick={() => onOpenDayMode(todayJob)}
            className="py-2 px-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold text-xs shadow-md"
          >
            Mode Hari H
          </button>
        </div>
      )}

      {/* QUICK ACTIONS */}
      <div>
        <h3 className="text-xs font-extrabold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2.5">
          Aksi Cepat
        </h3>
        <div className="grid grid-cols-4 gap-2.5">
          <button
            onClick={onOpenCreateJob}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 shadow-sm active:scale-95 transition-all"
          >
            <div className="w-10 h-10 rounded-xl bg-indigo-50 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center mb-1">
              <Plus className="w-5 h-5 stroke-[2.5]" />
            </div>
            <span className="text-[11px] font-bold text-slate-800 dark:text-slate-200 text-center leading-tight">Tambah Job</span>
          </button>

          <button
            onClick={() => onNavigateTab('daymode')}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 shadow-sm active:scale-95 transition-all"
          >
            <div className="w-10 h-10 rounded-xl bg-amber-50 dark:bg-amber-900/40 text-amber-600 dark:text-amber-400 flex items-center justify-center mb-1">
              <Zap className="w-5 h-5 stroke-[2.5]" />
            </div>
            <span className="text-[11px] font-bold text-slate-800 dark:text-slate-200 text-center leading-tight">Mode Hari H</span>
          </button>

          <button
            onClick={() => onNavigateTab('finance')}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 shadow-sm active:scale-95 transition-all"
          >
            <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-900/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mb-1">
              <FileText className="w-5 h-5 stroke-[2.5]" />
            </div>
            <span className="text-[11px] font-bold text-slate-800 dark:text-slate-200 text-center leading-tight">Invoice</span>
          </button>

          <button
            onClick={() => onNavigateTab('finance')}
            className="flex flex-col items-center justify-center p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 shadow-sm active:scale-95 transition-all"
          >
            <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400 flex items-center justify-center mb-1">
              <TrendingUp className="w-5 h-5 stroke-[2.5]" />
            </div>
            <span className="text-[11px] font-bold text-slate-800 dark:text-slate-200 text-center leading-tight">Keuangan</span>
          </button>
        </div>
      </div>

      {/* UPCOMING EVENTS LIST */}
      <div>
        <div className="flex items-center justify-between mb-2.5">
          <h3 className="text-xs font-extrabold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
            Acara Mendatang ({upcomingJobs.length})
          </h3>
          <button
            onClick={() => onNavigateTab('bookings')}
            className="text-xs font-bold text-indigo-600 dark:text-indigo-400 flex items-center hover:underline"
          >
            Lihat Semua <ChevronRight className="w-3.5 h-3.5 ml-0.5" />
          </button>
        </div>

        {upcomingJobs.length === 0 ? (
          <div className="card text-center py-8">
            <Calendar className="w-10 h-10 text-slate-300 dark:text-slate-600 mx-auto mb-2" />
            <h4 className="text-sm font-bold text-slate-700 dark:text-slate-300">Belum Ada Jadwal Job</h4>
            <p className="text-xs text-slate-400 mt-1">Klik "Tambah Job" untuk memasukkan jadwal MC baru.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {upcomingJobs.slice(0, 3).map((job) => {
              const remainingPay = (job.fee || 0) - (job.dp || 0);
              const isPaidFull = remainingPay <= 0;

              return (
                <div
                  key={job.id}
                  onClick={() => onOpenBookingDetail(job)}
                  className="card hover:border-indigo-300 dark:hover:border-indigo-600 cursor-pointer active:scale-[0.99] transition-all flex items-center justify-between"
                >
                  <div className="flex items-center gap-3.5">
                    {/* Date badge */}
                    <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-slate-800 text-indigo-600 dark:text-indigo-400 flex flex-col items-center justify-center font-extrabold border border-indigo-100 dark:border-slate-700">
                      <span className="text-[10px] uppercase font-bold text-slate-400 leading-none">
                        {new Date(job.date).toLocaleDateString('id-ID', { month: 'short' })}
                      </span>
                      <span className="text-lg leading-tight">
                        {new Date(job.date).getDate()}
                      </span>
                    </div>

                    <div>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 inline-block mb-1">
                        {job.category || 'Wedding'}
                      </span>
                      <h4 className="text-sm font-bold text-slate-900 dark:text-white leading-tight">
                        {job.name}
                      </h4>
                      <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                        Klien: {job.client || 'Umum'} • {job.loc || 'Lokasi TBD'}
                      </p>
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="text-xs font-extrabold text-slate-900 dark:text-white block">
                      {formatCurrency(job.fee)}
                    </span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full inline-block mt-1 ${isPaidFull ? 'badge-paid' : job.dp > 0 ? 'badge-partial' : 'badge-unpaid'}`}>
                      {isPaidFull ? 'Lunas' : job.dp > 0 ? 'DP Masuk' : 'Belum DP'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
