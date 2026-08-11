import React, { useState } from 'react';
import { Sparkles, ArrowRight, ShieldCheck, CalendarCheck, FileText, Smartphone } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, sendPasswordResetEmail } from 'firebase/auth';
import { auth } from '../firebase/config';

export const LoginPage: React.FC = () => {
  const { loginWithGoogle } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const [resetSent, setResetSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      if (isRegister) {
        await createUserWithEmailAndPassword(auth, email, password);
      } else {
        await signInWithEmailAndPassword(auth, email, password);
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Gagal autentikasi');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async () => {
    if (!email) {
      setErrorMsg('Masukkan email Anda terlebih dahulu');
      return;
    }
    try {
      await sendPasswordResetEmail(auth, email);
      setResetSent(true);
      setErrorMsg('');
    } catch (err: any) {
      setErrorMsg(err.message || 'Gagal mengirim email reset password');
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col justify-between p-6 relative overflow-hidden">
      {/* Background Glow Effect */}
      <div className="absolute -top-32 -left-32 w-80 h-80 bg-indigo-600/30 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-32 -right-32 w-80 h-80 bg-emerald-500/20 rounded-full blur-3xl pointer-events-none" />

      {/* Brand Header */}
      <div className="pt-8 text-center relative z-10">
        <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-indigo-600 to-indigo-400 mx-auto flex items-center justify-center shadow-xl shadow-indigo-500/30 mb-4 animate-pulse-glow">
          <Sparkles className="w-9 h-9 text-white" />
        </div>
        <h1 className="text-3xl font-extrabold tracking-tight">MCJobId</h1>
        <p className="text-slate-400 text-sm mt-1">Master of Ceremonies Job & Finance Hub</p>
      </div>

      {/* Feature Highlights */}
      <div className="my-6 space-y-3 relative z-10">
        <div className="flex items-center gap-3 bg-slate-800/60 backdrop-blur-md p-3.5 rounded-2xl border border-slate-700/50">
          <div className="w-10 h-10 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
            <CalendarCheck className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-200">Manajemen Job & Rundown</h4>
            <p className="text-xs text-slate-400">Jadwal acara, DP, dan Rundown Hari H</p>
          </div>
        </div>

        <div className="flex items-center gap-3 bg-slate-800/60 backdrop-blur-md p-3.5 rounded-2xl border border-slate-700/50">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
            <FileText className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-200">Invoice & Keuangan MC</h4>
            <p className="text-xs text-slate-400">Cetak Invoice PDF & Grafik Pemasukan</p>
          </div>
        </div>
      </div>

      {/* Auth Card */}
      <div className="bg-slate-800/90 backdrop-blur-xl p-6 rounded-3xl border border-slate-700 shadow-2xl relative z-10">
        <h2 className="text-lg font-bold text-white mb-4">
          {isRegister ? 'Buat Akun MC Job' : 'Masuk ke MCJobId'}
        </h2>

        {errorMsg && (
          <div className="mb-4 p-3 rounded-xl bg-rose-500/20 border border-rose-500/50 text-rose-300 text-xs font-medium">
            {errorMsg}
          </div>
        )}

        {resetSent && (
          <div className="mb-4 p-3 rounded-xl bg-emerald-500/20 border border-emerald-500/50 text-emerald-300 text-xs font-medium">
            Link reset password telah dikirim ke email Anda.
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5">
          {isRegister && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Nama Lengkap</label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Nama Anda"
                className="w-full px-4 py-3 rounded-xl bg-slate-900/80 border border-slate-700 text-white placeholder-slate-500 text-sm outline-none focus:border-indigo-500"
              />
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="mc@example.com"
              className="w-full px-4 py-3 rounded-xl bg-slate-900/80 border border-slate-700 text-white placeholder-slate-500 text-sm outline-none focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-3 rounded-xl bg-slate-900/80 border border-slate-700 text-white placeholder-slate-500 text-sm outline-none focus:border-indigo-500"
            />
          </div>

          {!isRegister && (
            <div className="text-right">
              <button
                type="button"
                onClick={handleForgotPassword}
                className="text-xs text-indigo-400 hover:underline"
              >
                Lupa Password?
              </button>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 font-bold text-white shadow-lg shadow-indigo-600/30 flex items-center justify-center gap-2 text-sm transition-all"
          >
            {loading ? 'Memproses...' : isRegister ? 'Daftar Sekarang' : 'Masuk'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="my-4 flex items-center gap-3">
          <div className="h-[1px] bg-slate-700 flex-1" />
          <span className="text-[11px] text-slate-400 font-semibold uppercase">atau</span>
          <div className="h-[1px] bg-slate-700 flex-1" />
        </div>

        <button
          onClick={loginWithGoogle}
          className="w-full py-3 rounded-xl bg-white text-slate-900 font-bold text-sm flex items-center justify-center gap-3 shadow-md hover:bg-slate-100 transition-all"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          Masuk dengan Google
        </button>

        <p className="text-center text-xs text-slate-400 mt-4">
          {isRegister ? 'Sudah punya akun?' : 'Belum punya akun?'}{' '}
          <button
            onClick={() => setIsRegister(!isRegister)}
            className="text-indigo-400 font-bold hover:underline"
          >
            {isRegister ? 'Masuk di sini' : 'Daftar sekarang'}
          </button>
        </p>
      </div>

      <div className="text-center pt-4 relative z-10">
        <p className="text-[11px] text-slate-500">© 2026 MCJobId. Ready for iOS & Android Web.</p>
      </div>
    </div>
  );
};
