import React, { useState, useEffect } from 'react';
import { Mic, ArrowLeft, Clock, Calendar, MapPin, Search, ChevronRight } from 'lucide-react';
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

  // If no booking is selected, show list to select
  if (!booking) {
    const todayStr = new Date().toISOString().split('T')[0];
    const upcoming = allBookings
      .filter(b => b.status !== 'CANCELLED' && b.eventDate >= todayStr)
      .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());

    return (
      <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
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

  // Day Mode View
  return (
    <div className="animate-fade-in" style={{
      maxWidth:'100%', margin:'-24px -32px', padding:'24px 32px', minHeight:'100vh',
      background:'linear-gradient(135deg, #09090b 0%, #18181b 100%)', color:'white',
      display:'flex', flexDirection:'column'
    }}>
      {/* ── TOP NAV ── */}
      <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'32px'}}>
        <button onClick={onBack} style={{background:'rgba(255,255,255,0.1)', border:'1px solid rgba(255,255,255,0.2)', padding:'8px 16px', borderRadius:'99px', color:'white', display:'flex', alignItems:'center', gap:'8px', fontSize:'13px', fontWeight:'600', cursor:'pointer'}}>
          <ArrowLeft size={16} /> Keluar Day Mode
        </button>
        <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
          <span style={{width:'8px', height:'8px', borderRadius:'50%', background:'var(--success)', boxShadow:'0 0 12px var(--success)'}} />
          <span style={{fontSize:'11px', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.1em', color:'rgba(255,255,255,0.6)'}}>Live</span>
        </div>
      </div>

      <div style={{maxWidth:'800px', margin:'0 auto', width:'100%'}}>
        {/* ── BIG CLOCK ── */}
        <div style={{textAlign:'center', marginBottom:'48px'}}>
          <div style={{fontSize:'clamp(64px, 12vw, 120px)', fontWeight:'800', fontVariantNumeric:'tabular-nums', letterSpacing:'-0.03em', lineHeight:'1', textShadow:'0 0 40px rgba(255,255,255,0.1)'}}>
            {formatTime(time)}
          </div>
        </div>

        {/* ── EVENT INFO CARD ── */}
        <div style={{background:'rgba(255,255,255,0.05)', backdropFilter:'blur(20px)', border:'1px solid rgba(255,255,255,0.1)', borderRadius:'24px', padding:'32px'}}>
          <span style={{display:'inline-block', padding:'4px 12px', background:'rgba(79,70,229,0.3)', border:'1px solid rgba(79,70,229,0.5)', color:'#A5B4FC', borderRadius:'99px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.05em', marginBottom:'16px'}}>
            SEDANG BERJALAN
          </span>
          <h1 style={{fontSize:'clamp(24px, 4vw, 36px)', fontWeight:'800', letterSpacing:'-0.02em', marginBottom:'8px', lineHeight:'1.2'}}>
            {booking.eventTitle || booking.clientName}
          </h1>
          <p style={{fontSize:'15px', color:'rgba(255,255,255,0.5)', marginBottom:'32px'}}>Klien: {booking.clientName}</p>

          <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))', gap:'20px'}}>
            <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
              <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'rgba(255,255,255,0.1)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <Calendar size={20} color="white" />
              </div>
              <div>
                <span style={{fontSize:'12px', color:'rgba(255,255,255,0.5)', display:'block'}}>Tanggal</span>
                <span style={{fontSize:'15px', fontWeight:'600'}}>{booking.eventDate}</span>
              </div>
            </div>
            
            <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
              <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'rgba(255,255,255,0.1)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <Clock size={20} color="white" />
              </div>
              <div>
                <span style={{fontSize:'12px', color:'rgba(255,255,255,0.5)', display:'block'}}>Waktu Mulai</span>
                <span style={{fontSize:'15px', fontWeight:'600'}}>{booking.eventTime || 'TBA'} WIB</span>
              </div>
            </div>

            <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
              <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'rgba(255,255,255,0.1)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <MapPin size={20} color="white" />
              </div>
              <div>
                <span style={{fontSize:'12px', color:'rgba(255,255,255,0.5)', display:'block'}}>Lokasi</span>
                <span style={{fontSize:'15px', fontWeight:'600'}}>{booking.venue || 'TBA'}</span>
              </div>
            </div>
          </div>

          {/* ── NOTES / CUE CARD ── */}
          <div style={{marginTop:'32px', paddingTop:'32px', borderTop:'1px solid rgba(255,255,255,0.1)'}}>
            <h3 style={{fontSize:'12px', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.05em', color:'rgba(255,255,255,0.4)', marginBottom:'12px'}}>Catatan Khusus (Cue Card)</h3>
            <div style={{background:'rgba(0,0,0,0.3)', padding:'24px', borderRadius:'16px', fontSize:'15px', lineHeight:'1.7', color:'rgba(255,255,255,0.9)'}}>
              {booking.notes ? booking.notes : <span style={{color:'rgba(255,255,255,0.3)', fontStyle:'italic'}}>Tidak ada catatan khusus untuk acara ini.</span>}
            </div>
          </div>
        </div>
      </div>
      
      <style>{`
        /* Remove mobile padding for full immersion */
        @media (max-width: 767px) {
          .animate-fade-in { margin: -16px; padding: 16px; min-height: 100vh; }
        }
      `}</style>
    </div>
  );
};
