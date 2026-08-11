import React, { useState } from 'react';
import { User, Smartphone, CreditCard, LogOut, CheckCircle2, Share, PlusSquare, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { saveUserProfile } from '../services/firebaseService';

export const ProfilePage: React.FC = () => {
  const { currentUser, userProfile, logout, refreshProfile } = useAuth();
  const [stageName, setStageName] = useState(userProfile?.stageName || '');
  const [phone, setPhone] = useState(userProfile?.phone || '');
  const [instagram, setInstagram] = useState(userProfile?.instagram || '');
  const [bankName, setBankName] = useState(userProfile?.bankName || 'BCA');
  const [bankAccount, setBankAccount] = useState(userProfile?.bankAccount || '');
  const [bankHolder, setBankHolder] = useState(userProfile?.bankHolder || '');
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;
    setSaving(true);
    try {
      await saveUserProfile({
        uid: currentUser.uid,
        name: userProfile?.name || currentUser.displayName || 'MC Talent',
        email: currentUser.email || '',
        stageName,
        phone,
        instagram,
        bankName,
        bankAccount,
        bankHolder,
        profileCompleted: true,
        createdAt: userProfile?.createdAt || new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
      await refreshProfile();
      setSuccessMsg(true);
      setTimeout(() => setSuccessMsg(false), 3000);
    } catch (err) {
      console.error('Error saving profile:', err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Profile Header */}
      <div className="card text-center py-6 bg-gradient-to-br from-indigo-900 to-slate-900 text-white border-none shadow-xl">
        <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-indigo-500 to-emerald-400 mx-auto flex items-center justify-center font-black text-2xl shadow-lg mb-2">
          {(userProfile?.stageName || userProfile?.name || 'MC').substring(0, 2).toUpperCase()}
        </div>
        <h2 className="text-xl font-extrabold">{userProfile?.stageName || userProfile?.name || 'MC Talent'}</h2>
        <p className="text-xs text-indigo-200">{currentUser?.email}</p>
        <span className="inline-block mt-2 text-[10px] font-bold px-3 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/40">
          PRO ACCOUNT • SINKRON REALTIME
        </span>
      </div>

      {/* iOS PWA INSTALLATION GUIDE BANNER */}
      <div className="bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-200 dark:border-indigo-800 rounded-3xl p-4 space-y-2 text-xs">
        <h3 className="font-extrabold text-indigo-900 dark:text-indigo-200 flex items-center gap-1.5 text-sm">
          <Smartphone className="w-4 h-4 text-indigo-600" /> Panduan Akses iOS (iPhone/iPad)
        </h3>
        <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
          Agar aplikasi ini bisa dibuka tanpa address bar layaknya aplikasi iOS native:
        </p>
        <ol className="list-decimal list-inside space-y-1 text-slate-700 dark:text-slate-200 font-medium">
          <li>Buka web ini di browser <strong>Safari iOS</strong>.</li>
          <li>Ketuk tombol <strong>Bagikan / Share</strong> (<Share className="w-3.5 h-3.5 inline mx-0.5" />) di bagian bawah Safari.</li>
          <li>Pilih menu <strong>"Tambah ke Layar Utama" (Add to Home Screen)</strong> (<PlusSquare className="w-3.5 h-3.5 inline mx-0.5" />).</li>
          <li>Icon MCJobId akan muncul di layar utama iPhone Anda!</li>
        </ol>
      </div>

      {/* Edit Profile Form */}
      <div className="card space-y-3">
        <h3 className="font-extrabold text-slate-900 dark:text-white text-sm border-b border-slate-100 dark:border-slate-800 pb-2">
          Edit Informasi Profil & Rekening
        </h3>

        {successMsg && (
          <div className="p-3 rounded-xl bg-emerald-500/20 text-emerald-600 dark:text-emerald-300 text-xs font-bold flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4" /> Profil berhasil diperbarui!
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-3 text-xs">
          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nama Panggung / Brand MC</label>
            <input
              type="text"
              value={stageName}
              onChange={(e) => setStageName(e.target.value)}
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">No WhatsApp</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="input-field text-xs py-2.5"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Instagram</label>
              <input
                type="text"
                value={instagram}
                onChange={(e) => setInstagram(e.target.value)}
                className="input-field text-xs py-2.5"
              />
            </div>
          </div>

          <div className="pt-2 border-t border-slate-100 dark:border-slate-800">
            <h4 className="font-bold text-slate-800 dark:text-slate-200 mb-2">Rekening Invoice Default</h4>
            <div className="grid grid-cols-3 gap-2">
              <div>
                <label className="block font-bold text-slate-400 mb-1">Bank</label>
                <input
                  type="text"
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                  className="input-field text-xs py-2"
                />
              </div>
              <div>
                <label className="block font-bold text-slate-400 mb-1">No Rekening</label>
                <input
                  type="text"
                  value={bankAccount}
                  onChange={(e) => setBankAccount(e.target.value)}
                  className="input-field text-xs py-2"
                />
              </div>
              <div>
                <label className="block font-bold text-slate-400 mb-1">A.n Pemilik</label>
                <input
                  type="text"
                  value={bankHolder}
                  onChange={(e) => setBankHolder(e.target.value)}
                  className="input-field text-xs py-2"
                />
              </div>
            </div>
          </div>

          <div className="pt-3">
            <button type="submit" disabled={saving} className="btn-primary w-full py-3 text-xs">
              {saving ? 'Menyimpan Perubahan...' : 'Simpan Profil'}
            </button>
          </div>
        </form>
      </div>

      {/* Logout */}
      <button
        onClick={logout}
        className="w-full py-3 rounded-2xl bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 font-bold text-xs flex items-center justify-center gap-2 border border-rose-200 dark:border-rose-900/50 hover:bg-rose-100 transition-colors"
      >
        <LogOut className="w-4 h-4" /> Keluar dari Akun MC
      </button>
    </div>
  );
};
