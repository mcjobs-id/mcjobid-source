import React, { useState } from 'react';
import { ArrowLeft, MessageSquare, Search, Copy, CheckCircle2, AlertCircle, Send, Phone } from 'lucide-react';
import type { Booking, Client } from '../types';

interface FollowUpPageProps {
  onBack: () => void;
  bookings: Booking[];
  clients?: Client[];
}

export const FollowUpPage: React.FC<FollowUpPageProps> = ({ onBack, bookings, clients = [] }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const todayStr = new Date().toISOString().split('T')[0];

  // Kriteria Follow Up: Belum lunas ATAU acara H-7
  const followUpTargets = bookings.filter(b => {
    if (b.status === 'CANCELLED') return false;
    const isPending = b.paymentStatus !== 'PAID';
    
    // Check if event is within next 7 days
    const eventTime = new Date(b.eventDate || b.date || todayStr).getTime();
    const todayTime = new Date(todayStr).getTime();
    const diffDays = (eventTime - todayTime) / (1000 * 3600 * 24);
    const isUpcomingH7 = diffDays >= 0 && diffDays <= 7;

    const title = b.eventTitle || b.name || '';
    const clientName = b.clientName || b.client || '';
    const matchSearch = clientName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                        title.toLowerCase().includes(searchQuery.toLowerCase());

    return (isPending || isUpcomingH7) && matchSearch;
  });

  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  const generateMessage = (b: Booking) => {
    const total = b.fee || 0;
    const dp = b.dp || 0;
    const sisa = total - dp;
    const title = b.eventTitle || b.name || 'Event';
    const clientName = b.clientName || b.client || 'Kak';
    const eventDate = b.eventDate || b.date || '';

    return `Halo Kak ${clientName},\n\nSemoga kabarnya baik ya kak. Saya ingin mengkonfirmasi kembali untuk acara *${title}* pada tanggal *${eventDate}*.\n\n${b.paymentStatus !== 'PAID' && sisa > 0 ? `Sebagai pengingat, masih terdapat sisa pelunasan sebesar *${formatRp(sisa)}*.` : 'Untuk administrasi pembayaran sudah tercatat lunas.'}\n\nKira-kira apakah ada update terbaru mengenai rundown atau teknis acara kak?\n\nTerima kasih banyak 🙏`;
  };

  const copyMessage = (b: Booking) => {
    navigator.clipboard.writeText(generateMessage(b));
    setCopiedId(b.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const openWhatsApp = (b: Booking) => {
    const clientName = b.clientName || b.client || '';
    // Find client phone number from clients database or booking object
    const matchedClient = clients.find(c => c.name.toLowerCase() === clientName.toLowerCase());
    const phone = matchedClient?.phone || '';
    const cleanPhone = phone.replace(/[^0-9]/g, '');
    const encodedText = encodeURIComponent(generateMessage(b));
    
    if (cleanPhone) {
      const formattedPhone = cleanPhone.startsWith('0') ? `62${cleanPhone.slice(1)}` : cleanPhone;
      window.open(`https://wa.me/${formattedPhone}?text=${encodedText}`, '_blank');
    } else {
      window.open(`https://wa.me/?text=${encodedText}`, '_blank');
    }
  };

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
              <MessageSquare size={20} color="#2563EB" />
              Pusat Follow Up Klien
            </h1>
            <p className="page-subtitle">Daftar klien yang perlu di-follow up (H-7 atau belum lunas).</p>
          </div>
        </div>
      </div>

      <div className="card" style={{padding:'14px 16px', marginBottom:'20px'}}>
        <div style={{position:'relative'}}>
          <Search size={15} style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)'}} />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Cari nama klien atau judul acara..."
            className="input-field"
            style={{paddingLeft:'38px'}}
          />
        </div>
      </div>

      {followUpTargets.length === 0 ? (
        <div className="card empty-state" style={{padding:'56px 24px'}}>
          <div className="empty-state-icon" style={{background:'#EFF6FF', color:'#2563EB'}}><CheckCircle2 size={24} /></div>
          <div>
            <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Semua Aman!</p>
            <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto'}}>
              Tidak ada klien yang perlu di-follow up saat ini (tidak ada tagihan tertunggak atau acara H-7).
            </p>
          </div>
        </div>
      ) : (
        <div style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          {followUpTargets.map(b => {
            const total = b.fee || 0;
            const dp = b.dp || 0;
            const sisa = total - dp;
            const clientName = b.clientName || b.client || 'Klien';
            const matchedClient = clients.find(c => c.name.toLowerCase() === clientName.toLowerCase());

            return (
              <div key={b.id} className="card" style={{padding:'20px'}}>
                <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:'16px', flexWrap:'wrap', gap:'8px'}}>
                  <div>
                    <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', marginBottom:'4px'}}>{clientName}</h3>
                    <p style={{fontSize:'13px', color:'var(--text-3)'}}>{b.eventTitle || b.name || 'Acara'} • {b.eventDate || b.date}</p>
                    {matchedClient?.phone && (
                      <p style={{fontSize:'12px', color:'var(--primary)', fontWeight:'600', marginTop:'2px', display:'flex', alignItems:'center', gap:'4px'}}>
                        <Phone size={12} /> {matchedClient.phone}
                      </p>
                    )}
                  </div>
                  {b.paymentStatus !== 'PAID' && sisa > 0 && (
                    <span className="badge badge-error" style={{gap:'4px'}}>
                      <AlertCircle size={12} /> Sisa {formatRp(sisa)}
                    </span>
                  )}
                </div>

                <div style={{background:'var(--bg-surface-2)', padding:'16px', borderRadius:'12px', border:'1px solid var(--border)', marginBottom:'16px'}}>
                  <p style={{fontSize:'13px', color:'var(--text-2)', lineHeight:'1.6', whiteSpace:'pre-wrap'}}>
                    {generateMessage(b)}
                  </p>
                </div>

                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'10px'}}>
                  <button 
                    onClick={() => copyMessage(b)}
                    className="btn btn-secondary btn-full"
                    style={{color: copiedId === b.id ? 'var(--success)' : 'var(--text-2)', borderColor: copiedId === b.id ? 'var(--success)' : 'var(--border)'}}
                  >
                    {copiedId === b.id ? <><CheckCircle2 size={15} /> Tersalin</> : <><Copy size={15} /> Salin Pesan</>}
                  </button>

                  <button 
                    onClick={() => openWhatsApp(b)}
                    className="btn btn-primary btn-full"
                    style={{background:'#25D366', borderColor:'#25D366'}}
                  >
                    <Send size={15} /> Buka WhatsApp
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

    </div>
  );
};
