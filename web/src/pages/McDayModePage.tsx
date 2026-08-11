import React, { useState, useEffect } from 'react';
import { Mic, ArrowLeft, Clock, Calendar, MapPin, ChevronRight } from 'lucide-react';
import type { Booking } from '../types';

interface McDayModePageProps {
  booking: Booking | null;
  allBookings: Booking[];
  onBack: () => void;
  onSelectBooking: (booking: Booking) => void;
}

export const McDayModePage: React.FC<McDayModePageProps> = ({ booking, allBookings, onBack, onSelectBooking }) => {
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit', second: '2-digit' }).replace(/\./g, ':');
  };

  // If no booking selected — show picker
  if (!booking) {
    const todayStr = new Date().toISOString().split('T')[0];
    const upcoming = allBookings
      .filter(b => b.status !== 'CANCELLED' && b.eventDate >= todayStr)
      .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());

    return (
      <div className="animate-fade-in" style={{width:'100%', paddingBottom:'24px'}}>
        <div style={{display:'flex', alignItems:'center', gap:'12px', marginBottom:'24px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <h1 className="page-title">Pilih Acara untuk Day Mode</h1>
        </div>

        {upcoming.length === 0 ? (
          <div className="empty-state card">
            <Mic size={24} className="empty-state-icon" />
            <p style={{fontSize:'14px', fontWeight:'600'}}>Belum ada acara mendatang</p>
          </div>
        ) : (
          <div style={{display:'grid', gap:'12px'}}>
            {upcoming.map(b => (
              <div key={b.id} onClick={() => onSelectBooking(b)} className="card card-interactive" style={{padding:'16px', display:'flex', alignItems:'center', gap:'12px'}}>
                <div style={{width:'40px', height:'40px', borderRadius:'12px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                  <Mic size={18} color="var(--primary)" />
                </div>
                <div style={{flex:1}}>
                  <h3 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)'}}>{b.eventTitle || b.clientName}</h3>
                  <p style={{fontSize:'12px', color:'var(--text-3)'}}>{b.eventDate} • {b.venue || 'TBA'}</p>
                </div>
                <ChevronRight size={18} color="var(--text-4)" />
              </div>
            ))}
          </div>
        )}
      </div>
    );
  }

  // ── DAY MODE VIEW — Clean White Design ──
  return (
    <div className="animate-fade-in" style={{
      width:'100%', minHeight:'100vh', padding:'24px',
      background:'linear-gradient(160deg, #fafafa 0%, #f3f0ff 100%)',
      display:'flex', flexDirection:'column',
      position:'relative', overflow:'hidden'
    }}>
      {/* Decorative top accent bar */}
      <div style={{position:'absolute', top:0, left:0, right:0, height:'4px', background:'linear-gradient(90deg, #4F46E5, #7C3AED, #9333EA)', borderRadius:'0'}} />

      {/* Subtle background orb */}
      <div style={{position:'absolute', top:'-100px', right:'-100px', width:'350px', height:'350px', borderRadius:'50%', background:'radial-gradient(circle, rgba(139,92,246,0.08) 0%, transparent 70%)', pointerEvents:'none'}} />

      {/* ── TOP NAV ── */}
      <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'28px', position:'relative', zIndex:1}}>
        <button
          onClick={onBack}
          style={{background:'white', border:'1px solid #E5E7EB', padding:'8px 16px', borderRadius:'99px', color:'#374151', display:'flex', alignItems:'center', gap:'8px', fontSize:'13px', fontWeight:'600', cursor:'pointer', boxShadow:'0 1px 4px rgba(0,0,0,0.06)'}}
        >
          <ArrowLeft size={15} /> Keluar Day Mode
        </button>
        <div style={{display:'flex', alignItems:'center', gap:'8px', background:'white', padding:'6px 14px', borderRadius:'99px', border:'1px solid #E5E7EB', boxShadow:'0 1px 4px rgba(0,0,0,0.06)'}}>
          <span style={{width:'8px', height:'8px', borderRadius:'50%', background:'#22C55E', boxShadow:'0 0 10px rgba(34,197,94,0.6)', animation:'pulse 2s infinite'}} />
          <span style={{fontSize:'11px', fontWeight:'800', textTransform:'uppercase', letterSpacing:'0.1em', color:'#374151'}}>Live</span>
        </div>
      </div>

      {/* ── BIG CLOCK ── */}
      <div style={{textAlign:'center', marginBottom:'32px', position:'relative', zIndex:1}}>
        <div style={{
          fontSize:'clamp(64px, 16vw, 128px)',
          fontWeight:'900',
          fontVariantNumeric:'tabular-nums',
          letterSpacing:'-0.04em',
          lineHeight:'1',
          background:'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
          WebkitBackgroundClip:'text',
          WebkitTextFillColor:'transparent',
          backgroundClip:'text',
          filter:'drop-shadow(0 4px 12px rgba(79,70,229,0.15))'
        }}>
          {formatTime(time)}
        </div>
      </div>

      {/* ── EVENT INFO CARD ── */}
      <div style={{
        maxWidth:'800px', margin:'0 auto', width:'100%',
        background:'white',
        borderRadius:'24px',
        padding:'28px',
        boxShadow:'0 4px 24px rgba(0,0,0,0.08), 0 1px 3px rgba(0,0,0,0.04)',
        border:'1px solid rgba(79,70,229,0.1)',
        position:'relative', zIndex:1
      }}>
        {/* Badge */}
        <span style={{
          display:'inline-flex', alignItems:'center', gap:'6px',
          padding:'5px 14px',
          background:'linear-gradient(135deg, #EEF2FF, #F5F3FF)',
          border:'1px solid rgba(79,70,229,0.2)',
          color:'#4F46E5',
          borderRadius:'99px', fontSize:'11px', fontWeight:'800', letterSpacing:'0.08em', marginBottom:'16px'
        }}>
          <span style={{width:'6px', height:'6px', borderRadius:'50%', background:'#22C55E', boxShadow:'0 0 6px rgba(34,197,94,0.6)', flexShrink:0}} />
          SEDANG BERJALAN
        </span>

        <h1 style={{fontSize:'clamp(20px, 4vw, 32px)', fontWeight:'800', color:'#111827', letterSpacing:'-0.02em', marginBottom:'4px', lineHeight:'1.2'}}>
          {booking.eventTitle || booking.clientName}
        </h1>
        <p style={{fontSize:'14px', color:'#6B7280', marginBottom:'24px'}}>Klien: {booking.clientName}</p>

        {/* Info row */}
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(140px, 1fr))', gap:'12px'}}>
          <div style={{display:'flex', alignItems:'center', gap:'12px', background:'#F9FAFB', borderRadius:'14px', padding:'14px', border:'1px solid #F3F4F6'}}>
            <div style={{width:'40px', height:'40px', borderRadius:'10px', background:'#EEF2FF', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
              <Calendar size={17} color="#4F46E5" />
            </div>
            <div>
              <span style={{fontSize:'10px', color:'#9CA3AF', display:'block', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.05em'}}>Tanggal</span>
              <span style={{fontSize:'13px', fontWeight:'700', color:'#111827'}}>{booking.eventDate}</span>
            </div>
          </div>

          <div style={{display:'flex', alignItems:'center', gap:'12px', background:'#F9FAFB', borderRadius:'14px', padding:'14px', border:'1px solid #F3F4F6'}}>
            <div style={{width:'40px', height:'40px', borderRadius:'10px', background:'#EEF2FF', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
              <Clock size={17} color="#4F46E5" />
            </div>
            <div>
              <span style={{fontSize:'10px', color:'#9CA3AF', display:'block', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.05em'}}>Waktu Mulai</span>
              <span style={{fontSize:'13px', fontWeight:'700', color:'#111827'}}>{booking.eventTime || 'TBA'} WIB</span>
            </div>
          </div>

          <div style={{display:'flex', alignItems:'center', gap:'12px', background:'#F9FAFB', borderRadius:'14px', padding:'14px', border:'1px solid #F3F4F6'}}>
            <div style={{width:'40px', height:'40px', borderRadius:'10px', background:'#EEF2FF', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
              <MapPin size={17} color="#4F46E5" />
            </div>
            <div>
              <span style={{fontSize:'10px', color:'#9CA3AF', display:'block', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.05em'}}>Lokasi</span>
              <span style={{fontSize:'13px', fontWeight:'700', color:'#111827'}}>{booking.venue || 'TBA'}</span>
            </div>
          </div>
        </div>

        {/* Cue Card */}
        <div style={{marginTop:'20px', paddingTop:'20px', borderTop:'1px solid #F3F4F6'}}>
          <h3 style={{fontSize:'11px', fontWeight:'800', textTransform:'uppercase', letterSpacing:'0.08em', color:'#9CA3AF', marginBottom:'10px'}}>📋 Catatan Khusus (Cue Card)</h3>
          <div style={{background:'#F9FAFB', border:'1px solid #F3F4F6', padding:'18px', borderRadius:'14px', fontSize:'15px', lineHeight:'1.8', color:'#374151'}}>
            {booking.notes
              ? booking.notes
              : <span style={{color:'#D1D5DB', fontStyle:'italic'}}>Tidak ada catatan khusus untuk acara ini.</span>
            }
          </div>
        </div>
      </div>
    </div>
  );
};
