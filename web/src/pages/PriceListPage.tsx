import React, { useState } from 'react';
import { ArrowLeft, Plus, Tag, Trash2, Edit2, Copy, CheckCircle2 } from 'lucide-react';
import type { RateCard } from '../types';
import { useAuth } from '../context/AuthContext';
import { Modal } from '../components/Modal';
import { saveRateCard, deleteRateCard } from '../services/firebaseService';

interface PriceListPageProps {
  onBack: () => void;
  rateCards: RateCard[];
}

export const PriceListPage: React.FC<PriceListPageProps> = ({ onBack, rateCards }) => {
  const { currentUser } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<RateCard | null>(null);
  
  const [name, setName] = useState('');
  const [price, setPrice] = useState<number | ''>('');
  const [features, setFeatures] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const [copiedId, setCopiedId] = useState<string | null>(null);

  const openModal = (card?: RateCard) => {
    if (card) {
      setEditingCard(card);
      setName(card.name);
      setPrice(card.price);
      setFeatures(card.features.join('\n'));
      setNotes(card.notes || '');
    } else {
      setEditingCard(null);
      setName('');
      setPrice('');
      setFeatures('');
      setNotes('');
    }
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser || price === '') return;
    setSaving(true);
    
    try {
      const cardData: RateCard = {
        id: editingCard ? editingCard.id : Date.now().toString(),
        ownerId: currentUser.uid,
        name,
        price: Number(price),
        features: features.split('\n').filter(f => f.trim() !== ''),
        notes,
        createdAt: editingCard ? editingCard.createdAt : new Date().toISOString(),
      };
      await saveRateCard(currentUser.uid, cardData);
      setIsModalOpen(false);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!currentUser || !confirm('Hapus paket harga ini?')) return;
    try {
      await deleteRateCard(currentUser.uid, id);
    } catch (err) {
      console.error(err);
    }
  };

  const copyToClipboard = (card: RateCard) => {
    const text = `PAKET MC: ${card.name}\nHarga: Rp ${card.price.toLocaleString('id-ID')}\n\nFasilitas:\n${card.features.map(f => `- ${f}`).join('\n')}\n\n${card.notes ? `Catatan: ${card.notes}` : ''}`;
    navigator.clipboard.writeText(text);
    setCopiedId(card.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="animate-fade-in" style={{maxWidth:'1000px', margin:'0 auto', paddingBottom:'24px'}}>
      
      {/* ── HEADER ── */}
      <div className="page-header" style={{alignItems:'center'}}>
        <div style={{display:'flex', alignItems:'center', gap:'16px'}}>
          <button onClick={onBack} className="btn btn-ghost" style={{padding:'0 8px', marginLeft:'-8px'}}>
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="page-title" style={{display:'flex', alignItems:'center', gap:'8px'}}>
              <Tag size={20} color="var(--primary)" /> Rate Card & Price List
            </h1>
            <p className="page-subtitle">Kelola paket harga MC dan bagikan ke klien dengan mudah.</p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn btn-primary">
          <Plus size={15} /> Tambah Paket
        </button>
      </div>

      {/* ── GRID ── */}
      {rateCards.length === 0 ? (
        <div className="card empty-state" style={{padding:'56px 24px'}}>
          <div className="empty-state-icon"><Tag size={24} /></div>
          <div>
            <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Belum ada paket harga</p>
            <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto'}}>Buat paket harga (contoh: Wedding Gold, Corporate Event) untuk memudahkan penawaran ke klien.</p>
          </div>
          <button onClick={() => openModal()} className="btn btn-primary btn-sm" style={{marginTop:'8px'}}>
            <Plus size={14} /> Buat Paket Pertama
          </button>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(280px, 1fr))', gap:'20px'}}>
          {rateCards.map((card) => (
            <div key={card.id} className="card" style={{display:'flex', flexDirection:'column', padding:0, overflow:'hidden', position:'relative'}}>
              {/* Card Header (Gradient) */}
              <div style={{padding:'20px 24px', background:'var(--primary-light)', borderBottom:'1px solid rgba(79,70,229,0.1)'}}>
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'12px'}}>
                  <h3 style={{fontSize:'16px', fontWeight:'800', color:'var(--primary-text)', letterSpacing:'-0.01em', paddingRight:'40px'}}>{card.name}</h3>
                  <div style={{display:'flex', gap:'4px', position:'absolute', top:'16px', right:'16px'}}>
                    <button onClick={() => openModal(card)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--primary)'}}><Edit2 size={14} /></button>
                    <button onClick={() => handleDelete(card.id)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--error)'}}><Trash2 size={14} /></button>
                  </div>
                </div>
                <div>
                  <span style={{fontSize:'24px', fontWeight:'800', color:'var(--primary)', fontVariantNumeric:'tabular-nums', letterSpacing:'-0.03em'}}>
                    Rp {card.price.toLocaleString('id-ID')}
                  </span>
                </div>
              </div>

              {/* Card Body */}
              <div style={{padding:'20px 24px', flex:1, display:'flex', flexDirection:'column'}}>
                <ul style={{listStyle:'none', padding:0, margin:0, display:'flex', flexDirection:'column', gap:'12px', flex:1}}>
                  {card.features.map((f, i) => (
                    <li key={i} style={{display:'flex', alignItems:'flex-start', gap:'10px', fontSize:'13px', color:'var(--text-2)'}}>
                      <CheckCircle2 size={16} color="var(--success)" style={{flexShrink:0, marginTop:'2px'}} />
                      <span style={{lineHeight:'1.5'}}>{f}</span>
                    </li>
                  ))}
                </ul>
                
                {card.notes && (
                  <div style={{marginTop:'20px', paddingTop:'16px', borderTop:'1px dashed var(--border)', fontSize:'12px', color:'var(--text-3)', fontStyle:'italic'}}>
                    * {card.notes}
                  </div>
                )}
              </div>

              {/* Card Footer */}
              <div style={{padding:'16px 24px', borderTop:'1px solid var(--border)', background:'var(--bg-surface-2)'}}>
                <button 
                  onClick={() => copyToClipboard(card)}
                  className="btn btn-secondary btn-full btn-sm"
                  style={{color: copiedId === card.id ? 'var(--success)' : 'var(--text-2)', borderColor: copiedId === card.id ? 'var(--success)' : 'var(--border)'}}
                >
                  {copiedId === card.id ? <><CheckCircle2 size={14} /> Tersalin</> : <><Copy size={14} /> Salin Text Penawaran</>}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── MODAL ── */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingCard ? "Edit Paket Harga" : "Tambah Paket Harga"}>
        <form onSubmit={handleSave} style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          <div>
            <label className="input-label">Nama Paket *</label>
            <input type="text" required value={name} onChange={e => setName(e.target.value)} className="input-field" placeholder="Contoh: Gold Wedding Package" />
          </div>
          <div>
            <label className="input-label">Harga (Rp) *</label>
            <input type="number" required value={price} onChange={e => setPrice(Number(e.target.value))} className="input-field" placeholder="0" style={{fontWeight:'700', color:'var(--primary)'}} />
          </div>
          <div>
            <label className="input-label">Fasilitas / Include (1 per baris) *</label>
            <textarea 
              required 
              value={features} 
              onChange={e => setFeatures(e.target.value)} 
              className="input-field" 
              style={{height:'120px', padding:'10px 12px', resize:'none', lineHeight:'1.6'}} 
              placeholder="MC Resepsi (Max 3 Jam)\nMeeting H-7\nFree Konsultasi Rundown" 
            />
            <p className="input-hint">Pisahkan setiap fasilitas dengan enter (baris baru).</p>
          </div>
          <div>
            <label className="input-label">Catatan Tambahan (S&K)</label>
            <input type="text" value={notes} onChange={e => setNotes(e.target.value)} className="input-field" placeholder="Contoh: Harga belum termasuk transport luar kota" />
          </div>
          
          <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{marginTop:'8px'}}>
            {saving ? 'Menyimpan...' : 'Simpan Paket Harga'}
          </button>
        </form>
      </Modal>
    </div>
  );
};
