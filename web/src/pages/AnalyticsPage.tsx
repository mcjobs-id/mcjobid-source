import React from 'react';
import { ArrowLeft, BarChart2, TrendingUp, Calendar, DollarSign, Activity } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import type { Booking, Expense } from '../types';

interface AnalyticsPageProps {
  onBack: () => void;
  bookings: Booking[];
  expenses: Expense[];
}

export const AnalyticsPage: React.FC<AnalyticsPageProps> = ({ onBack, bookings, expenses }) => {
  const currentYear = new Date().getFullYear();
  
  // Calculate yearly metrics
  const yearlyBookings = bookings.filter(b => b.status !== 'CANCELLED' && b.eventDate.startsWith(currentYear.toString()));
  const totalHonor = yearlyBookings.reduce((sum, b) => sum + (b.fee || 0), 0);
  
  const yearlyExpenses = expenses.filter(e => e.date.startsWith(currentYear.toString()));
  const totalExpense = yearlyExpenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  
  const netProfit = totalHonor - totalExpense;
  const margin = totalHonor > 0 ? ((netProfit / totalHonor) * 100).toFixed(1) : '0';

  // Format to standard Rp currency without decimals
  const formatRp = (val: number) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);

  // Prepare chart data
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
  const chartData = monthNames.map((m, index) => {
    const monthStr = (index + 1).toString().padStart(2, '0');
    const monthPrefix = `${currentYear}-${monthStr}`;
    
    const monthlyHonor = yearlyBookings
      .filter(b => b.eventDate.startsWith(monthPrefix))
      .reduce((sum, b) => sum + (b.fee || 0), 0);
      
    const monthlyExpense = yearlyExpenses
      .filter(e => e.date.startsWith(monthPrefix))
      .reduce((sum, e) => sum + (e.amount || 0), 0);
      
    return {
      name: m,
      Omset: monthlyHonor,
      Pengeluaran: monthlyExpense,
      Profit: monthlyHonor - monthlyExpense
    };
  });

  return (
    <div className="animate-fade-in" style={{maxWidth:'1000px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <BarChart2 size={20} color="var(--primary)" />
              Analisis Performa Bisnis
            </h1>
            <p className="page-subtitle">Ringkasan kinerja tahunan {currentYear} berdasarkan data transaksi tersimpan.</p>
          </div>
        </div>
      </div>

      {/* ── KPI HIGHLIGHTS ── */}
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(220px, 1fr))', gap:'16px', marginBottom:'24px'}}>
        
        <div className="card" style={{padding:'24px', borderTop:'3px solid var(--primary)'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em', display:'block', marginBottom:'8px'}}>
            Total Omset {currentYear}
          </span>
          <h2 style={{fontSize:'clamp(24px, 3vw, 28px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>
            {formatRp(totalHonor)}
          </h2>
          <div style={{display:'flex', alignItems:'center', gap:'6px', marginTop:'12px'}}>
            <span className="badge badge-primary"><Calendar size={12} /> {yearlyBookings.length} Job Selesai</span>
          </div>
        </div>

        <div className="card" style={{padding:'24px', borderTop:'3px solid var(--success)'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em', display:'block', marginBottom:'8px'}}>
            Net Profit (Margin {margin}%)
          </span>
          <h2 style={{fontSize:'clamp(24px, 3vw, 28px)', fontWeight:'800', color:'var(--success)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>
            {formatRp(netProfit)}
          </h2>
          <div style={{display:'flex', alignItems:'center', gap:'6px', marginTop:'12px'}}>
            <span className="badge badge-success" style={{background:'rgba(5,150,105,0.1)'}}><TrendingUp size={12} /> Laba Bersih</span>
          </div>
        </div>

        <div className="card" style={{padding:'24px', borderTop:'3px solid var(--error)'}}>
          <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em', display:'block', marginBottom:'8px'}}>
            Pengeluaran Operasional
          </span>
          <h2 style={{fontSize:'clamp(24px, 3vw, 28px)', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>
            {formatRp(totalExpense)}
          </h2>
          <div style={{display:'flex', alignItems:'center', gap:'6px', marginTop:'12px'}}>
            <span className="badge badge-error" style={{background:'rgba(220,38,38,0.1)'}}><DollarSign size={12} /> Biaya Keluar</span>
          </div>
        </div>

      </div>

      {/* ── CHARTS ── */}
      <div className="card" style={{padding:'24px'}}>
        <h3 style={{fontSize:'16px', fontWeight:'700', color:'var(--text-1)', marginBottom:'20px'}}>Tren Keuangan {currentYear}</h3>
        <div style={{height:'300px', width:'100%'}}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={chartData}
              margin={{ top: 10, right: 10, left: 20, bottom: 0 }}
            >
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
                contentStyle={{borderRadius: '8px', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)'}}
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
