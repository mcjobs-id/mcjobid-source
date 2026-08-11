import React from 'react';
import { ArrowLeft, Bell, Calendar, DollarSign, AlertCircle } from 'lucide-react';
import type { Booking } from '../types';

interface NotificationPageProps {
  onBack: () => void;
  bookings: Booking[];
}

export const NotificationPage: React.FC<NotificationPageProps> = ({ onBack, bookings }) => {
  const todayStr = new Date().toISOString().split('T')[0];

  // 1. Events tomorrow
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];
  
  const tomorrowEvents = bookings.filter(b => b.status !== 'CANCELLED' && b.eventDate === tomorrowStr);

  // 2. Unpaid events (past or upcoming)
  const unpaidEvents = bookings.filter(b => b.status !== 'CANCELLED' && b.paymentStatus !== 'PAID');

  const notifications = [
    ...tomorrowEvents.map(b => ({
      id: `evt-${b.id}`,
      type: 'event',
      title: 'Acara Besok!',
      message: `Persiapkan dirimu untuk acara ${b.eventTitle || b.clientName} di ${b.venue || 'lokasi belum ditentukan'}.`,
      time: b.eventTime || 'TBA',
    })),
    ...unpaidEvents.map(b => {
      const sisa = (b.fee || 0) - (b.dp || 0);
      return {
        id: `pay-${b.id}`,
        type: 'payment',
        title: 'Tagihan Belum Lunas',
        message: `Klien ${b.clientName} belum melunasi tagihan sebesar Rp ${sisa.toLocaleString('id-ID')}.`,
        time: b.eventDate,
      };
    })
  ];

  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <Bell size={20} color="var(--error)" />
              Pusat Pengingat
            </h1>
            <p className="page-subtitle">Notifikasi otomatis agenda H-1 dan tagihan sisa pelunasan.</p>
          </div>
        </div>
      </div>

      <div style={{display:'flex', flexDirection:'column', gap:'16px'}}>
        {notifications.length === 0 ? (
          <div className="card empty-state" style={{padding:'56px 24px'}}>
            <div className="empty-state-icon" style={{background:'var(--bg-surface-2)'}}><Bell size={24} /></div>
            <div>
              <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Tidak Ada Notifikasi Baru</p>
              <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto'}}>Semua agenda aman dan tidak ada tagihan tertunggak.</p>
            </div>
          </div>
        ) : (
          notifications.map(n => (
            <div key={n.id} className="card" style={{padding:'20px', display:'flex', gap:'16px', alignItems:'flex-start'}}>
              <div style={{
                width:'40px', height:'40px', borderRadius:'12px', flexShrink:0,
                background: n.type === 'event' ? 'var(--primary-light)' : 'var(--error-light)',
                display:'flex', alignItems:'center', justifyContent:'center'
              }}>
                {n.type === 'event' ? <Calendar size={18} color="var(--primary)" /> : <AlertCircle size={18} color="var(--error)" />}
              </div>
              <div>
                <div style={{display:'flex', alignItems:'center', gap:'12px', marginBottom:'4px'}}>
                  <h3 style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)'}}>{n.title}</h3>
                  <span style={{fontSize:'11px', color:'var(--text-4)', fontWeight:'600'}}>{n.time}</span>
                </div>
                <p style={{fontSize:'13px', color:'var(--text-2)', lineHeight:'1.5'}}>{n.message}</p>
              </div>
            </div>
          ))
        )}
      </div>

    </div>
  );
};
