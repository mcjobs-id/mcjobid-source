import React, { useState } from 'react';
import { 
  ArrowLeft, Edit, Trash2, Calendar, MapPin, 
  Clock, Users, FileText, CheckCircle, 
  DollarSign, Mic, CreditCard, ChevronRight 
} from 'lucide-react';
import type { Booking } from '../types';
import { Modal } from '../components/Modal';

interface BookingDetailPageProps {
  booking: Booking;
  onBack: () => void;
  onEdit: (b: Booking) => void;
  onDelete: (id: string) => void;
  onOpenInvoice: (b: Booking) => void;
  onOpenMcDayMode: (b: Booking) => void;
}

function formatRp(val: number) {
  return `Rp ${val.toLocaleString('id-ID')}`;
}

export const BookingDetailPage: React.FC<BookingDetailPageProps> = ({
  booking, onBack, onEdit, onDelete, onOpenInvoice, onOpenMcDayMode
}) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const isPaid = booking.paymentStatus === 'PAID';
  const isDP   = !isPaid && booking.dpAmount > 0;
  
  const sisaBayar = (booking.totalFee || 0) - (booking.dpAmount || 0);

  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── TOP NAV ── */}
      <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'24px'}}>
        <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px', color:'var(--text-2)'}}>
          <ArrowLeft size={16} /> Kembali
        </button>
        <div style={{display:'flex', gap:'8px'}}>
          <button onClick={() => onEdit(booking)} className="btn btn-secondary btn-sm"><Edit size={14} /> Edit</button>
          <button onClick={() => setShowDeleteModal(true)} className="btn btn-destructive btn-sm" style={{padding:'0 8px'}}><Trash2 size={14} /></button>
        </div>
      </div>

      {/* ── HEADER CARD ── */}
      <div className="card" style={{padding:'24px', marginBottom:'20px', position:'relative', overflow:'hidden'}}>
        <div style={{position:'absolute', top:0, left:0, bottom:0, width:'4px', background: isPaid ? 'var(--success)' : isDP ? 'var(--warning)' : 'var(--error)'}} />
        
        <div style={{display:'flex', alignItems:'center', gap:'8px', marginBottom:'16px', flexWrap:'wrap'}}>
          <span className="badge badge-primary">{booking.category || 'Wedding'}</span>
          <span className={`badge badge-dot ${isPaid ? 'badge-success' : isDP ? 'badge-warning' : 'badge-error'}`}>
            {isPaid ? 'LUNAS' : isDP ? 'DP MASUK' : 'BELUM BAYAR'}
          </span>
          <span className="badge badge-muted">{booking.status}</span>
        </div>

        <h1 style={{fontSize:'clamp(20px, 3vw, 24px)', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.02em', lineHeight:'1.2', marginBottom:'8px'}}>
          {booking.eventTitle || booking.clientName}
        </h1>
        <p style={{fontSize:'14px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'8px'}}>
          <Users size={14} /> Klien: <span style={{fontWeight:'600', color:'var(--text-2)'}}>{booking.clientName}</span>
        </p>
      </div>

      {/* ── ACTION BUTTONS ── */}
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))', gap:'12px', marginBottom:'24px'}}>
        <button onClick={() => onOpenMcDayMode(booking)} className="card card-interactive" style={{padding:'16px', display:'flex', alignItems:'center', gap:'12px', background:'var(--primary-light)', borderColor:'rgba(79,70,229,0.2)'}}>
          <div style={{width:'36px', height:'36px', borderRadius:'10px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center'}}>
            <Mic size={18} color="white" />
          </div>
          <div style={{flex:1, textAlign:'left'}}>
            <span style={{fontSize:'13px', fontWeight:'700', color:'var(--primary)', display:'block'}}>MC Day Mode</span>
            <span style={{fontSize:'11px', color:'var(--primary-text)'}}>Run-down & Cue Card</span>
          </div>
          <ChevronRight size={16} color="var(--primary)" />
        </button>

        <button onClick={() => onOpenInvoice(booking)} className="card card-interactive" style={{padding:'16px', display:'flex', alignItems:'center', gap:'12px'}}>
          <div style={{width:'36px', height:'36px', borderRadius:'10px', background:'var(--bg-surface-2)', display:'flex', alignItems:'center', justifyContent:'center'}}>
            <FileText size={18} color="var(--text-2)" />
          </div>
          <div style={{flex:1, textAlign:'left'}}>
            <span style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', display:'block'}}>Lihat Invoice</span>
            <span style={{fontSize:'11px', color:'var(--text-3)'}}>Cetak PDF Tagihan</span>
          </div>
          <ChevronRight size={16} color="var(--text-4)" />
        </button>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1fr', gap:'20px'}} className="md-two-col">
        {/* ── DETAIL EVENT ── */}
        <div style={{display:'flex', flexDirection:'column', gap:'20px'}}>
          <div className="card" style={{padding:'0', overflow:'hidden'}}>
            <div style={{padding:'16px', borderBottom:'1px solid var(--border)', background:'var(--bg-surface-2)'}}>
              <h3 style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', textTransform:'uppercase', letterSpacing:'0.05em'}}>Detail Acara</h3>
            </div>
            <div style={{padding:'16px', display:'flex', flexDirection:'column', gap:'16px'}}>
              <div style={{display:'flex', gap:'12px'}}>
                <Calendar size={16} color="var(--text-4)" style={{marginTop:'2px', flexShrink:0}} />
                <div>
                  <span style={{fontSize:'11px', color:'var(--text-3)', display:'block', marginBottom:'2px'}}>Tanggal</span>
                  <span style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)'}}>{booking.eventDate}</span>
                </div>
              </div>
              <div style={{display:'flex', gap:'12px'}}>
                <Clock size={16} color="var(--text-4)" style={{marginTop:'2px', flexShrink:0}} />
                <div>
                  <span style={{fontSize:'11px', color:'var(--text-3)', display:'block', marginBottom:'2px'}}>Waktu Standby / Mulai</span>
                  <span style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)'}}>{booking.eventTime || 'TBA'} WIB</span>
                </div>
              </div>
              <div style={{display:'flex', gap:'12px'}}>
                <MapPin size={16} color="var(--text-4)" style={{marginTop:'2px', flexShrink:0}} />
                <div>
                  <span style={{fontSize:'11px', color:'var(--text-3)', display:'block', marginBottom:'2px'}}>Lokasi Venue</span>
                  <span style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)'}}>{booking.venue || '-'}</span>
                </div>
              </div>
              <div style={{display:'flex', gap:'12px'}}>
                <FileText size={16} color="var(--text-4)" style={{marginTop:'2px', flexShrink:0}} />
                <div>
                  <span style={{fontSize:'11px', color:'var(--text-3)', display:'block', marginBottom:'2px'}}>Catatan Acara</span>
                  <span style={{fontSize:'14px', color:'var(--text-2)', lineHeight:'1.5'}}>{booking.notes || 'Tidak ada catatan.'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ── KEUANGAN ── */}
        <div style={{display:'flex', flexDirection:'column', gap:'20px'}}>
          <div className="card" style={{padding:'0', overflow:'hidden'}}>
            <div style={{padding:'16px', borderBottom:'1px solid var(--border)', background:'var(--bg-surface-2)'}}>
              <h3 style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', textTransform:'uppercase', letterSpacing:'0.05em'}}>Rincian Keuangan</h3>
            </div>
            <div style={{padding:'16px', display:'flex', flexDirection:'column', gap:'16px'}}>
              
              <div style={{display:'flex', alignItems:'center', justifyContent:'space-between'}}>
                <span style={{fontSize:'13px', color:'var(--text-3)'}}>Total Honorarium</span>
                <span style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)', fontVariantNumeric:'tabular-nums'}}>
                  {formatRp(booking.totalFee || 0)}
                </span>
              </div>
              
              <div style={{display:'flex', alignItems:'center', justifyContent:'space-between'}}>
                <span style={{fontSize:'13px', color:'var(--text-3)'}}>DP Diterima</span>
                <span style={{fontSize:'15px', fontWeight:'600', color:'var(--success)', fontVariantNumeric:'tabular-nums'}}>
                  {formatRp(booking.dpAmount || 0)}
                </span>
              </div>
              
              <div style={{height:'1px', background:'var(--border)', margin:'4px 0'}} />
              
              <div style={{display:'flex', alignItems:'center', justifyContent:'space-between'}}>
                <span style={{fontSize:'13px', fontWeight:'600', color:'var(--text-1)'}}>Sisa Tagihan</span>
                <span style={{fontSize:'18px', fontWeight:'700', color: isPaid ? 'var(--success)' : 'var(--error)', fontVariantNumeric:'tabular-nums'}}>
                  {isPaid ? 'LUNAS' : formatRp(sisaBayar)}
                </span>
              </div>

            </div>
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal-overlay" onClick={e => { if(e.target === e.currentTarget) setShowDeleteModal(false); }}>
          <div className="modal-panel" style={{textAlign:'center'}}>
            <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 16px'}}>
              <Trash2 size={24} color="var(--error)" />
            </div>
            <h3 style={{fontSize:'18px', fontWeight:'700', color:'var(--text-1)', marginBottom:'8px'}}>Hapus Job Ini?</h3>
            <p style={{fontSize:'13px', color:'var(--text-3)', lineHeight:'1.5', marginBottom:'24px', maxWidth:'280px', margin:'0 auto 24px'}}>
              Tindakan ini tidak dapat dibatalkan. Semua data terkait acara ini akan dihapus permanen.
            </p>
            <div style={{display:'flex', gap:'12px'}}>
              <button onClick={() => setShowDeleteModal(false)} className="btn btn-secondary btn-full">Batal</button>
              <button 
                onClick={() => {
                  onDelete(booking.id);
                  setShowDeleteModal(false);
                  onBack();
                }} 
                className="btn btn-destructive btn-full"
              >
                Ya, Hapus
              </button>
            </div>
          </div>
        </div>
      )}

      <style>{`
        @media (min-width: 768px) {
          .md-two-col { grid-template-columns: 3fr 2fr !important; }
        }
      `}</style>
    </div>
  );
};
