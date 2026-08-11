import React, { useState } from 'react';
import { Wallet, TrendingUp, TrendingDown, Plus, Trash2, Calendar, FileText } from 'lucide-react';
import type { Booking, Expense } from '../types';
import { Modal } from '../components/Modal';

interface FinancePageProps {
  bookings: Booking[];
  expenses: Expense[];
  onSaveExpense: (exp: Expense) => Promise<void>;
  onDeleteExpense: (id: string) => Promise<void>;
}

export const FinancePage: React.FC<FinancePageProps> = ({
  bookings, expenses, onSaveExpense, onDeleteExpense
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Transport / Bensin');
  const [amount, setAmount] = useState<number | ''>('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [saving, setSaving] = useState(false);

  // Financial calculations
  let totalHonor = 0;
  let totalReceived = 0;
  bookings.forEach((b) => {
    if (b.status !== 'CANCELLED') {
      totalHonor += b.totalFee || 0;
      totalReceived += b.dpAmount || 0;
      if (b.paymentStatus === 'PAID') {
        // If PAID, ensure the full amount is counted as received
        totalReceived += (b.totalFee || 0) - (b.dpAmount || 0);
      }
    }
  });

  let totalExpense = 0;
  expenses.forEach((e) => {
    totalExpense += e.amount || 0;
  });

  const netIncome = totalReceived - totalExpense;

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  const handleAddExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!amount) return;
    setSaving(true);
    try {
      const exp: Expense = {
        id: Date.now().toString(),
        ownerId: '',
        title,
        category,
        amount: Number(amount),
        date,
        createdAt: new Date().toISOString()
      };
      await onSaveExpense(exp);
      setIsModalOpen(false);
      setTitle('');
      setAmount('');
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'1280px', margin:'0 auto', paddingBottom:'16px'}}>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
            <Wallet size={20} color="var(--primary)" />
            Laporan Keuangan MC
          </h1>
          <p className="page-subtitle">Ringkasan pemasukan honorarium dan pengeluaran operasional.</p>
        </div>
        <button onClick={() => setIsModalOpen(true)} className="btn btn-primary">
          <Plus size={15} /> Catat Pengeluaran
        </button>
      </div>

      {/* Main Financial Balance Cards */}
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(240px, 1fr))', gap:'16px', marginBottom:'24px'}}>
        
        {/* Net Income Hero Card */}
        <div className="card" style={{gridColumn:'1 / -1', padding:'24px', background:'var(--text-1)', border:'none', position:'relative', overflow:'hidden'}}>
          {/* Subtle gradient glow */}
          <div style={{position:'absolute', top:'-50%', right:'-10%', width:'300px', height:'300px', background:'radial-gradient(circle, rgba(79,70,229,0.3) 0%, transparent 70%)', borderRadius:'50%', pointerEvents:'none'}} />
          
          <div style={{position:'relative', zIndex:1, display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:'20px', flexWrap:'wrap'}}>
            <div>
              <span style={{fontSize:'12px', fontWeight:'700', color:'rgba(255,255,255,0.6)', textTransform:'uppercase', letterSpacing:'0.08em', display:'block', marginBottom:'6px'}}>
                PROFIT BERSIH (NET INCOME)
              </span>
              <h2 style={{fontSize:'clamp(28px,4vw,36px)', fontWeight:'800', color:'white', letterSpacing:'-0.03em', fontVariantNumeric:'tabular-nums', lineHeight:'1', marginBottom:'8px'}}>
                {formatCurrency(netIncome)}
              </h2>
              <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
                <span className="badge badge-success" style={{background:'rgba(5,150,105,0.2)', color:'var(--success-text)', border:'1px solid rgba(5,150,105,0.3)'}}>
                  <TrendingUp size={12} /> +12% vs bulan lalu
                </span>
                <span style={{fontSize:'11px', color:'rgba(255,255,255,0.5)'}}>Dari total {bookings.length} job</span>
              </div>
            </div>
            
            <div style={{width:'56px', height:'56px', borderRadius:'16px', background:'rgba(255,255,255,0.1)', display:'flex', alignItems:'center', justifyContent:'center', border:'1px solid rgba(255,255,255,0.15)'}}>
              <Wallet size={24} color="white" />
            </div>
          </div>
        </div>

        {/* Total Income */}
        <div className="card" style={{padding:'20px', borderTop:'3px solid var(--success)'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
            <span style={{fontSize:'12px', fontWeight:'600', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em'}}>Total Pemasukan</span>
            <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--success-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <TrendingUp size={16} color="var(--success)" />
            </div>
          </div>
          <h3 style={{fontSize:'24px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>
            {formatCurrency(totalReceived)}
          </h3>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'4px'}}>Total honorarium tercatat: {formatCurrency(totalHonor)}</p>
        </div>

        {/* Total Expense */}
        <div className="card" style={{padding:'20px', borderTop:'3px solid var(--error)'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'12px'}}>
            <span style={{fontSize:'12px', fontWeight:'600', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em'}}>Pengeluaran Operasional</span>
            <div style={{width:'32px', height:'32px', borderRadius:'8px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
              <TrendingDown size={16} color="var(--error)" />
            </div>
          </div>
          <h3 style={{fontSize:'24px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.02em', fontVariantNumeric:'tabular-nums'}}>
            {formatCurrency(totalExpense)}
          </h3>
          <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'4px'}}>Transport, Kostum, & Operasional</p>
        </div>
      </div>

      {/* Expense History List */}
      <div>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'16px'}}>
          <h3 style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', display:'flex', alignItems:'center', gap:'8px'}}>
            <FileText size={16} color="var(--primary)" />
            Riwayat Pengeluaran
          </h3>
          <span style={{fontSize:'12px', fontWeight:'600', color:'var(--text-3)'}}>{expenses.length} Transaksi</span>
        </div>

        {expenses.length === 0 ? (
          <div className="card" style={{padding:0}}>
            <div className="empty-state">
              <div className="empty-state-icon"><TrendingDown size={22} /></div>
              <div>
                <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Belum ada riwayat pengeluaran</p>
                <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto 16px'}}>Catat pengeluaran pertama Anda seperti bensin, tol, atau kostum.</p>
              </div>
              <button onClick={() => setIsModalOpen(true)} className="btn btn-secondary btn-sm">
                <Plus size={14} /> Catat Pengeluaran
              </button>
            </div>
          </div>
        ) : (
          <div className="card" style={{padding:0, overflow:'hidden'}}>
            {/* Table Header (Desktop only visually) */}
            <table className="data-table">
              <thead className="hidden md:table-header-group">
                <tr>
                  <th style={{width:'40%'}}>Keterangan</th>
                  <th style={{width:'20%'}}>Kategori</th>
                  <th style={{width:'20%'}}>Tanggal</th>
                  <th style={{width:'20%', textAlign:'right'}}>Nominal</th>
                </tr>
              </thead>
              <tbody style={{display:'block', width:'100%'}} className="md:table-row-group">
                {expenses.map((exp) => (
                  <tr key={exp.id} style={{display:'flex', flexDirection:'column', padding:'12px 16px', borderBottom:'1px solid var(--border)'}} className="md:table-row md:p-0">
                    {/* Mobile View / Table Row wrapper */}
                    <td style={{display:'flex', alignItems:'center', gap:'12px', padding:'0', borderBottom:'none'}} className="md:table-cell md:p-4 md:border-b">
                      <div className="md:hidden" style={{width:'36px', height:'36px', borderRadius:'10px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                        <TrendingDown size={16} color="var(--error)" />
                      </div>
                      <div style={{flex:1}}>
                        <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)'}}>{exp.title}</p>
                        <p className="md:hidden" style={{fontSize:'12px', color:'var(--text-3)', marginTop:'2px'}}>{exp.category} • {exp.date}</p>
                      </div>
                    </td>
                    
                    <td className="hidden md:table-cell">
                      <span className="badge badge-muted">{exp.category}</span>
                    </td>
                    <td className="hidden md:table-cell">
                      <span style={{display:'flex', alignItems:'center', gap:'6px', color:'var(--text-3)'}}><Calendar size={13}/>{exp.date}</span>
                    </td>
                    
                    <td style={{display:'flex', alignItems:'center', justifyContent:'flex-end', gap:'12px', padding:'0', marginTop:'8px'}} className="md:table-cell md:mt-0 md:text-right md:p-4">
                      <span style={{fontSize:'14px', fontWeight:'700', color:'var(--text-1)', fontVariantNumeric:'tabular-nums'}}>
                        - {formatCurrency(exp.amount)}
                      </span>
                      <button
                        onClick={() => onDeleteExpense(exp.id)}
                        className="btn btn-ghost btn-sm"
                        style={{width:'32px', height:'32px', padding:0, color:'var(--text-4)'}}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ADD EXPENSE MODAL */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Catat Pengeluaran Operasional">
        <form onSubmit={handleAddExpense} style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          <div>
            <label className="input-label">Keterangan / Judul *</label>
            <input type="text" required value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Contoh: Bensin Tol Jkt-Bdg / Laundry Jas" className="input-field" />
          </div>

          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
            <div>
              <label className="input-label">Kategori</label>
              <select value={category} onChange={(e) => setCategory(e.target.value)} className="input-field">
                <option value="Transport / Bensin">Transport / Bensin</option>
                <option value="Kostum / Jas">Kostum / Wardrobe</option>
                <option value="Makeup / Grooming">Makeup / Grooming</option>
                <option value="Konsumsi / Makan">Konsumsi / Makan</option>
                <option value="Equipment / Mic">Equipment / Sound</option>
                <option value="Lainnya">Lainnya</option>
              </select>
            </div>
            <div>
              <label className="input-label">Nominal (Rp) *</label>
              <input type="number" required value={amount} onChange={(e) => setAmount(Number(e.target.value))} placeholder="0" className="input-field" style={{fontWeight:'700', color:'var(--error)'}} />
            </div>
          </div>

          <div>
            <label className="input-label">Tanggal</label>
            <input type="date" required value={date} onChange={(e) => setDate(e.target.value)} className="input-field" />
          </div>

          <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{marginTop:'8px'}}>
            {saving ? 'Menyimpan...' : 'Simpan Pengeluaran'}
          </button>
        </form>
      </Modal>

      <style>{`
        @media (min-width: 768px) {
          .md\\:table-header-group { display: table-header-group !important; }
          .md\\:table-row-group { display: table-row-group !important; }
          .md\\:table-row { display: table-row !important; }
          .md\\:table-cell { display: table-cell !important; }
          .md\\:hidden { display: none !important; }
          .md\\:mt-0 { margin-top: 0 !important; }
          .md\\:p-0 { padding: 0 !important; }
          .md\\:p-4 { padding: 16px !important; }
          .md\\:border-b { border-bottom: 1px solid var(--border) !important; }
          .md\\:text-right { text-align: right !important; }
        }
      `}</style>
    </div>
  );
};
