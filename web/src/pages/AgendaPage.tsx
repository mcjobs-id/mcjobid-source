import React, { useState } from 'react';
import { Calendar, Plus, Search, MapPin } from 'lucide-react';
import type { Booking } from '../types';
import { getBookingName, getBookingClient, getBookingDate, getBookingFee, getBookingDp, getBookingVenue, normalizeStatus } from '../types';

interface AgendaPageProps {
  bookings: Booking[];
  onSaveBooking: (booking: Booking) => void;
  onOpenDetail: (booking: Booking) => void;
  onOpenCreateJob: () => void;
}

type AgendaFilter = 'ALL' | 'UPCOMING' | 'COMPLETED' | 'DP_PENDING' | 'CANCELLED';

function formatRpFull(val: number) {
  return `Rp ${val.toLocaleString('id-ID')}`;
}

export const AgendaPage: React.FC<AgendaPageProps> = ({ bookings, onOpenDetail, onOpenCreateJob }) => {
  const [activeFilter, setActiveFilter] = useState<AgendaFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const todayStr = new Date().toISOString().split('T')[0];

  const filtered = bookings.filter(b => {
    const q = searchQuery.toLowerCase();
    const name = getBookingName(b).toLowerCase();
    const client = getBookingClient(b).toLowerCase();
    const venue = getBookingVenue(b).toLowerCase();
    const matchSearch = !q || name.includes(q) || client.includes(q) || venue.includes(q);
    if (!matchSearch) return false;

    const dateStr = getBookingDate(b);
    const status = normalizeStatus(b.status as string);

    if (activeFilter === 'UPCOMING') return dateStr >= todayStr && status !== 'cancelled';
    if (activeFilter === 'COMPLETED') return status === 'completed';
    if (activeFilter === 'DP_PENDING') {
      const fee = getBookingFee(b);
      const dp = getBookingDp(b);
      return status !== 'cancelled' && fee > 0 && dp < fee;
    }
    if (activeFilter === 'CANCELLED') return status === 'cancelled';
    return true;
  });

  const filterLabels: Record<AgendaFilter, string> = {
    ALL: 'Semua Job', UPCOMING: 'Mendatang', COMPLETED: 'Selesai', DP_PENDING: 'Belum Lunas', CANCELLED: 'Batal'
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'1280px', margin:'0 auto', paddingBottom:'16px'}}>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
            <Calendar size={20} color="var(--primary)" />
            Agenda Manggung & Jadwal MC
          </h1>
          <p className="page-subtitle">Daftar lengkap jadwal acara, venue, honorarium, dan status pelunasan.</p>
        </div>
        <button onClick={onOpenCreateJob} className="btn btn-primary">
          <Plus size={15} />
          Tambah Job
        </button>
      </div>

      {/* Search + Filter Bar */}
      <div className="card" style={{padding:'14px 16px', marginBottom:'20px'}}>
        <div style={{position:'relative', marginBottom:'12px'}}>
          <Search size={15} style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)'}} />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Cari judul acara, klien, atau lokasi venue..."
            className="input-field"
            style={{paddingLeft:'38px'}}
          />
        </div>

        <div style={{display:'flex', gap:'8px', overflowX:'auto', paddingBottom:'2px'}} className="scrollbar-none">
          {(Object.keys(filterLabels) as AgendaFilter[]).map(f => (
            <button key={f} onClick={() => setActiveFilter(f)} className={`chip${activeFilter === f ? ' active' : ''}`}>
              {filterLabels[f]}
              {f === 'ALL' && bookings.length > 0 && (
                <span style={{marginLeft:'4px', fontSize:'10px', opacity:0.7}}>({bookings.length})</span>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Booking Grid */}
      {filtered.length === 0 ? (
        <div className="card" style={{padding:0}}>
          <div className="empty-state">
            <div className="empty-state-icon"><Calendar size={22} /></div>
            <div>
              <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>
                {searchQuery ? 'Tidak ada hasil pencarian' : 'Belum ada agenda'}
              </p>
              <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto 16px'}}>
                {searchQuery ? 'Coba ubah kata kunci atau filter.' : 'Tambah job pertama Anda untuk mulai mengelola jadwal profesional.'}
              </p>
            </div>
            {!searchQuery && (
              <button onClick={onOpenCreateJob} className="btn btn-primary btn-sm">
                <Plus size={14} /> Tambah Job Baru
              </button>
            )}
          </div>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(280px, 1fr))', gap:'14px'}}>
          {filtered.map(b => {
            const fee = getBookingFee(b);
            const dp = getBookingDp(b);
            const outstanding = Math.max(0, fee - dp);
            const isLunas = fee > 0 && outstanding <= 0;
            const hasDP = dp > 0 && !isLunas;
            const status = normalizeStatus(b.status as string);
            return (
              <div key={b.id} className="card card-interactive" onClick={() => onOpenDetail(b)} style={{padding:'18px', display:'flex', flexDirection:'column', gap:'12px', opacity: status === 'cancelled' ? 0.6 : 1}}>
                {/* Top row: category + status */}
                <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', gap:'8px'}}>
                  <span className="badge badge-primary">{b.category || 'Wedding'}</span>
                  <span className={`badge badge-dot ${isLunas ? 'badge-success' : hasDP ? 'badge-warning' : status === 'cancelled' ? 'badge-error' : 'badge-error'}`}>
                    {isLunas ? 'LUNAS' : hasDP ? 'DP' : status === 'cancelled' ? 'BATAL' : 'BELUM BAYAR'}
                  </span>
                </div>

                {/* Title + Client */}
                <div>
                  <h3 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', marginBottom:'2px'}}>
                    {getBookingName(b)}
                  </h3>
                  <p style={{fontSize:'12px', color:'var(--text-3)'}}>Klien: <span style={{fontWeight:'600', color:'var(--text-2)'}}>{getBookingClient(b) || '-'}</span></p>
                </div>

                {/* Date + Venue */}
                <div style={{display:'flex', flexDirection:'column', gap:'5px'}}>
                  <span style={{fontSize:'12px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'6px'}}>
                    <Calendar size={12} color="var(--primary)" />
                    {getBookingDate(b) || 'Tanggal TBD'} {b.start || b.eventTime ? `• ${b.start || b.eventTime}` : ''}
                  </span>
                  {getBookingVenue(b) && (
                    <span style={{fontSize:'12px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'6px', overflow:'hidden'}}>
                      <MapPin size={12} color="var(--error)" style={{flexShrink:0}} />
                      <span style={{overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{getBookingVenue(b)}</span>
                    </span>
                  )}
                </div>

                {/* Divider + Fee */}
                <div style={{borderTop:'1px solid var(--border)', paddingTop:'12px', display:'flex', alignItems:'center', justifyContent:'space-between'}}>
                  <span style={{fontSize:'11px', color:'var(--text-4)'}}>Honorarium</span>
                  <span style={{fontSize:'16px', fontWeight:'700', color:'var(--primary)', fontVariantNumeric:'tabular-nums', letterSpacing:'-0.01em'}}>
                    {formatRpFull(fee)}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
