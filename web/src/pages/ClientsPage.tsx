import React, { useState } from 'react';
import { Users, Plus, Phone, Mail, MapPin, Building, Trash2, Edit2, Search, Star, User } from 'lucide-react';
import type { Client } from '../types';
import { Modal } from '../components/Modal';
import { useAuth } from '../context/AuthContext';

interface ClientsPageProps {
  clients: Client[];
  onSaveClient: (client: Client) => Promise<void>;
  onDeleteClient: (id: string) => Promise<void>;
}

export const ClientsPage: React.FC<ClientsPageProps> = ({ clients, onSaveClient, onDeleteClient }) => {
  const { currentUser } = useAuth();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  // Form states
  const [name, setName] = useState('');
  const [type, setType] = useState('DIRECT_CLIENT');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const openModal = (client?: Client) => {
    if (client) {
      setEditingClient(client);
      setName(client.name);
      setType(client.type || 'DIRECT_CLIENT');
      setPhone(client.phone || '');
      setEmail(client.email || '');
      setAddress(client.address || '');
      setNotes(client.notes || '');
    } else {
      setEditingClient(null);
      setName('');
      setType('DIRECT_CLIENT');
      setPhone('');
      setEmail('');
      setAddress('');
      setNotes('');
    }
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const clientData: Client = {
        id: editingClient ? editingClient.id : `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUser?.uid || '',
        name,
        type: type as any,
        phone,
        email,
        address,
        notes,
        isFavorite: editingClient?.isFavorite ?? false,
        isArchived: editingClient?.isArchived ?? false,
        createdAt: editingClient ? editingClient.createdAt : new Date().toISOString()
      };
      await onSaveClient(clientData);
      setIsModalOpen(false);
    } catch (error) {
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  const filtered = clients.filter(c => 
    c.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    (c.phone && c.phone.includes(searchQuery))
  );

  return (
    <div className="animate-fade-in" style={{maxWidth:'1280px', margin:'0 auto', paddingBottom:'16px'}}>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
            <Users size={20} color="var(--primary)" />
            Database Klien & WO
          </h1>
          <p className="page-subtitle">Kelola kontak klien langsung, Wedding Organizer, dan Event Organizer.</p>
        </div>
        <button onClick={() => openModal()} className="btn btn-primary">
          <Plus size={15} /> Tambah Klien
        </button>
      </div>

      {/* Search */}
      <div className="card" style={{padding:'14px 16px', marginBottom:'20px'}}>
        <div style={{position:'relative'}}>
          <Search size={15} style={{position:'absolute', left:'12px', top:'50%', transform:'translateY(-50%)', color:'var(--text-4)'}} />
          <input
            type="text"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            placeholder="Cari nama klien atau nomor telepon..."
            className="input-field"
            style={{paddingLeft:'38px'}}
          />
        </div>
      </div>

      {/* Grid */}
      {filtered.length === 0 ? (
        <div className="card" style={{padding:0}}>
          <div className="empty-state">
            <div className="empty-state-icon"><Users size={22} /></div>
            <div>
              <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Belum ada kontak</p>
              <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto 16px'}}>Tambahkan kontak klien, WO, atau EO untuk memudahkan pengisian data saat menambah job baru.</p>
            </div>
            {!searchQuery && (
              <button onClick={() => openModal()} className="btn btn-primary btn-sm">
                <Plus size={14} /> Tambah Kontak
              </button>
            )}
          </div>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(300px, 1fr))', gap:'16px'}}>
          {filtered.map(c => (
            <div key={c.id} className="card" style={{padding:'20px', display:'flex', flexDirection:'column'}}>
              <div style={{display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:'16px'}}>
                <div style={{display:'flex', alignItems:'center', gap:'12px'}}>
                  <div style={{width:'40px', height:'40px', borderRadius:'12px', background: c.type === 'WO' ? 'var(--primary-light)' : 'var(--success-light)', display:'flex', alignItems:'center', justifyContent:'center'}}>
                    {c.type === 'WO' ? <Building size={18} color="var(--primary)" /> : <User size={18} color="var(--success)" />}
                  </div>
                  <div>
                    <h3 style={{fontSize:'15px', fontWeight:'700', color:'var(--text-1)', letterSpacing:'-0.01em', marginBottom:'2px'}}>{c.name}</h3>
                    <span className="badge badge-muted" style={{fontSize:'10px'}}>{c.type === 'WO' ? 'Wedding Organizer' : 'Klien Langsung'}</span>
                  </div>
                </div>
                <div style={{display:'flex', gap:'4px'}}>
                  <button onClick={() => openModal(c)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--text-4)'}}><Edit2 size={13} /></button>
                  <button onClick={() => onDeleteClient(c.id)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--error)'}}><Trash2 size={13} /></button>
                </div>
              </div>

              <div style={{display:'flex', flexDirection:'column', gap:'8px', flex:1}}>
                {c.phone && (
                  <div style={{display:'flex', alignItems:'center', gap:'8px', fontSize:'13px', color:'var(--text-2)'}}>
                    <Phone size={14} color="var(--text-4)" /> {c.phone}
                  </div>
                )}
                {c.email && (
                  <div style={{display:'flex', alignItems:'center', gap:'8px', fontSize:'13px', color:'var(--text-2)'}}>
                    <Mail size={14} color="var(--text-4)" /> {c.email}
                  </div>
                )}
                {c.address && (
                  <div style={{display:'flex', alignItems:'flex-start', gap:'8px', fontSize:'13px', color:'var(--text-2)'}}>
                    <MapPin size={14} color="var(--text-4)" style={{marginTop:'3px', flexShrink:0}} />
                    <span style={{lineHeight:'1.4'}}>{c.address}</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingClient ? "Edit Data Klien" : "Tambah Klien Baru"}>
        <form onSubmit={handleSave} style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          <div>
            <label className="input-label">Nama Klien / WO *</label>
            <input type="text" required value={name} onChange={e => setName(e.target.value)} className="input-field" placeholder="Masukkan nama" />
          </div>
          <div>
            <label className="input-label">Tipe Klien</label>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'8px'}}>
              <button
                type="button"
                onClick={() => setType('DIRECT_CLIENT')}
                style={{
                  padding:'10px', borderRadius:'10px', fontSize:'13px', fontWeight:'600',
                  border: `1px solid ${type === 'DIRECT_CLIENT' ? 'var(--primary)' : 'var(--border)'}`,
                  background: type === 'DIRECT_CLIENT' ? 'var(--primary-light)' : 'var(--bg-surface)',
                  color: type === 'DIRECT_CLIENT' ? 'var(--primary)' : 'var(--text-3)',
                }}
              >Klien Langsung</button>
              <button
                type="button"
                onClick={() => setType('WO')}
                style={{
                  padding:'10px', borderRadius:'10px', fontSize:'13px', fontWeight:'600',
                  border: `1px solid ${type === 'WO' ? 'var(--primary)' : 'var(--border)'}`,
                  background: type === 'WO' ? 'var(--primary-light)' : 'var(--bg-surface)',
                  color: type === 'WO' ? 'var(--primary)' : 'var(--text-3)',
                }}
              >Wedding Organizer</button>
            </div>
          </div>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
            <div>
              <label className="input-label">No. Telepon / WhatsApp</label>
              <input type="tel" value={phone} onChange={e => setPhone(e.target.value)} className="input-field" placeholder="08..." />
            </div>
            <div>
              <label className="input-label">Email</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="input-field" placeholder="nama@email.com" />
            </div>
          </div>
          <div>
            <label className="input-label">Alamat Domisili / Kantor</label>
            <textarea value={address} onChange={e => setAddress(e.target.value)} className="input-field" style={{height:'80px', padding:'10px 12px', resize:'none'}} placeholder="Detail alamat..." />
          </div>
          <div>
            <label className="input-label">Catatan Tambahan</label>
            <input type="text" value={notes} onChange={e => setNotes(e.target.value)} className="input-field" placeholder="Preferensi, karakter, dll." />
          </div>
          <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{marginTop:'8px'}}>
            {saving ? 'Menyimpan...' : 'Simpan Data Klien'}
          </button>
        </form>
      </Modal>
    </div>
  );
};
