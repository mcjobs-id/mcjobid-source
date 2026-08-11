import React from 'react';
import { TrendingUp, ArrowLeft, DollarSign, Calendar, PieChart } from 'lucide-react';
import type { Booking, Expense } from '../types';

interface AnalyticsPageProps {
  bookings: Booking[];
  expenses: Expense[];
  onBack: () => void;
}

export const AnalyticsPage: React.FC<AnalyticsPageProps> = ({ bookings, expenses, onBack }) => {
  const totalRevenue = bookings.reduce((sum, b) => sum + (b.totalFee || 0), 0);
  const totalExpenses = expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const netProfit = totalRevenue - totalExpenses;
  const profitMargin = totalRevenue > 0 ? ((netProfit / totalRevenue) * 100).toFixed(1) : '0';

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
            <TrendingUp className="w-5 h-5 text-emerald-600" />
            <span>Analisis Performa Bisnis MC</span>
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Laporan omset, rasio laba bersih, dan statistik performa acara.
          </p>
        </div>
      </div>

      {/* Metrics Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Omset</p>
          <p className="text-2xl font-black text-indigo-600 dark:text-indigo-400 mt-1">
            Rp {totalRevenue.toLocaleString('id-ID')}
          </p>
        </div>

        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Pengeluaran</p>
          <p className="text-2xl font-black text-rose-600 dark:text-rose-400 mt-1">
            Rp {totalExpenses.toLocaleString('id-ID')}
          </p>
        </div>

        <div className="bg-white dark:bg-slate-800 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-700 shadow-sm">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Net Profit Margin</p>
          <p className="text-2xl font-black text-emerald-600 dark:text-emerald-400 mt-1">
            {profitMargin}%
          </p>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-800 p-6 rounded-3xl border border-slate-200/80 dark:border-slate-700 shadow-sm space-y-4">
        <h3 className="text-sm font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
          <PieChart className="w-4 h-4 text-indigo-600" />
          <span>Ringkasan Laba Bersih</span>
        </h3>
        
        <div className="p-4 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-100 dark:border-indigo-900 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold text-indigo-900 dark:text-indigo-200">Estimasi Laba Bersih MC Studio</p>
            <p className="text-2xl font-black text-indigo-700 dark:text-indigo-400 mt-0.5">
              Rp {netProfit.toLocaleString('id-ID')}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
