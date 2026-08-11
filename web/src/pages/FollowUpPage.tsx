import React from 'react';
import { MessageSquare, ArrowLeft, Send, PhoneCall, DollarSign } from 'lucide-react';
import type { Booking, Client } from '../types';

interface FollowUpPageProps {
  bookings: Booking[];
  clients: Client[];
  onBack: () => void;
}

export const FollowUpPage: React.FC<FollowUpPageProps> = ({ bookings, clients, onBack }) => {
  // Pending payment bookings for follow-up
  const pendingBookings = bookings.filter(b => b.paymentStatus !== 'PAID' && b.status !== 'CANCELLED');

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
            <MessageSquare className="w-5 h-5 text-blue-600" />
            <span>Pusat Follow Up Klien & WO</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Daftar acara yang membutuhkan konfirmasi briefing atau penagihan sisa pelunasan.
          </p>
        </div>
      </div>

      {pendingBookings.length === 0 ? (
        <div className="bg-white dark:bg-slate-800 p-10 rounded-3xl border border-slate-200/80 dark:border-slate-700 text-center space-y-2">
          <MessageSquare className="w-8 h-8 text-emerald-500 mx-auto" />
          <h4 className="text-base font-extrabold text-slate-900 dark:text-white">Semua Pembayaran Lunas!</h4>
          <p className="text-xs text-slate-400">Tidak ada tagihan sisa pelunasan yang membutuhkan follow up saat ini.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {pendingBookings.map((b) => {
            const unpaidAmount = b.totalFee - (b.dpAmount || 0);
            const clientMatch = clients.find(c => c.name.toLowerCase() === b.clientName.toLowerCase());
            const phone = clientMatch?.phone || '';

            const waText = encodeURIComponent(
              `Halo Kak ${b.clientName}, salam dari MC. Mengenai acara "${b.eventTitle}" pada ${b.eventDate}, mohon konfirmasi rincian rundown dan sisa pelunasan sebesar Rp ${unpaidAmount.toLocaleString('id-ID')}. Terima kasih!`
            );
            const waUrl = phone ? `https://wa.me/${phone.replace(/[^0-9]/g, '')}?text=${waText}` : '#';

            return (
              <div
                key={b.id}
                className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4"
              >
                <div>
                  <span className="px-2.5 py-0.5 rounded-full text-[11px] font-extrabold bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400">
                    SISA TAGIHAN
                  </span>
                  <h4 className="text-base font-extrabold text-slate-900 dark:text-white mt-1">
                    {b.eventTitle || b.clientName}
                  </h4>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    Tanggal: {b.eventDate} • Klien: {b.clientName}
                  </p>
                  <p className="text-xs font-bold text-amber-600 dark:text-amber-400 mt-1">
                    Sisa Pelunasan: Rp {unpaidAmount.toLocaleString('id-ID')}
                  </p>
                </div>

                {phone ? (
                  <a
                    href={waUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs flex items-center justify-center gap-2 shadow-md cursor-pointer flex-shrink-0"
                  >
                    <Send className="w-4 h-4" />
                    <span>Follow Up WhatsApp</span>
                  </a>
                ) : (
                  <span className="text-xs text-slate-400 italic">Nomor telepon belum diisi</span>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
