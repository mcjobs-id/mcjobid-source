import React, { useState } from 'react';
import { createPortal } from 'react-dom';
import { Lock, Mail, Eye, EyeOff, ShieldCheck, RefreshCw, X, Mic, Calendar, DollarSign, BarChart2, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword, sendPasswordResetEmail, updateProfile } from 'firebase/auth';
import { auth } from '../firebase/config';

// Friendly Indonesian Auth Errors Mapper
function getIndonesianAuthErrorMessage(error: any): string {
  const code = error?.code || '';
  switch (code) {
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
      return 'Email atau password yang Anda masukkan salah. Silakan periksa kembali.';
    case 'auth/email-already-in-use':
      return 'Email ini sudah terdaftar. Silakan klik "Masuk sekarang" untuk login.';
    case 'auth/invalid-email':
      return 'Format email tidak valid. Pastikan penulisan email sudah benar.';
    case 'auth/weak-password':
      return 'Password terlalu lemah. Gunakan minimal 6 karakter kombinasi huruf dan angka.';
    case 'auth/too-many-requests':
      return 'Terlalu banyak percobaan gagal. Silakan coba beberapa saat lagi.';
    case 'auth/network-request-failed':
      return 'Koneksi internet bermasalah. Periksa koneksi perangkat Anda.';
    default:
      return error?.message || 'Terjadi kesalahan sistem. Silakan coba lagi.';
  }
}

export const LoginPage: React.FC = () => {
  const [nameState, setNameState] = useState('');
  const [emailState, setEmailState] = useState('');
  const [passwordState, setPasswordState] = useState('');
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [showLoginDialog, setShowLoginDialog] = useState(false);
  const [showResetDialog, setShowResetDialog] = useState(false);
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [isLoginPasswordVisible, setIsLoginPasswordVisible] = useState(false);
  const [resetEmail, setResetEmail] = useState('');
  const [resetStatus, setResetStatus] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loginErrorMsg, setLoginErrorMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const hasUpper = /[A-Z]/.test(passwordState);
  const hasLower = /[a-z]/.test(passwordState);
  const hasDigit = /[0-9]/.test(passwordState);
  const isPasswordValid = hasUpper && hasLower && hasDigit && passwordState.length >= 6;

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    if (!nameState.trim()) { setErrorMsg('Nama / Stage Name wajib diisi.'); return; }
    if (!emailState.trim()) { setErrorMsg('Email pembayaran wajib diisi.'); return; }
    if (!isPasswordValid) { setErrorMsg('Password harus mengisikan huruf kapital, huruf kecil, dan angka (min 6 karakter).'); return; }
    setLoading(true);
    try {
      const cred = await createUserWithEmailAndPassword(auth, emailState, passwordState);
      // Save name to Firebase Auth so AuthContext uses it for profile creation
      await updateProfile(cred.user, { displayName: nameState.trim() });
    } catch (err: any) {
      setErrorMsg(getIndonesianAuthErrorMessage(err));
    } finally { setLoading(false); }
  };

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoginErrorMsg(null);
    if (!loginEmail.trim() || !loginPassword.trim()) { setLoginErrorMsg('Email dan password wajib diisi.'); return; }
    setLoading(true);
    try {
      await signInWithEmailAndPassword(auth, loginEmail, loginPassword);
      setShowLoginDialog(false);
    } catch (err: any) {
      setLoginErrorMsg(getIndonesianAuthErrorMessage(err));
    } finally { setLoading(false); }
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetEmail.trim()) { setResetStatus('Gagal: Email wajib diisi.'); return; }
    setLoading(true);
    try {
      await sendPasswordResetEmail(auth, resetEmail);
      setResetStatus('Tautan reset password berhasil dikirim ke email Anda! Silakan cek kotak masuk.');
    } catch (err: any) {
      setResetStatus(`Gagal: ${getIndonesianAuthErrorMessage(err)}`);
    } finally { setLoading(false); }
  };

  const features = [
    { icon: Calendar, title: 'Manajemen Jadwal Acara', desc: 'Catat dan kelola semua jadwal manggung dalam satu tampilan profesional.' },
    { icon: DollarSign, title: 'Keuangan & Invoice Otomatis', desc: 'Pantau honorarium, DP, piutang, dan hasilkan invoice PDF profesional.' },
    { icon: BarChart2, title: 'Analitik Performa Bisnis', desc: 'Laporan omset, net profit, dan tren perkembangan karier MC Anda.' },
  ];

  return (
    <>
      <div className="login-layout">
        {/* ========== LEFT HERO PANEL ========== */}
        <div className="login-hero">
          {/* Decorative circles */}
          <div style={{position:'absolute', top: '-60px', right: '-60px', width: '240px', height: '240px', borderRadius: '50%', background: 'rgba(255,255,255,0.05)', zIndex: 0}} />
          <div style={{position:'absolute', bottom: '80px', left: '-40px', width: '160px', height: '160px', borderRadius: '50%', background: 'rgba(255,255,255,0.04)', zIndex: 0}} />

          {/* Hero content */}
          <div style={{position: 'relative', zIndex: 1}}>
            {/* Brand */}
            <div style={{display:'flex', alignItems:'center', gap: '12px', marginBottom: '64px'}}>
              <div style={{width:'40px', height:'40px', borderRadius:'12px', background:'rgba(255,255,255,0.15)', border:'1px solid rgba(255,255,255,0.2)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                <Mic size={20} color="white" />
              </div>
              <div>
                <span style={{fontSize:'18px', fontWeight:'800', color:'white', letterSpacing:'-0.02em'}}>mcjob.id</span>
                <p style={{fontSize:'10px', fontWeight:'600', color:'rgba(255,255,255,0.55)', textTransform:'uppercase', letterSpacing:'0.08em', marginTop:'1px'}}>powered by career mc academy</p>
              </div>
            </div>

            {/* Hero Title */}
            <div style={{marginBottom: '40px'}}>
              <h1 style={{fontSize:'clamp(28px,3.5vw,40px)', fontWeight:'800', color:'white', letterSpacing:'-0.03em', lineHeight:'1.1', marginBottom:'16px'}}>
                Platform Profesional<br/>
                untuk MC Indonesia
              </h1>
              <p style={{fontSize:'15px', color:'rgba(255,255,255,0.65)', lineHeight:'1.7', maxWidth:'340px'}}>
                Catat jadwal acara, kelola keuangan, dan hasilkan invoice—semuanya dalam satu platform eksklusif.
              </p>
            </div>

            {/* Feature Bullets */}
            <div style={{display:'flex', flexDirection:'column', gap:'16px'}}>
              {features.map((f, i) => {
                const Icon = f.icon;
                return (
                  <div key={i} style={{display:'flex', alignItems:'flex-start', gap:'14px'}}>
                    <div style={{width:'36px', height:'36px', borderRadius:'10px', background:'rgba(255,255,255,0.12)', border:'1px solid rgba(255,255,255,0.15)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0}}>
                      <Icon size={16} color="white" />
                    </div>
                    <div>
                      <p style={{fontSize:'13px', fontWeight:'700', color:'white', marginBottom:'2px'}}>{f.title}</p>
                      <p style={{fontSize:'12px', color:'rgba(255,255,255,0.55)', lineHeight:'1.5'}}>{f.desc}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Hero Bottom: Testimonial/Trust Badge */}
          <div style={{position:'relative', zIndex:1, padding:'20px', borderRadius:'16px', background:'rgba(255,255,255,0.08)', border:'1px solid rgba(255,255,255,0.12)'}}>
            <div style={{display:'flex', alignItems:'center', gap:'10px', marginBottom:'10px'}}>
              {[1,2,3].map(i => (
                <div key={i} style={{width:'28px', height:'28px', borderRadius:'50%', background:`rgba(255,255,255,${0.15 + i*0.05})`, border:'2px solid rgba(255,255,255,0.3)', marginLeft: i > 1 ? '-8px' : '0', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'10px', fontWeight:'800', color:'white'}}>
                  {['A','R','M'][i-1]}
                </div>
              ))}
              <span style={{fontSize:'11px', color:'rgba(255,255,255,0.65)', marginLeft:'4px'}}>+200 MC aktif menggunakan platform ini</span>
            </div>
            <div style={{display:'flex', gap:'4px'}}>
              {[...Array(5)].map((_, i) => (
                <svg key={i} width="12" height="12" viewBox="0 0 12 12" fill="#FCD34D"><path d="M6 0l1.5 4.6H12L8.3 7.4l1.4 4.6L6 9.4 2.3 12l1.4-4.6L0 4.6h4.5z"/></svg>
              ))}
              <span style={{fontSize:'11px', color:'rgba(255,255,255,0.65)', marginLeft:'6px'}}>4.9/5 — Rating Pengguna</span>
            </div>
          </div>
        </div>

        {/* ========== RIGHT FORM PANEL ========== */}
        <div className="login-form-panel">
          <div style={{width:'100%', maxWidth:'400px'}}>
            {/* Mobile Brand (hidden on desktop) */}
            <div style={{display:'flex', alignItems:'center', gap:'10px', marginBottom:'32px'}} className="lg:hidden-brand">
              <div style={{width:'36px', height:'36px', borderRadius:'10px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center', boxShadow:'0 4px 12px rgba(79,70,229,0.3)'}}>
                <Mic size={18} color="white" />
              </div>
              <div>
                <span style={{fontSize:'16px', fontWeight:'800', color:'var(--primary)', letterSpacing:'-0.02em'}}>mcjob.id</span>
              </div>
            </div>

            {/* Form Header */}
            <div style={{marginBottom:'28px'}}>
              <h2 style={{fontSize:'24px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.025em', lineHeight:'1.2', marginBottom:'6px'}}>
                Akses Eksklusif
              </h2>
              <p style={{fontSize:'14px', color:'var(--text-3)', lineHeight:'1.6'}}>
                Asisten khusus MC profesional Indonesia — catat jadwal & pantau keuangan dengan aman.
              </p>
            </div>

            {/* Notice box */}
            <div style={{padding:'10px 14px', borderRadius:'10px', background:'var(--primary-light)', border:'1px solid rgba(79,70,229,0.2)', marginBottom:'24px', display:'flex', alignItems:'flex-start', gap:'10px'}}>
              <Lock size={14} color="var(--primary)" style={{marginTop:'1px', flexShrink:0}} />
              <p style={{fontSize:'12px', fontWeight:'600', color:'var(--primary-text)', lineHeight:'1.5'}}>
                Silakan buat akun menggunakan <strong>Email Pembayaran</strong> yang telah Anda daftarkan.
              </p>
            </div>

            {/* Error */}
            {errorMsg && (
              <div style={{padding:'10px 14px', borderRadius:'10px', background:'var(--error-light)', border:'1px solid rgba(220,38,38,0.2)', marginBottom:'16px'}}>
                <p style={{fontSize:'12px', fontWeight:'600', color:'var(--error-text)'}}>{errorMsg}</p>
              </div>
            )}

            {/* Registration Form */}
            <form onSubmit={handleRegisterSubmit} style={{display:'flex', flexDirection:'column', gap:'16px'}}>

              {/* Name field */}
              <div>
                <label className="input-label">Nama Lengkap / Stage Name *</label>
                <div className="input-group">
                  <span style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)', display:'flex', pointerEvents:'none'}}>
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>
                  </span>
                  <input
                    type="text"
                    required
                    value={nameState}
                    onChange={(e) => setNameState(e.target.value)}
                    placeholder="Contoh: Rian Febrian / MC Kirana"
                    className="input-field"
                    style={{paddingLeft: '38px'}}
                  />
                </div>
              </div>

              <div>
                <label className="input-label">Email Pembayaran Anda</label>
                <div className="input-group">
                  <Mail size={15} className="input-icon-left" />
                  <input
                    type="email"
                    required
                    value={emailState}
                    onChange={(e) => setEmailState(e.target.value)}
                    placeholder="nama@email.com"
                    className="input-field"
                    style={{paddingLeft: '38px'}}
                  />
                </div>
              </div>

              <div>
                <label className="input-label">Buat Password / Kata Sandi</label>
                <div className="input-group" style={{position:'relative'}}>
                  <Lock size={15} className="input-icon-left" />
                  <input
                    type={isPasswordVisible ? 'text' : 'password'}
                    required
                    value={passwordState}
                    onChange={(e) => setPasswordState(e.target.value)}
                    placeholder="Min. 6 karakter"
                    className="input-field"
                    style={{paddingLeft: '38px', paddingRight: '40px'}}
                  />
                  <button
                    type="button"
                    onClick={() => setIsPasswordVisible(!isPasswordVisible)}
                    style={{position:'absolute', right:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)', background:'none', border:'none', cursor:'pointer', padding:0, display:'flex', alignItems:'center'}}
                  >
                    {isPasswordVisible ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>
                {/* Password strength indicators */}
                {passwordState.length > 0 && (
                  <div style={{display:'flex', gap:'8px', marginTop:'8px', flexWrap:'wrap'}}>
                    {[
                      { check: hasUpper, label: 'A-Z Kapital' },
                      { check: hasLower, label: 'a-z Kecil' },
                      { check: hasDigit, label: '0-9 Angka' },
                      { check: passwordState.length >= 6, label: '6+ Karakter' },
                    ].map((item) => (
                      <span key={item.label} style={{
                        fontSize:'10px', fontWeight:'600', padding:'2px 7px', borderRadius:'9999px',
                        background: item.check ? 'var(--success-light)' : 'var(--bg-surface-2)',
                        color: item.check ? 'var(--success-text)' : 'var(--text-4)',
                        border: `1px solid ${item.check ? 'rgba(5,150,105,0.2)' : 'var(--border)'}`,
                        display:'inline-flex', alignItems:'center', gap:'3px'
                      }}>
                        {item.check ? <CheckCircle2 size={9} /> : null}
                        {item.label}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <button type="submit" disabled={loading} className="btn btn-primary btn-full btn-lg" style={{marginTop:'4px'}}>
                {loading ? <RefreshCw size={16} style={{animation:'spin 1s linear infinite'}} /> : 'Daftar & Masuk ke mcjob.id'}
              </button>
            </form>

            {/* Login Link */}
            <p style={{textAlign:'center', fontSize:'13px', color:'var(--text-3)', marginTop:'20px'}}>
              Sudah punya akun?{' '}
              <button
                type="button"
                onClick={() => setShowLoginDialog(true)}
                style={{color:'var(--primary)', fontWeight:'600', background:'none', border:'none', cursor:'pointer', fontSize:'13px'}}
              >
                Masuk sekarang
              </button>
            </p>

            {/* Security Footer */}
            <div style={{display:'flex', alignItems:'center', justifyContent:'center', gap:'6px', marginTop:'28px'}}>
              <ShieldCheck size={13} color="var(--success)" />
              <span style={{fontSize:'11px', color:'var(--text-4)'}}>
                Keamanan Data & Privasi Terenkripsi — Powered by @careermc.academy
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* ========== LOGIN DIALOG MODAL ========== */}
      {showLoginDialog && createPortal(
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowLoginDialog(false); }}>
          <div className="modal-panel animate-fade-in">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'20px'}}>
              <div>
                <h3 style={{fontSize:'18px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.015em'}}>Masuk ke Akun</h3>
                <p style={{fontSize:'12px', color:'var(--text-3)', marginTop:'2px'}}>Gunakan email & password yang sudah terdaftar.</p>
              </div>
              <button onClick={() => setShowLoginDialog(false)} className="btn btn-ghost btn-sm" style={{width:'32px', padding:0}}>
                <X size={16} />
              </button>
            </div>

            {loginErrorMsg && (
              <div style={{padding:'10px 14px', borderRadius:'10px', background:'var(--error-light)', border:'1px solid rgba(220,38,38,0.2)', marginBottom:'16px'}}>
                <p style={{fontSize:'12px', fontWeight:'600', color:'var(--error-text)'}}>{loginErrorMsg}</p>
              </div>
            )}

            <form onSubmit={handleLoginSubmit} style={{display:'flex', flexDirection:'column', gap:'14px'}}>
              <div>
                <label className="input-label">Email</label>
                <input type="email" required value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} placeholder="nama@email.com" className="input-field" />
              </div>
              <div>
                <label className="input-label">Password</label>
                <div style={{position:'relative'}}>
                  <input
                    type={isLoginPasswordVisible ? 'text' : 'password'}
                    required
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="Kata sandi Anda"
                    className="input-field"
                    style={{paddingRight:'40px'}}
                  />
                  <button type="button" onClick={() => setIsLoginPasswordVisible(!isLoginPasswordVisible)}
                    style={{position:'absolute', right:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)', background:'none', border:'none', cursor:'pointer', display:'flex', alignItems:'center'}}>
                    {isLoginPasswordVisible ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
              </div>

              <div style={{textAlign:'right'}}>
                <button type="button" onClick={() => { setShowLoginDialog(false); setShowResetDialog(true); }}
                  style={{fontSize:'12px', fontWeight:'600', color:'var(--primary)', background:'none', border:'none', cursor:'pointer'}}>
                  Lupa Password?
                </button>
              </div>

              <button type="submit" disabled={loading} className="btn btn-primary btn-full">
                {loading ? <RefreshCw size={15} style={{animation:'spin 1s linear infinite'}} /> : 'Masuk Sekarang'}
              </button>
            </form>
          </div>
        </div>,
        document.body
      )}

      {/* ========== RESET PASSWORD MODAL ========== */}
      {showResetDialog && createPortal(
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowResetDialog(false); }}>
          <div className="modal-panel animate-fade-in">
            <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'20px'}}>
              <div>
                <h3 style={{fontSize:'18px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.015em'}}>Reset Password</h3>
                <p style={{fontSize:'12px', color:'var(--text-3)', marginTop:'2px'}}>Masukkan email terdaftar untuk menerima tautan reset.</p>
              </div>
              <button onClick={() => setShowResetDialog(false)} className="btn btn-ghost btn-sm" style={{width:'32px', padding:0}}>
                <X size={16} />
              </button>
            </div>

            {resetStatus && (
              <div style={{padding:'10px 14px', borderRadius:'10px', marginBottom:'16px',
                background: resetStatus.startsWith('Gagal') ? 'var(--error-light)' : 'var(--success-light)',
                border: resetStatus.startsWith('Gagal') ? '1px solid rgba(220,38,38,0.2)' : '1px solid rgba(5,150,105,0.2)'
              }}>
                <p style={{fontSize:'12px', fontWeight:'600', color: resetStatus.startsWith('Gagal') ? 'var(--error-text)' : 'var(--success-text)'}}>
                  {resetStatus}
                </p>
              </div>
            )}

            <form onSubmit={handleResetSubmit} style={{display:'flex', flexDirection:'column', gap:'14px'}}>
              <div>
                <label className="input-label">Email Terdaftar</label>
                <input type="email" required value={resetEmail} onChange={(e) => setResetEmail(e.target.value)} placeholder="nama@email.com" className="input-field" />
              </div>
              <button type="submit" disabled={loading} className="btn btn-primary btn-full">
                {loading ? <RefreshCw size={15} style={{animation:'spin 1s linear infinite'}} /> : 'Kirim Tautan Reset'}
              </button>
            </form>
          </div>
        </div>,
        document.body
      )}

      <style>{`
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        @media (min-width: 1024px) { .lg\\:hidden-brand { display: none !important; } }
      `}</style>
    </>
  );
};
