import React, { useState } from 'react';
import { Sparkles, ArrowRight, CalendarCheck, FileText, CheckCircle2, ShieldCheck } from 'lucide-react';
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
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-white flex items-center justify-center p-4 md:p-8">
      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-12 bg-white dark:bg-slate-800 rounded-3xl border border-slate-200/80 dark:border-slate-700 shadow-2xl overflow-hidden min-h-[600px]">
        {/* Left Side: Professional Branding & Feature Highlights (Hidden on small screens) */}
        <div className="lg:col-span-6 bg-gradient-to-br from-indigo-700 via-indigo-600 to-indigo-900 p-8 lg:p-12 text-white flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 -mr-20 -mt-20 w-80 h-80 bg-white/10 rounded-full blur-3xl pointer-events-none" />

          {/* Logo & Header */}
          <div className="relative z-10 space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center shadow-lg border border-white/20">
              <Sparkles className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl lg:text-4xl font-extrabold tracking-tight">MCJobId</h1>
              <p className="text-indigo-100 text-sm font-medium mt-1">
                Platform Profesional Khusus Master of Ceremonies (MC)
              </p>
            </div>
          </div>

          {/* Feature List */}
          <div className="relative z-10 my-8 space-y-4">
            <div className="flex items-start gap-3 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/10">
              <CalendarCheck className="w-6 h-6 text-indigo-200 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="font-bold text-sm text-white">Manajemen Jadwal & Rundown Acara</h4>
                <p className="text-xs text-indigo-100 mt-0.5">
                  Atur tanggal manggung, rincian lokasi venue, catatan khusus, dan rundown per jam.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/10">
              <FileText className="w-6 h-6 text-emerald-300 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="font-bold text-sm text-white">Invoice PDF & Pencatatan DP</h4>
                <p className="text-xs text-indigo-100 mt-0.5">
                  Cetak invoice resmi pelunasan honorarium dalam format PDF profesional.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/10">
              <ShieldCheck className="w-6 h-6 text-indigo-300 flex-shrink-0 mt-0.5" />
              <div>
                <h4 className="font-bold text-sm text-white">Akses Multi-Device Real-time</h4>
                <p className="text-xs text-indigo-100 mt-0.5">
                  Bisa dibuka dengan lancar dari Laptop/Desktop, iPhone, iPad, maupun Android.
                </p>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="relative z-10 pt-4 border-t border-white/10 text-xs text-indigo-200">
            © 2026 MCJobId. Ready for Desktop, iOS & Android.
          </div>
        </div>

        {/* Right Side: Clean Login Form */}
        <div className="lg:col-span-6 p-6 sm:p-10 md:p-12 flex flex-col justify-center bg-white dark:bg-slate-800">
          <div className="max-w-md mx-auto w-full space-y-6">
            <div>
              <h2 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tight">
                {isRegister ? 'Buat Akun MC Studio' : 'Selamat Datang Kembali'}
              </h2>
              <p className="text-slate-500 dark:text-slate-400 text-sm mt-1">
                {isRegister ? 'Daftar untuk mengelola jadwal job MC Anda.' : 'Masuk untuk mengelola agenda dan keuangan Anda.'}
              </p>
            </div>

            {errorMsg && (
              <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-medium dark:bg-rose-950/40 dark:border-rose-800 dark:text-rose-300">
                {errorMsg}
              </div>
            )}

            {resetSent && (
              <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-medium dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-300">
                Link reset password telah dikirim ke email Anda.
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {isRegister && (
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider">
                    Nama Lengkap / Stage Name
                  </label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Contoh: MC Royan Subroto"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-sm outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/20 transition-all"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider">
                  Email
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="mc@example.com"
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-sm outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/20 transition-all"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider">
                  Password
                </label>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-sm outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/20 transition-all"
                />
              </div>

              {!isRegister && (
                <div className="text-right">
                  <button
                    type="button"
                    onClick={handleForgotPassword}
                    className="text-xs font-bold text-indigo-600 dark:text-indigo-400 hover:underline cursor-pointer"
                  >
                    Lupa Password?
                  </button>
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm shadow-md shadow-indigo-600/20 flex items-center justify-center gap-2 transition-all cursor-pointer"
              >
                {loading ? 'Memproses...' : isRegister ? 'Daftar Sekarang' : 'Masuk'}
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>

            <div className="my-4 flex items-center gap-3">
              <div className="h-[1px] bg-slate-200 dark:bg-slate-700 flex-1" />
              <span className="text-[11px] text-slate-400 font-semibold uppercase">atau</span>
              <div className="h-[1px] bg-slate-200 dark:bg-slate-700 flex-1" />
            </div>

            <button
              onClick={loginWithGoogle}
              className="w-full py-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200 font-bold text-sm flex items-center justify-center gap-3 shadow-sm hover:bg-slate-50 dark:hover:bg-slate-800 transition-all cursor-pointer"
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

            <p className="text-center text-xs text-slate-500 dark:text-slate-400 mt-4">
              {isRegister ? 'Sudah punya akun?' : 'Belum punya akun?'}{' '}
              <button
                onClick={() => setIsRegister(!isRegister)}
                className="text-indigo-600 dark:text-indigo-400 font-bold hover:underline cursor-pointer"
              >
                {isRegister ? 'Masuk di sini' : 'Daftar sekarang'}
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
