import React from 'react';
import { Home, Calendar, Users, DollarSign, Grid } from 'lucide-react';
import type { TabType } from './Sidebar';

interface BottomNavProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
}

const tabs = [
  { id: 'home',    label: 'Beranda',  icon: Home },
  { id: 'agenda',  label: 'Agenda',   icon: Calendar },
  { id: 'clients', label: 'Klien',    icon: Users },
  { id: 'finance', label: 'Keuangan', icon: DollarSign },
  { id: 'more',    label: 'Lainnya',  icon: Grid },
];

export const BottomNav: React.FC<BottomNavProps> = ({ activeTab, onChangeTab }) => {
  return (
    <nav
      className="glass-bottom"
      style={{
        position: 'fixed', bottom: 0, left: 0, right: 0,
        zIndex: 40, padding: '6px 8px 8px',
        display: 'flex', alignItems: 'stretch',
        gap: '4px',
      }}
    >
      {tabs.map(({ id, label, icon: Icon }) => {
        const isActive = activeTab === id;
        return (
          <button
            key={id}
            onClick={() => onChangeTab(id as TabType)}
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '3px',
              padding: '6px 4px',
              borderRadius: '10px',
              border: 'none',
              background: isActive ? 'var(--primary-light)' : 'transparent',
              cursor: 'pointer',
              transition: 'all 0.15s ease',
              WebkitTapHighlightColor: 'transparent',
            }}
          >
            <Icon
              size={20}
              style={{
                color: isActive ? 'var(--primary)' : 'var(--text-4)',
                transition: 'color 0.15s ease',
              }}
            />
            <span style={{
              fontSize: '9.5px',
              fontWeight: isActive ? '700' : '500',
              color: isActive ? 'var(--primary)' : 'var(--text-4)',
              letterSpacing: '0.01em',
              transition: 'all 0.15s ease',
              lineHeight: 1,
            }}>
              {label}
            </span>
          </button>
        );
      })}
    </nav>
  );
};
