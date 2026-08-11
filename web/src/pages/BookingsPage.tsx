import React, { useState, useMemo } from 'react';
import { Search, Plus, Calendar, MapPin, User } from 'lucide-react';
import type { Booking } from '../types';
import { Modal } from '../components/Modal';

interface BookingsPageProps {
  bookings: Booking[];
  onSaveBooking: (booking: Booking) => Promise<void>;
  onOpenDetail: (booking: Booking) => void;
  openCreateDirectly?: boolean;
}

export const BookingsPage: React.FC<BookingsPageProps> = ({
  bookings,
  onSaveBooking,
  onOpenDetail,
  openCreateDirectly = false
}) => {
  const [search, setSearch] = useState('');
  const [filterTab, setFilterTab] = useState<'all' | 'upcoming' | 'unpaid' | 'completed'>('all');
  const [isModalOpen, setIsModalOpen] = useState(openCreateDirectly);

  // Form State
  const [name, setName] = useState('');
  const [client, setClient] = useState('');
  const [category, setCategory] = useState('Wedding');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [start, setStart] = useState('08:00');
  const [end, setEnd] = useState('12:00');
  const [loc, setLoc] = useState('');
  const [address, setAddress] = useState('');
  const [fee, setFee] = useState(0);
  const [dp, setDp] = useState(0);
  const [note, setNote] = useState('');
  const [saving, setSaving] = useState(false);

  const filteredBookings = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    return bookings.filter((b) => {
      const matchSearch =
        (b.name || '').toLowerCase().includes(search.toLowerCase()) ||
        (b.client || '').toLowerCase().includes(search.toLowerCase()) ||
        (b.loc || '').toLowerCase().includes(search.toLowerCase());

      if (!matchSearch) return false;

      if (filterTab === 'upcoming') return b.date >= today && b.status !== 'completed';
      if (filterTab === 'unpaid') return (b.fee || 0) - (b.dp || 0) > 0;
      if (filterTab === 'completed') return b.status === 'completed' || b.date < today;
      return true;
    });
  }, [bookings, search, filterTab]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const newBooking: Booking = {
        id: Date.now().toString(),
        ownerId: '', // set in service or parent
        name,
        client,
        category,
        date,
        start,
        end,
        loc,
        address,
        fee: Number(fee),
        dp: Number(dp),
        note,
        status: 'confirmed',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      await onSaveBooking(newBooking);
      setIsModalOpen(false);
      // Reset
      setName('');
      setClient('');
      setFee(0);
      setDp(0);
    } catch (err) {
      console.error('Error saving booking:', err);
    } finally {
      setSaving(false);
    }
  };

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-extrabold text-slate-900 dark:text-white">Jadwal Acara MC</h2>
          <p className="text-xs text-slate-400">Total {bookings.length} Acara Terdaftar</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="btn-primary py-2.5 px-4 text-xs font-bold shadow-md"
        >
          <Plus className="w-4 h-4 stroke-[3]" /> Tambah Job
        </button>
      </div>

      {/* Search & Tabs */}
      <div className="space-y-2.5">
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3.5 top-3.5 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Cari nama acara, klien, atau lokasi..."
            className="input-field pl-10 py-2.5 text-xs"
          />
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar">
          {[
            { id: 'all', label: 'Semua' },
            { id: 'upcoming', label: 'Mendatang' },
            { id: 'unpaid', label: 'Belum Lunas' },
            { id: 'completed', label: 'Selesai' }
          ].map((t) => (
            <button
              key={t.id}
              onClick={() => setFilterTab(t.id as any)}
              className={`py-1.5 px-3.5 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                filterTab === t.id
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Bookings List */}
      <div className="space-y-3">
        {filteredBookings.length === 0 ? (
          <div className="card text-center py-10">
            <Calendar className="w-12 h-12 text-slate-300 dark:text-slate-600 mx-auto mb-2" />
            <h4 className="text-sm font-bold text-slate-700 dark:text-slate-300">Tidak ada jadwal ditemukan</h4>
            <p className="text-xs text-slate-400 mt-1">Coba ganti kata kunci pencarian atau tab filter.</p>
          </div>
        ) : (
          filteredBookings.map((job) => {
            const remaining = (job.fee || 0) - (job.dp || 0);
            const isPaid = remaining <= 0;

            return (
              <div
                key={job.id}
                onClick={() => onOpenDetail(job)}
                className="card hover:border-indigo-400 dark:hover:border-indigo-500 cursor-pointer active:scale-[0.99] transition-all"
              >
                <div className="flex items-start justify-between">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-md bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
                        {job.category || 'Wedding'}
                      </span>
                      <span className="text-xs font-semibold text-slate-400">
                        {job.start ? `${job.start} WIB` : 'All Day'}
                      </span>
                    </div>
                    <h3 className="text-base font-extrabold text-slate-900 dark:text-white leading-tight">
                      {job.name}
                    </h3>
                  </div>

                  <span className={`text-[10px] font-bold px-2.5 py-1 rounded-full ${isPaid ? 'badge-paid' : job.dp > 0 ? 'badge-partial' : 'badge-unpaid'}`}>
                    {isPaid ? 'LUNAS' : job.dp > 0 ? 'DP MASUK' : 'BELUM DP'}
                  </span>
                </div>

                <div className="mt-3 pt-3 border-t border-slate-100 dark:border-slate-800 grid grid-cols-2 gap-2 text-xs text-slate-500 dark:text-slate-400">
                  <div className="flex items-center gap-1.5">
                    <User className="w-3.5 h-3.5 text-indigo-500 shrink-0" />
                    <span className="truncate">{job.client || 'Klien Umum'}</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-rose-500 shrink-0" />
                    <span className="truncate">{job.loc || 'Lokasi belum diset'}</span>
                  </div>
                </div>

                <div className="mt-2.5 flex items-center justify-between pt-2">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
                      {new Date(job.date).toLocaleDateString('id-ID', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })}
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="text-xs font-extrabold text-indigo-600 dark:text-indigo-400">
                      {formatCurrency(job.fee)}
                    </span>
                    {!isPaid && (
                      <span className="text-[10px] text-rose-500 font-semibold block">
                        Sisa: {formatCurrency(remaining)}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* CREATE BOOKING MODAL */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Tambah Acara MC Baru"
      >
        <form onSubmit={handleCreate} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nama Acara / Event *</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Contoh: Wedding Sarah & Budi"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Kategori Event</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="input-field text-xs py-2.5"
              >
                <option value="Wedding">Wedding</option>
                <option value="Gathering">Corporate Gathering</option>
                <option value="Birthday">Birthday Party</option>
                <option value="Seminar">Seminar / Workshop</option>
                <option value="Concert">Concert / Festival</option>
                <option value="Lainnya">Lainnya</option>
              </select>
            </div>

            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Nama Klien / WO</label>
              <input
                type="text"
                value={client}
                onChange={(e) => setClient(e.target.value)}
                placeholder="Nama Klien"
                className="input-field text-xs py-2.5"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Tanggal *</label>
              <input
                type="date"
                required
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="input-field text-xs py-2"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Jam Mulai</label>
              <input
                type="time"
                value={start}
                onChange={(e) => setStart(e.target.value)}
                className="input-field text-xs py-2"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Jam Selesai</label>
              <input
                type="time"
                value={end}
                onChange={(e) => setEnd(e.target.value)}
                className="input-field text-xs py-2"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Lokasi Venue</label>
            <input
              type="text"
              value={loc}
              onChange={(e) => setLoc(e.target.value)}
              placeholder="Gedung / Hotel / Resto"
              className="input-field text-xs py-2.5"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5 pt-1">
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Total Fee (Rp)</label>
              <input
                type="number"
                value={fee}
                onChange={(e) => setFee(Number(e.target.value))}
                placeholder="0"
                className="input-field text-xs py-2.5 font-bold"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">DP Masuk (Rp)</label>
              <input
                type="number"
                value={dp}
                onChange={(e) => setDp(Number(e.target.value))}
                placeholder="0"
                className="input-field text-xs py-2.5 font-bold text-emerald-600"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 dark:text-slate-300 mb-1">Catatan Tambahan</label>
            <textarea
              rows={2}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Dresscode, kontak WO, request lagu, dll..."
              className="input-field text-xs py-2"
            />
          </div>

          <div className="pt-3">
            <button
              type="submit"
              disabled={saving}
              className="btn-primary w-full py-3 text-xs"
            >
              {saving ? 'Menyimpan Acara...' : 'Simpan Acara MC'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
