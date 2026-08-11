export interface UserProfile {
  uid: string;
  name: string;
  stageName?: string;
  email: string;
  phone?: string;
  bio?: string;
  photoUrl?: string;
  bankName?: string;
  bankAccount?: string;
  bankHolder?: string;
  instagram?: string;
  tiktok?: string;
  profileCompleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Booking {
  id: string;
  ownerId: string;
  name: string;
  client?: string;
  clientId?: string;
  category?: string;
  date: string; // YYYY-MM-DD
  start?: string; // HH:mm
  end?: string; // HH:mm
  loc?: string;
  address?: string;
  dresscode?: string;
  theme?: string;
  mcType?: string; // Single / Duet
  language?: string;
  audience?: string;
  specialRequest?: string;
  pic?: string;
  fee: number;
  dp: number;
  note?: string;
  status: 'draft' | 'confirmed' | 'upcoming' | 'today' | 'completed' | 'cancelled' | 'active' | 'done';
  createdAt: string;
  updatedAt: string;
}

export interface Client {
  id: string;
  ownerId: string;
  name: string;
  company?: string;
  phone?: string;
  email?: string;
  instagram?: string;
  notes?: string;
  totalBookings?: number;
  createdAt: string;
  updatedAt: string;
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
  bankDetails: {
    bankName: string;
    accountNumber: string;
    accountHolder: string;
  };
  notes?: string;
  status: 'unpaid' | 'partial' | 'paid';
  createdAt: string;
  updatedAt: string;
}

export interface Expense {
  id: string;
  ownerId: string;
  bookingId?: string;
  title: string;
  category: string;
  amount: number;
  date: string;
  notes?: string;
  createdAt: string;
}

export interface Payment {
  id: string;
  ownerId: string;
  bookingId: string;
  amount: number;
  date: string;
  paymentMethod: string;
  note?: string;
  createdAt: string;
}

export interface RateCard {
  id: string;
  ownerId: string;
  title: string;
  category: string;
  price: number;
  duration?: string;
  inclusions: string[];
  description?: string;
  isPopular?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Testimonial {
  id: string;
  userId: string; // MC owner ID
  clientName: string;
  eventName: string;
  rating: number; // 1-5
  comment: string;
  date: string;
  createdAt: string;
}

export interface ChecklistItem {
  id: string;
  ownerId: string;
  bookingId: string;
  title: string;
  time?: string;
  isCompleted: boolean;
  order: number;
  createdAt: string;
}

export interface TodoItem {
  id: string;
  ownerId: string;
  title: string;
  dueDate?: string;
  isCompleted: boolean;
  priority: 'low' | 'medium' | 'high';
  createdAt: string;
}
