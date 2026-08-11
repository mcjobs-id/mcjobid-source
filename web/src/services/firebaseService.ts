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

// --- User Profile ---
export const getUserProfile = async (uid: string): Promise<UserProfile | null> => {
  const docRef = doc(db, 'users', uid);
  const snap = await getDoc(docRef);
  if (snap.exists()) {
    return snap.data() as UserProfile;
  }
  return null;
};

export const saveUserProfile = async (profile: UserProfile): Promise<void> => {
  const docRef = doc(db, 'users', profile.uid);
  await setDoc(docRef, {
    ...profile,
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
      list.push(doc.data() as Booking);
    });
    // Sort by date descending locally
    list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    callback(list);
  });
};

export const saveBooking = async (booking: Booking): Promise<void> => {
  const docRef = doc(db, 'bookings', booking.id);
  const now = new Date().toISOString();
  const data = {
    ...booking,
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
      list.push(doc.data() as Client);
    });
    list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    callback(list);
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
      list.push(doc.data() as Invoice);
    });
    callback(list);
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
      list.push(doc.data() as Expense);
    });
    list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    callback(list);
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
      list.push(doc.data() as RateCard);
    });
    callback(list);
  });
};

export const saveRateCard = async (rateCard: RateCard): Promise<void> => {
  const docRef = doc(db, 'rate_cards', rateCard.id);
  const now = new Date().toISOString();
  await setDoc(docRef, {
    ...rateCard,
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
  });
};

export const saveTestimonial = async (testimonial: Testimonial): Promise<void> => {
  const docRef = doc(db, 'testimonials', testimonial.id);
  await setDoc(docRef, testimonial, { merge: true });
};
