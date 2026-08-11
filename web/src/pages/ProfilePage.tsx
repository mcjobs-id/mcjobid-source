import React, { useState } from 'react';
import {
  ArrowLeft, User, Phone, MapPin, CheckCircle2, ShieldCheck,
  Building, CreditCard, AtSign, Percent, FileText, Hash,
  Star, Globe, AlertCircle, Save, Upload
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { updateProfile } from 'firebase/auth';
import { saveUserProfile } from '../services/firebaseService';
import type { UserProfile } from '../types';

interface ProfilePageProps {
  onBack: () => void;
}

type SectionKey = 'identity' | 'contact' | 'professional' | 'banking' | 'business';

export const ProfilePage: React.FC<ProfilePageProps> = ({ onBack }) => {
  const { currentUser, userProfile, updateContextProfile } = useAuth();

  const [form, setForm] = useState<Partial<UserProfile>>(() => ({
    displayName: userProfile?.displayName || userProfile?.name || currentUser?.displayName || '',
    stageName: userProfile?.stageName || '',
    bio: userProfile?.bio || '',
    city: userProfile?.city || '',
    areaCoverage: userProfile?.areaCoverage || '',
    specialization: userProfile?.specialization || '',
    languages: userProfile?.languages || '',
    experienceYears: userProfile?.experienceYears || '',
    phone: userProfile?.phone || userProfile?.phoneNumber || '',
    secondaryPhone: userProfile?.secondaryPhone || '',
    instagram: userProfile?.instagram || userProfile?.instagramHandle || '',
    tiktok: userProfile?.tiktok || '',
    bankName: userProfile?.bankName || '',
    bankAccountNumber: userProfile?.bankAccountNumber || userProfile?.bankAccount || '',
    bankAccountHolder: userProfile?.bankAccountHolder || userProfile?.bankHolder || '',
    secondaryBankInfo: userProfile?.secondaryBankInfo || '',
    baseFee: userProfile?.baseFee || 0,
    defaultDpPercentage: userProfile?.defaultDpPercentage ?? 30,
    npwpNumber: userProfile?.npwpNumber || '',
    termsAndConditions: userProfile?.termsAndConditions || '',
    photoUrl: userProfile?.photoUrl || userProfile?.photoUri || currentUser?.photoURL || '',
  }));

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [activeSection, setActiveSection] = useState<SectionKey>('identity');

  // Reactively sync form fields when userProfile is loaded or updated asynchronously
  React.useEffect(() => {
    if (userProfile) {
      setForm({
        displayName: userProfile.displayName || userProfile.name || currentUser?.displayName || '',
        stageName: userProfile.stageName || '',
        bio: userProfile.bio || '',
        city: userProfile.city || '',
        areaCoverage: userProfile.areaCoverage || '',
        specialization: userProfile.specialization || '',
        languages: userProfile.languages || '',
        experienceYears: userProfile.experienceYears || '',
        phone: userProfile.phone || userProfile.phoneNumber || '',
        secondaryPhone: userProfile.secondaryPhone || '',
        instagram: userProfile.instagram || userProfile.instagramHandle || '',
        tiktok: userProfile.tiktok || '',
        bankName: userProfile.bankName || '',
        bankAccountNumber: userProfile.bankAccountNumber || userProfile.bankAccount || '',
        bankAccountHolder: userProfile.bankAccountHolder || userProfile.bankHolder || '',
        secondaryBankInfo: userProfile.secondaryBankInfo || '',
        baseFee: userProfile.baseFee || 0,
        defaultDpPercentage: userProfile.defaultDpPercentage ?? 30,
        npwpNumber: userProfile.npwpNumber || '',
        termsAndConditions: userProfile.termsAndConditions || '',
        photoUrl: userProfile.photoUrl || userProfile.photoUri || currentUser?.photoURL || '',
      });
    }
  }, [userProfile, currentUser]);

  const set = (key: keyof UserProfile, value: any) => {
    setForm(prev => ({ ...prev, [key]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;
    if (!form.displayName?.trim()) {
      setMessage({ type: 'error', text: 'Nama Lengkap / Nama Panggung wajib diisi.' });
      return;
    }

    setSaving(true);
    setMessage(null);
    try {
      if (currentUser.displayName !== form.displayName) {
        await updateProfile(currentUser, { displayName: form.displayName });
      }

      const cleanDpPct = Math.min(100, Math.max(0, Number(form.defaultDpPercentage) || 30));
      const cleanBaseFee = Math.max(0, Number(form.baseFee) || 0);

      const updatedProfile: UserProfile = {
        uid: currentUser.uid,
        email: currentUser.email || '',
        ...form,
        displayName: form.displayName.trim(),
        name: form.displayName.trim(),
        phone: form.phone?.trim() || '',
        phoneNumber: form.phone?.trim() || '',
        bankAccountNumber: form.bankAccountNumber?.trim() || '',
        bankAccount: form.bankAccountNumber?.trim() || '',
        bankAccountHolder: form.bankAccountHolder?.trim() || '',
        bankHolder: form.bankAccountHolder?.trim() || '',
        instagram: form.instagram?.trim() || '',
        instagramHandle: form.instagram?.trim() || '',
        photoUrl: form.photoUrl?.trim() || '',
        photoUri: form.photoUrl?.trim() || '',
        baseFee: cleanBaseFee,
        defaultDpPercentage: cleanDpPct,
        profileCompleted: Boolean(form.displayName?.trim() && (form.phone?.trim() || form.city?.trim() || userProfile?.profileCompleted)),
        updatedAt: new Date().toISOString()
      };

      await saveUserProfile(updatedProfile);
      updateContextProfile(updatedProfile);
      setMessage({ type: 'success', text: 'Profil MC berhasil disimpan & tersinkronisasi!' });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
      console.error(error);
      setMessage({ type: 'error', text: 'Gagal memperbarui profil. Periksa koneksi Anda dan coba lagi.' });
    } finally {
      setSaving(false);
    }
  };

  const sections: { key: SectionKey; label: string; icon: React.ReactNode }[] = [
    { key: 'identity', label: 'Identitas MC', icon: <User size={14} /> },
    { key: 'contact', label: 'Kontak & Sosmed', icon: <Phone size={14} /> },
    { key: 'professional', label: 'Profil Profesional', icon: <Star size={14} /> },
    { key: 'banking', label: 'Rekening Bank', icon: <CreditCard size={14} /> },
    { key: 'business', label: 'Pengaturan Bisnis', icon: <Building size={14} /> },
  ];

  const completedCount = [
    form.displayName, form.phone, form.city,
    form.bankAccountNumber, form.specialization, form.bio
  ].filter(Boolean).length;
  const completionPct = Math.round((completedCount / 6) * 100);

  return (
    <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '40px' }}>

      {/* Profile Banner */}
      <div className="card" style={{ padding: '24px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '20px', flexWrap: 'wrap' }}>
        <div style={{ width: '72px', height: '72px', borderRadius: '20px', background: 'var(--primary-light)', border: '2px solid rgba(79,70,229,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, overflow: 'hidden' }}>
          {form.photoUrl
            ? <img src={form.photoUrl as string} alt="avatar" style={{ width: '100%', height: '100%', borderRadius: '18px', objectFit: 'cover' }} referrerPolicy="no-referrer" onError={(e) => { (e.target as HTMLImageElement).style.display='none'; }} />
            : <span style={{ fontSize: '28px', fontWeight: '800', color: 'var(--primary)' }}>{(form.displayName || currentUser?.displayName || 'M').charAt(0).toUpperCase()}</span>
          }
        </div>
        <div style={{ flex: 1 }}>
          <h2 style={{ fontSize: '18px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '4px' }}>{form.displayName || 'MC Professional'}</h2>
          <p style={{ fontSize: '12px', color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: '5px', marginBottom: '10px' }}>
            <ShieldCheck size={13} color="var(--success)" /> {currentUser?.email} · Akun Terverifikasi
          </p>
          {/* Profile Completion Bar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ flex: 1, height: '5px', background: 'var(--border)', borderRadius: '3px', overflow: 'hidden' }}>
              <div style={{ height: '100%', background: completionPct === 100 ? 'var(--success)' : 'var(--primary)', width: `${completionPct}%`, transition: 'width 0.5s ease', borderRadius: '3px' }} />
            </div>
            <span style={{ fontSize: '11px', fontWeight: '700', color: completionPct === 100 ? 'var(--success)' : 'var(--primary)', flexShrink: 0 }}>
              {completionPct}% Lengkap
            </span>
          </div>
        </div>
      </div>

      {/* Message */}
      {message && (
        <div style={{
          padding: '12px 16px', borderRadius: '10px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px',
          background: message.type === 'success' ? 'var(--success-light)' : 'var(--error-light)',
          border: `1px solid ${message.type === 'success' ? 'rgba(5,150,105,0.2)' : 'rgba(220,38,38,0.2)'}`,
          color: message.type === 'success' ? 'var(--success-text)' : 'var(--error-text)'
        }}>
          {message.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
          <p style={{ fontSize: '13px', fontWeight: '600' }}>{message.text}</p>
        </div>
      )}

      {/* Section Tabs */}
      <div style={{ display: 'flex', gap: '6px', overflowX: 'auto', marginBottom: '20px', paddingBottom: '4px' }} className="scrollbar-none">
        {sections.map(s => (
          <button
            key={s.key}
            onClick={() => setActiveSection(s.key)}
            style={{
              flexShrink: 0, display: 'flex', alignItems: 'center', gap: '6px',
              padding: '8px 14px', borderRadius: '10px', border: 'none', cursor: 'pointer',
              fontSize: '12px', fontWeight: '600', transition: 'all 0.15s',
              background: activeSection === s.key ? 'var(--primary)' : 'var(--bg-surface)',
              color: activeSection === s.key ? 'white' : 'var(--text-3)',
              border: `1px solid ${activeSection === s.key ? 'var(--primary)' : 'var(--border)'}`
            }}
          >
            {s.icon} {s.label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSave}>
        <div className="card" style={{ padding: '24px 28px' }}>

          {/* IDENTITY */}
          {activeSection === 'identity' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <SectionTitle icon={<User size={15} />} title="Identitas MC" subtitle="Nama dan informasi dasar yang tampil di invoice dan profil publik." />

              {/* Photo Input (File Upload + URL) */}
              <div>
                <label className="input-label">Foto Profil MC</label>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap', marginBottom: '8px' }}>
                  <label
                    className="btn btn-secondary btn-sm"
                    style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: '700' }}
                  >
                    <Upload size={14} /> Pilih Foto dari Perangkat
                    <input
                      type="file"
                      accept="image/*"
                      style={{ display: 'none' }}
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        if (file) {
                          if (file.size > 2 * 1024 * 1024) {
                            setMessage({ type: 'error', text: 'Ukuran foto maksimal 2MB agar loading cepat.' });
                            return;
                          }
                          const reader = new FileReader();
                          reader.onload = (event) => {
                            if (event.target?.result) {
                              set('photoUrl', event.target.result as string);
                            }
                          };
                          reader.readAsDataURL(file);
                        }
                      }}
                    />
                  </label>
                  {form.photoUrl && (
                    <button
                      type="button"
                      onClick={() => set('photoUrl', '')}
                      className="btn btn-ghost btn-sm"
                      style={{ color: 'var(--error)', fontSize: '12px' }}
                    >
                      Hapus Foto
                    </button>
                  )}
                </div>
                <input
                  type="url"
                  value={typeof form.photoUrl === 'string' && !form.photoUrl.startsWith('data:') ? form.photoUrl : ''}
                  onChange={e => set('photoUrl', e.target.value)}
                  className="input-field"
                  placeholder="Atau masukkan URL foto (https://...)"
                />
                <p className="input-hint">Foto profil Anda akan terintegrasi langsung di Navbar, Hub Bisnis, dan Invoice.</p>
              </div>

              <div>
                <label className="input-label">Email (Read-only)</label>
                <input type="email" value={currentUser?.email || ''} disabled className="input-field" style={{ background: 'var(--bg-surface-2)', color: 'var(--text-4)' }} />
                <p className="input-hint">Email adalah identitas akun utama.</p>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Nama Lengkap *</label>
                  <div className="input-group"><User size={14} className="input-icon-left" />
                    <input type="text" required value={form.displayName || ''} onChange={e => set('displayName', e.target.value)} className="input-field" placeholder="Nama MC Anda" />
                  </div>
                </div>
                <div>
                  <label className="input-label">Nama Panggung</label>
                  <input type="text" value={form.stageName || ''} onChange={e => set('stageName', e.target.value)} className="input-field" placeholder="Nama panggung jika ada" />
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Kota Domisili</label>
                  <div className="input-group"><MapPin size={14} className="input-icon-left" />
                    <input type="text" value={form.city || ''} onChange={e => set('city', e.target.value)} className="input-field" placeholder="Jakarta, Bandung..." />
                  </div>
                </div>
                <div>
                  <label className="input-label">Area Coverage</label>
                  <input type="text" value={form.areaCoverage || ''} onChange={e => set('areaCoverage', e.target.value)} className="input-field" placeholder="Jabodetabek, Jawa Barat..." />
                </div>
              </div>
              <div>
                <label className="input-label">Bio / Deskripsi Singkat</label>
                <textarea value={form.bio || ''} onChange={e => set('bio', e.target.value)} className="input-field" style={{ height: '90px', padding: '10px 12px', resize: 'none' }} placeholder="Ceritakan tentang Anda, spesialisasi, dan pengalaman singkat..." />
              </div>
            </div>
          )}

          {/* CONTACT */}
          {activeSection === 'contact' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <SectionTitle icon={<Phone size={15} />} title="Kontak & Media Sosial" subtitle="Nomor HP untuk WA dan akun sosial media profesional Anda." />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">No. WhatsApp / HP Utama</label>
                  <div className="input-group"><Phone size={14} className="input-icon-left" />
                    <input type="tel" value={form.phone || ''} onChange={e => set('phone', e.target.value)} className="input-field" placeholder="08xxxxxxxxxx" />
                  </div>
                </div>
                <div>
                  <label className="input-label">No. HP Kedua (Opsional)</label>
                  <div className="input-group"><Phone size={14} className="input-icon-left" />
                    <input type="tel" value={form.secondaryPhone || ''} onChange={e => set('secondaryPhone', e.target.value)} className="input-field" placeholder="08xxxxxxxxxx" />
                  </div>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Instagram</label>
                  <div className="input-group">
                    <AtSign size={14} className="input-icon-left" />
                    <input type="text" value={form.instagram || ''} onChange={e => set('instagram', e.target.value)} className="input-field" placeholder="@username" />
                  </div>
                </div>
                <div>
                  <label className="input-label">TikTok</label>
                  <div className="input-group">
                    <Globe size={14} className="input-icon-left" />
                    <input type="text" value={form.tiktok || ''} onChange={e => set('tiktok', e.target.value)} className="input-field" placeholder="@username" />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* PROFESSIONAL */}
          {activeSection === 'professional' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <SectionTitle icon={<Star size={15} />} title="Profil Profesional" subtitle="Informasi spesialisasi dan pengalaman untuk portfolio dan invoice." />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Spesialisasi</label>
                  <input type="text" value={form.specialization || ''} onChange={e => set('specialization', e.target.value)} className="input-field" placeholder="Wedding, Corporate, Concert..." />
                </div>
                <div>
                  <label className="input-label">Tahun Pengalaman</label>
                  <input type="text" value={form.experienceYears || ''} onChange={e => set('experienceYears', e.target.value)} className="input-field" placeholder="Contoh: 5 tahun" />
                </div>
              </div>
              <div>
                <label className="input-label">Bahasa yang Dikuasai</label>
                <input type="text" value={form.languages || ''} onChange={e => set('languages', e.target.value)} className="input-field" placeholder="Bahasa Indonesia, English, Sunda..." />
              </div>
            </div>
          )}

          {/* BANKING */}
          {activeSection === 'banking' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <SectionTitle icon={<CreditCard size={15} />} title="Rekening Bank" subtitle="Informasi rekening untuk ditampilkan di invoice dan tagihan klien." />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Nama Bank Utama</label>
                  <input type="text" value={form.bankName || ''} onChange={e => set('bankName', e.target.value)} className="input-field" placeholder="BCA, Mandiri, BRI..." />
                </div>
                <div>
                  <label className="input-label">Nomor Rekening</label>
                  <input type="text" value={form.bankAccountNumber || ''} onChange={e => set('bankAccountNumber', e.target.value)} className="input-field" placeholder="0123456789" />
                </div>
              </div>
              <div>
                <label className="input-label">Nama Pemilik Rekening</label>
                <input type="text" value={form.bankAccountHolder || ''} onChange={e => set('bankAccountHolder', e.target.value)} className="input-field" placeholder="Nama sesuai buku tabungan" />
              </div>
              <div>
                <label className="input-label">Info Rekening Kedua (Opsional)</label>
                <textarea value={form.secondaryBankInfo || ''} onChange={e => set('secondaryBankInfo', e.target.value)} className="input-field" style={{ height: '70px', padding: '10px 12px', resize: 'none' }} placeholder="GoPay, OVO, Dana, atau rekening alternatif..." />
              </div>
            </div>
          )}

          {/* BUSINESS */}
          {activeSection === 'business' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <SectionTitle icon={<Building size={15} />} title="Pengaturan Bisnis" subtitle="Default harga, ketentuan, dan nomor NPWP untuk keperluan pajak." />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label className="input-label">Base Fee / Harga Dasar (Rp)</label>
                  <input type="number" min="0" value={form.baseFee || ''} onChange={e => set('baseFee', Number(e.target.value))} className="input-field" style={{ fontWeight: '700', color: 'var(--primary)' }} placeholder="0" />
                </div>
                <div>
                  <label className="input-label">Default DP (%)</label>
                  <div className="input-group">
                    <Percent size={14} className="input-icon-left" />
                    <input type="number" min="0" max="100" value={form.defaultDpPercentage || 30} onChange={e => set('defaultDpPercentage', Number(e.target.value))} className="input-field" />
                  </div>
                </div>
              </div>
              <div>
                <label className="input-label">Nomor NPWP (Opsional)</label>
                <div className="input-group">
                  <Hash size={14} className="input-icon-left" />
                  <input type="text" value={form.npwpNumber || ''} onChange={e => set('npwpNumber', e.target.value)} className="input-field" placeholder="XX.XXX.XXX.X-XXX.XXX" />
                </div>
              </div>
              <div>
                <label className="input-label">Syarat & Ketentuan (T&C Invoice)</label>
                <textarea value={form.termsAndConditions || ''} onChange={e => set('termsAndConditions', e.target.value)} className="input-field" style={{ height: '100px', padding: '10px 12px', resize: 'none' }} placeholder="Contoh: Pembayaran DP wajib sebelum acara. Sisa pembayaran dilunasi H-1 atau di hari acara..." />
              </div>
            </div>
          )}

        </div>

        {message && (
          <div style={{
            marginTop: '16px', padding: '12px 16px', borderRadius: '10px', display: 'flex', alignItems: 'center', gap: '10px',
            background: message.type === 'success' ? 'var(--success-light)' : 'var(--error-light)',
            border: `1px solid ${message.type === 'success' ? 'rgba(5,150,105,0.2)' : 'rgba(220,38,38,0.2)'}`,
            color: message.type === 'success' ? 'var(--success-text)' : 'var(--error-text)'
          }}>
            {message.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
            <p style={{ fontSize: '13px', fontWeight: '600' }}>{message.text}</p>
          </div>
        )}

        <div style={{ marginTop: '20px', display: 'flex', gap: '12px' }}>
          <button type="submit" disabled={saving} className="btn btn-primary btn-lg" style={{ flex: 1, gap: '6px' }}>
            {saving ? 'Menyimpan...' : <><Save size={15} /> Simpan Perubahan</>}
          </button>
        </div>
      </form>
    </div>
  );
};

const SectionTitle: React.FC<{ icon: React.ReactNode; title: string; subtitle: string }> = ({ icon, title, subtitle }) => (
  <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-start', paddingBottom: '16px', borderBottom: '1px solid var(--border)' }}>
    <div style={{ width: '36px', height: '36px', borderRadius: '10px', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, color: 'var(--primary)' }}>{icon}</div>
    <div>
      <h3 style={{ fontSize: '15px', fontWeight: '700', color: 'var(--text-1)', marginBottom: '2px' }}>{title}</h3>
      <p style={{ fontSize: '12px', color: 'var(--text-4)', lineHeight: '1.4' }}>{subtitle}</p>
    </div>
  </div>
);
