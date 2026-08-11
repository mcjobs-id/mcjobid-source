import React, { useState } from 'react';
import { Calendar, Plus, Search, MapPin } from 'lucide-react';
import type { Booking } from '../types';
import { getBookingName, getBookingClient, getBookingDate, getBookingFee, getBookingDp, getBookingVenue, normalizeStatus, derivePaymentStatus } from '../types';
import { FAB } from '../components/FAB';

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

    if (activeFilter === 'UPCOMING') return dateStr >= todayStr && status !== 'cancelled' && status !== 'completed';
    if (activeFilter === 'COMPLETED') return status === 'completed';
    if (activeFilter === 'DP_PENDING') {
      const fee = getBookingFee(b);
      const dp = getBookingDp(b);
      return status !== 'cancelled' && fee > 0 && dp < fee;
    }
    if (activeFilter === 'CANCELLED') return status === 'cancelled';
    return true;
  });

  // Re-sort: UPCOMING and DP_PENDING should be ascending (nearest date first)
  // ALL, COMPLETED, CANCELLED should be descending (newest first, which is the default from firebase)
  if (activeFilter === 'UPCOMING' || activeFilter === 'DP_PENDING') {
    filtered.sort((a, b) => {
      const dateA = getBookingDate(a);
      const dateB = getBookingDate(b);
      return dateA.localeCompare(dateB); // ascending
    });
  }

  const filterLabels: Record<AgendaFilter, string> = {
    ALL: 'Semua Job', UPCOMING: 'Mendatang', COMPLETED: 'Selesai', DP_PENDING: 'Belum Lunas', CANCELLED: 'Batal'
  };

  return (
    <div className="animate-fade-in" style={{width:'100%', paddingBottom:'16px'}}>
      {/* Search + Filter Bar */}
      <div className="card" style={{padding:'14px 16px', marginBottom:'20px'}}>
        <div style={{position:'relative', width:'100%', marginBottom:'12px'}}>
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
              <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto'}}>
                {searchQuery ? 'Coba ubah kata kunci atau filter.' : 'Tambah job pertama Anda menggunakan tombol Buat Job di atas.'}
              </p>
            </div>
          </div>
        </div>
      ) : (
        <div>
          {(() => {
            // Group by month
            const groups: Record<string, Booking[]> = {};
            filtered.forEach(b => {
              const dateStr = getBookingDate(b);
              let groupKey = 'TBD / Belum Ditentukan';
              if (dateStr) {
                try {
                  const d = new Date(dateStr + 'T00:00:00');
                  groupKey = d.toLocaleDateString('id-ID', { month: 'long', year: 'numeric' });
                } catch {
                  // Ignore
                }
              }
              if (!groups[groupKey]) groups[groupKey] = [];
              groups[groupKey].push(b);
            });

            return Object.keys(groups).map(groupKey => (
              <div key={groupKey} style={{ marginBottom: '24px' }}>
                <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '12px', paddingBottom: '8px', borderBottom: '2px solid var(--border)' }}>
                  {groupKey}
                </h2>
                <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(280px, 1fr))', gap:'14px'}}>
                  {groups[groupKey].map(b => {
                    const fee = getBookingFee(b);
                    const status = normalizeStatus(b.status as string);
                    const paymentStatus = derivePaymentStatus(b);
                    
                    let badgeLabel = 'TBD';
                    let badgeClass = 'badge-secondary';
                    if (paymentStatus === 'TBD') {
                       badgeLabel = 'HARGA TBD';
                       badgeClass = 'badge-secondary';
                    } else if (paymentStatus === 'PAID') {
                       badgeLabel = 'LUNAS';
                       badgeClass = 'badge-success';
                    } else if (paymentStatus === 'PARTIAL') {
                       badgeLabel = 'DP';
                       badgeClass = 'badge-warning';
                    } else if (paymentStatus === 'UNPAID') {
                       badgeLabel = 'BELUM BAYAR';
                       badgeClass = 'badge-error';
                    } else if (paymentStatus === 'OVERDUE') {
                       badgeLabel = 'JATUH TEMPO';
                       badgeClass = 'badge-error';
                    }
                    if (status === 'cancelled') {
                       badgeLabel = 'BATAL';
                       badgeClass = 'badge-error';
                    }

                    return (
                      <div key={b.id} className="card card-interactive" onClick={() => onOpenDetail(b)} style={{padding:'18px', display:'flex', flexDirection:'column', gap:'12px', opacity: status === 'cancelled' ? 0.6 : 1}}>
                        {/* Top row: category + status */}
                        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', gap:'8px'}}>
                          <span className="badge badge-primary">{b.category || 'Wedding'}</span>
                          <span className={`badge badge-dot ${badgeClass}`}>
                            {badgeLabel}
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
              </div>
            ));
          })()}
        </div>
      )}
      {/* ── FLOATING ACTION BUTTON ── */}
      <FAB onClick={onOpenCreateJob} label="Buat Job" />
    </div>
  );
};
