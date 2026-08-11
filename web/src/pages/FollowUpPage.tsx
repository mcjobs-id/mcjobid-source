import React, { useState } from 'react';
import { ArrowLeft, MessageSquare, Search, Copy, CheckCircle2, AlertCircle } from 'lucide-react';
import type { Booking } from '../types';

interface FollowUpPageProps {
  onBack: () => void;
  bookings: Booking[];
}

export const FollowUpPage: React.FC<FollowUpPageProps> = ({ onBack, bookings }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const todayStr = new Date().toISOString().split('T')[0];

  // Kriteria Follow Up: Belum lunas ATAU acara H-7
  const followUpTargets = bookings.filter(b => {
    if (b.status === 'CANCELLED') return false;
    const isPending = b.paymentStatus !== 'PAID';
    
    // Check if event is within next 7 days
    const eventTime = new Date(b.eventDate).getTime();
    const todayTime = new Date(todayStr).getTime();
    const diffDays = (eventTime - todayTime) / (1000 * 3600 * 24);
    const isUpcomingH7 = diffDays >= 0 && diffDays <= 7;

    const matchSearch = b.clientName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                        (b.eventTitle && b.eventTitle.toLowerCase().includes(searchQuery.toLowerCase()));

    return (isPending || isUpcomingH7) && matchSearch;
  });

  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  const generateMessage = (b: Booking) => {
    const sisa = (b.totalFee || 0) - (b.dpAmount || 0);
    return `Halo Kak ${b.clientName},\n\nSemoga kabarnya baik ya kak. Saya ingin mengkonfirmasi kembali untuk acara *${b.eventTitle || 'Event'}* pada tanggal *${b.eventDate}*.\n\n${b.paymentStatus !== 'PAID' ? `Sebagai pengingat, masih terdapat sisa pelunasan sebesar *${formatRp(sisa)}*.` : 'Untuk administrasi sudah lunas.'}\n\nKira-kira apakah ada update terbaru mengenai rundown atau teknis acara kak?\n\nTerima kasih banyak 🙏`;
  };

  const copyMessage = (b: Booking) => {
    navigator.clipboard.writeText(generateMessage(b));
    setCopiedId(b.id);
    setTimeout(() => setCopiedId(null), 2000);
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
              Pusat Follow Up
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
            const sisa = (b.totalFee || 0) - (b.dpAmount || 0);
            return (
              <div key={b.id} className="card" style={{padding:'20px'}}>
                <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:'16px'}}>
                  <div>
                    <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', marginBottom:'4px'}}>{b.clientName}</h3>
                    <p style={{fontSize:'13px', color:'var(--text-3)'}}>{b.eventTitle || 'Acara'} • {b.eventDate}</p>
                  </div>
                  {b.paymentStatus !== 'PAID' && (
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

                <button 
                  onClick={() => copyMessage(b)}
                  className="btn btn-secondary btn-full"
                  style={{color: copiedId === b.id ? 'var(--success)' : 'var(--text-2)', borderColor: copiedId === b.id ? 'var(--success)' : 'var(--border)'}}
                >
                  {copiedId === b.id ? <><CheckCircle2 size={16} /> Pesan Tersalin</> : <><Copy size={16} /> Salin Pesan Follow Up</>}
                </button>
              </div>
            )
          })}
        </div>
      )}

    </div>
  );
};
