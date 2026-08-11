import React, { useState } from 'react';
import { createPortal } from 'react-dom';
import { Plus, X } from 'lucide-react';

export interface FABItem {
  key: string;
  label: string;
  icon?: React.ComponentType<{ size?: number; color?: string }>;
  onClick: () => void;
  color?: string;
}

interface FABProps {
  onClick?: () => void;
  label?: string;
  icon?: React.ComponentType<{ size?: number; color?: string }>;
  items?: FABItem[];
}

export const FAB: React.FC<FABProps> = ({
  onClick,
  label = 'Tambah',
  icon: Icon = Plus,
  items
}) => {
  const [isOpen, setIsOpen] = useState(false);

  // Single Action FAB
  if (!items || items.length === 0) {
    return createPortal(
      <button
        onClick={onClick}
        aria-label={label}
        className="fab-button animate-fade-in"
      >
        <Icon size={20} color="#FFFFFF" />
        <span style={{ whiteSpace: 'nowrap' }}>{label}</span>
      </button>,
      document.body
    );
  }

  // Multi-Action Speed Dial FAB
  return createPortal(
    <>
      {/* Backdrop overlay when open */}
      {isOpen && (
        <div
          onClick={() => setIsOpen(false)}
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 95,
            background: 'rgba(15, 23, 42, 0.25)',
            backdropFilter: 'blur(2px)'
          }}
        />
      )}

      <div className="fab-container" style={{ zIndex: 100 }}>
        {/* Speed Dial Sub-Items */}
        {isOpen && (
          <div
            className="animate-fade-in"
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-end',
              gap: '10px',
              marginBottom: '6px'
            }}
          >
            {items.map(item => {
              const ItemIcon = item.icon || Plus;
              return (
                <div
                  key={item.key}
                  onClick={() => {
                    setIsOpen(false);
                    item.onClick();
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    cursor: 'pointer'
                  }}
                >
                  <span
                    style={{
                      fontSize: '13px',
                      fontWeight: '700',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-1)',
                      padding: '8px 14px',
                      borderRadius: '9999px',
                      boxShadow: '0 8px 20px rgba(0,0,0,0.18)',
                      border: '1px solid var(--border)',
                      whiteSpace: 'nowrap'
                    }}
                  >
                    {item.label}
                  </span>
                  <div
                    style={{
                      width: '44px',
                      height: '44px',
                      borderRadius: '50%',
                      background: item.color || 'var(--primary)',
                      color: '#FFFFFF',
                      boxShadow: '0 8px 20px rgba(0,0,0,0.2)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0
                    }}
                  >
                    <ItemIcon size={20} color="#FFFFFF" />
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Main Trigger Button */}
        <button
          onClick={() => setIsOpen(!isOpen)}
          aria-label={label}
          className="fab-button-inner"
        >
          {isOpen ? <X size={20} color="#FFFFFF" /> : <Icon size={20} color="#FFFFFF" />}
          <span style={{ whiteSpace: 'nowrap' }}>{isOpen ? 'Tutup' : label}</span>
        </button>
      </div>
    </>,
    document.body
  );
};
