import React, { useState, useMemo } from 'react';
import {
  Wallet, TrendingUp, TrendingDown, Plus, Trash2, Calendar,
  FileText, CreditCard, ChevronDown, BarChart2, CheckCircle2
} from 'lucide-react';
import type { Booking, Expense, Payment } from '../types';
import { getBookingFee, getBookingDate, normalizeStatus } from '../types';
import { Modal } from '../components/Modal';

interface FinancePageProps {
  bookings: Booking[];
  expenses: Expense[];
  payments: Payment[];
  onSaveExpense: (exp: Expense) => Promise<void>;
  onDeleteExpense: (id: string) => Promise<void>;
  onSavePayment?: (p: Payment) => Promise<void>;
  onDeletePayment?: (id: string) => Promise<void>;
  currentUserId?: string;
}

const EXPENSE_CATEGORIES = [
  'Transport / Bensin', 'Tol / Parkir', 'Kostum / Wardrobe', 'Makeup / Grooming',
  'Konsumsi / Makan', 'Equipment / Mic', 'Laundry', 'Koordinasi / Tips', 'Lainnya'
];

function formatCurrency(val: number) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(Math.max(0, val));
}

function formatDate(d: string) {
  if (!d) return '-';
  try {
    return new Date(d + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
  } catch { return d; }
}

type TabType = 'summary' | 'expenses' | 'payments';

export const FinancePage: React.FC<FinancePageProps> = ({
  bookings, expenses, payments, onSaveExpense, onDeleteExpense
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('summary');
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);

  // Expense form state
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Transport / Bensin');
  const [amount, setAmount] = useState<number | ''>('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [saving, setSaving] = useState(false);

  // ── Financial Calculations (mirrors Android CalculateFinancialSummaryUseCase) ──

  const calculations = useMemo(() => {
    const activeBookings = bookings.filter(b => normalizeStatus(b.status as string) !== 'cancelled');

    // Total Honor (all active bookings)
    const totalHonor = activeBookings.reduce((s, b) => s + getBookingFee(b), 0);

    // Calculate paid per booking using payment records (fallback to dp field)
    const paidByBooking: Record<string, number> = {};
    payments.forEach(p => {
      paidByBooking[p.bookingId] = (paidByBooking[p.bookingId] || 0) + p.amount;
    });

    let totalPaid = 0;
    let totalOutstanding = 0;
    activeBookings.forEach(b => {
      const paid = paidByBooking[b.id] !== undefined
        ? paidByBooking[b.id]
        : (b.dp ?? 0);
      totalPaid += paid;
      totalOutstanding += Math.max(0, getBookingFee(b) - paid);
    });

    // Total Expenses
    const totalExpenses = expenses.reduce((s, e) => s + (e.amount || 0), 0);

    // Net Income = total paid (cash received) - total expenses
    const netIncome = totalPaid - totalExpenses;

    // Completed job count
    const completedCount = bookings.filter(b => normalizeStatus(b.status as string) === 'completed').length;
    const upcomingCount = bookings.filter(b => {
      const s = normalizeStatus(b.status as string);
      return s === 'confirmed' || s === 'upcoming' || s === 'active';
    }).length;

    return { totalHonor, totalPaid, totalOutstanding, totalExpenses, netIncome, completedCount, upcomingCount, activeBookings };
  }, [bookings, expenses, payments]);

  const handleAddExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!amount) return;
    setSaving(true);
    try {
      const exp: Expense = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUserId || '',
        title: title || category,
        category,
        amount: Number(amount),
        date,
        createdAt: new Date().toISOString()
      };
      await onSaveExpense(exp);
      setIsExpenseModalOpen(false);
      setTitle('');
      setAmount('');
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const tabs: { key: TabType; label: string; count?: number }[] = [
    { key: 'summary', label: 'Ringkasan' },
    { key: 'expenses', label: 'Pengeluaran', count: expenses.length },
    { key: 'payments', label: 'Pembayaran', count: payments.length },
  ];

  return (
    <div className="animate-fade-in" style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '16px' }}>

      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Wallet size={20} color="var(--primary)" />
            Keuangan & Cashflow MC
          </h1>
          <p className="page-subtitle">Ringkasan pemasukan honorarium, pengeluaran operasional, dan piutang.</p>
        </div>
        <button onClick={() => setIsExpenseModalOpen(true)} className="btn btn-primary" style={{ gap: '6px' }}>
          <Plus size={15} /> Catat Pengeluaran
        </button>
      </div>

      {/* Hero — Net Income */}
      <div className="card" style={{ padding: '24px', marginBottom: '16px', background: 'var(--text-1)', border: 'none', position: 'relative', overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: '-50%', right: '-10%', width: '300px', height: '300px', background: 'radial-gradient(circle, rgba(79,70,229,0.3) 0%, transparent 70%)', borderRadius: '50%', pointerEvents: 'none' }} />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <span style={{ fontSize: '11px', fontWeight: '700', color: 'rgba(255,255,255,0.6)', textTransform: 'uppercase', letterSpacing: '0.08em', display: 'block', marginBottom: '6px' }}>NET INCOME (KAS BERSIH)</span>
          <h2 style={{ fontSize: 'clamp(28px,4vw,36px)', fontWeight: '800', color: 'white', letterSpacing: '-0.03em', fontVariantNumeric: 'tabular-nums', lineHeight: '1', marginBottom: '12px' }}>
            {formatCurrency(calculations.netIncome)}
          </h2>
          <p style={{ fontSize: '12px', color: 'rgba(255,255,255,0.55)' }}>
            Uang diterima {formatCurrency(calculations.totalPaid)} − Pengeluaran {formatCurrency(calculations.totalExpenses)}
          </p>
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', marginBottom: '24px' }}>
        <KpiCard
          label="Total Kontrak"
          value={formatCurrency(calculations.totalHonor)}
          sub={`${calculations.activeBookings.length} job aktif`}
          color="var(--primary)"
          icon={<BarChart2 size={16} color="var(--primary)" />}
        />
        <KpiCard
          label="Total Terbayar"
          value={formatCurrency(calculations.totalPaid)}
          sub="Kas masuk"
          color="var(--success)"
          icon={<TrendingUp size={16} color="var(--success)" />}
        />
        <KpiCard
          label="Sisa Piutang"
          value={formatCurrency(calculations.totalOutstanding)}
          sub={calculations.totalOutstanding > 0 ? 'Belum dilunasi' : 'Semua lunas ✓'}
          color={calculations.totalOutstanding > 0 ? 'var(--warning)' : 'var(--success)'}
          icon={<CreditCard size={16} color={calculations.totalOutstanding > 0 ? 'var(--warning)' : 'var(--success)'} />}
        />
        <KpiCard
          label="Total Pengeluaran"
          value={formatCurrency(calculations.totalExpenses)}
          sub="Operasional"
          color="var(--error)"
          icon={<TrendingDown size={16} color="var(--error)" />}
        />
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '4px', marginBottom: '20px', background: 'var(--bg-surface-2)', padding: '4px', borderRadius: '12px', width: 'fit-content' }}>
        {tabs.map(t => (
          <button
            key={t.key}
            onClick={() => setActiveTab(t.key)}
            style={{
              padding: '7px 16px', borderRadius: '9px', border: 'none', cursor: 'pointer',
              fontSize: '13px', fontWeight: '600', transition: 'all 0.15s',
              background: activeTab === t.key ? 'var(--bg-surface)' : 'transparent',
              color: activeTab === t.key ? 'var(--text-1)' : 'var(--text-4)',
              boxShadow: activeTab === t.key ? '0 1px 4px rgba(0,0,0,0.08)' : 'none',
              display: 'flex', alignItems: 'center', gap: '6px'
            }}
          >
            {t.label}
            {t.count !== undefined && t.count > 0 && (
              <span style={{ fontSize: '10px', fontWeight: '700', color: activeTab === t.key ? 'var(--primary)' : 'var(--text-4)', background: activeTab === t.key ? 'var(--primary-light)' : 'transparent', padding: '0 6px', borderRadius: '8px' }}>
                {t.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Tab: Summary */}
      {activeTab === 'summary' && (
        <div>
          <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-2)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '7px' }}>
            <FileText size={14} color="var(--primary)" /> Ringkasan Per Job
          </h3>
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            {calculations.activeBookings.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon"><Wallet size={22} /></div>
                <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-1)' }}>Belum ada job aktif</p>
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Nama Acara</th>
                    <th style={{ textAlign: 'right' }}>Total Honor</th>
                    <th style={{ textAlign: 'right' }}>Terbayar</th>
                    <th style={{ textAlign: 'right' }}>Piutang</th>
                  </tr>
                </thead>
                <tbody>
                  {calculations.activeBookings.map(b => {
                    const fee = getBookingFee(b);
                    const bookingPayments = payments.filter(p => p.bookingId === b.id);
                    const paid = bookingPayments.length > 0
                      ? bookingPayments.reduce((s, p) => s + p.amount, 0)
                      : (b.dp ?? 0);
                    const outstanding = Math.max(0, fee - paid);
                    const isLunas = outstanding <= 0 && fee > 0;
                    return (
                      <tr key={b.id}>
                        <td>
                          <p style={{ fontWeight: '600', fontSize: '13px' }}>{b.name || b.eventTitle || b.clientName || 'Acara MC'}</p>
                          <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{formatDate(getBookingDate(b))} · {b.category}</p>
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: '700', fontSize: '13px' }}>{formatCurrency(fee)}</td>
                        <td style={{ textAlign: 'right', fontWeight: '600', fontSize: '13px', color: 'var(--success)' }}>{formatCurrency(paid)}</td>
                        <td style={{ textAlign: 'right' }}>
                          {isLunas
                            ? <span style={{ fontSize: '11px', fontWeight: '700', color: 'var(--success)', background: 'var(--success-light)', padding: '2px 8px', borderRadius: '8px' }}>LUNAS</span>
                            : <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--warning)' }}>{formatCurrency(outstanding)}</span>
                          }
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Tab: Expenses */}
      {activeTab === 'expenses' && (
        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
            <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-2)', display: 'flex', alignItems: 'center', gap: '7px' }}>
              <TrendingDown size={14} color="var(--error)" /> Riwayat Pengeluaran
            </h3>
            <button onClick={() => setIsExpenseModalOpen(true)} className="btn btn-secondary btn-sm" style={{ gap: '5px' }}>
              <Plus size={13} /> Tambah
            </button>
          </div>
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            {expenses.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon"><TrendingDown size={22} /></div>
                <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-1)', marginBottom: '4px' }}>Belum ada pengeluaran</p>
                <p style={{ fontSize: '12px', color: 'var(--text-3)', maxWidth: '260px', margin: '0 auto 16px' }}>Catat pengeluaran operasional seperti bensin, kostum, atau makeup.</p>
                <button onClick={() => setIsExpenseModalOpen(true)} className="btn btn-secondary btn-sm" style={{ gap: '4px' }}>
                  <Plus size={13} /> Catat Pengeluaran
                </button>
              </div>
            ) : (
              expenses.map((exp, i) => (
                <div key={exp.id} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '14px 16px', borderBottom: i < expenses.length - 1 ? '1px solid var(--border)' : 'none' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'var(--error-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <TrendingDown size={16} color="var(--error)" />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <p style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-1)', marginBottom: '2px' }}>{exp.title}</p>
                    <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{exp.category} · {formatDate(exp.date)}</p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--text-1)', fontVariantNumeric: 'tabular-nums' }}>- {formatCurrency(exp.amount)}</span>
                    <button onClick={() => onDeleteExpense(exp.id)} className="btn btn-ghost btn-sm" style={{ width: '30px', height: '30px', padding: 0, color: 'var(--text-4)' }}>
                      <Trash2 size={13} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Tab: Payments */}
      {activeTab === 'payments' && (
        <div>
          <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-2)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '7px' }}>
            <CreditCard size={14} color="var(--success)" /> Riwayat Pembayaran
          </h3>
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            {payments.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon"><CreditCard size={22} /></div>
                <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-1)', marginBottom: '4px' }}>Belum ada rekam pembayaran</p>
                <p style={{ fontSize: '12px', color: 'var(--text-3)', maxWidth: '260px', margin: '0 auto' }}>Catat pembayaran melalui halaman Detail Job untuk setiap booking.</p>
              </div>
            ) : (
              payments.map((p, i) => {
                const booking = bookings.find(b => b.id === p.bookingId);
                return (
                  <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '14px 16px', borderBottom: i < payments.length - 1 ? '1px solid var(--border)' : 'none' }}>
                    <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'var(--success-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                      <CheckCircle2 size={16} color="var(--success)" />
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-1)', marginBottom: '2px' }}>
                        {booking ? (booking.name || booking.eventTitle || booking.clientName || 'Job') : p.bookingId.slice(0, 8)}
                      </p>
                      <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{p.paymentMethod} · {formatDate(p.paymentDate)} {p.note ? `· ${p.note}` : ''}</p>
                    </div>
                    <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--success)', fontVariantNumeric: 'tabular-nums' }}>{formatCurrency(p.amount)}</span>
                  </div>
                );
              })
            )}
          </div>
        </div>
      )}

      {/* Add Expense Modal */}
      <Modal isOpen={isExpenseModalOpen} onClose={() => setIsExpenseModalOpen(false)} title="Catat Pengeluaran Operasional">
        <form onSubmit={handleAddExpense} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label className="input-label">Keterangan / Judul *</label>
            <input type="text" value={title} onChange={e => setTitle(e.target.value)} placeholder="Contoh: Bensin Tol Jkt-Bdg / Laundry Jas" className="input-field" />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label className="input-label">Kategori</label>
              <select value={category} onChange={e => setCategory(e.target.value)} className="input-field">
                {EXPENSE_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div>
              <label className="input-label">Nominal (Rp) *</label>
              <input type="number" required min="1" value={amount} onChange={e => setAmount(Number(e.target.value))} placeholder="0" className="input-field" style={{ fontWeight: '700', color: 'var(--error)' }} />
            </div>
          </div>
          <div>
            <label className="input-label">Tanggal</label>
            <input type="date" required value={date} onChange={e => setDate(e.target.value)} className="input-field" />
          </div>
          <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{ marginTop: '8px' }}>
            {saving ? 'Menyimpan...' : 'Simpan Pengeluaran'}
          </button>
        </form>
      </Modal>
    </div>
  );
};

const KpiCard: React.FC<{ label: string; value: string; sub: string; color: string; icon: React.ReactNode }> = ({ label, value, sub, color, icon }) => (
  <div className="card" style={{ padding: '20px', borderTop: `3px solid ${color}` }}>
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
      <span style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-4)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>{label}</span>
      <div style={{ width: '30px', height: '30px', borderRadius: '8px', background: 'var(--bg-surface-2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{icon}</div>
    </div>
    <h3 style={{ fontSize: '20px', fontWeight: '800', color: 'var(--text-1)', letterSpacing: '-0.02em', fontVariantNumeric: 'tabular-nums', marginBottom: '4px' }}>{value}</h3>
    <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{sub}</p>
  </div>
);
