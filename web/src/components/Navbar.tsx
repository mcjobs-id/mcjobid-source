import React from 'react';
import { Bell } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface NavbarProps {
  title?: string;
  subtitle?: string;
}

export const Navbar: React.FC<NavbarProps> = ({ title = 'mcjob.id', subtitle }) => {
  const { userProfile } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="top-navbar">
      {/* Left: Page Title */}
      <div style={{flex:1, overflow:'hidden'}}>
        {/* Mobile brand */}
        <div style={{display:'flex', alignItems:'center', gap:'8px'}}>
          <div className="md-hidden" style={{width:'26px', height:'26px', borderRadius:'7px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center'}}>
            <span style={{fontSize:'9px', fontWeight:'800', color:'white', letterSpacing:'-0.01em'}}>MC</span>
          </div>
          <div>
            <h1 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.015em', lineHeight:'1.2', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
              {title}
            </h1>
            {subtitle && (
              <p style={{fontSize:'11px', color:'var(--text-4)', marginTop:'1px'}}>{subtitle}</p>
            )}
          </div>
        </div>
      </div>

      {/* Right: Actions */}
      <div style={{display:'flex', alignItems:'center', gap:'6px'}}>

        {/* Notification bell */}
        <button
          onClick={() => navigate('/notifications')}
          className="btn btn-ghost btn-sm"
          style={{width:'36px', padding:0, position:'relative', display:'flex', alignItems:'center', justifyContent:'center'}}
          aria-label="Notifikasi"
        >
          <Bell size={16} />
          <span style={{position:'absolute', top:'8px', right:'8px', width:'6px', height:'6px', borderRadius:'50%', background:'var(--error)', border:'1.5px solid var(--bg-surface)'}} />
        </button>

        {/* Divider */}
        <div style={{width:'1px', height:'20px', background:'var(--border)', margin:'0 4px'}} />

        {/* User avatar pill */}
        <div 
          onClick={() => navigate('/profile')}
          style={{display:'flex', alignItems:'center', gap:'8px', padding:'5px 10px 5px 5px', borderRadius:'9999px', background:'var(--bg-surface-2)', border:'1px solid var(--border)', cursor:'pointer'}}
        >
          <div style={{width:'24px', height:'24px', borderRadius:'50%', background:'var(--primary-light)', border:'1px solid rgba(79,70,229,0.25)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
            <span style={{fontSize:'10px', fontWeight:'700', color:'var(--primary)'}}>
              {userProfile?.displayName?.charAt(0).toUpperCase() || 'M'}
            </span>
          </div>
          <span style={{fontSize:'12px', fontWeight:'600', color:'var(--text-2)', maxWidth:'100px', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
            {userProfile?.displayName || 'MC Studio'}
          </span>
        </div>
      </div>
    </header>
  );
};
