import React, { useRef } from 'react';
import { ArrowLeft, Printer, FileDown, CheckCircle2, DollarSign } from 'lucide-react';
import type { Booking } from '../types';
import { useAuth } from '../context/AuthContext';
import html2pdf from 'html2pdf.js';

interface InvoicePageProps {
  booking: Booking | null;
  onBack: () => void;
}

export const InvoicePage: React.FC<InvoicePageProps> = ({ booking, onBack }) => {
  const { userProfile } = useAuth();
  const invoiceRef = useRef<HTMLDivElement>(null);

  if (!booking) {
    return (
      <div className="animate-fade-in" style={{padding:'24px', textAlign:'center'}}>
        <div className="empty-state">
          <DollarSign size={24} />
          <p>Booking tidak ditemukan atau belum dipilih.</p>
          <button onClick={onBack} className="btn btn-secondary btn-sm" style={{marginTop:'12px'}}>Kembali</button>
        </div>
      </div>
    );
  }

  const invoiceNumber = `INV-${booking.id.substring(booking.id.length - 6).toUpperCase()}`;
  const totalFee = booking.totalFee || 0;
  const dpAmount = booking.dpAmount || 0;
  const isPaid = booking.paymentStatus === 'PAID';
  const remaining = isPaid ? 0 : totalFee - dpAmount;
  
  const formattedDate = new Intl.DateTimeFormat('id-ID', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(booking.eventDate));
  
  // Format to standard Rp currency without decimals
  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  const handleDownloadPDF = () => {
    if (!invoiceRef.current) return;
    const element = invoiceRef.current;
    
    // We clone to hide buttons from PDF
    const opt = {
      margin:       10,
      filename:     `${invoiceNumber}_${booking.clientName.replace(/\s+/g, '_')}.pdf`,
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  { scale: 2, useCORS: true },
      jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(element).save();
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── TOP NAV & ACTIONS ── */}
      <div className="hide-on-print" style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'24px', flexWrap:'wrap', gap:'12px'}}>
        <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px', color:'var(--text-2)'}}>
          <ArrowLeft size={16} /> Kembali
        </button>
        <div style={{display:'flex', gap:'8px'}}>
          <button onClick={handlePrint} className="btn btn-secondary btn-sm"><Printer size={14} /> Cetak (A4)</button>
          <button onClick={handleDownloadPDF} className="btn btn-primary btn-sm"><FileDown size={14} /> Download PDF</button>
        </div>
      </div>

      {/* ── INVOICE DOCUMENT A4 FORMAT ── */}
      <div 
        ref={invoiceRef}
        className="card" 
        style={{
          padding: '40px', 
          background: '#FFFFFF',
          color: '#111827',
          fontFamily: "'Inter', sans-serif",
          boxShadow: '0 20px 40px rgba(0,0,0,0.08)',
          position: 'relative'
        }}
      >
        {/* Paid Watermark */}
        {isPaid && (
          <div style={{position:'absolute', top:'50%', left:'50%', transform:'translate(-50%, -50%) rotate(-30deg)', border:'8px solid rgba(5,150,105,0.1)', color:'rgba(5,150,105,0.1)', fontSize:'80px', fontWeight:'900', letterSpacing:'0.2em', padding:'20px 40px', borderRadius:'24px', pointerEvents:'none', zIndex:0}}>
            LUNAS
          </div>
        )}

        <div style={{position:'relative', zIndex:1}}>
          {/* Header */}
          <div style={{display:'flex', justifyContent:'space-between', borderBottom:'2px solid #E4E7EC', paddingBottom:'24px', marginBottom:'32px'}}>
            <div>
              <div style={{width:'40px', height:'40px', borderRadius:'10px', background:'#4F46E5', display:'flex', alignItems:'center', justifyContent:'center', marginBottom:'12px'}}>
                <span style={{fontSize:'14px', fontWeight:'800', color:'white'}}>MC</span>
              </div>
              <h2 style={{fontSize:'20px', fontWeight:'700', color:'#111827', letterSpacing:'-0.02em'}}>{userProfile?.displayName || 'MC Professional'}</h2>
              <p style={{fontSize:'12px', color:'#6B7280', marginTop:'2px'}}>{userProfile?.city || 'Indonesia'}</p>
            </div>
            <div style={{textAlign:'right'}}>
              <h1 style={{fontSize:'32px', fontWeight:'800', color:'#4F46E5', letterSpacing:'-0.02em', marginBottom:'4px'}}>INVOICE</h1>
              <p style={{fontSize:'13px', fontWeight:'600', color:'#374151'}}>No: {invoiceNumber}</p>
              <p style={{fontSize:'12px', color:'#6B7280'}}>Tanggal: {formattedDate}</p>
            </div>
          </div>

          {/* Info Billing & Event */}
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'32px', marginBottom:'40px'}}>
            <div>
              <p style={{fontSize:'11px', fontWeight:'700', color:'#9CA3AF', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'8px'}}>Ditagihkan Kepada:</p>
              <h3 style={{fontSize:'15px', fontWeight:'700', color:'#111827', marginBottom:'4px'}}>{booking.clientName}</h3>
              <p style={{fontSize:'13px', color:'#4B5563', lineHeight:'1.5'}}>{booking.notes || '-'}</p>
            </div>
            <div>
              <p style={{fontSize:'11px', fontWeight:'700', color:'#9CA3AF', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'8px'}}>Detail Acara:</p>
              <p style={{fontSize:'13px', color:'#374151', marginBottom:'4px'}}><strong>Nama Acara:</strong> {booking.eventTitle || booking.clientName}</p>
              <p style={{fontSize:'13px', color:'#374151', marginBottom:'4px'}}><strong>Kategori:</strong> {booking.category}</p>
              <p style={{fontSize:'13px', color:'#374151'}}><strong>Lokasi:</strong> {booking.venue || '-'}</p>
            </div>
          </div>

          {/* Item Table */}
          <table style={{width:'100%', borderCollapse:'collapse', marginBottom:'32px'}}>
            <thead>
              <tr style={{background:'#F3F4F6'}}>
                <th style={{padding:'12px 16px', textAlign:'left', fontSize:'12px', fontWeight:'600', color:'#374151', borderTopLeftRadius:'8px', borderBottomLeftRadius:'8px'}}>Keterangan Layanan</th>
                <th style={{padding:'12px 16px', textAlign:'right', fontSize:'12px', fontWeight:'600', color:'#374151', borderTopRightRadius:'8px', borderBottomRightRadius:'8px'}}>Jumlah</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={{padding:'16px', borderBottom:'1px solid #E4E7EC'}}>
                  <p style={{fontSize:'14px', fontWeight:'600', color:'#111827'}}>Jasa Master of Ceremonies (MC)</p>
                  <p style={{fontSize:'12px', color:'#6B7280', marginTop:'2px'}}>Sesi {booking.eventTime || 'TBA'}</p>
                </td>
                <td style={{padding:'16px', borderBottom:'1px solid #E4E7EC', textAlign:'right', fontSize:'14px', fontWeight:'600', color:'#111827', fontVariantNumeric:'tabular-nums'}}>
                  {formatRp(totalFee)}
                </td>
              </tr>
            </tbody>
          </table>

          {/* Totals */}
          <div style={{display:'flex', justifyContent:'flex-end', marginBottom:'48px'}}>
            <div style={{width:'300px'}}>
              <div style={{display:'flex', justifyContent:'space-between', padding:'8px 0', borderBottom:'1px solid #E4E7EC'}}>
                <span style={{fontSize:'13px', color:'#4B5563'}}>Subtotal</span>
                <span style={{fontSize:'13px', fontWeight:'600', color:'#111827', fontVariantNumeric:'tabular-nums'}}>{formatRp(totalFee)}</span>
              </div>
              <div style={{display:'flex', justifyContent:'space-between', padding:'8px 0', borderBottom:'1px solid #E4E7EC'}}>
                <span style={{fontSize:'13px', color:'#4B5563'}}>DP (Down Payment)</span>
                <span style={{fontSize:'13px', fontWeight:'600', color:'#059669', fontVariantNumeric:'tabular-nums'}}>- {formatRp(dpAmount)}</span>
              </div>
              <div style={{display:'flex', justifyContent:'space-between', padding:'12px 0', marginTop:'4px'}}>
                <span style={{fontSize:'15px', fontWeight:'800', color:'#111827'}}>Total Tagihan</span>
                <span style={{fontSize:'18px', fontWeight:'800', color: isPaid ? '#059669' : '#DC2626', fontVariantNumeric:'tabular-nums'}}>
                  {isPaid ? 'LUNAS' : formatRp(remaining)}
                </span>
              </div>
            </div>
          </div>

          {/* Footer Notes */}
          <div style={{borderTop:'2px solid #E4E7EC', paddingTop:'24px', display:'flex', justifyContent:'space-between', alignItems:'flex-end'}}>
            <div>
              <p style={{fontSize:'11px', fontWeight:'700', color:'#9CA3AF', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'8px'}}>Informasi Pembayaran:</p>
              <p style={{fontSize:'13px', color:'#374151', fontWeight:'600'}}>BCA - 1234567890</p>
              <p style={{fontSize:'13px', color:'#6B7280'}}>A.n. {userProfile?.displayName || 'Nama Rekening'}</p>
            </div>
            
            <div style={{textAlign:'center'}}>
              <p style={{fontSize:'12px', color:'#6B7280', marginBottom:'40px'}}>Hormat Kami,</p>
              <p style={{fontSize:'14px', fontWeight:'700', color:'#111827'}}>{userProfile?.displayName || 'MC Professional'}</p>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        @media print {
          body * { visibility: hidden; }
          .hide-on-print { display: none !important; }
          .app-layout { background: white !important; }
          .main-area, .content-area { overflow: visible !important; height: auto !important; padding: 0 !important; }
          #root { background: white !important; }
          
          /* The specific invoice card to print */
          .card {
            visibility: visible;
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            margin: 0;
            padding: 20px !important;
            box-shadow: none !important;
            border: none !important;
          }
          .card * { visibility: visible; }
        }
      `}</style>
    </div>
  );
};
