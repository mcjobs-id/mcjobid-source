import React, { useState } from 'react';
import { ArrowLeft, BarChart2, TrendingUp, Calendar, DollarSign, Activity, AlertTriangle, Lightbulb, ChevronRight, Zap, CheckCircle2, Award } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
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

  // Filter bookings by period
  const filteredBookings = bookings.filter(b => {
    if (b.status === 'CANCELLED' || b.status === 'cancelled') return false;
    const bDate = b.date || b.eventDate || '';
    if (!bDate) return true;
    
    if (period === 'YEAR') return bDate.startsWith(currentYear.toString());
    if (period === 'MONTH') {
      const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0');
      return bDate.startsWith(`${currentYear}-${currentMonth}`);
    }
    if (period === 'QUARTER') {
      const m = new Date().getMonth() + 1;
      const qMonths = m <= 3 ? ['01','02','03'] : m <= 6 ? ['04','05','06'] : m <= 9 ? ['07','08','09'] : ['10','11','12'];
      return qMonths.some(qm => bDate.startsWith(`${currentYear}-${qm}`));
    }
    return true;
  });

  const filteredExpenses = expenses.filter(e => {
    if (period === 'YEAR') return e.date.startsWith(currentYear.toString());
    if (period === 'MONTH') {
      const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0');
      return e.date.startsWith(`${currentYear}-${currentMonth}`);
    }
    return true;
  });

  // Calculate Metrics
  const grossRevenue = filteredBookings.reduce((sum, b) => sum + getBookingFee(b), 0);
  const totalExpense = filteredExpenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const netIncome = grossRevenue - totalExpense;
  const margin = grossRevenue > 0 ? ((netIncome / grossRevenue) * 100).toFixed(1) : '0';

  const collectedCash = filteredBookings.reduce((sum, b) => sum + getBookingDp(b), 0);
  const totalOutstanding = filteredBookings.reduce((sum, b) => sum + getBookingOutstanding(b), 0);
  
  const avgFee = filteredBookings.length > 0 ? Math.round(grossRevenue / filteredBookings.length) : 0;
  const collectionRate = grossRevenue > 0 ? Math.round((collectedCash / grossRevenue) * 100) : 0;

  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  // Generate Smart Insights (Android logic)
  const insights = [];
  if (totalOutstanding > 0) {
    insights.push({
      type: 'CASH_FLOW_WARNING',
      title: 'Peringatan Piutang Klien!',
      desc: `Terdapat sisa tagihan Rp ${totalOutstanding.toLocaleString('id-ID')} yang belum dilunasi. Lakukan follow-up ke klien terkait.`,
      icon: AlertTriangle,
      color: 'var(--error)',
      bg: 'var(--error-light)',
      actionText: 'Ke Pusat Follow Up 🚀',
      actionRoute: '/followup'
    });
  }

  if (grossRevenue > 10000000) {
    insights.push({
      type: 'REVENUE_GROWTH',
      title: 'Performa Bisnis Sangat Baik!',
      desc: `Omset Anda mencapai ${formatRp(grossRevenue)} dengan rasio kas terkumpul ${collectionRate}%. Pertahankan kualitas perform jam terbang Anda!`,
      icon: Award,
      color: 'var(--success)',
      bg: 'var(--success-light)',
      actionText: 'Lihat Agenda Job 📅',
      actionRoute: '/agenda'
    });
  }

  if (filteredBookings.length >= 5) {
    insights.push({
      type: 'RATE_CARD_OPTIMIZATION',
      title: 'Peluang Optimasi Rate Card!',
      desc: `Jam terbang Anda cukup padat (${filteredBookings.length} job). Ini saat yang tepat untuk menaikkan paket harga Rate Card Anda.`,
      icon: Lightbulb,
      color: '#7C3AED',
      bg: '#F5F3FF',
      actionText: 'Update Rate Card 💰',
      actionRoute: '/price-list'
    });
  }

  // Monthly Chart Data
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
  const chartData = monthNames.map((m, index) => {
    const monthStr = (index + 1).toString().padStart(2, '0');
    const monthPrefix = `${currentYear}-${monthStr}`;
    
    const mRevenue = bookings
      .filter(b => (b.date || b.eventDate || '').startsWith(monthPrefix) && b.status !== 'CANCELLED')
      .reduce((sum, b) => sum + getBookingFee(b), 0);
      
    const mExpense = expenses
      .filter(e => e.date.startsWith(monthPrefix))
      .reduce((sum, e) => sum + (e.amount || 0), 0);
      
    return {
      name: m,
      Omset: mRevenue,
      Pengeluaran: mExpense,
      Profit: mRevenue - mExpense
    };
  });

  return (
    <div className="animate-fade-in" style={{maxWidth:'1000px', margin:'0 auto', paddingBottom:'32px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <BarChart2 size={20} color="var(--primary)" /> Analisis & Insight Performa
            </h1>
            <p className="page-subtitle">Ringkasan keuangan, laba bersih, piutang, dan rekomendasi otomatis.</p>
          </div>
        </div>
      </div>

      {/* ── PERIOD FILTER CHIPS ── */}
      <div style={{display:'flex', gap:'8px', marginBottom:'20px', overflowX:'auto'}}>
        {[
          { id: 'YEAR', label: `Tahun Ini (${currentYear})` },
          { id: 'MONTH', label: 'Bulan Ini' },
          { id: 'QUARTER', label: 'Triwulan Ini' },
          { id: 'ALL', label: 'Semua Waktu' }
        ].map(p => (
          <button
            key={p.id}
            onClick={() => setPeriod(p.id as any)}
            className={`badge ${period === p.id ? 'badge-primary' : 'badge-neutral'}`}
            style={{
              cursor:'pointer', padding:'8px 16px', fontSize:'13px', borderRadius:'9999px',
              border: period === p.id ? 'none' : '1px solid var(--border)',
              background: period === p.id ? 'var(--primary)' : 'var(--bg-surface)'
            }}
          >
            {p.label}
          </button>
        ))}
      </div>

      {/* ── HERO NET INCOME CARD ── */}
      <div className="card" style={{padding:'28px', background:'linear-gradient(135deg, #4F46E5 0%, #3730A3 100%)', color:'white', marginBottom:'24px', position:'relative', overflow:'hidden'}}>
        <div style={{position:'relative', zIndex:1}}>
          <span style={{fontSize:'12px', fontWeight:'700', textTransform:'uppercase', letterSpacing:'0.08em', color:'rgba(255,255,255,0.75)'}}>
            NET INCOME (LABA BERSIH)
          </span>
          
          <h2 style={{fontSize:'clamp(28px, 4vw, 36px)', fontWeight:'900', letterSpacing:'-0.03em', fontVariantNumeric:'tabular-nums', margin:'6px 0 12px'}}>
            {formatRp(netIncome)}
          </h2>

          <div style={{display:'flex', flexWrap:'wrap', gap:'12px', alignItems:'center'}}>
            <span style={{fontSize:'12px', fontWeight:'700', background:'rgba(255,255,255,0.2)', padding:'4px 12px', borderRadius:'9999px'}}>
              Margin Keuntungan: {margin}%
            </span>
            <span style={{fontSize:'12px', color:'rgba(255,255,255,0.8)'}}>
              Omset: {formatRp(grossRevenue)} • Pengeluaran: {formatRp(totalExpense)}
            </span>
          </div>
        </div>
      </div>

      {/* ── BUSINESS METRICS 2x2 GRID ── */}
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(220px, 1fr))', gap:'16px', marginBottom:'24px'}}>
        
        <div className="card" style={{padding:'20px'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase'}}>Total Job Acara</span>
          <h3 style={{fontSize:'24px', fontWeight:'800', color:'var(--text-1)', margin:'4px 0'}}>
            {filteredBookings.length} <span style={{fontSize:'13px', fontWeight:'normal', color:'var(--text-3)'}}>Acara</span>
          </h3>
          <p style={{fontSize:'12px', color:'var(--text-3)'}}>Rata-rata Tarif: {formatRp(avgFee)}</p>
        </div>

        <div className="card" style={{padding:'20px'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase'}}>Kas Masuk (DP/Lunas)</span>
          <h3 style={{fontSize:'24px', fontWeight:'800', color:'var(--success)', margin:'4px 0'}}>
            {formatRp(collectedCash)}
          </h3>
          <p style={{fontSize:'12px', color:'var(--text-3)'}}>Terkumpul: {collectionRate}% dari Total Omset</p>
        </div>

        <div className="card" style={{padding:'20px', borderLeft: totalOutstanding > 0 ? '4px solid var(--error)' : 'none'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color: totalOutstanding > 0 ? 'var(--error)' : 'var(--text-3)', textTransform:'uppercase'}}>Sisa Piutang Klien</span>
          <h3 style={{fontSize:'24px', fontWeight:'800', color: totalOutstanding > 0 ? 'var(--error)' : 'var(--text-1)', margin:'4px 0'}}>
            {formatRp(totalOutstanding)}
          </h3>
          <p style={{fontSize:'12px', color:'var(--text-3)'}}>
            {totalOutstanding > 0 ? 'Terdapat pelunasan tertunda' : 'Semua tagihan aman'}
          </p>
        </div>

        <div className="card" style={{padding:'20px'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase'}}>Total Pengeluaran</span>
          <h3 style={{fontSize:'24px', fontWeight:'800', color:'var(--text-1)', margin:'4px 0'}}>
            {formatRp(totalExpense)}
          </h3>
          <p style={{fontSize:'12px', color:'var(--text-3)'}}>{filteredExpenses.length} transaksi dicatat</p>
        </div>

      </div>

      {/* ── SMART AUTOMATED INSIGHTS ── */}
      {insights.length > 0 && (
        <div style={{marginBottom:'28px'}}>
          <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', marginBottom:'12px', display:'flex', alignItems:'center', gap:'8px'}}>
            <Zap size={18} color="var(--primary)" /> Smart Automated Insights
          </h3>
          <div style={{display:'flex', flexDirection:'column', gap:'12px'}}>
            {insights.map((item, idx) => {
              const Icon = item.icon;
              return (
                <div key={idx} className="card" style={{padding:'18px 20px', display:'flex', alignItems:'center', justifyContent:'space-between', gap:'16px', borderLeft:`4px solid ${item.color}`}}>
                  <div style={{display:'flex', alignItems:'flex-start', gap:'14px'}}>
                    <div style={{width:'38px', height:'38px', borderRadius:'10px', background:item.bg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                      <Icon size={18} color={item.color} />
                    </div>
                    <div>
                      <h4 style={{fontSize:'14.5px', fontWeight:'700', color:'var(--text-1)'}}>{item.title}</h4>
                      <p style={{fontSize:'12.5px', color:'var(--text-3)', marginTop:'2px', lineHeight:'1.4'}}>{item.desc}</p>
                    </div>
                  </div>
                  <button onClick={() => navigate(item.actionRoute)} className="btn btn-secondary btn-sm" style={{flexShrink:0, color:item.color, borderColor:item.color}}>
                    {item.actionText}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* ── CHARTS ── */}
      <div className="card" style={{padding:'24px'}}>
        <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', marginBottom:'20px'}}>Tren Omset & Pengeluaran ({currentYear})</h3>
        <div style={{height:'320px', width:'100%'}}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
              <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fontSize: 12, fill: 'var(--text-3)'}} />
              <YAxis 
                axisLine={false} 
                tickLine={false} 
                tick={{fontSize: 12, fill: 'var(--text-3)'}}
                tickFormatter={(value) => `Rp${(value/1000000).toFixed(0)}M`}
              />
              <Tooltip 
                formatter={(value: number) => [formatRp(value), undefined]}
                contentStyle={{borderRadius: '12px', border: '1px solid var(--border)', boxShadow: 'var(--shadow-md)'}}
              />
              <Legend wrapperStyle={{fontSize: '12px', paddingTop: '10px'}} />
              <Bar dataKey="Omset" fill="var(--primary)" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Pengeluaran" fill="var(--error)" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Profit" fill="var(--success)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

    </div>
  );
};
