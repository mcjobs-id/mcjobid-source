import React, { useState } from 'react';
import { ArrowLeft, Save, Calendar, User, DollarSign, MapPin, AlignLeft, CheckCircle2 } from 'lucide-react';
import type { Booking } from '../types';

interface WizardPageProps {
  onClose: () => void;
  onSave: (booking: Booking) => Promise<void>;
}

export const WizardPage: React.FC<WizardPageProps> = ({ onClose, onSave }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);

  // Form states
  const [clientName, setClientName] = useState('');
  const [category, setCategory] = useState('Wedding');
  const [eventDate, setEventDate] = useState('');
  const [eventTime, setEventTime] = useState('');
  const [venue, setVenue] = useState('');
  const [totalFee, setTotalFee] = useState<number | ''>('');
  const [dpAmount, setDpAmount] = useState<number | ''>('');
  const [notes, setNotes] = useState('');

  const handleNext = () => {
    if (currentStep < 3) setCurrentStep(currentStep + 1);
  };
  const handlePrev = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const b: Booking = {
        id: Date.now().toString(),
        ownerId: '',
        clientName,
        category,
        eventDate,
        eventTime,
        venue,
        totalFee: Number(totalFee),
        dpAmount: Number(dpAmount),
        paymentStatus: Number(dpAmount) >= Number(totalFee) ? 'PAID' : 'UNPAID',
        notes,
        status: 'CONFIRMED',
        createdAt: new Date().toISOString()
      };
      await onSave(b);
      setSuccess(true);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (success) {
    return (
      <div className="animate-fade-in" style={{position:'fixed', inset:0, zIndex:100, background:'var(--bg-app)', display:'flex', alignItems:'center', justifyContent:'center', padding:'24px'}}>
        <div className="card" style={{padding:'40px 24px', textAlign:'center', maxWidth:'400px', width:'100%'}}>
          <div style={{width:'64px', height:'64px', borderRadius:'50%', background:'var(--success)', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 24px', color:'white', boxShadow:'0 8px 24px rgba(5,150,105,0.3)'}}>
            <CheckCircle2 size={32} />
          </div>
          <h2 style={{fontSize:'20px', fontWeight:'700', color:'var(--text-1)', marginBottom:'8px'}}>Acara Tersimpan!</h2>
          <p style={{fontSize:'14px', color:'var(--text-3)', lineHeight:'1.5'}}>
            Agenda berhasil ditambahkan ke kalender dan dashboard keuangan Anda.
          </p>
        </div>
      </div>
    );
  }

  const steps = [
    { title: 'Info Klien & Acara' },
    { title: 'Jadwal & Lokasi' },
    { title: 'Keuangan & Detail' },
  ];

  return (
    <div className="animate-fade-in" style={{
      position: 'fixed', inset: 0, zIndex: 100, background: 'var(--bg-app)',
      display: 'flex', flexDirection: 'column'
    }}>
      {/* ── TOP NAV ── */}
      <div style={{
        height: '56px', background: 'var(--bg-surface)', borderBottom: '1px solid var(--border)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 16px',
        position: 'sticky', top: 0, zIndex: 10
      }}>
        <button onClick={onClose} className="btn btn-ghost" style={{padding:'0 8px'}}>
          <ArrowLeft size={18} /> <span className="hidden md:inline">Batal</span>
        </button>
        <h1 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)'}}>Tambah Job Baru</h1>
        <div style={{width:'80px'}} /> {/* spacer for center alignment */}
      </div>

      <div style={{flex: 1, overflowY: 'auto', padding: '24px 16px', display:'flex', flexDirection:'column', alignItems:'center'}}>
        <div style={{width:'100%', maxWidth:'500px'}}>
          
          {/* ── PROGRESS INDICATOR ── */}
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'32px', position:'relative'}}>
            <div style={{position:'absolute', top:'12px', left:'10%', right:'10%', height:'2px', background:'var(--border)', zIndex:0}} />
            <div style={{position:'absolute', top:'12px', left:'10%', width:`${((currentStep - 1) / 2) * 80}%`, height:'2px', background:'var(--primary)', zIndex:1, transition:'width 0.3s ease'}} />
            
            {steps.map((s, i) => {
              const num = i + 1;
              const active = currentStep === num;
              const completed = currentStep > num;
              return (
                <div key={num} style={{display:'flex', flexDirection:'column', alignItems:'center', gap:'8px', position:'relative', zIndex:2}}>
                  <div style={{
                    width:'24px', height:'24px', borderRadius:'50%', fontSize:'11px', fontWeight:'700',
                    display:'flex', alignItems:'center', justifyContent:'center', transition:'all 0.3s ease',
                    background: active || completed ? 'var(--primary)' : 'var(--bg-surface-2)',
                    color: active || completed ? 'white' : 'var(--text-3)',
                    border: `2px solid ${active || completed ? 'var(--primary)' : 'var(--border)'}`,
                    boxShadow: active ? '0 0 0 4px rgba(79,70,229,0.1)' : 'none'
                  }}>
                    {completed ? <CheckCircle2 size={12} /> : num}
                  </div>
                  <span style={{fontSize:'11px', fontWeight: active ? '700' : '500', color: active ? 'var(--text-1)' : 'var(--text-4)'}} className="hidden md:block">
                    {s.title}
                  </span>
                </div>
              );
            })}
          </div>

          <form onSubmit={handleSubmit} className="card" style={{padding:'24px', borderTop:'4px solid var(--primary)'}}>
            
            {currentStep === 1 && (
              <div className="animate-slide-in" style={{display:'flex', flexDirection:'column', gap:'16px'}}>
                <h2 style={{fontSize:'18px', fontWeight:'700', marginBottom:'8px'}}>Siapa Klien Anda?</h2>
                <div>
                  <label className="input-label">Nama Klien / Instansi *</label>
                  <div className="input-group">
                    <User size={15} className="input-icon-left" />
                    <input type="text" required value={clientName} onChange={e => setClientName(e.target.value)} className="input-field" autoFocus />
                  </div>
                </div>
                <div>
                  <label className="input-label">Kategori Acara</label>
                  <select value={category} onChange={e => setCategory(e.target.value)} className="input-field">
                    <option value="Wedding">Wedding</option>
                    <option value="Corporate Event">Corporate Event</option>
                    <option value="Gathering">Gathering</option>
                    <option value="Engagement">Engagement / Lamaran</option>
                    <option value="Sweet 17">Sweet 17 / Birthday</option>
                    <option value="Concert">Concert / Festival</option>
                    <option value="Other">Lainnya</option>
                  </select>
                </div>
              </div>
            )}

            {currentStep === 2 && (
              <div className="animate-slide-in" style={{display:'flex', flexDirection:'column', gap:'16px'}}>
                <h2 style={{fontSize:'18px', fontWeight:'700', marginBottom:'8px'}}>Kapan & Dimana?</h2>
                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
                  <div>
                    <label className="input-label">Tanggal *</label>
                    <input type="date" required value={eventDate} onChange={e => setEventDate(e.target.value)} className="input-field" />
                  </div>
                  <div>
                    <label className="input-label">Waktu Mulai (WIB)</label>
                    <input type="time" value={eventTime} onChange={e => setEventTime(e.target.value)} className="input-field" />
                  </div>
                </div>
                <div>
                  <label className="input-label">Lokasi / Venue</label>
                  <div className="input-group">
                    <MapPin size={15} className="input-icon-left" />
                    <input type="text" value={venue} onChange={e => setVenue(e.target.value)} className="input-field" placeholder="Nama Gedung / Hotel" />
                  </div>
                </div>
              </div>
            )}

            {currentStep === 3 && (
              <div className="animate-slide-in" style={{display:'flex', flexDirection:'column', gap:'16px'}}>
                <h2 style={{fontSize:'18px', fontWeight:'700', marginBottom:'8px'}}>Detail Keuangan</h2>
                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
                  <div>
                    <label className="input-label">Total Honor (Rp) *</label>
                    <input type="number" required value={totalFee} onChange={e => setTotalFee(Number(e.target.value))} className="input-field" style={{fontWeight:'700', color:'var(--primary)'}} />
                  </div>
                  <div>
                    <label className="input-label">DP Masuk (Rp)</label>
                    <input type="number" value={dpAmount} onChange={e => setDpAmount(Number(e.target.value))} className="input-field" style={{fontWeight:'600', color:'var(--success)'}} />
                  </div>
                </div>
                <div>
                  <label className="input-label">Catatan (Opsional)</label>
                  <div className="input-group">
                    <AlignLeft size={15} className="input-icon-left" />
                    <input type="text" value={notes} onChange={e => setNotes(e.target.value)} className="input-field" placeholder="Dresscode, Request khusus, dll" />
                  </div>
                </div>
              </div>
            )}

            {/* Actions */}
            <div style={{display:'flex', alignItems:'center', gap:'12px', marginTop:'32px', paddingTop:'20px', borderTop:'1px solid var(--border)'}}>
              {currentStep > 1 && (
                <button type="button" onClick={handlePrev} className="btn btn-secondary btn-lg" style={{flex:1}}>
                  Kembali
                </button>
              )}
              {currentStep < 3 ? (
                <button type="button" onClick={handleNext} className="btn btn-primary btn-lg" style={{flex:2}}>
                  Selanjutnya
                </button>
              ) : (
                <button type="submit" disabled={saving} className="btn btn-primary btn-lg" style={{flex:2}}>
                  {saving ? 'Menyimpan...' : <><Save size={16}/> Simpan Acara</>}
                </button>
              )}
            </div>

          </form>
        </div>
      </div>
      
      <style>{`
        .animate-slide-in { animation: slideInLeft 0.25s ease-out forwards; }
        @keyframes slideInLeft {
          from { opacity: 0; transform: translateX(10px); }
          to { opacity: 1; transform: translateX(0); }
        }
      `}</style>
    </div>
  );
};
