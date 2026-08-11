import React, { useState, useEffect, createContext, useContext } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation, useParams } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar, type TabType } from './components/Sidebar';
import { Navbar } from './components/Navbar';
import { BottomNav } from './components/BottomNav';

import { LoginPage } from './pages/LoginPage';
import { WizardPage } from './pages/WizardPage';
import { HomePage } from './pages/HomePage';
import { AgendaPage } from './pages/AgendaPage';
import { ClientsPage } from './pages/ClientsPage';
import { FinancePage } from './pages/FinancePage';
import { MorePage } from './pages/MorePage';
import { BookingDetailPage } from './pages/BookingDetailPage';
import { McDayModePage } from './pages/McDayModePage';
import { InvoicePage } from './pages/InvoicePage';
import { PriceListPage } from './pages/PriceListPage';
import { ProfilePage } from './pages/ProfilePage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { FollowUpPage } from './pages/FollowUpPage';
import { TodoPage } from './pages/TodoPage';
import { NotificationPage } from './pages/NotificationPage';
import { QuickActionSettingsPage } from './pages/QuickActionSettingsPage';
import { TestimonialPage } from './pages/TestimonialPage';

import type { Booking, Expense, Payment, Client, RateCard, TodoItem, Testimonial } from './types';
import {
  subscribeBookings, saveBooking, deleteBooking,
  subscribeExpenses, saveExpense, deleteExpense,
  subscribePayments, savePayment, deletePayment,
  subscribeClients, saveClient, deleteClient,
  subscribeRateCards, saveRateCard, deleteRateCard,
  saveUserProfile,
  subscribeTodos, saveTodo, deleteTodo,
  subscribeTestimonials, saveTestimonial, deleteTestimonial,
  saveInvoice
} from './services/firebaseService';

// Global outlet context
const OutletContext = createContext<any>(null);
const useOutletContext = () => useContext(OutletContext);

// ── AppLayout ─────────────────────────────────────────────────
const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setShowWizardModal } = useOutletContext();

  const path = location.pathname;
  let activeTab: TabType = 'home';
  if (path.includes('/agenda')) activeTab = 'agenda';
  if (path.includes('/clients')) activeTab = 'clients';
  if (path.includes('/finance')) activeTab = 'finance';
  if (
    path.includes('/more') ||
    path.includes('/price-list') ||
    path.includes('/profile') ||
    path.includes('/analytics') ||
    path.includes('/followup') ||
    path.includes('/todo') ||
    path.includes('/notifications') ||
    path.includes('/quick-action') ||
    path.includes('/testimonial')
  ) activeTab = 'more';

  const isDayMode = path.includes('/daymode');
  const hideBottomNav = isDayMode;
  const isSubPage = !['/user/home', '/user/agenda', '/user/clients', '/user/finance', '/user/more', '/home', '/agenda', '/clients', '/finance', '/more', '/', '/user'].includes(path);

  const getPageTitle = () => {
    if (path.includes('/booking/')) return 'Detail Job Acara';
    if (path.includes('/invoice')) return 'Generator Invoice PDF';
    if (path.includes('/price-list')) return 'Rate Card & Price List';
    if (path.includes('/profile')) return 'Profil MC Studio';
    if (path.includes('/analytics')) return 'Analisis Performa Bisnis';
    if (path.includes('/followup')) return 'Pusat Follow Up Klien';
    if (path.includes('/todo')) return 'Daftar Tugas To-Do';
    if (path.includes('/notifications')) return 'Pengingat & Notifikasi';
    if (path.includes('/quick-action')) return 'Pengaturan Pintasan';
    if (path.includes('/testimonial')) return 'Testimoni Klien';
    switch (activeTab) {
      case 'home': return 'Beranda';
      case 'agenda': return 'Agenda Acara';
      case 'clients': return 'Database Klien & WO';
      case 'finance': return 'Keuangan & Cashflow';
      case 'more': return 'Menu Lainnya';
      default: return 'Beranda';
    }
  };

  return (
    <div className={`app-layout${isDayMode ? ' daymode-active' : ''}`}>
      {!isDayMode && (
        <Sidebar
          activeTab={activeTab}
          onChangeTab={(t) => navigate(`/user/${t}`)}
          onOpenCreateJob={() => setShowWizardModal(true)}
        />
      )}
      <div className="main-area">
        {!isDayMode && (
          <Navbar
            title={getPageTitle()}
            showBack={isSubPage}
            onBack={() => navigate(-1)}
          />
        )}
        <main className="content-area">{children}</main>
      </div>
      {!isDayMode && !hideBottomNav && (
        <BottomNav
          activeTab={activeTab}
          onChangeTab={(t) => navigate(`/user/${t}`)}
        />
      )}
    </div>
  );
};

// ── Scroll To Top on Route Change ────────────────────────────
const ScrollToTop: React.FC = () => {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
    const resetScroll = () => {
      const contentEl = document.querySelector('.content-area');
      if (contentEl) contentEl.scrollTop = 0;
    };
    resetScroll();
    const animId = requestAnimationFrame(resetScroll);
    return () => cancelAnimationFrame(animId);
  }, [pathname]);
  return null;
};

// ── Loading Screen ─────────────────────────────────────────────
const AppLoadingScreen = () => (
  <div style={{
    minHeight: '100vh', display: 'flex', flexDirection: 'column',
    alignItems: 'center', justifyContent: 'center', gap: '16px',
    background: 'var(--bg-app)'
  }}>
    <div style={{
      width: '56px', height: '56px', borderRadius: '16px',
      background: 'var(--primary)', display: 'flex', alignItems: 'center',
      justifyContent: 'center', fontWeight: '800', color: 'white',
      fontSize: '20px', boxShadow: '0 8px 24px rgba(79,70,229,0.35)',
      animation: 'pulse 2s infinite'
    }}>MC</div>
    <div style={{ textAlign: 'center' }}>
      <p style={{ fontSize: '14px', fontWeight: '600', color: 'var(--text-1)', marginBottom: '4px' }}>
        mcjob.id
      </p>
      <p style={{ fontSize: '12px', color: 'var(--text-4)' }}>Memuat aplikasi...</p>
    </div>
    <style>{`@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.7} }`}</style>
  </div>
);

// ── Main App ───────────────────────────────────────────────────
const MainApp: React.FC = () => {
  const { currentUser, authState, loading, logout } = useAuth();

  const [showWizardModal, setShowWizardModal] = useState(false);
  const [selectedRateCard, setSelectedRateCard] = useState<RateCard | null>(null);

  // Firestore collections
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [rateCards, setRateCards] = useState<RateCard[]>([]);
  const [todos, setTodos] = useState<TodoItem[]>([]);
  const [testimonials, setTestimonials] = useState<Testimonial[]>([]);

  useEffect(() => {
    // Only subscribe to user data if the profile is fully ready.
    if (authState !== 'PROFILE_READY' || !currentUser) return;
    const uid = currentUser.uid;
    const unsubs = [
      subscribeBookings(uid, setBookings),
      subscribeExpenses(uid, setExpenses),
      subscribePayments(uid, setPayments),
      subscribeClients(uid, setClients),
      subscribeRateCards(uid, setRateCards),
      subscribeTodos(uid, setTodos),
      subscribeTestimonials(uid, setTestimonials),
    ];
    return () => unsubs.forEach(u => u());
  }, [currentUser, authState]);

  useEffect(() => {
    if (typeof window !== 'undefined' && window.location.hash.startsWith('#/')) {
      const rawPath = window.location.hash.replace('#/', '');
      const cleanPath = rawPath.startsWith('user/') ? rawPath : `user/${rawPath}`;
      window.history.replaceState(null, '', `/${cleanPath}`);
    }
  }, []);

  // Show loading during auth initialization
  if (loading) return <AppLoadingScreen />;

  // Show login if not authenticated
  if (authState === 'UNAUTHENTICATED' || !currentUser) {
    return <LoginPage />;
  }

  // Recoverable profile error screen
  if (authState === 'PROFILE_ERROR') {
    return (
      <div style={{
        minHeight: '100vh', display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center', gap: '16px',
        background: 'var(--bg-app)', padding: '24px', textAlign: 'center'
      }}>
        <h2 style={{ fontSize: '20px', fontWeight: 'bold', color: 'var(--text-1)' }}>Gagal Memuat Profil</h2>
        <p style={{ color: 'var(--text-3)', maxWidth: '400px' }}>
          Terjadi masalah saat membaca data profil Anda. Pastikan koneksi internet stabil atau coba masuk kembali.
        </p>
        <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
          <button className="btn btn-primary" onClick={() => window.location.reload()}>Coba Lagi</button>
          <button className="btn btn-secondary" onClick={logout}>Keluar</button>
        </div>
      </div>
    );
  }

  // ── Handlers ─────────────────────────────────────────────────

  const handleSaveBooking = async (b: Booking) => {
    setBookings(prev => {
      const idx = prev.findIndex(item => item.id === b.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = b;
        return updated;
      }
      return [b, ...prev];
    });
    if (currentUser) await saveBooking({ ...b, ownerId: currentUser.uid });
  };
  const handleDeleteBooking = async (id: string) => {
    setBookings(prev => prev.filter(b => b.id !== id));
    await deleteBooking(id);
  };

  const handleSaveInvoice = async (inv: any) => {
    if (currentUser) await saveInvoice({ ...inv, ownerId: currentUser.uid });
  };

  const handleSaveExpense = async (e: Expense) => {
    setExpenses(prev => {
      const idx = prev.findIndex(item => item.id === e.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = e;
        return updated;
      }
      return [e, ...prev];
    });
    if (currentUser) await saveExpense({ ...e, ownerId: currentUser.uid });
  };
  const handleDeleteExpense = async (id: string) => {
    setExpenses(prev => prev.filter(e => e.id !== id));
    await deleteExpense(id);
  };

  const handleSavePayment = async (p: Payment) => {
    setPayments(prev => {
      const idx = prev.findIndex(item => item.id === p.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = p;
        return updated;
      }
      return [p, ...prev];
    });
    if (currentUser) await savePayment({ ...p, ownerId: currentUser.uid });
  };
  const handleDeletePayment = async (id: string) => {
    setPayments(prev => prev.filter(p => p.id !== id));
    await deletePayment(id);
  };

  const handleSaveClient = async (c: Client) => {
    setClients(prev => {
      const idx = prev.findIndex(item => item.id === c.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = c;
        return updated;
      }
      return [c, ...prev];
    });
    if (currentUser) await saveClient({ ...c, ownerId: currentUser.uid });
  };
  const handleDeleteClient = async (id: string) => {
    setClients(prev => prev.filter(c => c.id !== id));
    await deleteClient(id);
  };

  const handleSaveRateCard = async (rc: RateCard) => {
    setRateCards(prev => {
      const idx = prev.findIndex(item => item.id === rc.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = rc;
        return updated;
      }
      return [rc, ...prev];
    });
    if (currentUser) await saveRateCard({ ...rc, ownerId: currentUser.uid });
  };
  const handleDeleteRateCard = async (id: string) => {
    setRateCards(prev => prev.filter(rc => rc.id !== id));
    await deleteRateCard(id);
  };

  const handleSaveTodo = async (t: TodoItem) => {
    setTodos(prev => {
      const idx = prev.findIndex(item => item.id === t.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = t;
        return updated;
      }
      return [t, ...prev];
    });
    if (currentUser) await saveTodo({ ...t, ownerId: currentUser.uid });
  };
  const handleDeleteTodo = async (id: string) => {
    setTodos(prev => prev.filter(t => t.id !== id));
    await deleteTodo(id);
  };

  const handleSaveTestimonial = async (t: Testimonial) => {
    setTestimonials(prev => {
      const idx = prev.findIndex(item => item.id === t.id);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = t;
        return updated;
      }
      return [t, ...prev];
    });
    if (currentUser) await saveTestimonial({ ...t, userId: currentUser.uid });
  };
  const handleDeleteTestimonial = async (id: string) => {
    setTestimonials(prev => prev.filter(t => t.id !== id));
    await deleteTestimonial(id);
  };

  const contextValue = {
    setShowWizardModal,
    setSelectedRateCard,
    bookings, expenses, payments, clients, rateCards, todos, testimonials,
    handleSaveBooking, handleDeleteBooking,
    handleSaveExpense, handleDeleteExpense,
    handleSavePayment, handleDeletePayment,
    handleSaveClient, handleDeleteClient,
    handleSaveRateCard, handleDeleteRateCard,
    handleSaveTodo, handleDeleteTodo,
    handleSaveTestimonial, handleDeleteTestimonial,
    handleSaveInvoice,
    currentUserId: currentUser.uid,
  };

  return (
    <BrowserRouter>
      <ScrollToTop />
      <OutletContext.Provider value={contextValue}>
        <AppLayout>
          <Routes>
            {/* Automatic Redirects for root & legacy non-user paths */}
            <Route path="/" element={<Navigate to="/user/home" replace />} />
            <Route path="/user" element={<Navigate to="/user/home" replace />} />
            <Route path="/user/" element={<Navigate to="/user/home" replace />} />
            <Route path="/home" element={<Navigate to="/user/home" replace />} />
            <Route path="/agenda" element={<Navigate to="/user/agenda" replace />} />
            <Route path="/clients" element={<Navigate to="/user/clients" replace />} />
            <Route path="/finance" element={<Navigate to="/user/finance" replace />} />
            <Route path="/more" element={<Navigate to="/user/more" replace />} />
            <Route path="/price-list" element={<Navigate to="/user/price-list" replace />} />
            <Route path="/profile" element={<Navigate to="/user/profile" replace />} />
            <Route path="/analytics" element={<Navigate to="/user/analytics" replace />} />
            <Route path="/followup" element={<Navigate to="/user/followup" replace />} />
            <Route path="/todo" element={<Navigate to="/user/todo" replace />} />
            <Route path="/notifications" element={<Navigate to="/user/notifications" replace />} />
            <Route path="/quick-action" element={<Navigate to="/user/quick-action" replace />} />
            <Route path="/testimonial" element={<Navigate to="/user/testimonial" replace />} />
            <Route path="/testimonials" element={<Navigate to="/user/testimonial" replace />} />

            {/* Primary /user Routes */}
            <Route path="/user/home" element={<HomePageWrapper />} />
            <Route path="/user/agenda" element={<AgendaPageWrapper />} />
            <Route path="/user/clients" element={<ClientsPageWrapper />} />
            <Route path="/user/finance" element={<FinancePageWrapper />} />
            <Route path="/user/more" element={<MorePageWrapper />} />
            <Route path="/user/booking/:id" element={<BookingDetailWrapper />} />
            <Route path="/user/invoice/:id?" element={<InvoicePageWrapper />} />
            <Route path="/user/price-list" element={<PriceListPageWrapper />} />
            <Route path="/user/profile" element={<ProfilePageWrapper />} />
            <Route path="/user/analytics" element={<AnalyticsPageWrapper />} />
            <Route path="/user/followup" element={<FollowUpPageWrapper />} />
            <Route path="/user/todo" element={<TodoPageWrapper />} />
            <Route path="/user/notifications" element={<NotificationPageWrapper />} />
            <Route path="/user/quick-action" element={<QuickActionSettingsPageWrapper />} />
            <Route path="/user/testimonial" element={<TestimonialPageWrapper />} />
            <Route path="/user/testimonials" element={<TestimonialPageWrapper />} />
            <Route path="/user/daymode/:id?" element={<DayModeWrapper />} />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/user/home" replace />} />
          </Routes>
        </AppLayout>

        {showWizardModal && (
          <WizardPage
            onClose={() => {
              setShowWizardModal(false);
              setSelectedRateCard(null);
            }}
            onSave={async (b) => {
              await handleSaveBooking(b);
            }}
            onSaveClient={handleSaveClient}
            onSavePayment={handleSavePayment}
            clients={contextValue.clients}
            existingBookings={contextValue.bookings}
            rateCards={contextValue.rateCards}
            initialRateCard={selectedRateCard}
          />
        )}
      </OutletContext.Provider>
    </BrowserRouter>
  );
};

// ── Route Wrappers ─────────────────────────────────────────────

const HomePageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <HomePage
      bookings={ctx.bookings}
      onNavigateTab={(t) => navigate(`/user/${t}`)}
      onOpenCreateJob={() => ctx.setShowWizardModal(true)}
      onOpenBookingDetail={(b) => navigate(`/user/booking/${b.id}`)}
      onOpenDayMode={(b) => navigate(`/user/daymode/${b.id}`)}
    />
  );
};

const AgendaPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <AgendaPage
      bookings={ctx.bookings}
      onSaveBooking={ctx.handleSaveBooking}
      onOpenDetail={(b) => navigate(`/user/booking/${b.id}`)}
      onOpenCreateJob={() => ctx.setShowWizardModal(true)}
    />
  );
};

const ClientsPageWrapper = () => {
  const ctx = useOutletContext();
  return (
    <ClientsPage
      clients={ctx.clients}
      onSaveClient={ctx.handleSaveClient}
      onDeleteClient={ctx.handleDeleteClient}
    />
  );
};

const FinancePageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <FinancePage
      bookings={ctx.bookings}
      expenses={ctx.expenses}
      payments={ctx.payments}
      onSaveExpense={ctx.handleSaveExpense}
      onDeleteExpense={ctx.handleDeleteExpense}
      onSavePayment={ctx.handleSavePayment}
      onDeletePayment={ctx.handleDeletePayment}
      onSaveBooking={ctx.handleSaveBooking}
      currentUserId={ctx.currentUserId}
      onOpenBookingDetail={(id) => navigate(`/user/booking/${id}`)}
    />
  );
};

const MorePageWrapper = () => {
  const navigate = useNavigate();
  return <MorePage onNavigateTab={(t) => navigate(`/user/${t}`)} />;
};

const BookingDetailWrapper = () => {
  const { id } = useParams();
  const ctx = useOutletContext();
  const navigate = useNavigate();
  const booking = ctx.bookings.find((b: Booking) => b.id === id);

  if (!booking) {
    return (
      <div style={{ padding: '40px 24px', textAlign: 'center' }}>
        <p style={{ color: 'var(--text-3)', marginBottom: '16px' }}>
          {ctx.bookings.length === 0 ? 'Memuat data job...' : 'Job tidak ditemukan.'}
        </p>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>Kembali</button>
      </div>
    );
  }

  return (
    <BookingDetailPage
      booking={booking}
      clients={ctx.clients}
      payments={ctx.payments.filter((p: any) => p.bookingId === booking.id)}
      expenses={ctx.expenses.filter((e: any) => e.bookingId === booking.id)}
      onBack={() => navigate(-1)}
      onEdit={ctx.handleSaveBooking}
      onDelete={async (bookingId: string) => {
        await ctx.handleDeleteBooking(bookingId);
        navigate(-1);
      }}
      onOpenMcDayMode={(b) => navigate(`/user/daymode/${b.id}`)}
      onOpenInvoice={(b) => navigate(`/user/invoice/${b.id}`)}
      onSavePayment={ctx.handleSavePayment}
      onDeletePayment={ctx.handleDeletePayment}
      onSaveExpense={ctx.handleSaveExpense}
      onDeleteExpense={ctx.handleDeleteExpense}
      currentUserId={ctx.currentUserId}
    />
  );
};

const InvoicePageWrapper = () => {
  const { id } = useParams();
  const ctx = useOutletContext();
  const navigate = useNavigate();
  const booking = id
    ? ctx.bookings.find((b: Booking) => b.id === id)
    : ctx.bookings[0] || null;
  return <InvoicePage booking={booking} allBookings={ctx.bookings} payments={ctx.payments} onSaveInvoice={ctx.handleSaveInvoice} onBack={() => navigate(-1)} />;
};

const DayModeWrapper = () => {
  const { id } = useParams();
  const ctx = useOutletContext();
  const navigate = useNavigate();
  const booking = id ? ctx.bookings.find((b: Booking) => b.id === id) : null;
  const [selected, setSelected] = useState<Booking | null>(booking);

  useEffect(() => {
    if (booking && !selected) setSelected(booking);
  }, [booking, selected]);

  return (
    <McDayModePage
      booking={selected}
      allBookings={ctx.bookings}
      onBack={() => navigate(-1)}
      onSelectBooking={(b) => setSelected(b)}
    />
  );
};

const PriceListPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <PriceListPage
      rateCards={ctx.rateCards}
      onBack={() => navigate(-1)}
      onSaveRateCard={ctx.handleSaveRateCard}
      onDeleteRateCard={ctx.handleDeleteRateCard}
      onUseForJob={(card) => {
        if (ctx.setSelectedRateCard) ctx.setSelectedRateCard(card);
        ctx.setShowWizardModal(true);
      }}
    />
  );
};

const ProfilePageWrapper = () => {
  const navigate = useNavigate();
  return <ProfilePage onBack={() => navigate(-1)} />;
};

const AnalyticsPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <AnalyticsPage
      bookings={ctx.bookings}
      expenses={ctx.expenses}
      onBack={() => navigate(-1)}
    />
  );
};

const FollowUpPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <FollowUpPage
      bookings={ctx.bookings}
      clients={ctx.clients}
      onBack={() => navigate(-1)}
    />
  );
};

const TodoPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <TodoPage
      todos={ctx.todos}
      onSaveTodo={ctx.handleSaveTodo}
      onDeleteTodo={ctx.handleDeleteTodo}
      currentUserId={ctx.currentUserId}
      onBack={() => navigate(-1)}
    />
  );
};

const NotificationPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return <NotificationPage bookings={ctx.bookings} onBack={() => navigate(-1)} />;
};

const QuickActionSettingsPageWrapper = () => {
  const navigate = useNavigate();
  return <QuickActionSettingsPage onBack={() => navigate(-1)} />;
};

const TestimonialPageWrapper = () => {
  const ctx = useOutletContext();
  const navigate = useNavigate();
  return (
    <TestimonialPage
      testimonials={ctx.testimonials || []}
      onSaveTestimonial={ctx.handleSaveTestimonial}
      onDeleteTestimonial={ctx.handleDeleteTestimonial}
      onBack={() => navigate(-1)}
    />
  );
};

// ── Root ───────────────────────────────────────────────────────
export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
