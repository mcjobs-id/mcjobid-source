import React, { useState } from 'react';
import { Zap, ArrowLeft, Check } from 'lucide-react';

interface QuickActionSettingsPageProps {
  onBack: () => void;
}

export const QuickActionSettingsPage: React.FC<QuickActionSettingsPageProps> = ({ onBack }) => {
  const [enabled, setEnabled] = useState(true);
  const [qaJob, setQaJob] = useState(true);
  const [qaClient, setQaClient] = useState(true);
  const [qaPayment, setQaPayment] = useState(true);
  const [qaExpense, setQaExpense] = useState(true);
  const [qaInvoice, setQaInvoice] = useState(true);

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
            <Zap className="w-5 h-5 text-amber-500" />
            <span>Pengaturan Pintasan FAB Melayang</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Kustomisasi tombol aksi cepat yang muncul di sudut layar dasbor.
          </p>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200/80 dark:border-slate-700 shadow-sm p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h4 className="text-sm font-extrabold text-slate-900 dark:text-white">Aktifkan Tombol FAB</h4>
            <p className="text-xs text-slate-400">Tampilkan tombol melayang di beranda</p>
          </div>
          <input
            type="checkbox"
            checked={enabled}
            onChange={(e) => setEnabled(e.target.checked)}
            className="w-5 h-5 accent-indigo-600 rounded cursor-pointer"
          />
        </div>

        <div className="h-[1px] bg-slate-100 dark:bg-slate-700" />

        <div className="space-y-3">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Pilih Pintasan Aktif</p>

          {[
            { label: 'Tambah Job Acara', state: qaJob, setState: setQaJob },
            { label: 'Tambah Klien Baru', state: qaClient, setState: setQaClient },
            { label: 'Catat Pemasukan / DP', state: qaPayment, setState: setQaPayment },
            { label: 'Catat Pengeluaran', state: qaExpense, setState: setQaExpense },
            { label: 'Buat Invoice PDF', state: qaInvoice, setState: setQaInvoice },
          ].map((item, idx) => (
            <label key={idx} className="flex items-center justify-between py-1 cursor-pointer">
              <span className="text-xs font-bold text-slate-700 dark:text-slate-200">{item.label}</span>
              <input
                type="checkbox"
                checked={item.state}
                onChange={(e) => item.setState(e.target.checked)}
                className="w-4 h-4 accent-indigo-600 rounded cursor-pointer"
              />
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};
