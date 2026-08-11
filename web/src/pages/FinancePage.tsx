import React, { useState, useMemo } from 'react';
import {
  Wallet, TrendingUp, TrendingDown, Plus, Trash2, Calendar,
  FileText, CreditCard, ChevronDown, BarChart2, CheckCircle2,
  DollarSign, MessageSquare, ArrowRight, Check, X, Building2,
  PieChart, ShieldCheck, Sparkles, Filter, Receipt
} from 'lucide-react';
import type { Booking, Expense, Payment } from '../types';
import { getBookingFee, getBookingDate, getBookingClient, getBookingName, normalizeStatus } from '../types';
import { Modal } from '../components/Modal';
import { FAB } from '../components/FAB';

interface FinancePageProps {
  bookings: Booking[];
  expenses: Expense[];
  payments: Payment[];
  onSaveExpense: (exp: Expense) => Promise<void>;
  onDeleteExpense: (id: string) => Promise<void>;
  onSavePayment?: (p: Payment) => Promise<void>;
  onDeletePayment?: (id: string) => Promise<void>;
  onSaveBooking?: (b: Booking) => Promise<void>;
  currentUserId?: string;
  onOpenBookingDetail?: (bookingId: string) => void;
}

const EXPENSE_CATEGORIES = [
  'Transport / Bensin', 'Tol / Parkir', 'Kostum / Wardrobe', 'Makeup / Grooming',
  'Konsumsi / Makan', 'Equipment / Mic', 'Laundry', 'Koordinasi / Tips', 'Lainnya'
];

const PAYMENT_METHODS = ['Transfer Bank', 'Cash', 'QRIS', 'GoPay', 'OVO', 'Dana', 'Lainnya'];

function formatCurrency(val: number) {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(Math.max(0, val));
}

function formatDate(d: string) {
  if (!d) return '-';
  try {
    return new Date(d + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
  } catch { return d; }
}

type FinanceTab = 'PIUTANG' | 'BEBAN' | 'ARUS_KAS';
type PiutangFilter = 'ALL' | 'OUTSTANDING' | 'PARTIAL' | 'PAID';

export const FinancePage: React.FC<FinancePageProps> = ({
  bookings = [],
  expenses = [],
  payments = [],
  onSaveExpense,
  onDeleteExpense,
  onSavePayment,
  onDeletePayment,
  onSaveBooking,
  currentUserId,
  onOpenBookingDetail
}) => {
  const [activeTab, setActiveTab] = useState<FinanceTab>('PIUTANG');
  const [piutangFilter, setPiutangFilter] = useState<PiutangFilter>('ALL');
  const [expenseCategoryFilter, setExpenseCategoryFilter] = useState<string>('ALL');
  
  const [toastMsg, setToastMsg] = useState('');

  // Modals state
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);
  const [showBookingPicker, setShowBookingPicker] = useState(false);
  const [selectedBookingForPayment, setSelectedBookingForPayment] = useState<Booking | null>(null);
  const [pendingPayment, setPendingPayment] = useState<{ booking: Booking; amount: number; date: string; method: string; note: string } | null>(null);

  // Expense form state
  const [expBookingId, setExpBookingId] = useState('');
  const [expCategory, setExpCategory] = useState('Transport / Bensin');
  const [expAmount, setExpAmount] = useState<number | ''>('');
  const [expDate, setExpDate] = useState(new Date().toISOString().split('T')[0]);
  const [expNote, setExpNote] = useState('');
  const [expSaving, setExpSaving] = useState(false);

  // Payment form state
  const [payAmount, setPayAmount] = useState<number | ''>('');
  const [payDate, setPayDate] = useState(new Date().toISOString().split('T')[0]);
  const [payMethod, setPayMethod] = useState('Transfer Bank');
  const [payNote, setPayNote] = useState('');
  const [paySaving, setPaySaving] = useState(false);

  const showToast = (msg: string) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(''), 3000);
  };

  // ── Financial Calculations (Mirrors Android CalculateFinancialSummaryUseCase) ──
  const summary = useMemo(() => {
    const activeBookings = bookings.filter(b => normalizeStatus(b.status as string) !== 'cancelled');

    // Calculate total paid per booking
    const paidByBooking: Record<string, number> = {};
    payments.forEach(p => {
      paidByBooking[p.bookingId] = (paidByBooking[p.bookingId] || 0) + p.amount;
    });

    let totalHonor = 0;
    let totalPaid = 0;
    let totalOutstanding = 0;

    const enrichedBookings = activeBookings.map(b => {
      const fee = getBookingFee(b);
      const sumPayments = paidByBooking[b.id] || 0;
      const paid = paidByBooking[b.id] !== undefined 
        ? Math.max(sumPayments, (b.paidAmount ?? b.dp ?? 0)) 
        : (b.paidAmount ?? b.dp ?? 0);
      const outstanding = Math.max(0, fee - paid);
      totalHonor += fee;
      totalPaid += paid;
      totalOutstanding += outstanding;
      return { ...b, computedFee: fee, computedPaid: paid, computedOutstanding: outstanding };
    });

    const totalExpenses = expenses.reduce((s, e) => s + (e.amount || 0), 0);
    const netIncome = totalPaid - totalExpenses;
    const grossMargin = totalHonor > 0 ? ((totalHonor - totalExpenses) / totalHonor) * 100 : 100;

    return {
      totalHonor, totalPaid, totalOutstanding, totalExpenses, netIncome, grossMargin,
      enrichedBookings,
      outstandingJobCount: enrichedBookings.filter(b => b.computedOutstanding > 0).length
    };
  }, [bookings, expenses, payments]);

  // Filtered Bookings for Piutang Tab
  const filteredBookings = useMemo(() => {
    return summary.enrichedBookings.filter(b => {
      if (piutangFilter === 'OUTSTANDING') return b.computedOutstanding > 0;
      if (piutangFilter === 'PARTIAL') return b.computedPaid > 0 && b.computedOutstanding > 0;
      if (piutangFilter === 'PAID') return b.computedOutstanding <= 0 && b.computedFee > 0;
      return true;
    });
  }, [summary.enrichedBookings, piutangFilter]);

  // Filtered Expenses for Beban Tab
  const filteredExpenses = useMemo(() => {
    return expenses.filter(e => {
      if (expenseCategoryFilter === 'ALL') return true;
      return e.category === expenseCategoryFilter || e.category?.includes(expenseCategoryFilter);
    });
  }, [expenses, expenseCategoryFilter]);

  // Handle Add Expense
  const handleAddExpenseSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!expBookingId) {
      showToast('⚠️ Anda harus memilih Job Acara!');
      return;
    }
    if (!expAmount || Number(expAmount) <= 0) return;
    setExpSaving(true);
    try {
      const exp: Expense = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUserId || '',
        bookingId: expBookingId,
        title: expCategory, // fallback for backward compatibility on Web, though we rely on category
        category: expCategory,
        amount: Number(expAmount),
        date: expDate,
        note: expNote,
        createdAt: new Date().toISOString()
      };
      await onSaveExpense(exp);
      setIsExpenseModalOpen(false);
      setExpBookingId('');
      setExpAmount('');
      setExpNote('');
      showToast('Pengeluaran berhasil dicatat!');
    } catch (err) {
      console.error(err);
    } finally {
      setExpSaving(false);
    }
  };

  // Handle Payment Submit
  const handlePaymentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBookingForPayment || !payAmount || Number(payAmount) <= 0) return;
    setPendingPayment({
      booking: selectedBookingForPayment,
      amount: Number(payAmount),
      date: payDate,
      method: payMethod,
      note: payNote
    });
    setSelectedBookingForPayment(null);
  };

  const handleConfirmPayment = async () => {
    if (!pendingPayment || !onSavePayment) return;
    setPaySaving(true);
    try {
      const p: Payment = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUserId || '',
        bookingId: pendingPayment.booking.id,
        amount: pendingPayment.amount,
        paymentDate: pendingPayment.date,
        paymentMethod: pendingPayment.method,
        note: pendingPayment.note,
        createdAt: new Date().toISOString()
      };
      await onSavePayment(p);

      // Update booking DP if onSaveBooking exists
      if (onSaveBooking) {
        const newDp = (pendingPayment.booking.dp || 0) + pendingPayment.amount;
        await onSaveBooking({
          ...pendingPayment.booking,
          dp: newDp,
          paidAmount: newDp,
          status: newDp >= getBookingFee(pendingPayment.booking) ? 'completed' : pendingPayment.booking.status
        });
      }

      setPendingPayment(null);
      setPayAmount('');
      setPayNote('');
      showToast('Pembayaran pelunasan berhasil disimpan! 🎉');
    } catch (err) {
      console.error(err);
    } finally {
      setPaySaving(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '40px' }}>
      
      {/* Toast */}
      {toastMsg && (
        <div className="animate-fade-in" style={{
          position: 'fixed', bottom: '88px', left: '50%', transform: 'translateX(-50%)',
          zIndex: 99999, background: '#059669', color: '#FFFFFF', padding: '12px 22px',
          borderRadius: '9999px', fontWeight: '700', fontSize: '13px',
          boxShadow: '0 10px 30px -4px rgba(5,150,105,0.45)',
          display: 'flex', alignItems: 'center', gap: '10px', whiteSpace: 'nowrap',
          pointerEvents: 'none'
        }}>
          <div style={{
            width: '22px', height: '22px', borderRadius: '50%',
            background: 'rgba(255,255,255,0.2)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
          }}>
            <Check size={14} color="#FFFFFF" strokeWidth={3} />
          </div>
          <span>{toastMsg}</span>
        </div>
      )}


      {/* ── TAB SELECTOR: flat chip row, no container ── */}
      <div style={{ display: 'flex', gap: '6px', marginBottom: '14px', overflowX: 'auto', paddingBottom: '2px' }} className="scrollbar-none">
        <button
          onClick={() => setActiveTab('PIUTANG')}
          className={`chip${activeTab === 'PIUTANG' ? ' active' : ''}`}
        >
          <Wallet size={13} style={{ marginRight: '4px' }} /> Piutang Klien
          {summary.outstandingJobCount > 0 && (
            <span style={{ marginLeft: '6px', fontSize: '10px', fontWeight: '800', background: activeTab === 'PIUTANG' ? 'rgba(255,255,255,0.25)' : '#FEF3C7', color: activeTab === 'PIUTANG' ? '#fff' : '#D97706', padding: '1px 6px', borderRadius: '99px' }}>
              {summary.outstandingJobCount}
            </span>
          )}
        </button>

        <button
          onClick={() => setActiveTab('BEBAN')}
          className={`chip${activeTab === 'BEBAN' ? ' active' : ''}`}
        >
          <Receipt size={13} style={{ marginRight: '4px' }} /> Beban
          {expenses.length > 0 && (
            <span style={{ marginLeft: '6px', fontSize: '10px', fontWeight: '800', background: activeTab === 'BEBAN' ? 'rgba(255,255,255,0.25)' : '#FEE2E2', color: activeTab === 'BEBAN' ? '#fff' : '#DC2626', padding: '1px 6px', borderRadius: '99px' }}>
              {expenses.length}
            </span>
          )}
        </button>

        <button
          onClick={() => setActiveTab('ARUS_KAS')}
          className={`chip${activeTab === 'ARUS_KAS' ? ' active' : ''}`}
        >
          <TrendingUp size={13} style={{ marginRight: '4px' }} /> Arus Kas
        </button>
      </div>

      {/* ── KPI SUMMARY: hero + 2-col sub ── */}
      {activeTab === 'PIUTANG' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
          <SummaryBox label="TOTAL OMSET KONTRAK" value={formatCurrency(summary.totalHonor)} color="var(--primary)" bg="rgba(79,70,229,0.08)" icon={<BarChart2 size={16} color="var(--primary)" />} hero />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <SummaryBox label="TERBAYAR" value={formatCurrency(summary.totalPaid)} color="#059669" bg="#D1FAE5" icon={<TrendingUp size={16} color="#059669" />} />
            <SummaryBox label="PIUTANG" value={formatCurrency(summary.totalOutstanding)} color="#D97706" bg="#FEF3C7" icon={<CreditCard size={16} color="#D97706" />} />
          </div>
        </div>
      )}

      {activeTab === 'BEBAN' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
          <SummaryBox label="TOTAL BEBAN OPERASIONAL" value={formatCurrency(summary.totalExpenses)} color="#DC2626" bg="#FEE2E2" icon={<TrendingDown size={16} color="#DC2626" />} hero />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <SummaryBox label="OMSET BRUTO" value={formatCurrency(summary.totalHonor)} color="var(--primary)" bg="rgba(79,70,229,0.08)" icon={<BarChart2 size={16} color="var(--primary)" />} />
            <SummaryBox label="LABA BERSIH" value={formatCurrency(summary.netIncome)} color="#059669" bg="#D1FAE5" icon={<TrendingUp size={16} color="#059669" />} />
          </div>
        </div>
      )}

      {activeTab === 'ARUS_KAS' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
          <SummaryBox label="NET INCOME (KAS BERSIH)" value={formatCurrency(summary.netIncome)} color="var(--primary)" bg="rgba(79,70,229,0.08)" icon={<Wallet size={16} color="var(--primary)" />} hero />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <SummaryBox label="KAS MASUK" value={formatCurrency(summary.totalPaid)} color="#059669" bg="#D1FAE5" icon={<TrendingUp size={16} color="#059669" />} />
            <SummaryBox label="KAS KELUAR" value={formatCurrency(summary.totalExpenses)} color="#DC2626" bg="#FEE2E2" icon={<TrendingDown size={16} color="#DC2626" />} />
          </div>
        </div>
      )}

      {/* ── TAB 1: PIUTANG CONTENT ── */}
      {activeTab === 'PIUTANG' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>

          {/* Filter Chips Bar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)', marginRight: '4px' }}>Filter:</h3>
            {[
              { id: 'ALL', label: 'Semua' },
              { id: 'OUTSTANDING', label: 'Piutang Aktif' },
              { id: 'PARTIAL', label: 'DP Masuk' },
              { id: 'PAID', label: 'Lunas' },
            ].map(f => (
              <button
                key={f.id}
                onClick={() => setPiutangFilter(f.id as PiutangFilter)}
                className={`chip${piutangFilter === f.id ? ' active' : ''}`}
              >
                {f.label}
              </button>
            ))}
          </div>

          {/* Bookings Piutang Cards */}
          {filteredBookings.length === 0 ? (
            <div className="card empty-state" style={{ padding: '48px 24px' }}>
              <div className="empty-state-icon"><Wallet size={24} /></div>
              <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)', marginTop: '8px' }}>Tidak Ada Data Piutang</h3>
              <p style={{ fontSize: '13px', color: 'var(--text-3)', maxWidth: '300px', margin: '4px auto 0' }}>
                Tidak ditemukan job acara dengan kriteria filter piutang yang dipilih.
              </p>
            </div>
          ) : (
            filteredBookings.map(b => {
              const isLunas = b.computedOutstanding <= 0 && b.computedFee > 0;
              const clientPhone = (b.pic || '').replace(/\D/g, '');
              const waMsg = `Halo Kak ${getBookingClient(b) || 'Klien'}, izin mengonfirmasi terkait status pelunasan honor untuk acara *${getBookingName(b)}* dengan sisa tagihan sebesar *${formatCurrency(b.computedOutstanding)}*. Terima kasih banyak 🙏`;
              const waLink = clientPhone
                ? `https://wa.me/${clientPhone.startsWith('0') ? '62' + clientPhone.slice(1) : clientPhone}?text=${encodeURIComponent(waMsg)}`
                : `https://wa.me/?text=${encodeURIComponent(waMsg)}`;

              return (
                <div key={b.id} className="card hover-scale" style={{ padding: '20px', borderLeft: isLunas ? '5px solid #059669' : '5px solid #D97706' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '12px', flexWrap: 'wrap', gap: '10px' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                        <span className="badge badge-primary" style={{ fontSize: '11px', fontWeight: '700' }}>🎤 {b.category || 'Event'}</span>
                        <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>• {formatDate(getBookingDate(b))}</span>
                      </div>
                      <h4
                        onClick={() => onOpenBookingDetail && onOpenBookingDetail(b.id)}
                        style={{ fontSize: '16px', fontWeight: '900', color: 'var(--text-1)', cursor: onOpenBookingDetail ? 'pointer' : 'default' }}
                      >
                        {getBookingName(b)}
                      </h4>
                      <p style={{ fontSize: '13px', color: 'var(--text-3)', marginTop: '2px' }}>
                        Klien: <strong style={{ color: 'var(--text-1)' }}>{getBookingClient(b) || 'Personal'}</strong>
                      </p>
                    </div>

                    <span style={{ fontSize: '11px', fontWeight: '800', padding: '4px 12px', borderRadius: '9999px', color: isLunas ? '#059669' : '#D97706', background: isLunas ? '#D1FAE5' : '#FEF3C7' }}>
                      {isLunas ? '✓ LUNAS' : `SISA PIUTANG ${formatCurrency(b.computedOutstanding)}`}
                    </span>
                  </div>

                  {/* Financial Bar Progress */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12.5px', marginBottom: '6px' }}>
                    <span style={{ color: 'var(--text-3)' }}>Terbayar: <strong style={{ color: '#059669' }}>{formatCurrency(b.computedPaid)}</strong></span>
                    <span style={{ color: 'var(--text-3)' }}>Total Honor: <strong style={{ color: 'var(--text-1)' }}>{formatCurrency(b.computedFee)}</strong></span>
                  </div>
                  <div style={{ height: '6px', background: 'var(--bg-surface-2)', borderRadius: '3px', overflow: 'hidden', marginBottom: '14px' }}>
                    <div style={{ height: '100%', background: isLunas ? '#059669' : 'var(--primary)', width: `${b.computedFee > 0 ? Math.min(100, (b.computedPaid / b.computedFee) * 100) : 0}%`, transition: 'width 0.3s' }} />
                  </div>

                  {/* Action Buttons */}
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {!isLunas && (
                      <>
                        <button onClick={() => setSelectedBookingForPayment(b)} className="btn btn-primary btn-sm" style={{ gap: '5px', background: '#7C3AED', borderColor: '#7C3AED' }}>
                          <CreditCard size={14} /> Catat Pelunasan
                        </button>
                        <a href={waLink} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none' }}>
                          <button className="btn btn-secondary btn-sm" style={{ gap: '5px', color: '#25D366', borderColor: 'rgba(37,211,102,0.3)', background: 'rgba(37,211,102,0.08)' }}>
                            <MessageSquare size={14} /> Follow Up WA
                          </button>
                        </a>
                      </>
                    )}
                    {onOpenBookingDetail && (
                      <button onClick={() => onOpenBookingDetail(b.id)} className="btn btn-ghost btn-sm" style={{ gap: '4px', marginLeft: 'auto' }}>
                        Detail Job <ArrowRight size={14} />
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          )}

        </div>
      )}

      {/* ── TAB 2: BEBAN OPERASIONAL CONTENT ── */}
      {activeTab === 'BEBAN' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* Category Chips Bar */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '10px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)' }}>Beban Operasional & Vendor</h3>

            <div style={{ display: 'flex', gap: '6px', overflowX: 'auto', paddingBottom: '4px' }} className="scrollbar-none">
              {['ALL', ...EXPENSE_CATEGORIES].map(cat => (
                <button
                  key={cat}
                  onClick={() => setExpenseCategoryFilter(cat)}
                  className="badge"
                  style={{
                    cursor: 'pointer', padding: '6px 14px', fontSize: '12px', fontWeight: '700', borderRadius: '9999px',
                    border: expenseCategoryFilter === cat ? '1px solid #DC2626' : '1px solid var(--border)',
                    background: expenseCategoryFilter === cat ? '#DC2626' : 'var(--bg-surface-2)',
                    color: expenseCategoryFilter === cat ? '#FFFFFF' : 'var(--text-2)',
                    transition: 'all 0.15s'
                  }}
                >
                  {cat === 'ALL' ? 'Semua Kategori' : cat}
                </button>
              ))}
            </div>
          </div>

          {/* Expenses List Cards */}
          {filteredExpenses.length === 0 ? (
            <div className="card empty-state" style={{ padding: '48px 24px' }}>
              <div className="empty-state-icon"><Receipt size={24} color="#DC2626" /></div>
              <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)', marginTop: '8px' }}>Belum Ada Pengeluaran</h3>
              <p style={{ fontSize: '13px', color: 'var(--text-3)', maxWidth: '320px', margin: '4px auto' }}>
                Catatan beban operasional seperti bensin, tol, wardrobe, makeup, atau sound system akan muncul di sini.
              </p>
            </div>
          ) : (
            filteredExpenses.map(exp => {
              const linkedBooking = bookings.find(b => b.id === exp.bookingId);
              return (
                <div key={exp.id} className="card hover-scale" style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: '#FEE2E2', color: '#DC2626', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                      <TrendingDown size={20} />
                    </div>
                    <div>
                      <h4 style={{ fontSize: '14.5px', fontWeight: '800', color: 'var(--text-1)' }}>{exp.category}</h4>
                      <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px' }}>
                        {exp.note && <span style={{ marginRight: '6px' }}>{exp.note} •</span>}
                        <span>{formatDate(exp.date)}</span>
                        {linkedBooking && <span style={{ marginLeft: '6px', color: 'var(--primary)' }}>({getBookingName(linkedBooking)})</span>}
                      </p>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span style={{ fontSize: '16px', fontWeight: '900', color: '#DC2626' }}>- {formatCurrency(exp.amount)}</span>
                    <button onClick={() => onDeleteExpense(exp.id)} className="btn btn-ghost btn-sm" style={{ color: 'var(--error)', padding: '6px' }} title="Hapus">
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
              );
            })
          )}

        </div>
      )}

      {/* ── TAB 3: ARUS KAS & MARGIN CONTENT ── */}
      {activeTab === 'ARUS_KAS' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* Monthly Revenue Bar Chart (Android Parity: FinanceChart.kt) */}
          <div className="card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: '900', color: 'var(--text-1)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <BarChart2 size={18} color="var(--primary)" /> Grafik Omset & Tren Bulanan MC
                </h3>
                <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px' }}>
                  Distribusi omset honorarium dari seluruh job acara per bulan.
                </p>
              </div>
            </div>

            {/* Visual Bar Chart */}
            <MonthlyRevenueBarChart bookings={bookings} />
          </div>

          {/* Ringkasan Arus Kas Card */}
          <div className="card" style={{ padding: '24px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: '900', color: 'var(--text-1)', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <PieChart size={18} color="var(--primary)" /> Ringkasan Arus Kas & Gross Margin MC
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', fontSize: '14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Omset Bruto (Kontrak All Active Jobs)</span>
                <span style={{ fontWeight: '800', color: 'var(--text-1)' }}>{formatCurrency(summary.totalHonor)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Kas Masuk Terbayar (DP & Pelunasan)</span>
                <span style={{ fontWeight: '800', color: '#059669' }}>{formatCurrency(summary.totalPaid)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '10px' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Beban Operasional / Vendor</span>
                <span style={{ fontWeight: '800', color: '#DC2626' }}>- {formatCurrency(summary.totalExpenses)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '6px' }}>
                <span style={{ fontWeight: '900', color: 'var(--text-1)' }}>Net Income (Pendapatan Bersih Realized)</span>
                <span style={{ fontSize: '18px', fontWeight: '900', color: summary.netIncome >= 0 ? '#059669' : '#DC2626' }}>
                  {formatCurrency(summary.netIncome)}
                </span>
              </div>
            </div>

            {/* Profit Margin Indicator */}
            <div style={{ marginTop: '20px', padding: '14px 16px', borderRadius: '12px', background: 'rgba(79,70,229,0.08)', border: '1px solid rgba(79,70,229,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: '800', color: 'var(--primary)', textTransform: 'uppercase' }}>ESTIMASI MARGIN LABA</span>
                <p style={{ fontSize: '12px', color: 'var(--text-2)', marginTop: '2px' }}>Persentase kebersihan honor dari beban operasional.</p>
              </div>
              <span style={{ fontSize: '24px', fontWeight: '900', color: 'var(--primary)' }}>
                {summary.grossMargin.toFixed(1)}%
              </span>
            </div>
          </div>

          {/* Payment Log History */}
          <div className="card" style={{ padding: '20px' }}>
            <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CreditCard size={18} color="#059669" /> Riwayat Transaksi Pembayaran Masuk
            </h3>

            {payments.length === 0 ? (
              <p style={{ fontSize: '13px', color: 'var(--text-3)', textAlign: 'center', padding: '20px 0' }}>Belum ada rekam transaksi pembayaran.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {payments.map(p => {
                  const b = bookings.find(item => item.id === p.bookingId);
                  return (
                    <div key={p.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 14px', borderRadius: '10px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)' }}>
                      <div>
                        <h5 style={{ fontSize: '13.5px', fontWeight: '800', color: 'var(--text-1)' }}>
                          {b ? getBookingName(b) : `Job #${p.bookingId.takeLast ? p.bookingId.takeLast(4) : p.bookingId}`}
                        </h5>
                        <p style={{ fontSize: '11.5px', color: 'var(--text-3)', marginTop: '2px' }}>
                          Metode: <strong style={{ color: 'var(--text-2)' }}>{p.paymentMethod}</strong> • {formatDate(p.paymentDate)} {p.note ? `(${p.note})` : ''}
                        </p>
                      </div>
                      <span style={{ fontSize: '15px', fontWeight: '900', color: '#059669' }}>+ {formatCurrency(p.amount)}</span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

        </div>
      )}


      {/* ══════════════ MODALS ══════════════ */}

      {/* 1. MODAL CATAT BEBAN / EXPENSE */}
      <Modal isOpen={isExpenseModalOpen} onClose={() => setIsExpenseModalOpen(false)} title="Catat Beban Operasional / Vendor">
        <form onSubmit={handleAddExpenseSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label className="input-label">Pilih Acara / Job Terkait *</label>
            <select required value={expBookingId} onChange={e => setExpBookingId(e.target.value)} className="input-field" style={{ fontWeight: '700' }}>
              <option value="" disabled>-- Pilih Job Acara Aktif --</option>
              {summary.enrichedBookings.map(b => (
                <option key={b.id} value={b.id}>{getBookingName(b)} ({getBookingClient(b) || 'Client'})</option>
              ))}
            </select>
          </div>
          <div>
            <label className="input-label">Kategori Beban</label>
            <select value={expCategory} onChange={e => setExpCategory(e.target.value)} className="input-field">
              {EXPENSE_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="input-label">Nominal (Rp) *</label>
            <input type="number" required min="1" value={expAmount} onChange={e => setExpAmount(e.target.value ? Number(e.target.value) : '')} placeholder="0" className="input-field" style={{ fontWeight: '700', color: '#DC2626' }} />
          </div>
          <div>
            <label className="input-label">Tanggal</label>
            <input type="date" value={expDate} onChange={e => setExpDate(e.target.value)} className="input-field" />
          </div>
          <div>
            <label className="input-label">Catatan Tambahan (Opsional)</label>
            <input type="text" value={expNote} onChange={e => setExpNote(e.target.value)} className="input-field" placeholder="Keterangan vendor / bon..." />
          </div>
          <button type="submit" disabled={expSaving} className="btn btn-primary btn-full btn-lg" style={{ background: '#DC2626', borderColor: '#DC2626', marginTop: '6px' }}>
            {expSaving ? 'Menyimpan...' : 'Simpan Beban Operasional 🚀'}
          </button>
        </form>
      </Modal>

      {/* 2. MODAL PICKER JOB PELUNASAN (Android Parity) */}
      <Modal isOpen={showBookingPicker} onClose={() => setShowBookingPicker(false)} title="Pilih Job Acara untuk Pelunasan">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <p style={{ fontSize: '13px', color: 'var(--text-3)', marginBottom: '4px' }}>Pilih job acara aktif yang menerima pembayaran pelunasan:</p>
          {summary.enrichedBookings.length === 0 ? (
            <p style={{ fontSize: '13px', color: 'var(--text-4)', textAlign: 'center', padding: '20px 0' }}>Belum ada job aktif.</p>
          ) : (
            summary.enrichedBookings.map(b => (
              <div
                key={b.id}
                onClick={() => {
                  setSelectedBookingForPayment(b);
                  setShowBookingPicker(false);
                }}
                className="card card-interactive"
                style={{ padding: '14px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
              >
                <div>
                  <h5 style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-1)' }}>{getBookingName(b)}</h5>
                  <p style={{ fontSize: '11.5px', color: 'var(--text-3)', marginTop: '2px' }}>
                    {getBookingClient(b) || 'Personal'} • {formatDate(getBookingDate(b))}
                  </p>
                </div>
                <span style={{ fontSize: '12.5px', fontWeight: '800', color: b.computedOutstanding > 0 ? '#D97706' : '#059669' }}>
                  {b.computedOutstanding > 0 ? `Sisa ${formatCurrency(b.computedOutstanding)}` : 'LUNAS ✓'}
                </span>
              </div>
            ))
          )}
        </div>
      </Modal>

      {/* 3. MODAL FORM PELUNASAN / PAYMENT INPUT */}
      {selectedBookingForPayment && (
        <Modal isOpen={!!selectedBookingForPayment} onClose={() => setSelectedBookingForPayment(null)} title={`Catat Pelunasan: ${getBookingName(selectedBookingForPayment)}`}>
          <form onSubmit={handlePaymentSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div style={{ padding: '12px 14px', borderRadius: '10px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)', fontSize: '13px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Honor Acara:</span>
                <span style={{ fontWeight: '700' }}>{formatCurrency(selectedBookingForPayment.computedFee)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '4px' }}>
                <span style={{ color: 'var(--text-3)' }}>Sisa Tagihan Piutang:</span>
                <span style={{ fontWeight: '800', color: '#D97706' }}>{formatCurrency(selectedBookingForPayment.computedOutstanding)}</span>
              </div>
            </div>

            <div>
              <label className="input-label">Jumlah Pembayaran (Rp) *</label>
              <input
                type="number" required min="1"
                value={payAmount}
                onChange={e => setPayAmount(e.target.value ? Number(e.target.value) : '')}
                className="input-field"
                placeholder={`Maks sisa: ${formatCurrency(selectedBookingForPayment.computedOutstanding)}`}
              />
            </div>
            <div>
              <label className="input-label">Metode Pembayaran</label>
              <select value={payMethod} onChange={e => setPayMethod(e.target.value)} className="input-field">
                {PAYMENT_METHODS.map(m => <option key={m} value={m}>{m}</option>)}
              </select>
            </div>
            <div>
              <label className="input-label">Tanggal Pembayaran</label>
              <input type="date" value={payDate} onChange={e => setPayDate(e.target.value)} className="input-field" />
            </div>
            <div>
              <label className="input-label">Catatan Tambahan (Opsional)</label>
              <input type="text" value={payNote} onChange={e => setPayNote(e.target.value)} className="input-field" placeholder="Keterangan transfer..." />
            </div>
            <button type="submit" className="btn btn-primary btn-full btn-lg" style={{ background: '#7C3AED', borderColor: '#7C3AED', marginTop: '6px' }}>
              Lanjutkan Konfirmasi Pembayaran ➔
            </button>
          </form>
        </Modal>
      )}

      {/* 4. MODAL INSTANT PAYMENT CONFIRMATION (Android Parity) */}
      {pendingPayment && (
        <Modal isOpen={!!pendingPayment} onClose={() => setPendingPayment(null)} title="Konfirmasi Pelunasan Pembayaran">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ padding: '16px', borderRadius: '12px', background: '#ECFDF5', border: '1px solid #A7F3D0', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', fontWeight: '800', color: '#059669', textTransform: 'uppercase' }}>NOMINAL PEMBAYARAN</span>
              <h2 style={{ fontSize: '26px', fontWeight: '900', color: '#047857', marginTop: '2px' }}>{formatCurrency(pendingPayment.amount)}</h2>
              <p style={{ fontSize: '12px', color: '#065F46', marginTop: '2px' }}>Metode: {pendingPayment.method} • {pendingPayment.date}</p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '13px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Acara:</span>
                <span style={{ fontWeight: '700' }}>{getBookingName(pendingPayment.booking)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Honor Acara:</span>
                <span style={{ fontWeight: '700' }}>{formatCurrency(pendingPayment.booking.computedFee)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '6px', borderTop: '1px solid var(--border)' }}>
                <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>Sisa Piutang Setelah Pembayaran Ini:</span>
                <span style={{ fontWeight: '900', color: Math.max(0, pendingPayment.booking.computedOutstanding - pendingPayment.amount) === 0 ? '#059669' : '#D97706' }}>
                  {formatCurrency(Math.max(0, pendingPayment.booking.computedOutstanding - pendingPayment.amount))}
                </span>
              </div>
            </div>

            <button onClick={handleConfirmPayment} disabled={paySaving} className="btn btn-primary btn-full btn-lg" style={{ background: '#059669', borderColor: '#059669' }}>
              {paySaving ? 'Menyimpan...' : 'Konfirmasi & Simpan Pembayaran ✓'}
            </button>
          </div>
        </Modal>
      )}

      {/* ── FLOATING SPEED DIAL ACTION BUTTON ── */}
      <FAB
        label="Keuangan"
        items={[
          {
            key: 'expense',
            label: 'Catat Pengeluaran',
            icon: Plus,
            onClick: () => setIsExpenseModalOpen(true)
          },
          {
            key: 'payment',
            label: 'Catat Pelunasan Klien',
            icon: CreditCard,
            onClick: () => setShowBookingPicker(true)
          }
        ]}
      />
    </div>
  );
};

const SummaryBox: React.FC<{ label: string; value: string; color: string; bg: string; icon: React.ReactNode; hero?: boolean }> = ({ label, value, color, bg, icon, hero }) => (
  <div className="card" style={{ padding: hero ? '14px 16px' : '10px 12px', borderLeft: `3px solid ${color}` }}>
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: hero ? '6px' : '4px' }}>
      <span style={{ fontSize: hero ? '10px' : '9px', fontWeight: '800', color: 'var(--text-4)', textTransform: 'uppercase', letterSpacing: '0.05em', lineHeight: '1.3' }}>{label}</span>
      <div style={{ width: hero ? '28px' : '22px', height: hero ? '28px' : '22px', borderRadius: '8px', background: bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
        {React.cloneElement(icon as React.ReactElement, { size: hero ? 14 : 11 })}
      </div>
    </div>
    <h3 style={{ fontSize: hero ? 'clamp(16px,4vw,20px)' : 'clamp(12px, 3.5vw, 15px)', fontWeight: '800', color: 'var(--text-1)', fontVariantNumeric: 'tabular-nums', lineHeight: '1.2' }}>{value}</h3>
  </div>
);

const MonthlyRevenueBarChart: React.FC<{ bookings: Booking[] }> = ({ bookings }) => {
  const chartData = useMemo(() => {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
    const monthlyAmounts = Array(12).fill(0);

    bookings.forEach(b => {
      if (normalizeStatus(b.status as string) === 'cancelled') return;
      const dStr = getBookingDate(b);
      if (!dStr) return;
      const dateObj = new Date(dStr + 'T00:00:00');
      if (isNaN(dateObj.getTime())) return;
      const monthIdx = dateObj.getMonth();
      if (monthIdx >= 0 && monthIdx < 12) {
        monthlyAmounts[monthIdx] += getBookingFee(b);
      }
    });

    const maxVal = Math.max(...monthlyAmounts, 1);
    return months.map((label, i) => ({
      label,
      amount: monthlyAmounts[i],
      percentage: (monthlyAmounts[i] / maxVal) * 100
    }));
  }, [bookings]);

  const hasData = chartData.some(d => d.amount > 0);

  if (!hasData) {
    return (
      <div style={{ padding: '32px 16px', textAlign: 'center', background: 'var(--bg-surface-2)', borderRadius: '12px', border: '1px border var(--border)' }}>
        <Calendar size={28} color="var(--primary)" style={{ margin: '0 auto 8px', opacity: 0.7 }} />
        <h4 style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-1)' }}>Belum Ada Data Transaksi Bulanan</h4>
        <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '2px' }}>
          Tambahkan tanggal & honorarium job acara untuk melihat tren grafik bulanan.
        </p>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: '6px', height: '160px', padding: '10px 0 6px' }}>
        {chartData.map((d, i) => (
          <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%', justifyContent: 'flex-end', position: 'relative' }} className="group">
            {/* Tooltip on Hover */}
            {d.amount > 0 && (
              <div style={{
                position: 'absolute', top: '-28px', background: '#0F172A', color: 'white',
                padding: '2px 6px', borderRadius: '4px', fontSize: '10px', fontWeight: '700',
                whiteSpace: 'nowrap', opacity: 0.9, pointerEvents: 'none'
              }}>
                {formatCurrency(d.amount)}
              </div>
            )}
            <div
              style={{
                width: '100%', maxWidth: '24px',
                height: `${Math.max(d.percentage, d.amount > 0 ? 8 : 4)}%`,
                background: d.percentage > 70 ? 'var(--primary)' : d.amount > 0 ? 'rgba(79,70,229,0.45)' : 'var(--border)',
                borderRadius: '6px 6px 2px 2px', transition: 'height 0.3s ease'
              }}
            />
          </div>
        ))}
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: '6px', marginTop: '6px', borderTop: '1px solid var(--border)', paddingTop: '6px' }}>
        {chartData.map((d, i) => (
          <div key={i} style={{ flex: 1, textAlign: 'center', fontSize: '10.5px', fontWeight: '700', color: d.amount > 0 ? 'var(--primary)' : 'var(--text-4)' }}>
            {d.label}
          </div>
        ))}
      </div>
    </div>
  );
};

