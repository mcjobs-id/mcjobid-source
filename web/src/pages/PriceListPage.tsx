import React, { useState } from 'react';
import { ArrowLeft, Plus, Tag, Trash2, Edit2, Copy, CheckCircle2, Share2, Search, MessageSquare, ExternalLink, Calendar, User } from 'lucide-react';
import type { RateCard } from '../types';
import { useAuth } from '../context/AuthContext';
import { Modal } from '../components/Modal';

interface PriceListPageProps {
  onBack: () => void;
  rateCards: RateCard[];
  onSaveRateCard?: (card: RateCard) => Promise<void>;
  onDeleteRateCard?: (id: string) => Promise<void>;
  onUseForJob?: (card: RateCard) => void;
}

const CATEGORIES = ['Semua', 'Wedding', 'Corporate', 'Private Event', 'Government', 'Lainnya'];

export const PriceListPage: React.FC<PriceListPageProps> = ({
  onBack,
  rateCards,
  onSaveRateCard,
  onDeleteRateCard,
  onUseForJob
}) => {
  const { currentUser, userProfile } = useAuth();
  
  // Filtering states
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Semua');

  // Add/Edit Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCard, setEditingCard] = useState<RateCard | null>(null);
  
  const [name, setName] = useState('');
  const [category, setCategory] = useState('Wedding');
  const [price, setPrice] = useState<number | ''>('');
  const [durationHours, setDurationHours] = useState<number | ''>(3);
  const [features, setFeatures] = useState('');
  const [addOns, setAddOns] = useState('');
  const [terms, setTerms] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  // WhatsApp Quote Modal state
  const [shareModalCard, setShareModalCard] = useState<RateCard | null>(null);
  const [clientName, setClientName] = useState('');
  const [eventDate, setEventDate] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const openModal = (card?: RateCard) => {
    if (card) {
      setEditingCard(card);
      setName(card.name || card.title || '');
      setCategory(card.category || 'Wedding');
      setPrice(card.price);
      setDurationHours(card.durationHours || 3);
      setFeatures((card.inclusions || card.features || []).join('\n'));
      setAddOns((card.addOns || []).join('\n'));
      setTerms(card.terms || '');
      setNotes(card.notes || card.description || '');
    } else {
      setEditingCard(null);
      setName('');
      setCategory('Wedding');
      setPrice('');
      setDurationHours(3);
      setFeatures('');
      setAddOns('');
      setTerms('');
      setNotes('');
    }
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser || price === '') return;
    setSaving(true);
    try {
      const featureList = features.split('\n').filter(f => f.trim() !== '');
      const addOnList = addOns.split('\n').filter(a => a.trim() !== '');
      
      const cardData: RateCard = {
        id: editingCard ? editingCard.id : `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: currentUser.uid,
        name,
        title: name,
        category,
        price: Number(price),
        durationHours: Number(durationHours) || 3,
        duration: `${durationHours || 3} Jam`,
        features: featureList,
        inclusions: featureList,
        addOns: addOnList,
        terms,
        notes,
        description: notes,
        createdAt: editingCard ? editingCard.createdAt : new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      if (onSaveRateCard) {
        await onSaveRateCard(cardData);
      }
      setIsModalOpen(false);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Hapus paket harga ini?')) return;
    try {
      if (onDeleteRateCard) await onDeleteRateCard(id);
    } catch (err) {
      console.error(err);
    }
  };

  // Build formatted WA Quote string (Android logic)
  const generateWaQuoteText = (card: RateCard, cName: string, eDate: string) => {
    const mcName = userProfile?.displayName || 'MC Professional';
    const inclusions = (card.inclusions || card.features || []);
    const addOnsList = (card.addOns || []);
    
    let text = `*PENAWARAN HARGA MC - ${mcName.toUpperCase()}*\n\n`;
    if (cName) text += `Kepada Yth. *${cName}*\n`;
    if (eDate) text += `Rencana Acara: *${eDate}*\n\n`;
    
    text += `📌 *PAKET: ${card.name || card.title}*\n`;
    text += `🏷️ Kategori: ${card.category || 'Event'}\n`;
    text += `⏱️ Durasi: ${card.durationHours || 3} Jam\n`;
    text += `💰 *Investasi: Rp ${card.price.toLocaleString('id-ID')}*\n\n`;

    if (inclusions.length > 0) {
      text += `*Fasilitas / Included:*\n`;
      inclusions.forEach(inc => {
        text += `✓ ${inc}\n`;
      });
      text += `\n`;
    }

    if (addOnsList.length > 0) {
      text += `💡 *Opsi Tambahan (Add-ons):*\n`;
      addOnsList.forEach(ao => {
        text += `+ ${ao}\n`;
      });
      text += `\n`;
    }

    if (card.terms) {
      text += `📜 *Ketentuan Booking:*\n${card.terms}\n\n`;
    }

    if (card.notes) {
      text += `💬 *Catatan:* ${card.notes}\n\n`;
    }

    text += `Terima kasih! Silakan hubungi kami kembali untuk konfirmasi tanggal. 🙏`;
    return text;
  };

  const handleCopyWaText = () => {
    if (!shareModalCard) return;
    const txt = generateWaQuoteText(shareModalCard, clientName, eventDate);
    navigator.clipboard.writeText(txt);
    setCopiedId(shareModalCard.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleOpenWaLink = () => {
    if (!shareModalCard) return;
    const txt = generateWaQuoteText(shareModalCard, clientName, eventDate);
    const encoded = encodeURIComponent(txt);
    window.open(`https://api.whatsapp.com/send?text=${encoded}`, '_blank');
  };

  // Filtered rate cards
  const filteredRateCards = rateCards.filter(card => {
    const matchesCategory = selectedCategory === 'Semua' || card.category === selectedCategory;
    const q = searchQuery.toLowerCase();
    const matchesSearch = !q || 
      (card.name || card.title || '').toLowerCase().includes(q) ||
      (card.description || card.notes || '').toLowerCase().includes(q) ||
      (card.inclusions || card.features || []).some(f => f.toLowerCase().includes(q));
    return matchesCategory && matchesSearch;
  });

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
            <p className="page-subtitle">Katalog paket harga MC & generator penawaran langsung ke WhatsApp klien.</p>
          </div>
        </div>
        <button onClick={() => openModal()} className="btn btn-primary">
          <Plus size={15} /> Buat Paket Baru 🚀
        </button>
      </div>

      {/* ── SEARCH & CATEGORY CHIPS ── */}
      <div style={{display:'flex', flexDirection:'column', gap:'12px', marginBottom:'24px'}}>
        <div style={{position:'relative', width:'100%'}}>
          <Search size={16} color="var(--text-3)" style={{position:'absolute', left:'14px', top:'50%', transform:'translateY(-50%)'}} />
          <input
            type="text"
            className="input-field"
            style={{paddingLeft:'38px'}}
            placeholder="Cari nama paket, fasilitas, atau kata kunci..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
        
        <div style={{display:'flex', gap:'8px', overflowX:'auto', paddingBottom:'4px'}}>
          {CATEGORIES.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`badge ${selectedCategory === cat ? 'badge-primary' : 'badge-neutral'}`}
              style={{
                cursor:'pointer', padding:'8px 14px', fontSize:'13px', borderRadius:'9999px',
                border: selectedCategory === cat ? 'none' : '1px solid var(--border)',
                background: selectedCategory === cat ? 'var(--primary)' : 'var(--bg-surface)'
              }}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* ── GRID ── */}
      {filteredRateCards.length === 0 ? (
        <div className="card empty-state" style={{padding:'56px 24px'}}>
          <div className="empty-state-icon"><Tag size={24} /></div>
          <div>
            <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)', marginBottom:'4px'}}>Tidak ada paket harga</p>
            <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'0 auto'}}>
              {searchQuery || selectedCategory !== 'Semua' 
                ? 'Tidak ada paket yang sesuai dengan filter atau kata kunci pencarian Anda.'
                : 'Buat paket harga pertama (contoh: Gold Wedding, Corporate Event) untuk memudahkan penawaran ke klien.'}
            </p>
          </div>
          <button onClick={() => openModal()} className="btn btn-primary btn-sm" style={{marginTop:'8px'}}>
            <Plus size={14} /> Buat Paket Baru
          </button>
        </div>
      ) : (
        <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(300px, 1fr))', gap:'20px'}}>
          {filteredRateCards.map((card) => (
            <div key={card.id} className="card" style={{display:'flex', flexDirection:'column', padding:0, overflow:'hidden', position:'relative'}}>
              {/* Card Header (Gradient) */}
              <div style={{padding:'20px 24px', background:'var(--primary-light)', borderBottom:'1px solid rgba(79,70,229,0.1)'}}>
                <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'8px'}}>
                  <span className="badge badge-primary" style={{fontSize:'11px', textTransform:'uppercase'}}>
                    {card.category || 'Wedding'}
                  </span>
                  <div style={{display:'flex', gap:'4px'}}>
                    <button onClick={() => openModal(card)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--primary)'}}><Edit2 size={14} /></button>
                    <button onClick={() => handleDelete(card.id)} className="btn btn-ghost btn-sm" style={{padding:0, width:'28px', height:'28px', color:'var(--error)'}}><Trash2 size={14} /></button>
                  </div>
                </div>

                <h3 style={{fontSize:'17px', fontWeight:'800', color:'var(--text-1)', letterSpacing:'-0.01em', marginBottom:'4px'}}>
                  {card.name || card.title}
                </h3>

                <div style={{display:'flex', alignItems:'baseline', gap:'8px'}}>
                  <span style={{fontSize:'22px', fontWeight:'800', color:'var(--primary)', fontVariantNumeric:'tabular-nums'}}>
                    Rp {card.price.toLocaleString('id-ID')}
                  </span>
                  <span style={{fontSize:'12px', color:'var(--text-3)'}}>
                    / {card.durationHours || 3} Jam
                  </span>
                </div>
              </div>

              {/* Card Body */}
              <div style={{padding:'20px 24px', flex:1, display:'flex', flexDirection:'column'}}>
                <p style={{fontSize:'12px', fontWeight:'600', color:'var(--text-3)', textTransform:'uppercase', letterSpacing:'0.05em', marginBottom:'10px'}}>Fasilitas Termasuk:</p>
                <ul style={{listStyle:'none', padding:0, margin:0, display:'flex', flexDirection:'column', gap:'10px', flex:1}}>
                  {(card.inclusions || card.features || []).map((f, i) => (
                    <li key={i} style={{display:'flex', alignItems:'flex-start', gap:'10px', fontSize:'13px', color:'var(--text-2)'}}>
                      <CheckCircle2 size={15} color="var(--success)" style={{flexShrink:0, marginTop:'2px'}} />
                      <span style={{lineHeight:'1.4'}}>{f}</span>
                    </li>
                  ))}
                </ul>

                {card.addOns && card.addOns.length > 0 && (
                  <div style={{marginTop:'16px', paddingTop:'12px', borderTop:'1px dashed var(--border)'}}>
                    <p style={{fontSize:'11px', fontWeight:'700', color:'var(--primary)', marginBottom:'6px'}}>Opsi Tambahan (Add-ons):</p>
                    <div style={{display:'flex', flexWrap:'wrap', gap:'4px'}}>
                      {card.addOns.map((ao, idx) => (
                        <span key={idx} style={{fontSize:'11px', background:'rgba(79,70,229,0.08)', color:'var(--primary)', padding:'2px 8px', borderRadius:'6px'}}>
                          + {ao}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                
                {card.notes && (
                  <div style={{marginTop:'16px', paddingTop:'12px', borderTop:'1px dashed var(--border)', fontSize:'12px', color:'var(--text-3)', fontStyle:'italic'}}>
                    * {card.notes}
                  </div>
                )}
              </div>

              {/* Card Footer Actions */}
              <div style={{padding:'14px 20px', borderTop:'1px solid var(--border)', background:'var(--bg-surface-2)', display:'grid', gridTemplateColumns:'1fr 1fr', gap:'8px'}}>
                <button 
                  onClick={() => setShareModalCard(card)}
                  className="btn btn-secondary btn-sm"
                  style={{justifyContent:'center', gap:'6px'}}
                >
                  <Share2 size={14} color="#2563EB" /> Quote WA
                </button>

                <button 
                  onClick={() => onUseForJob && onUseForJob(card)}
                  className="btn btn-primary btn-sm"
                  style={{justifyContent:'center', gap:'6px'}}
                >
                  Gunakan Job 🚀
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── MODAL ADD / EDIT ── */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingCard ? "Edit Paket Harga" : "Buat Paket Harga Baru"}>
        <form onSubmit={handleSave} style={{display:'flex', flexDirection:'column', gap:'16px'}}>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
            <div>
              <label className="input-label">Nama Paket *</label>
              <input type="text" required value={name} onChange={e => setName(e.target.value)} className="input-field" placeholder="Contoh: Gold Wedding" />
            </div>
            <div>
              <label className="input-label">Kategori</label>
              <select value={category} onChange={e => setCategory(e.target.value)} className="input-field">
                {CATEGORIES.filter(c => c !== 'Semua').map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
          </div>

          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
            <div>
              <label className="input-label">Harga (Rp) *</label>
              <input type="number" required value={price} onChange={e => setPrice(Number(e.target.value))} className="input-field" placeholder="0" style={{fontWeight:'700', color:'var(--primary)'}} />
            </div>
            <div>
              <label className="input-label">Durasi Acara (Jam)</label>
              <input type="number" step="0.5" value={durationHours} onChange={e => setDurationHours(Number(e.target.value))} className="input-field" placeholder="3" />
            </div>
          </div>

          <div>
            <label className="input-label">Fasilitas / Inklusi (1 per baris) *</label>
            <textarea 
              required 
              value={features} 
              onChange={e => setFeatures(e.target.value)} 
              className="input-field" 
              style={{height:'100px', padding:'10px 12px', resize:'none', lineHeight:'1.5'}} 
              placeholder="MC Resepsi (Max 3 Jam)\nMeeting Koordinasi H-7\nFree Konsultasi Rundown" 
            />
            <p className="input-hint">Pisahkan setiap poin fasilitas dengan tekan enter (baris baru).</p>
          </div>

          <div>
            <label className="input-label">Opsi Tambahan / Add-ons (Optional)</label>
            <textarea 
              value={addOns} 
              onChange={e => setAddOns(e.target.value)} 
              className="input-field" 
              style={{height:'60px', padding:'8px 12px', resize:'none', lineHeight:'1.5'}} 
              placeholder="MC Akad Tambahan (+Rp 500.000)\nOvertime per jam (+Rp 300.000)" 
            />
          </div>

          <div>
            <label className="input-label">Syarat & Ketentuan Booking</label>
            <input type="text" value={terms} onChange={e => setTerms(e.target.value)} className="input-field" placeholder="DP 30% untuk kunci tanggal, Pelunasan H-3" />
          </div>

          <div>
            <label className="input-label">Catatan Khusus</label>
            <input type="text" value={notes} onChange={e => setNotes(e.target.value)} className="input-field" placeholder="Harga belum termasuk transport luar kota" />
          </div>
          
          <button type="submit" disabled={saving} className="btn btn-primary btn-full btn-lg" style={{marginTop:'8px'}}>
            {saving ? 'Menyimpan Paket...' : 'Simpan Paket Harga 🚀'}
          </button>
        </form>
      </Modal>

      {/* ── MODAL WA QUOTE GENERATOR ── */}
      {shareModalCard && (
        <Modal isOpen={!!shareModalCard} onClose={() => setShareModalCard(null)} title="Bagikan Quote Penawaran WhatsApp">
          <div style={{display:'flex', flexDirection:'column', gap:'16px'}}>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px'}}>
              <div>
                <label className="input-label"><User size={12} /> Nama Klien / Pasangan</label>
                <input type="text" value={clientName} onChange={e => setClientName(e.target.value)} className="input-field" placeholder="Bpk. Kevin & Vania" />
              </div>
              <div>
                <label className="input-label"><Calendar size={12} /> Rencana Tanggal</label>
                <input type="text" value={eventDate} onChange={e => setEventDate(e.target.value)} className="input-field" placeholder="12 September 2026" />
              </div>
            </div>

            <div>
              <label className="input-label">Live Preview Teks WhatsApp:</label>
              <div style={{
                background:'#ECFDF5', border:'1px solid #A7F3D0', borderRadius:'12px',
                padding:'14px', fontSize:'12.5px', color:'#065F46', whiteSpace:'pre-wrap',
                maxHeight:'220px', overflowY:'auto', fontFamily:'monospace', lineHeight:'1.5'
              }}>
                {generateWaQuoteText(shareModalCard, clientName, eventDate)}
              </div>
            </div>

            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'10px', marginTop:'8px'}}>
              <button 
                onClick={handleCopyWaText}
                className="btn btn-secondary btn-full"
                style={{color: copiedId === shareModalCard.id ? 'var(--success)' : 'inherit'}}
              >
                {copiedId === shareModalCard.id ? <><CheckCircle2 size={15} /> Tersalin!</> : <><Copy size={15} /> Salin Teks</>}
              </button>
              
              <button 
                onClick={handleOpenWaLink}
                className="btn btn-primary btn-full"
                style={{background:'#059669', borderColor:'#059669'}}
              >
                <MessageSquare size={15} /> Buka WA 🚀
              </button>
            </div>
          </div>
        </Modal>
      )}

    </div>
  );
};
