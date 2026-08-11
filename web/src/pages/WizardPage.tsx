import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import {
  ArrowLeft, Save, Calendar, User, DollarSign, MapPin,
  AlignLeft, CheckCircle2, Clock, Mic, Phone, Tag,
  Users, ChevronLeft, ChevronRight, X, AlertTriangle, ExternalLink, Sparkles, Building2, Globe, Shirt, Palette
} from 'lucide-react';
import type { Booking, Client, Payment, RateCard } from '../types';
import { useAuth } from '../context/AuthContext';

interface WizardPageProps {
  onClose: () => void;
  onSave: (booking: Booking) => Promise<void>;
  onSaveClient?: (client: Client) => Promise<void>;
  onSavePayment?: (payment: Payment) => Promise<void>;
  clients?: Client[];
  existingBookings?: Booking[];
  rateCards?: RateCard[];
  initialRateCard?: RateCard | null;
}

const CATEGORY_PRESETS = ['Wedding', 'Corporate', 'Birthday', 'Seminar', 'Concert', 'Gathering'];
const TIME_SLOT_PRESETS = [
  { label: 'Pagi (08:00 - 12:00)', start: '08:00', end: '12:00' },
  { label: 'Siang (13:00 - 17:00)', start: '13:00', end: '17:00' },
  { label: 'Malam (19:00 - 22:00)', start: '19:00', end: '22:00' },
  { label: 'Seharian (08:00 - 22:00)', start: '08:00', end: '22:00' },
];

const DRESSCODE_PRESETS = ['Black Tie', 'Batik Modern', 'Formal Suit', 'Smart Casual', 'White Elegance'];
const THEME_PRESETS = ['Modern Elegance', 'Traditional Formal', 'Glamour Night', 'Casual Outdoor'];
const MC_TYPE_PRESETS = ['Single', 'Duet', 'Group'];
const LANGUAGE_PRESETS = ['Bahasa Indonesia', 'Bilingual (Indo-English)', 'English'];

function formatRp(val: number) {
  return `Rp ${val.toLocaleString('id-ID')}`;
}

export const WizardPage: React.FC<WizardPageProps> = ({
  onClose,
  onSave,
  onSaveClient,
  onSavePayment,
  clients = [],
  existingBookings = [],
  rateCards = [],
  initialRateCard
}) => {
  const { currentUser, userProfile } = useAuth();
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Dialog states
  const [showConflictDialog, setShowConflictDialog] = useState(false);
  const [conflictJobs, setConflictJobs] = useState<Booking[]>([]);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [savedBooking, setSavedBooking] = useState<Booking | null>(null);

  // Section 1 — Info Acara
  const [eventName, setEventName] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [category, setCategory] = useState('Wedding');
  const [customCategory, setCustomCategory] = useState('');
  const [startTime, setStartTime] = useState('19:00');
  const [endTime, setEndTime] = useState('22:00');
  const [location, setLocation] = useState('');
  const [address, setAddress] = useState('');

  // Section 2 — Info Klien & WO
  const [clientInput, setClientInput] = useState('');
  const [clientId, setClientId] = useState('');
  const [clientPhone, setClientPhone] = useState('');
  const [clientCompany, setClientCompany] = useState('');
  const [pic, setPic] = useState('');
  const [showClientDropdown, setShowClientDropdown] = useState(false);

  // Section 3 — Detail MC
  const [dresscode, setDresscode] = useState('');
  const [theme, setTheme] = useState('');
  const [mcType, setMcType] = useState('Single');
  const [language, setLanguage] = useState('Bahasa Indonesia');
  const [audience, setAudience] = useState('');
  const [specialRequest, setSpecialRequest] = useState('');

  // Section 4 — Keuangan & Honor MC
  const [fee, setFee] = useState<number | ''>(userProfile?.baseFee || '');
  const [dp, setDp] = useState<number | ''>(() => {
    if (userProfile?.baseFee && userProfile?.defaultDpPercentage) {
      return Math.round((userProfile.baseFee * userProfile.defaultDpPercentage) / 100);
    }
    return '';
  });

  // Section 5 — Catatan Brief
  const [note, setNote] = useState(userProfile?.termsAndConditions || '');

  // Pre-fill asynchronously loaded user profile defaults
  useEffect(() => {
    if (userProfile) {
      if (fee === '' && userProfile.baseFee) {
        setFee(userProfile.baseFee);
        if (userProfile.defaultDpPercentage) {
          setDp(Math.round((userProfile.baseFee * userProfile.defaultDpPercentage) / 100));
        }
      }
      if (!note && userProfile.termsAndConditions) {
        setNote(userProfile.termsAndConditions);
      }
    }
  }, [userProfile]);

  // Pre-fill initial rate card if provided
  useEffect(() => {
    if (initialRateCard) {
      if (initialRateCard.category) setCategory(initialRateCard.category);
      if (initialRateCard.price) {
        setFee(initialRateCard.price);
        if (userProfile?.defaultDpPercentage) {
          setDp(Math.round((initialRateCard.price * userProfile.defaultDpPercentage) / 100));
        }
      }
    }
  }, [initialRateCard]);

  const TOTAL_STEPS = 5;
  const steps = [
    { num: 1, title: 'Informasi Acara', icon: Calendar },
    { num: 2, title: 'Klien & WO', icon: User },
    { num: 3, title: 'Spesifikasi MC', icon: Mic },
    { num: 4, title: 'Keuangan & Honor', icon: DollarSign },
    { num: 5, title: 'Brief & Rundown', icon: AlignLeft },
  ];

  // Client autocomplete
  const filteredClients = clients.filter(c =>
    c.name.toLowerCase().includes(clientInput.toLowerCase()) ||
    (c.company && c.company.toLowerCase().includes(clientInput.toLowerCase()))
  ).slice(0, 5);

  const selectClient = (c: Client) => {
    setClientInput(c.name);
    setClientId(c.id);
    setClientPhone(c.phone || '');
    setClientCompany(c.company || '');
    setPic(c.pic || c.phone || '');
    setShowClientDropdown(false);
  };

  // Financial calculations
  const feeVal = Number(fee) || 0;
  const dpVal = Number(dp) || 0;
  const remaining = Math.max(0, feeVal - dpVal);
  const isPaid = feeVal > 0 && dpVal >= feeVal;

  const validateCurrentStep = (): string => {
    if (step === 1) {
      if (!eventName.trim()) return 'Nama Acara / Event wajib diisi.';
      if (!date) return 'Tanggal Acara wajib diisi.';
      if (!startTime || !endTime) return 'Jam Mulai dan Jam Selesai wajib diisi.';
      if (endTime <= startTime) return 'Waktu Selesai harus lebih dari Waktu Mulai.';
    }
    if (step === 2) {
      if (!clientInput.trim()) return 'Nama Klien / Penyelenggara wajib diisi.';
    }
    if (step === 4) {
      if (fee === '') return 'Total Honor MC wajib diisi.';
      if (feeVal < 0) return 'Total Honor MC tidak boleh kurang dari Rp 0.';
      if (dpVal > feeVal) return 'DP tidak boleh melebihi Total Honor.';
    }
    return '';
  };

  const handleNext = () => {
    const err = validateCurrentStep();
    if (err) { setError(err); return; }
    setError('');
    if (step < TOTAL_STEPS) setStep(s => s + 1);
  };

  const handlePrev = () => {
    setError('');
    if (step > 1) setStep(s => s - 1);
  };

  // Schedule conflict check (Android logic)
  const checkConflicts = (): Booking[] => {
    return existingBookings.filter(b => {
      if (b.status === 'CANCELLED' || b.status === 'cancelled') return false;
      const bDate = b.date || b.eventDate;
      if (bDate !== date) return false;
      
      const bStart = b.start || b.eventTime || '00:00';
      const bEnd = b.end || '23:59';
      
      // Overlap condition: start < bEnd && end > bStart
      return (startTime < bEnd && endTime > bStart);
    });
  };

  const executeSave = async (forceSave = false) => {
    const err = validateCurrentStep();
    if (err) { setError(err); return; }

    if (!forceSave) {
      const conflicts = checkConflicts();
      if (conflicts.length > 0) {
        setConflictJobs(conflicts);
        setShowConflictDialog(true);
        return;
      }
    }

    setSaving(true);
    setError('');
    try {
      const finalCategory = category === 'Lainnya' && customCategory ? customCategory : category;

      // Resolve or auto-create client before constructing booking to get correct clientId
      let resolvedClientId: string | undefined = clientId || undefined;
      if (clientInput.trim()) {
        const existingClient = clients.find(c => (clientId && c.id === clientId) || c.name.toLowerCase() === clientInput.trim().toLowerCase());
        if (existingClient) {
          resolvedClientId = existingClient.id;
        } else if (onSaveClient) {
          const newClient: Client = {
            id: `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
            ownerId: currentUser?.uid || '',
            name: clientInput.trim(),
            phone: clientPhone,
            company: clientCompany,
            pic: pic,
            createdAt: new Date().toISOString()
          };
          await onSaveClient(newClient);
          resolvedClientId = newClient.id;
        }
      }

      const booking: Booking = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        ownerId: currentUser?.uid || '',
        name: eventName.trim() || clientInput.trim(),
        eventTitle: eventName.trim() || clientInput.trim(),
        client: clientInput.trim(),
        clientName: clientInput.trim(),
        clientId: resolvedClientId,
        category: finalCategory,
        status: 'confirmed',
        date,
        eventDate: date,
        start: startTime,
        eventTime: startTime,
        end: endTime,
        location: location.trim(),
        venue: location.trim(),
        address: address.trim(),
        mcType,
        language,
        dresscode,
        theme,
        audience,
        pic,
        specialRequest,
        fee: feeVal,
        dp: dpVal,
        note,
        notes: note,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      await onSave(booking);

      // Auto-create Payment record for DP to sync with Cashflow/Finance
      if (dpVal > 0 && onSavePayment) {
        const payment: Payment = {
          id: `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
          ownerId: currentUser?.uid || '',
          bookingId: booking.id,
          amount: dpVal,
          paymentDate: date,
          date: date,
          paymentMethod: 'Transfer Bank',
          note: 'DP Awal (Catat Job)',
          createdAt: new Date().toISOString()
        };
        await onSavePayment(payment);
      }

      setSavedBooking(booking);
      setShowSuccessModal(true);
    } catch (err) {
      console.error(err);
      setError('Gagal menyimpan job. Periksa koneksi internet Anda.');
    } finally {
      setSaving(false);
    }
  };

  // Google Calendar Link Builder with robust time & date formatting
  const getGoogleCalendarUrl = (b: Booking) => {
    const titleEnc = encodeURIComponent(`🎤 MC Perform: ${b.name || b.eventTitle || 'Acara MC'}`);
    const dateFormatted = (b.eventDate || b.date || '').replace(/-/g, '');

    const padTime = (t: string, fallback: string) => {
      const parts = (t || fallback).split(':');
      const hh = (parts[0] || '00').padStart(2, '0');
      const mm = (parts[1] || '00').padStart(2, '0');
      return `${hh}${mm}00`;
    };

    const startTimeClean = padTime(b.start || b.eventTime || '', '19:00');
    const endTimeClean = padTime(b.end || '', '22:00');

    const datesParam = `${dateFormatted}T${startTimeClean}/${dateFormatted}T${endTimeClean}`;
    const locationEnc = encodeURIComponent(`${b.venue || b.location || ''} ${b.address || ''}`.trim());

    const details = `MC Perform: ${b.name}\nKlien: ${b.clientName || b.client}\nHonor: ${formatRp(b.fee || 0)}\nDresscode: ${b.dresscode || '-'}\nPIC: ${b.pic || '-'}\nCatatan: ${b.notes || b.note || '-'}`;
    const detailsEnc = encodeURIComponent(details);

    return `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${titleEnc}&dates=${datesParam}&details=${detailsEnc}&location=${locationEnc}`;
  };

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 900, background: 'var(--bg-app)', display: 'flex', flexDirection: 'column' }}>
      
      {/* ── TOP NAV ── */}
      <div style={{ height: '56px', background: 'var(--bg-surface)', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 16px', flexShrink: 0 }}>
        <button onClick={onClose} className="btn btn-ghost" style={{ padding: '0 8px', gap: '6px' }}>
          <X size={18} />
          <span style={{ fontSize: '14px' }}>Batal</span>
        </button>

        <div style={{ textAlign: 'center' }}>
          <h1 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)', lineHeight: '1.2' }}>Buat Job Baru</h1>
          <p style={{ fontSize: '11px', color: 'var(--text-3)' }}>Langkah {step} dari {TOTAL_STEPS}</p>
        </div>

        <button 
          onClick={() => executeSave(false)} 
          disabled={saving}
          className="btn btn-primary btn-sm"
          style={{ gap: '6px' }}
        >
          <Save size={14} /> {saving ? 'Menyimpan...' : 'Simpan'}
        </button>
      </div>

      {/* ── STEP PROGRESS BAR ── */}
      <div style={{ height: '3px', background: 'var(--border)', flexShrink: 0 }}>
        <div style={{ height: '100%', background: 'var(--primary)', width: `${(step / TOTAL_STEPS) * 100}%`, transition: 'width 0.3s ease' }} />
      </div>

      {/* ── STEP HEADER CHIPS ── */}
      <div style={{ display: 'flex', background: 'var(--bg-surface)', borderBottom: '1px solid var(--border)', overflowX: 'auto', flexShrink: 0 }} className="scrollbar-none">
        {steps.map(s => {
          const isActive = step === s.num;
          const isDone = step > s.num;
          const Icon = s.icon;
          return (
            <button
              key={s.num}
              onClick={() => { if (isDone || s.num <= step) setStep(s.num); }}
              style={{
                flex: '0 0 auto', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: '8px',
                fontSize: '12.5px', fontWeight: isActive ? '800' : '600',
                color: isActive ? 'var(--primary)' : isDone ? 'var(--success)' : 'var(--text-3)',
                background: 'none', border: 'none', cursor: 'pointer',
                borderBottom: isActive ? '2.5px solid var(--primary)' : '2.5px solid transparent',
                whiteSpace: 'nowrap'
              }}
            >
              {isDone ? <CheckCircle2 size={14} /> : <Icon size={14} />}
              <span>{s.num}. {s.title}</span>
            </button>
          );
        })}
      </div>

      {/* ── MAIN CONTENT CONTAINER ── */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '24px 16px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ width: '100%', maxWidth: '640px' }}>
          
          <form onSubmit={e => { e.preventDefault(); executeSave(false); }}>
            
            {/* ── SECTION 1: INFORMASI ACARA ── */}
            {step === 1 && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', borderTop: '4px solid var(--primary)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Calendar size={18} color="var(--primary)" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)' }}>Informasi Acara</h2>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)' }}>Nama acara, jadwal perform, dan lokasi venue.</p>
                  </div>
                </div>

                {/* Nama Acara */}
                <div>
                  <label className="input-label">Nama Acara *</label>
                  <input
                    type="text" autoFocus required
                    value={eventName}
                    onChange={e => setEventName(e.target.value)}
                    className="input-field"
                    placeholder="Contoh: Wedding Resepsi Kevin & Vania"
                  />
                </div>

                {/* Tanggal Acara */}
                <div>
                  <label className="input-label">Tanggal Acara *</label>
                  <input
                    type="date" required
                    value={date}
                    onChange={e => setDate(e.target.value)}
                    className="input-field"
                    style={{ fontWeight: '600' }}
                  />
                </div>

                {/* Kategori Acara + Chip Presets */}
                <div>
                  <label className="input-label">Kategori Event</label>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '10px' }}>
                    {CATEGORY_PRESETS.map(cat => (
                      <button
                        key={cat} type="button"
                        onClick={() => { setCategory(cat); setCustomCategory(''); }}
                        className="badge"
                        style={{
                          cursor: 'pointer', padding: '8px 14px', fontSize: '12px', fontWeight: '700', borderRadius: '9999px',
                          border: category === cat ? '1px solid var(--primary)' : '1px solid var(--border)',
                          background: category === cat ? 'var(--primary)' : 'var(--bg-surface-2)',
                          color: category === cat ? '#FFFFFF' : 'var(--text-2)',
                          transition: 'all 0.15s'
                        }}
                      >
                        {cat}
                      </button>
                    ))}
                    <button
                      type="button"
                      onClick={() => setCategory('Lainnya')}
                      className="badge"
                      style={{
                        cursor: 'pointer', padding: '8px 14px', fontSize: '12px', fontWeight: '700', borderRadius: '9999px',
                        border: category === 'Lainnya' ? '1px solid var(--primary)' : '1px solid var(--border)',
                        background: category === 'Lainnya' ? 'var(--primary)' : 'var(--bg-surface-2)',
                        color: category === 'Lainnya' ? '#FFFFFF' : 'var(--text-2)',
                        transition: 'all 0.15s'
                      }}
                    >
                      + Kustom / Lainnya
                    </button>
                  </div>

                  {category === 'Lainnya' && (
                    <input
                      type="text"
                      value={customCategory}
                      onChange={e => setCustomCategory(e.target.value)}
                      className="input-field"
                      placeholder="Ketik nama kategori kustom..."
                    />
                  )}
                </div>

                {/* Waktu Perform (Jam Mulai & Selesai) */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Jam Mulai (WIB) *</label>
                    <input
                      type="time" required
                      value={startTime}
                      onChange={e => setStartTime(e.target.value)}
                      className="input-field"
                    />
                  </div>
                  <div>
                    <label className="input-label">Jam Selesai (WIB) *</label>
                    <input
                      type="time" required
                      value={endTime}
                      onChange={e => setEndTime(e.target.value)}
                      className="input-field"
                    />
                  </div>
                </div>

                {/* Time Preset Chips (Android Feature) */}
                <div>
                  <label className="input-label">Pilihan Slot Waktu Cepat:</label>
                  <div style={{ display: 'flex', gap: '6px', overflowX: 'auto', paddingBottom: '4px' }} className="scrollbar-none">
                    {TIME_SLOT_PRESETS.map(slot => (
                      <button
                        key={slot.label} type="button"
                        onClick={() => { setStartTime(slot.start); setEndTime(slot.end); }}
                        className="btn btn-secondary btn-sm"
                        style={{ fontSize: '11px', whiteSpace: 'nowrap', flexShrink: 0, padding: '4px 10px' }}
                      >
                        ⏱️ {slot.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Venue & Alamat */}
                <div>
                  <label className="input-label">Nama Venue / Gedung</label>
                  <input
                    type="text"
                    value={location}
                    onChange={e => setLocation(e.target.value)}
                    className="input-field"
                    placeholder="Contoh: Grand Ballroom Hotel Mulia"
                  />
                </div>

                <div>
                  <label className="input-label">Alamat Lengkap Venue (Lokasi Peta)</label>
                  <textarea
                    value={address}
                    onChange={e => setAddress(e.target.value)}
                    className="input-field"
                    style={{ height: '70px', padding: '10px 12px', resize: 'none' }}
                    placeholder="Jl. Asia Afrika No.1, Senayan, Jakarta Pusat"
                  />
                </div>

                {/* Live GPS Maps Testing Card */}
                {(location || address) && (
                  <div style={{ background: '#F0FDFA', border: '1px solid #99F6E4', padding: '14px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div>
                      <span style={{ fontSize: '11px', fontWeight: '800', color: '#0F766E', textTransform: 'uppercase' }}>✓ Titik Peta Akurat</span>
                      <p style={{ fontSize: '12px', color: '#115E59', marginTop: '2px' }}>{location || address}</p>
                    </div>
                    <a
                      href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${location} ${address}`.trim())}`}
                      target="_blank" rel="noopener noreferrer"
                      className="btn btn-sm" style={{ background: '#0D9488', color: 'white', border: 'none', gap: '4px', fontSize: '11px' }}
                    >
                      <ExternalLink size={12} /> Tes Maps 🗺️
                    </a>
                  </div>
                )}
              </div>
            )}

            {/* ── SECTION 2: INFORMASI KLIEN & WO ── */}
            {step === 2 && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', borderTop: '4px solid #2563EB' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: '#EFF6FF', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <User size={18} color="#2563EB" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)' }}>SEKSI 2: INFORMASI KLIEN & WO</h2>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)' }}>Identitas klien, penyelenggara, dan kontak PIC lapangan.</p>
                  </div>
                </div>

                {/* Client Name with Autocomplete Suggestion Chips */}
                <div style={{ position: 'relative' }}>
                  <label className="input-label">Nama Klien / Penyelenggara *</label>
                  <input
                    type="text" autoFocus required
                    value={clientInput}
                    onChange={e => {
                      setClientInput(e.target.value);
                      setClientId('');
                      setShowClientDropdown(true);
                    }}
                    onFocus={() => setShowClientDropdown(true)}
                    onBlur={() => setTimeout(() => setShowClientDropdown(false), 200)}
                    className="input-field"
                    placeholder="Ketik nama klien, pasangan, atau instansi..."
                  />

                  {/* Autocomplete Dropdown List */}
                  {showClientDropdown && filteredClients.length > 0 && clientInput && (
                    <div style={{
                      position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 50,
                      background: 'var(--bg-surface)', border: '1px solid var(--border)',
                      borderRadius: '12px', boxShadow: 'var(--shadow-lg)',
                      overflow: 'hidden', marginTop: '4px'
                    }}>
                      {filteredClients.map(c => (
                        <button
                          key={c.id} type="button"
                          onMouseDown={() => selectClient(c)}
                          style={{
                            width: '100%', padding: '10px 14px', display: 'flex', alignItems: 'center',
                            gap: '10px', background: 'none', border: 'none', cursor: 'pointer',
                            textAlign: 'left', borderBottom: '1px solid var(--border)'
                          }}
                        >
                          <Users size={15} color="var(--primary)" />
                          <div>
                            <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)' }}>{c.name}</p>
                            <p style={{ fontSize: '11px', color: 'var(--text-3)' }}>{c.company || c.phone || 'Klien Tersimpan'}</p>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">No. WhatsApp Klien</label>
                    <input
                      type="tel"
                      value={clientPhone}
                      onChange={e => setClientPhone(e.target.value)}
                      className="input-field"
                      placeholder="081234567890"
                    />
                  </div>
                  <div>
                    <label className="input-label">Perusahaan / Organisasi</label>
                    <input
                      type="text"
                      value={clientCompany}
                      onChange={e => setClientCompany(e.target.value)}
                      className="input-field"
                      placeholder="PT. XYZ / Personal"
                    />
                  </div>
                </div>

                <div>
                  <label className="input-label">Kontak PIC / Event Organizer (WO)</label>
                  <input
                    type="text"
                    value={pic}
                    onChange={e => setPic(e.target.value)}
                    className="input-field"
                    placeholder="Siska (Happy WO - 081987654321)"
                  />
                </div>
              </div>
            )}

            {/* ── SECTION 3: DETAIL PROFESIONAL MC ── */}
            {step === 3 && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', borderTop: '4px solid #7C3AED' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: '#F5F3FF', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Mic size={18} color="#7C3AED" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)' }}>SEKSI 3: DETAIL PROFESIONAL MC</h2>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)' }}>Dresscode, tema acara, format perform, dan bahasa pengantar.</p>
                  </div>
                </div>

                {/* Dresscode + Preset Chips */}
                <div>
                  <label className="input-label">Dresscode / Wardrobe</label>
                  <input
                    type="text"
                    value={dresscode}
                    onChange={e => setDresscode(e.target.value)}
                    className="input-field"
                    placeholder="Contoh: Black Tie / Jas Hitam / Batik Modern"
                    style={{ marginBottom: '8px' }}
                  />
                  <div style={{ display: 'flex', gap: '6px', overflowX: 'auto' }} className="scrollbar-none">
                    {DRESSCODE_PRESETS.map(d => (
                      <button
                        key={d} type="button" onClick={() => setDresscode(d)}
                        className="btn btn-secondary btn-sm" style={{ fontSize: '11px', whiteSpace: 'nowrap' }}
                      >
                        👔 {d}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Tema Acara + Preset Chips */}
                <div>
                  <label className="input-label">Tema Acara</label>
                  <input
                    type="text"
                    value={theme}
                    onChange={e => setTheme(e.target.value)}
                    className="input-field"
                    placeholder="Contoh: Modern Elegance / Rustic"
                    style={{ marginBottom: '8px' }}
                  />
                  <div style={{ display: 'flex', gap: '6px', overflowX: 'auto' }} className="scrollbar-none">
                    {THEME_PRESETS.map(t => (
                      <button
                        key={t} type="button" onClick={() => setTheme(t)}
                        className="btn btn-secondary btn-sm" style={{ fontSize: '11px', whiteSpace: 'nowrap' }}
                      >
                        🎨 {t}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Format MC & Bahasa */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Format MC</label>
                    <select value={mcType} onChange={e => setMcType(e.target.value)} className="input-field">
                      {MC_TYPE_PRESETS.map(m => <option key={m} value={m}>{m}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="input-label">Bahasa Pengantar</label>
                    <select value={language} onChange={e => setLanguage(e.target.value)} className="input-field">
                      {LANGUAGE_PRESETS.map(l => <option key={l} value={l}>{l}</option>)}
                    </select>
                  </div>
                </div>

                <div>
                  <label className="input-label">Estimasi Audience / Tamu Undangan</label>
                  <input
                    type="text"
                    value={audience}
                    onChange={e => setAudience(e.target.value)}
                    className="input-field"
                    placeholder="Contoh: 500 Tamu Undangan"
                  />
                </div>

                <div>
                  <label className="input-label">Request Khusus / Protokol VIP</label>
                  <textarea
                    value={specialRequest}
                    onChange={e => setSpecialRequest(e.target.value)}
                    className="input-field"
                    style={{ height: '70px', padding: '10px 12px', resize: 'none' }}
                    placeholder="Ada pejabat VIP hadir, permainan games interaktif, rundowm ketat..."
                  />
                </div>
              </div>
            )}

            {/* ── SECTION 4: KEUANGAN & HONOR MC ── */}
            {step === 4 && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', borderTop: '4px solid var(--success)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'var(--success-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <DollarSign size={18} color="var(--success)" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)' }}>SEKSI 4: KEUANGAN & HONOR MC</h2>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)' }}>Honorarium kesepakatan, DP diterima, dan sisa pelunasan.</p>
                  </div>
                </div>

                {rateCards.length > 0 && (
                  <div style={{ marginBottom: '4px' }}>
                    <label className="input-label">Pilih Paket MC / Rate Card (Opsional)</label>
                    <select 
                      className="input-field"
                      onChange={e => {
                        const selectedId = e.target.value;
                        if (!selectedId) return;
                        const selectedPackage = rateCards.find(rc => rc.id === selectedId);
                        if (selectedPackage) {
                          setFee(selectedPackage.price);
                          if (userProfile?.defaultDpPercentage) {
                            setDp(Math.round((selectedPackage.price * userProfile.defaultDpPercentage) / 100));
                          }
                        }
                      }}
                    >
                      <option value="">-- Input Manual --</option>
                      {rateCards.map(rc => (
                        <option key={rc.id} value={rc.id}>{rc.name} - {formatRp(rc.price)}</option>
                      ))}
                    </select>
                  </div>
                )}

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Total Honor MC (Rp) *</label>
                    <input
                      type="number" autoFocus required min="0"
                      value={fee}
                      onChange={e => setFee(e.target.value === '' ? '' : Number(e.target.value))}
                      className="input-field"
                      style={{ fontWeight: '800', color: 'var(--primary)', fontSize: '16px' }}
                      placeholder="0"
                    />
                    {feeVal > 0 && (
                      <p className="input-hint" style={{ color: 'var(--primary)', fontWeight: '700', marginTop: '4px' }}>
                        Pratinjau: {formatRp(feeVal)}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="input-label">DP Diterima (Rp)</label>
                    <input
                      type="number" min="0"
                      value={dp}
                      onChange={e => setDp(e.target.value === '' ? '' : Number(e.target.value))}
                      className="input-field"
                      style={{ fontWeight: '800', color: 'var(--success)', fontSize: '16px' }}
                      placeholder="0"
                    />
                    {dpVal > 0 && (
                      <p className="input-hint" style={{ color: 'var(--success)', fontWeight: '700', marginTop: '4px' }}>
                        Pratinjau: {formatRp(dpVal)}
                      </p>
                    )}
                  </div>
                </div>

                {/* Live Calculated Sisa Tagihan Box (Android Feature) */}
                {feeVal > 0 && (
                  <div className="card" style={{ padding: '18px 20px', background: 'var(--bg-surface-2)', border: isPaid ? '2px solid #059669' : '2px solid #F59E0B' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <div>
                        <span style={{ fontSize: '11px', fontWeight: '800', color: 'var(--text-3)', textTransform: 'uppercase' }}>KALKULASI SISA TAGIHAN</span>
                        <h3 style={{ fontSize: '22px', fontWeight: '900', color: isPaid ? '#059669' : '#D97706', fontVariantNumeric: 'tabular-nums', marginTop: '2px' }}>
                          {isPaid ? 'LUNAS ✓' : formatRp(remaining)}
                        </h3>
                      </div>

                      <span className={`badge ${isPaid ? 'badge-success' : 'badge-warning'}`} style={{ padding: '6px 14px', fontSize: '12px' }}>
                        {isPaid ? 'PEMBAYARAN LUNAS' : `SISA PIUTANG ${Math.round((remaining / feeVal) * 100)}%`}
                      </span>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* ── SECTION 5: CATATAN BRIEF & RUNDOWN ── */}
            {step === 5 && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px', borderTop: '4px solid #0369A1' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: '#E0F2FE', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <AlignLeft size={18} color="#0369A1" />
                  </div>
                  <div>
                    <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)' }}>SEKSI 5: CATATAN BRIEF & RUNDOWN</h2>
                    <p style={{ fontSize: '12px', color: 'var(--text-3)' }}>Catatan internal, brief tambahan, dan petunjuk rundown.</p>
                  </div>
                </div>

                <div>
                  <label className="input-label">Catatan Briefing & Rundown Internal MC</label>
                  <textarea
                    autoFocus
                    value={note}
                    onChange={e => setNote(e.target.value)}
                    className="input-field"
                    style={{ height: '140px', padding: '12px', resize: 'vertical', lineHeight: '1.6' }}
                    placeholder="Ketik catatan pribadi, nomor meja VIP, instruksi WO, atau ringkasan rundown di sini..."
                  />
                </div>
              </div>
            )}

            {/* Error Banner */}
            {error && (
              <div style={{ marginTop: '16px', padding: '12px 16px', borderRadius: '10px', background: 'var(--error-light)', color: 'var(--error)', fontSize: '13px', fontWeight: '700', border: '1px solid rgba(220,38,38,0.2)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <AlertTriangle size={16} /> {error}
              </div>
            )}

            {/* Step Navigation Buttons */}
            <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
              {step > 1 && (
                <button type="button" onClick={handlePrev} className="btn btn-secondary btn-full btn-lg" style={{ gap: '6px' }}>
                  <ChevronLeft size={16} /> Kembali
                </button>
              )}

              {step < TOTAL_STEPS ? (
                <button type="button" onClick={handleNext} className="btn btn-primary btn-full btn-lg" style={{ gap: '6px' }}>
                  Selanjutnya <ChevronRight size={16} />
                </button>
              ) : (
                <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{ gap: '6px', background: '#059669', borderColor: '#059669' }}>
                  <Save size={16} /> {saving ? 'Menyimpan Job...' : 'Simpan Job'}
                </button>
              )}
            </div>

          </form>

        </div>
      </div>

      {/* ── CONFLICT DIALOG MODAL ── */}
      {showConflictDialog && createPortal(
        <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) setShowConflictDialog(false); }}>
          <div className="modal-panel animate-fade-in" style={{ maxWidth: '480px' }}>
            <div style={{ textAlign: 'center', marginBottom: '16px' }}>
              <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#FEF3C7', color: '#D97706', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px' }}>
                <AlertTriangle size={28} />
              </div>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-1)' }}>Jadwal Bentrok</h3>
              <p style={{ fontSize: '13px', color: 'var(--text-3)', marginTop: '4px' }}>
                Terdapat acara lain pada tanggal <strong>{date}</strong> di jam yang sama:
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '20px', maxHeight: '160px', overflowY: 'auto' }}>
              {conflictJobs.map(cj => (
                <div key={cj.id} style={{ background: 'var(--bg-surface-2)', padding: '10px 14px', borderRadius: '10px', border: '1px solid var(--border)' }}>
                  <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)' }}>{cj.name || cj.eventTitle}</p>
                  <p style={{ fontSize: '11px', color: 'var(--text-3)' }}>⏱️ {cj.start || cj.eventTime} - {cj.end || 'Selesai'} • 📍 {cj.venue || 'Venue'}</p>
                </div>
              ))}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <button onClick={() => setShowConflictDialog(false)} className="btn btn-secondary btn-full">
                Ubah Jadwal
              </button>
              <button onClick={() => { setShowConflictDialog(false); executeSave(true); }} className="btn btn-primary btn-full" style={{ background: '#D97706', borderColor: '#D97706' }}>
                Tetap Simpan
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}

      {/* ── SUCCESS & GOOGLE CALENDAR MODAL ── */}
      {showSuccessModal && savedBooking && createPortal(
        <div className="modal-overlay">
          <div className="modal-panel animate-fade-in" style={{ maxWidth: '440px', textAlign: 'center' }}>
            <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: '#D1FAE5', color: '#059669', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
              <CheckCircle2 size={32} />
            </div>

            <h3 style={{ fontSize: '20px', fontWeight: '900', color: 'var(--text-1)' }}>Job Berhasil Disimpan</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-3)', marginTop: '4px', marginBottom: '20px' }}>
              Acara <strong>{savedBooking.name}</strong> telah tersimpan di agenda & laporan keuangan Anda.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <a
                href={getGoogleCalendarUrl(savedBooking)}
                target="_blank" rel="noopener noreferrer"
                className="btn btn-primary btn-full btn-lg"
                style={{ background: '#4F46E5', borderColor: '#4F46E5', gap: '8px', textDecoration: 'none' }}
              >
                <Calendar size={18} /> Tambah ke Google Calendar
              </a>

              <button onClick={onClose} className="btn btn-secondary btn-full btn-lg">
                Selesai
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}

    </div>
  );
};
