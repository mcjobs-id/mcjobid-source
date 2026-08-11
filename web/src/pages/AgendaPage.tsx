import React, { useState } from 'react';
import { Calendar, Search, Plus, MapPin, DollarSign, Filter } from 'lucide-react';
import type { Booking } from '../types';

interface AgendaPageProps {
  bookings: Booking[];
  onSaveBooking: (booking: Booking) => void;
  onOpenDetail: (booking: Booking) => void;
  onOpenCreateJob: () => void;
}

type AgendaFilter = 'ALL' | 'UPCOMING' | 'COMPLETED' | 'DP_PENDING';

export const AgendaPage: React.FC<AgendaPageProps> = ({
  bookings,
  onOpenDetail,
  onOpenCreateJob
}) => {
  const [activeFilter, setActiveFilter] = useState<AgendaFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const todayStr = new Date().toISOString().split('T')[0];

  const filteredBookings = bookings.filter((b) => {
    // Search query filter
    const matchesSearch = 
      (b.eventTitle || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (b.clientName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (b.venue || '').toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    // Filter status
    if (activeFilter === 'UPCOMING') {
      return b.eventDate >= todayStr && b.status !== 'CANCELLED';
    }
    if (activeFilter === 'COMPLETED') {
      return b.status === 'COMPLETED' || (b.eventDate < todayStr && b.status !== 'CANCELLED');
    }
    if (activeFilter === 'DP_PENDING') {
      return b.paymentStatus !== 'PAID' && b.status !== 'CANCELLED';
    }
    return true;
  });

  return (
    <div className="space-y-5 animate-fade-in max-w-7xl mx-auto pb-10">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tight flex items-center gap-2">
            <Calendar className="w-5 h-5 text-indigo-600" />
            <span>Agenda Manggung & Jadwal MC</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Daftar lengkap jadwal acara, lokasi venue, dan catatan pelunasan honorarium.
          </p>
        </div>

        <button
          onClick={onOpenCreateJob}
          className="py-2.5 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-[0.98] text-white font-extrabold text-xs flex items-center justify-center gap-2 shadow-md shadow-indigo-600/20 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Tambah Job Baru</span>
        </button>
      </div>

      {/* Search Bar & Filter Chips */}
      <div className="bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm space-y-3">
        {/* Search Field */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Cari berdasarkan judul acara, nama klien, atau lokasi venue..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-xs outline-none focus:border-indigo-600"
          />
        </div>

        {/* Filter Chips */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 text-xs font-bold scrollbar-none">
          {[
            { id: 'ALL', label: 'Semua Job' },
            { id: 'UPCOMING', label: 'Mendatang' },
            { id: 'COMPLETED', label: 'Selesai' },
            { id: 'DP_PENDING', label: 'DP / Belum Lunas' },
          ].map((f) => (
            <button
              key={f.id}
              onClick={() => setActiveFilter(f.id as AgendaFilter)}
              className={`py-1.5 px-3 rounded-xl transition-all whitespace-nowrap cursor-pointer ${
                activeFilter === f.id
                  ? 'bg-indigo-600 text-white font-black shadow-sm'
                  : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300 hover:bg-slate-200'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* Agenda Booking List */}
      {filteredBookings.length === 0 ? (
        <div className="bg-white dark:bg-slate-800 p-12 rounded-3xl border border-slate-200/80 dark:border-slate-700 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 mx-auto flex items-center justify-center">
            <Calendar className="w-6 h-6" />
          </div>
          <h4 className="text-base font-extrabold text-slate-800 dark:text-slate-200">
            Tidak Ada Agenda Acara
          </h4>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Tidak ditemukan acara yang sesuai dengan pencarian atau filter yang dipilih.
          </p>
          <button
            onClick={onOpenCreateJob}
            className="py-2.5 px-4 rounded-xl bg-indigo-600 text-white font-bold text-xs inline-flex items-center gap-1.5 shadow-md cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Tambah Acara Baru</span>
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredBookings.map((b) => (
            <div
              key={b.id}
              onClick={() => onOpenDetail(b)}
              className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm hover:shadow-md hover:border-indigo-300 dark:hover:border-indigo-600 transition-all cursor-pointer space-y-3 flex flex-col justify-between"
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="px-2.5 py-0.5 rounded-full text-[11px] font-extrabold bg-indigo-50 text-indigo-700 dark:bg-indigo-950/60 dark:text-indigo-400">
                    {b.category || 'Wedding'}
                  </span>
                  <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-extrabold ${
                    b.paymentStatus === 'PAID'
                      ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400'
                      : b.dpAmount > 0
                      ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400'
                      : 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-400'
                  }`}>
                    {b.paymentStatus === 'PAID' ? 'LUNAS' : b.dpAmount > 0 ? 'DP' : 'BELUM BAYAR'}
                  </span>
                </div>

                <h4 className="text-base font-extrabold text-slate-900 dark:text-white leading-snug">
                  {b.eventTitle || b.clientName}
                </h4>

                <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                  Klien: <span className="font-bold text-slate-800 dark:text-slate-200">{b.clientName}</span>
                </p>

                <div className="space-y-1 pt-1 text-xs text-slate-500 dark:text-slate-400">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-indigo-500 flex-shrink-0" />
                    <span>{b.eventDate} ({b.eventTime || '19:00'})</span>
                  </div>
                  {b.venue && (
                    <div className="flex items-center gap-1.5 truncate">
                      <MapPin className="w-3.5 h-3.5 text-rose-500 flex-shrink-0" />
                      <span className="truncate">{b.venue}</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 dark:border-slate-700 flex items-center justify-between">
                <span className="text-[11px] text-slate-400">Honorarium</span>
                <span className="text-base font-black text-indigo-600 dark:text-indigo-400">
                  Rp {(b.totalFee || 0).toLocaleString('id-ID')}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
