import React from 'react';
import { Home, Calendar, Users, DollarSign, Grid, Plus, LogOut, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export type TabType = 
  | 'home' | 'agenda' | 'clients' | 'finance' | 'more'
  | 'daymode' | 'price_list' | 'profile' | 'testimonial'
  | 'analytics' | 'followup' | 'todo' | 'notifications' | 'quick_action_settings';

const mainNavItems = [
  { id: 'home',    label: 'Beranda',        icon: Home },
  { id: 'agenda',  label: 'Agenda Acara',   icon: Calendar },
  { id: 'clients', label: 'Klien & WO',     icon: Users },
  { id: 'finance', label: 'Keuangan',       icon: DollarSign },
  { id: 'more',    label: 'Lainnya & Hub',  icon: Grid },
];

interface SidebarProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
  onOpenCreateJob: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab, onChangeTab, onOpenCreateJob
}) => {
  const { userProfile, logout } = useAuth();

  return (
    <aside className="sidebar">
      {/* Brand Header */}
      <div style={{padding:'0 16px', height:'56px', display:'flex', alignItems:'center', justifyContent:'space-between', borderBottom:'1px solid var(--border)', flexShrink:0}}>
        <div style={{display:'flex', alignItems:'center', gap:'10px'}}>
          <div style={{width:'30px', height:'30px', borderRadius:'8px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center', boxShadow:'0 2px 8px rgba(79,70,229,0.3)', flexShrink:0}}>
            <span style={{fontSize:'11px', fontWeight:'800', color:'white', letterSpacing:'-0.02em'}}>MC</span>
          </div>
          <div>
            <span style={{fontSize:'14px', fontWeight:'800', color:'var(--primary)', letterSpacing:'-0.02em', display:'block', lineHeight:'1.2'}}>mcjob.id</span>
            <span style={{fontSize:'9px', fontWeight:'600', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.06em'}}>Career MC Academy</span>
          </div>
        </div>
      </div>

      {/* Quick Action */}
      <div style={{padding:'12px 12px 0'}}>
        <button
          onClick={onOpenCreateJob}
          className="btn btn-primary btn-full btn-sm"
          style={{fontSize:'12px', gap:'6px'}}
        >
          <Plus size={14} />
          Tambah Job Baru
        </button>
      </div>

      {/* Navigation */}
      <nav style={{flex:1, padding:'8px 8px', overflowY:'auto'}}>
        <p style={{fontSize:'10px', fontWeight:'700', color:'var(--text-4)', textTransform:'uppercase', letterSpacing:'0.07em', padding:'8px 12px 4px'}}>Navigasi</p>

        {mainNavItems.map(({ id, label, icon: Icon }) => {
          const isActive = activeTab === id;
          return (
            <button
              key={id}
              onClick={() => onChangeTab(id as TabType)}
              className={`nav-item${isActive ? ' active' : ''}`}
              style={{marginBottom:'1px'}}
            >
              <Icon size={16} style={{flexShrink:0, opacity: isActive ? 1 : 0.65}} />
              <span style={{fontSize:'13px'}}>{label}</span>
              {isActive && (
                <div style={{position:'absolute', left:0, top:'50%', transform:'translateY(-50%)', width:'3px', height:'20px', background:'var(--primary)', borderRadius:'0 3px 3px 0'}} />
              )}
            </button>
          );
        })}
      </nav>

      {/* Bottom User Widget */}
      <div style={{padding:'12px', borderTop:'1px solid var(--border)', flexShrink:0}}>
        {/* User profile */}
        <div style={{display:'flex', alignItems:'center', gap:'10px', padding:'8px 10px', borderRadius:'10px', background:'var(--bg-surface-2)', border:'1px solid var(--border)'}}>
          <div style={{width:'30px', height:'30px', borderRadius:'50%', background:'var(--primary-light)', border:'1px solid rgba(79,70,229,0.25)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <span style={{fontSize:'12px', fontWeight:'700', color:'var(--primary)'}}>
              {userProfile?.displayName?.charAt(0).toUpperCase() || 'M'}
            </span>
          </div>
          <div style={{flex:1, overflow:'hidden'}}>
            <p style={{fontSize:'12px', fontWeight:'600', color:'var(--text-1)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
              {userProfile?.displayName || 'MC Professional'}
            </p>
            <p style={{fontSize:'10px', color:'var(--text-4)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
              {userProfile?.city || 'Indonesia'}
            </p>
          </div>
          <button
            onClick={logout}
            style={{color:'var(--text-4)', background:'none', border:'none', cursor:'pointer', padding:'4px', borderRadius:'6px', display:'flex', alignItems:'center'}}
            title="Keluar"
          >
            <LogOut size={13} />
          </button>
        </div>
      </div>
    </aside>
  );
};
