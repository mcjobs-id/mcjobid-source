import React from 'react';
import logoUrl from '../../public/logo.png';

import { Bell, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface NavbarProps {
  title?: string;
  subtitle?: string;
  showBack?: boolean;
  onBack?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ title = 'mcjob.id', subtitle, showBack, onBack }) => {
  const { userProfile, currentUser } = useAuth();
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate(-1);
    }
  };

  return (
    <header className="top-navbar">
      {/* Left: Page Title */}
      <div style={{flex:1, overflow:'hidden', display:'flex', alignItems:'center', gap:'10px'}}>
        {showBack && (
          <button
            onClick={handleBack}
            className="btn btn-ghost btn-sm"
            style={{padding:'4px 8px', marginLeft:'-4px', gap:'6px', display:'flex', alignItems:'center', flexShrink:0, color:'var(--text-1)'}}
            aria-label="Kembali"
          >
            <ArrowLeft size={18} />
            <span style={{fontSize:'13px', fontWeight:'600'}} className="hidden sm:inline">Kembali</span>
          </button>
        )}
        {/* Mobile brand logo from Android */}
        <div style={{display:'flex', alignItems:'center', gap:'8px', minWidth:0, flex:1}}>
          {!showBack && (
            <img src={logoUrl} alt="MCJob.id Logo" style={{width:'28px', height:'28px', objectFit:'contain', borderRadius:'8px'}} className="block md:hidden" />
          )}
          <div style={{overflow:'hidden', minWidth:0}}>
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
        {(() => {
          const userName = userProfile?.stageName?.trim() || userProfile?.displayName?.trim() || userProfile?.name?.trim() || currentUser?.displayName?.trim() || 'Profil MC';
          const userPhoto = userProfile?.photoUrl || userProfile?.photoUri || currentUser?.photoURL;
          const initial = userName.charAt(0).toUpperCase();

          return (
            <div 
              onClick={() => navigate('/profile')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '4px 12px 4px 5px',
                borderRadius: '9999px',
                background: 'var(--bg-surface-2)',
                border: '1px solid var(--border)',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
              title="Pengaturan Profil MC"
            >
              <div style={{
                width: '26px',
                height: '26px',
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%)',
                border: '1.5px solid var(--primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                overflow: 'hidden'
              }}>
                {userPhoto ? (
                  <img
                    src={userPhoto}
                    alt={userName}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    referrerPolicy="no-referrer"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                  />
                ) : (
                  <span style={{ fontSize: '11px', fontWeight: '800', color: 'var(--primary)' }}>
                    {initial}
                  </span>
                )}
              </div>
              <span style={{
                fontSize: '12px',
                fontWeight: '700',
                color: 'var(--text-1)',
                maxWidth: '120px',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap'
              }}>
                {userName}
              </span>
            </div>
          );
        })()}
      </div>
    </header>
  );
};
