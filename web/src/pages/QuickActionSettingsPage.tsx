import React from 'react';
import { ArrowLeft, Zap, ToggleRight, LayoutGrid } from 'lucide-react';

interface QuickActionSettingsPageProps {
  onBack: () => void;
}

export const QuickActionSettingsPage: React.FC<QuickActionSettingsPageProps> = ({ onBack }) => {
  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <Zap size={20} color="var(--warning)" />
              Pintasan Cepat Dasbor
            </h1>
            <p className="page-subtitle">Kustomisasi tampilan tombol aksi cepat di beranda.</p>
          </div>
        </div>
      </div>

      <div className="card" style={{padding:'40px 24px', textAlign:'center'}}>
        <div style={{width:'64px', height:'64px', borderRadius:'16px', background:'var(--warning-light)', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 20px'}}>
          <LayoutGrid size={32} color="var(--warning)" />
        </div>
        <h3 style={{fontSize:'18px', fontWeight:'700', color:'var(--text-1)', marginBottom:'8px'}}>Segera Hadir</h3>
        <p style={{fontSize:'14px', color:'var(--text-3)', maxWidth:'400px', margin:'0 auto', lineHeight:'1.6'}}>
          Fitur untuk mengatur urutan dan memunculkan/menyembunyikan menu pintasan di beranda sedang dalam tahap pengembangan akhir.
        </p>
      </div>

    </div>
  );
};
