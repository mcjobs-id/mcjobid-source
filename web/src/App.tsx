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

import type { Booking, Expense, Payment, Client, RateCard, TodoItem } from './types';
import {
  subscribeBookings, saveBooking, deleteBooking,
  subscribeExpenses, saveExpense, deleteExpense,
  subscribePayments, savePayment, deletePayment,
  subscribeClients, saveClient, deleteClient,
  subscribeRateCards, saveRateCard, deleteRateCard,
  saveUserProfile,
  subscribeTodos, saveTodo, deleteTodo
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
  if (path.startsWith('/agenda')) activeTab = 'agenda';
  if (path.startsWith('/clients')) activeTab = 'clients';
  if (path.startsWith('/finance')) activeTab = 'finance';
  if (
    path.startsWith('/more') ||
    path.startsWith('/price-list') ||
    path.startsWith('/profile') ||
    path.startsWith('/analytics') ||
    path.startsWith('/followup') ||
    path.startsWith('/todo') ||
    path.startsWith('/notifications') ||
    path.startsWith('/quick-action') ||
    path.startsWith('/testimonial')
  ) activeTab = 'more';

  const isDayMode = path.startsWith('/daymode');
  const hideBottomNav = isDayMode;

  const getPageTitle = () => {
    if (path.startsWith('/booking/')) return 'Detail Job Acara';
    if (path.startsWith('/invoice')) return 'Generator Invoice PDF';
    if (path.startsWith('/price-list')) return 'Rate Card & Price List';
    if (path.startsWith('/profile')) return 'Profil MC Studio';
    if (path.startsWith('/analytics')) return 'Analisis Performa Bisnis';
    if (path.startsWith('/followup')) return 'Pusat Follow Up Klien';
    if (path.startsWith('/todo')) return 'Daftar Tugas To-Do';
    if (path.startsWith('/notifications')) return 'Pengingat & Notifikasi';
    if (path.startsWith('/quick-action')) return 'Pengaturan Pintasan';
    if (path.startsWith('/testimonial')) return 'Testimoni Klien';
    switch (activeTab) {
      case 'home': return 'mcjob.id';
      case 'agenda': return 'Agenda Acara';
      case 'clients': return 'Database Klien & WO';
      case 'finance': return 'Keuangan & Cashflow';
      case 'more': return 'Menu Lainnya';
      default: return 'mcjob.id';
    }
  };

  return (
    <div className="app-layout">
      {!isDayMode && (
        <Sidebar
          activeTab={activeTab}
          onChangeTab={(t) => navigate(`/${t}`)}
          onOpenCreateJob={() => setShowWizardModal(true)}
        />
      )}
      <div className="main-area">
        {!isDayMode && (
          <Navbar
            title={getPageTitle()}
          />
        )}
        <main className="content-area">{children}</main>
      </div>
      {!isDayMode && !hideBottomNav && (
        <div className="md:hidden" style={{ display: 'block' }}>
          <BottomNav
            activeTab={activeTab}
            onChangeTab={(t) => navigate(`/${t}`)}
          />
        </div>
      )}
    </div>
  );
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
  const { currentUser, authState, loading } = useAuth();

  const [showWizardModal, setShowWizardModal] = useState(false);

  // Firestore collections
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [rateCards, setRateCards] = useState<RateCard[]>([]);
  const [todos, setTodos] = useState<TodoItem[]>([]);



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
    ];
    return () => unsubs.forEach(u => u());
  }, [currentUser, authState]);

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
          <button className="btn btn-secondary" onClick={useAuth().logout}>Keluar</button>
        </div>
      </div>
    );
  }

  // ── Handlers ─────────────────────────────────────────────────

  const handleSaveBooking = async (b: Booking) => {
    if (currentUser) await saveBooking({ ...b, ownerId: currentUser.uid });
  };
  const handleDeleteBooking = async (id: string) => {
    await deleteBooking(id);
  };

  const handleSaveExpense = async (e: Expense) => {
    if (currentUser) await saveExpense({ ...e, ownerId: currentUser.uid });
  };
  const handleDeleteExpense = async (id: string) => {
    await deleteExpense(id);
  };

  const handleSavePayment = async (p: Payment) => {
    if (currentUser) await savePayment({ ...p, ownerId: currentUser.uid });
  };
  const handleDeletePayment = async (id: string) => {
    await deletePayment(id);
  };

  const handleSaveClient = async (c: Client) => {
    if (currentUser) await saveClient({ ...c, ownerId: currentUser.uid });
  };
  const handleDeleteClient = async (id: string) => {
    await deleteClient(id);
  };

  const handleSaveRateCard = async (rc: RateCard) => {
    if (currentUser) await saveRateCard({ ...rc, ownerId: currentUser.uid });
  };
  const handleDeleteRateCard = async (id: string) => {
    await deleteRateCard(id);
  };

  const handleSaveTodo = async (t: TodoItem) => {
    if (currentUser) await saveTodo({ ...t, ownerId: currentUser.uid });
  };
  const handleDeleteTodo = async (id: string) => {
    await deleteTodo(id);
  };

  const contextValue = {
    setShowWizardModal,
    bookings, expenses, payments, clients, rateCards, todos,
    handleSaveBooking, handleDeleteBooking,
    handleSaveExpense, handleDeleteExpense,
    handleSavePayment, handleDeletePayment,
    handleSaveClient, handleDeleteClient,
    handleSaveRateCard, handleDeleteRateCard,
    handleSaveTodo, handleDeleteTodo,
    currentUserId: currentUser.uid,
  };

  return (
    <BrowserRouter>
      <OutletContext.Provider value={contextValue}>
        <AppLayout>
          <Routes>
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/home" element={<HomePageWrapper />} />
            <Route path="/agenda" element={<AgendaPageWrapper />} />
            <Route path="/clients" element={<ClientsPageWrapper />} />
            <Route path="/finance" element={<FinancePageWrapper />} />
            <Route path="/more" element={<MorePageWrapper />} />
            <Route path="/booking/:id" element={<BookingDetailWrapper />} />
            <Route path="/invoice/:id?" element={<InvoicePageWrapper />} />
            <Route path="/price-list" element={<PriceListPageWrapper />} />
            <Route path="/profile" element={<ProfilePageWrapper />} />
            <Route path="/analytics" element={<AnalyticsPageWrapper />} />
            <Route path="/followup" element={<FollowUpPageWrapper />} />
            <Route path="/todo" element={<TodoPageWrapper />} />
            <Route path="/notifications" element={<NotificationPageWrapper />} />
            <Route path="/quick-action" element={<QuickActionSettingsPageWrapper />} />
            <Route path="/daymode/:id?" element={<DayModeWrapper />} />
            <Route path="*" element={<Navigate to="/home" replace />} />
          </Routes>
        </AppLayout>

        {showWizardModal && (
          <WizardPage
            onClose={() => setShowWizardModal(false)}
            onSave={async (b) => {
              await handleSaveBooking(b);
              setShowWizardModal(false);
            }}
            clients={contextValue.clients}
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
      onNavigateTab={(t) => navigate(`/${t}`)}
      onOpenCreateJob={() => ctx.setShowWizardModal(true)}
      onOpenBookingDetail={(b) => navigate(`/booking/${b.id}`)}
      onOpenDayMode={(b) => navigate(`/daymode/${b.id}`)}
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
      onOpenDetail={(b) => navigate(`/booking/${b.id}`)}
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
  return (
    <FinancePage
      bookings={ctx.bookings}
      expenses={ctx.expenses}
      payments={ctx.payments}
      onSaveExpense={ctx.handleSaveExpense}
      onDeleteExpense={ctx.handleDeleteExpense}
      onSavePayment={ctx.handleSavePayment}
      onDeletePayment={ctx.handleDeletePayment}
      currentUserId={ctx.currentUserId}
    />
  );
};

const MorePageWrapper = () => {
  const navigate = useNavigate();
  return <MorePage onNavigateTab={(t) => navigate(`/${t}`)} />;
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
      onBack={() => navigate(-1)}
      onEdit={ctx.handleSaveBooking}
      onDelete={async (bookingId: string) => {
        await ctx.handleDeleteBooking(bookingId);
        navigate(-1);
      }}
      onOpenMcDayMode={(b) => navigate(`/daymode/${b.id}`)}
      onOpenInvoice={(b) => navigate(`/invoice/${b.id}`)}
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
  return <InvoicePage booking={booking} allBookings={ctx.bookings} payments={ctx.payments} onBack={() => navigate(-1)} />;
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

// ── Root ───────────────────────────────────────────────────────
export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
