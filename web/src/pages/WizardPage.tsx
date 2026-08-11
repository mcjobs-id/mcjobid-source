import React, { useState } from 'react';
import {
  ArrowLeft, Save, Calendar, User, DollarSign, MapPin,
  AlignLeft, CheckCircle2, Clock, Mic, Phone, Tag,
  Users, ChevronLeft, ChevronRight, X
} from 'lucide-react';
import type { Booking, Client } from '../types';
import { useAuth } from '../context/AuthContext';

interface WizardPageProps {
  onClose: () => void;
  onSave: (booking: Booking) => Promise<void>;
  clients?: Client[];
}

const CATEGORIES = ['Wedding', 'Engagement / Lamaran', 'Corporate Event', 'Gathering / Arisan', 'Sweet 17 / Birthday', 'Concert / Festival', 'Seminar / Workshop', 'Lainnya'];
const MC_TYPES = ['Single', 'Duet', 'Tim (3+)'];
const LANGUAGES = ['Bahasa Indonesia', 'English', 'Bilingual (Indonesia + English)', 'Bahasa Daerah'];

function fmtRp(val: number) {
  return `Rp ${val.toLocaleString('id-ID')}`;
}

export const WizardPage: React.FC<WizardPageProps> = ({ onClose, onSave, clients = [] }) => {
  const { currentUser } = useAuth();
  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  // Step 1 — Client & Event Info
  const [eventName, setEventName] = useState('');
  const [clientInput, setClientInput] = useState('');
  const [clientId, setClientId] = useState('');
  const [showClientDropdown, setShowClientDropdown] = useState(false);
  const [category, setCategory] = useState('Wedding');

  // Step 2 — Schedule & Location
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [location, setLocation] = useState('');
  const [address, setAddress] = useState('');

  // Step 3 — MC Specification
  const [mcType, setMcType] = useState('Single');
  const [language, setLanguage] = useState('Bahasa Indonesia');
  const [dresscode, setDresscode] = useState('');
  const [theme, setTheme] = useState('');
  const [audience, setAudience] = useState('');
  const [pic, setPic] = useState('');
  const [specialRequest, setSpecialRequest] = useState('');

  // Step 4 — Financials & Notes
  const [fee, setFee] = useState<number | ''>('');
  const [dp, setDp] = useState<number | ''>('');
  const [note, setNote] = useState('');

  const TOTAL_STEPS = 4;
  const steps = [
    { title: 'Info Klien & Acara', icon: User },
    { title: 'Jadwal & Lokasi', icon: Calendar },
    { title: 'Spesifikasi MC', icon: Mic },
    { title: 'Keuangan & Catatan', icon: DollarSign },
  ];

  // Client autocomplete
  const filteredClients = clients.filter(c =>
    c.name.toLowerCase().includes(clientInput.toLowerCase())
  ).slice(0, 6);

  const selectClient = (c: Client) => {
    setClientInput(c.name);
    setClientId(c.id);
    setPic(c.phone || '');
    setShowClientDropdown(false);
  };

  const validateStep = (): string => {
    if (step === 1) {
      if (!clientInput.trim()) return 'Nama klien wajib diisi.';
    }
    if (step === 2) {
      if (!date) return 'Tanggal acara wajib diisi.';
    }
    if (step === 4) {
      if (!fee || Number(fee) <= 0) return 'Honor MC wajib diisi.';
    }
    return '';
  };

  const handleNext = () => {
    const err = validateStep();
    if (err) { setError(err); return; }
    setError('');
    if (step < TOTAL_STEPS) setStep(s => s + 1);
  };

  const handlePrev = () => {
    setError('');
    if (step > 1) setStep(s => s - 1);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const err = validateStep();
    if (err) { setError(err); return; }
    setSaving(true);
    setError('');
    try {
      const feeNum = Number(fee) || 0;
      const dpNum = Number(dp) || 0;

      const booking: Booking = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        ownerId: currentUser?.uid || '',
        // Event identity
        name: eventName || clientInput,
        eventTitle: eventName || clientInput,
        // Client
        client: clientInput,
        clientName: clientInput,
        clientId: clientId || undefined,
        // Category & Status
        category,
        status: 'confirmed',
        // Dates
        date,
        eventDate: date,
        start: startTime,
        eventTime: startTime,
        end: endTime,
        // Venue
        location,
        venue: location,
        address,
        // MC Specification
        mcType,
        language,
        dresscode,
        theme,
        audience,
        pic,
        specialRequest,
        // Financials
        fee: feeNum,
        dp: dpNum,
        // Notes
        note,
        notes: note,
        // Metadata
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      await onSave(booking);
      setSuccess(true);
      setTimeout(onClose, 1800);
    } catch (err) {
      console.error(err);
      setError('Gagal menyimpan job. Coba lagi.');
    } finally {
      setSaving(false);
    }
  };

  if (success) {
    return (
      <div style={{ position: 'fixed', inset: 0, zIndex: 200, background: 'var(--bg-app)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px' }}>
        <div className="card" style={{ padding: '48px 32px', textAlign: 'center', maxWidth: '380px', width: '100%' }}>
          <div style={{ width: '72px', height: '72px', borderRadius: '50%', background: 'var(--success-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px', border: '2px solid rgba(5,150,105,0.2)' }}>
            <CheckCircle2 size={36} color="var(--success)" />
          </div>
          <h2 style={{ fontSize: '22px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '8px' }}>Job Berhasil Disimpan!</h2>
          <p style={{ fontSize: '14px', color: 'var(--text-3)', lineHeight: '1.6' }}>
            <strong>{eventName || clientInput}</strong> telah ditambahkan ke agenda dan dashboard keuangan Anda.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 200, background: 'var(--bg-app)', display: 'flex', flexDirection: 'column' }}>
      {/* Top Nav */}
      <div style={{ height: '56px', background: 'var(--bg-surface)', borderBottom: '1px solid var(--border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 16px', flexShrink: 0 }}>
        <button onClick={onClose} className="btn btn-ghost" style={{ padding: '0 8px', gap: '6px' }}>
          <X size={18} />
          <span className="hidden md:inline" style={{ fontSize: '14px' }}>Batal</span>
        </button>
        <div style={{ textAlign: 'center' }}>
          <h1 style={{ fontSize: '15px', fontWeight: '700', color: 'var(--text-1)', lineHeight: '1.2' }}>Tambah Job Baru</h1>
          <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>Langkah {step} dari {TOTAL_STEPS}</p>
        </div>
        <div style={{ width: '80px' }} />
      </div>

      {/* Progress Bar */}
      <div style={{ height: '3px', background: 'var(--border)', flexShrink: 0 }}>
        <div style={{ height: '100%', background: 'var(--primary)', width: `${(step / TOTAL_STEPS) * 100}%`, transition: 'width 0.35s ease' }} />
      </div>

      {/* Step Tabs */}
      <div style={{ display: 'flex', background: 'var(--bg-surface)', borderBottom: '1px solid var(--border)', overflowX: 'auto', flexShrink: 0 }} className="scrollbar-none">
        {steps.map((s, i) => {
          const num = i + 1;
          const isActive = step === num;
          const isDone = step > num;
          const Icon = s.icon;
          return (
            <button
              key={num}
              onClick={() => { if (isDone) setStep(num); }}
              style={{
                flex: '0 0 auto', padding: '10px 16px', display: 'flex', alignItems: 'center', gap: '6px',
                fontSize: '12px', fontWeight: isActive ? '700' : '500',
                color: isActive ? 'var(--primary)' : isDone ? 'var(--success)' : 'var(--text-4)',
                background: 'none', border: 'none', cursor: isDone ? 'pointer' : 'default',
                borderBottom: isActive ? '2px solid var(--primary)' : '2px solid transparent',
                whiteSpace: 'nowrap'
              }}
            >
              {isDone ? <CheckCircle2 size={13} /> : <Icon size={13} />}
              <span className="hidden sm:inline">{s.title}</span>
              <span className="sm:hidden">{num}</span>
            </button>
          );
        })}
      </div>

      {/* Content */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '24px 16px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ width: '100%', maxWidth: '560px' }}>
          <form onSubmit={handleSubmit}>

            {/* Step 1: Client & Event */}
            {step === 1 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '4px' }}>Info Klien & Acara</h2>
                  <p style={{ fontSize: '13px', color: 'var(--text-3)' }}>Siapa klien dan apa nama acaranya?</p>
                </div>

                {/* Client Name with Autocomplete */}
                <div style={{ position: 'relative' }}>
                  <label className="input-label">Nama Klien / WO *</label>
                  <div className="input-group">
                    <User size={15} className="input-icon-left" />
                    <input
                      type="text" autoFocus
                      value={clientInput}
                      onChange={e => {
                        setClientInput(e.target.value);
                        setClientId('');
                        setShowClientDropdown(true);
                      }}
                      onFocus={() => setShowClientDropdown(true)}
                      onBlur={() => setTimeout(() => setShowClientDropdown(false), 200)}
                      className="input-field"
                      placeholder="Nama klien, WO, atau instansi..."
                    />
                  </div>
                  {showClientDropdown && filteredClients.length > 0 && clientInput && (
                    <div style={{
                      position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 50,
                      background: 'var(--bg-surface)', border: '1px solid var(--border)',
                      borderRadius: '10px', boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
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
                          <Users size={14} color="var(--text-4)" />
                          <div>
                            <p style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-1)' }}>{c.name}</p>
                            <p style={{ fontSize: '11px', color: 'var(--text-4)' }}>{c.phone || c.type || 'Klien'}</p>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                {/* Event Name */}
                <div>
                  <label className="input-label">Nama Acara / Event</label>
                  <div className="input-group">
                    <Tag size={15} className="input-icon-left" />
                    <input
                      type="text" value={eventName}
                      onChange={e => setEventName(e.target.value)}
                      className="input-field"
                      placeholder="Contoh: Wedding Adi & Sari, Gathering PT. XYZ..."
                    />
                  </div>
                  <p className="input-hint">Jika kosong, nama klien akan digunakan sebagai nama acara.</p>
                </div>

                {/* Category */}
                <div>
                  <label className="input-label">Kategori Acara</label>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '8px' }}>
                    {CATEGORIES.map(c => (
                      <button
                        key={c} type="button" onClick={() => setCategory(c)}
                        style={{
                          padding: '10px 12px', borderRadius: '10px', fontSize: '12px', fontWeight: '600',
                          border: `1.5px solid ${category === c ? 'var(--primary)' : 'var(--border)'}`,
                          background: category === c ? 'var(--primary-light)' : 'var(--bg-surface)',
                          color: category === c ? 'var(--primary)' : 'var(--text-3)',
                          cursor: 'pointer', textAlign: 'left', transition: 'all 0.15s'
                        }}
                      >{c}</button>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Step 2: Schedule & Location */}
            {step === 2 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '4px' }}>Jadwal & Lokasi</h2>
                  <p style={{ fontSize: '13px', color: 'var(--text-3)' }}>Kapan dan di mana acaranya berlangsung?</p>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
                  <div style={{ gridColumn: '1 / -1' }}>
                    <label className="input-label">Tanggal Acara *</label>
                    <div className="input-group">
                      <Calendar size={15} className="input-icon-left" />
                      <input type="date" required value={date} onChange={e => setDate(e.target.value)} className="input-field" />
                    </div>
                  </div>
                  <div>
                    <label className="input-label">Mulai (WIB)</label>
                    <div className="input-group">
                      <Clock size={15} className="input-icon-left" />
                      <input type="time" value={startTime} onChange={e => setStartTime(e.target.value)} className="input-field" />
                    </div>
                  </div>
                  <div>
                    <label className="input-label">Selesai (WIB)</label>
                    <div className="input-group">
                      <Clock size={15} className="input-icon-left" />
                      <input type="time" value={endTime} onChange={e => setEndTime(e.target.value)} className="input-field" />
                    </div>
                  </div>
                </div>

                <div>
                  <label className="input-label">Nama Venue / Gedung</label>
                  <div className="input-group">
                    <MapPin size={15} className="input-icon-left" />
                    <input
                      type="text" value={location}
                      onChange={e => setLocation(e.target.value)}
                      className="input-field"
                      placeholder="Contoh: The Sultan Hotel, Gedung Serbaguna..."
                    />
                  </div>
                </div>

                <div>
                  <label className="input-label">Alamat Lengkap</label>
                  <textarea
                    value={address} onChange={e => setAddress(e.target.value)}
                    className="input-field" style={{ height: '80px', padding: '10px 12px', resize: 'none' }}
                    placeholder="Jl. nama jalan, No. xxx, Kota..."
                  />
                </div>
              </div>
            )}

            {/* Step 3: MC Specification */}
            {step === 3 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '4px' }}>Spesifikasi MC</h2>
                  <p style={{ fontSize: '13px', color: 'var(--text-3)' }}>Detail teknis dan permintaan khusus dari klien.</p>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Format MC</label>
                    <select value={mcType} onChange={e => setMcType(e.target.value)} className="input-field">
                      {MC_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="input-label">Bahasa Pengantar</label>
                    <select value={language} onChange={e => setLanguage(e.target.value)} className="input-field">
                      {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
                    </select>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Dresscode / Wardrobe</label>
                    <input
                      type="text" value={dresscode}
                      onChange={e => setDresscode(e.target.value)}
                      className="input-field"
                      placeholder="Contoh: Jas Hitam, Batik..."
                    />
                  </div>
                  <div>
                    <label className="input-label">Tema Acara</label>
                    <input
                      type="text" value={theme}
                      onChange={e => setTheme(e.target.value)}
                      className="input-field"
                      placeholder="Contoh: Rustic, Mewah Modern..."
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Estimasi Tamu / Audience</label>
                    <input
                      type="text" value={audience}
                      onChange={e => setAudience(e.target.value)}
                      className="input-field"
                      placeholder="Contoh: 200-300 orang"
                    />
                  </div>
                  <div>
                    <label className="input-label">Kontak PIC / WO</label>
                    <div className="input-group">
                      <Phone size={15} className="input-icon-left" />
                      <input
                        type="tel" value={pic}
                        onChange={e => setPic(e.target.value)}
                        className="input-field"
                        placeholder="No. HP PIC"
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <label className="input-label">Request Khusus / Protokol VIP</label>
                  <textarea
                    value={specialRequest} onChange={e => setSpecialRequest(e.target.value)}
                    className="input-field" style={{ height: '80px', padding: '10px 12px', resize: 'none' }}
                    placeholder="Contoh: Tidak boleh bercanda, ada tamu VIP pejabat, rundown ketat..."
                  />
                </div>
              </div>
            )}

            {/* Step 4: Financials & Notes */}
            {step === 4 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div>
                  <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '4px' }}>Keuangan & Catatan</h2>
                  <p style={{ fontSize: '13px', color: 'var(--text-3)' }}>Honorarium dan down payment yang disepakati.</p>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label className="input-label">Total Honor (Rp) *</label>
                    <input
                      type="number" required min="0"
                      value={fee}
                      onChange={e => setFee(e.target.value === '' ? '' : Number(e.target.value))}
                      className="input-field"
                      style={{ fontWeight: '700', color: 'var(--primary)', fontSize: '16px' }}
                      placeholder="0"
                    />
                  </div>
                  <div>
                    <label className="input-label">DP Diterima (Rp)</label>
                    <input
                      type="number" min="0"
                      value={dp}
                      onChange={e => setDp(e.target.value === '' ? '' : Number(e.target.value))}
                      className="input-field"
                      style={{ fontWeight: '600', color: 'var(--success)', fontSize: '16px' }}
                      placeholder="0"
                    />
                  </div>
                </div>

                {Number(fee) > 0 && (
                  <div className="card" style={{ padding: '16px', background: 'var(--bg-surface-2)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>Total Honor</span>
                      <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-1)' }}>{fmtRp(Number(fee))}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                      <span style={{ fontSize: '12px', color: 'var(--text-3)' }}>DP Masuk</span>
                      <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--success)' }}>{fmtRp(Number(dp) || 0)}</span>
                    </div>
                    <div style={{ borderTop: '1px solid var(--border)', paddingTop: '8px', display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-1)' }}>Sisa Tagihan</span>
                      <span style={{ fontSize: '14px', fontWeight: '700', color: Number(dp) >= Number(fee) ? 'var(--success)' : 'var(--warning)' }}>
                        {Number(dp) >= Number(fee) ? 'LUNAS ✓' : fmtRp(Number(fee) - (Number(dp) || 0))}
                      </span>
                    </div>
                  </div>
                )}

                <div>
                  <label className="input-label">Catatan Internal MC</label>
                  <div className="input-group">
                    <AlignLeft size={15} className="input-icon-left" />
                    <input
                      type="text" value={note}
                      onChange={e => setNote(e.target.value)}
                      className="input-field"
                      placeholder="Catatan pribadi, reminder, hal yang perlu dipersiapkan..."
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Error */}
            {error && (
              <div style={{ marginTop: '16px', padding: '10px 14px', borderRadius: '8px', background: 'var(--error-light)', color: 'var(--error)', fontSize: '13px', fontWeight: '600', border: '1px solid rgba(220,38,38,0.2)' }}>
                {error}
              </div>
            )}

            {/* Navigation */}
            <div style={{ display: 'flex', gap: '12px', marginTop: '32px', paddingTop: '20px', borderTop: '1px solid var(--border)' }}>
              {step > 1 && (
                <button type="button" onClick={handlePrev} className="btn btn-secondary btn-lg" style={{ flex: 1, gap: '6px' }}>
                  <ChevronLeft size={16} /> Kembali
                </button>
              )}
              {step < TOTAL_STEPS ? (
                <button type="button" onClick={handleNext} className="btn btn-primary btn-lg" style={{ flex: 2, gap: '6px' }}>
                  Selanjutnya <ChevronRight size={16} />
                </button>
              ) : (
                <button type="submit" disabled={saving} className="btn btn-primary btn-lg" style={{ flex: 2, gap: '6px' }}>
                  {saving ? 'Menyimpan...' : <><Save size={16} /> Simpan Job</>}
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
