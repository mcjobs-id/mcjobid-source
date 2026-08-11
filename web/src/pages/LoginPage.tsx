import React, { useState } from 'react';
import { Lock, Mail, Eye, EyeOff, ShieldCheck, LockKeyhole, RefreshCw, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, sendPasswordResetEmail } from 'firebase/auth';
import { auth } from '../firebase/config';

export const LoginPage: React.FC = () => {
  const { loginWithGoogle } = useAuth();
  
  // Registration form state
  const [emailState, setEmailState] = useState('');
  const [passwordState, setPasswordState] = useState('');
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  
  // Modals state
  const [showLoginDialog, setShowLoginDialog] = useState(false);
  const [showResetDialog, setShowResetDialog] = useState(false);
  
  // Login Dialog state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [isLoginPasswordVisible, setIsLoginPasswordVisible] = useState(false);
  
  // Reset Password state
  const [resetEmail, setResetEmail] = useState('');
  const [resetStatus, setResetStatus] = useState<string | null>(null);

  // Status & Error
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loginErrorMsg, setLoginErrorMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Password complexity check
  const hasUpper = /[A-Z]/.test(passwordState);
  const hasLower = /[a-z]/.test(passwordState);
  const hasDigit = /[0-9]/.test(passwordState);
  const isPasswordValid = hasUpper && hasLower && hasDigit && passwordState.length >= 6;

  // Submit Register
  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    if (!emailState.trim()) {
      setErrorMsg('Email pembayaran wajib diisi.');
      return;
    }

    if (!isPasswordValid) {
      setErrorMsg('Password harus mengandung minimal Kapital (A-Z), Huruf Kecil (a-z), dan Angka (0-9).');
      return;
    }

    setLoading(true);
    try {
      await createUserWithEmailAndPassword(auth, emailState, passwordState);
    } catch (err: any) {
      setErrorMsg(err.message || 'Gagal mendaftarkan akun. Silakan coba lagi.');
    } finally {
      setLoading(false);
    }
  };

  // Submit Login Dialog
  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginErrorMsg(null);

    if (!loginEmail.trim() || !loginPassword.trim()) {
      setLoginErrorMsg('Email dan password wajib diisi.');
      return;
    }

    setLoading(true);
    try {
      await signInWithEmailAndPassword(auth, loginEmail, loginPassword);
      setShowLoginDialog(false);
    } catch (err: any) {
      setLoginErrorMsg(err.message || 'Email atau password salah.');
    } finally {
      setLoading(false);
    }
  };

  // Submit Reset Password
  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetEmail.trim()) {
      setResetStatus('Gagal: Email wajib diisi.');
      return;
    }

    setLoading(true);
    try {
      await sendPasswordResetEmail(auth, resetEmail);
      setResetStatus('Tautan reset password berhasil dikirim ke email Anda!');
    } catch (err: any) {
      setResetStatus(`Gagal: ${err.message || 'Email tidak ditemukan.'}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] dark:bg-slate-900 text-[#0F172A] dark:text-white flex flex-col justify-between items-center p-4 sm:p-6 transition-colors">
      <div className="w-full max-w-md mx-auto py-4 space-y-6 flex-1 flex flex-col justify-center">
        {/* Centered Logo Header */}
        <div className="text-center space-y-1">
          <div className="w-12 h-12 rounded-2xl bg-indigo-600 mx-auto flex items-center justify-center text-white shadow-lg shadow-indigo-600/30 font-black text-xl tracking-tighter">
            MC
          </div>
          <h1 className="text-xl font-black text-indigo-600 dark:text-indigo-400 tracking-tight mt-2">
            mcjob.id
          </h1>
          <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
            powered by career mc academy
          </p>
        </div>

        {/* Main Card Container: "Akses Eksklusif" */}
        <div className="bg-white dark:bg-slate-800 rounded-3xl p-6 sm:p-8 border border-slate-200/80 dark:border-slate-700 shadow-xl shadow-slate-200/50 dark:shadow-none space-y-5 relative">
          {/* Top Accent Blue Bar */}
          <div className="w-14 h-1 bg-indigo-600 rounded-full mx-auto" />

          {/* Lock Icon Badge */}
          <div className="w-16 h-16 rounded-2xl bg-[#0F172A] dark:bg-slate-900 mx-auto flex items-center justify-center text-white shadow-md">
            <Lock className="w-7 h-7 text-white" />
          </div>

          {/* Title */}
          <div className="text-center">
            <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
              Akses Eksklusif
            </h2>

            {/* Subtitle Pill Badge */}
            <div className="mt-2.5 p-3 rounded-xl bg-blue-50 dark:bg-blue-950/50 border border-blue-100 dark:border-blue-900 text-blue-800 dark:text-blue-300 text-xs font-medium leading-relaxed">
              Asisten khusus untuk mencatat jadwal MC dan pantau keuangan dengan aman & rahasia.
            </div>

            {/* Instructional Notice Box */}
            <div className="mt-2.5 p-3 rounded-xl bg-indigo-50 dark:bg-indigo-950/50 border border-indigo-100 dark:border-indigo-900 text-indigo-900 dark:text-indigo-300 text-xs font-bold">
              Silakan Buat Akun dengan Email Pembayaran Anda
            </div>
          </div>

          {/* Error Feedback */}
          {errorMsg && (
            <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold dark:bg-rose-950/50 dark:border-rose-900 dark:text-rose-300">
              {errorMsg}
            </div>
          )}

          {/* Registration Form */}
          <form onSubmit={handleRegisterSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Email Pembayaran Anda
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  value={emailState}
                  onChange={(e) => setEmailState(e.target.value)}
                  placeholder="nama@email.com"
                  className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-sm outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/20 transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Buat Password / Kata Sandi
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type={isPasswordVisible ? 'text' : 'password'}
                  required
                  value={passwordState}
                  onChange={(e) => setPasswordState(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-10 py-3 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-sm outline-none focus:border-indigo-600 focus:ring-2 focus:ring-indigo-600/20 transition-all"
                />
                <button
                  type="button"
                  onClick={() => setIsPasswordVisible(!isPasswordVisible)}
                  className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 cursor-pointer"
                >
                  {isPasswordVisible ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
                Mendukung kombinasi huruf kapital, huruf kecil, dan angka.
              </p>
            </div>

            {/* Primary CTA Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl bg-[#0F172A] hover:bg-slate-800 active:scale-[0.98] text-white font-extrabold text-sm shadow-md transition-all cursor-pointer flex items-center justify-center gap-2"
            >
              {loading ? (
                <RefreshCw className="w-4 h-4 animate-spin text-white" />
              ) : (
                <span>Daftar MCJOB.id</span>
              )}
            </button>
          </form>

          {/* Login Option for Existing Users */}
          <div className="pt-2 text-center text-xs text-slate-500 dark:text-slate-400">
            Sudah punya akun?{' '}
            <button
              type="button"
              onClick={() => setShowLoginDialog(true)}
              className="text-indigo-600 dark:text-indigo-400 font-extrabold hover:underline cursor-pointer"
            >
              Login
            </button>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-[1px] bg-slate-200 dark:bg-slate-700 flex-1" />
            <span className="text-[10px] text-slate-400 font-bold uppercase">atau</span>
            <div className="h-[1px] bg-slate-200 dark:bg-slate-700 flex-1" />
          </div>

          {/* Google Login Option */}
          <button
            type="button"
            onClick={loginWithGoogle}
            className="w-full py-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200 font-bold text-xs flex items-center justify-center gap-2.5 shadow-sm hover:bg-slate-50 dark:hover:bg-slate-800 transition-all cursor-pointer"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
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
        </div>

        {/* Security Footer */}
        <div className="text-center space-y-1 pt-2">
          <div className="inline-flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            <span>Keamanan Data & Privasi Terenkripsi</span>
          </div>
          <p className="text-[11px] text-slate-400 dark:text-slate-500">
            Powered by @careermc.academy
          </p>
        </div>
      </div>

      {/* LoginDialog Modal */}
      {showLoginDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-white dark:bg-slate-800 rounded-3xl p-6 sm:p-8 max-w-sm w-full border border-slate-200 dark:border-slate-700 shadow-2xl space-y-4 relative">
            <button
              onClick={() => setShowLoginDialog(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="text-center space-y-2">
              <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 mx-auto flex items-center justify-center">
                <LockKeyhole className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-black text-slate-900 dark:text-white">
                Login ke Akun
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                Gunakan Email & Password yang sudah Anda daftarkan sebelumnya.
              </p>
            </div>

            {loginErrorMsg && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold dark:bg-rose-950/50 dark:border-rose-900 dark:text-rose-300 text-center">
                {loginErrorMsg}
              </div>
            )}

            <form onSubmit={handleLoginSubmit} className="space-y-3">
              <div>
                <input
                  type="email"
                  required
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  placeholder="nama@email.com"
                  className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-xs outline-none focus:border-indigo-600"
                />
              </div>

              <div>
                <div className="relative">
                  <input
                    type={isLoginPasswordVisible ? 'text' : 'password'}
                    required
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="Kata sandi Anda"
                    className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-xs outline-none focus:border-indigo-600"
                  />
                  <button
                    type="button"
                    onClick={() => setIsLoginPasswordVisible(!isLoginPasswordVisible)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400"
                  >
                    {isLoginPasswordVisible ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>

              <div className="text-right">
                <button
                  type="button"
                  onClick={() => {
                    setShowLoginDialog(false);
                    setShowResetDialog(true);
                  }}
                  className="text-xs font-bold text-indigo-600 dark:text-indigo-400 hover:underline cursor-pointer"
                >
                  Lupa Password?
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-[#0F172A] hover:bg-slate-800 text-white font-extrabold text-xs shadow-md transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                {loading ? <RefreshCw className="w-4 h-4 animate-spin text-white" /> : 'Masuk Sekarang'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ResetPasswordDialog Modal */}
      {showResetDialog && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in">
          <div className="bg-white dark:bg-slate-800 rounded-3xl p-6 sm:p-8 max-w-sm w-full border border-slate-200 dark:border-slate-700 shadow-2xl space-y-4 relative">
            <button
              onClick={() => setShowResetDialog(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="text-center space-y-2">
              <h3 className="text-xl font-black text-slate-900 dark:text-white">
                Reset Password
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
                Masukkan email terdaftar Anda. Kami akan mengirim tautan untuk membuat password baru.
              </p>
            </div>

            {resetStatus && (
              <div className={`p-3 rounded-xl text-xs font-semibold text-center ${
                resetStatus.startsWith('Gagal')
                  ? 'bg-rose-50 border border-rose-200 text-rose-700 dark:bg-rose-950/50 dark:text-rose-300'
                  : 'bg-emerald-50 border border-emerald-200 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300'
              }`}>
                {resetStatus}
              </div>
            )}

            <form onSubmit={handleResetSubmit} className="space-y-3">
              <input
                type="email"
                required
                value={resetEmail}
                onChange={(e) => setResetEmail(e.target.value)}
                placeholder="nama@email.com"
                className="w-full px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 text-xs outline-none focus:border-indigo-600"
              />

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-[#0F172A] hover:bg-slate-800 text-white font-extrabold text-xs shadow-md transition-all cursor-pointer flex items-center justify-center"
              >
                {loading ? <RefreshCw className="w-4 h-4 animate-spin text-white" /> : 'Kirim Tautan Reset'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
