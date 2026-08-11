import React, { useState, useRef } from 'react';
import { ArrowLeft, Printer, FileDown, CheckCircle2, DollarSign, Copy, FileText, Layout, Sparkles, Building2, Crown, Palette } from 'lucide-react';
import type { Booking, Payment } from '../types';
import { getBookingName, getBookingClient, getBookingDate, getBookingFee, getBookingDp } from '../types';
import { useAuth } from '../context/AuthContext';

interface InvoicePageProps {
  booking: Booking | null;
  allBookings?: Booking[];
  payments?: Payment[];
  onSaveInvoice?: (inv: any) => Promise<void>;
  onBack: () => void;
}

type TemplateType = 'CORPORATE' | 'LUXURY' | 'MINIMALIST';

export const InvoicePage: React.FC<InvoicePageProps> = ({
  booking: initialBooking,
  allBookings = [],
  payments = [],
  onSaveInvoice,
  onBack
}) => {
  const { userProfile } = useAuth();
  const invoiceRef = useRef<HTMLDivElement>(null);

  // Selected booking state
  const [selectedBookingId, setSelectedBookingId] = useState<string>(
    initialBooking ? initialBooking.id : (allBookings[0]?.id || '')
  );
  
  // Selected template state
  const [template, setTemplate] = useState<TemplateType>('CORPORATE');
  const [copied, setCopied] = useState(false);

  const booking = allBookings.find(b => b.id === selectedBookingId) || initialBooking || allBookings[0] || null;

  // Mock data for preview if no booking exists at all
  const mockBooking: Booking = {
    id: 'MOCK-123',
    ownerId: 'demo',
    name: 'Wedding Resepsi Kevin & Vania',
    clientName: 'Bpk. Kevin & Vania',
    category: 'Wedding',
    status: 'confirmed',
    eventDate: '2026-09-12',
    venue: 'Grand Ballroom Hotel Mulia, Jakarta',
    fee: 5000000,
    dp: 2000000,
    notes: 'Pelunasan H-3 sebelum acara.'
  };

  const activeBooking = booking || mockBooking;

  const invoiceNumber = `INV-${new Date().getFullYear()}-${(activeBooking.id || '0000').slice(-4).toUpperCase()}`;
  const totalFee = getBookingFee(activeBooking) || 5000000;
  
  const bookingPayments = payments.filter(p => p.bookingId === activeBooking.id);
  const totalPaid = bookingPayments.length > 0 
    ? bookingPayments.reduce((s, p) => s + p.amount, 0)
    : getBookingDp(activeBooking);

  const remaining = Math.max(0, totalFee - totalPaid);
  const isPaid = totalFee > 0 && totalPaid >= totalFee;
  
  const dateStr = getBookingDate(activeBooking) || new Date().toISOString().split('T')[0];
  const formattedDate = new Intl.DateTimeFormat('id-ID', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(dateStr));
  
  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  const handlePrint = async () => {
    if (onSaveInvoice && activeBooking && activeBooking.id !== 'MOCK-123') {
      try {
        await onSaveInvoice({
          id: `INV-${Date.now()}`,
          ownerId: activeBooking.ownerId,
          bookingId: activeBooking.id,
          invoiceNumber,
          clientName: getBookingClient(activeBooking) || 'Klien',
          eventName: getBookingName(activeBooking) || 'Event',
          eventDate: getBookingDate(activeBooking) || '',
          totalFee,
          dpAmount: totalPaid,
          remainingAmount: remaining,
          dueDate: getBookingDate(activeBooking) || '',
          createdAt: new Date().toISOString(),
          template
        });
      } catch (err) {
        console.error('Failed to log invoice history:', err);
      }
    }
    window.print();
  };

  const handleCopyText = () => {
    const txt = `*INVOICE TAGIHAN MC - ${invoiceNumber}*\n` +
      `Klien: ${getBookingClient(activeBooking) || 'Klien'}\n` +
      `Acara: ${getBookingName(activeBooking)}\n` +
      `Tanggal: ${formattedDate}\n` +
      `-----------------------------\n` +
      `Total Honor: ${formatRp(totalFee)}\n` +
      `Sudah Dibayar (DP): ${formatRp(totalPaid)}\n` +
      `*SISA TAGIHAN: ${isPaid ? 'LUNAS' : formatRp(remaining)}*\n` +
      `-----------------------------\n` +
      `Rekening Pembayaran:\n` +
      `${userProfile?.bankName || 'BCA'} - ${userProfile?.bankAccountNumber || '1234567890'}\n` +
      `A.n. ${userProfile?.bankAccountHolder || userProfile?.stageName || userProfile?.displayName || 'Nama Rekening'}\n\n` +
      `Terima kasih! 🙏`;

    navigator.clipboard.writeText(txt);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Color styles per template
  const getTemplateStyles = () => {
    switch (template) {
      case 'LUXURY':
        return {
          primaryColor: '#B45309',
          bgColor: '#FFFBEB',
          borderColor: '#FCD34D',
          badgeBg: '#FEF3C7',
          fontFamily: "'Playfair Display', serif, Georgia"
        };
      case 'MINIMALIST':
        return {
          primaryColor: '#059669',
          bgColor: '#ECFDF5',
          borderColor: '#6EE7B7',
          badgeBg: '#D1FAE5',
          fontFamily: "'Inter', sans-serif"
        };
      case 'CORPORATE':
      default:
        return {
          primaryColor: '#4F46E5',
          bgColor: '#EEF2FF',
          borderColor: '#C7D2FE',
          badgeBg: '#E0E7FF',
          fontFamily: "'Inter', sans-serif"
        };
    }
  };

  const styles = getTemplateStyles();

  return (
    <div className="animate-fade-in" style={{width:'100%', paddingBottom:'32px'}}>
      
      {/* ── CONTROL BAR ── */}
      <div className="hide-on-print" style={{marginBottom:'16px', display:'flex', flexDirection:'column', gap:'12px'}}>

        {/* Row 1: Title + Action Buttons */}
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', gap:'12px', flexWrap:'wrap'}}>
          <span style={{fontSize:'13px', fontWeight:'800', color:'var(--text-1)', display:'flex', alignItems:'center', gap:'7px'}}>
            <FileText size={14} color="var(--primary)" /> Generator Invoice
          </span>
          <div style={{display:'flex', gap:'8px', flexShrink:0}}>
            <button onClick={handleCopyText} className="btn btn-secondary btn-sm" style={{color: copied ? 'var(--success)' : undefined}}>
              {copied ? <><CheckCircle2 size={13} /> Tersalin!</> : <><Copy size={13} /> Salin WA</>}
            </button>
            <button onClick={handlePrint} className="btn btn-primary btn-sm">
              <Printer size={13} /> Cetak A4
            </button>
          </div>
        </div>

        {/* Row 2: Job selector */}
        <div>
          <label className="input-label" style={{marginBottom:'5px'}}>Pilih Acara / Job:</label>
          {allBookings.length > 0 ? (
            <select
              value={selectedBookingId}
              onChange={e => setSelectedBookingId(e.target.value)}
              className="input-field"
              style={{fontWeight:'600'}}
            >
              {allBookings.map(b => (
                <option key={b.id} value={b.id}>
                  {getBookingName(b)} — {getBookingClient(b)} ({formatRp(getBookingFee(b))})
                </option>
              ))}
            </select>
          ) : (
            <div style={{fontSize:'12px', color:'var(--text-3)', fontStyle:'italic', padding:'10px', background:'var(--bg-surface-2)', borderRadius:'10px'}}>
              Belum ada job tersimpan. Menampilkan data demo.
            </div>
          )}
        </div>

        {/* Row 3: Template chips — wrap, no overflow */}
        <div>
          <label className="input-label" style={{marginBottom:'6px'}}>Desain Template:</label>
          <div style={{display:'flex', gap:'8px', flexWrap:'wrap'}}>
            <button
              onClick={() => setTemplate('CORPORATE')}
              className={`chip${template === 'CORPORATE' ? ' active' : ''}`}
            >
              <Building2 size={12} style={{marginRight:'4px'}} /> Modern Corporate
            </button>
            <button
              onClick={() => setTemplate('LUXURY')}
              className={`chip${template === 'LUXURY' ? ' active' : ''}`}
              style={template === 'LUXURY' ? {background:'#B45309', borderColor:'#B45309', color:'#fff'} : undefined}
            >
              <Crown size={12} style={{marginRight:'4px'}} /> Luxury Elegant
            </button>
            <button
              onClick={() => setTemplate('MINIMALIST')}
              className={`chip${template === 'MINIMALIST' ? ' active' : ''}`}
              style={template === 'MINIMALIST' ? {background:'#059669', borderColor:'#059669', color:'#fff'} : undefined}
            >
              <Palette size={12} style={{marginRight:'4px'}} /> Minimalist
            </button>
          </div>
        </div>

      </div>

      {/* ── INVOICE DOCUMENT A4 FORMAT ── */}
      <div 
        ref={invoiceRef}
        className="card invoice-document" 
        style={{
          padding: 'clamp(20px, 5vw, 44px)', 
          background: '#FFFFFF',
          color: '#111827',
          fontFamily: styles.fontFamily,
          boxShadow: '0 20px 40px rgba(0,0,0,0.08)',
          position: 'relative',
          borderRadius: '16px',
          borderTop: `6px solid ${styles.primaryColor}`
        }}
      >
        {/* Paid Watermark */}
        {isPaid && (
          <div style={{position:'absolute', top:'50%', left:'50%', transform:'translate(-50%, -50%) rotate(-30deg)', border:`8px solid ${styles.primaryColor}`, color:styles.primaryColor, opacity:0.12, fontSize:'80px', fontWeight:'900', letterSpacing:'0.2em', padding:'20px 40px', borderRadius:'24px', pointerEvents:'none', zIndex:0}}>
            LUNAS
          </div>
        )}

        <div style={{position:'relative', zIndex:1}}>
          {/* Header */}
          <div className="invoice-header" style={{display:'flex', justifyContent:'space-between', flexWrap:'wrap', gap:'16px', borderBottom:`2px solid ${styles.borderColor}`, paddingBottom:'24px', marginBottom:'28px'}}>
            <div style={{flex:'1', minWidth:'160px'}}>
              <div style={{width:'44px', height:'44px', borderRadius:'12px', background:styles.primaryColor, display:'flex', alignItems:'center', justifyContent:'center', marginBottom:'10px'}}>
                <span style={{fontSize:'16px', fontWeight:'800', color:'white'}}>MC</span>
              </div>
              <h2 style={{fontSize:'clamp(16px,4vw,22px)', fontWeight:'800', color:'#111827', letterSpacing:'-0.02em'}}>{userProfile?.stageName || userProfile?.displayName || 'MC Professional'}</h2>
              <p style={{fontSize:'12px', color:'#6B7280', marginTop:'2px'}}>{userProfile?.city || 'Indonesia'} • Professional Master of Ceremonies</p>
              {userProfile?.phoneNumber && <p style={{fontSize:'11px', color:'#6B7280'}}>Telp/WA: {userProfile.phoneNumber}</p>}
            </div>
            
            <div style={{textAlign:'right', flexShrink:0}}>
              <h1 style={{fontSize:'clamp(24px,6vw,32px)', fontWeight:'900', color:styles.primaryColor, letterSpacing:'-0.02em', marginBottom:'4px'}}>INVOICE</h1>
              <p style={{fontSize:'12px', fontWeight:'700', color:'#374151'}}>No: {invoiceNumber}</p>
              <p style={{fontSize:'11px', color:'#6B7280'}}>Tanggal Penerbitan: {formattedDate}</p>
              <div style={{marginTop:'8px'}}>
                <span style={{fontSize:'10px', fontWeight:'700', background: isPaid ? '#D1FAE5' : styles.badgeBg, color: isPaid ? '#065F46' : styles.primaryColor, padding:'4px 10px', borderRadius:'9999px', textTransform:'uppercase', whiteSpace:'nowrap'}}>
                  {isPaid ? 'STATUS: LUNAS' : (totalPaid > 0 ? 'STATUS: DP (SEBAGIAN)' : 'STATUS: BELUM DIBAYAR')}
                </span>
              </div>
            </div>
          </div>

          {/* Info Billing & Event */}
          <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(140px, 1fr))', gap:'16px', marginBottom:'32px', background:styles.bgColor, padding:'16px', borderRadius:'12px', border:`1px solid ${styles.borderColor}`}}>
            <div>
              <p style={{fontSize:'11px', fontWeight:'800', color:styles.primaryColor, textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'6px'}}>DITAGIHKAN KEPADA:</p>
              <h3 style={{fontSize:'16px', fontWeight:'800', color:'#111827', marginBottom:'4px'}}>{getBookingClient(activeBooking) || 'Klien MC'}</h3>
              <p style={{fontSize:'13px', color:'#4B5563', lineHeight:'1.5'}}>{activeBooking.notes || '-'}</p>
            </div>
            <div>
              <p style={{fontSize:'11px', fontWeight:'800', color:styles.primaryColor, textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'6px'}}>DETAIL ACARA / PERFORM:</p>
              <p style={{fontSize:'13px', color:'#374151', marginBottom:'3px'}}><strong>Acara:</strong> {getBookingName(activeBooking)}</p>
              <p style={{fontSize:'13px', color:'#374151', marginBottom:'3px'}}><strong>Kategori:</strong> {activeBooking.category || 'Wedding'}</p>
              <p style={{fontSize:'13px', color:'#374151'}}><strong>Lokasi:</strong> {activeBooking.venue || activeBooking.location || 'Venue Event'}</p>
            </div>
          </div>

          {/* Item Table */}
          <table style={{width:'100%', borderCollapse:'collapse', marginBottom:'32px'}}>
            <thead>
              <tr style={{background:styles.bgColor}}>
                <th style={{padding:'12px 16px', textAlign:'left', fontSize:'12px', fontWeight:'700', color:styles.primaryColor, borderTopLeftRadius:'8px', borderBottomLeftRadius:'8px'}}>Keterangan Layanan / Perform MC</th>
                <th style={{padding:'12px 16px', textAlign:'right', fontSize:'12px', fontWeight:'700', color:styles.primaryColor, borderTopRightRadius:'8px', borderBottomRightRadius:'8px'}}>Total (Rp)</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={{padding:'18px 16px', borderBottom:`1px solid ${styles.borderColor}`}}>
                  <p style={{fontSize:'14px', fontWeight:'700', color:'#111827'}}>Jasa Master of Ceremonies (MC) Professional</p>
                  <p style={{fontSize:'12px', color:'#6B7280', marginTop:'4px'}}>
                    Acara: {getBookingName(activeBooking)} • Sesi Jam: {activeBooking.eventTime || activeBooking.start || 'Sesuai Rundown'}
                  </p>
                </td>
                <td style={{padding:'18px 16px', borderBottom:`1px solid ${styles.borderColor}`, textAlign:'right', fontSize:'15px', fontWeight:'700', color:'#111827', fontVariantNumeric:'tabular-nums'}}>
                  {formatRp(totalFee)}
                </td>
              </tr>
            </tbody>
          </table>

          {/* Totals */}
          <div style={{display:'flex', justifyContent:'flex-end', marginBottom:'40px'}}>
            <div style={{width:'min(320px, 100%)'}}>
              <div style={{display:'flex', justifyContent:'space-between', padding:'8px 0', borderBottom:'1px solid #E4E7EC'}}>
                <span style={{fontSize:'13px', color:'#4B5563'}}>Subtotal Total Honor</span>
                <span style={{fontSize:'13px', fontWeight:'600', color:'#111827', fontVariantNumeric:'tabular-nums'}}>{formatRp(totalFee)}</span>
              </div>
              <div style={{display:'flex', justifyContent:'space-between', padding:'8px 0', borderBottom:'1px solid #E4E7EC'}}>
                <span style={{fontSize:'13px', color:'#4B5563'}}>Sudah Dibayar (DP)</span>
                <span style={{fontSize:'13px', fontWeight:'600', color:'#059669', fontVariantNumeric:'tabular-nums'}}>- {formatRp(totalPaid)}</span>
              </div>
              <div style={{display:'flex', justifyContent:'space-between', padding:'14px 0', marginTop:'4px'}}>
                <span style={{fontSize:'15px', fontWeight:'800', color:'#111827'}}>SISA PELUNASAN</span>
                <span style={{fontSize:'20px', fontWeight:'900', color: isPaid ? '#059669' : styles.primaryColor, fontVariantNumeric:'tabular-nums'}}>
                  {isPaid ? 'LUNAS' : formatRp(remaining)}
                </span>
              </div>
            </div>
          </div>

          {/* Footer Notes */}
          <div style={{borderTop:`2px solid ${styles.borderColor}`, paddingTop:'20px', display:'flex', justifyContent:'space-between', alignItems:'flex-end', flexWrap:'wrap', gap:'16px'}}>
            <div>
              <p style={{fontSize:'11px', fontWeight:'800', color:styles.primaryColor, textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'6px'}}>REKENING PEMBAYARAN:</p>
              <p style={{fontSize:'14px', color:'#111827', fontWeight:'800'}}>{userProfile?.bankName || 'BCA'} - {userProfile?.bankAccountNumber || '1234567890'}</p>
              <p style={{fontSize:'13px', color:'#6B7280'}}>Atas Nama: <strong>{userProfile?.bankAccountHolder || userProfile?.displayName || 'Nama Rekening MC'}</strong></p>
            </div>
            
            <div style={{textAlign:'center'}}>
              <p style={{fontSize:'12px', color:'#6B7280', marginBottom:'40px'}}>Hormat Kami,</p>
              <p style={{fontSize:'14px', fontWeight:'800', color:'#111827'}}>{userProfile?.displayName || userProfile?.stageName || 'MC Professional'}</p>
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
          
          .invoice-document {
            visibility: visible !important;
            position: absolute !important;
            left: 0 !important;
            top: 0 !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 30px !important;
            box-shadow: none !important;
            border: none !important;
          }
          .invoice-document * { visibility: visible !important; }
        }
      `}</style>
    </div>
  );
};
