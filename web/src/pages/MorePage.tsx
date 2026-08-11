import React, { useState } from 'react';
import { 
  Tag, TrendingUp, MessageSquare, CheckSquare, Zap, FileText, Bell, Settings, LogOut, ChevronRight, AlertTriangle, User, BarChart2, ArrowLeft
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface MorePageProps {
  onNavigateTab: (tab: any) => void;
}

const menuItems = [
  {
    id: 'price-list',
    label: 'Rate Card & Price List',
    desc: 'Kelola paket harga, bagikan ke klien & buat job instan',
    icon: Tag,
    color: 'var(--primary)',
    bg: 'var(--primary-light)',
  },
  {
    id: 'analytics',
    label: 'Analisis Performa Bisnis',
    desc: 'Pantau omset, pengeluaran, dan net profit secara visual',
    icon: BarChart2,
    color: 'var(--success)',
    bg: 'var(--success-light)',
  },
  {
    id: 'followup',
    label: 'Pusat Follow Up Klien',
    desc: 'Konfirmasi agenda dan tagihan sisa pelunasan',
    icon: MessageSquare,
    color: '#2563EB',
    bg: '#EFF6FF',
  },
  {
    id: 'todo',
    label: 'Daftar Tugas & To-Do MC',
    desc: 'Checklist persiapan perform, gladi resik, & karier',
    icon: CheckSquare,
    color: '#7C3AED',
    bg: '#F5F3FF',
  },
  {
    id: 'quick-action',
    label: 'Pintasan Cepat Dasbor',
    desc: 'Kustomisasi tombol melayang & hak akses fitur cepat',
    icon: Zap,
    color: 'var(--warning)',
    bg: 'var(--warning-light)',
  },
  {
    id: 'invoice',
    label: 'Generator Invoice PDF',
    desc: 'Buat dan bagikan invoice profesional ke klien',
    icon: FileText,
    color: '#0369A1',
    bg: '#F0F9FF',
  },
  {
    id: 'notifications',
    label: 'Pusat Pengingat',
    desc: 'Notifikasi otomatis agenda dan pelunasan H-1',
    icon: Bell,
    color: 'var(--error)',
    bg: 'var(--error-light)',
  },
];

export const MorePage: React.FC<MorePageProps> = ({ onNavigateTab }) => {
  const { userProfile, logout } = useAuth();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  return (
    <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'16px', display:'flex', flexDirection:'column', gap:'24px'}}>
      {/* Header */}
      <div>
        <h1 className="page-title">Lainnya & Hub Bisnis MC</h1>
        <p className="page-subtitle">Pusat kendali fitur profesional, Rate Card, Invoice, dan pengaturan akun.</p>
      </div>

      {/* Profile Card */}
      <div
        onClick={() => onNavigateTab('profile')}
        style={{
          padding:'20px',
          borderRadius:'16px',
          background:'linear-gradient(135deg, #4F46E5 0%, #6D28D9 100%)',
          boxShadow:'0 8px 24px rgba(79,70,229,0.2)',
          display:'flex', alignItems:'center', justifyContent:'space-between', gap:'16px',
          cursor:'pointer', transition:'opacity 0.15s',
        }}
        className="hover-opacity"
      >
        <div style={{display:'flex', alignItems:'center', gap:'14px'}}>
          <div style={{width:'48px', height:'48px', borderRadius:'50%', background:'rgba(255,255,255,0.2)', border:'2px solid rgba(255,255,255,0.35)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <span style={{fontSize:'18px', fontWeight:'800', color:'white'}}>
              {userProfile?.displayName?.charAt(0).toUpperCase() || 'M'}
            </span>
          </div>
          <div>
            <h3 style={{fontSize:'16px', fontWeight:'700', color:'white', letterSpacing:'-0.01em'}}>
              {userProfile?.displayName || 'MC Professional'}
            </h3>
            <p style={{fontSize:'12px', color:'rgba(255,255,255,0.65)', marginTop:'2px'}}>
              {userProfile?.city || 'Indonesia'} • Professional MC
            </p>
          </div>
        </div>
        <div style={{display:'flex', alignItems:'center', gap:'6px', background:'rgba(255,255,255,0.15)', padding:'8px 14px', borderRadius:'9999px', flexShrink:0}}>
          <span style={{fontSize:'12px', fontWeight:'600', color:'white'}}>Edit Profil</span>
          <ChevronRight size={14} color="rgba(255,255,255,0.8)" />
        </div>
      </div>

      {/* Hub Menu Card */}
      <div className="card" style={{padding:0, overflow:'hidden'}}>
        {menuItems.map((item, i) => {
          const Icon = item.icon;
          return (
            <div
              key={item.id}
              onClick={() => onNavigateTab(item.id)}
              style={{
                display:'flex', alignItems:'center', gap:'14px', padding:'14px 18px',
                borderBottom: i < menuItems.length - 1 ? '1px solid var(--border)' : 'none',
                cursor:'pointer', transition:'background 0.12s',
              }}
              onMouseEnter={e => (e.currentTarget.style.background = 'var(--bg-surface-2)')}
              onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
            >
              <div style={{width:'38px', height:'38px', borderRadius:'10px', background:item.bg, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                <Icon size={18} color={item.color} />
              </div>
              <div style={{flex:1}}>
                <p style={{fontSize:'13.5px', fontWeight:'600', color:'var(--text-1)'}}>{item.label}</p>
                <p style={{fontSize:'11px', color:'var(--text-3)', marginTop:'1px'}}>{item.desc}</p>
              </div>
              <ChevronRight size={16} color="var(--text-4)" style={{flexShrink:0}} />
            </div>
          );
        })}
      </div>

      {/* Logout Row */}
      <div className="card" style={{padding:0, overflow:'hidden'}}>
        <div
          onClick={() => setShowLogoutModal(true)}
          style={{
            display:'flex', alignItems:'center', gap:'14px', padding:'14px 18px',
            cursor:'pointer', transition:'background 0.12s',
          }}
          onMouseEnter={e => (e.currentTarget.style.background = 'var(--error-light)')}
          onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
        >
          <div style={{width:'38px', height:'38px', borderRadius:'10px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <LogOut size={18} color="var(--error)" />
          </div>
          <div style={{flex:1}}>
            <p style={{fontSize:'13.5px', fontWeight:'600', color:'var(--error)'}}>Keluar Sesi</p>
            <p style={{fontSize:'11px', color:'var(--text-3)', marginTop:'1px'}}>Selesaikan sesi akses mcjob.id Anda</p>
          </div>
          <ChevronRight size={16} color="var(--error)" style={{opacity:0.5, flexShrink:0}} />
        </div>
      </div>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) setShowLogoutModal(false); }}>
          <div className="modal-panel animate-fade-in" style={{textAlign:'center'}}>
            <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'var(--error-light)', display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 16px'}}>
              <AlertTriangle size={22} color="var(--error)" />
            </div>
            <h3 style={{fontSize:'18px', fontWeight:'700', color:'var(--text-1)', marginBottom:'6px', letterSpacing:'-0.015em'}}>Konfirmasi Keluar Sesi</h3>
            <p style={{fontSize:'13px', color:'var(--text-3)', lineHeight:'1.6', marginBottom:'24px', maxWidth:'280px', margin:'0 auto 24px'}}>
              Apakah Anda yakin ingin keluar? Data tersinkronisasi aman di server cloud.
            </p>
            <div style={{display:'flex', gap:'10px'}}>
              <button onClick={() => setShowLogoutModal(false)} className="btn btn-secondary btn-full">
                Batal
              </button>
              <button onClick={logout} className="btn btn-destructive btn-full">
                Ya, Keluar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
