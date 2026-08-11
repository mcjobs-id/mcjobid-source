import React, { useState } from 'react';
import { ArrowLeft, User, Phone, MapPin, CheckCircle2, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { updateProfile } from 'firebase/auth';
import { getFirestore, doc, setDoc } from 'firebase/firestore';

interface ProfilePageProps {
  onBack: () => void;
}

export const ProfilePage: React.FC<ProfilePageProps> = ({ onBack }) => {
  const { currentUser, userProfile, updateContextProfile } = useAuth();
  
  const [displayName, setDisplayName] = useState(userProfile?.displayName || '');
  const [phone, setPhone] = useState(userProfile?.phone || '');
  const [city, setCity] = useState(userProfile?.city || '');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{type: 'success'|'error', text: string} | null>(null);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;
    setSaving(true);
    setMessage(null);

    try {
      if (currentUser.displayName !== displayName) {
        await updateProfile(currentUser, { displayName });
      }

      const db = getFirestore();
      const updatedData = {
        displayName,
        phone,
        city,
        updatedAt: new Date().toISOString()
      };
      await setDoc(doc(db, 'users', currentUser.uid), updatedData, { merge: true });
      
      updateContextProfile({ ...userProfile, ...updatedData });
      setMessage({ type: 'success', text: 'Profil berhasil diperbarui!' });
    } catch (error) {
      console.error(error);
      setMessage({ type: 'error', text: 'Gagal memperbarui profil.' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'640px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              Pengaturan Profil
            </h1>
            <p className="page-subtitle">Kelola informasi pribadi dan data kontak MC Anda.</p>
          </div>
        </div>
      </div>

      <div className="card" style={{padding:0, overflow:'hidden', borderTop:'4px solid var(--primary)'}}>
        
        {/* Banner Area */}
        <div style={{padding:'32px', display:'flex', flexDirection:'column', alignItems:'center', background:'var(--bg-surface-2)', borderBottom:'1px solid var(--border)'}}>
          <div style={{width:'80px', height:'80px', borderRadius:'24px', background:'var(--primary-light)', border:'2px solid rgba(79,70,229,0.2)', display:'flex', alignItems:'center', justifyContent:'center', marginBottom:'16px'}}>
            <span style={{fontSize:'32px', fontWeight:'800', color:'var(--primary)'}}>
              {displayName.charAt(0).toUpperCase() || 'M'}
            </span>
          </div>
          <h2 style={{fontSize:'20px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.02em'}}>{displayName || 'MC Professional'}</h2>
          <p style={{fontSize:'13px', color:'var(--text-3)', marginTop:'2px', display:'flex', alignItems:'center', gap:'6px'}}>
            <ShieldCheck size={14} color="var(--success)" /> Akun Terverifikasi
          </p>
        </div>

        {/* Form Area */}
        <div style={{padding:'32px'}}>
          {message && (
            <div style={{padding:'12px 16px', borderRadius:'10px', marginBottom:'24px', display:'flex', alignItems:'center', gap:'10px',
              background: message.type === 'success' ? 'var(--success-light)' : 'var(--error-light)',
              border: `1px solid ${message.type === 'success' ? 'rgba(5,150,105,0.2)' : 'rgba(220,38,38,0.2)'}`,
              color: message.type === 'success' ? 'var(--success-text)' : 'var(--error-text)'
            }}>
              {message.type === 'success' ? <CheckCircle2 size={16} /> : null}
              <p style={{fontSize:'13px', fontWeight:'600'}}>{message.text}</p>
            </div>
          )}

          <form onSubmit={handleSave} style={{display:'flex', flexDirection:'column', gap:'20px'}}>
            <div>
              <label className="input-label">Email Pendaftaran (Read-only)</label>
              <input type="email" value={currentUser?.email || ''} disabled className="input-field" style={{background:'var(--bg-surface-2)', color:'var(--text-3)'}} />
              <p className="input-hint">Email tidak dapat diubah karena terhubung dengan metode login.</p>
            </div>

            <div>
              <label className="input-label">Nama Lengkap / Nama Panggung</label>
              <div className="input-group">
                <User size={15} className="input-icon-left" />
                <input type="text" required value={displayName} onChange={e => setDisplayName(e.target.value)} className="input-field" placeholder="Nama MC" />
              </div>
            </div>

            <div>
              <label className="input-label">Nomor WhatsApp / Telepon</label>
              <div className="input-group">
                <Phone size={15} className="input-icon-left" />
                <input type="tel" value={phone} onChange={e => setPhone(e.target.value)} className="input-field" placeholder="08..." />
              </div>
            </div>

            <div>
              <label className="input-label">Kota Domisili</label>
              <div className="input-group">
                <MapPin size={15} className="input-icon-left" />
                <input type="text" value={city} onChange={e => setCity(e.target.value)} className="input-field" placeholder="Jakarta, Bandung..." />
              </div>
            </div>

            <div style={{paddingTop:'12px', borderTop:'1px solid var(--border)', marginTop:'8px'}}>
              <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg">
                {saving ? 'Menyimpan...' : 'Simpan Perubahan'}
              </button>
            </div>
          </form>
        </div>

      </div>
    </div>
  );
};
