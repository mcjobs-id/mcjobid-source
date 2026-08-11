import React, { useState } from 'react';
import { ArrowLeft, Star, Plus, Edit2, Trash2, CheckCircle2, MessageSquare, Lightbulb, User, Sparkles, Building2 } from 'lucide-react';
import type { Testimonial } from '../types';
import { useAuth } from '../context/AuthContext';
import { Modal } from '../components/Modal';
import { FAB } from '../components/FAB';

interface TestimonialPageProps {
  testimonials: Testimonial[];
  onSaveTestimonial: (testimonial: Testimonial) => Promise<void>;
  onDeleteTestimonial: (id: string) => Promise<void>;
  onBack: () => void;
}

const AVATAR_COLORS = [
  '#4F46E5', '#7C3AED', '#DB2777', '#D97706',
  '#059669', '#0284C7', '#DC2626', '#0891B2'
];

function getAvatarColor(name: string) {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i);
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

function getInitials(name: string) {
  const parts = name.trim().split(' ');
  if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  if (parts.length === 1 && parts[0]) return parts[0][0].toUpperCase();
  return 'MC';
}

// Seed testimonials removed for production
export const TestimonialPage: React.FC<TestimonialPageProps> = ({
  testimonials = [],
  onSaveTestimonial,
  onDeleteTestimonial,
  onBack
}) => {
  const { currentUser, userProfile } = useAuth();

  // Use real testimonials from Firestore
  const displayList = testimonials || [];

  // Check if current user has an existing review
  const myReview = testimonials.find(t => t.userId === currentUser?.uid);

  // Form Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [rating, setRating] = useState(5);
  const [review, setReview] = useState('');
  const [suggestion, setSuggestion] = useState('');
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  const openForm = () => {
    if (myReview) {
      setName(myReview.userName || myReview.clientName || userProfile?.stageName || userProfile?.displayName || '');
      setRating(myReview.rating || 5);
      setReview(myReview.review || myReview.comment || '');
      setSuggestion(myReview.suggestion || '');
    } else {
      setName(userProfile?.stageName || userProfile?.displayName || '');
      setRating(5);
      setReview('');
      setSuggestion('');
    }
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!review.trim() || !currentUser) return;
    setSaving(true);
    try {
      const item: Testimonial = {
        id: myReview ? myReview.id : `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        userId: currentUser.uid,
        userName: name.trim() || userProfile?.stageName || userProfile?.displayName || 'MC Talent',
        clientName: name.trim() || userProfile?.stageName || userProfile?.displayName || 'MC Talent',
        photoUrl: userProfile?.photoUrl || userProfile?.photoUri,
        rating,
        review: review.trim(),
        comment: review.trim(),
        suggestion: suggestion.trim(),
        date: new Date().toISOString().split('T')[0],
        createdAt: myReview?.createdAt || new Date().toISOString()
      };
      await onSaveTestimonial(item);
      setIsModalOpen(false);
      setSuccessMsg('Testimoni Anda berhasil dikirim & dipublikasikan! 🎉');
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Hapus ulasan Anda?')) return;
    try {
      await onDeleteTestimonial(id);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="animate-fade-in" style={{ width: '100%', paddingBottom: '80px' }}>


      {/* Success Notification */}
      {successMsg && (
        <div className="animate-fade-in" style={{ padding: '12px 16px', background: '#D1FAE5', border: '1px solid #10B981', color: '#065F46', borderRadius: '12px', fontSize: '13px', fontWeight: '700', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <CheckCircle2 size={18} /> {successMsg}
        </div>
      )}

      {/* ── HERO SHOWCASE BANNER ── */}
      <div className="card" style={{ padding: '24px', marginBottom: '24px', background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)', color: 'white' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <span style={{ fontSize: '11px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'rgba(255,255,255,0.75)' }}>KOMUNITAS MC INDONESIA</span>
            <h2 style={{ fontSize: '22px', fontWeight: '900', marginTop: '2px', color: 'white' }}>Apa Kata Para MC Profesional?</h2>
            <p style={{ fontSize: '13px', color: 'rgba(255,255,255,0.85)', marginTop: '4px', maxWidth: '440px' }}>
              Bagikan pengalaman Anda menggunakan MCJob.id untuk membangun reputasi dan memberikan masukan pengembangan fitur!
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255,255,255,0.15)', padding: '12px 20px', borderRadius: '16px', backdropFilter: 'blur(8px)' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', justifyContent: 'center' }}>
                <span style={{ fontSize: '28px', fontWeight: '900', color: '#FBBF24' }}>5.0</span>
                <Star size={22} color="#FBBF24" fill="#FBBF24" />
              </div>
              <span style={{ fontSize: '11px', color: 'rgba(255,255,255,0.8)', fontWeight: '600' }}>{displayList.length} Ulasan Terverifikasi</span>
            </div>
          </div>
        </div>
      </div>

      {/* ── TESTIMONIALS LIST ── */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {displayList.map(t => {
          const isOwn = currentUser && t.userId === currentUser.uid;
          const nameStr = t.userName || t.clientName || 'MC Professional';
          const initials = getInitials(nameStr);
          const avatarBg = getAvatarColor(nameStr);
          const ratingVal = t.rating || 5;
          const reviewText = t.review || t.comment || '';
          const suggestionText = t.suggestion || '';

          return (
            <div
              key={t.id}
              className="card hover-scale"
              style={{
                padding: '20px 24px',
                borderLeft: isOwn ? '5px solid #7C3AED' : '1px solid var(--border)',
                background: isOwn ? 'var(--bg-surface-2)' : 'var(--bg-surface)',
                transition: 'all 0.2s ease'
              }}
            >
              {/* Top Row: User info + Rating + Own Badge */}
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '14px', flexWrap: 'wrap', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  {/* Avatar */}
                  <div style={{
                    width: '44px', height: '44px', borderRadius: '50%',
                    background: avatarBg, color: 'white',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontWeight: '800', fontSize: '15px', flexShrink: 0, overflow: 'hidden'
                  }}>
                    {t.photoUrl ? (
                      <img src={t.photoUrl} alt={nameStr} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    ) : initials}
                  </div>

                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <h3 style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-1)' }}>{nameStr}</h3>
                      {isOwn && (
                        <span className="badge" style={{ background: '#7C3AED', color: 'white', fontSize: '10px', fontWeight: '700', padding: '2px 8px' }}>
                          Ulasan Anda ✨
                        </span>
                      )}
                    </div>
                    <p style={{ fontSize: '11px', color: 'var(--text-3)', marginTop: '2px' }}>
                      {t.date || new Date(t.createdAt || Date.now()).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })}
                    </p>
                  </div>
                </div>

                {/* Rating Stars & Actions */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ display: 'flex', gap: '2px' }}>
                    {[1, 2, 3, 4, 5].map(star => (
                      <Star
                        key={star} size={15}
                        color={star <= ratingVal ? '#F59E0B' : 'var(--border)'}
                        fill={star <= ratingVal ? '#F59E0B' : 'none'}
                      />
                    ))}
                  </div>

                  {isOwn && (
                    <div style={{ display: 'flex', gap: '4px' }}>
                      <button onClick={openForm} className="btn btn-ghost btn-sm" style={{ padding: '4px', color: '#7C3AED' }} title="Edit Ulasan">
                        <Edit2 size={15} />
                      </button>
                      <button onClick={() => handleDelete(t.id)} className="btn btn-ghost btn-sm" style={{ padding: '4px', color: 'var(--error)' }} title="Hapus Ulasan">
                        <Trash2 size={15} />
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {/* Review Text */}
              <p style={{ fontSize: '14px', color: 'var(--text-1)', lineHeight: '1.6', fontWeight: '500' }}>
                "{reviewText}"
              </p>

              {/* Suggestion Text Card (Android feature) */}
              {suggestionText && (
                <div style={{ marginTop: '12px', background: 'var(--bg-surface-2)', border: '1px solid var(--border)', borderRadius: '10px', padding: '10px 14px', display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                  <Lightbulb size={16} color="#D97706" style={{ flexShrink: 0, marginTop: '2px' }} />
                  <div>
                    <span style={{ fontSize: '11px', fontWeight: '700', color: '#D97706', textTransform: 'uppercase' }}>Saran & Masukan Fitur:</span>
                    <p style={{ fontSize: '12.5px', color: 'var(--text-2)', marginTop: '2px', lineHeight: '1.4' }}>{suggestionText}</p>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* ── MODAL FORM TULIS / EDIT TESTIMONI ── */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={myReview ? 'Edit Testimoni Saya' : 'Tulis Testimoni MC ✨'}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label className="input-label">Nama Lengkap / Stage Name *</label>
            <input
              type="text" required
              value={name}
              onChange={e => setName(e.target.value)}
              className="input-field"
              placeholder="Contoh: Rian Febrian, S.I.Kom"
            />
          </div>

          <div>
            <label className="input-label">Beri Rating Pengalaman (1 - 5 Bintang) *</label>
            <div style={{ display: 'flex', gap: '8px', padding: '8px 0' }}>
              {[1, 2, 3, 4, 5].map(star => (
                <button
                  key={star} type="button"
                  onClick={() => setRating(star)}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                >
                  <Star
                    size={28}
                    color={star <= rating ? '#F59E0B' : 'var(--border)'}
                    fill={star <= rating ? '#F59E0B' : 'none'}
                  />
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="input-label">Ulasan Pengalaman MCJob.id *</label>
            <textarea
              required
              value={review}
              onChange={e => setReview(e.target.value)}
              className="input-field"
              style={{ height: '90px', padding: '10px 12px', resize: 'vertical' }}
              placeholder="Ceritakan bagaimana MCJob.id membantu perform & manajemen event Anda..."
            />
          </div>

          <div>
            <label className="input-label">Saran & Masukan Fitur Aplikasi (Opsional)</label>
            <textarea
              value={suggestion}
              onChange={e => setSuggestion(e.target.value)}
              className="input-field"
              style={{ height: '70px', padding: '10px 12px', resize: 'vertical' }}
              placeholder="Saran fitur baru yang Anda inginkan..."
            />
          </div>

          <button
            type="submit"
            disabled={saving}
            className="btn btn-primary btn-full btn-lg"
            style={{ background: '#7C3AED', borderColor: '#7C3AED', marginTop: '8px' }}
          >
            {saving ? 'Mengirim...' : 'Kirim & Publikasikan 🚀'}
          </button>
        </form>
      </Modal>

      {/* ── FLOATING ACTION BUTTON ── */}
      <FAB onClick={openForm} label={myReview ? 'Edit Ulasan' : 'Tulis Testimoni'} color="#7C3AED" />
    </div>
  );
};
