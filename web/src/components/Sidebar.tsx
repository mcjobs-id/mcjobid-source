import React from 'react';
import logoUrl from '../../public/logo.png';
import { Home, Calendar, Users, DollarSign, Grid, Plus, LogOut, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export type TabType = 
  | 'home' | 'agenda' | 'clients' | 'finance' | 'more'
  | 'daymode' | 'price-list' | 'profile' | 'testimonial'
  | 'analytics' | 'followup' | 'todo' | 'notifications' | 'quick-action';

const mainNavItems = [
  { id: 'home',    label: 'Beranda',        icon: Home },
  { id: 'agenda',  label: 'Agenda Acara',   icon: Calendar },
  { id: 'clients', label: 'Klien & WO',     icon: Users },
  { id: 'finance', label: 'Keuangan',       icon: DollarSign },
  { id: 'more',    label: 'Menu Lainnya',   icon: Grid },
];

interface SidebarProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
  onOpenCreateJob: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab, onChangeTab, onOpenCreateJob
}) => {
  const { userProfile, currentUser, logout } = useAuth();
  const userName = userProfile?.stageName?.trim() || userProfile?.displayName?.trim() || userProfile?.name?.trim() || currentUser?.displayName?.trim() || 'Profil MC';
  const userPhoto = userProfile?.photoUrl || userProfile?.photoUri || currentUser?.photoURL;
  const userSubtitle = userProfile?.city?.trim() ? `${userProfile.city} • MC` : (userProfile?.specialization?.trim() || 'MC Professional');
  const initial = userName.charAt(0).toUpperCase();

  return (
    <aside className="sidebar">
      {/* Brand Header */}
      <div style={{padding:'0 16px', height:'56px', display:'flex', alignItems:'center', justifyContent:'space-between', borderBottom:'1px solid var(--border)', flexShrink:0}}>
        <div style={{display:'flex', alignItems:'center', gap:'10px'}}>
          <img src={logoUrl} alt="MCJob.id Logo" style={{width:'32px', height:'32px', objectFit:'contain', borderRadius:'8px'}} />
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
          Buat Job
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
              className={`sidebar-link ${isActive ? 'active' : ''}`}
            >
              <Icon size={17} />
              <span>{label}</span>
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
        <div 
          onClick={() => onChangeTab('profile')}
          style={{display:'flex', alignItems:'center', gap:'10px', padding:'8px 10px', borderRadius:'10px', background:'var(--bg-surface-2)', border:'1px solid var(--border)', cursor:'pointer'}}
          title="Buka Profil MC"
        >
          <div style={{width:'32px', height:'32px', borderRadius:'50%', background:'linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%)', border:'1.5px solid var(--primary)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, overflow:'hidden'}}>
            {userPhoto ? (
              <img
                src={userPhoto}
                alt={userName}
                style={{width:'100%', height:'100%', objectFit:'cover'}}
                referrerPolicy="no-referrer"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
              />
            ) : (
              <span style={{fontSize:'12px', fontWeight:'800', color:'var(--primary)'}}>
                {initial}
              </span>
            )}
          </div>
          <div style={{flex:1, overflow:'hidden'}}>
            <p style={{fontSize:'12px', fontWeight:'700', color:'var(--text-1)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
              {userName}
            </p>
            <p style={{fontSize:'10px', color:'var(--text-4)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
              {userSubtitle}
            </p>
          </div>
          <button
            onClick={(e) => {
              e.stopPropagation();
              logout();
            }}
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
