import React, { useState } from 'react';
import { Wallet, TrendingUp, TrendingDown, Plus, Trash2 } from 'lucide-react';
import type { Booking, Expense } from '../types';
import { Modal } from '../components/Modal';

interface FinancePageProps {
  bookings: Booking[];
  expenses: Expense[];
  onSaveExpense: (exp: Expense) => Promise<void>;
  onDeleteExpense: (id: string) => Promise<void>;
}

export const FinancePage: React.FC<FinancePageProps> = ({
  bookings,
  expenses,
  onSaveExpense,
  onDeleteExpense
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Transport / Bensin');
  const [amount, setAmount] = useState(0);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [saving, setSaving] = useState(false);

  // Financial calculations
  let totalHonor = 0;
  let totalReceived = 0;
  bookings.forEach((b) => {
    totalHonor += b.fee || 0;
    totalReceived += b.dp || 0;
  });

  let totalExpense = 0;
  expenses.forEach((e) => {
    totalExpense += e.amount || 0;
  });

  const netIncome = totalReceived - totalExpense;

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  const handleAddExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const exp: Expense = {
        id: Date.now().toString(),
        ownerId: '',
        title,
        category,
        amount: Number(amount),
        date,
        createdAt: new Date().toISOString()
      };
      await onSaveExpense(exp);
      setIsModalOpen(false);
      setTitle('');
      setAmount(0);
    } catch (err) {
      console.error('Error saving expense:', err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-extrabold text-slate-900 dark:text-white">Laporan Keuangan MC</h2>
          <p className="text-xs text-slate-400">Ringkasan Pemasukan & Pengeluaran Operasional</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="btn-secondary py-2 px-3 text-xs font-bold shadow-sm"
        >
          <Plus className="w-4 h-4" /> Catat Pengeluaran
        </button>
      </div>

      {/* Main Financial Balance Cards */}
      <div className="grid grid-cols-2 gap-3">
        <div className="card bg-gradient-to-br from-emerald-600 to-emerald-700 text-white border-none p-4">
          <div className="flex items-center gap-1.5 text-emerald-100 text-[11px] font-bold uppercase">
            <TrendingUp className="w-4 h-4 text-emerald-200" /> Total Diterima (DP)
          </div>
          <h3 className="text-lg font-black mt-2 leading-tight">{formatCurrency(totalReceived)}</h3>
          <span className="text-[10px] text-emerald-200 block mt-1">Dari total honor {formatCurrency(totalHonor)}</span>
        </div>

        <div className="card bg-gradient-to-br from-rose-600 to-rose-700 text-white border-none p-4">
          <div className="flex items-center gap-1.5 text-rose-100 text-[11px] font-bold uppercase">
            <TrendingDown className="w-4 h-4 text-rose-200" /> Total Pengeluaran
          </div>
          <h3 className="text-lg font-black mt-2 leading-tight">{formatCurrency(totalExpense)}</h3>
          <span className="text-[10px] text-rose-200 block mt-1">Transport, Kostum, & Operasional</span>
        </div>

        <div className="col-span-2 card bg-slate-900 text-white p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-400 font-bold uppercase block">BERSIH (NET INCOME)</span>
            <h3 className="text-xl font-black text-emerald-400 mt-0.5">{formatCurrency(netIncome)}</h3>
          </div>
          <div className="w-10 h-10 rounded-2xl bg-indigo-600/30 text-indigo-400 flex items-center justify-center font-bold">
            <Wallet className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Expense History List */}
      <div className="space-y-2.5">
        <h3 className="text-xs font-extrabold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
          Riwayat Pengeluaran Operasional ({expenses.length})
        </h3>

        {expenses.length === 0 ? (
          <div className="card text-center py-8 text-xs text-slate-400">
            Belum ada catatan pengeluaran. Klik "Catat Pengeluaran" di atas.
          </div>
        ) : (
          expenses.map((exp) => (
            <div
              key={exp.id}
              className="card py-3 px-4 flex items-center justify-between hover:border-slate-300 dark:hover:border-slate-700"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-rose-50 dark:bg-rose-950/50 text-rose-600 dark:text-rose-400 flex items-center justify-center font-bold text-xs">
                  <TrendingDown className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-900 dark:text-white leading-tight">{exp.title}</h4>
                  <span className="text-[10px] text-slate-400">{exp.category} • {exp.date}</span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <span className="text-xs font-extrabold text-rose-600 dark:text-rose-400">
                  -{formatCurrency(exp.amount)}
                </span>
                <button
                  onClick={() => onDeleteExpense(exp.id)}
                  className="text-slate-400 hover:text-rose-500 p-1"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* ADD EXPENSE MODAL */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Catat Pengeluaran Operasional">
        <form onSubmit={handleAddExpense} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Keterangan / Judul *</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Contoh: Bensin Tol Jakarta-Bandung / Laundry Jas"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Kategori</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="input-field text-xs py-2.5"
              >
                <option value="Transport / Bensin">Transport / Bensin</option>
                <option value="Kostum / Jas">Kostum / Wardrobe</option>
                <option value="Makeup / Grooming">Makeup / Grooming</option>
                <option value="Konsumsi / Makan">Konsumsi / Makan</option>
                <option value="Equipment / Mic">Equipment / Sound</option>
                <option value="Lainnya">Lainnya</option>
              </select>
            </div>

            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nominal (Rp) *</label>
              <input
                type="number"
                required
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
                placeholder="0"
                className="input-field text-xs py-2.5 font-bold text-rose-600"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Tanggal</label>
            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="pt-2">
            <button type="submit" disabled={saving} className="btn-primary w-full py-3 text-xs">
              {saving ? 'Menyimpan...' : 'Simpan Pengeluaran'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
