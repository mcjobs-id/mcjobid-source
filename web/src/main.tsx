import React from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// ── Global Error Boundary ─────────────────────────────────────
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: string }
> {
  constructor(props: any) {
    super(props);
    this.state = { hasError: false, error: '' };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error: error?.message || 'Unknown error' };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error('App crashed:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '100vh', display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center', gap: '16px',
          background: '#f8f9ff', padding: '24px', textAlign: 'center'
        }}>
          <div style={{
            width: '56px', height: '56px', borderRadius: '16px',
            background: '#4F46E5', display: 'flex', alignItems: 'center',
            justifyContent: 'center', fontWeight: '800', color: 'white', fontSize: '20px'
          }}>MC</div>
          <div>
            <p style={{ fontSize: '16px', fontWeight: '700', color: '#1e1b4b', marginBottom: '8px' }}>
              mcjob.id — Gagal Memuat
            </p>
            <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '20px' }}>
              Terjadi kesalahan. Silakan refresh halaman ini.
            </p>
            <button
              onClick={() => window.location.reload()}
              style={{
                background: '#4F46E5', color: 'white', border: 'none',
                borderRadius: '10px', padding: '10px 24px', fontSize: '14px',
                fontWeight: '600', cursor: 'pointer'
              }}
            >
              🔄 Refresh Sekarang
            </button>
          </div>
          <p style={{ fontSize: '11px', color: '#9ca3af' }}>Error: {this.state.error}</p>
        </div>
      );
    }
    return this.props.children;
  }
}

createRoot(document.getElementById('root')!).render(
  <ErrorBoundary>
    <App />
  </ErrorBoundary>
)
