import React, { useState, useEffect, useCallback } from 'react';
import {
  ArrowLeft, Edit, Trash2, Calendar, MapPin, Clock, Users, FileText,
  CheckCircle, DollarSign, Mic, CreditCard, ChevronRight, Plus, X,
  CheckCircle2, Circle, Phone, Navigation, MessageSquare, Copy,
  AlertTriangle, Receipt, Check, ExternalLink, ChevronDown, ChevronUp
} from 'lucide-react';
import type { Booking, ChecklistItem, Payment, Expense, Client } from '../types';
import { useAuth } from '../context/AuthContext';
import {
  subscribeChecklists, saveChecklistItem, deleteChecklistItem,
  subscribeBookingPayments, savePayment, deletePayment,
  subscribeBookingExpenses, saveExpense, deleteExpense,
  saveBooking
} from '../services/firebaseService';
import { getBookingName, getBookingClient, getBookingDate, getBookingStart, getBookingFee, getBookingDp, getBookingOutstanding, getBookingVenue } from '../types';

interface BookingDetailPageProps {
  booking: Booking;
  clients?: Client[];
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

type ActiveSheet = 'edit' | 'payment' | 'expense' | 'delete' | 'status' | 'duplicate' | null;

const STATUS_LABELS: Record<string, { label: string; color: string; bg: string }> = {
  draft: { label: 'Draft', color: 'var(--text-3)', bg: 'var(--bg-surface-2)' },
  confirmed: { label: 'Confirmed', color: '#7C3AED', bg: '#F5F3FF' },
  upcoming: { label: 'Upcoming', color: '#1D4ED8', bg: '#EFF6FF' },
  today: { label: 'Hari Ini', color: '#D97706', bg: '#FFFBEB' },
  active: { label: 'Aktif', color: '#15803D', bg: '#F0FDF4' },
  completed: { label: 'Selesai', color: 'var(--success)', bg: 'var(--success-light)' },
  cancelled: { label: 'Dibatalkan', color: 'var(--error)', bg: 'var(--error-light)' },
};

const PAYMENT_METHODS = ['Transfer Bank', 'Cash', 'QRIS', 'GoPay', 'OVO', 'Dana', 'Lainnya'];
const EXPENSE_CATEGORIES = ['Transport / Bensin', 'Tol / Parkir', 'Kostum / Wardrobe', 'Makeup / Grooming', 'Konsumsi / Makan', 'Equipment / Mic', 'Laundry', 'Koordinasi / Tips', 'Lainnya'];

export const BookingDetailPage: React.FC<BookingDetailPageProps> = ({
  booking, clients = [], onBack, onEdit, onDelete, onOpenInvoice, onOpenMcDayMode,
  currentUserId
}) => {
  const { currentUser } = useAuth();
  const uid = currentUserId || currentUser?.uid || '';

  const [activeSheet, setActiveSheet] = useState<ActiveSheet>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [saveMsg, setSaveMsg] = useState('');

  // Live data subscriptions
  const [checklist, setChecklist] = useState<ChecklistItem[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [dataLoading, setDataLoading] = useState(true);

  // Checklist add input
  const [newCheckTitle, setNewCheckTitle] = useState('');
  const [showCheckInput, setShowCheckInput] = useState(false);

  // Payment form
  const [payAmount, setPayAmount] = useState<number | ''>('');
  const [payDate, setPayDate] = useState(new Date().toISOString().split('T')[0]);
  const [payMethod, setPayMethod] = useState('Transfer Bank');
  const [payNote, setPayNote] = useState('');
  const [payConfirm, setPayConfirm] = useState(false);
  const [paySaving, setPaySaving] = useState(false);

  // Expense form
  const [expTitle, setExpTitle] = useState('');
  const [expCategory, setExpCategory] = useState('Transport / Bensin');
  const [expAmount, setExpAmount] = useState<number | ''>('');
  const [expDate, setExpDate] = useState(new Date().toISOString().split('T')[0]);
  const [expNote, setExpNote] = useState('');
  const [expSaving, setExpSaving] = useState(false);

  // Edit form (simple inline edit of key fields)
  const [editForm, setEditForm] = useState({ ...booking });
  const [editSaving, setEditSaving] = useState(false);
  const [showAllDetails, setShowAllDetails] = useState(false);

  useEffect(() => {
    if (!uid || !booking.id) return;
    setDataLoading(true);
    const unsubs = [
      subscribeChecklists(uid, booking.id, (items) => {
        setChecklist(items);
        setDataLoading(false);
      }),
      subscribeBookingPayments(uid, booking.id, setPayments),
      subscribeBookingExpenses(uid, booking.id, setExpenses),
    ];
    return () => unsubs.forEach(u => u());
  }, [uid, booking.id]);

  const fee = getBookingFee(booking);
  const dpField = getBookingDp(booking);
  // Use payment records if available, fallback to booking.dp
  const totalPaid = payments.length > 0 ? payments.reduce((s, p) => s + p.amount, 0) : dpField;
  const outstanding = Math.max(0, fee - totalPaid);
  const totalExpensesAmount = expenses.reduce((s, e) => s + e.amount, 0);
  const netIncome = fee - totalExpensesAmount;
  const normalizedStatus = (booking.status || 'confirmed').toLowerCase();
  const statusInfo = STATUS_LABELS[normalizedStatus] || STATUS_LABELS.confirmed;
  const isLunas = outstanding <= 0 && fee > 0;
  const isCompleted = normalizedStatus === 'completed';
  const isCancelled = normalizedStatus === 'cancelled';

  const notify = (msg: string) => {
    setSaveMsg(msg);
    setTimeout(() => setSaveMsg(''), 2500);
  };

  // Checklist actions
  const addChecklistItem = async () => {
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
    notify('Item checklist ditambahkan.');
  };

  const toggleChecklist = async (item: ChecklistItem) => {
    await saveChecklistItem({ ...item, isCompleted: !item.isCompleted });
  };

  const removeChecklist = async (item: ChecklistItem) => {
    await deleteChecklistItem(item.id);
  };

  // Payment actions
  const handleSavePayment = async () => {
    if (!payAmount || Number(payAmount) <= 0 || !uid) return;
    setPaySaving(true);
    try {
      const p: Payment = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: uid,
        bookingId: booking.id,
        amount: Number(payAmount),
        paymentDate: payDate,
        date: payDate,
        paymentMethod: payMethod,
        note: payNote,
        createdAt: new Date().toISOString()
      };
      await savePayment(p);
      setPayAmount('');
      setPayNote('');
      setPayConfirm(false);
      setActiveSheet(null);
      notify('Pembayaran berhasil dicatat.');
    } catch (e) {
      console.error(e);
    } finally {
      setPaySaving(false);
    }
  };

  const handleDeletePayment = async (paymentId: string) => {
    await deletePayment(paymentId);
    notify('Pembayaran dihapus.');
  };

  // Expense actions
  const handleSaveExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!expAmount || Number(expAmount) <= 0 || !uid) return;
    setExpSaving(true);
    try {
      const exp: Expense = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: uid,
        bookingId: booking.id,
        title: expTitle || expCategory,
        category: expCategory,
        amount: Number(expAmount),
        date: expDate,
        note: expNote,
        createdAt: new Date().toISOString()
      };
      await saveExpense(exp);
      setExpTitle('');
      setExpAmount('');
      setExpNote('');
      setActiveSheet(null);
      notify('Pengeluaran berhasil dicatat.');
    } catch (e) {
      console.error(e);
    } finally {
      setExpSaving(false);
    }
  };

  const handleDeleteExpense = async (expenseId: string) => {
    await deleteExpense(expenseId);
    notify('Pengeluaran dihapus.');
  };

  // Status change
  const handleStatusChange = async (newStatus: string) => {
    const updated: Booking = { ...booking, status: newStatus as any, updatedAt: new Date().toISOString() };
    await saveBooking({ ...updated, ownerId: uid });
    await onEdit(updated);
    setActiveSheet(null);
    notify(`Status diubah ke: ${STATUS_LABELS[newStatus]?.label || newStatus}`);
  };

  // Edit save
  const handleEditSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setEditSaving(true);
    try {
      await onEdit({ ...editForm, ownerId: uid });
      setActiveSheet(null);
      notify('Job berhasil diperbarui.');
    } catch (err) {
      console.error(err);
    } finally {
      setEditSaving(false);
    }
  };

  // Duplicate
  const handleDuplicate = async () => {
    const newBooking: Booking = {
      ...booking,
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      status: 'confirmed',
      dp: 0,
      dp: 0,
      date: '',
      eventDate: '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    await onEdit(newBooking);
    setActiveSheet(null);
    notify('Job diduplikasi! Silakan buka untuk mengisi tanggal baru.');
  };

  const waFollowUpMsg = `Halo Kak ${getBookingClient(booking) || 'Kak'}, izin follow up terkait pembayaran untuk acara ${getBookingName(booking)}. Saat ini masih terdapat sisa pembayaran sebesar ${formatRp(outstanding)}. Mohon konfirmasinya ya. Terima kasih 🙏`;
  const picPhone = booking.pic?.replace(/\D/g, '');
  const waLink = picPhone
    ? `https://wa.me/${picPhone.startsWith('0') ? '62' + picPhone.slice(1) : picPhone}?text=${encodeURIComponent(waFollowUpMsg)}`
    : `https://wa.me/?text=${encodeURIComponent(waFollowUpMsg)}`;
  const mapsLink = `https://maps.google.com/?q=${encodeURIComponent(getBookingVenue(booking) + ' ' + (booking.address || ''))}`;

  const completedCount = checklist.filter(c => c.isCompleted).length;

  return (
    <div className="animate-fade-in" style={{ maxWidth: '900px', margin: '0 auto', paddingBottom: '32px' }}>

      {/* Toast */}
      {saveMsg && (
        <div style={{
          position: 'fixed', top: '80px', left: '50%', transform: 'translateX(-50%)',
          zIndex: 500, background: 'var(--success)', color: 'white', padding: '10px 20px',
          borderRadius: '30px', fontWeight: '700', fontSize: '13px', boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
          display: 'flex', alignItems: 'center', gap: '8px'
        }}>
          <Check size={15} /> {saveMsg}
        </div>
      )}

      {/* ── TOP NAV ── */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
        <button onClick={onBack} className="btn btn-ghost" style={{ padding: '0 8px', marginLeft: '-8px', gap: '6px' }}>
          <ArrowLeft size={16} /> Kembali
        </button>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button onClick={() => { setEditForm({ ...booking }); setActiveSheet('edit'); }} className="btn btn-secondary btn-sm" style={{ gap: '5px' }}>
            <Edit size={13} /> Edit
          </button>
          <button onClick={() => setActiveSheet('status')} className="btn btn-secondary btn-sm" style={{ gap: '5px' }}>
            Status
          </button>
          <button onClick={() => setActiveSheet('delete')} className="btn btn-destructive btn-sm" style={{ padding: '0 10px' }}>
            <Trash2 size={14} />
          </button>
        </div>
      </div>

      {/* ── HEADER CARD ── */}
      <div className="card" style={{ padding: '24px', marginBottom: '20px', position: 'relative', overflow: 'hidden', borderLeft: `4px solid ${isCompleted ? 'var(--success)' : isCancelled ? 'var(--error)' : 'var(--primary)'}` }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px', flexWrap: 'wrap' }}>
          <span className="badge badge-primary">🎤 {booking.category || 'Wedding'}</span>
          <span style={{ padding: '3px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', color: statusInfo.color, background: statusInfo.bg }}>
            {statusInfo.label.toUpperCase()}
          </span>
          {fee > 0 && (
            <span style={{ padding: '3px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', color: isLunas ? 'var(--success)' : outstanding > 0 ? 'var(--warning)' : 'var(--text-3)', background: isLunas ? 'var(--success-light)' : 'var(--warning-light)' }}>
              {isLunas ? '✓ LUNAS' : `Piutang ${formatRp(outstanding)}`}
            </span>
          )}
        </div>
        <h1 style={{ fontSize: 'clamp(20px, 3vw, 26px)', fontWeight: '800', color: 'var(--text-1)', letterSpacing: '-0.02em', marginBottom: '6px' }}>
          {getBookingName(booking)}
        </h1>
        <p style={{ fontSize: '14px', color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Users size={14} /> Klien: <strong style={{ color: 'var(--text-2)' }}>{getBookingClient(booking) || '-'}</strong>
        </p>
      </div>

      {/* ── ACTION BUTTONS ── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '10px', marginBottom: '20px' }}>
        {/* MC Day Mode */}
        <button onClick={() => onOpenMcDayMode(booking)} className="card card-interactive" style={{ padding: '14px 16px', display: 'flex', alignItems: 'center', gap: '10px', background: 'var(--primary-light)', borderColor: 'rgba(79,70,229,0.2)' }}>
          <div style={{ width: '34px', height: '34px', borderRadius: '9px', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <Mic size={17} color="white" />
          </div>
          <div style={{ textAlign: 'left' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--primary)', display: 'block' }}>MC Day Mode</span>
            <span style={{ fontSize: '10px', color: 'var(--text-3)' }}>Rundown Hari H</span>
          </div>
        </button>

        {/* Invoice */}
        <button onClick={() => onOpenInvoice(booking)} className="card card-interactive" style={{ padding: '14px 16px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '34px', height: '34px', borderRadius: '9px', background: 'var(--warning-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <Receipt size={17} color="var(--warning)" />
          </div>
          <div style={{ textAlign: 'left' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-1)', display: 'block' }}>Buat Invoice</span>
            <span style={{ fontSize: '10px', color: 'var(--text-3)' }}>PDF Tagihan</span>
          </div>
        </button>

        {/* Follow Up WA */}
        {outstanding > 0 && (
          <a href={waLink} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none' }}>
            <button className="card card-interactive" style={{ padding: '14px 16px', display: 'flex', alignItems: 'center', gap: '10px', width: '100%', background: 'rgba(37,211,102,0.08)', borderColor: 'rgba(37,211,102,0.25)' }}>
              <div style={{ width: '34px', height: '34px', borderRadius: '9px', background: 'rgba(37,211,102,0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                <MessageSquare size={17} color="#25D366" />
              </div>
              <div style={{ textAlign: 'left' }}>
                <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-1)', display: 'block' }}>Follow Up WA</span>
                <span style={{ fontSize: '10px', color: 'var(--text-3)' }}>Piutang {formatRp(outstanding)}</span>
              </div>
            </button>
          </a>
        )}

        {/* Duplicate */}
        <button onClick={() => setActiveSheet('duplicate')} className="card card-interactive" style={{ padding: '14px 16px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '34px', height: '34px', borderRadius: '9px', background: 'var(--bg-surface-2)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <Copy size={17} color="var(--text-3)" />
          </div>
          <div style={{ textAlign: 'left' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-1)', display: 'block' }}>Duplikasi Job</span>
            <span style={{ fontSize: '10px', color: 'var(--text-3)' }}>Klien repeat</span>
          </div>
        </button>
      </div>

      {/* ── TWO COLUMN LAYOUT ── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }} className="detail-two-col">

        {/* LEFT COLUMN */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

          {/* 1. CHECKLIST */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <CheckCircle size={15} color="var(--primary)" />
                <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)' }}>Persiapan Acara</h3>
                {checklist.length > 0 && (
                  <span style={{ fontSize: '11px', fontWeight: '600', color: 'var(--primary)', background: 'var(--primary-light)', padding: '2px 8px', borderRadius: '10px' }}>
                    {completedCount}/{checklist.length}
                  </span>
                )}
              </div>
              <button onClick={() => setShowCheckInput(v => !v)} className="btn btn-ghost btn-sm" style={{ gap: '4px', fontSize: '12px' }}>
                <Plus size={13} /> Tambah
              </button>
            </div>
            {checklist.length > 0 && (
              <div style={{ padding: '6px 16px', borderBottom: '1px solid var(--border)' }}>
                <div style={{ height: '5px', background: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}>
                  <div style={{ height: '100%', background: completedCount === checklist.length ? 'var(--success)' : 'var(--primary)', width: `${checklist.length > 0 ? (completedCount / checklist.length) * 100 : 0}%`, transition: 'width 0.3s ease', borderRadius: '3px' }} />
                </div>
              </div>
            )}
            <div style={{ padding: checklist.length === 0 && !showCheckInput ? '24px' : '8px 16px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
              {checklist.length === 0 && !showCheckInput && (
                <div style={{ textAlign: 'center', padding: '8px 0' }}>
                  <p style={{ fontSize: '13px', color: 'var(--text-3)', marginBottom: '12px' }}>Belum ada item persiapan</p>
                  <button onClick={() => setShowCheckInput(true)} className="btn btn-secondary btn-sm" style={{ gap: '4px' }}>
                    <Plus size={13} /> Tambah Item
                  </button>
                </div>
              )}
              {checklist.map(item => (
                <div key={item.id} style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '6px 0', borderBottom: '1px solid var(--border)', minHeight: '40px' }}>
                  <button onClick={() => toggleChecklist(item)} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, flexShrink: 0 }}>
                    {item.isCompleted
                      ? <CheckCircle2 size={20} color="var(--success)" />
                      : <Circle size={20} color="var(--border)" />}
                  </button>
                  <span style={{ flex: 1, fontSize: '13px', fontWeight: item.isCompleted ? '400' : '600', color: item.isCompleted ? 'var(--text-4)' : 'var(--text-1)', textDecoration: item.isCompleted ? 'line-through' : 'none' }}>
                    {item.title}
                  </span>
                  <button onClick={() => removeChecklist(item)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-4)', padding: '0 4px' }}>
                    <X size={14} />
                  </button>
                </div>
              ))}
              {showCheckInput && (
                <div style={{ display: 'flex', gap: '8px', padding: '8px 0', alignItems: 'center' }}>
                  <input
                    autoFocus type="text" value={newCheckTitle}
                    onChange={e => setNewCheckTitle(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); addChecklistItem(); } if (e.key === 'Escape') setShowCheckInput(false); }}
                    className="input-field" style={{ flex: 1 }}
                    placeholder="Contoh: Cek cue card, konfirmasi sound..."
                  />
                  <button onClick={addChecklistItem} className="btn btn-primary btn-sm">Simpan</button>
                  <button onClick={() => { setShowCheckInput(false); setNewCheckTitle(''); }} className="btn btn-ghost btn-sm"><X size={14} /></button>
                </div>
              )}
            </div>
          </div>

          {/* 2. KEUANGAN */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <DollarSign size={15} color="var(--primary)" />
                <h3 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)' }}>Keuangan & Net Income</h3>
              </div>
            </div>
            <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '13px', color: 'var(--text-3)' }}>Total Honor</span>
                <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--text-1)' }}>{formatRp(fee)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '13px', color: 'var(--text-3)' }}>Total Terbayar</span>
                <span style={{ fontSize: '14px', fontWeight: '600', color: 'var(--success)' }}>{formatRp(totalPaid)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '8px', borderTop: '1px solid var(--border)' }}>
                <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-1)' }}>Sisa Piutang</span>
                {isLunas
                  ? <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--success)', background: 'var(--success-light)', padding: '2px 10px', borderRadius: '8px' }}>Rp 0 · LUNAS ✓</span>
                  : <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--warning)' }}>{formatRp(outstanding)}</span>}
              </div>
              {expenses.length > 0 && (
                <>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-3)' }}>Total Pengeluaran</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: 'var(--error)' }}>- {formatRp(totalExpensesAmount)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', borderRadius: '10px', background: 'var(--primary-light)' }}>
                    <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--primary)' }}>NET INCOME</span>
                    <span style={{ fontSize: '15px', fontWeight: '800', color: 'var(--primary)' }}>{formatRp(netIncome)}</span>
                  </div>
                </>
              )}
              {/* Action Buttons */}
              <div style={{ display: 'flex', gap: '8px', paddingTop: '8px' }}>
                {!isLunas && fee > 0 && (
                  <button onClick={() => setActiveSheet('payment')} className="btn btn-primary" style={{ flex: 1, fontSize: '12px', gap: '5px' }}>
                    <CreditCard size={13} /> Catat Pembayaran
                  </button>
                )}
                <button onClick={() => setActiveSheet('expense')} className="btn btn-secondary" style={{ flex: 1, fontSize: '12px', gap: '5px' }}>
                  <Receipt size={13} /> Catat Pengeluaran
                </button>
              </div>
            </div>

            {/* Payment History */}
            {payments.length > 0 && (
              <div style={{ borderTop: '1px solid var(--border)', padding: '12px 16px' }}>
                <p style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-4)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '10px' }}>Riwayat Pembayaran</p>
                {payments.map(p => (
                  <div key={p.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '7px 0', borderBottom: '1px solid var(--border)' }}>
                    <div>
                      <p style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-1)' }}>{p.paymentMethod}</p>
                      <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{p.paymentDate} {p.note ? `· ${p.note}` : ''}</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--success)' }}>{formatRp(p.amount)}</span>
                      <button onClick={() => handleDeletePayment(p.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-4)', padding: '0 2px' }}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Expense History */}
            {expenses.length > 0 && (
              <div style={{ borderTop: '1px solid var(--border)', padding: '12px 16px' }}>
                <p style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-4)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '10px' }}>Riwayat Pengeluaran</p>
                {expenses.map(exp => (
                  <div key={exp.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '7px 0', borderBottom: '1px solid var(--border)' }}>
                    <div>
                      <p style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-1)' }}>{exp.title}</p>
                      <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{exp.category} · {exp.date}</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--error)' }}>- {formatRp(exp.amount)}</span>
                      <button onClick={() => handleDeleteExpense(exp.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-4)', padding: '0 2px' }}>
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* RIGHT COLUMN */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

          {/* 3. JADWAL & LOKASI */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)', background: 'var(--bg-surface-2)' }}>
              <h3 style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-1)', textTransform: 'uppercase', letterSpacing: '0.06em', display: 'flex', alignItems: 'center', gap: '7px' }}>
                <Calendar size={13} color="var(--primary)" /> Jadwal & Lokasi
              </h3>
            </div>
            <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <DetailRow icon={<Calendar size={14} />} label="Tanggal" value={formatDate(getBookingDate(booking))} />
              <DetailRow icon={<Clock size={14} />} label="Waktu" value={`${getBookingStart(booking) || '?'}${booking.end ? ` – ${booking.end}` : ''} WIB`} />
              {getBookingVenue(booking) && (
                <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start' }}>
                  <div style={{ color: 'var(--text-4)', marginTop: '2px', flexShrink: 0 }}><MapPin size={14} /></div>
                  <div style={{ flex: 1 }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-4)', display: 'block', marginBottom: '2px' }}>Venue</span>
                    <a href={mapsLink} target="_blank" rel="noopener noreferrer" style={{ fontSize: '13px', fontWeight: '600', color: 'var(--primary)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '5px' }}>
                      {getBookingVenue(booking)} <ExternalLink size={11} />
                    </a>
                    {booking.address && <p style={{ fontSize: '11px', color: 'var(--text-3)', marginTop: '2px', lineHeight: '1.4' }}>{booking.address}</p>}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* 4. SPESIFIKASI MC */}
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--border)', background: 'var(--bg-surface-2)' }}>
              <h3 style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-1)', textTransform: 'uppercase', letterSpacing: '0.06em', display: 'flex', alignItems: 'center', gap: '7px' }}>
                <Mic size={13} color="var(--primary)" /> Spesifikasi MC
              </h3>
            </div>
            <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {booking.mcType && <DetailRow icon={<Mic size={14} />} label="Format MC" value={booking.mcType} />}
              {booking.language && <DetailRow icon={<Users size={14} />} label="Bahasa" value={booking.language} />}
              {booking.dresscode && <DetailRow icon={<FileText size={14} />} label="Dresscode" value={booking.dresscode} />}
              {booking.theme && <DetailRow icon={<FileText size={14} />} label="Tema" value={booking.theme} />}
              {booking.audience && <DetailRow icon={<Users size={14} />} label="Estimasi Tamu" value={booking.audience} />}
              {booking.pic && (
                <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start' }}>
                  <div style={{ color: 'var(--text-4)', marginTop: '2px', flexShrink: 0 }}><Phone size={14} /></div>
                  <div style={{ flex: 1 }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-4)', display: 'block', marginBottom: '2px' }}>Kontak PIC / WO</span>
                    <a
                      href={`https://wa.me/${booking.pic.replace(/\D/g, '').replace(/^0/, '62')}`}
                      target="_blank" rel="noopener noreferrer"
                      style={{ fontSize: '13px', fontWeight: '600', color: 'var(--primary)', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '5px' }}
                    >
                      {booking.pic} <ExternalLink size={11} />
                    </a>
                  </div>
                </div>
              )}

              {/* Show more toggle */}
              {(booking.specialRequest || booking.note || booking.notes) && (
                <button onClick={() => setShowAllDetails(v => !v)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '12px', fontWeight: '600', color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '4px', padding: 0, marginTop: '4px' }}>
                  {showAllDetails ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                  {showAllDetails ? 'Sembunyikan detail' : 'Lihat semua detail'}
                </button>
              )}
            </div>

            {showAllDetails && (
              <div style={{ padding: '0 16px 16px', display: 'flex', flexDirection: 'column', gap: '14px', borderTop: '1px solid var(--border)' }}>
                {booking.specialRequest && (
                  <div style={{ marginTop: '14px' }}>
                    <p style={{ fontSize: '11px', fontWeight: '700', color: 'var(--warning)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '6px' }}>⚡ Request Khusus / Protokol VIP</p>
                    <div style={{ padding: '10px 12px', borderRadius: '8px', background: 'rgba(245,158,11,0.08)', border: '1px solid rgba(245,158,11,0.2)', fontSize: '13px', color: 'var(--text-2)', lineHeight: '1.5' }}>
                      {booking.specialRequest}
                    </div>
                  </div>
                )}
                {(booking.note || booking.notes) && (
                  <div>
                    <p style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-4)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '6px' }}>📝 Catatan Internal MC</p>
                    <div style={{ padding: '10px 12px', borderRadius: '8px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)', fontSize: '13px', color: 'var(--text-2)', lineHeight: '1.5' }}>
                      {booking.note || booking.notes}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ══════════════ MODALS / SHEETS ══════════════ */}

      {/* STATUS SHEET */}
      {activeSheet === 'status' && (
        <ModalOverlay onClose={() => setActiveSheet(null)} title="Ubah Status Job">
          <p style={{ fontSize: '13px', color: 'var(--text-3)', marginBottom: '16px' }}>Status saat ini: <strong style={{ color: statusInfo.color }}>{statusInfo.label}</strong></p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {Object.entries(STATUS_LABELS).map(([key, info]) => (
              key !== normalizedStatus && (
                <button key={key} onClick={() => handleStatusChange(key)}
                  style={{ padding: '12px 16px', borderRadius: '10px', border: `1.5px solid ${info.bg}`, background: info.bg, color: info.color, fontWeight: '700', fontSize: '13px', cursor: 'pointer', textAlign: 'left' }}>
                  {info.label}
                </button>
              )
            ))}
          </div>
        </ModalOverlay>
      )}

      {/* PAYMENT SHEET */}
      {activeSheet === 'payment' && (
        <ModalOverlay onClose={() => { setActiveSheet(null); setPayConfirm(false); }} title="Catat Pembayaran">
          {!payConfirm ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ padding: '12px', borderRadius: '10px', background: 'var(--bg-surface-2)', display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>Sisa Piutang</span>
                <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--warning)' }}>{formatRp(outstanding)}</span>
              </div>
              <div>
                <label className="input-label">Nominal Pembayaran (Rp) *</label>
                <input type="number" min="1" max={outstanding} autoFocus required
                  value={payAmount} onChange={e => setPayAmount(Number(e.target.value))}
                  className="input-field" style={{ fontWeight: '700', fontSize: '18px', color: 'var(--success)' }}
                  placeholder="0"
                />
                <p className="input-hint">Sisa setelah pembayaran: {formatRp(Math.max(0, outstanding - (Number(payAmount) || 0)))}</p>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
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
              </div>
              <div>
                <label className="input-label">Catatan (Opsional)</label>
                <input type="text" value={payNote} onChange={e => setPayNote(e.target.value)} className="input-field" placeholder="Pelunasan, cicilan ke-2, dll." />
              </div>
              <button disabled={!payAmount || Number(payAmount) <= 0} onClick={() => setPayConfirm(true)} className="btn btn-primary btn-lg btn-full">
                Lanjutkan
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ padding: '16px', borderRadius: '12px', background: 'var(--success-light)', border: '1px solid rgba(5,150,105,0.2)', textAlign: 'center' }}>
                <p style={{ fontSize: '12px', color: 'var(--text-3)', marginBottom: '4px' }}>Jumlah Pembayaran</p>
                <p style={{ fontSize: '28px', fontWeight: '800', color: 'var(--success)' }}>{formatRp(Number(payAmount))}</p>
                <p style={{ fontSize: '12px', color: 'var(--text-3)', marginTop: '4px' }}>{payMethod} · {payDate}</p>
              </div>
              {Number(payAmount) > outstanding && (
                <div style={{ padding: '10px', borderRadius: '8px', background: 'var(--warning-light)', border: '1px solid rgba(245,158,11,0.2)', fontSize: '12px', color: 'var(--warning)', display: 'flex', gap: '8px' }}>
                  <AlertTriangle size={14} style={{ flexShrink: 0, marginTop: '1px' }} />
                  Pembayaran melebihi sisa piutang ({formatRp(outstanding)}). Akan dianggap lunas.
                </div>
              )}
              <div style={{ display: 'flex', gap: '10px' }}>
                <button onClick={() => setPayConfirm(false)} className="btn btn-secondary" style={{ flex: 1 }}>Kembali</button>
                <button onClick={handleSavePayment} disabled={paySaving} className="btn btn-primary" style={{ flex: 2 }}>
                  {paySaving ? 'Menyimpan...' : '✓ Konfirmasi Pembayaran'}
                </button>
              </div>
            </div>
          )}
        </ModalOverlay>
      )}

      {/* EXPENSE SHEET */}
      {activeSheet === 'expense' && (
        <ModalOverlay onClose={() => setActiveSheet(null)} title="Catat Pengeluaran">
          <form onSubmit={handleSaveExpense} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div>
              <label className="input-label">Keterangan Pengeluaran</label>
              <input type="text" autoFocus value={expTitle} onChange={e => setExpTitle(e.target.value)} className="input-field" placeholder="Contoh: Bensin Tol, Laundry Jas..." />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Kategori</label>
                <select value={expCategory} onChange={e => setExpCategory(e.target.value)} className="input-field">
                  {EXPENSE_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div>
                <label className="input-label">Nominal (Rp) *</label>
                <input type="number" required min="1" value={expAmount} onChange={e => setExpAmount(Number(e.target.value))} className="input-field" style={{ fontWeight: '700', color: 'var(--error)' }} placeholder="0" />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Tanggal</label>
                <input type="date" value={expDate} onChange={e => setExpDate(e.target.value)} className="input-field" />
              </div>
              <div>
                <label className="input-label">Catatan</label>
                <input type="text" value={expNote} onChange={e => setExpNote(e.target.value)} className="input-field" placeholder="Opsional..." />
              </div>
            </div>
            <button type="submit" disabled={expSaving} className="btn btn-primary btn-lg btn-full">
              {expSaving ? 'Menyimpan...' : 'Simpan Pengeluaran'}
            </button>
          </form>
        </ModalOverlay>
      )}

      {/* EDIT SHEET */}
      {activeSheet === 'edit' && (
        <ModalOverlay onClose={() => setActiveSheet(null)} title="Edit Job" wide>
          <form onSubmit={handleEditSave} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Nama Klien</label>
                <input type="text" value={editForm.client || editForm.clientName || ''} onChange={e => setEditForm(f => ({ ...f, client: e.target.value, clientName: e.target.value }))} className="input-field" />
              </div>
              <div>
                <label className="input-label">Nama Acara</label>
                <input type="text" value={editForm.name || editForm.eventTitle || ''} onChange={e => setEditForm(f => ({ ...f, name: e.target.value, eventTitle: e.target.value }))} className="input-field" />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Tanggal</label>
                <input type="date" value={editForm.date || editForm.eventDate || ''} onChange={e => setEditForm(f => ({ ...f, date: e.target.value, eventDate: e.target.value }))} className="input-field" />
              </div>
              <div>
                <label className="input-label">Mulai</label>
                <input type="time" value={editForm.start || editForm.eventTime || ''} onChange={e => setEditForm(f => ({ ...f, start: e.target.value, eventTime: e.target.value }))} className="input-field" />
              </div>
              <div>
                <label className="input-label">Selesai</label>
                <input type="time" value={editForm.end || ''} onChange={e => setEditForm(f => ({ ...f, end: e.target.value }))} className="input-field" />
              </div>
            </div>
            <div>
              <label className="input-label">Venue</label>
              <input type="text" value={editForm.location || editForm.venue || ''} onChange={e => setEditForm(f => ({ ...f, location: e.target.value, venue: e.target.value }))} className="input-field" />
            </div>
            <div>
              <label className="input-label">Alamat Lengkap</label>
              <input type="text" value={editForm.address || ''} onChange={e => setEditForm(f => ({ ...f, address: e.target.value }))} className="input-field" />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Dresscode</label>
                <input type="text" value={editForm.dresscode || ''} onChange={e => setEditForm(f => ({ ...f, dresscode: e.target.value }))} className="input-field" />
              </div>
              <div>
                <label className="input-label">Tema</label>
                <input type="text" value={editForm.theme || ''} onChange={e => setEditForm(f => ({ ...f, theme: e.target.value }))} className="input-field" />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Format MC</label>
                <select value={editForm.mcType || 'Single'} onChange={e => setEditForm(f => ({ ...f, mcType: e.target.value }))} className="input-field">
                  {['Single', 'Duet', 'Tim (3+)'].map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="input-label">Bahasa</label>
                <select value={editForm.language || 'Bahasa Indonesia'} onChange={e => setEditForm(f => ({ ...f, language: e.target.value }))} className="input-field">
                  {['Bahasa Indonesia', 'English', 'Bilingual (Indonesia + English)', 'Bahasa Daerah'].map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label className="input-label">Total Honor (Rp)</label>
                <input type="number" min="0" value={editForm.fee || ''} onChange={e => setEditForm(f => ({ ...f, fee: Number(e.target.value) }))} className="input-field" style={{ fontWeight: '700', color: 'var(--primary)' }} />
              </div>
              <div>
                <label className="input-label">DP Diterima (Rp)</label>
                <input type="number" min="0" value={editForm.dp || ''} onChange={e => setEditForm(f => ({ ...f, dp: Number(e.target.value) }))} className="input-field" style={{ fontWeight: '600', color: 'var(--success)' }} />
              </div>
            </div>
            <div>
              <label className="input-label">Kontak PIC / WO</label>
              <input type="tel" value={editForm.pic || ''} onChange={e => setEditForm(f => ({ ...f, pic: e.target.value }))} className="input-field" />
            </div>
            <div>
              <label className="input-label">Audience / Tamu</label>
              <input type="text" value={editForm.audience || ''} onChange={e => setEditForm(f => ({ ...f, audience: e.target.value }))} className="input-field" />
            </div>
            <div>
              <label className="input-label">Request Khusus</label>
              <textarea value={editForm.specialRequest || ''} onChange={e => setEditForm(f => ({ ...f, specialRequest: e.target.value }))} className="input-field" style={{ height: '70px', padding: '10px 12px', resize: 'none' }} />
            </div>
            <div>
              <label className="input-label">Catatan Internal MC</label>
              <textarea value={editForm.note || editForm.notes || ''} onChange={e => setEditForm(f => ({ ...f, note: e.target.value, notes: e.target.value }))} className="input-field" style={{ height: '70px', padding: '10px 12px', resize: 'none' }} />
            </div>
            <div style={{ display: 'flex', gap: '10px', paddingTop: '8px', borderTop: '1px solid var(--border)' }}>
              <button type="button" onClick={() => setActiveSheet(null)} className="btn btn-secondary" style={{ flex: 1 }}>Batal</button>
              <button type="submit" disabled={editSaving} className="btn btn-primary" style={{ flex: 2 }}>
                {editSaving ? 'Menyimpan...' : 'Simpan Perubahan'}
              </button>
            </div>
          </form>
        </ModalOverlay>
      )}

      {/* DELETE CONFIRM */}
      {activeSheet === 'delete' && (
        <ModalOverlay onClose={() => setActiveSheet(null)} title="">
          <div style={{ textAlign: 'center', padding: '8px 0' }}>
            <div style={{ width: '52px', height: '52px', borderRadius: '14px', background: 'var(--error-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
              <Trash2 size={26} color="var(--error)" />
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '8px' }}>Hapus Job Ini?</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-3)', lineHeight: '1.6', marginBottom: '24px' }}>
              Data <strong>{getBookingName(booking)}</strong> akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.
            </p>
            <div style={{ display: 'flex', gap: '12px' }}>
              <button onClick={() => setActiveSheet(null)} className="btn btn-secondary" style={{ flex: 1 }}>Batal</button>
              <button
                disabled={isDeleting}
                onClick={async () => {
                  setIsDeleting(true);
                  await onDelete(booking.id);
                  setActiveSheet(null);
                }}
                className="btn btn-destructive" style={{ flex: 1 }}
              >
                {isDeleting ? 'Menghapus...' : 'Ya, Hapus'}
              </button>
            </div>
          </div>
        </ModalOverlay>
      )}

      {/* DUPLICATE CONFIRM */}
      {activeSheet === 'duplicate' && (
        <ModalOverlay onClose={() => setActiveSheet(null)} title="">
          <div style={{ textAlign: 'center', padding: '8px 0' }}>
            <div style={{ width: '52px', height: '52px', borderRadius: '14px', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
              <Copy size={26} color="var(--primary)" />
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '8px' }}>Duplikasi Job?</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-3)', lineHeight: '1.6', marginBottom: '24px' }}>
              Job <strong>{getBookingName(booking)}</strong> akan diduplikasi. Tanggal dan pembayaran akan dikosongkan. Cocok untuk klien repeat order.
            </p>
            <div style={{ display: 'flex', gap: '12px' }}>
              <button onClick={() => setActiveSheet(null)} className="btn btn-secondary" style={{ flex: 1 }}>Batal</button>
              <button onClick={handleDuplicate} className="btn btn-primary" style={{ flex: 1 }}>Duplikasi</button>
            </div>
          </div>
        </ModalOverlay>
      )}

      <style>{`
        @media (min-width: 768px) {
          .detail-two-col { grid-template-columns: 1fr 340px !important; }
        }
      `}</style>
    </div>
  );
};

// ── Helper Components ──────────────────────────────────────────

const DetailRow: React.FC<{ icon: React.ReactNode; label: string; value: string }> = ({ icon, label, value }) => (
  <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start' }}>
    <div style={{ color: 'var(--text-4)', marginTop: '2px', flexShrink: 0 }}>{icon}</div>
    <div>
      <span style={{ fontSize: '11px', color: 'var(--text-4)', display: 'block', marginBottom: '2px' }}>{label}</span>
      <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-1)' }}>{value || '-'}</span>
    </div>
  </div>
);

const ModalOverlay: React.FC<{ onClose: () => void; title: string; children: React.ReactNode; wide?: boolean }> = ({ onClose, title, children, wide }) => (
  <div
    onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    style={{
      position: 'fixed', inset: 0, zIndex: 300, background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)',
      display: 'flex', alignItems: 'flex-end', justifyContent: 'center', padding: '0'
    }}
  >
    <div style={{
      background: 'var(--bg-surface)', borderRadius: '20px 20px 0 0', width: '100%',
      maxWidth: wide ? '700px' : '520px', maxHeight: '92vh', overflowY: 'auto',
      padding: '0 0 env(safe-area-inset-bottom, 16px)',
      boxShadow: '0 -8px 40px rgba(0,0,0,0.2)'
    }}>
      <div style={{ padding: '16px 20px', borderBottom: title ? '1px solid var(--border)' : 'none', display: 'flex', alignItems: 'center', justifyContent: 'space-between', position: 'sticky', top: 0, background: 'var(--bg-surface)', zIndex: 10 }}>
        {title && <h2 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-1)' }}>{title}</h2>}
        <button onClick={onClose} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-4)', padding: '4px' }}>
          <X size={20} />
        </button>
      </div>
      <div style={{ padding: '20px' }}>{children}</div>
    </div>
  </div>
);
