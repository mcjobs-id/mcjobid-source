import React, { useState } from 'react';
import { Plus, Phone, AtSign, Search, Trash2, Building2 } from 'lucide-react';
import type { Client } from '../types';
import { Modal } from '../components/Modal';

interface ClientsPageProps {
  clients: Client[];
  onSaveClient: (client: Client) => Promise<void>;
  onDeleteClient: (id: string) => Promise<void>;
}

export const ClientsPage: React.FC<ClientsPageProps> = ({ clients, onSaveClient, onDeleteClient }) => {
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [company, setCompany] = useState('');
  const [phone, setPhone] = useState('');
  const [instagram, setInstagram] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);

  const filtered = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      (c.company || '').toLowerCase().includes(search.toLowerCase())
  );

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const cli: Client = {
        id: Date.now().toString(),
        ownerId: '',
        name,
        company,
        phone,
        instagram,
        notes,
        totalBookings: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      await onSaveClient(cli);
      setIsModalOpen(false);
      setName('');
      setPhone('');
    } catch (err) {
      console.error('Error saving client:', err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-extrabold text-slate-900 dark:text-white">Database Klien & WO</h2>
          <p className="text-xs text-slate-400">Total {clients.length} Klien Terdaftar</p>
        </div>
        <button onClick={() => setIsModalOpen(true)} className="btn-primary py-2 px-3 text-xs font-bold shadow-md">
          <Plus className="w-4 h-4" /> Tambah Klien
        </button>
      </div>

      <div className="relative">
        <Search className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Cari nama klien atau nama WO..."
          className="input-field pl-10 py-2.5 text-xs"
        />
      </div>

      <div className="space-y-3">
        {filtered.length === 0 ? (
          <div className="card text-center py-10 text-xs text-slate-400">
            Belum ada data klien. Klik "+ Tambah Klien".
          </div>
        ) : (
          filtered.map((cli) => (
            <div key={cli.id} className="card p-4 space-y-2 hover:border-indigo-300 dark:hover:border-indigo-700">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-sm font-extrabold text-slate-900 dark:text-white">{cli.name}</h3>
                  {cli.company && (
                    <span className="text-[11px] text-indigo-600 dark:text-indigo-400 font-bold flex items-center gap-1 mt-0.5">
                      <Building2 className="w-3 h-3" /> {cli.company}
                    </span>
                  )}
                </div>
                <button onClick={() => onDeleteClient(cli.id)} className="text-slate-400 hover:text-rose-500 p-1">
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>

              <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs">
                {cli.phone ? (
                  <a
                    href={`https://wa.me/${cli.phone.replace(/\D/g, '')}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-emerald-600 dark:text-emerald-400 font-bold flex items-center gap-1 hover:underline"
                  >
                    <Phone className="w-3.5 h-3.5" /> {cli.phone}
                  </a>
                ) : (
                  <span className="text-slate-400">-</span>
                )}

                {cli.instagram && (
                  <span className="text-indigo-600 dark:text-indigo-400 font-medium flex items-center gap-1">
                    <AtSign className="w-3.5 h-3.5" /> {cli.instagram}
                  </span>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Tambah Klien / WO Baru">
        <form onSubmit={handleAdd} className="space-y-3 text-xs">
          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nama Klien / Kontak *</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Contoh: Sarah / WO Royal Event"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Perusahaan / WO</label>
              <input
                type="text"
                value={company}
                onChange={(e) => setCompany(e.target.value)}
                placeholder="Nama Agency WO"
                className="input-field text-xs py-2.5"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nomor WhatsApp</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="08123456789"
                className="input-field text-xs py-2.5"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Instagram</label>
            <input
              type="text"
              value={instagram}
              onChange={(e) => setInstagram(e.target.value)}
              placeholder="@wo_royal_event"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="pt-2">
            <button type="submit" disabled={saving} className="btn-primary w-full py-3 text-xs">
              {saving ? 'Menyimpan...' : 'Simpan Klien'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
