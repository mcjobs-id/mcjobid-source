import React, { useState, useEffect } from 'react';
import {
  ArrowLeft, Edit, Trash2, Calendar, MapPin, Clock, Users, FileText,
  CheckCircle, DollarSign, Mic, CreditCard, Plus, X,
  CheckCircle2, Circle, Phone, Navigation, MessageSquare, Copy,
  Receipt, Check, ExternalLink, Zap, AlertTriangle, ShieldCheck, Sparkles, MoreVertical
} from 'lucide-react';
import type { Booking, ChecklistItem, Payment, Expense, Client } from '../types';
import { useAuth } from '../context/AuthContext';
import {
  subscribeChecklists, saveChecklistItem, deleteChecklistItem,
  subscribeBookingPayments, savePayment, deletePayment,
  subscribeBookingExpenses, saveExpense, deleteExpense
} from '../services/firebaseService';
import { getBookingName, getBookingClient, getBookingDate, getBookingStart, getBookingFee, getBookingDp, getBookingVenue } from '../types';
import { Modal } from '../components/Modal';

interface BookingDetailPageProps {
  booking: Booking;
  clients?: Client[];
  payments?: Payment[];
  expenses?: Expense[];
  onBack: () => void;
  onEdit: (b: Booking) => Promise<void>;
  onDelete: (id: string) => void;
  onOpenInvoice: (b: Booking) => void;
  onOpenMcDayMode: (b: Booking) => void;
  onSavePayment?: (p: Payment) => Promise<void>;
  onDeletePayment?: (id: string) => Promise<void>;
  onSaveExpense?: (e: Expense) => Promise<void>;
  onDeleteExpense?: (id: string) => Promise<void>;
  currentUserId?: string;
}

function formatRp(val: number) {
  return `Rp ${Math.max(0, val).toLocaleString('id-ID')}`;
}

function formatDate(d: string) {
  if (!d) return '-';
  try {
    return new Date(d + 'T00:00:00').toLocaleDateString('id-ID', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  } catch { return d; }
}

const STATUS_LABELS: Record<string, { label: string; color: string; bg: string }> = {
  draft: { label: 'Draft', color: 'var(--text-3)', bg: 'var(--bg-surface-2)' },
  confirmed: { label: 'CONFIRMED / ACTIVE', color: '#7C3AED', bg: '#F5F3FF' },
  upcoming: { label: 'UPCOMING', color: '#1D4ED8', bg: '#EFF6FF' },
  today: { label: 'HARI INI (HARI H)', color: '#D97706', bg: '#FFFBEB' },
  completed: { label: '✓ SELESAI', color: '#059669', bg: '#D1FAE5' },
  cancelled: { label: '✕ DIBATALKAN', color: '#DC2626', bg: '#FEE2E2' },
};

const PAYMENT_METHODS = ['Transfer Bank', 'Cash', 'QRIS', 'GoPay', 'OVO', 'Dana', 'Lainnya'];
const EXPENSE_CATEGORIES = ['Transport / Bensin', 'Tol / Parkir', 'Kostum / Wardrobe', 'Makeup / Grooming', 'Konsumsi / Makan', 'Equipment / Mic', 'Laundry', 'Koordinasi / Tips', 'Lainnya'];

export const BookingDetailPage: React.FC<BookingDetailPageProps> = ({
  booking, clients = [], payments = [], expenses = [], onBack, onEdit, onDelete, onOpenInvoice, onOpenMcDayMode,
  currentUserId
}) => {
  const { currentUser } = useAuth();
  const uid = currentUserId || currentUser?.uid || '';

  const [activeModal, setActiveModal] = useState<'brief' | 'payment' | 'paymentConfirm' | 'expense' | 'edit' | 'status' | 'delete' | 'duplicate' | null>(null);
  const [toastMsg, setToastMsg] = useState('');

  // Subscribed live data
  const [checklist, setChecklist] = useState<ChecklistItem[]>([]);

  // Checklist input state
  const [newCheckTitle, setNewCheckTitle] = useState('');
  const [showCheckInput, setShowCheckInput] = useState(false);

  // Payment form state
  const [payAmount, setPayAmount] = useState<number | ''>('');
  const [payDate, setPayDate] = useState(new Date().toISOString().split('T')[0]);
  const [payMethod, setPayMethod] = useState('Transfer Bank');
  const [payNote, setPayNote] = useState('');
  const [pendingPayment, setPendingPayment] = useState<{ amount: number; date: string; method: string; note: string } | null>(null);
  const [paySaving, setPaySaving] = useState(false);

  // Expense form state
  const [expTitle, setExpTitle] = useState('');
  const [expCategory, setExpCategory] = useState('Transport / Bensin');
  const [expAmount, setExpAmount] = useState<number | ''>('');
  const [expDate, setExpDate] = useState(new Date().toISOString().split('T')[0]);
  const [expNote, setExpNote] = useState('');
  const [expSaving, setExpSaving] = useState(false);

  // Edit form state
  const [editForm, setEditForm] = useState({ ...booking });
  const [editSaving, setEditSaving] = useState(false);

  useEffect(() => {
    if (!uid || !booking.id) return;
    const unsubs = [
      subscribeChecklists(uid, booking.id, setChecklist),
    ];
    return () => unsubs.forEach(u => u());
  }, [uid, booking.id]);

  const fee = getBookingFee(booking);
  const dpField = getBookingDp(booking);
  const sumPayments = payments.reduce((s, p) => s + p.amount, 0);
  const totalPaid = payments.length > 0 ? Math.max(sumPayments, (booking.paidAmount ?? dpField)) : (booking.paidAmount ?? dpField);
  const outstanding = Math.max(0, fee - totalPaid);
  const totalExpense = expenses.reduce((s, e) => s + e.amount, 0);
  const netIncome = fee - totalExpense;
  const isLunas = outstanding <= 0 && fee > 0;
  
  const normalizedStatus = (booking.status || 'confirmed').toLowerCase();
  const statusInfo = STATUS_LABELS[normalizedStatus] || STATUS_LABELS.confirmed;
  const isCompleted = normalizedStatus === 'completed';
  const isCancelled = normalizedStatus === 'cancelled';
  
  const todayStr = new Date().toISOString().split('T')[0];
  const eventDateStr = getBookingDate(booking);
  const isToday = eventDateStr === todayStr || normalizedStatus === 'today';

  const showToast = (msg: string) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(''), 3000);
  };

  // Checklist Actions
  const handleAddChecklist = async () => {
    if (!newCheckTitle.trim() || !uid) return;
    const item: ChecklistItem = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      ownerId: uid,
      bookingId: booking.id,
      title: newCheckTitle.trim(),
      isCompleted: false,
      order: checklist.length
    };
    await saveChecklistItem(item);
    setNewCheckTitle('');
    setShowCheckInput(false);
    showToast('Checklist ditambahkan!');
  };

  const handleToggleChecklist = async (item: ChecklistItem) => {
    await saveChecklistItem({ ...item, isCompleted: !item.isCompleted });
  };

  const handleDeleteChecklist = async (item: ChecklistItem) => {
    await deleteChecklistItem(item.id);
  };

  // Payment Actions
  const handleInitiatePayment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!payAmount || Number(payAmount) <= 0) return;
    setPendingPayment({
      amount: Number(payAmount),
      date: payDate,
      method: payMethod,
      note: payNote
    });
    setActiveModal('paymentConfirm');
  };

  const handleConfirmPayment = async () => {
    if (!pendingPayment || !uid) return;
    setPaySaving(true);
    try {
      // Auto-migrate legacy DP if it exists and no payment records exist
      if (payments.length === 0 && dpField > 0) {
        const legacyPayment: Payment = {
          id: `legacy-dp-${booking.id}`,
          ownerId: uid,
          bookingId: booking.id,
          amount: dpField,
          paymentDate: getBookingDate(booking),
          date: getBookingDate(booking),
          paymentMethod: 'Transfer Bank',
          note: 'DP Awal (Migrasi Otomatis)',
          createdAt: new Date().toISOString()
        };
        await savePayment(legacyPayment);
      }

      const p: Payment = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: uid,
        bookingId: booking.id,
        amount: pendingPayment.amount,
        paymentDate: pendingPayment.date,
        paymentMethod: pendingPayment.method,
        note: pendingPayment.note
      };
      await savePayment(p);
      
      // Update booking DP total
      const newDp = totalPaid + pendingPayment.amount;
      const updatedBooking: Booking = {
        ...booking,
        dp: newDp,
        paidAmount: newDp,
        status: newDp >= fee ? 'completed' : booking.status
      };
      await onEdit(updatedBooking);

      setPendingPayment(null);
      setActiveModal(null);
      setPayAmount('');
      setPayNote('');
      showToast('Pembayaran berhasil dicatat!');
    } catch (err) {
      console.error(err);
    } finally {
      setPaySaving(false);
    }
  };

  // Expense Actions
  const handleSaveExpenseForm = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!expTitle.trim() || !expAmount || Number(expAmount) <= 0 || !uid) return;
    setExpSaving(true);
    try {
      const exp: Expense = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: uid,
        bookingId: booking.id,
        title: expTitle.trim(),
        category: expCategory,
        amount: Number(expAmount),
        date: expDate,
        note: expNote
      };
      await saveExpense(exp);
      setActiveModal(null);
      setExpTitle('');
      setExpAmount('');
      setExpNote('');
      showToast('Pengeluaran berhasil dicatat!');
    } catch (err) {
      console.error(err);
    } finally {
      setExpSaving(false);
    }
  };

  // Duplicate Job
  const handleDuplicateJob = async () => {
    const duplicated: Booking = {
      ...booking,
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      name: `${getBookingName(booking)} (Copy)`,
      status: 'confirmed',
      dp: 0,
      paidAmount: 0,
      date: '',
      eventDate: '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    await onEdit(duplicated);
    setActiveModal(null);
    showToast('Job diduplikasi! Silakan perbarui tanggalnya.');
  };

  const completedChecklistCount = checklist.filter(c => c.isCompleted).length;

  const waFollowUpMsg = `Halo Kak ${getBookingClient(booking) || 'Klien'}, izin follow up terkait pembayaran untuk acara ${getBookingName(booking)}. Saat ini masih terdapat sisa pembayaran sebesar ${formatRp(outstanding)}. Terima kasih 🙏`;
  const picPhone = (booking.pic || '').replace(/\D/g, '');
  const waLink = picPhone
    ? `https://wa.me/${picPhone.startsWith('0') ? '62' + picPhone.slice(1) : picPhone}?text=${encodeURIComponent(waFollowUpMsg)}`
    : `https://wa.me/?text=${encodeURIComponent(waFollowUpMsg)}`;
  const mapsLink = `https://maps.google.com/?q=${encodeURIComponent(getBookingVenue(booking) + ' ' + (booking.address || ''))}`;

  return (
    <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '40px' }}>
      
      {/* Toast Notification */}
      {toastMsg && (
        <div className="animate-fade-in" style={{
          position: 'fixed', top: '80px', left: '50%', transform: 'translateX(-50%)',
          zIndex: 9999, background: '#059669', color: 'white', padding: '10px 20px',
          borderRadius: '30px', fontWeight: '700', fontSize: '13px', boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
          display: 'flex', alignItems: 'center', gap: '8px'
        }}>
          <Check size={16} /> {toastMsg}
        </div>
      )}

      {/* ── TOP ACTION BAR ── */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', flexWrap: 'wrap', gap: '12px', marginBottom: '20px' }}>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <button
            onClick={() => setActiveModal('brief')}
            className="btn btn-secondary btn-sm"
            style={{ color: '#D97706', borderColor: '#FCD34D', background: '#FEF3C7', gap: '6px', fontWeight: '700' }}
          >
            <Zap size={15} fill="#D97706" /> Quick Brief
          </button>
          <button
            onClick={() => { setEditForm({ ...booking }); setActiveModal('edit'); }}
            className="btn btn-secondary btn-sm"
            style={{ gap: '5px' }}
          >
            <Edit size={14} /> Edit
          </button>
          <button
            onClick={() => setActiveModal('status')}
            className="btn btn-primary btn-sm"
            style={{ gap: '5px' }}
          >
            Status
          </button>
          <button
            onClick={() => setActiveModal('delete')}
            className="btn btn-ghost btn-sm"
            style={{ color: 'var(--error)', padding: '6px 8px' }}
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>

      {/* ── MC DAY MODE BANNER (Android Parity: Shown if event is TODAY) ── */}
      {isToday && (
        <button
          onClick={() => onOpenMcDayMode(booking)}
          className="card card-interactive animate-pulse"
          style={{
            width: '100%', padding: '16px 20px', marginBottom: '20px',
            background: 'linear-gradient(135deg, #D97706 0%, #B45309 100%)',
            color: 'white', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            boxShadow: '0 8px 24px rgba(217, 119, 6, 0.3)', border: 'none'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: 'rgba(255,255,255,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Mic size={22} color="white" />
            </div>
            <div style={{ textAlign: 'left' }}>
              <span style={{ fontSize: '15px', fontWeight: '900', letterSpacing: '0.02em', display: 'block' }}>⚡ BUKA MC DAY MODE (HARI H)</span>
              <span style={{ fontSize: '12px', color: 'rgba(255,255,255,0.9)' }}>Mode Fokus Panggung: Teleprompter Rundown, Timer & Sound Board</span>
            </div>
          </div>
          <Zap size={22} fill="white" />
        </button>
      )}

      {/* ── HEADER STATUS CARD (Android Layout) ── */}
      <div className="card" style={{ padding: '24px', marginBottom: '20px', borderLeft: `5px solid ${isCompleted ? '#059669' : isCancelled ? '#DC2626' : '#7C3AED'}` }}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '10px' }}>
            <span className="badge badge-primary" style={{ padding: '3px 10px', fontSize: '11px', fontWeight: '700' }}>
              {booking.category || 'Wedding'}
            </span>
            <span style={{ padding: '3px 10px', borderRadius: '9999px', fontSize: '11px', fontWeight: '700', color: statusInfo.color, background: statusInfo.bg }}>
              {statusInfo.label}
            </span>
            {fee > 0 && (
              <span style={{ padding: '3px 10px', borderRadius: '9999px', fontSize: '11px', fontWeight: '700', color: isLunas ? '#059669' : '#D97706', background: isLunas ? '#D1FAE5' : '#FEF3C7' }}>
                {isLunas ? 'LUNAS' : `PIUTANG ${formatRp(outstanding)}`}
              </span>
            )}
          </div>

        <h1 style={{ fontSize: '22px', fontWeight: '900', color: 'var(--text-1)', letterSpacing: '-0.02em', marginBottom: '4px' }}>
          {getBookingName(booking)}
        </h1>
        <p style={{ fontSize: '13px', color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: '5px' }}>
          Klien: <strong style={{ color: 'var(--text-1)' }}>{getBookingClient(booking) || 'Personal'}</strong>
        </p>
      </div>

      {/* ── SECTION 1: PERSIAPAN ACARA (CHECKLIST) ── */}
      <div className="card" style={{ padding: '20px', marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CheckCircle size={18} color="var(--primary)" />
            <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)' }}>Persiapan Acara (Checklist)</h3>
          </div>
          <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--primary)', background: 'var(--bg-surface-2)', padding: '4px 12px', borderRadius: '9999px', border: '1px solid var(--border)' }}>
            {checklist.length > 0 ? `${completedChecklistCount} / ${checklist.length} Selesai` : 'Belum Ada Item'}
          </span>
        </div>

        {checklist.length > 0 ? (
          <>
            <div style={{ height: '6px', background: 'var(--bg-surface-2)', borderRadius: '3px', overflow: 'hidden', marginBottom: '16px' }}>
              <div style={{ height: '100%', background: completedChecklistCount === checklist.length ? '#059669' : 'var(--primary)', width: `${(completedChecklistCount / checklist.length) * 100}%`, transition: 'width 0.3s' }} />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '14px' }}>
              {checklist.map(chk => (
                <div key={chk.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px', borderRadius: '10px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1, cursor: 'pointer' }} onClick={() => handleToggleChecklist(chk)}>
                    {chk.isCompleted ? <CheckCircle2 size={18} color="#059669" /> : <Circle size={18} color="var(--border)" />}
                    <span style={{ fontSize: '13.5px', fontWeight: chk.isCompleted ? '500' : '700', color: chk.isCompleted ? 'var(--text-4)' : 'var(--text-1)', textDecoration: chk.isCompleted ? 'line-through' : 'none' }}>
                      {chk.title}
                    </span>
                  </div>
                  <button onClick={() => handleDeleteChecklist(chk)} className="btn btn-ghost btn-sm" style={{ padding: '2px 6px', color: 'var(--text-4)' }}>
                    <X size={14} />
                  </button>
                </div>
              ))}
            </div>
          </>
        ) : (
          <div style={{ textAlign: 'center', padding: '20px 12px', background: 'var(--bg-surface-2)', borderRadius: '12px', marginBottom: '14px', border: '1px dashed var(--border)' }}>
            <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-2)' }}>Belum Ada Item Persiapan</p>
            <p style={{ fontSize: '11.5px', color: 'var(--text-3)', marginTop: '2px' }}>Catat kebutuhan cue card, dresscode, sound system, atau rundown acara.</p>
          </div>
        )}

        {showCheckInput ? (
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              autoFocus type="text"
              value={newCheckTitle}
              onChange={e => setNewCheckTitle(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') handleAddChecklist(); if (e.key === 'Escape') setShowCheckInput(false); }}
              placeholder="Contoh: Cek Cue Card, Brief Sound..."
              className="input-field" style={{ flex: 1 }}
            />
            <button onClick={handleAddChecklist} className="btn btn-primary btn-sm">Simpan</button>
            <button onClick={() => setShowCheckInput(false)} className="btn btn-ghost btn-sm"><X size={16} /></button>
          </div>
        ) : (
          <button onClick={() => setShowCheckInput(true)} className="btn btn-secondary btn-full btn-sm" style={{ gap: '6px' }}>
            <Plus size={14} /> Tambah Item Checklist
          </button>
        )}
      </div>

      {/* ── SECTION 2: KEUANGAN & NET INCOME (Android Parity) ── */}
      <div className="card" style={{ padding: '20px', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '7px' }}>
          <DollarSign size={15} color="var(--primary)" /> Keuangan & Net Income
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Total Honor (Revenue)</span>
            <span style={{ fontWeight: '800', color: 'var(--text-1)' }}>{formatRp(fee)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Total DP / Terbayar</span>
            <span style={{ fontWeight: '800', color: '#059669' }}>{formatRp(totalPaid)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px', padding: '6px 0', borderTop: '1px solid var(--border)' }}>
            <span style={{ color: 'var(--text-2)', fontWeight: '600' }}>Sisa Piutang</span>
            {isLunas ? (
              <span style={{ fontWeight: '900', color: '#059669', background: '#D1FAE5', padding: '2px 10px', borderRadius: '6px', fontSize: '12px' }}>
                Rp0 • LUNAS ✓
              </span>
            ) : (
              <span style={{ fontWeight: '800', color: '#D97706' }}>{formatRp(outstanding)}</span>
            )}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Total Pengeluaran (Expense)</span>
            <span style={{ fontWeight: '800', color: '#DC2626' }}>- {formatRp(totalExpense)}</span>
          </div>

          {/* NET INCOME BOX */}
          <div style={{ padding: '14px 16px', borderRadius: '12px', background: 'rgba(79,70,229,0.08)', border: '1px solid rgba(79,70,229,0.2)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '6px' }}>
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--primary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>NET INCOME (BERSIH)</span>
            <span style={{ fontSize: '18px', fontWeight: '900', color: 'var(--primary)' }}>{formatRp(netIncome)}</span>
          </div>
        </div>

        {/* Financial Actions */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px' }}>
          {!isLunas && fee > 0 && (
            <button onClick={() => setActiveModal('payment')} className="btn btn-primary btn-sm" style={{ gap: '6px' }}>
              <CreditCard size={14} /> Pelunasan / Catat Pembayaran
            </button>
          )}
          <button onClick={() => setActiveModal('expense')} className="btn btn-secondary btn-sm" style={{ gap: '6px' }}>
            <Receipt size={14} /> Catat Pengeluaran
          </button>
        </div>

        {/* Payment History */}
        {payments.length > 0 && (
          <div style={{ marginTop: '16px', paddingTop: '14px', borderTop: '1px solid var(--border)' }}>
            <p style={{ fontSize: '11px', fontWeight: '800', color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '8px' }}>RIWAYAT PEMBAYARAN MASUK</p>
            {payments.map(p => (
              <div key={p.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 0', borderBottom: '1px solid var(--border)', fontSize: '12.5px' }}>
                <div>
                  <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{p.paymentMethod}</span>
                  <span style={{ fontSize: '11px', color: 'var(--text-3)', marginLeft: '8px' }}>({p.paymentDate})</span>
                </div>
                <span style={{ fontWeight: '800', color: '#059669' }}>{formatRp(p.amount)}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── SECTION 3: JADWAL & LOKASI (Android Parity) ── */}
      <div className="card" style={{ padding: '20px', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '7px' }}>
          <Calendar size={15} color="var(--primary)" /> Jadwal & Lokasi
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Tanggal Acara</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{formatDate(getBookingDate(booking))}</span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Waktu Perform</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>
              {getBookingStart(booking) || '19:00'} – {booking.end || '22:00'} WIB
            </span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Lokasi Venue</span>
            {getBookingVenue(booking) ? (
              <a href={mapsLink} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm" style={{ padding: '4px 10px', fontSize: '12px', gap: '6px', color: 'var(--primary)' }}>
                <MapPin size={13} /> {getBookingVenue(booking)} <ExternalLink size={11} />
              </a>
            ) : (
              <span style={{ fontWeight: '700', color: 'var(--text-3)' }}>Belum ditentukan</span>
            )}
          </div>

          {booking.address && (
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
              <span style={{ color: 'var(--text-3)' }}>Alamat Lengkap</span>
              <span style={{ fontWeight: '600', color: 'var(--text-2)', maxWidth: '280px', textAlign: 'right' }}>{booking.address}</span>
            </div>
          )}
        </div>
      </div>

      {/* ── SECTION 4: SPESIFIKASI MC & KONSEP (ALWAYS FULLY VISIBLE) ── */}
      <div className="card" style={{ padding: '20px', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '7px' }}>
          <Mic size={15} color="var(--primary)" /> Detail Spesifikasi MC & Konsep
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Dresscode / Wardrobe</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.dresscode || 'Sesuaikan Theme'}</span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Tema Acara</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.theme || '-'}</span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Format MC</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.mcType || 'Single MC'}</span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Bahasa Pengantar</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.language || 'Bahasa Indonesia'}</span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13.5px' }}>
            <span style={{ color: 'var(--text-3)' }}>Estimasi Audience / Tamu</span>
            <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.audience || '-'}</span>
          </div>

          {booking.pic && (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '13.5px', paddingTop: '4px' }}>
              <span style={{ color: 'var(--text-3)' }}>Kontak PIC / WO</span>
              <a href={waLink} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm" style={{ padding: '4px 10px', fontSize: '12px', gap: '6px', color: '#25D366' }}>
                <Phone size={13} /> {booking.pic} <ExternalLink size={11} />
              </a>
            </div>
          )}
        </div>
      </div>

      {/* ── SECTION 5: REQUEST KHUSUS & PROTOKOL VIP (ALWAYS FULLY VISIBLE IF PRESENT) ── */}
      {booking.specialRequest && (
        <div className="card" style={{ padding: '20px', marginBottom: '20px', background: 'rgba(245, 158, 11, 0.06)', border: '1px solid rgba(245, 158, 11, 0.25)' }}>
          <h3 style={{ fontSize: '13px', fontWeight: '800', color: '#D97706', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Zap size={13} fill="#D97706" /> Request Khusus & Protokol VIP
          </h3>
          <p style={{ fontSize: '13.5px', color: 'var(--text-1)', lineHeight: '1.6', fontWeight: '500' }}>
            {booking.specialRequest}
          </p>
        </div>
      )}

      {/* ── SECTION 6: CATATAN INTERNAL MC (ALWAYS FULLY VISIBLE IF PRESENT) ── */}
      {(booking.note || booking.notes) && (
        <div className="card" style={{ padding: '20px', marginBottom: '20px', background: 'var(--bg-surface-2)' }}>
          <h3 style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <FileText size={13} color="var(--primary)" /> Catatan Internal MC
          </h3>
          <p style={{ fontSize: '13.5px', color: 'var(--text-2)', lineHeight: '1.6' }}>
            {booking.note || booking.notes}
          </p>
        </div>
      )}

      {/* ── SECTION 7: STACK ACTION BUTTONS (Android Parity) ── */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {outstanding > 0 && (
          <a href={waLink} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none' }}>
            <button className="btn btn-primary btn-full" style={{ background: 'var(--primary)', borderColor: 'var(--primary)', gap: '8px' }}>
              <MessageSquare size={15} /> Follow Up Piutang via WA ({formatRp(outstanding)})
            </button>
          </a>
        )}

        <button onClick={() => onOpenInvoice(booking)} className="btn btn-secondary btn-full" style={{ gap: '7px' }}>
          <Receipt size={15} /> Buat / Lihat Invoice
        </button>

        <button onClick={handleDuplicateJob} className="btn btn-secondary btn-full" style={{ gap: '7px' }}>
          <Copy size={15} /> Duplikasi Job (Repeat Client)
        </button>
      </div>


      {/* ══════════════ MODALS / DIALOGS ══════════════ */}

      {/* 1. QUICK BRIEF MODAL (Android EventBriefDialog) */}
      <Modal isOpen={activeModal === 'brief'} onClose={() => setActiveModal(null)} title="⚡ Quick Stage Brief MC">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ padding: '14px', borderRadius: '12px', background: '#FEF3C7', border: '1px solid #FCD34D' }}>
            <span style={{ fontSize: '11px', fontWeight: '800', color: '#D97706', textTransform: 'uppercase' }}>RINGKASAN CEPAT PANGGUNG</span>
            <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#92400E', marginTop: '2px' }}>{getBookingName(booking)}</h3>
            <p style={{ fontSize: '13px', color: '#B45309', marginTop: '2px' }}>Klien: {getBookingClient(booking) || 'Personal'}</p>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13.5px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              <span style={{ color: 'var(--text-3)' }}>Waktu & Tanggal</span>
              <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{formatDate(getBookingDate(booking))} ({getBookingStart(booking) || '19:00'} WIB)</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              <span style={{ color: 'var(--text-3)' }}>Lokasi Venue</span>
              <span style={{ fontWeight: '700', color: 'var(--primary)' }}>{getBookingVenue(booking) || 'Venue Event'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              <span style={{ color: 'var(--text-3)' }}>Dresscode</span>
              <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.dresscode || 'Sesuaikan'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '6px' }}>
              <span style={{ color: 'var(--text-3)' }}>Format MC</span>
              <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>{booking.mcType || 'Single'} ({booking.language || 'ID'})</span>
            </div>
          </div>

          {booking.specialRequest && (
            <div>
              <span style={{ fontSize: '11px', fontWeight: '800', color: '#D97706', textTransform: 'uppercase' }}>REQUEST KHUSUS VIP</span>
              <p style={{ fontSize: '13px', color: 'var(--text-1)', background: '#FFFBEB', padding: '10px', borderRadius: '8px', border: '1px solid #FDE68A', marginTop: '4px' }}>
                {booking.specialRequest}
              </p>
            </div>
          )}

          <button onClick={() => onOpenMcDayMode(booking)} className="btn btn-primary btn-full btn-lg" style={{ background: '#D97706', borderColor: '#D97706', gap: '6px' }}>
            <Mic size={18} /> Masuk MC Day Mode Sekarang ⚡
          </button>
        </div>
      </Modal>

      {/* 2. CATAT PEMBAYARAN MODAL */}
      <Modal isOpen={activeModal === 'payment'} onClose={() => setActiveModal(null)} title="Catat Pembayaran Masuk">
        <form onSubmit={handleInitiatePayment} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label className="input-label">Jumlah Pembayaran (Rp) *</label>
            <input
              type="number" required min="1"
              value={payAmount}
              onChange={e => setPayAmount(e.target.value ? Number(e.target.value) : '')}
              className="input-field"
              placeholder={`Sisa piutang: ${formatRp(outstanding)}`}
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
            <input type="text" value={payNote} onChange={e => setPayNote(e.target.value)} className="input-field" placeholder="Contoh: Pelunasan via Transfer BCA" />
          </div>
          <button type="submit" className="btn btn-primary btn-full btn-lg" style={{ background: '#7C3AED', borderColor: '#7C3AED', marginTop: '6px' }}>
            Lanjutkan Konfirmasi Pembayaran ➔
          </button>
        </form>
      </Modal>

      {/* 3. KONFIRMASI PEMBAYARAN MODAL (Android Payment Confirmation Dialog) */}
      <Modal isOpen={activeModal === 'paymentConfirm'} onClose={() => setActiveModal(null)} title="Konfirmasi Simpan Pembayaran">
        {pendingPayment && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ padding: '14px', borderRadius: '12px', background: '#ECFDF5', border: '1px solid #A7F3D0', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', fontWeight: '800', color: '#059669', textTransform: 'uppercase' }}>JUMLAH PEMBAYARAN</span>
              <h2 style={{ fontSize: '26px', fontWeight: '900', color: '#047857', marginTop: '2px' }}>{formatRp(pendingPayment.amount)}</h2>
              <p style={{ fontSize: '12px', color: '#065F46', marginTop: '2px' }}>Metode: {pendingPayment.method} • {pendingPayment.date}</p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '13px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Total Honor Acara:</span>
                <span style={{ fontWeight: '700' }}>{formatRp(fee)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-3)' }}>Total DP / Pembayaran Sebelumnya:</span>
                <span style={{ fontWeight: '700' }}>{formatRp(totalPaid)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '6px', borderTop: '1px solid var(--border)' }}>
                <span style={{ fontWeight: '700', color: 'var(--text-1)' }}>Sisa Piutang Setelah Pembayaran Ini:</span>
                <span style={{ fontWeight: '800', color: Math.max(0, fee - (totalPaid + pendingPayment.amount)) === 0 ? '#059669' : '#D97706' }}>
                  {formatRp(Math.max(0, fee - (totalPaid + pendingPayment.amount)))}
                </span>
              </div>
            </div>

            <button onClick={handleConfirmPayment} disabled={paySaving} className="btn btn-primary btn-full btn-lg" style={{ background: '#059669', borderColor: '#059669' }}>
              {paySaving ? 'Menyimpan...' : 'Konfirmasi & Simpan Pembayaran ✓'}
            </button>
          </div>
        )}
      </Modal>

      {/* 4. CATAT PENGELUARAN MODAL */}
      <Modal isOpen={activeModal === 'expense'} onClose={() => setActiveModal(null)} title="Catat Pengeluaran Event">
        <form onSubmit={handleSaveExpenseForm} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label className="input-label">Judul Pengeluaran *</label>
            <input type="text" required value={expTitle} onChange={e => setExpTitle(e.target.value)} className="input-field" placeholder="Contoh: Bensin & Tol Ke Venue" />
          </div>
          <div>
            <label className="input-label">Kategori Pengeluaran</label>
            <select value={expCategory} onChange={e => setExpCategory(e.target.value)} className="input-field">
              {EXPENSE_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="input-label">Jumlah Pengeluaran (Rp) *</label>
            <input type="number" required min="1" value={expAmount} onChange={e => setExpAmount(e.target.value ? Number(e.target.value) : '')} className="input-field" placeholder="0" />
          </div>
          <div>
            <label className="input-label">Tanggal</label>
            <input type="date" value={expDate} onChange={e => setExpDate(e.target.value)} className="input-field" />
          </div>
          <button type="submit" disabled={expSaving} className="btn btn-primary btn-full btn-lg">
            {expSaving ? 'Menyimpan...' : 'Simpan Pengeluaran 🚀'}
          </button>
        </form>
      </Modal>

      {/* 5. UBAH STATUS MODAL */}
      <Modal isOpen={activeModal === 'status'} onClose={() => setActiveModal(null)} title="Ubah Status Job">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {Object.entries(STATUS_LABELS).map(([key, info]) => (
            <button
              key={key}
              onClick={async () => {
                const updated = { ...booking, status: key };
                await onEdit(updated);
                setActiveModal(null);
                showToast(`Status diubah menjadi ${info.label}`);
              }}
              style={{
                padding: '12px 16px', borderRadius: '12px', border: `1.5px solid ${info.bg}`,
                background: info.bg, color: info.color, fontWeight: '800', fontSize: '13.5px',
                cursor: 'pointer', textAlign: 'left'
              }}
            >
              {info.label}
            </button>
          ))}
        </div>
      </Modal>

      {/* 6. KONFIRMASI HAPUS MODAL */}
      <Modal isOpen={activeModal === 'delete'} onClose={() => setActiveModal(null)} title="Hapus Job MC?">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <p style={{ fontSize: '13.5px', color: 'var(--text-2)', lineHeight: '1.5' }}>
            Data job <strong>{getBookingName(booking)}</strong> akan dihapus permanen dari database. Tindakan ini tidak dapat dibatalkan.
          </p>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button onClick={() => setActiveModal(null)} className="btn btn-secondary" style={{ flex: 1 }}>Batal</button>
            <button onClick={() => { onDelete(booking.id); setActiveModal(null); }} className="btn btn-destructive" style={{ flex: 1 }}>Ya, Hapus Permanen</button>
          </div>
        </div>
      </Modal>

    </div>
  );
};
