import React from 'react';
import { createPortal } from 'react-dom';
import { AlertCircle, HelpCircle, Trash2 } from 'lucide-react';

interface ConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'primary' | 'warning';
  loading?: boolean;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Konfirmasi Tindakan',
  message,
  confirmText = 'Ya, Lanjutkan',
  cancelText = 'Batal',
  type = 'primary',
  loading = false
}) => {
  if (!isOpen) return null;

  const isDanger = type === 'danger';
  const isWarning = type === 'warning';

  const iconBg = isDanger ? 'var(--error-light)' : isWarning ? 'rgba(217,119,6,0.1)' : 'var(--primary-light)';
  const iconColor = isDanger ? 'var(--error)' : isWarning ? '#D97706' : 'var(--primary)';
  const btnBg = isDanger ? 'var(--error)' : 'var(--primary)';

  return createPortal(
    <div
      className="modal-overlay"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(15, 23, 42, 0.6)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
        zIndex: 9999
      }}
    >
      <div
        className="animate-fade-in"
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '100%',
          maxWidth: '400px',
          background: 'var(--bg-surface)',
          borderRadius: 'var(--radius-xl)',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-xl)',
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          textAlign: 'center',
          gap: '16px'
        }}
      >
        {/* Icon Header */}
        <div style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          background: iconBg,
          color: iconColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          {isDanger ? <Trash2 size={26} /> : isWarning ? <AlertCircle size={26} /> : <HelpCircle size={26} />}
        </div>

        {/* Text Content */}
        <div>
          <h3 style={{ fontSize: '17px', fontWeight: '800', color: 'var(--text-1)', marginBottom: '6px', letterSpacing: '-0.015em' }}>
            {title}
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-3)', lineHeight: '1.5', margin: 0 }}>
            {message}
          </p>
        </div>

        {/* Buttons Row */}
        <div style={{ display: 'flex', gap: '10px', width: '100%', marginTop: '8px' }}>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="btn btn-secondary btn-full"
            style={{ flex: 1, padding: '10px 16px', fontSize: '13px', fontWeight: '600' }}
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={() => {
              onConfirm();
              onClose();
            }}
            disabled={loading}
            className="btn btn-primary btn-full"
            style={{ flex: 1, padding: '10px 16px', fontSize: '13px', fontWeight: '700', background: btnBg, borderColor: btnBg }}
          >
            {loading ? 'Memproses...' : confirmText}
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
};
