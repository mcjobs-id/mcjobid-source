import React, { useState, useEffect } from 'react';
import { ArrowLeft, Zap, CheckCircle2, Shield, Calendar, UserPlus, DollarSign, FileText, Tag, BarChart2, MessageSquare, CheckSquare, Bell, User, Settings } from 'lucide-react';

interface QuickActionSettingsPageProps {
  onBack: () => void;
}

export const QA_CONFIG_KEY = 'mcjobid_qa_config';

export interface QuickActionConfig {
  masterEnabled: boolean;
  addJob: boolean;
  addClient: boolean;
  addPayment: boolean;
  addExpense: boolean;
  invoice: boolean;
  rateCard: boolean;
  analytics: boolean;
  followUp: boolean;
  todo: boolean;
  notifications: boolean;
  profile: boolean;
}

export const DEFAULT_QA_CONFIG: QuickActionConfig = {
  masterEnabled: true,
  addJob: true,
  addClient: true,
  addPayment: true,
  addExpense: true,
  invoice: true,
  rateCard: true,
  analytics: true,
  followUp: true,
  todo: true,
  notifications: true,
  profile: true,
};

export function getStoredQaConfig(): QuickActionConfig {
  try {
    const raw = localStorage.getItem(QA_CONFIG_KEY);
    if (raw) return { ...DEFAULT_QA_CONFIG, ...JSON.parse(raw) };
  } catch (e) {
    console.error(e);
  }
  return DEFAULT_QA_CONFIG;
}

export const QuickActionSettingsPage: React.FC<QuickActionSettingsPageProps> = ({ onBack }) => {
  const [config, setConfig] = useState<QuickActionConfig>(getStoredQaConfig);
  const [savedMessage, setSavedMessage] = useState(false);

  const toggleMaster = () => {
    const next = { ...config, masterEnabled: !config.masterEnabled };
    setConfig(next);
    localStorage.setItem(QA_CONFIG_KEY, JSON.stringify(next));
    showSaved();
  };

  const toggleItem = (key: keyof Omit<QuickActionConfig, 'masterEnabled'>) => {
    const next = { ...config, [key]: !config[key] };
    setConfig(next);
    localStorage.setItem(QA_CONFIG_KEY, JSON.stringify(next));
    showSaved();
  };

  const selectAll = (val: boolean) => {
    const next: QuickActionConfig = {
      masterEnabled: config.masterEnabled,
      addJob: val,
      addClient: val,
      addPayment: val,
      addExpense: val,
      invoice: val,
      rateCard: val,
      analytics: val,
      followUp: val,
      todo: val,
      notifications: val,
      profile: val,
    };
    setConfig(next);
    localStorage.setItem(QA_CONFIG_KEY, JSON.stringify(next));
    showSaved();
  };

  const showSaved = () => {
    setSavedMessage(true);
    setTimeout(() => setSavedMessage(false), 1500);
  };

  const items = [
    { key: 'addJob', label: 'Catat Job Baru', icon: Calendar, color: 'var(--primary)' },
    { key: 'addClient', label: 'Tambah Klien / WO Baru', icon: UserPlus, color: '#2563EB' },
    { key: 'addPayment', label: 'Catat Pelunasan / DP', icon: DollarSign, color: 'var(--success)' },
    { key: 'addExpense', label: 'Catat Pengeluaran Operasional', icon: DollarSign, color: 'var(--error)' },
    { key: 'invoice', label: 'Generator Invoice PDF', icon: FileText, color: '#0369A1' },
    { key: 'rateCard', label: 'Simulasi Rate Card & Paket', icon: Tag, color: 'var(--primary)' },
    { key: 'analytics', label: 'Analisis Omset & Statistik', icon: BarChart2, color: 'var(--success)' },
    { key: 'followUp', label: 'Pusat Follow Up Klien', icon: MessageSquare, color: '#2563EB' },
    { key: 'todo', label: 'Daftar Tugas & To-Do MC', icon: CheckSquare, color: '#7C3AED' },
    { key: 'notifications', label: 'Pusat Pengingat & Notifikasi', icon: Bell, color: 'var(--error)' },
    { key: 'profile', label: 'Edit Profil MC', icon: User, color: 'var(--primary)' },
  ] as const;

  const enabledCount = items.filter(i => config[i.key]).length;

  return (
    <div className="animate-fade-in" style={{maxWidth:'850px', margin:'0 auto', paddingBottom:'32px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <Zap size={20} color="var(--warning)" /> Pintasan Cepat Dasbor
            </h1>
            <p className="page-subtitle">Kustomisasi tombol melayang (FAB) & hak akses fitur cepat di Beranda.</p>
          </div>
        </div>

        {savedMessage && (
          <span className="badge badge-success animate-fade-in" style={{fontSize:'12px', padding:'6px 12px'}}>
            <CheckCircle2 size={13} /> Konfigurasi Tersimpan!
          </span>
        )}
      </div>

      {/* ── MASTER TOGGLE HERO ── */}
      <div className="card" style={{padding:'24px', marginBottom:'24px', background:'var(--primary-light)', border:'1px solid rgba(79,70,229,0.2)'}}>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', flexWrap:'wrap', gap:'16px'}}>
          <div style={{display:'flex', alignItems:'center', gap:'14px'}}>
            <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center', color:'white', flexShrink:0}}>
              <Zap size={24} />
            </div>
            <div>
              <h3 style={{fontSize:'17px', fontWeight:'800', color:'var(--primary-text)'}}>Aktifkan Tombol Pintasan Melayang (FAB)</h3>
              <p style={{fontSize:'12.5px', color:'var(--text-2)', marginTop:'2px'}}>
                Menampilkan tombol melayang `+` di pojok kanan bawah Beranda yang bisa mengembang memunculkan menu pintasan.
              </p>
            </div>
          </div>

          <label style={{position:'relative', display:'inline-block', width:'52px', height:'28px', cursor:'pointer', flexShrink:0}}>
            <input 
              type="checkbox" 
              checked={config.masterEnabled} 
              onChange={toggleMaster}
              style={{opacity:0, width:0, height:0}} 
            />
            <span style={{
              position:'absolute', inset:0, background: config.masterEnabled ? 'var(--primary)' : 'var(--border-strong)',
              borderRadius:'28px', transition:'0.2s', display:'block'
            }}>
              <span style={{
                position:'absolute', height:'22px', width:'22px', left:'3px', bottom:'3px',
                background:'white', borderRadius:'50%', transition:'0.2s',
                transform: config.masterEnabled ? 'translateX(24px)' : 'none'
              }} />
            </span>
          </label>
        </div>
      </div>

      {/* ── SELECTION CONTROLS ── */}
      <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'16px', flexWrap:'wrap', gap:'12px'}}>
        <div>
          <h3 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)'}}>Hak Akses Item Pintasan</h3>
          <p style={{fontSize:'12px', color:'var(--text-3)'}}>{enabledCount} dari {items.length} fitur terpilih</p>
        </div>

        <div style={{display:'flex', gap:'8px'}}>
          <button onClick={() => selectAll(true)} className="btn btn-secondary btn-sm">Pilih Semua</button>
          <button onClick={() => selectAll(false)} className="btn btn-secondary btn-sm">Hapus Semua</button>
        </div>
      </div>

      {/* ── TOGGLE GRID ── */}
      <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(260px, 1fr))', gap:'14px'}}>
        {items.map(item => {
          const Icon = item.icon;
          const isChecked = config[item.key];
          return (
            <div 
              key={item.key} 
              className="card hover-scale"
              onClick={() => toggleItem(item.key)}
              style={{
                padding:'16px 18px', 
                display:'flex', 
                alignItems:'center', 
                justifyContent:'space-between', 
                gap:'12px',
                cursor:'pointer',
                border: isChecked ? '1px solid var(--primary)' : '1px solid var(--border)',
                background: isChecked ? 'var(--bg-surface)' : 'var(--bg-surface-2)',
                opacity: config.masterEnabled ? 1 : 0.5
              }}
            >
              <div style={{display:'flex', alignItems:'center', gap:'12px'}}>
                <div style={{width:'34px', height:'34px', borderRadius:'10px', background:'rgba(79,70,229,0.08)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                  <Icon size={16} color={item.color} />
                </div>
                <span style={{fontSize:'13.5px', fontWeight:'600', color:'var(--text-1)'}}>{item.label}</span>
              </div>

              <input 
                type="checkbox"
                checked={isChecked}
                onChange={() => {}} // handled by div onClick
                disabled={!config.masterEnabled}
                style={{width:'18px', height:'18px', accentColor:'var(--primary)', cursor:'pointer'}}
              />
            </div>
          );
        })}
      </div>

    </div>
  );
};
