import React, { useState, useEffect } from 'react';
import { Calendar, Plus, ArrowRight, Mic, MessageSquare, HelpCircle, TrendingUp, TrendingDown, MapPin, ChevronRight, DollarSign, Zap, UserPlus, FileText, Tag, BarChart2, CheckSquare, Bell, User, Clock, Star, AlertCircle } from 'lucide-react';
import type { Booking } from '../types';
import { useAuth } from '../context/AuthContext';
import { getStoredQaConfig, type QuickActionConfig } from './QuickActionSettingsPage';
import { FAB } from '../components/FAB';

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
  const { userProfile, currentUser } = useAuth();
  const [selectedFilter, setSelectedFilter] = useState<TimeFilter>('THIS_MONTH');
  const [qaConfig, setQaConfig] = useState<QuickActionConfig>(getStoredQaConfig);

  useEffect(() => {
    setQaConfig(getStoredQaConfig());
  }, []);

  const now = new Date();
  const todayStr = now.toISOString().split('T')[0];
  const todayBooking = bookings.find(b => b.eventDate === todayStr && b.status !== 'CANCELLED');

  const upcomingBookings = bookings
    .filter(b => b.status !== 'CANCELLED' && b.eventDate >= todayStr)
    .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime());

  const filteredBookings = bookings.filter(b => {
    if (b.status === 'CANCELLED') return false;
    const bDate = new Date(b.eventDate);
    if (selectedFilter === 'TODAY') return b.eventDate === todayStr;
    if (selectedFilter === 'THIS_MONTH') return bDate.getMonth() === now.getMonth() && bDate.getFullYear() === now.getFullYear();
    if (selectedFilter === 'THIS_YEAR') return bDate.getFullYear() === now.getFullYear();
    return true;
  });

  const totalHonor  = filteredBookings.reduce((s, b) => s + (b.fee || 0), 0);
  const totalPaid   = filteredBookings.reduce((s, b) => s + (b.dp || 0), 0);
  const totalUnpaid = filteredBookings.reduce((s, b) => b.paymentStatus === 'PAID' ? s : s + ((b.fee || 0) - (b.dp || 0)), 0);
  const totalJobs   = filteredBookings.length;
  const paidJobs    = filteredBookings.filter(b => b.paymentStatus === 'PAID').length;
  const paidRatio   = totalHonor > 0 ? Math.round((totalPaid / totalHonor) * 100) : 0;

  const bulanMap = ['Januari','Februari','Maret','April','Mei','Juni','Juli','Agustus','September','Oktober','November','Desember'];
  const greeting = now.getHours() < 12 ? 'Selamat Pagi' : now.getHours() < 17 ? 'Selamat Siang' : 'Selamat Malam';
  const displayName = userProfile?.stageName || userProfile?.displayName || currentUser?.displayName || 'MC Professional';
  const userPhoto = userProfile?.photoUrl || userProfile?.photoUri || currentUser?.photoURL;

  const hariMap = ['Minggu', 'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu'];
  const dayName = hariMap[now.getDay()];
  const dayNum = now.getDate();
  const monthName = bulanMap[now.getMonth()];
  const yearNum = now.getFullYear();
  const formattedFullDate = `${dayName}, ${dayNum} ${monthName} ${yearNum}`;

  // Speed Dial items
  const fabItems = [
    { key: 'addJob', label: 'Buat Job', icon: Plus, color: 'var(--primary)', onClick: onOpenCreateJob },
    { key: 'addClient', label: 'Tambah Klien', icon: UserPlus, color: '#2563EB', onClick: () => onNavigateTab('clients') },
    { key: 'addPayment', label: 'Catat Pembayaran', icon: DollarSign, color: 'var(--success)', onClick: () => onNavigateTab('finance') },
    { key: 'invoice', label: 'Buat Invoice', icon: FileText, color: '#0369A1', onClick: () => onNavigateTab('invoice') },
    { key: 'rateCard', label: 'Rate Card', icon: Tag, color: 'var(--primary)', onClick: () => onNavigateTab('price-list') },
    { key: 'analytics', label: 'Analisis Bisnis', icon: BarChart2, color: 'var(--success)', onClick: () => onNavigateTab('analytics') },
    { key: 'followUp', label: 'Follow Up Klien', icon: MessageSquare, color: '#2563EB', onClick: () => onNavigateTab('followup') },
    { key: 'todo', label: 'Daftar Tugas', icon: CheckSquare, color: '#7C3AED', onClick: () => onNavigateTab('todo') },
  ].filter(item => (qaConfig as any)[item.key]);

  return (
    <div className="animate-fade-in" style={{width:'100%', display:'flex', flexDirection:'column', gap:'14px', paddingBottom:'16px'}}>

      {/* ── POLISHED HEADER WELCOME BANNER ── */}
      <div 
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '16px',
          padding: '20px 24px',
          background: 'var(--bg-surface)',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)',
          position: 'relative',
          overflow: 'hidden'
        }}
      >
        {/* Subtle background glow */}
        <div style={{
          position: 'absolute',
          top: '-40px',
          right: '-40px',
          width: '180px',
          height: '180px',
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(79, 70, 229, 0.08) 0%, rgba(79, 70, 229, 0) 70%)',
          pointerEvents: 'none'
        }} />

        <div style={{ flex: 1, minWidth: 0, zIndex: 1 }}>
          {/* Date & Greeting Badge Row */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
            <span style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '5px',
              padding: '3px 10px',
              borderRadius: '9999px',
              background: 'var(--primary-light)',
              color: 'var(--primary)',
              fontSize: '11px',
              fontWeight: '700',
              border: '1px solid rgba(79, 70, 229, 0.15)'
            }}>
              <Calendar size={12} />
              {formattedFullDate}
            </span>
            <span style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '5px',
              padding: '3px 10px',
              borderRadius: '9999px',
              background: 'var(--bg-surface-2)',
              color: 'var(--text-3)',
              fontSize: '11px',
              fontWeight: '600',
              border: '1px solid var(--border)'
            }}>
              <Clock size={12} />
              {greeting}
            </span>
          </div>

          {/* Main Title & Name */}
          <h1 style={{
            fontSize: 'clamp(20px, 3vw, 26px)',
            fontWeight: '800',
            color: 'var(--text-1)',
            letterSpacing: '-0.03em',
            lineHeight: '1.25',
            margin: 0
          }}>
            <span style={{ fontWeight: '500', color: 'var(--text-3)' }}>{greeting}, </span>
            <span style={{ color: 'var(--primary)', fontWeight: '800' }}>{displayName}</span>
            <span style={{ marginLeft: '6px' }}>👋</span>
          </h1>

          {/* Subtitle / Status */}
          <p style={{
            fontSize: '12.5px',
            color: 'var(--text-3)',
            marginTop: '6px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            fontWeight: '500'
          }}>
            <span>Ada <strong style={{ color: 'var(--text-1)', fontWeight: '700' }}>{upcomingBookings.length} job mendatang</strong> di agenda Anda.</span>
          </p>
        </div>
      </div>

      {/* ── TODAY'S EVENT BANNER ── */}
      {todayBooking && (
        <div style={{
          padding:'14px 16px',
          borderRadius:'14px',
          background:'linear-gradient(135deg, #4F46E5 0%, #6D28D9 100%)',
          boxShadow:'0 6px 20px rgba(79,70,229,0.3)',
          display:'flex', alignItems:'center', justifyContent:'space-between', gap:'12px'
        }}>
          <div style={{flex:1, minWidth:0}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'rgba(255,255,255,0.65)', textTransform:'uppercase', letterSpacing:'0.1em', display:'block', marginBottom:'3px'}}>🎤 Job Hari Ini</span>
            <h3 style={{fontSize:'15px', fontWeight:'700', color:'white', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{todayBooking.eventTitle || todayBooking.clientName}</h3>
            <p style={{fontSize:'11px', color:'rgba(255,255,255,0.65)', display:'flex', alignItems:'center', gap:'6px', marginTop:'2px'}}>
              <Clock size={10} /><span>{todayBooking.eventTime || 'TBA'} WIB</span>
              {todayBooking.venue && <><span>•</span><MapPin size={10} /><span style={{overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{todayBooking.venue}</span></>}
            </p>
          </div>
          <button
            onClick={() => onOpenDayMode(todayBooking)}
            style={{padding:'9px 14px', borderRadius:'10px', background:'rgba(255,255,255,0.18)', backdropFilter:'blur(8px)', border:'1px solid rgba(255,255,255,0.3)', color:'white', fontWeight:'700', fontSize:'12px', cursor:'pointer', display:'flex', alignItems:'center', gap:'6px', flexShrink:0, whiteSpace:'nowrap'}}
          >
            <Mic size={13} /> Day Mode
          </button>
        </div>
      )}

      {/* ── FILTER CHIPS ── */}
      <div style={{display:'flex', alignItems:'center', gap:'6px', overflowX:'auto', paddingBottom:'2px'}} className="scrollbar-none">
        {timeFilters.map(f => (
          <button key={f.id} onClick={() => setSelectedFilter(f.id)} className={`chip${selectedFilter === f.id ? ' active' : ''}`}>
            {f.label}
          </button>
        ))}
      </div>

      {/* ── KPI STATS: 2x2 compact grid ── */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'10px'}}>
        {/* Omzet */}
        <div className="stat-card" style={{padding:'14px 16px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'8px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Total Omzet</span>
            <div style={{width:'28px', height:'28px', borderRadius:'8px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <DollarSign size={13} color="var(--primary)" />
            </div>
          </div>
          <div style={{fontSize:'clamp(16px,3vw,20px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{formatRp(totalHonor)}</div>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'3px'}}>{totalJobs} job tercatat</p>
        </div>

        {/* Diterima */}
        <div className="stat-card" style={{padding:'14px 16px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'8px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Diterima</span>
            <div style={{width:'28px', height:'28px', borderRadius:'8px', background:'var(--success-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <TrendingUp size={13} color="var(--success)" />
            </div>
          </div>
          <div style={{fontSize:'clamp(16px,3vw,20px)', fontWeight:'800', color:'var(--success)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{formatRp(totalPaid)}</div>
          {/* Progress bar */}
          <div style={{height:'4px', background:'var(--bg-surface-2)', borderRadius:'99px', marginTop:'6px', overflow:'hidden'}}>
            <div style={{height:'100%', width:`${paidRatio}%`, background:'var(--success)', borderRadius:'99px', transition:'width 0.6s ease'}} />
          </div>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'3px'}}>{paidRatio}% dari omzet</p>
        </div>

        {/* Piutang */}
        <div className="stat-card" style={{padding:'14px 16px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'8px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Piutang</span>
            <div style={{width:'28px', height:'28px', borderRadius:'8px', background:'var(--warning-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <AlertCircle size={13} color="var(--warning)" />
            </div>
          </div>
          <div style={{fontSize:'clamp(16px,3vw,20px)', fontWeight:'800', color:'var(--warning)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{formatRp(totalUnpaid)}</div>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'3px'}}>{totalJobs - paidJobs} job belum lunas</p>
        </div>

        {/* Total Job */}
        <div className="stat-card" style={{padding:'14px 16px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'8px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Total Job</span>
            <div style={{width:'28px', height:'28px', borderRadius:'8px', background:'var(--bg-surface-2)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <Calendar size={13} color="var(--text-3)" />
            </div>
          </div>
          <div style={{fontSize:'clamp(16px,3vw,20px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{totalJobs}</div>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'3px'}}>{paidJobs} lunas • {totalJobs - paidJobs} pending</p>
        </div>
      </div>

      {/* ── UPCOMING AGENDA ── */}
      <div>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'10px'}}>
          <h2 style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', display:'flex', alignItems:'center', gap:'6px'}}>
            <Calendar size={14} color="var(--primary)" /> Agenda Mendatang
          </h2>
          <button onClick={() => onNavigateTab('agenda')} style={{fontSize:'11px', fontWeight:'600', color:'var(--primary)', background:'none', border:'none', cursor:'pointer', display:'flex', alignItems:'center', gap:'3px'}}>
            Semua <ArrowRight size={12} />
          </button>
        </div>

        {upcomingBookings.length === 0 ? (
          <div className="card" style={{padding:'20px', textAlign:'center'}}>
            <Calendar size={20} color="var(--text-4)" style={{margin:'0 auto 8px'}} />
            <p style={{fontSize:'13px', fontWeight:'600', color:'var(--text-2)'}}>Belum ada agenda mendatang</p>
            <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'2px'}}>Tambah job baru lewat tombol + di bawah</p>
          </div>
        ) : (
          <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
            {/* Featured next booking */}
            <div
              className="card card-interactive"
              onClick={() => onOpenBookingDetail(upcomingBookings[0])}
              style={{padding:'14px 16px'}}
            >
              <div style={{display:'flex', alignItems:'flex-start', gap:'12px'}}>
                <div style={{width:'42px', height:'42px', borderRadius:'12px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                  <Mic size={18} color="var(--primary)" />
                </div>
                <div style={{flex:1, minWidth:0}}>
                  <div style={{display:'flex', alignItems:'center', gap:'6px', marginBottom:'3px', flexWrap:'wrap'}}>
                    <span className="badge badge-primary" style={{fontSize:'10px'}}>{upcomingBookings[0].category || 'Event'}</span>
                    <span className={`badge badge-dot ${upcomingBookings[0].paymentStatus === 'PAID' ? 'badge-success' : upcomingBookings[0].dp > 0 ? 'badge-warning' : 'badge-error'}`} style={{fontSize:'10px'}}>
                      {upcomingBookings[0].paymentStatus === 'PAID' ? 'Lunas' : upcomingBookings[0].dp > 0 ? 'DP' : 'Belum Bayar'}
                    </span>
                  </div>
                  <h3 style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
                    {upcomingBookings[0].eventTitle || upcomingBookings[0].clientName}
                  </h3>
                  <p style={{fontSize:'11px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'6px', marginTop:'2px', flexWrap:'wrap'}}>
                    <span style={{display:'flex', alignItems:'center', gap:'3px'}}><Calendar size={10} />{upcomingBookings[0].eventDate}</span>
                    {upcomingBookings[0].eventTime && <span style={{display:'flex', alignItems:'center', gap:'3px'}}><Clock size={10} />{upcomingBookings[0].eventTime} WIB</span>}
                    {upcomingBookings[0].venue && <span style={{display:'flex', alignItems:'center', gap:'3px', overflow:'hidden'}}><MapPin size={10} /><span style={{overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{upcomingBookings[0].venue}</span></span>}
                  </p>
                </div>
                <div style={{textAlign:'right', flexShrink:0}}>
                  <span style={{fontSize:'15px', fontWeight:'800', color:'var(--primary)', fontVariantNumeric:'tabular-nums'}}>{formatRp(upcomingBookings[0].fee || 0)}</span>
                  <ChevronRight size={14} color="var(--text-4)" style={{display:'block', marginTop:'4px', marginLeft:'auto'}} />
                </div>
              </div>
            </div>

            {/* Rest of upcoming (compact rows) */}
            {upcomingBookings.slice(1, 5).map(b => (
              <div
                key={b.id}
                onClick={() => onOpenBookingDetail(b)}
                className="card card-interactive"
                style={{padding:'10px 14px', display:'flex', alignItems:'center', gap:'10px'}}
              >
                <div style={{width:'34px', height:'34px', borderRadius:'10px', background:'var(--bg-surface-2)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                  <Calendar size={14} color="var(--text-3)" />
                </div>
                <div style={{flex:1, minWidth:0}}>
                  <p style={{fontSize:'12px', fontWeight:'600', color:'var(--text-1)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{b.eventTitle || b.clientName}</p>
                  <p style={{fontSize:'11px', color:'var(--text-3)'}}>{b.eventDate} • {b.category || 'Event'}</p>
                </div>
                <div style={{textAlign:'right', flexShrink:0}}>
                  <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-2)', fontVariantNumeric:'tabular-nums'}}>{formatRp(b.fee || 0)}</span>
                  <span className={`badge badge-dot ${b.paymentStatus === 'PAID' ? 'badge-success' : b.dp > 0 ? 'badge-warning' : 'badge-error'}`} style={{fontSize:'10px', display:'block', marginTop:'2px'}}>
                    {b.paymentStatus === 'PAID' ? 'Lunas' : b.dp > 0 ? 'DP' : 'Belum'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── QUICK LINKS ── */}
      <div>
        <h2 style={{fontSize:'13px', fontWeight:'700', color:'var(--text-1)', marginBottom:'10px', display:'flex', alignItems:'center', gap:'6px'}}>
          <Zap size={14} color="var(--primary)" /> Akses Cepat
        </h2>
        <div className="card" style={{padding:'0', overflow:'hidden'}}>
          {[
            { label: 'Analisis Bisnis', desc: 'Grafik & laporan performa', icon: BarChart2, color:'var(--primary)', bg:'var(--primary-light)', id:'analytics' },
            { label: 'Invoice Generator', desc: 'Buat invoice PDF profesional', icon: FileText, color:'#0369A1', bg:'rgba(3,105,161,0.1)', id:'invoice' },
            { label: 'Testimoni Klien', desc: 'Review & rating dari klien', icon: Star, color:'var(--warning)', bg:'var(--warning-light)', id:'testimonial' },
            { label: 'Follow Up Klien', desc: 'Manajemen prospek & leads', icon: MessageSquare, color:'var(--success)', bg:'var(--success-light)', id:'followup' },
            { label: 'Bantuan & Support', desc: 'Hubungi @careermc.academy', icon: HelpCircle, color:'var(--text-3)', bg:'var(--bg-surface-2)', id:'support', href:'https://wa.link/rxug92' },
          ].map((item, i, arr) => {
            const Icon = item.icon;
            const inner = (
              <div
                key={item.label}
                style={{display:'flex', alignItems:'center', gap:'10px', padding:'11px 14px', borderBottom: i < arr.length - 1 ? '1px solid var(--border)' : 'none', cursor:'pointer'}}
                onClick={item.href ? undefined : () => onNavigateTab(item.id)}
              >
                <div style={{width:'32px', height:'32px', borderRadius:'9px', background:item.bg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                  <Icon size={15} color={item.color} />
                </div>
                <div style={{flex:1}}>
                  <p style={{fontSize:'12px', fontWeight:'700', color:'var(--text-1)'}}>{item.label}</p>
                  <p style={{fontSize:'11px', color:'var(--text-3)'}}>{item.desc}</p>
                </div>
                <ChevronRight size={14} color="var(--text-4)" />
              </div>
            );
            return item.href ? (
              <a key={item.label} href={item.href} target="_blank" rel="noopener noreferrer" style={{textDecoration:'none', display:'block'}}>{inner}</a>
            ) : inner;
          })}
        </div>
      </div>

      {/* ── FAB ── */}
      {qaConfig.masterEnabled && fabItems.length > 0 && (
        <FAB label="Buat Job" items={fabItems} />
      )}

      <style>{`
        @media (min-width: 1024px) {
          .home-two-col { display: grid; grid-template-columns: 1fr 340px; gap: 16px; align-items: start; }
        }
      `}</style>
    </div>
  );
};
