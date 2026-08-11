import React, { useState } from 'react';
import { ArrowLeft, MessageSquare, Search, Copy, CheckCircle2, AlertCircle, Send, Phone, Calendar, DollarSign } from 'lucide-react';
import type { Booking, Client } from '../types';
import { getBookingName, getBookingClient, getBookingDate, getBookingFee, getBookingDp, getBookingOutstanding } from '../types';

interface FollowUpPageProps {
  onBack: () => void;
  bookings: Booking[];
  clients?: Client[];
}

export const FollowUpPage: React.FC<FollowUpPageProps> = ({ onBack, bookings, clients = [] }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'ALL' | 'PAYMENT' | 'EVENT'>('ALL');

  const todayStr = new Date().toISOString().split('T')[0];

  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  // Filter targets
  const followUpTargets = bookings.filter(b => {
    if (b.status === 'CANCELLED' || b.status === 'cancelled') return false;
    
    const outstanding = getBookingOutstanding(b);
    const isUnpaid = outstanding > 0;
    
    // Check if event is within next 7 days
    const eventTime = new Date(getBookingDate(b) || todayStr).getTime();
    const todayTime = new Date(todayStr).getTime();
    const diffDays = (eventTime - todayTime) / (1000 * 3600 * 24);
    const isUpcomingH7 = diffDays >= 0 && diffDays <= 7;

    if (activeTab === 'PAYMENT' && !isUnpaid) return false;
    if (activeTab === 'EVENT' && !isUpcomingH7) return false;

    const title = getBookingName(b);
    const clientName = getBookingClient(b);
    const matchSearch = !searchQuery || 
      clientName.toLowerCase().includes(searchQuery.toLowerCase()) || 
      title.toLowerCase().includes(searchQuery.toLowerCase());

    return (isUnpaid || isUpcomingH7) && matchSearch;
  });

  const generateMessage = (b: Booking) => {
    const outstanding = getBookingOutstanding(b);
    const title = getBookingName(b);
    const clientName = getBookingClient(b) || 'Kak';
    const eventDate = getBookingDate(b);

    if (outstanding > 0) {
      return `Halo Kak ${clientName},\n\nSemoga kabarnya sehat selalu. Mengingatkan kembali untuk agenda *${title}* pada tanggal *${eventDate}*.\n\nSebagai catatan administrasi, masih terdapat sisa pelunasan sebesar *${formatRp(outstanding)}*.\n\nMohon konfirmasinya untuk proses pelunasan ya kak. Terima kasih banyak! 🙏`;
    } else {
      return `Halo Kak ${clientName},\n\nSemoga kabarnya sehat selalu. Mengonfirmasi kembali untuk kesiapan acara *${title}* pada tanggal *${eventDate}*.\n\nUntuk pembayaran sudah lunas. Kira-kira apakah ada update terbaru mengenai rundown atau technical meeting H-7 kak?\n\nTerima kasih banyak! 🙏`;
    }
  };

  const copyMessage = (b: Booking) => {
    navigator.clipboard.writeText(generateMessage(b));
    setCopiedId(b.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const openWhatsApp = (b: Booking) => {
    const clientName = getBookingClient(b);
    const matchedClient = clients.find(c => c.name.toLowerCase() === clientName.toLowerCase());
    const phone = matchedClient?.phone || b.pic || '';
    const cleanPhone = phone.replace(/[^0-9]/g, '');
    const encodedText = encodeURIComponent(generateMessage(b));
    
    if (cleanPhone) {
      const formattedPhone = cleanPhone.startsWith('0') ? `62${cleanPhone.slice(1)}` : cleanPhone;
      window.open(`https://api.whatsapp.com/send?phone=${formattedPhone}&text=${encodedText}`, '_blank');
    } else {
      window.open(`https://api.whatsapp.com/send?text=${encodedText}`, '_blank');
    }
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'850px', margin:'0 auto', paddingBottom:'32px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <MessageSquare size={20} color="#2563EB" /> Pusat Follow Up Klien
            </h1>
            <p className="page-subtitle">Otomatisasi pengingat pelunasan piutang & konfirmasi H-7 acara via WA.</p>
          </div>
        </div>
      </div>

      {/* ── FILTER & SEARCH ── */}
      <div style={{display:'flex', flexDirection:'column', gap:'12px', marginBottom:'20px'}}>
        <div style={{position:'relative'}}>
          <Search size={15} style={{position:'absolute', left:'14px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)'}} />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Cari nama klien atau judul acara..."
            className="input-field"
            style={{paddingLeft:'38px'}}
          />
        </div>

        <div style={{display:'flex', gap:'8px'}}>
          <button
            onClick={() => setActiveTab('ALL')}
            className={`btn btn-sm ${activeTab === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
          >
            Semua Follow Up ({followUpTargets.length})
          </button>
          <button
            onClick={() => setActiveTab('PAYMENT')}
            className={`btn btn-sm ${activeTab === 'PAYMENT' ? 'btn-primary' : 'btn-secondary'}`}
            style={{background: activeTab === 'PAYMENT' ? 'var(--error)' : undefined, borderColor: activeTab === 'PAYMENT' ? 'var(--error)' : undefined}}
          >
            <DollarSign size={13} /> Sisa Pembayaran / Piutang
          </button>
          <button
            onClick={() => setActiveTab('EVENT')}
            className={`btn btn-sm ${activeTab === 'EVENT' ? 'btn-primary' : 'btn-secondary'}`}
          >
            <Calendar size={13} /> Konfirmasi H-7 Acara
          </button>
        </div>
      </div>

      {followUpTargets.length === 0 ? (
        <div className="card empty-state" style={{padding:'56px 24px'}}>
          <div className="empty-state-icon" style={{background:'#EFF6FF', color:'#2563EB'}}><CheckCircle2 size={24} /></div>
          <div>
            <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Semua Follow Up Aman! 🎉</p>
            <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'300px', margin:'0 auto'}}>
              Tidak ada tagihan piutang tertunggak atau acara H-7 yang memerlukan follow up saat ini.
            </p>
          </div>
        </div>
      ) : (
        <div style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          {followUpTargets.map(b => {
            const outstanding = getBookingOutstanding(b);
            const clientName = getBookingClient(b) || 'Klien MC';
            const matchedClient = clients.find(c => c.name.toLowerCase() === clientName.toLowerCase());
            const phone = matchedClient?.phone || b.pic || '';

            return (
              <div 
                key={b.id} 
                className="card" 
                style={{
                  padding:'20px 24px', 
                  borderLeft: outstanding > 0 ? '5px solid var(--error)' : '5px solid #2563EB',
                  position:'relative'
                }}
              >
                <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:'14px', flexWrap:'wrap', gap:'8px'}}>
                  <div>
                    <div style={{display:'flex', alignItems:'center', gap:'8px', marginBottom:'4px'}}>
                      <span className={`badge ${outstanding > 0 ? 'badge-error' : 'badge-primary'}`} style={{fontSize:'11px'}}>
                        {outstanding > 0 ? 'FOLLOW UP PELUNASAN' : 'KONFIRMASI ACARA H-7'}
                      </span>
                      <span style={{fontSize:'12px', color:'var(--text-3)'}}>• {getBookingDate(b)}</span>
                    </div>

                    <h3 style={{fontSize:'17px', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.01em'}}>{clientName}</h3>
                    <p style={{fontSize:'13px', color:'var(--text-2)', marginTop:'2px'}}>{getBookingName(b)}</p>
                    
                    {phone && (
                      <p style={{fontSize:'12px', color:'var(--primary)', fontWeight:'600', marginTop:'4px', display:'flex', alignItems:'center', gap:'4px'}}>
                        <Phone size={13} /> {phone}
                      </p>
                    )}
                  </div>

                  {outstanding > 0 && (
                    <div style={{textAlign:'right', background:'var(--error-light)', padding:'8px 14px', borderRadius:'10px'}}>
                      <span style={{fontSize:'11px', fontWeight:'700', color:'var(--error)', textTransform:'uppercase'}}>SISA PIUTANG:</span>
                      <h4 style={{fontSize:'18px', fontWeight:'800', color:'var(--error)', fontVariantNumeric:'tabular-nums'}}>
                        {formatRp(outstanding)}
                      </h4>
                    </div>
                  )}
                </div>

                <div style={{background:'var(--bg-surface-2)', padding:'14px', borderRadius:'12px', border:'1px solid var(--border)', marginBottom:'16px'}}>
                  <p style={{fontSize:'12.5px', color:'var(--text-2)', lineHeight:'1.5', whiteSpace:'pre-wrap', fontFamily:'sans-serif'}}>
                    {generateMessage(b)}
                  </p>
                </div>

                <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'10px'}}>
                  <button 
                    onClick={() => copyMessage(b)}
                    className="btn btn-secondary btn-full btn-sm"
                    style={{color: copiedId === b.id ? 'var(--success)' : 'var(--text-2)', borderColor: copiedId === b.id ? 'var(--success)' : 'var(--border)'}}
                  >
                    {copiedId === b.id ? <><CheckCircle2 size={15} /> Tersalin!</> : <><Copy size={15} /> Salin Teks</>}
                  </button>

                  <button 
                    onClick={() => openWhatsApp(b)}
                    className="btn btn-primary btn-full btn-sm"
                    style={{background:'#059669', borderColor:'#059669'}}
                  >
                    <Send size={15} /> Follow Up Chat WA 🚀
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
