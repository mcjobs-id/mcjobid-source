import React, { useRef, useState } from 'react';
import { ArrowLeft, Download, Share2, Sparkles } from 'lucide-react';
import type { Booking } from '../types';
import { useAuth } from '../context/AuthContext';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

interface InvoicePageProps {
  booking: Booking;
  onBack: () => void;
}

export const InvoicePage: React.FC<InvoicePageProps> = ({ booking, onBack }) => {
  const { userProfile } = useAuth();
  const invoiceRef = useRef<HTMLDivElement>(null);
  const [downloading, setDownloading] = useState(false);

  const invoiceNo = `INV-${new Date().getFullYear()}${(new Date().getMonth() + 1).toString().padStart(2, '0')}-${booking.id.slice(-4)}`;
  const remainingPay = (booking.fee || 0) - (booking.dp || 0);
  const isPaidFull = remainingPay <= 0;

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  const handleDownloadPdf = async () => {
    if (!invoiceRef.current) return;
    setDownloading(true);
    try {
      const canvas = await html2canvas(invoiceRef.current, { scale: 2, useCORS: true });
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
      const imgWidth = 210;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight);
      pdf.save(`Invoice_${booking.client || 'Klien'}_${booking.name}.pdf`);
    } catch (err) {
      console.error('PDF export error:', err);
    } finally {
      setDownloading(false);
    }
  };

  const handleShareWhatsApp = () => {
    const text = `Halo Kak ${booking.client || ''},\n\nBerikut terlampir rincian Invoice untuk acara *${booking.name}*:\n` +
      `📌 *No Invoice*: ${invoiceNo}\n` +
      `🗓 *Tanggal Event*: ${booking.date}\n` +
      `💰 *Total Honor*: ${formatCurrency(booking.fee)}\n` +
      `✅ *DP Terbayar*: ${formatCurrency(booking.dp)}\n` +
      `🔴 *Sisa Pelunasan*: ${formatCurrency(remainingPay)}\n\n` +
      `Pembayaran dapat ditransfer ke:\n` +
      `Bank: ${userProfile?.bankName || 'BCA'}\n` +
      `No Rek: ${userProfile?.bankAccount || '-'}\n` +
      `A.n: ${userProfile?.bankHolder || userProfile?.name || '-'}\n\n` +
      `Terima kasih! 🙏`;

    window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, '_blank');
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Top Action Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 hover:bg-slate-200 font-bold text-xs flex items-center gap-1"
        >
          <ArrowLeft className="w-4 h-4" /> Kembali
        </button>

        <div className="flex items-center gap-2">
          <button
            onClick={handleShareWhatsApp}
            className="py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center gap-1.5 shadow-md"
          >
            <Share2 className="w-4 h-4" /> WhatsApp
          </button>

          <button
            onClick={handleDownloadPdf}
            disabled={downloading}
            className="py-2 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs flex items-center gap-1.5 shadow-md"
          >
            <Download className="w-4 h-4" /> {downloading ? 'Memproses...' : 'Unduh PDF'}
          </button>
        </div>
      </div>

      {/* Invoice Printable View */}
      <div
        ref={invoiceRef}
        className="bg-white text-slate-900 p-6 rounded-3xl border border-slate-200 shadow-xl space-y-5 text-xs font-sans"
      >
        {/* Header Branding */}
        <div className="flex items-start justify-between border-b border-slate-200 pb-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <div className="w-8 h-8 rounded-xl bg-indigo-600 text-white flex items-center justify-center font-bold">
                <Sparkles className="w-4 h-4" />
              </div>
              <h2 className="text-lg font-extrabold tracking-tight text-slate-900">
                {userProfile?.stageName || userProfile?.name || 'MC PROFESSIONAL'}
              </h2>
            </div>
            <p className="text-slate-500 text-[11px]">{userProfile?.email} • {userProfile?.phone || '-'}</p>
            {userProfile?.instagram && <p className="text-indigo-600 text-[11px]">IG: {userProfile.instagram}</p>}
          </div>

          <div className="text-right">
            <span className="text-xs font-black uppercase text-indigo-600 tracking-wider block">INVOICE</span>
            <span className="font-extrabold text-sm text-slate-800">{invoiceNo}</span>
            <span className="text-[11px] text-slate-400 block mt-0.5">Tgl: {new Date().toLocaleDateString('id-ID')}</span>
          </div>
        </div>

        {/* Billed To & Event Details */}
        <div className="grid grid-cols-2 gap-4 bg-slate-50 p-4 rounded-2xl border border-slate-100">
          <div>
            <span className="text-[10px] font-bold text-slate-400 uppercase block mb-1">DITUJUKAN KEPADA:</span>
            <h4 className="font-extrabold text-sm text-slate-900">{booking.client || 'Klien'}</h4>
            <p className="text-slate-500">{booking.pic ? `PIC: ${booking.pic}` : ''}</p>
          </div>

          <div>
            <span className="text-[10px] font-bold text-slate-400 uppercase block mb-1">DETAIL EVENT:</span>
            <h4 className="font-bold text-slate-900">{booking.name}</h4>
            <p className="text-slate-500">{booking.date} ({booking.start || 'TBD'})</p>
            <p className="text-slate-500">{booking.loc || '-'}</p>
          </div>
        </div>

        {/* Itemized Table */}
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b-2 border-slate-200 text-slate-400 text-[10px] font-extrabold uppercase">
              <th className="py-2">Deskripsi Layanan</th>
              <th className="py-2 text-right">Jumlah</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            <tr>
              <td className="py-3">
                <span className="font-bold text-slate-800 block text-xs">Jasa Master of Ceremonies (MC) - {booking.category || 'Wedding'}</span>
                <span className="text-[11px] text-slate-400">Format {booking.mcType || 'Single'} • Durasi Event</span>
              </td>
              <td className="py-3 text-right font-extrabold text-slate-900 text-xs">
                {formatCurrency(booking.fee)}
              </td>
            </tr>
          </tbody>
        </table>

        {/* Calculation Summary */}
        <div className="border-t-2 border-slate-200 pt-3 space-y-1.5 text-right">
          <div className="flex justify-between text-xs">
            <span className="text-slate-500">Subtotal Honor:</span>
            <span className="font-bold">{formatCurrency(booking.fee)}</span>
          </div>

          <div className="flex justify-between text-xs">
            <span className="text-slate-500">DP Terbayar:</span>
            <span className="font-bold text-emerald-600">({formatCurrency(booking.dp)})</span>
          </div>

          <div className="flex justify-between text-sm font-black pt-2 border-t border-slate-100">
            <span className="text-slate-900">Total Sisa Tagihan:</span>
            <span className={isPaidFull ? 'text-emerald-600' : 'text-rose-600'}>
              {formatCurrency(remainingPay)}
            </span>
          </div>
        </div>

        {/* Payment Info & Signature */}
        <div className="pt-4 border-t border-slate-200 grid grid-cols-2 gap-4 items-end">
          <div className="bg-indigo-50/60 p-3 rounded-2xl border border-indigo-100">
            <span className="text-[10px] font-extrabold text-indigo-900 uppercase block mb-1">REKENING PEMBAYARAN:</span>
            <p className="font-bold text-indigo-950 text-xs">{userProfile?.bankName || 'BCA'}</p>
            <p className="font-mono font-extrabold text-indigo-700 text-sm">{userProfile?.bankAccount || '1234567890'}</p>
            <p className="text-[11px] text-indigo-900">A.n {userProfile?.bankHolder || userProfile?.name || '-'}</p>
          </div>

          <div className="text-center">
            <p className="text-[10px] text-slate-400 mb-8">Hormat Kami,</p>
            <p className="font-bold text-slate-900 border-t border-slate-300 pt-1 inline-block px-4">
              {userProfile?.stageName || userProfile?.name || 'MC Talent'}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
