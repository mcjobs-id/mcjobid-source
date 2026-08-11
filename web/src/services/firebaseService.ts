import {
  collection,
  doc,
  getDoc,
  setDoc,
  deleteDoc,
  query,
  where,
  onSnapshot
} from 'firebase/firestore';
import { db } from '../firebase/config';
import type {
  UserProfile,
  Booking,
  Client,
  Invoice,
  Expense,
  RateCard,
  Testimonial,
  ChecklistItem
} from '../types';

// Helper to normalize Booking object fields so components never see undefined
const normalizeBooking = (raw: any): Booking => {
  const eventTitle = raw.eventTitle || raw.name || raw.clientName || raw.client || 'Acara MC';
  const name = raw.name || eventTitle;
  const clientName = raw.clientName || raw.client || 'Klien';
  const client = raw.client || clientName;
  const eventDate = raw.eventDate || raw.date || new Date().toISOString().split('T')[0];
  const date = raw.date || eventDate;
  const eventTime = raw.eventTime || raw.start || '';
  const start = raw.start || eventTime;
  const venue = raw.venue || raw.loc || '';
  const loc = raw.loc || venue;
  const totalFee = Number(raw.totalFee ?? raw.fee ?? 0);
  const fee = Number(raw.fee ?? totalFee);
  const dpAmount = Number(raw.dpAmount ?? raw.dp ?? 0);
  const dp = Number(raw.dp ?? dpAmount);
  
  let paymentStatus = raw.paymentStatus;
  if (!paymentStatus) {
    if (totalFee > 0 && dpAmount >= totalFee) paymentStatus = 'PAID';
    else if (dpAmount > 0) paymentStatus = 'PARTIAL';
    else paymentStatus = 'UNPAID';
  }
  paymentStatus = paymentStatus.toUpperCase();

  const notes = raw.notes || raw.note || '';
  const note = raw.note || notes;
  const status = raw.status || 'CONFIRMED';

  return {
    ...raw,
    id: raw.id,
    ownerId: raw.ownerId || '',
    eventTitle,
    name,
    clientName,
    client,
    category: raw.category || 'Wedding',
    eventDate,
    date,
    eventTime,
    start,
    venue,
    loc,
    totalFee,
    fee,
    dpAmount,
    dp,
    paymentStatus: paymentStatus as any,
    notes,
    note,
    status
  };
};

// Helper to normalize RateCard object fields
const normalizeRateCard = (raw: any): RateCard => {
  const name = raw.name || raw.title || 'Paket MC';
  const title = raw.title || name;
  const features = Array.isArray(raw.features) ? raw.features : (Array.isArray(raw.inclusions) ? raw.inclusions : []);
  const inclusions = Array.isArray(raw.inclusions) ? raw.inclusions : features;
  const notes = raw.notes || raw.description || '';
  const description = raw.description || notes;

  return {
    ...raw,
    id: raw.id,
    ownerId: raw.ownerId || '',
    name,
    title,
    price: Number(raw.price || 0),
    features,
    inclusions,
    notes,
    description,
    category: raw.category || 'General'
  };
};

// --- User Profile ---
export const getUserProfile = async (uid: string): Promise<UserProfile | null> => {
  const docRef = doc(db, 'users', uid);
  const snap = await getDoc(docRef);
  if (snap.exists()) {
    const data = snap.data();
    return {
      ...data,
      displayName: data.displayName || data.name || 'MC Professional',
      profileCompleted: data.profileCompleted ?? true
    } as UserProfile;
  }
  return null;
};

export const saveUserProfile = async (profile: UserProfile): Promise<void> => {
  const docRef = doc(db, 'users', profile.uid);
  await setDoc(docRef, {
    ...profile,
    displayName: profile.displayName || profile.name || 'MC Professional',
    updatedAt: new Date().toISOString()
  }, { merge: true });
};

// --- Bookings ---
export const subscribeBookings = (ownerId: string, callback: (bookings: Booking[]) => void) => {
  const q = query(
    collection(db, 'bookings'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Booking[] = [];
    snapshot.forEach((doc) => {
      list.push(normalizeBooking({ id: doc.id, ...doc.data() }));
    });
    list.sort((a, b) => new Date(b.eventDate!).getTime() - new Date(a.eventDate!).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to bookings:', err);
    callback([]);
  });
};

export const saveBooking = async (booking: Booking): Promise<void> => {
  const docRef = doc(db, 'bookings', booking.id);
  const now = new Date().toISOString();
  const normalized = normalizeBooking(booking);
  const data = {
    ...normalized,
    createdAt: booking.createdAt || now,
    updatedAt: now
  };
  await setDoc(docRef, data, { merge: true });
};

export const deleteBooking = async (bookingId: string): Promise<void> => {
  await deleteDoc(doc(db, 'bookings', bookingId));
};

// --- Clients ---
export const subscribeClients = (ownerId: string, callback: (clients: Client[]) => void) => {
  const q = query(
    collection(db, 'clients'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Client[] = [];
    snapshot.forEach((doc) => {
      const data = doc.data();
      list.push({
        id: doc.id,
        ownerId: data.ownerId || ownerId,
        name: data.name || 'Klien',
        type: data.type || 'DIRECT_CLIENT',
        phone: data.phone || '',
        email: data.email || '',
        address: data.address || '',
        notes: data.notes || '',
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    callback(list);
  }, (err) => {
    console.error('Error subscribing to clients:', err);
    callback([]);
  });
};

export const saveClient = async (client: Client): Promise<void> => {
  const docRef = doc(db, 'clients', client.id);
  const now = new Date().toISOString();
  await setDoc(docRef, {
    ...client,
    createdAt: client.createdAt || now,
    updatedAt: now
  }, { merge: true });
};

export const deleteClient = async (clientId: string): Promise<void> => {
  await deleteDoc(doc(db, 'clients', clientId));
};

// --- Invoices ---
export const subscribeInvoices = (ownerId: string, callback: (invoices: Invoice[]) => void) => {
  const q = query(
    collection(db, 'invoices'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Invoice[] = [];
    snapshot.forEach((doc) => {
      list.push({ id: doc.id, ...doc.data() } as Invoice);
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to invoices:', err);
    callback([]);
  });
};

export const saveInvoice = async (invoice: Invoice): Promise<void> => {
  const docRef = doc(db, 'invoices', invoice.id);
  const now = new Date().toISOString();
  await setDoc(docRef, {
    ...invoice,
    createdAt: invoice.createdAt || now,
    updatedAt: now
  }, { merge: true });
};

// --- Expenses ---
export const subscribeExpenses = (ownerId: string, callback: (expenses: Expense[]) => void) => {
  const q = query(
    collection(db, 'expenses'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Expense[] = [];
    snapshot.forEach((doc) => {
      const data = doc.data();
      list.push({
        id: doc.id,
        ownerId: data.ownerId || ownerId,
        title: data.title || 'Pengeluaran',
        category: data.category || 'Operasional',
        amount: Number(data.amount || 0),
        date: data.date || new Date().toISOString().split('T')[0],
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to expenses:', err);
    callback([]);
  });
};

export const saveExpense = async (expense: Expense): Promise<void> => {
  const docRef = doc(db, 'expenses', expense.id);
  await setDoc(docRef, {
    ...expense,
    createdAt: expense.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deleteExpense = async (expenseId: string): Promise<void> => {
  await deleteDoc(doc(db, 'expenses', expenseId));
};

// --- Rate Cards ---
export const subscribeRateCards = (ownerId: string, callback: (rateCards: RateCard[]) => void) => {
  const q = query(
    collection(db, 'rate_cards'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: RateCard[] = [];
    snapshot.forEach((doc) => {
      list.push(normalizeRateCard({ id: doc.id, ...doc.data() }));
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to rate cards:', err);
    callback([]);
  });
};

export const saveRateCard = async (rateCard: RateCard): Promise<void> => {
  const docRef = doc(db, 'rate_cards', rateCard.id);
  const now = new Date().toISOString();
  const normalized = normalizeRateCard(rateCard);
  await setDoc(docRef, {
    ...normalized,
    createdAt: rateCard.createdAt || now,
    updatedAt: now
  }, { merge: true });
};

export const deleteRateCard = async (rateCardId: string): Promise<void> => {
  await deleteDoc(doc(db, 'rate_cards', rateCardId));
};

// --- Checklists ---
export const subscribeChecklists = (ownerId: string, bookingId: string, callback: (checklists: ChecklistItem[]) => void) => {
  const q = query(
    collection(db, 'checklists'),
    where('ownerId', '==', ownerId),
    where('bookingId', '==', bookingId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: ChecklistItem[] = [];
    snapshot.forEach((doc) => {
      list.push(doc.data() as ChecklistItem);
    });
    list.sort((a, b) => a.order - b.order);
    callback(list);
  }, (err) => {
    console.error('Error subscribing to checklists:', err);
    callback([]);
  });
};

export const saveChecklistItem = async (item: ChecklistItem): Promise<void> => {
  const docRef = doc(db, 'checklists', item.id);
  await setDoc(docRef, item, { merge: true });
};

export const deleteChecklistItem = async (itemId: string): Promise<void> => {
  await deleteDoc(doc(db, 'checklists', itemId));
};

// --- Testimonials ---
export const subscribeTestimonials = (userId: string, callback: (testimonials: Testimonial[]) => void) => {
  const q = query(
    collection(db, 'testimonials'),
    where('userId', '==', userId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Testimonial[] = [];
    snapshot.forEach((doc) => {
      list.push(doc.data() as Testimonial);
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to testimonials:', err);
    callback([]);
  });
};

export const saveTestimonial = async (testimonial: Testimonial): Promise<void> => {
  const docRef = doc(db, 'testimonials', testimonial.id);
  await setDoc(docRef, testimonial, { merge: true });
};
