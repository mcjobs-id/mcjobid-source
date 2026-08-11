import React, { useState } from 'react';
import { ArrowLeft, CheckSquare, Plus, Trash2, Sparkles, Filter, AlertCircle, Calendar, CheckCircle2, Save } from 'lucide-react';
import type { TodoItem } from '../types';
import { useAuth } from '../context/AuthContext';
import { FAB } from '../components/FAB';
import { ConfirmModal } from '../components/ConfirmModal';

interface TodoPageProps {
  todos: TodoItem[];
  onSaveTodo: (todo: TodoItem) => Promise<void>;
  onDeleteTodo: (id: string) => Promise<void>;
  onBack: () => void;
}

const PREDEFINED_MC_TEMPLATES: Omit<TodoItem, 'id' | 'ownerId'>[] = [
  { title: 'Check Rundown & Technical Meeting H-7 dengan WO/Klien', category: 'PERSIAPAN', priority: 'TINGGI', isCompleted: false },
  { title: 'Fitting Dresscode & Outfit MC sesuai Tema Acara', category: 'PERSIAPAN', priority: 'SEDANG', isCompleted: false },
  { title: 'Latihan Olah Vokal & Pemanasan Suara H-1', category: 'PERSIAPAN', priority: 'SEDANG', isCompleted: false },
  { title: 'Uji Coba Microphone & Sound Check di Venue Acara', category: 'HARI_H', priority: 'TINGGI', isCompleted: false },
  { title: 'Konfirmasi Susunan Nama Tamu VVIP & Gelar Jabatan', category: 'HARI_H', priority: 'TINGGI', isCompleted: false },
  { title: 'Terbitkan Invoice & Kirim Tagihan Pelunasan ke Klien', category: 'PASCA_EVENT', priority: 'TINGGI', isCompleted: false },
  { title: 'Minta Testimoni & Ulasan Klien untuk Portofolio', category: 'PASCA_EVENT', priority: 'SEDANG', isCompleted: false },
  { title: 'Update Foto & Video Highlight Perform di Instagram/TikTok', category: 'KARIER', priority: 'RENDAH', isCompleted: false },
];

export const TodoPage: React.FC<TodoPageProps> = ({ todos, onSaveTodo, onDeleteTodo, onBack }) => {
  const { currentUser } = useAuth();
  
  // Page view mode: 'list' or 'form' (Dedicated Full Page Form View)
  const [viewMode, setViewMode] = useState<'list' | 'form'>('list');

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('SEMUA');
  const [selectedStatus, setSelectedStatus] = useState<'ALL' | 'PENDING' | 'DONE'>('ALL');

  // Form State
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<'PERSIAPAN' | 'HARI_H' | 'PASCA_EVENT' | 'KARIER' | 'UMUM'>('PERSIAPAN');
  const [priority, setPriority] = useState<'TINGGI' | 'SEDANG' | 'RENDAH'>('SEDANG');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  // Confirm Modal state
  const [confirmConfig, setConfirmConfig] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    confirmText: string;
    type: 'primary' | 'danger' | 'warning';
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    confirmText: 'Ya',
    type: 'primary',
    onConfirm: () => {}
  });

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    const uid = currentUser?.uid || 'user_local';
    setSaving(true);
    try {
      const newTodo: TodoItem = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        ownerId: uid,
        title: title.trim(),
        category,
        priority,
        notes: notes.trim(),
        isCompleted: false,
        createdAt: new Date().toISOString()
      };
      await onSaveTodo(newTodo);
      setTitle('');
      setNotes('');
      setViewMode('list');
    } catch (err) {
      console.error('Failed to save todo:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleApplyTemplates = () => {
    if (!currentUser?.uid) return;
    setConfirmConfig({
      isOpen: true,
      title: 'Muat Template MC',
      message: 'Muat 8 checklist tugas standar MC ke daftar tugas Anda?',
      confirmText: 'Ya, Muat Sekarang',
      type: 'primary',
      onConfirm: async () => {
        for (const item of PREDEFINED_MC_TEMPLATES) {
          const newTodo: TodoItem = {
            id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
            ownerId: currentUser.uid,
            title: item.title,
            category: item.category,
            priority: item.priority,
            isCompleted: false,
            createdAt: new Date().toISOString()
          };
          await onSaveTodo(newTodo);
        }
      }
    });
  };

  const toggleTodo = async (todo: TodoItem) => {
    await onSaveTodo({ ...todo, isCompleted: !todo.isCompleted });
  };

  // Stats
  const totalCount = todos.length;
  const completedCount = todos.filter(t => t.isCompleted).length;
  const pendingCount = totalCount - completedCount;
  const highPriorityPending = todos.filter(t => !t.isCompleted && (t.priority === 'high' || t.priority === 'TINGGI')).length;
  const percent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;

  // Filtering
  const filteredTodos = todos.filter(t => {
    if (selectedStatus === 'PENDING' && t.isCompleted) return false;
    if (selectedStatus === 'DONE' && !t.isCompleted) return false;
    if (selectedCategory !== 'SEMUA' && t.category !== selectedCategory) return false;

    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const matchTitle = t.title.toLowerCase().includes(q);
      const matchNotes = (t.notes || '').toLowerCase().includes(q);
      return matchTitle || matchNotes;
    }
    return true;
  });

  const getPriorityBadge = (p?: string) => {
    if (p === 'TINGGI' || p === 'high') return <span className="badge badge-error" style={{fontSize:'10px'}}>Prioritas Tinggi</span>;
    if (p === 'SEDANG' || p === 'medium') return <span className="badge badge-warning" style={{fontSize:'10px'}}>Prioritas Sedang</span>;
    return <span className="badge badge-success" style={{fontSize:'10px', background:'rgba(5,150,105,0.1)'}}>Prioritas Rendah</span>;
  };

  const getCategoryLabel = (c?: string) => {
    switch (c) {
      case 'PERSIAPAN': return 'Persiapan Event 🎯';
      case 'HARI_H': return 'Hari-H Acara 🎤';
      case 'PASCA_EVENT': return 'Pasca Acara 💼';
      case 'KARIER': return 'Karier & Portofolio 🚀';
      default: return 'Catatan Umum 📝';
    }
  };

  // ── DEDICATED FORM PAGE VIEW (TAMBAH TUGAS FULL SCREEN) ──
  if (viewMode === 'form') {
    return (
      <div className="animate-fade-in" style={{maxWidth:'800px', margin:'0 auto', paddingBottom:'32px'}}>

        <form onSubmit={handleAdd} style={{display:'flex', flexDirection:'column', gap:'20px'}}>
          <div className="card" style={{padding:'24px', display:'flex', flexDirection:'column', gap:'16px'}}>
            <div>
              <label className="input-label">Judul / Detail Tugas *</label>
              <input 
                type="text" 
                required 
                value={title} 
                onChange={e => setTitle(e.target.value)} 
                className="input-field" 
                placeholder="Contoh: Fitting Dresscode H-3 di Designer" 
              />
            </div>

            <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(240px, 1fr))', gap:'16px'}}>
              <div>
                <label className="input-label">Kategori Tugas</label>
                <select value={category} onChange={e => setCategory(e.target.value as any)} className="input-field">
                  <option value="PERSIAPAN">Persiapan Event 🎯</option>
                  <option value="HARI_H">Hari-H Acara 🎤</option>
                  <option value="PASCA_EVENT">Pasca Acara 💼</option>
                  <option value="KARIER">Karier & Portofolio 🚀</option>
                  <option value="UMUM">Catatan Umum 📝</option>
                </select>
              </div>

              <div>
                <label className="input-label">Skala Prioritas</label>
                <select value={priority} onChange={e => setPriority(e.target.value as any)} className="input-field">
                  <option value="TINGGI">Tinggi (High) 🚨</option>
                  <option value="SEDANG">Sedang (Medium) ⚡</option>
                  <option value="RENDAH">Rendah (Low) ☕</option>
                </select>
              </div>
            </div>

            <div>
              <label className="input-label">Catatan Tambahan (Optional)</label>
              <textarea 
                value={notes} 
                onChange={e => setNotes(e.target.value)} 
                className="input-field" 
                style={{height:'80px', padding:'12px', resize:'vertical'}}
                placeholder="Catatan kecil atau kontak penanggung jawab..." 
              />
            </div>
          </div>

          <div style={{display:'flex', gap:'12px'}}>
            <button 
              type="button" 
              onClick={() => setViewMode('list')} 
              className="btn btn-secondary btn-full btn-lg"
            >
              Batal
            </button>
            <button 
              type="submit" 
              disabled={saving} 
              className="btn btn-primary btn-full btn-lg" 
              style={{background:'#7C3AED', borderColor:'#7C3AED'}}
            >
              <Save size={16} /> {saving ? 'Menyimpan Tugas...' : 'Simpan Tugas 🚀'}
            </button>
          </div>
        </form>
      </div>
    );
  }

  const todoFabItems = [
    {
      key: 'add-task',
      label: 'Tambah Tugas Baru',
      icon: Plus,
      onClick: () => setViewMode('form')
    },
    {
      key: 'load-template',
      label: 'Muat Template MC 🚀',
      icon: Sparkles,
      onClick: handleApplyTemplates
    }
  ];

  // ── LIST VIEW ──
  return (
    <div className="animate-fade-in" style={{width:'100%', paddingBottom:'32px'}}>
      

      {/* ── HERO STATS CARD ── */}
      <div className="card" style={{padding:'24px', marginBottom:'24px', borderTop:'4px solid #7C3AED'}}>
        <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'14px', flexWrap:'wrap', gap:'12px'}}>
          <div>
            <span style={{fontSize:'12px', fontWeight:'700', color:'var(--text-3)', textTransform:'uppercase'}}>Progress Kesiapan Performance</span>
            <h3 style={{fontSize:'20px', fontWeight:'800', color:'var(--text-1)', marginTop:'2px'}}>
              {completedCount} dari {totalCount} tugas selesai ({percent}%)
            </h3>
          </div>
          
          <div style={{display:'flex', alignItems:'center', gap:'12px'}}>
            {highPriorityPending > 0 && (
              <span className="badge badge-error" style={{gap:'4px', padding:'6px 12px'}}>
                <AlertCircle size={13} /> {highPriorityPending} Prioritas Tinggi Tertunda
              </span>
            )}
            <span style={{fontSize:'28px', fontWeight:'900', color:'#7C3AED', fontVariantNumeric:'tabular-nums'}}>
              {percent}%
            </span>
          </div>
        </div>
        
        {/* Progress Bar */}
        <div style={{width:'100%', height:'10px', background:'var(--bg-surface-2)', borderRadius:'99px', overflow:'hidden'}}>
          <div style={{
            height:'100%', background:'linear-gradient(90deg, #7C3AED 0%, #6D28D9 100%)', 
            width: `${percent}%`,
            transition: 'width 0.3s ease'
          }} />
        </div>
      </div>

      {/* ── FILTERS & STATUS TABS ── */}
      <div style={{display:'flex', flexDirection:'column', gap:'12px', marginBottom:'20px'}}>
        <div style={{display:'flex', gap:'8px', overflowX:'auto', width:'100%'}} className="scrollbar-none">
          <button
            onClick={() => setSelectedStatus('ALL')}
            className={`btn btn-sm ${selectedStatus === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
            style={{background: selectedStatus === 'ALL' ? '#7C3AED' : undefined, borderColor: selectedStatus === 'ALL' ? '#7C3AED' : undefined, color: selectedStatus === 'ALL' ? '#FFFFFF' : undefined, whiteSpace:'nowrap', flexShrink:0}}
          >
            Semua ({totalCount})
          </button>
          <button
            onClick={() => setSelectedStatus('PENDING')}
            className={`btn btn-sm ${selectedStatus === 'PENDING' ? 'btn-primary' : 'btn-secondary'}`}
            style={{background: selectedStatus === 'PENDING' ? '#7C3AED' : undefined, borderColor: selectedStatus === 'PENDING' ? '#7C3AED' : undefined, color: selectedStatus === 'PENDING' ? '#FFFFFF' : undefined, whiteSpace:'nowrap', flexShrink:0}}
          >
            Tertunda ({pendingCount})
          </button>
          <button
            onClick={() => setSelectedStatus('DONE')}
            className={`btn btn-sm ${selectedStatus === 'DONE' ? 'btn-primary' : 'btn-secondary'}`}
            style={{background: selectedStatus === 'DONE' ? '#7C3AED' : undefined, borderColor: selectedStatus === 'DONE' ? '#7C3AED' : undefined, color: selectedStatus === 'DONE' ? '#FFFFFF' : undefined, whiteSpace:'nowrap', flexShrink:0}}
          >
            Selesai ({completedCount})
          </button>
        </div>

        {/* Category Filter Chips (Responsive Scroll) */}
        <div style={{display:'flex', gap:'8px', overflowX:'auto', width:'100%', paddingBottom:'4px'}} className="scrollbar-none">
          {[
            { id: 'SEMUA', label: 'Semua Kategori' },
            { id: 'PERSIAPAN', label: 'Persiapan Event 🎯' },
            { id: 'HARI_H', label: 'Hari-H Acara 🎤' },
            { id: 'PASCA_EVENT', label: 'Pasca Acara 💼' },
            { id: 'KARIER', label: 'Karier & Portofolio 🚀' },
          ].map(c => (
            <button
              key={c.id}
              onClick={() => setSelectedCategory(c.id)}
              className="badge"
              style={{
                cursor:'pointer', padding:'8px 14px', fontSize:'12px', fontWeight:'700', borderRadius:'9999px',
                border: selectedCategory === c.id ? '1px solid #7C3AED' : '1px solid var(--border)',
                background: selectedCategory === c.id ? '#7C3AED' : 'var(--bg-surface-2)',
                color: selectedCategory === c.id ? '#FFFFFF' : 'var(--text-2)',
                whiteSpace:'nowrap', flexShrink:0, transition:'all 0.15s'
              }}
            >
              {c.label}
            </button>
          ))}
        </div>
      </div>

      {/* ── LIST ── */}
      <div style={{display:'flex', flexDirection:'column', gap:'10px'}}>
        {filteredTodos.length === 0 ? (
          <div className="card empty-state" style={{padding:'48px 24px'}}>
            <CheckSquare size={24} className="empty-state-icon" style={{color:'#7C3AED', background:'#F5F3FF'}} />
            <p style={{fontSize:'14px', fontWeight:'600', color:'var(--text-1)'}}>Tidak ada tugas dalam tampilan ini!</p>
            <p style={{fontSize:'12px', color:'var(--text-3)', maxWidth:'280px', margin:'4px auto'}}>
              Gunakan tombol Template MC atau Tambah Tugas di atas untuk mulai mengelola daftar tugas Anda.
            </p>
          </div>
        ) : (
          filteredTodos.map(t => (
            <div 
              key={t.id} 
              className="card hover-scale" 
              style={{
                padding:'16px 20px', 
                display:'flex', 
                alignItems:'flex-start', 
                gap:'14px', 
                transition:'all 0.2s', 
                opacity: t.isCompleted ? 0.6 : 1,
                borderLeft: t.isCompleted ? '4px solid var(--success)' : '4px solid #7C3AED'
              }}
            >
              <div 
                onClick={() => toggleTodo(t)}
                style={{
                  width:'24px', height:'24px', borderRadius:'6px', 
                  border:`2px solid ${t.isCompleted ? 'var(--success)' : '#7C3AED'}`,
                  background: t.isCompleted ? 'var(--success)' : 'transparent', 
                  display:'flex', alignItems:'center', justifyContent:'center',
                  cursor:'pointer', flexShrink:0, marginTop:'2px', transition:'all 0.15s'
                }}
              >
                {t.isCompleted && <CheckCircle2 size={16} color="white" />}
              </div>
              
              <div style={{flex:1}} onClick={() => toggleTodo(t)}>
                <div style={{display:'flex', alignItems:'center', gap:'8px', marginBottom:'4px', flexWrap:'wrap'}}>
                  {getPriorityBadge(t.priority)}
                  <span style={{fontSize:'11px', color:'var(--text-3)'}}>• {getCategoryLabel(t.category)}</span>
                </div>

                <p style={{
                  fontSize:'14.5px', fontWeight:'600', color:'var(--text-1)', 
                  textDecoration: t.isCompleted ? 'line-through' : 'none', 
                  cursor:'pointer', lineHeight:'1.4'
                }}>
                  {t.title}
                </p>

                {t.notes && (
                  <p style={{fontSize:'12px', color:'var(--text-3)', marginTop:'4px'}}>{t.notes}</p>
                )}
              </div>

              <button 
                onClick={() => {
                  setConfirmConfig({
                    isOpen: true,
                    title: 'Hapus Tugas',
                    message: `Hapus tugas "${t.title}"?`,
                    confirmText: 'Ya, Hapus',
                    type: 'danger',
                    onConfirm: () => onDeleteTodo(t.id)
                  });
                }} 
                className="btn btn-ghost btn-sm" 
                style={{padding:0, width:'32px', height:'32px', color:'var(--error)', flexShrink:0}}
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))
        )}
      </div>

      {/* ── FLOATING SPEED DIAL ACTION BUTTON ── */}
      {viewMode === 'list' && (
        <FAB label="Tugas MC" items={todoFabItems} />
      )}

      {/* ── CUSTOM CONFIRMATION MODAL ── */}
      <ConfirmModal
        isOpen={confirmConfig.isOpen}
        onClose={() => setConfirmConfig(prev => ({ ...prev, isOpen: false }))}
        onConfirm={confirmConfig.onConfirm}
        title={confirmConfig.title}
        message={confirmConfig.message}
        confirmText={confirmConfig.confirmText}
        type={confirmConfig.type}
      />
    </div>
  );
};
