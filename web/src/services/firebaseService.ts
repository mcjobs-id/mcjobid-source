import {
  collection,
  doc,
  getDoc,
  setDoc,
  deleteDoc,
  query,
  where,
  orderBy,
  onSnapshot
} from 'firebase/firestore';
import { db } from '../firebase/config';
import type {
  UserProfile,
  Booking,
  Client,
  Invoice,
  Expense,
  Payment,
  RateCard,
  Testimonial,
  ChecklistItem,
  TodoItem
} from '../types';

// ─────────────────────────────────────────────────────────────
// Normalization helpers — guarantee components never see undefined
// ─────────────────────────────────────────────────────────────

const normalizeBooking = (raw: any): Booking => {
  // Handle dual-field naming: (name/eventTitle), (client/clientName), (date/eventDate), etc.
  const name = raw.name || raw.eventTitle || raw.clientName || raw.client || 'Acara MC';
  const eventTitle = raw.eventTitle || name;
  const client = raw.client || raw.clientName || '';
  const clientName = raw.clientName || client;
  const date = raw.date || raw.eventDate || new Date().toISOString().split('T')[0];
  const eventDate = raw.eventDate || date;
  const start = raw.start || raw.eventTime || '';
  const eventTime = raw.eventTime || start;
  const location = raw.location || raw.venue || raw.loc || '';
  const venue = raw.venue || location;
  const loc = raw.loc || location;

  const fee = Number(raw.fee ?? raw.totalFee ?? 0);
  const totalFee = Number(raw.totalFee ?? fee);
  const dp = Number(raw.dp ?? raw.dpAmount ?? 0);
  const dpAmount = Number(raw.dpAmount ?? dp);

  const note = raw.note || raw.notes || '';
  const notes = raw.notes || note;

  const rawStatus = (raw.status || 'confirmed').toLowerCase();

  // Derive paymentStatus from stored field, or compute from fee/dp
  let paymentStatus = raw.paymentStatus;
  if (!paymentStatus) {
    const today = new Date().toISOString().split('T')[0];
    if (fee === 0 && dp === 0) paymentStatus = 'TBD';
    else if (fee > 0 && dp >= fee) paymentStatus = 'PAID';
    else if (date < today && dp < fee && fee > 0) paymentStatus = 'OVERDUE';
    else if (dp > 0 && dp < fee) paymentStatus = 'PARTIAL';
    else if (dp === 0 && fee > 0) paymentStatus = 'UNPAID';
    else paymentStatus = 'TBD';
  }
  if (typeof paymentStatus === 'string') {
    paymentStatus = paymentStatus.toUpperCase();
  }

  return {
    ...raw,
    id: raw.id,
    ownerId: raw.ownerId, // removed fallback to ''
    // Canonical name fields
    name,
    eventTitle,
    // Canonical client fields
    client,
    clientName,
    clientId: raw.clientId || null,
    // Category & Status
    category: raw.category || 'Wedding',
    status: rawStatus as any,
    paymentStatus,
    // Dates
    date,
    eventDate,
    start,
    eventTime,
    end: raw.end || '',
    // Venue
    location,
    venue,
    loc,
    address: raw.address || '',
    // MC Specification
    dresscode: raw.dresscode || '',
    theme: raw.theme || '',
    mcType: raw.mcType || 'Single',
    language: raw.language || 'Bahasa Indonesia',
    audience: raw.audience || '',
    specialRequest: raw.specialRequest || '',
    pic: raw.pic || '',
    // Financials
    fee,
    totalFee,
    dp,
    dpAmount,
    // Notes
    note,
    notes,
    // Metadata
    createdAt: raw.createdAt || new Date().toISOString(),
    updatedAt: raw.updatedAt || new Date().toISOString()
  };
};

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
    ownerId: raw.ownerId, // removed fallback to ''
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

// ─────────────────────────────────────────────────────────────
// User Profile
// ─────────────────────────────────────────────────────────────

export const getUserProfile = async (uid: string): Promise<UserProfile | null> => {
  const docRef = doc(db, 'users', uid);
  const snap = await getDoc(docRef);
  if (snap.exists()) {
    const data = snap.data();
    return {
      uid,
      ...data,
      displayName: data.displayName || data.name || 'MC Professional',
      name: data.name || data.displayName || 'MC Professional',
      profileCompleted: data.profileCompleted ?? false
    } as UserProfile;
  }
  return null;
};

export const saveUserProfile = async (profile: UserProfile): Promise<void> => {
  const docRef = doc(db, 'users', profile.uid);
  const data: any = { ...profile };
  // Ensure both aliases are saved
  data.displayName = profile.displayName || profile.name || 'MC Professional';
  data.name = data.displayName;
  data.phoneNumber = profile.phoneNumber || profile.phone;
  data.phone = data.phoneNumber;
  data.bankAccountNumber = profile.bankAccountNumber || profile.bankAccount;
  data.bankAccount = data.bankAccountNumber;
  data.bankAccountHolder = profile.bankAccountHolder || profile.bankHolder;
  data.bankHolder = data.bankAccountHolder;
  data.instagramHandle = profile.instagramHandle || profile.instagram;
  data.instagram = data.instagramHandle;
  data.updatedAt = new Date().toISOString();
  await setDoc(docRef, data, { merge: true });
};

// ─────────────────────────────────────────────────────────────
// Bookings
// ─────────────────────────────────────────────────────────────

export const subscribeBookings = (ownerId: string, callback: (bookings: Booking[]) => void) => {
  const q = query(
    collection(db, 'bookings'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Booking[] = [];
    snapshot.forEach((docSnap) => {
      list.push(normalizeBooking({ id: docSnap.id, ...docSnap.data() }));
    });
    // Sort by event date descending
    list.sort((a, b) => {
      const dateA = a.date || a.eventDate || '';
      const dateB = b.date || b.eventDate || '';
      return dateB.localeCompare(dateA);
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to bookings:', err);
    callback([]);
  });
};

export const saveBooking = async (booking: Booking): Promise<void> => {
  if (!booking.ownerId) throw new Error('Cannot save booking without a valid ownerId');
  const docRef = doc(db, 'bookings', booking.id);
  const now = new Date().toISOString();
  const normalized = normalizeBooking(booking);
  await setDoc(docRef, {
    ...normalized,
    createdAt: booking.createdAt || now,
    updatedAt: now
  }, { merge: true });
};

export const deleteBooking = async (bookingId: string): Promise<void> => {
  await deleteDoc(doc(db, 'bookings', bookingId));
};

// ─────────────────────────────────────────────────────────────
// Payments (separate collection per Android model)
// ─────────────────────────────────────────────────────────────

export const subscribePayments = (ownerId: string, callback: (payments: Payment[]) => void) => {
  const q = query(
    collection(db, 'payments'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Payment[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        bookingId: data.bookingId || '',
        amount: Number(data.amount || 0),
        paymentDate: data.paymentDate || data.date || new Date().toISOString().split('T')[0],
        date: data.date || data.paymentDate || new Date().toISOString().split('T')[0],
        paymentMethod: data.paymentMethod || 'Transfer Bank',
        note: data.note || data.notes || '',
        notes: data.notes || data.note || '',
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to payments:', err);
    callback([]);
  });
};

export const subscribeBookingPayments = (ownerId: string, bookingId: string, callback: (payments: Payment[]) => void) => {
  const q = query(
    collection(db, 'payments'),
    where('ownerId', '==', ownerId),
    where('bookingId', '==', bookingId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Payment[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        bookingId: data.bookingId || bookingId,
        amount: Number(data.amount || 0),
        paymentDate: data.paymentDate || data.date || new Date().toISOString().split('T')[0],
        date: data.date || data.paymentDate,
        paymentMethod: data.paymentMethod || 'Transfer Bank',
        note: data.note || data.notes || '',
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => new Date(a.paymentDate).getTime() - new Date(b.paymentDate).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to booking payments:', err);
    callback([]);
  });
};

export const savePayment = async (payment: Payment): Promise<void> => {
  if (!payment.ownerId) throw new Error('Cannot save payment without a valid ownerId');
  const docRef = doc(db, 'payments', payment.id);
  await setDoc(docRef, {
    ...payment,
    paymentDate: payment.paymentDate || payment.date,
    date: payment.date || payment.paymentDate,
    createdAt: payment.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deletePayment = async (paymentId: string): Promise<void> => {
  await deleteDoc(doc(db, 'payments', paymentId));
};

// ─────────────────────────────────────────────────────────────
// Clients
// ─────────────────────────────────────────────────────────────

export const subscribeClients = (ownerId: string, callback: (clients: Client[]) => void) => {
  const q = query(
    collection(db, 'clients'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Client[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        name: data.name || 'Klien',
        type: data.type || 'DIRECT_CLIENT',
        company: data.company || '',
        phone: data.phone || '',
        email: data.email || '',
        address: data.address || '',
        instagram: data.instagram || '',
        pic: data.pic || '',
        notes: data.notes || '',
        isFavorite: data.isFavorite ?? false,
        isArchived: data.isArchived ?? false,
        totalBookings: data.totalBookings || 0,
        createdAt: data.createdAt || new Date().toISOString(),
        updatedAt: data.updatedAt || new Date().toISOString()
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
  if (!client.ownerId) throw new Error('Cannot save client without a valid ownerId');
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

// ─────────────────────────────────────────────────────────────
// Invoices
// ─────────────────────────────────────────────────────────────

export const subscribeInvoices = (ownerId: string, callback: (invoices: Invoice[]) => void) => {
  const q = query(
    collection(db, 'invoices'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Invoice[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as Invoice);
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to invoices:', err);
    callback([]);
  });
};

export const saveInvoice = async (invoice: Invoice): Promise<void> => {
  if (!invoice.ownerId) throw new Error('Cannot save invoice without a valid ownerId');
  const docRef = doc(db, 'invoices', invoice.id);
  const now = new Date().toISOString();
  await setDoc(docRef, {
    ...invoice,
    createdAt: invoice.createdAt || now,
    updatedAt: now
  }, { merge: true });
};

// ─────────────────────────────────────────────────────────────
// Expenses
// ─────────────────────────────────────────────────────────────

export const subscribeExpenses = (ownerId: string, callback: (expenses: Expense[]) => void) => {
  const q = query(
    collection(db, 'expenses'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Expense[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        bookingId: data.bookingId || '',
        title: data.title || data.category || 'Pengeluaran',
        category: data.category || 'Operasional',
        amount: Number(data.amount || 0),
        date: data.date || new Date().toISOString().split('T')[0],
        notes: data.notes || data.note || '',
        note: data.note || data.notes || '',
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

export const subscribeBookingExpenses = (ownerId: string, bookingId: string, callback: (expenses: Expense[]) => void) => {
  const q = query(
    collection(db, 'expenses'),
    where('ownerId', '==', ownerId),
    where('bookingId', '==', bookingId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Expense[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        bookingId: data.bookingId || bookingId,
        title: data.title || data.category || 'Pengeluaran',
        category: data.category || 'Operasional',
        amount: Number(data.amount || 0),
        date: data.date || new Date().toISOString().split('T')[0],
        notes: data.notes || data.note || '',
        note: data.note || data.notes || '',
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to booking expenses:', err);
    callback([]);
  });
};

export const saveExpense = async (expense: Expense): Promise<void> => {
  if (!expense.ownerId) throw new Error('Cannot save expense without a valid ownerId');
  const docRef = doc(db, 'expenses', expense.id);
  await setDoc(docRef, {
    ...expense,
    createdAt: expense.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deleteExpense = async (expenseId: string): Promise<void> => {
  await deleteDoc(doc(db, 'expenses', expenseId));
};

// ─────────────────────────────────────────────────────────────
// Rate Cards
// ─────────────────────────────────────────────────────────────

export const subscribeRateCards = (ownerId: string, callback: (rateCards: RateCard[]) => void) => {
  const q = query(
    collection(db, 'rate_cards'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: RateCard[] = [];
    snapshot.forEach((docSnap) => {
      list.push(normalizeRateCard({ id: docSnap.id, ...docSnap.data() }));
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to rate cards:', err);
    callback([]);
  });
};

export const saveRateCard = async (rateCard: RateCard): Promise<void> => {
  if (!rateCard.ownerId) throw new Error('Cannot save rate card without a valid ownerId');
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

// ─────────────────────────────────────────────────────────────
// Checklists (per booking)
// ─────────────────────────────────────────────────────────────

export const subscribeChecklists = (ownerId: string, bookingId: string, callback: (checklists: ChecklistItem[]) => void) => {
  const q = query(
    collection(db, 'checklists'),
    where('ownerId', '==', ownerId),
    where('bookingId', '==', bookingId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: ChecklistItem[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        bookingId: data.bookingId || bookingId,
        title: data.title || '',
        time: data.time,
        isCompleted: data.isCompleted ?? false,
        order: data.order ?? 0,
        createdAt: data.createdAt
      });
    });
    list.sort((a, b) => a.order - b.order);
    callback(list);
  }, (err) => {
    console.error('Error subscribing to checklists:', err);
    callback([]);
  });
};

export const saveChecklistItem = async (item: ChecklistItem): Promise<void> => {
  if (!item.ownerId) throw new Error('Cannot save checklist item without a valid ownerId');
  const docRef = doc(db, 'checklists', item.id);
  await setDoc(docRef, {
    ...item,
    createdAt: item.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deleteChecklistItem = async (itemId: string): Promise<void> => {
  await deleteDoc(doc(db, 'checklists', itemId));
};

// ─────────────────────────────────────────────────────────────
// Testimonials
// ─────────────────────────────────────────────────────────────

export const subscribeTestimonials = (userId: string, callback: (testimonials: Testimonial[]) => void) => {
  const q = query(
    collection(db, 'testimonials'),
    where('userId', '==', userId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: Testimonial[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as Testimonial);
    });
    list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    callback(list);
  }, (err) => {
    console.error('Error subscribing to testimonials:', err);
    callback([]);
  });
};

export const saveTestimonial = async (testimonial: Testimonial): Promise<void> => {
  // Note: Testimonial uses userId instead of ownerId in some places. Check this later if needed.
  if (!testimonial.userId) throw new Error('Cannot save testimonial without a valid userId');
  const docRef = doc(db, 'testimonials', testimonial.id);
  await setDoc(docRef, {
    ...testimonial,
    createdAt: testimonial.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deleteTestimonial = async (testimonialId: string): Promise<void> => {
  await deleteDoc(doc(db, 'testimonials', testimonialId));
};

// ─────────────────────────────────────────────────────────────
// Todos
// ─────────────────────────────────────────────────────────────

export const subscribeTodos = (ownerId: string, callback: (todos: TodoItem[]) => void) => {
  const q = query(
    collection(db, 'todos'),
    where('ownerId', '==', ownerId)
  );
  return onSnapshot(q, (snapshot) => {
    const list: TodoItem[] = [];
    snapshot.forEach((docSnap) => {
      const data = docSnap.data();
      list.push({
        id: docSnap.id,
        ownerId: data.ownerId || ownerId,
        title: data.title || data.text || 'Tugas Baru',
        isCompleted: data.isCompleted ?? data.completed ?? false,
        dueDate: data.dueDate,
        priority: data.priority,
        createdAt: data.createdAt || new Date().toISOString()
      });
    });
    list.sort((a, b) => {
      if (a.isCompleted !== b.isCompleted) return a.isCompleted ? 1 : -1;
      return new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime();
    });
    callback(list);
  }, (err) => {
    console.error('Error subscribing to todos:', err);
    callback([]);
  });
};

export const saveTodo = async (todo: TodoItem): Promise<void> => {
  if (!todo.ownerId) throw new Error('Cannot save todo without a valid ownerId');
  const docRef = doc(db, 'todos', todo.id);
  await setDoc(docRef, {
    ...todo,
    createdAt: todo.createdAt || new Date().toISOString()
  }, { merge: true });
};

export const deleteTodo = async (todoId: string): Promise<void> => {
  await deleteDoc(doc(db, 'todos', todoId));
};
