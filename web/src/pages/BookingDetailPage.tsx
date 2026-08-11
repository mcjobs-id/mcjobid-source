import React, { useState, useEffect } from 'react';
import { ArrowLeft, Calendar, MapPin, Zap, FileText, CheckSquare, Trash2, Check, Plus } from 'lucide-react';
import type { Booking, ChecklistItem } from '../types';
import { subscribeChecklists, saveChecklistItem, deleteChecklistItem } from '../services/firebaseService';

interface BookingDetailPageProps {
  booking: Booking;
  onBack: () => void;
  onOpenDayMode: (booking: Booking) => void;
  onOpenInvoice: (booking: Booking) => void;
  onDeleteBooking: (bookingId: string) => Promise<void>;
  onUpdateBooking: (booking: Booking) => Promise<void>;
}

export const BookingDetailPage: React.FC<BookingDetailPageProps> = ({
  booking,
  onBack,
  onOpenDayMode,
  onOpenInvoice,
  onDeleteBooking,
  onUpdateBooking
}) => {
  const [checklists, setChecklists] = useState<ChecklistItem[]>([]);
  const [newChecklistTitle, setNewChecklistTitle] = useState('');
  const [newChecklistTime, setNewChecklistTime] = useState('');
  const [showAddChecklist, setShowAddChecklist] = useState(false);

  useEffect(() => {
    const unsub = subscribeChecklists(booking.ownerId || '', booking.id, (items) => {
      setChecklists(items);
    });
    return () => unsub();
  }, [booking.id, booking.ownerId]);

  const handleToggleChecklist = async (item: ChecklistItem) => {
    await saveChecklistItem({
      ...item,
      isCompleted: !item.isCompleted
    });
  };

  const handleAddChecklist = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChecklistTitle) return;
    const newItem: ChecklistItem = {
      id: Date.now().toString(),
      ownerId: booking.ownerId,
      bookingId: booking.id,
      title: newChecklistTitle,
      time: newChecklistTime,
      isCompleted: false,
      order: checklists.length + 1,
      createdAt: new Date().toISOString()
    };
    await saveChecklistItem(newItem);
    setNewChecklistTitle('');
    setNewChecklistTime('');
    setShowAddChecklist(false);
  };

  const remainingPay = (booking.fee || 0) - (booking.dp || 0);
  const isPaidFull = remainingPay <= 0;

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 hover:bg-slate-200 font-bold text-xs flex items-center gap-1"
        >
          <ArrowLeft className="w-4 h-4" /> Kembali
        </button>

        <div className="flex items-center gap-2">
          <button
            onClick={() => onDeleteBooking(booking.id)}
            className="p-2 rounded-xl bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 hover:bg-rose-100"
            title="Hapus Acara"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Main Card Header */}
      <div className="card bg-gradient-to-br from-slate-900 to-indigo-950 text-white border-none p-5 relative overflow-hidden shadow-xl">
        <div className="flex items-start justify-between">
          <div>
            <span className="text-[10px] font-extrabold uppercase px-2.5 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-400/30">
              {booking.category || 'Wedding'}
            </span>
            <h2 className="text-xl font-extrabold mt-2 leading-tight">{booking.name}</h2>
            <p className="text-xs text-indigo-200 mt-1 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5" />
              {new Date(booking.date).toLocaleDateString('id-ID', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
            </p>
          </div>

          <span className={`text-[10px] font-bold px-3 py-1 rounded-full ${isPaidFull ? 'badge-paid' : booking.dp > 0 ? 'badge-partial' : 'badge-unpaid'}`}>
            {isPaidFull ? 'LUNAS' : booking.dp > 0 ? 'DP MASUK' : 'BELUM DP'}
          </span>
        </div>

        {/* Action Buttons */}
        <div className="grid grid-cols-2 gap-2.5 mt-5 pt-4 border-t border-slate-800">
          <button
            onClick={() => onOpenDayMode(booking)}
            className="py-2.5 px-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold text-xs shadow-md flex items-center justify-center gap-1.5"
          >
            <Zap className="w-4 h-4 fill-slate-950" /> Mode Hari H
          </button>

          <button
            onClick={() => onOpenInvoice(booking)}
            className="py-2.5 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-extrabold text-xs shadow-md flex items-center justify-center gap-1.5"
          >
            <FileText className="w-4 h-4" /> Cetak Invoice
          </button>
        </div>
      </div>

      {/* Details Grid */}
      <div className="card space-y-3 text-xs">
        <h3 className="font-extrabold text-slate-900 dark:text-white text-sm border-b border-slate-100 dark:border-slate-800 pb-2">
          Informasi Detail Acara
        </h3>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <span className="text-[11px] text-slate-400 block">Klien / Organizer</span>
            <span className="font-bold text-slate-800 dark:text-slate-200">{booking.client || '-'}</span>
          </div>

          <div>
            <span className="text-[11px] text-slate-400 block">Kontak PIC / WO</span>
            <span className="font-bold text-indigo-600 dark:text-indigo-400">{booking.pic || '-'}</span>
          </div>

          <div>
            <span className="text-[11px] text-slate-400 block">Waktu / Durasi</span>
            <span className="font-bold text-slate-800 dark:text-slate-200">
              {booking.start ? `${booking.start} - ${booking.end || 'Selesai'}` : 'Sepanjang Hari'}
            </span>
          </div>

          <div>
            <span className="text-[11px] text-slate-400 block">Format MC</span>
            <span className="font-bold text-slate-800 dark:text-slate-200">{booking.mcType || 'Single'}</span>
          </div>

          <div className="col-span-2">
            <span className="text-[11px] text-slate-400 block">Lokasi & Alamat Venue</span>
            <span className="font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1 mt-0.5">
              <MapPin className="w-3.5 h-3.5 text-rose-500 shrink-0" />
              {booking.loc || 'Belum diisi'} {booking.address ? `(${booking.address})` : ''}
            </span>
          </div>

          {booking.dresscode && (
            <div>
              <span className="text-[11px] text-slate-400 block">Dresscode</span>
              <span className="font-bold text-slate-800 dark:text-slate-200">{booking.dresscode}</span>
            </div>
          )}

          {booking.theme && (
            <div>
              <span className="text-[11px] text-slate-400 block">Tema Acara</span>
              <span className="font-bold text-slate-800 dark:text-slate-200">{booking.theme}</span>
            </div>
          )}
        </div>

        {booking.note && (
          <div className="mt-2 pt-2 border-t border-slate-100 dark:border-slate-800">
            <span className="text-[11px] text-slate-400 block mb-0.5">Catatan Khusus</span>
            <p className="bg-slate-50 dark:bg-slate-800 p-2.5 rounded-xl text-slate-700 dark:text-slate-300 italic">
              "{booking.note}"
            </p>
          </div>
        )}
      </div>

      {/* Financial Status Card */}
      <div className="card space-y-2.5 text-xs">
        <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">Rincian Pembayaran</h3>

        <div className="space-y-1.5 pt-1">
          <div className="flex justify-between">
            <span className="text-slate-500">Total Honor MC:</span>
            <span className="font-extrabold text-slate-900 dark:text-white">{formatCurrency(booking.fee)}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-500">DP Terbayar:</span>
            <span className="font-bold text-emerald-600">{formatCurrency(booking.dp)}</span>
          </div>

          <div className="flex justify-between pt-1 border-t border-slate-100 dark:border-slate-800 text-sm">
            <span className="font-bold text-slate-700 dark:text-slate-300">Sisa Pelunasan:</span>
            <span className={`font-black ${isPaidFull ? 'text-emerald-500' : 'text-rose-500'}`}>
              {formatCurrency(remainingPay)}
            </span>
          </div>
        </div>
      </div>

      {/* Rundown & Checklist Tracker */}
      <div className="card space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-extrabold text-slate-900 dark:text-white text-sm flex items-center gap-1.5">
            <CheckSquare className="w-4 h-4 text-indigo-600" /> Rundown & Checklist ({checklists.length})
          </h3>
          <button
            onClick={() => setShowAddChecklist(!showAddChecklist)}
            className="text-xs font-bold text-indigo-600 dark:text-indigo-400 flex items-center gap-1 hover:underline"
          >
            <Plus className="w-3.5 h-3.5" /> Tambah Item
          </button>
        </div>

        {showAddChecklist && (
          <form onSubmit={handleAddChecklist} className="p-3 bg-slate-50 dark:bg-slate-800 rounded-xl space-y-2 text-xs">
            <input
              type="text"
              required
              placeholder="Contoh: Gladi Bersih / Sambutan Panitia"
              value={newChecklistTitle}
              onChange={(e) => setNewChecklistTitle(e.target.value)}
              className="input-field py-2 text-xs"
            />
            <div className="flex items-center gap-2">
              <input
                type="time"
                placeholder="Waktu"
                value={newChecklistTime}
                onChange={(e) => setNewChecklistTime(e.target.value)}
                className="input-field py-1.5 text-xs w-32"
              />
              <button type="submit" className="btn-primary py-1.5 px-4 text-xs font-bold flex-1">
                Simpan
              </button>
            </div>
          </form>
        )}

        {checklists.length === 0 ? (
          <p className="text-xs text-slate-400 text-center py-4">Belum ada item rundown diset. Klik "+ Tambah Item".</p>
        ) : (
          <div className="space-y-2">
            {checklists.map((item) => (
              <div
                key={item.id}
                onClick={() => handleToggleChecklist(item)}
                className={`p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all ${
                  item.isCompleted
                    ? 'bg-slate-50 dark:bg-slate-800/40 border-slate-200 dark:border-slate-800 opacity-60'
                    : 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-5 h-5 rounded-md flex items-center justify-center border transition-colors ${item.isCompleted ? 'bg-indigo-600 border-indigo-600 text-white' : 'border-slate-300 dark:border-slate-600'}`}>
                    {item.isCompleted && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                  </div>
                  <div>
                    <h4 className={`text-xs font-bold ${item.isCompleted ? 'line-through text-slate-400' : 'text-slate-800 dark:text-slate-200'}`}>
                      {item.title}
                    </h4>
                    {item.time && <span className="text-[10px] text-indigo-500 font-semibold">{item.time} WIB</span>}
                  </div>
                </div>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteChecklistItem(item.id);
                  }}
                  className="text-slate-400 hover:text-rose-500 p-1"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
