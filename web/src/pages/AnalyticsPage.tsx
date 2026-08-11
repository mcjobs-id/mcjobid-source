import React, { useState } from 'react';
import { BarChart2, TrendingUp, AlertTriangle, Lightbulb, ChevronRight, Zap, Award, DollarSign, Calendar, CreditCard, TrendingDown } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { Booking, Expense } from '../types';
import { getBookingFee, getBookingDp, getBookingOutstanding } from '../types';
import { useNavigate } from 'react-router-dom';

interface AnalyticsPageProps {
  onBack: () => void;
  bookings: Booking[];
  expenses: Expense[];
}

export const AnalyticsPage: React.FC<AnalyticsPageProps> = ({ onBack, bookings, expenses }) => {
  const navigate = useNavigate();
  const currentYear = new Date().getFullYear();
  const [period, setPeriod] = useState<'YEAR' | 'MONTH' | 'QUARTER' | 'ALL'>('YEAR');

  const filteredBookings = bookings.filter(b => {
    if (b.status === 'CANCELLED' || b.status === 'cancelled') return false;
    const bDate = b.date || b.eventDate || '';
    if (period === 'ALL') return true;
    if (!bDate) return false;
    if (period === 'YEAR') return bDate.startsWith(currentYear.toString());
    if (period === 'MONTH') {
      const cm = (new Date().getMonth() + 1).toString().padStart(2, '0');
      return bDate.startsWith(`${currentYear}-${cm}`);
    }
    if (period === 'QUARTER') {
      const m = new Date().getMonth() + 1;
      const qm = m <= 3 ? ['01','02','03'] : m <= 6 ? ['04','05','06'] : m <= 9 ? ['07','08','09'] : ['10','11','12'];
      return qm.some(q => bDate.startsWith(`${currentYear}-${q}`));
    }
    return true;
  });

  const filteredExpenses = expenses.filter(e => {
    if (period === 'ALL') return true;
    if (period === 'YEAR') return e.date.startsWith(currentYear.toString());
    if (period === 'MONTH') {
      const cm = (new Date().getMonth() + 1).toString().padStart(2, '0');
      return e.date.startsWith(`${currentYear}-${cm}`);
    }
    return true;
  });

  const grossRevenue   = filteredBookings.reduce((s, b) => s + getBookingFee(b), 0);
  const totalExpense   = filteredExpenses.reduce((s, e) => s + (e.amount || 0), 0);
  const netIncome      = grossRevenue - totalExpense;
  const margin         = grossRevenue > 0 ? ((netIncome / grossRevenue) * 100).toFixed(1) : '0';
  const collectedCash  = filteredBookings.reduce((s, b) => s + getBookingDp(b), 0);
  const totalOutstanding = filteredBookings.reduce((s, b) => s + getBookingOutstanding(b), 0);
  const avgFee         = filteredBookings.length > 0 ? Math.round(grossRevenue / filteredBookings.length) : 0;
  const collectionRate = grossRevenue > 0 ? Math.round((collectedCash / grossRevenue) * 100) : 0;
  const paidJobs       = filteredBookings.filter(b => b.paymentStatus === 'PAID').length;

  const fmt = (v: number) => {
    if (v >= 1_000_000) return `Rp ${(v / 1_000_000).toFixed(1)}jt`;
    if (v >= 1_000) return `Rp ${(v / 1_000).toFixed(0)}rb`;
    return `Rp ${v.toLocaleString('id-ID')}`;
  };
  const fmtFull = (v: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(v);

  // Smart Insights
  const insights: { title: string; desc: string; icon: any; color: string; bg: string; actionText: string; actionRoute: string }[] = [];
  if (totalOutstanding > 0) insights.push({ title: 'Piutang Belum Lunas', desc: `${fmtFull(totalOutstanding)} perlu di-follow-up.`, icon: AlertTriangle, color: 'var(--error)', bg: 'var(--error-light)', actionText: 'Follow-Up', actionRoute: '/followup' });
  if (grossRevenue > 10_000_000) insights.push({ title: 'Performa Baik!', desc: `Omzet ${fmt(grossRevenue)}, koleksi ${collectionRate}%.`, icon: Award, color: 'var(--success)', bg: 'var(--success-light)', actionText: 'Agenda', actionRoute: '/agenda' });
  if (filteredBookings.length >= 5) insights.push({ title: 'Saatnya Naikkan Rate', desc: `${filteredBookings.length} job cukup padat. Review rate card Anda.`, icon: Lightbulb, color: '#7C3AED', bg: '#F5F3FF', actionText: 'Rate Card', actionRoute: '/price-list' });

  // Chart
  const monthNames = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Agu','Sep','Okt','Nov','Des'];
  const chartData = monthNames.map((m, i) => {
    const mp = `${currentYear}-${(i + 1).toString().padStart(2, '0')}`;
    const rev = bookings.filter(b => (b.date || b.eventDate || '').startsWith(mp) && b.status !== 'CANCELLED').reduce((s, b) => s + getBookingFee(b), 0);
    const exp = expenses.filter(e => e.date.startsWith(mp)).reduce((s, e) => s + (e.amount || 0), 0);
    return { name: m, Omset: rev, Biaya: exp };
  });

  return (
    <div className="animate-fade-in" style={{width:'100%', paddingBottom:'24px', display:'flex', flexDirection:'column', gap:'14px'}}>

      {/* Period Filter */}
      <div style={{display:'flex', gap:'6px', overflowX:'auto', paddingBottom:'2px'}} className="scrollbar-none">
        {[
          { id: 'YEAR', label: `Tahun (${currentYear})` },
          { id: 'MONTH', label: 'Bulan Ini' },
          { id: 'QUARTER', label: 'Triwulan' },
          { id: 'ALL', label: 'Semua' }
        ].map(p => (
          <button
            key={p.id}
            onClick={() => setPeriod(p.id as any)}
            className={`chip${period === p.id ? ' active' : ''}`}
            style={{whiteSpace:'nowrap'}}
          >
            {p.label}
          </button>
        ))}
      </div>

      {/* Hero Net Income — compact */}
      <div style={{
        padding:'16px 20px',
        borderRadius:'16px',
        background:'linear-gradient(135deg, #4F46E5 0%, #3730A3 100%)',
        color:'white',
        position:'relative', overflow:'hidden'
      }}>
        <div style={{position:'absolute', top:'-30px', right:'-30px', width:'120px', height:'120px', borderRadius:'50%', background:'rgba(255,255,255,0.06)', pointerEvents:'none'}} />
        <p style={{fontSize:'10px', fontWeight:'800', textTransform:'uppercase', letterSpacing:'0.1em', color:'rgba(255,255,255,0.65)', marginBottom:'4px'}}>NET INCOME (LABA BERSIH)</p>
        <p style={{fontSize:'clamp(22px, 5vw, 30px)', fontWeight:'900', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums', marginBottom:'8px'}}>{fmtFull(netIncome)}</p>
        <div style={{display:'flex', flexWrap:'wrap', gap:'8px', alignItems:'center'}}>
          <span style={{fontSize:'11px', fontWeight:'700', background:'rgba(255,255,255,0.18)', padding:'3px 10px', borderRadius:'99px'}}>Margin: {margin}%</span>
          <span style={{fontSize:'11px', color:'rgba(255,255,255,0.75)'}}>Omset: {fmt(grossRevenue)} • Biaya: {fmt(totalExpense)}</span>
        </div>
      </div>

      {/* 4 KPI Cards — 2x2 compact grid */}
      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'10px'}}>
        {/* Total Job */}
        <div className="card" style={{padding:'12px 14px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'6px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Total Job</span>
            <div style={{width:'26px', height:'26px', borderRadius:'7px', background:'var(--primary-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <Calendar size={12} color="var(--primary)" />
            </div>
          </div>
          <p style={{fontSize:'clamp(18px, 4vw, 22px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em'}}>{filteredBookings.length}</p>
          <p style={{fontSize:'10px', color:'var(--text-4)', marginTop:'2px'}}>{paidJobs} lunas • avg {fmt(avgFee)}</p>
        </div>

        {/* Kas Masuk */}
        <div className="card" style={{padding:'12px 14px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'6px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Kas Masuk</span>
            <div style={{width:'26px', height:'26px', borderRadius:'7px', background:'var(--success-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <TrendingUp size={12} color="var(--success)" />
            </div>
          </div>
          <p style={{fontSize:'clamp(14px, 3.5vw, 18px)', fontWeight:'800', color:'var(--success)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{fmt(collectedCash)}</p>
          {/* Collection progress */}
          <div style={{height:'3px', background:'var(--bg-surface-2)', borderRadius:'99px', margin:'5px 0 2px', overflow:'hidden'}}>
            <div style={{height:'100%', width:`${collectionRate}%`, background:'var(--success)', borderRadius:'99px'}} />
          </div>
          <p style={{fontSize:'10px', color:'var(--text-4)'}}>{collectionRate}% dari omzet</p>
        </div>

        {/* Piutang */}
        <div className="card" style={{padding:'12px 14px', borderLeft: totalOutstanding > 0 ? '3px solid var(--error)' : 'none'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'6px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color: totalOutstanding > 0 ? 'var(--error)' : 'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Piutang</span>
            <div style={{width:'26px', height:'26px', borderRadius:'7px', background:'var(--warning-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <CreditCard size={12} color="var(--warning)" />
            </div>
          </div>
          <p style={{fontSize:'clamp(14px, 3.5vw, 18px)', fontWeight:'800', color: totalOutstanding > 0 ? 'var(--error)' : 'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{fmt(totalOutstanding)}</p>
          <p style={{fontSize:'10px', color:'var(--text-4)', marginTop:'2px'}}>{totalOutstanding > 0 ? 'Perlu follow-up' : 'Semua aman ✓'}</p>
        </div>

        {/* Pengeluaran */}
        <div className="card" style={{padding:'12px 14px'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'6px'}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Pengeluaran</span>
            <div style={{width:'26px', height:'26px', borderRadius:'7px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <TrendingDown size={12} color="var(--error)" />
            </div>
          </div>
          <p style={{fontSize:'clamp(14px, 3.5vw, 18px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>{fmt(totalExpense)}</p>
          <p style={{fontSize:'10px', color:'var(--text-4)', marginTop:'2px'}}>{filteredExpenses.length} transaksi</p>
        </div>
      </div>

      {/* Smart Insights — compact */}
      {insights.length > 0 && (
        <div>
          <h3 style={{fontSize:'12px', fontWeight:'700', color:'var(--text-1)', marginBottom:'8px', display:'flex', alignItems:'center', gap:'6px'}}>
            <Zap size={13} color="var(--primary)" /> Smart Insights
          </h3>
          <div style={{display:'flex', flexDirection:'column', gap:'8px'}}>
            {insights.map((item, idx) => {
              const Icon = item.icon;
              return (
                <div key={idx} className="card" style={{padding:'12px 14px', display:'flex', alignItems:'center', gap:'12px', borderLeft:`3px solid ${item.color}`}}>
                  <div style={{width:'32px', height:'32px', borderRadius:'9px', background:item.bg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                    <Icon size={15} color={item.color} />
                  </div>
                  <div style={{flex:1, minWidth:0}}>
                    <p style={{fontSize:'12px', fontWeight:'700', color:'var(--text-1)'}}>{item.title}</p>
                    <p style={{fontSize:'11px', color:'var(--text-3)', lineHeight:'1.4', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{item.desc}</p>
                  </div>
                  <button
                    onClick={() => navigate(item.actionRoute)}
                    style={{flexShrink:0, fontSize:'11px', fontWeight:'700', color:item.color, background:'transparent', border:`1px solid ${item.color}`, borderRadius:'8px', padding:'4px 10px', cursor:'pointer', whiteSpace:'nowrap'}}
                  >
                    {item.actionText}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Chart — compact */}
      <div className="card" style={{padding:'14px 16px'}}>
        <h3 style={{fontSize:'12px', fontWeight:'700', color:'var(--text-1)', marginBottom:'14px', display:'flex', alignItems:'center', gap:'6px'}}>
          <BarChart2 size={13} color="var(--primary)" /> Tren Omset {currentYear}
        </h3>
        <div style={{height:'200px', width:'100%'}}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 4, right: 4, left: -20, bottom: 0 }} barSize={8} barGap={2}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fontSize: 10, fill: 'var(--text-4)'}} />
              <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10, fill: 'var(--text-4)'}} tickFormatter={v => v >= 1_000_000 ? `${(v/1_000_000).toFixed(0)}M` : v >= 1000 ? `${(v/1000).toFixed(0)}K` : `${v}`} />
              <Tooltip formatter={(v: number) => [fmt(v), undefined]} contentStyle={{borderRadius:'10px', border:'1px solid var(--border)', fontSize:'12px'}} />
              <Bar dataKey="Omset" fill="var(--primary)" radius={[3,3,0,0]} />
              <Bar dataKey="Biaya" fill="var(--error)" radius={[3,3,0,0]} opacity={0.7} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        {/* Legend */}
        <div style={{display:'flex', gap:'14px', marginTop:'8px', justifyContent:'center'}}>
          <span style={{fontSize:'10px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'4px'}}>
            <span style={{width:'8px', height:'8px', borderRadius:'2px', background:'var(--primary)', display:'inline-block'}} /> Omset
          </span>
          <span style={{fontSize:'10px', color:'var(--text-3)', display:'flex', alignItems:'center', gap:'4px'}}>
            <span style={{width:'8px', height:'8px', borderRadius:'2px', background:'var(--error)', display:'inline-block', opacity:0.7}} /> Biaya
          </span>
        </div>
      </div>

    </div>
  );
};
