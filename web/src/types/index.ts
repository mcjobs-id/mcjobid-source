// ─────────────────────────────────────────────────────────────
// MCJOB.ID — Core Domain Types (aligned with Android models)
// ─────────────────────────────────────────────────────────────

export interface UserProfile {
  uid: string;
  // Identity
  name?: string;          // alias for displayName (backward compat)
  displayName?: string;
  stageName?: string;
  bio?: string;
  // Location & Coverage
  city?: string;
  areaCoverage?: string;
  // Professional
  specialization?: string;
  languages?: string;
  experienceYears?: string;
  // Contact
  email: string;
  phone?: string;            // alias for phoneNumber
  phoneNumber?: string;
  secondaryPhone?: string;
  // Media
  photoUrl?: string;
  photoUri?: string;
  instagram?: string;
  instagramHandle?: string;
  tiktok?: string;
  // Banking
  bankName?: string;
  bankAccount?: string;       // alias for bankAccountNumber
  bankAccountNumber?: string;
  bankHolder?: string;        // alias for bankAccountHolder
  bankAccountHolder?: string;
  secondaryBankInfo?: string;
  // Business Settings
  baseFee?: number;
  defaultDpPercentage?: number;
  npwpNumber?: string;
  termsAndConditions?: string;
  // Metadata
  profileCompleted: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Booking status values — must match Android BookingStatus enum (lowercase stored in Firestore)
export type BookingStatus =
  | 'draft' | 'confirmed' | 'upcoming' | 'today' | 'active' | 'completed' | 'cancelled'
  | 'DRAFT' | 'CONFIRMED' | 'UPCOMING' | 'TODAY' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  | 'done';

export type PaymentStatus = 'TBD' | 'UNPAID' | 'PARTIAL' | 'PAID' | 'OVERDUE'
  | 'unpaid' | 'partial' | 'paid';

export interface Booking {
  id: string;
  ownerId: string;

  // Event Name / Title (Android: `name`)
  name?: string;
  eventTitle?: string;    // normalized alias

  // Client info
  client?: string;        // client name (Android field)
  clientName?: string;    // normalized alias
  clientId?: string;

  // Category & Status
  category?: string;
  status: BookingStatus;
  paymentStatus?: PaymentStatus;

  // Dates & Times
  date?: string;          // YYYY-MM-DD (Android: `date`)
  eventDate?: string;     // normalized alias
  start?: string;         // HH:mm
  eventTime?: string;     // normalized alias
  end?: string;           // HH:mm

  // Venue & Location
  location?: string;      // venue name (Android: `location`)
  venue?: string;         // normalized alias
  loc?: string;           // legacy alias
  address?: string;       // full address

  // MC Specification
  dresscode?: string;
  theme?: string;
  mcType?: string;        // 'Single' | 'Duet' | etc.
  language?: string;      // 'Bahasa Indonesia' | 'English' | 'Bilingual'
  audience?: string;      // estimated audience count
  specialRequest?: string;
  pic?: string;           // PIC contact / phone number

  // Financials (Android: fee, dp)
  fee?: number;           // total honor (Android: `fee`)
  totalFee?: number;      // normalized alias
  dp?: number;            // down payment / paid amount (Android: `dp`)
  dpAmount?: number;      // normalized alias

  // Notes
  note?: string;          // internal MC note (Android: `note`)
  notes?: string;         // normalized alias

  // Metadata
  createdAt?: string;
  updatedAt?: string;
}

export interface Client {
  id: string;
  ownerId: string;
  name: string;
  type?: 'DIRECT_CLIENT' | 'WO' | 'EO' | 'OTHER' | string;
  company?: string;
  phone?: string;
  email?: string;
  address?: string;
  instagram?: string;
  pic?: string;           // photo URL
  notes?: string;
  isFavorite?: boolean;
  isArchived?: boolean;
  totalBookings?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Invoice {
  id: string;
  ownerId: string;
  bookingId: string;
  invoiceNumber: string;
  clientName: string;
  eventName: string;
  eventDate: string;
  totalFee: number;
  dpAmount: number;
  remainingAmount: number;
  dueDate: string;
  bankDetails?: {
    bankName: string;
    accountNumber: string;
    accountHolder: string;
  };
  notes?: string;
  status: 'unpaid' | 'partial' | 'paid';
  createdAt?: string;
  updatedAt?: string;
}

export interface Expense {
  id: string;
  ownerId: string;
  bookingId?: string;     // optional: link to a specific booking
  title: string;
  category: string;
  amount: number;
  date: string;
  notes?: string;
  note?: string;          // Android alias
  createdAt?: string;
}

// Separate Payment records (distinct from DP field on Booking)
// Android: id, bookingId, amount, paymentDate, paymentMethod, notes
export interface Payment {
  id: string;
  ownerId: string;
  bookingId: string;
  amount: number;
  paymentDate: string;    // YYYY-MM-DD (Android: paymentDate)
  date?: string;          // normalized alias
  paymentMethod: string;  // 'Transfer Bank' | 'Cash' | 'QRIS' | etc.
  note?: string;
  notes?: string;         // alias
  createdAt?: string;
}

export interface RateCard {
  id: string;
  ownerId: string;
  name?: string;
  title?: string;
  category?: string;
  price: number;
  duration?: string;
  features?: string[];
  inclusions?: string[];
  notes?: string;
  description?: string;
  isPopular?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Testimonial {
  id: string;
  userId: string;
  clientName: string;
  eventName: string;
  rating: number;
  comment: string;
  date: string;
  createdAt?: string;
}

export interface ChecklistItem {
  id: string;
  ownerId: string;
  bookingId: string;
  title: string;
  time?: string;
  isCompleted: boolean;
  order: number;
  createdAt?: string;
}

export interface TodoItem {
  id: string;
  ownerId: string;
  title: string;
  dueDate?: string;
  isCompleted: boolean;
  priority?: 'low' | 'medium' | 'high';
  createdAt?: string;
}

// ── Helpers ──────────────────────────────────────────────────

/** Get canonical event name from a booking */
export function getBookingName(b: Booking): string {
  return b.name || b.eventTitle || b.clientName || b.client || 'Acara MC';
}

/** Get canonical client name from a booking */
export function getBookingClient(b: Booking): string {
  return b.client || b.clientName || '';
}

/** Get canonical event date from a booking */
export function getBookingDate(b: Booking): string {
  return b.date || b.eventDate || '';
}

/** Get canonical start time from a booking */
export function getBookingStart(b: Booking): string {
  return b.start || b.eventTime || '';
}

/** Get canonical venue name from a booking */
export function getBookingVenue(b: Booking): string {
  return b.location || b.venue || b.loc || '';
}

/** Get canonical total fee from a booking */
export function getBookingFee(b: Booking): number {
  return Number(b.fee ?? b.totalFee ?? 0);
}

/** Get canonical dp/paid amount from a booking */
export function getBookingDp(b: Booking): number {
  return Number(b.dp ?? b.dpAmount ?? 0);
}

/** Calculate outstanding from booking (no payment records) */
export function getBookingOutstanding(b: Booking): number {
  return Math.max(0, getBookingFee(b) - getBookingDp(b));
}

/** Canonical payment status derived from booking fields */
export function derivePaymentStatus(b: Booking): 'TBD' | 'UNPAID' | 'PARTIAL' | 'PAID' | 'OVERDUE' {
  const fee = getBookingFee(b);
  const dp = getBookingDp(b);
  const dateStr = getBookingDate(b);
  const today = new Date().toISOString().split('T')[0];
  if (fee === 0 && dp === 0) return 'TBD';
  if (fee > 0 && dp >= fee) return 'PAID';
  if (dateStr && dateStr < today && dp < fee && fee > 0) return 'OVERDUE';
  if (dp > 0 && dp < fee) return 'PARTIAL';
  if (dp === 0 && fee > 0) return 'UNPAID';
  return 'TBD';
}

/** Normalize status to lowercase for comparison */
export function normalizeStatus(s: string): string {
  return (s || '').toLowerCase();
}
