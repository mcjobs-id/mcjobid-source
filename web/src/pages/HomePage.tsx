import React, { useState, useEffect } from 'react';
import { Calendar, Plus, ArrowRight, Mic, MessageSquare, HelpCircle, TrendingUp, TrendingDown, MapPin, ChevronRight, DollarSign, Zap, UserPlus, FileText, Tag, BarChart2, CheckSquare, Bell, User, X } from 'lucide-react';
import type { Booking } from '../types';
import { useAuth } from '../context/AuthContext';
import { getStoredQaConfig, type QuickActionConfig } from './QuickActionSettingsPage';

interface HomePageProps {
  bookings: Booking[];
  onNavigateTab: (tab: any) => void;
  onOpenCreateJob: () => void;
  onOpenBookingDetail: (booking: Booking) => void;
  onOpenDayMode: (booking: Booking) => void;
}

type TimeFilter = 'TODAY' | 'THIS_MONTH' | 'THIS_YEAR' | 'CUSTOM';

const timeFilters: { id: TimeFilter; label: string }[] = [
  { id: 'TODAY', label: 'Hari Ini' },
  { id: 'THIS_MONTH', label: 'Bulan Ini' },
  { id: 'THIS_YEAR', label: 'Tahun Ini' },
  { id: 'CUSTOM', label: 'Semua' },
];

function formatRp(val: number) {
  if (val >= 1_000_000) return `Rp ${(val / 1_000_000).toFixed(1)}jt`;
  if (val >= 1_000) return `Rp ${(val / 1_000).toFixed(0)}rb`;
  return `Rp ${val.toLocaleString('id-ID')}`;
}

function formatRpFull(val: number) {
  return `Rp ${val.toLocaleString('id-ID')}`;
}

export const HomePage: React.FC<HomePageProps> = ({
  bookings, onNavigateTab, onOpenCreateJob, onOpenBookingDetail, onOpenDayMode
}) => {
  const { userProfile } = useAuth();
  const [selectedFilter, setSelectedFilter] = useState<TimeFilter>('THIS_MONTH');
  
  // Floating Speed Dial state
  const [qaOpen, setQaOpen] = useState(false);
  const [qaConfig, setQaConfig] = useState<QuickActionConfig>(getStoredQaConfig);

  useEffect(() => {
    setQaConfig(getStoredQaConfig());
  }, []);

  const todayStr = new Date().toISOString().split('T')[0];
  const todayBooking = bookings.find(b => b.eventDate === todayStr && b.status !== 'CANCELLED');

  const upcomingBookings = bookings
    .filter(b => b.status !== 'CANCELLED' && b.eventDate >= todayStr)
    .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());
  const nextBooking = upcomingBookings[0];

  const filteredBookings = bookings.filter(b => {
    if (b.status === 'CANCELLED') return false;
    const bDate = new Date(b.eventDate);
    const now = new Date();
    if (selectedFilter === 'TODAY') return b.eventDate === todayStr;
    if (selectedFilter === 'THIS_MONTH') return bDate.getMonth() === now.getMonth() && bDate.getFullYear() === now.getFullYear();
    if (selectedFilter === 'THIS_YEAR') return bDate.getFullYear() === now.getFullYear();
    return true;
  });

  const totalHonor  = filteredBookings.reduce((s, b) => s + (b.fee || 0), 0);
  const totalPaid   = filteredBookings.reduce((s, b) => s + (b.dp || 0), 0);
  const totalUnpaid = filteredBookings.reduce((s, b) => b.paymentStatus === 'PAID' ? s : s + ((b.fee || 0) - (b.dp || 0)), 0);
  const totalJobs   = filteredBookings.length;

  const now = new Date();
  const bulanMap = ['Januari','Februari','Maret','April','Mei','Juni','Juli','Agustus','September','Oktober','November','Desember'];
  const greeting = now.getHours() < 12 ? 'Selamat pagi' : now.getHours() < 17 ? 'Selamat siang' : 'Selamat malam';

  // Speed Dial items based on configuration
  const fabItems = [
    { key: 'addJob', label: 'Catat Job Baru', icon: Plus, color: 'var(--primary)', onClick: onOpenCreateJob },
    { key: 'addClient', label: 'Tambah Klien Baru', icon: UserPlus, color: '#2563EB', onClick: () => onNavigateTab('clients') },
    { key: 'addPayment', label: 'Catat Pelunasan/DP', icon: DollarSign, color: 'var(--success)', onClick: () => onNavigateTab('finance') },
    { key: 'invoice', label: 'Generator Invoice', icon: FileText, color: '#0369A1', onClick: () => onNavigateTab('invoice') },
    { key: 'rateCard', label: 'Rate Card', icon: Tag, color: 'var(--primary)', onClick: () => onNavigateTab('price-list') },
    { key: 'analytics', label: 'Analisis Bisnis', icon: BarChart2, color: 'var(--success)', onClick: () => onNavigateTab('analytics') },
    { key: 'followUp', label: 'Follow Up Klien', icon: MessageSquare, color: '#2563EB', onClick: () => onNavigateTab('followup') },
    { key: 'todo', label: 'Daftar Tugas', icon: CheckSquare, color: '#7C3AED', onClick: () => onNavigateTab('todo') },
  ].filter(item => (qaConfig as any)[item.key]);

  return (
    <div className="animate-fade-in" style={{maxWidth:'1280px', margin:'0 auto', display:'flex', flexDirection:'column', gap:'24px', paddingBottom:'16px'}}>

      {/* ── GREETING ROW ── */}
      <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:'16px', flexWrap:'wrap'}}>
        <div>
          <p style={{fontSize:'12px', fontWeight:'600', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.07em', marginBottom:'4px'}}>
            {greeting}
          </p>
          <h1 style={{fontSize:'clamp(20px,3vw,26px)', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.02em', lineHeight:'1.2'}}>
            {userProfile?.displayName || 'MC Professional'} 👋
          </h1>
          <p style={{fontSize:'13px', color:'var(--text-3)', marginTop:'4px'}}>
            {bulanMap[now.getMonth()]} {now.getFullYear()} — {upcomingBookings.length} acara mendatang
          </p>
        </div>
        <button onClick={onOpenCreateJob} className="btn btn-primary" style={{flexShrink:0, gap:'6px'}}>
          <Plus size={15} />
          Tambah Job
        </button>
      </div>

      {/* ── TODAY'S EVENT BANNER (conditional) ── */}
      {todayBooking && (
        <div style={{
          padding:'20px 24px',
          borderRadius:'16px',
          background:'linear-gradient(135deg, #4F46E5 0%, #6D28D9 100%)',
          boxShadow:'0 8px 24px rgba(79,70,229,0.25)',
          display:'flex', alignItems:'center', justifyContent:'space-between', gap:'16px', flexWrap:'wrap'
        }}>
          <div>
            <span style={{fontSize:'10px', fontWeight:'700', color:'rgba(255,255,255,0.6)', textTransform:'uppercase', letterSpacing:'0.1em', display:'block', marginBottom:'4px'}}>
              🎤 Acara Hari Ini
            </span>
            <h3 style={{fontSize:'17px', fontWeight:'700', color:'white', marginBottom:'4px', letterSpacing:'-0.01em'}}>
              {todayBooking.eventTitle || todayBooking.clientName}
            </h3>
            <p style={{fontSize:'12px', color:'rgba(255,255,255,0.65)', display:'flex', alignItems:'center', gap:'8px'}}>
              <span>{todayBooking.eventTime || '19:00'} WIB</span>
              {todayBooking.venue && <><span>•</span><MapPin size={11} /><span>{todayBooking.venue}</span></>}
            </p>
          </div>
          <button
            onClick={() => onOpenDayMode(todayBooking)}
            style={{padding:'10px 18px', borderRadius:'10px', background:'rgba(255,255,255,0.18)', backdropFilter:'blur(8px)', border:'1px solid rgba(255,255,255,0.25)', color:'white', fontWeight:'700', fontSize:'13px', cursor:'pointer', display:'flex', alignItems:'center', gap:'8px', flexShrink:0, transition:'all 0.15s'}}
          >
            <Mic size={15} />
            MC Day Mode
          </button>
        </div>
      )}

      {/* ── STATS GRID ── */}
      <div>
        {/* Filter chips */}
        <div style={{display:'flex', alignItems:'center', gap:'8px', marginBottom:'16px', overflowX:'auto', paddingBottom:'4px'}} className="scrollbar-none">
          {timeFilters.map(f => (
            <button
              key={f.id}
              onClick={() => setSelectedFilter(f.id)}
              className={`chip${selectedFilter === f.id ? ' active' : ''}`}
            >
              {f.label}
            </button>
          ))}
        </div>

        {/* 4 KPI Stat Cards */}
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(180px, 1fr))', gap:'12px'}}>
          {/* Total Omset */}
          <div className="stat-card">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
              <span className="stat-card-label">Total Omset</span>
              <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <DollarSign size={15} color="var(--primary)" />
              </div>
            </div>
            <span className="stat-card-value">{formatRp(totalHonor)}</span>
            <p className="stat-card-sub">{totalJobs} acara tercatat</p>
          </div>

          {/* Terbayar/DP */}
          <div className="stat-card">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
              <span className="stat-card-label">Terbayar / DP</span>
              <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--success-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <TrendingUp size={15} color="var(--success)" />
              </div>
            </div>
            <span className="stat-card-value" style={{color:'var(--success)'}}>{formatRp(totalPaid)}</span>
            <p className="stat-card-sub">Sudah diterima</p>
          </div>

          {/* Piutang */}
          <div className="stat-card">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
              <span className="stat-card-label">Sisa Piutang</span>
              <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--warning-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <TrendingDown size={15} color="var(--warning)" />
              </div>
            </div>
            <span className="stat-card-value" style={{color:'var(--warning)'}}>{formatRp(totalUnpaid)}</span>
            <p className="stat-card-sub">Belum terlunasi</p>
          </div>

          {/* Total Job */}
          <div className="stat-card">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
              <span className="stat-card-label">Total Job</span>
              <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--bg-surface-2)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <Calendar size={15} color="var(--text-3)" />
              </div>
            </div>
            <span className="stat-card-value">{totalJobs}</span>
            <p className="stat-card-sub">Acara tersimpan</p>
          </div>
        </div>
      </div>

      {/* ── MAIN CONTENT GRID ── */}
      <div style={{display:'grid', gridTemplateColumns:'1fr', gap:'20px', alignItems:'start'}} className="lg-two-col">

        {/* Left: Agenda Berikutnya */}
        <div>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'14px'}}>
            <h2 style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', display:'flex', alignItems:'center', gap:'7px'}}>
              <Calendar size={15} color="var(--primary)" />
              Agenda Berikutnya
            </h2>
            <button
              onClick={() => onNavigateTab('agenda')}
              style={{fontSize:'12px', fontWeight:'600', color:'var(--primary)', background:'none', border:'none', cursor:'pointer', display:'flex', alignItems:'center', gap:'4px'}}
            >
              Lihat semua <ArrowRight size={13} />
            </button>
          </div>

          {nextBooking ? (
            <div
              className="card card-interactive"
              onClick={() => onOpenBookingDetail(nextBooking)}
              style={{padding:'20px'}}
            >
              <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:'12px', marginBottom:'14px'}}>
                <div style={{flex:1, overflow:'hidden'}}>
                  <div style={{display:'flex', alignItems:'center', gap:'8px', marginBottom:'6px', flexWrap:'wrap'}}>
                    <span className="badge badge-primary">{nextBooking.category || 'Wedding'}</span>
                    <span className={`badge badge-dot ${nextBooking.paymentStatus === 'PAID' ? 'badge-success' : nextBooking.dp > 0 ? 'badge-warning' : 'badge-error'}`}>
                      {nextBooking.paymentStatus === 'PAID' ? 'LUNAS' : nextBooking.dp > 0 ? 'DP MASUK' : 'BELUM BAYAR'}
                    </span>
                  </div>
                  <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
                    {nextBooking.eventTitle || nextBooking.clientName}
                  </h3>
                  <p style={{fontSize:'12px', color:'var(--text-3)', marginTop:'2px'}}>Klien: {nextBooking.clientName}</p>
                </div>
                <ChevronRight size={18} color="var(--text-4)" style={{flexShrink:0, marginTop:'2px'}} />
              </div>

              <div style={{display:'flex', gap:'16px', flexWrap:'wrap', marginBottom:'16px'}}>
                <span style={{fontSize:'12px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'5px'}}>
                  <Calendar size={12} color="var(--primary)" />
                  {nextBooking.eventDate}
                </span>
                {nextBooking.venue && (
                  <span style={{fontSize:'12px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'5px', overflow:'hidden'}}>
                    <MapPin size={12} color="var(--error)" />
                    <span style={{overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{nextBooking.venue}</span>
                  </span>
                )}
              </div>

              <div style={{borderTop:'1px solid var(--border)', paddingTop:'14px', display:'flex', alignItems:'center', justifyContent:'space-between'}}>
                <span style={{fontSize:'11px', color:'var(--text-4)'}}>Honorarium MC</span>
                <span style={{fontSize:'18px', fontWeight:'700', color:'var(--primary)', letterSpacing:'-0.01em', fontVariantNumeric:'tabular-nums'}}>
                  {formatRpFull(nextBooking.fee || 0)}
                </span>
              </div>
            </div>
          ) : (
            <div className="card" style={{padding:'0'}}>
              <div className="empty-state">
                <div className="empty-state-icon">
                  <Calendar size={22} />
                </div>
                <div>
                  <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Belum ada agenda mendatang</p>
                  <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto 16px'}}>Catat job pertama Anda dan mulai kelola jadwal profesional.</p>
                </div>
                <button onClick={onOpenCreateJob} className="btn btn-primary btn-sm">
                  <Plus size={14} />
                  Tambah Job Pertama
                </button>
              </div>
            </div>
          )}

          {/* Upcoming list (top 3) */}
          {upcomingBookings.length > 1 && (
            <div style={{marginTop:'12px', display:'flex', flexDirection:'column', gap:'8px'}}>
              {upcomingBookings.slice(1, 4).map(b => (
                <div
                  key={b.id}
                  onClick={() => onOpenBookingDetail(b)}
                  className="card card-interactive"
                  style={{padding:'12px 16px', display:'flex', alignItems:'center', gap:'12px'}}
                >
                  <div style={{width:'38px', height:'38px', borderRadius:'10px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                    <Calendar size={16} color="var(--primary)" />
                  </div>
                  <div style={{flex:1, overflow:'hidden'}}>
                    <p style={{fontSize:'13px', fontWeight:'600', color:'var(--text-1)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{b.eventTitle || b.clientName}</p>
                    <p style={{fontSize:'11px', color:'var(--text-3)'}}>{b.eventDate} • {b.category || 'Event'}</p>
                  </div>
                  <span style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', flexShrink:0, fontVariantNumeric:'tabular-nums'}}>{formatRp(b.fee || 0)}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Right: Quick Links */}
        <div style={{display:'flex', flexDirection:'column', gap:'12px'}}>
          <h2 style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em'}}>Akses Cepat</h2>

          <div className="card" style={{padding:'0', overflow:'hidden'}}>
            {[
              { label: 'Testimoni Klien', desc: 'Review & feedback acara', icon: MessageSquare, color:'var(--primary)', bg:'var(--primary-light)', id:'testimonial' },
              { label: 'Bantuan & Support', desc: 'Hubungi tim @careermc.academy', icon: HelpCircle, color:'var(--success)', bg:'var(--success-light)', id:'support', href:'https://wa.link/rxug92' },
              { label: 'Invoice Generator', desc: 'Buat invoice PDF profesional', icon: DollarSign, color:'var(--warning)', bg:'var(--warning-light)', id:'invoice' },
            ].map((item, i, arr) => {
              const Icon = item.icon;
              const inner = (
                <div key={item.label} style={{display:'flex', alignItems:'center', gap:'12px', padding:'14px 16px', borderBottom: i < arr.length - 1 ? '1px solid var(--border)' : 'none', cursor:'pointer', transition:'background 0.15s', opacity: item.id === 'testimonial' ? 0.6 : 1}}
                  onClick={item.href ? undefined : () => {
                    if (item.id === 'testimonial') {
                      alert('Fitur Testimoni Klien masih dalam tahap pengembangan (Coming Soon).');
                    } else {
                      onNavigateTab(item.id);
                    }
                  }}
                >
                  <div style={{width:'36px', height:'36px', borderRadius:'10px', background:item.bg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                    <Icon size={17} color={item.color} />
                  </div>
                  <div style={{flex:1}}>
                    <p style={{fontSize:'13px', fontWeight:'600', color:'var(--text-1)'}}>{item.label}</p>
                    <p style={{fontSize:'11px', color:'var(--text-3)'}}>{item.desc}</p>
                  </div>
                  <ChevronRight size={15} color="var(--text-4)" />
                </div>
              );
              return item.href ? (
                <a key={item.label} href={item.href} target="_blank" rel="noopener noreferrer" style={{textDecoration:'none', display:'block'}}>
                  {inner}
                </a>
              ) : inner;
            })}
          </div>
        </div>
      </div>

      {/* ── FLOATING SPEED DIAL FAB (Android quick action) ── */}
      {qaConfig.masterEnabled && fabItems.length > 0 && (
        <div style={{position:'fixed', bottom:'85px', right:'20px', zIndex:9999, display:'flex', flexDirection:'column', alignItems:'flex-end', gap:'10px'}}>
          {/* Expanded Speed Dial Items */}
          {qaOpen && (
            <div className="animate-fade-in" style={{display:'flex', flexDirection:'column', alignItems:'flex-end', gap:'10px', marginBottom:'4px'}}>
              {fabItems.map(item => {
                const Icon = item.icon;
                return (
                  <div 
                    key={item.key} 
                    onClick={() => { setQaOpen(false); item.onClick(); }}
                    style={{display:'flex', alignItems:'center', gap:'10px', cursor:'pointer'}}
                  >
                    <span style={{
                      fontSize:'12px', fontWeight:'700', background:'var(--bg-surface)', 
                      color:'var(--text-1)', padding:'6px 12px', borderRadius:'8px', 
                      boxShadow:'var(--shadow-md)', border:'1px solid var(--border)'
                    }}>
                      {item.label}
                    </span>
                    <div style={{
                      width:'42px', height:'42px', borderRadius:'50%', background:'white',
                      boxShadow:'var(--shadow-md)', border:'1px solid var(--border)',
                      display:'flex', alignItems:'center', justifyContent:'center'
                    }}>
                      <Icon size={18} color={item.color} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Main FAB Trigger Button */}
          <button
            onClick={() => setQaOpen(!qaOpen)}
            style={{
              width:'56px', height:'56px', borderRadius:'50%', background:'var(--primary)',
              color:'white', display:'flex', alignItems:'center', justifyContent:'center',
              boxShadow:'0 10px 25px rgba(79,70,229,0.4)', border:'none', cursor:'pointer',
              transition:'transform 0.2s ease', transform: qaOpen ? 'rotate(45deg)' : 'none'
            }}
            aria-label="Pintasan cepat"
          >
            <Plus size={28} />
          </button>
        </div>
      )}

      <style>{`
        @media (min-width: 1024px) {
          .lg-two-col { grid-template-columns: 1fr 360px !important; }
        }
      `}</style>
    </div>
  );
};
