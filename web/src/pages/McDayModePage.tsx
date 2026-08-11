import React, { useState, useEffect } from 'react';
import { ArrowLeft, Zap, Phone, CheckCircle2 } from 'lucide-react';
import type { Booking, ChecklistItem } from '../types';
import { subscribeChecklists, saveChecklistItem } from '../services/firebaseService';

interface McDayModePageProps {
  booking: Booking | null;
  allBookings: Booking[];
  onBack: () => void;
  onSelectBooking: (b: Booking) => void;
}

export const McDayModePage: React.FC<McDayModePageProps> = ({
  booking,
  allBookings,
  onBack,
  onSelectBooking
}) => {
  const [activeBooking, setActiveBooking] = useState<Booking | null>(booking);
  const [checklists, setChecklists] = useState<ChecklistItem[]>([]);
  const [timeStr, setTimeStr] = useState('');

  // Clock timer
  useEffect(() => {
    const timer = setInterval(() => {
      const now = new Date();
      setTimeStr(now.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Set default active booking if null
  useEffect(() => {
    if (!activeBooking && allBookings.length > 0) {
      const today = new Date().toISOString().split('T')[0];
      const todayJob = allBookings.find((b) => b.date === today) || allBookings[0];
      setActiveBooking(todayJob);
    }
  }, [allBookings, activeBooking]);

  useEffect(() => {
    if (!activeBooking) return;
    const unsub = subscribeChecklists(activeBooking.ownerId || '', activeBooking.id, (items) => {
      setChecklists(items);
    });
    return () => unsub();
  }, [activeBooking]);

  const handleToggle = async (item: ChecklistItem) => {
    await saveChecklistItem({
      ...item,
      isCompleted: !item.isCompleted
    });
  };

  if (!activeBooking) {
    return (
      <div className="text-center py-16 space-y-3">
        <Zap className="w-12 h-12 text-slate-300 dark:text-slate-600 mx-auto" />
        <h3 className="text-base font-bold text-slate-700 dark:text-slate-300">Belum Ada Acara Dipilih</h3>
        <p className="text-xs text-slate-400">Pilih acara dari daftar jadwal untuk mengaktifkan Mode Hari H.</p>
      </div>
    );
  }

  const completedCount = checklists.filter((c) => c.isCompleted).length;
  const progressPercent = checklists.length > 0 ? Math.round((completedCount / checklists.length) * 100) : 0;

  return (
    <div className="space-y-4 pb-24 animate-fade-in bg-slate-950 text-white p-4 -mx-4 -mt-4 min-h-screen">
      {/* Mode Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-slate-800 text-slate-300 hover:text-white text-xs font-bold flex items-center gap-1"
        >
          <ArrowLeft className="w-4 h-4" /> Keluar Mode Hari H
        </button>

        <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/40 text-xs font-extrabold animate-pulse">
          <Zap className="w-3.5 h-3.5 fill-amber-400" /> STAGE ON-AIR
        </div>
      </div>

      {/* Selector dropdown if multiple */}
      {allBookings.length > 1 && (
        <div>
          <label className="text-[10px] font-bold text-slate-400 block mb-1">GANTI EVENT AKTIF:</label>
          <select
            value={activeBooking.id}
            onChange={(e) => {
              const selected = allBookings.find((b) => b.id === e.target.value);
              if (selected) setActiveBooking(selected);
            }}
            className="w-full p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-white text-xs font-bold"
          >
            {allBookings.map((b) => (
              <option key={b.id} value={b.id}>
                {b.date} - {b.name} ({b.client || 'Umum'})
              </option>
            ))}
          </select>
        </div>
      )}

      {/* Clock Banner */}
      <div className="bg-gradient-to-br from-indigo-900 to-slate-900 p-5 rounded-3xl border border-indigo-500/30 text-center relative overflow-hidden shadow-2xl">
        <span className="text-xs font-bold text-indigo-300 uppercase tracking-widest block mb-1">JAM PANGGUNG REAL-TIME</span>
        <h1 className="text-4xl font-black tracking-tight text-white font-mono">{timeStr || '00:00:00'}</h1>
        <p className="text-xs text-indigo-200 mt-1 font-semibold">{activeBooking.name}</p>
        <p className="text-[11px] text-slate-400 mt-0.5">{activeBooking.loc} • {activeBooking.start || 'All Day'}</p>
      </div>

      {/* Progress Bar */}
      <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800 space-y-2">
        <div className="flex items-center justify-between text-xs font-bold">
          <span className="text-slate-300">Progres Rundown Panggung</span>
          <span className="text-amber-400">{progressPercent}% ({completedCount}/{checklists.length})</span>
        </div>
        <div className="h-2.5 w-full bg-slate-800 rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-amber-500 to-indigo-500 transition-all duration-300"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      {/* Emergency Contacts */}
      {activeBooking.pic && (
        <div className="bg-slate-900 p-3.5 rounded-2xl border border-slate-800 flex items-center justify-between">
          <div>
            <span className="text-[10px] font-extrabold text-slate-400 uppercase">Kontak Darurat WO / PIC</span>
            <h4 className="text-xs font-bold text-white mt-0.5">{activeBooking.pic}</h4>
          </div>
          <a
            href={`https://wa.me/${activeBooking.pic.replace(/\D/g, '')}`}
            target="_blank"
            rel="noreferrer"
            className="py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center gap-1.5"
          >
            <Phone className="w-3.5 h-3.5" /> Hubungi WO
          </a>
        </div>
      )}

      {/* Interactive Rundown List */}
      <div className="space-y-2">
        <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-wider">Urutan Rundown Acara</h3>
        {checklists.length === 0 ? (
          <p className="text-xs text-slate-500 text-center py-6">Belum ada item rundown. Tambahkan melalui halaman detail acara.</p>
        ) : (
          checklists.map((item) => (
            <div
              key={item.id}
              onClick={() => handleToggle(item)}
              className={`p-4 rounded-2xl border flex items-center justify-between cursor-pointer active:scale-[0.98] transition-all ${
                item.isCompleted
                  ? 'bg-slate-900/60 border-slate-800 opacity-50'
                  : 'bg-slate-900 border-indigo-500/40 text-white shadow-lg'
              }`}
            >
              <div className="flex items-center gap-3.5">
                <div className={`w-7 h-7 rounded-xl flex items-center justify-center font-bold text-xs ${item.isCompleted ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 border border-slate-700 text-slate-400'}`}>
                  {item.isCompleted ? <CheckCircle2 className="w-4 h-4" /> : item.order}
                </div>
                <div>
                  <h4 className={`text-sm font-extrabold ${item.isCompleted ? 'line-through text-slate-400' : 'text-white'}`}>
                    {item.title}
                  </h4>
                  {item.time && <span className="text-xs font-bold text-amber-400">{item.time} WIB</span>}
                </div>
              </div>

              <span className={`text-[10px] font-bold px-2.5 py-1 rounded-full ${item.isCompleted ? 'bg-slate-800 text-slate-400' : 'bg-amber-500 text-slate-950 font-black'}`}>
                {item.isCompleted ? 'SELESAI' : 'LANJUT'}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
