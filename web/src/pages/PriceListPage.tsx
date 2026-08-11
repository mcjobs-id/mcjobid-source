import React, { useState } from 'react';
import { Plus, Check, Share2, Trash2 } from 'lucide-react';
import type { RateCard } from '../types';
import { Modal } from '../components/Modal';

interface PriceListPageProps {
  rateCards: RateCard[];
  onSaveRateCard: (card: RateCard) => Promise<void>;
  onDeleteRateCard: (id: string) => Promise<void>;
}

export const PriceListPage: React.FC<PriceListPageProps> = ({ rateCards, onSaveRateCard, onDeleteRateCard }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('Wedding Package');
  const [price, setPrice] = useState(0);
  const [inclusionsText, setInclusionsText] = useState('');
  const [description, setDescription] = useState('');
  const [saving, setSaving] = useState(false);

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const card: RateCard = {
        id: Date.now().toString(),
        ownerId: '',
        title,
        category,
        price: Number(price),
        inclusions: inclusionsText.split('\n').filter((i) => i.trim() !== ''),
        description,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      await onSaveRateCard(card);
      setIsModalOpen(false);
      setTitle('');
      setPrice(0);
      setInclusionsText('');
    } catch (err) {
      console.error('Error saving rate card:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleSharePackage = (card: RateCard) => {
    const text = `✨ *${card.title}* (${card.category})\n` +
      `💰 *Harga Package*: ${formatCurrency(card.price)}\n\n` +
      `📌 *Fasilitas Included*:\n` +
      card.inclusions.map((inc) => `• ${inc}`).join('\n') +
      `\n\nUntuk booking & konsultasi jadwal silakan hubungi kontak kami. Terima kasih! 🙏`;

    window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, '_blank');
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-extrabold text-slate-900 dark:text-white">Price List & Rate Card</h2>
          <p className="text-xs text-slate-400">Katalog Paket MC & Kartu Tarif</p>
        </div>
        <button onClick={() => setIsModalOpen(true)} className="btn-primary py-2 px-3 text-xs font-bold shadow-md">
          <Plus className="w-4 h-4" /> Tambah Paket
        </button>
      </div>

      <div className="grid grid-cols-1 gap-3.5">
        {rateCards.length === 0 ? (
          <div className="card text-center py-10 text-xs text-slate-400">
            Belum ada paket rate card diset. Klik "+ Tambah Paket".
          </div>
        ) : (
          rateCards.map((card) => (
            <div key={card.id} className="card p-5 space-y-3 relative overflow-hidden border-2 hover:border-indigo-500 transition-all">
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-[10px] font-extrabold px-2.5 py-0.5 rounded-full bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
                    {card.category}
                  </span>
                  <h3 className="text-base font-extrabold text-slate-900 dark:text-white mt-1">{card.title}</h3>
                  <span className="text-lg font-black text-indigo-600 dark:text-indigo-400 block mt-0.5">
                    {formatCurrency(card.price)}
                  </span>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => handleSharePackage(card)}
                    className="p-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 hover:bg-emerald-100"
                    title="Bagikan ke WhatsApp"
                  >
                    <Share2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onDeleteRateCard(card.id)}
                    className="p-2 rounded-xl bg-rose-50 dark:bg-rose-950/40 text-rose-600 hover:bg-rose-100"
                    title="Hapus Paket"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {card.inclusions && card.inclusions.length > 0 && (
                <div className="pt-3 border-t border-slate-100 dark:border-slate-800 space-y-1.5 text-xs">
                  <span className="text-[10px] font-bold text-slate-400 uppercase block">Fasilitas Termasuk:</span>
                  {card.inclusions.map((inc, i) => (
                    <div key={i} className="flex items-center gap-2 text-slate-700 dark:text-slate-300">
                      <Check className="w-3.5 h-3.5 text-emerald-500 shrink-0 stroke-[3]" />
                      <span>{inc}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))
        )}
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Tambah Paket Rate Card Baru">
        <form onSubmit={handleAdd} className="space-y-3 text-xs">
          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nama Paket *</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Contoh: Paket Full Wedding MC + Rundown"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Kategori</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="input-field text-xs py-2.5"
              >
                <option value="Wedding Package">Wedding Package</option>
                <option value="Gathering Corporate">Gathering Corporate</option>
                <option value="Birthday & Private">Birthday & Private</option>
                <option value="Seminar & Formal">Seminar & Formal</option>
              </select>
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Tarif (Rp) *</label>
              <input
                type="number"
                required
                value={price}
                onChange={(e) => setPrice(Number(e.target.value))}
                placeholder="0"
                className="input-field text-xs py-2.5 font-bold"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">
              Fasilitas Termasuk (1 per baris)
            </label>
            <textarea
              rows={4}
              value={inclusionsText}
              onChange={(e) => setInclusionsText(e.target.value)}
              placeholder="MC Akad & Resepsi&#10;Free Konsultasi Rundown&#10;Gladi Bersih 1 Hari Sebelum"
              className="input-field text-xs py-2"
            />
          </div>

          <div className="pt-2">
            <button type="submit" disabled={saving} className="btn-primary w-full py-3 text-xs">
              {saving ? 'Menyimpan...' : 'Simpan Rate Card'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
