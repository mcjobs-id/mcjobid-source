import React, { useState } from 'react';
import { Sparkles, User, CreditCard, ArrowRight, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { saveUserProfile } from '../services/firebaseService';

export const WizardPage: React.FC<{ onComplete: () => void }> = ({ onComplete }) => {
  const { currentUser, userProfile, refreshProfile } = useAuth();
  const [step, setStep] = useState(1);
  const [stageName, setStageName] = useState(userProfile?.stageName || '');
  const [phone, setPhone] = useState(userProfile?.phone || '');
  const [instagram, setInstagram] = useState(userProfile?.instagram || '');
  const [bankName, setBankName] = useState(userProfile?.bankName || 'BCA');
  const [bankAccount, setBankAccount] = useState(userProfile?.bankAccount || '');
  const [bankHolder, setBankHolder] = useState(userProfile?.bankHolder || '');
  const [saving, setSaving] = useState(false);

  const handleFinish = async () => {
    if (!currentUser) return;
    setSaving(true);

    try {
      const updated = {
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
      };

      await saveUserProfile(updated);
      await refreshProfile();
      onComplete();
    } catch (err) {
      console.error('Error saving wizard profile:', err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col justify-between p-6">
      <div>
        {/* Step Indicators */}
        <div className="flex items-center justify-between mb-8 pt-4">
          <div className="flex items-center gap-2">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs ${step >= 1 ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-500'}`}>
              1
            </div>
            <div className={`h-1 w-12 rounded ${step >= 2 ? 'bg-indigo-600' : 'bg-slate-800'}`} />
            <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs ${step >= 2 ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-500'}`}>
              2
            </div>
          </div>
          <span className="text-xs text-slate-400 font-semibold">Langkah {step} dari 2</span>
        </div>

        {step === 1 ? (
          <div className="space-y-4 animate-fade-in">
            <div className="w-12 h-12 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center mb-2">
              <User className="w-6 h-6" />
            </div>
            <h2 className="text-2xl font-extrabold">Profil MC Anda</h2>
            <p className="text-sm text-slate-400">Lengkapi nama panggung dan kontak profesional Anda untuk ditampilkan pada invoice dan rate card.</p>

            <div className="space-y-3 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nama Panggung / Brand MC</label>
                <input
                  type="text"
                  value={stageName}
                  onChange={(e) => setStageName(e.target.value)}
                  placeholder="Contoh: MC Rizky / MC Wedding Pro"
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nomor WhatsApp / HP</label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="081234567890"
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Instagram (opsional)</label>
                <input
                  type="text"
                  value={instagram}
                  onChange={(e) => setInstagram(e.target.value)}
                  placeholder="@mc_rizky"
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                />
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-4 animate-fade-in">
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center mb-2">
              <CreditCard className="w-6 h-6" />
            </div>
            <h2 className="text-2xl font-extrabold">Informasi Pembayaran</h2>
            <p className="text-sm text-slate-400">Rekening ini akan dicantumkan secara otomatis di setiap Invoice yang Anda buat.</p>

            <div className="space-y-3 pt-2">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nama Bank</label>
                <select
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                >
                  <option value="BCA">BCA</option>
                  <option value="Mandiri">Bank Mandiri</option>
                  <option value="BRI">BRI</option>
                  <option value="BNI">BNI</option>
                  <option value="BSI">BSI</option>
                  <option value="CIMB Niaga">CIMB Niaga</option>
                  <option value="Lainnya">Lainnya</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nomor Rekening</label>
                <input
                  type="text"
                  value={bankAccount}
                  onChange={(e) => setBankAccount(e.target.value)}
                  placeholder="1234567890"
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Nama Pemilik Rekening</label>
                <input
                  type="text"
                  value={bankHolder}
                  onChange={(e) => setBankHolder(e.target.value)}
                  placeholder="Sesuai buku tabungan"
                  className="w-full px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-white text-sm outline-none focus:border-indigo-500"
                />
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="flex items-center gap-3 pt-6">
        {step === 2 && (
          <button
            onClick={() => setStep(1)}
            className="py-3.5 px-6 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-sm"
          >
            Kembali
          </button>
        )}

        {step === 1 ? (
          <button
            onClick={() => setStep(2)}
            className="flex-1 py-3.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 font-bold text-white shadow-lg flex items-center justify-center gap-2 text-sm"
          >
            Lanjut
            <ArrowRight className="w-4 h-4" />
          </button>
        ) : (
          <button
            onClick={handleFinish}
            disabled={saving}
            className="flex-1 py-3.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 font-bold text-white shadow-lg flex items-center justify-center gap-2 text-sm"
          >
            {saving ? 'Menyimpan...' : 'Selesai & Masuk Beranda'}
            <CheckCircle2 className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
};
