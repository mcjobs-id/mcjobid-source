import React, { useState } from 'react';
import { Users, Plus, Phone, Mail, MapPin, Building, Trash2, Edit2, Search, ArrowLeft, Check, ExternalLink, MessageSquare, User } from 'lucide-react';
import type { Client } from '../types';
import { useAuth } from '../context/AuthContext';
import { FAB } from '../components/FAB';

interface ClientsPageProps {
  clients: Client[];
  onSaveClient: (client: Client) => Promise<void>;
  onDeleteClient: (id: string) => Promise<void>;
  onBack?: () => void;
}

export const ClientsPage: React.FC<ClientsPageProps> = ({ clients, onSaveClient, onDeleteClient, onBack }) => {
  const { currentUser } = useAuth();

  // Page View Mode: 'list' (Grid Database) or 'form' (Dedicated Full Screen Form View)
  const [viewMode, setViewMode] = useState<'list' | 'form'>('list');
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  
  // Search & Filter
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState<'ALL' | 'DIRECT_CLIENT' | 'WO' | 'EO' | 'OTHER'>('ALL');
  const [toastMsg, setToastMsg] = useState('');

  // Form states
  const [name, setName] = useState('');
  const [type, setType] = useState<'DIRECT_CLIENT' | 'WO' | 'EO' | 'OTHER'>('DIRECT_CLIENT');
  const [company, setCompany] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [instagram, setInstagram] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const showToast = (msg: string) => {
    setToastMsg(msg);
    setTimeout(() => setToastMsg(''), 3000);
  };

  const openFormScreen = (client?: Client) => {
    if (client) {
      setEditingClient(client);
      setName(client.name);
      setType(client.type as any || 'DIRECT_CLIENT');
      setCompany(client.company || '');
      setPhone(client.phone || '');
      setEmail(client.email || '');
      setAddress(client.address || '');
      setInstagram(client.instagram || '');
      setNotes(client.notes || '');
    } else {
      setEditingClient(null);
      setName('');
      setType('DIRECT_CLIENT');
      setCompany('');
      setPhone('');
      setEmail('');
      setAddress('');
      setInstagram('');
      setNotes('');
    }
    setViewMode('form');
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setSaving(true);
    try {
      const clientData: Client = {
        ...(editingClient || {}), // Save existing fields (pic, totalBookings, etc)
        id: editingClient ? editingClient.id : `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUser?.uid || 'user_local',
        name: name.trim(),
        type: type,
        company: company.trim(),
        phone: phone.trim(),
        email: email.trim(),
        address: address.trim(),
        instagram: instagram.trim(),
        notes: notes.trim(),
        isFavorite: editingClient?.isFavorite ?? false,
        isArchived: editingClient?.isArchived ?? false,
        createdAt: editingClient ? editingClient.createdAt : new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      await onSaveClient(clientData);
      setViewMode('list');
      showToast(editingClient ? 'Data klien berhasil diperbarui!' : 'Klien baru berhasil ditambahkan!');
    } catch (error) {
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string, clientName: string) => {
    if (!confirm(`Hapus kontak "${clientName}" dari database?`)) return;
    try {
      await onDeleteClient(id);
      showToast('Kontak berhasil dihapus.');
    } catch (err) {
      console.error(err);
    }
  };

  const filtered = clients.filter(c => {
    const matchSearch = c.name.toLowerCase().includes(searchQuery.toLowerCase()) || (c.phone && c.phone.includes(searchQuery));
    const matchType = typeFilter === 'ALL' || c.type === typeFilter;
    return matchSearch && matchType;
  });

  // ── DEDICATED FULL SCREEN FORM VIEW MODE ──
  if (viewMode === 'form') {
    return (
      <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '40px' }}>
        
        {/* Form Page Header */}
        <div className="page-header" style={{ alignItems: 'center', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <button onClick={() => setViewMode('list')} className="btn btn-ghost" style={{ padding: '0 8px', marginLeft: '-8px' }}>
              <ArrowLeft size={18} />
            </button>
            <div>
              <h1 className="page-title">
                {editingClient ? 'Edit Data Klien & WO' : 'Tambah Klien & WO Baru'}
              </h1>
              <p className="page-subtitle">Isi formulir kontak klien untuk kemudahan integrasi acara & follow up.</p>
            </div>
          </div>
        </div>

        {/* Dedicated Form Card */}
        <div className="card" style={{ padding: '24px' }}>
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            
            <div>
              <label className="input-label">Nama Lengkap Klien / Organisasi WO *</label>
              <input
                type="text" required
                value={name}
                onChange={e => setName(e.target.value)}
                className="input-field"
                placeholder="Contoh: Bpk. Kevin & Vania / Mahkota WO"
              />
            </div>

            <div>
              <label className="input-label">Tipe Kategori Klien *</label>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setType('DIRECT_CLIENT')}
                  style={{
                    padding: '12px', borderRadius: '12px', fontSize: '13px', fontWeight: '800',
                    border: `1.5px solid ${type === 'DIRECT_CLIENT' ? 'var(--primary)' : 'var(--border)'}`,
                    background: type === 'DIRECT_CLIENT' ? 'rgba(79,70,229,0.08)' : 'var(--bg-surface-2)',
                    color: type === 'DIRECT_CLIENT' ? 'var(--primary)' : 'var(--text-3)',
                    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px'
                  }}
                >
                  <User size={16} /> Klien Langsung
                </button>
                <button
                  type="button"
                  onClick={() => setType('WO')}
                  style={{
                    padding: '12px', borderRadius: '12px', fontSize: '13px', fontWeight: '800',
                    border: `1.5px solid ${type === 'WO' ? 'var(--primary)' : 'var(--border)'}`,
                    background: type === 'WO' ? 'rgba(79,70,229,0.08)' : 'var(--bg-surface-2)',
                    color: type === 'WO' ? 'var(--primary)' : 'var(--text-3)',
                    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px'
                  }}
                >
                  <Building size={16} /> Wedding Organizer
                </button>
                <button
                  type="button"
                  onClick={() => setType('EO')}
                  style={{
                    padding: '12px', borderRadius: '12px', fontSize: '13px', fontWeight: '800',
                    border: `1.5px solid ${type === 'EO' ? 'var(--primary)' : 'var(--border)'}`,
                    background: type === 'EO' ? 'rgba(79,70,229,0.08)' : 'var(--bg-surface-2)',
                    color: type === 'EO' ? 'var(--primary)' : 'var(--text-3)',
                    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px'
                  }}
                >
                  <Users size={16} /> Event Organizer
                </button>
                <button
                  type="button"
                  onClick={() => setType('OTHER')}
                  style={{
                    padding: '12px', borderRadius: '12px', fontSize: '13px', fontWeight: '800',
                    border: `1.5px solid ${type === 'OTHER' ? 'var(--primary)' : 'var(--border)'}`,
                    background: type === 'OTHER' ? 'rgba(79,70,229,0.08)' : 'var(--bg-surface-2)',
                    color: type === 'OTHER' ? 'var(--primary)' : 'var(--text-3)',
                    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px'
                  }}
                >
                  <Building size={16} /> Lainnya / Agensi
                </button>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
              <div>
                <label className="input-label">Nama Perusahaan / Instansi</label>
                <input
                  type="text"
                  value={company}
                  onChange={e => setCompany(e.target.value)}
                  className="input-field"
                  placeholder="Contoh: PT. Maju Bersama / Mahkota WO"
                />
              </div>
              
              <div>
                <label className="input-label">Instagram</label>
                <div style={{ position: 'relative' }}>
                  <span style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-4)', fontSize: '14px', fontWeight: '600' }}>@</span>
                  <input
                    type="text"
                    value={instagram}
                    onChange={e => setInstagram(e.target.value)}
                    className="input-field"
                    placeholder="username_ig"
                    style={{ paddingLeft: '34px' }}
                  />
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
              <div>
                <label className="input-label">No. Telepon / WhatsApp</label>
                <input
                  type="tel"
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  className="input-field"
                  placeholder="081234567890"
                />
              </div>

              <div>
                <label className="input-label">Alamat Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="input-field"
                  placeholder="klien@email.com"
                />
              </div>
            </div>

            <div>
              <label className="input-label">Alamat Domisili / Kantor</label>
              <textarea
                value={address}
                onChange={e => setAddress(e.target.value)}
                className="input-field"
                style={{ height: '80px', padding: '10px 12px', resize: 'vertical' }}
                placeholder="Alamat rumah atau kantor WO..."
              />
            </div>

            <div>
              <label className="input-label">Catatan Tambahan (Opsional)</label>
              <input
                type="text"
                value={notes}
                onChange={e => setNotes(e.target.value)}
                className="input-field"
                placeholder="Karakter klien, preferensi acara, dll."
              />
            </div>

            <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
              <button type="button" onClick={() => setViewMode('list')} className="btn btn-secondary" style={{ flex: 1 }}>
                Batal
              </button>
              <button type="submit" disabled={saving} className="btn btn-primary" style={{ flex: 2, background: '#7C3AED', borderColor: '#7C3AED' }}>
                {saving ? 'Menyimpan...' : 'Simpan Klien'}
              </button>
            </div>

          </form>
        </div>

      </div>
    );
  }

  // ── GRID DATABASE LIST VIEW MODE ──
  return (
    <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '40px' }}>
      
      {/* Toast */}
      {toastMsg && (
        <div className="animate-fade-in" style={{
          position: 'fixed', top: '80px', left: '50%', transform: 'translateX(-50%)',
          zIndex: 9999, background: '#059669', color: 'white', padding: '10px 20px',
          borderRadius: '30px', fontWeight: '700', fontSize: '13px', boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
          display: 'flex', alignItems: 'center', gap: '8px'
        }}>
          <Check size={16} /> {toastMsg}
        </div>
      )}

      {/* Search & Filter Bar */}
      <div style={{ position: 'relative', width: '100%', marginBottom: '16px' }}>
        <Search size={15} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-4)' }} />
        <input
          type="text"
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          placeholder="Cari nama klien, WO, atau nomor telepon..."
          className="input-field"
          style={{ paddingLeft: '38px' }}
        />
      </div>

      {/* Filter Chips */}
      <div style={{ display: 'flex', gap: '6px', overflowX: 'auto', marginBottom: '20px' }} className="scrollbar-none">
        {[
          { id: 'ALL', label: 'Semua Kontak' },
          { id: 'DIRECT_CLIENT', label: 'Klien Langsung' },
          { id: 'WO', label: 'WO' },
          { id: 'EO', label: 'EO' },
          { id: 'OTHER', label: 'Lainnya' },
        ].map(f => (
          <button
            key={f.id}
            onClick={() => setTypeFilter(f.id as any)}
            className="badge"
            style={{
              cursor: 'pointer', padding: '8px 14px', fontSize: '12px', fontWeight: '700', borderRadius: '9999px',
              border: typeFilter === f.id ? '1px solid var(--primary)' : '1px solid var(--border)',
              background: typeFilter === f.id ? 'var(--primary)' : 'var(--bg-surface-2)',
              color: typeFilter === f.id ? '#FFFFFF' : 'var(--text-2)',
              transition: 'all 0.15s', whiteSpace: 'nowrap'
            }}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Grid List */}
      {filtered.length === 0 ? (
        <div className="card empty-state" style={{ padding: '56px 24px' }}>
          <div className="empty-state-icon"><Users size={24} /></div>
          <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)', marginTop: '8px' }}>Belum Ada Kontak</h3>
          <p style={{ fontSize: '13px', color: 'var(--text-3)', maxWidth: '320px', margin: '4px auto' }}>
            Tambahkan kontak klien atau WO menggunakan tombol Tambah Klien di atas untuk mengolah database kontak Anda.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {filtered.map(c => {
            let badgeBg = '#E6F4EA';
            let badgeColor = '#059669'; // DIRECT_CLIENT green
            let badgeLabel = 'Klien Langsung';
            let IconComponent = User;
            let borderColor = '#059669';
            let iconBg = 'rgba(5,150,105,0.12)';

            if (c.type === 'WO') {
              badgeBg = '#F5F3FF';
              badgeColor = '#7C3AED';
              badgeLabel = 'Wedding Organizer';
              IconComponent = Building;
              borderColor = '#7C3AED';
              iconBg = 'rgba(124,58,237,0.12)';
            } else if (c.type === 'EO') {
              badgeBg = '#EFF6FF';
              badgeColor = '#2563EB'; // Blue
              badgeLabel = 'Event Organizer';
              IconComponent = Users;
              borderColor = '#2563EB';
              iconBg = 'rgba(37,99,235,0.12)';
            } else if (c.type === 'OTHER') {
              badgeBg = '#F3F4F6';
              badgeColor = '#4B5563'; // Gray
              badgeLabel = 'Lainnya / Agensi';
              IconComponent = Building;
              borderColor = '#4B5563';
              iconBg = 'rgba(75,85,99,0.12)';
            }

            const cleanPhone = (c.phone || '').replace(/\D/g, '');
            const waLink = cleanPhone ? `https://wa.me/${cleanPhone.startsWith('0') ? '62' + cleanPhone.slice(1) : cleanPhone}` : '';

            return (
              <div key={c.id} className="card hover-scale" style={{ padding: '20px', display: 'flex', flexDirection: 'column', borderLeft: `5px solid ${borderColor}` }}>
                
                {/* Header Row */}
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '14px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                      width: '42px', height: '42px', borderRadius: '12px',
                      background: iconBg,
                      color: badgeColor,
                      display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
                    }}>
                      <IconComponent size={20} />
                    </div>

                    <div>
                      <h3 style={{ fontSize: '15.5px', fontWeight: '900', color: 'var(--text-1)', letterSpacing: '-0.01em', marginBottom: '2px' }}>{c.name}</h3>
                      {c.company && (
                        <div style={{ fontSize: '12px', color: 'var(--text-2)', fontWeight: '600', marginBottom: '4px' }}>
                          🏢 {c.company}
                        </div>
                      )}
                      <span className="badge" style={{ fontSize: '10.5px', fontWeight: '700', background: badgeBg, color: badgeColor }}>
                        {badgeLabel}
                      </span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '4px' }}>
                    <button onClick={() => openFormScreen(c)} className="btn btn-ghost btn-sm" style={{ padding: '4px', color: 'var(--text-3)' }} title="Edit Kontak">
                      <Edit2 size={15} />
                    </button>
                    <button onClick={() => handleDelete(c.id, c.name)} className="btn btn-ghost btn-sm" style={{ padding: '4px', color: 'var(--error)' }} title="Hapus Kontak">
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>

                {/* Info Fields */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1, fontSize: '13px' }}>
                  {c.phone && (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <span style={{ color: 'var(--text-3)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Phone size={14} color="var(--primary)" /> {c.phone}
                      </span>
                      {waLink && (
                        <a href={waLink} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm" style={{ padding: '2px 8px', fontSize: '11px', gap: '4px', color: '#25D366' }}>
                          <MessageSquare size={12} /> Chat WA
                        </a>
                      )}
                    </div>
                  )}

                  {c.email && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-2)' }}>
                      <Mail size={14} color="var(--text-4)" /> {c.email}
                    </div>
                  )}

                  {c.instagram && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-2)' }}>
                      <span style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-4)', width: '14px', textAlign: 'center' }}>@</span>
                      <a href={`https://instagram.com/${c.instagram.replace('@', '')}`} target="_blank" rel="noopener noreferrer" style={{ color: '#E1306C', textDecoration: 'none' }}>
                        {c.instagram}
                      </a>
                    </div>
                  )}

                  {c.address && (
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '6px', color: 'var(--text-2)' }}>
                      <MapPin size={14} color="var(--text-4)" style={{ marginTop: '2px', flexShrink: 0 }} />
                      <span style={{ lineHeight: '1.4' }}>{c.address}</span>
                    </div>
                  )}

                  {c.notes && (
                    <div style={{ marginTop: '6px', padding: '8px 10px', borderRadius: '8px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)', fontSize: '12px', color: 'var(--text-2)' }}>
                      📝 {c.notes}
                    </div>
                  )}
                </div>

              </div>
            );
          })}
        </div>
      )}

      {/* ── FLOATING ACTION BUTTON ── */}
      {viewMode === 'list' && (
        <FAB onClick={() => openFormScreen()} label="Tambah Klien" color="#7C3AED" />
      )}
    </div>
  );
};
