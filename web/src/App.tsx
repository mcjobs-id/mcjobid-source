import React, { useState, useEffect } from 'react';
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

import type { Booking, Expense, Client, RateCard } from './types';
import {
  subscribeBookings,
  saveBooking,
  deleteBooking,
  subscribeExpenses,
  saveExpense,
  deleteExpense,
  subscribeClients,
  saveClient,
  deleteClient,
  subscribeRateCards,
  saveRateCard,
  deleteRateCard
} from './services/firebaseService';

const MainApp: React.FC = () => {
  const { currentUser, userProfile, loading } = useAuth();
  
  // 5 Main Tabs matching Android app: 'home' | 'agenda' | 'clients' | 'finance' | 'more'
  const [activeTab, setActiveTab] = useState<TabType>('home');
  
  // SubView router state
  const [subView, setSubView] = useState<
    | 'main' 
    | 'booking_detail' 
    | 'invoice' 
    | 'price_list' 
    | 'profile' 
    | 'testimonial' 
    | 'analytics' 
    | 'followup' 
    | 'todo' 
    | 'notifications' 
    | 'quick_action_settings'
  >('main');

  const [isDarkMode, setIsDarkMode] = useState(() => {
    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  });

  // State collections from Firestore
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [rateCards, setRateCards] = useState<RateCard[]>([]);

  // Selected Booking for detail / invoice / daymode
  const [selectedBooking, setSelectedBooking] = useState<Booking | null>(null);

  // Toggle Dark Mode
  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkMode]);

  // Subscribe to Firestore collections when logged in
  useEffect(() => {
    if (!currentUser) return;
    const uid = currentUser.uid;

    const unsubBookings = subscribeBookings(uid, (data) => setBookings(data));
    const unsubExpenses = subscribeExpenses(uid, (data) => setExpenses(data));
    const unsubClients = subscribeClients(uid, (data) => setClients(data));
    const unsubRateCards = subscribeRateCards(uid, (data) => setRateCards(data));

    return () => {
      unsubBookings();
      unsubExpenses();
      unsubClients();
      unsubRateCards();
    };
  }, [currentUser]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center space-y-3">
        <div className="w-12 h-12 rounded-2xl bg-indigo-600 animate-pulse flex items-center justify-center font-bold text-xl">
          MC
        </div>
        <p className="text-xs font-semibold text-slate-400">Memuat MCJobId Web App...</p>
      </div>
    );
  }

  if (!currentUser) {
    return <LoginPage />;
  }

  if (userProfile && !userProfile.profileCompleted) {
    return <WizardPage onComplete={() => setActiveTab('home')} />;
  }

  // Handlers for Save/Delete
  const handleSaveBooking = async (b: Booking) => {
    if (currentUser) {
      await saveBooking({ ...b, ownerId: currentUser.uid });
    }
  };

  const handleDeleteBooking = async (id: string) => {
    if (window.confirm('Yakin ingin menghapus acara ini?')) {
      await deleteBooking(id);
      setSubView('main');
    }
  };

  const handleSaveExpense = async (e: Expense) => {
    if (currentUser) {
      await saveExpense({ ...e, ownerId: currentUser.uid });
    }
  };

  const handleDeleteExpense = async (id: string) => {
    await deleteExpense(id);
  };

  const handleSaveClient = async (c: Client) => {
    if (currentUser) {
      await saveClient({ ...c, ownerId: currentUser.uid });
    }
  };

  const handleDeleteClient = async (id: string) => {
    await deleteClient(id);
  };

  const handleSaveRateCard = async (rc: RateCard) => {
    if (currentUser) {
      await saveRateCard({ ...rc, ownerId: currentUser.uid });
    }
  };

  const handleDeleteRateCard = async (id: string) => {
    await deleteRateCard(id);
  };

  // Helper for title header
  const getPageTitle = () => {
    if (subView === 'booking_detail') return 'Detail Job Acara';
    if (subView === 'invoice') return 'Generator Invoice PDF';
    if (subView === 'price_list') return 'Katalog Rate Card & Price List';
    if (subView === 'profile') return 'Profil MC Studio';
    if (subView === 'analytics') return 'Analisis Performa Bisnis';
    if (subView === 'followup') return 'Pusat Follow Up Klien';
    if (subView === 'todo') return 'Daftar Tugas To-Do MC';
    if (subView === 'notifications') return 'Pusat Pengingat';
    if (subView === 'quick_action_settings') return 'Pengaturan Pintasan FAB';

    switch (activeTab) {
      case 'home': return 'mcjob.id Dashboard';
      case 'agenda': return 'Agenda Acara Manggung';
      case 'clients': return 'Daftar Klien & WO';
      case 'finance': return 'Keuangan & Cashflow';
      case 'more': return 'Lainnya & Hub Bisnis';
      case 'daymode': return 'Mode Hari H (Panggung)';
      default: return 'mcjob.id';
    }
  };

  // SubView Router
  const renderContent = () => {
    if (subView === 'booking_detail' && selectedBooking) {
      return (
        <BookingDetailPage
          booking={selectedBooking}
          onBack={() => setSubView('main')}
          onOpenDayMode={(b) => {
            setSelectedBooking(b);
            setActiveTab('daymode');
            setSubView('main');
          }}
          onOpenInvoice={(b) => {
            setSelectedBooking(b);
            setSubView('invoice');
          }}
          onDeleteBooking={handleDeleteBooking}
          onUpdateBooking={handleSaveBooking}
        />
      );
    }

    if (subView === 'invoice' && selectedBooking) {
      return (
        <InvoicePage
          booking={selectedBooking}
          onBack={() => setSubView('booking_detail')}
        />
      );
    }

    if (subView === 'price_list') {
      return (
        <PriceListPage
          rateCards={rateCards}
          onSaveRateCard={handleSaveRateCard}
          onDeleteRateCard={handleDeleteRateCard}
        />
      );
    }

    if (subView === 'profile') {
      return <ProfilePage />;
    }

    if (subView === 'analytics') {
      return (
        <AnalyticsPage
          bookings={bookings}
          expenses={expenses}
          onBack={() => setSubView('main')}
        />
      );
    }

    if (subView === 'followup') {
      return (
        <FollowUpPage
          bookings={bookings}
          clients={clients}
          onBack={() => setSubView('main')}
        />
      );
    }

    if (subView === 'todo') {
      return <TodoPage onBack={() => setSubView('main')} />;
    }

    if (subView === 'notifications') {
      return <NotificationPage onBack={() => setSubView('main')} />;
    }

    if (subView === 'quick_action_settings') {
      return <QuickActionSettingsPage onBack={() => setSubView('main')} />;
    }

    // 5 Main Tabs
    switch (activeTab) {
      case 'home':
        return (
          <HomePage
            bookings={bookings}
            onNavigateTab={(target) => {
              if (target === 'price_list' || target === 'testimonial' || target === 'notifications') {
                setSubView(target);
              } else {
                setSubView('main');
                setActiveTab(target);
              }
            }}
            onOpenCreateJob={() => {
              setActiveTab('agenda');
            }}
            onOpenBookingDetail={(b) => {
              setSelectedBooking(b);
              setSubView('booking_detail');
            }}
            onOpenDayMode={(b) => {
              setSelectedBooking(b);
              setActiveTab('daymode');
            }}
          />
        );

      case 'agenda':
        return (
          <AgendaPage
            bookings={bookings}
            onSaveBooking={handleSaveBooking}
            onOpenDetail={(b) => {
              setSelectedBooking(b);
              setSubView('booking_detail');
            }}
            onOpenCreateJob={() => {
              // Open create job modal
            }}
          />
        );

      case 'clients':
        return (
          <ClientsPage
            clients={clients}
            onSaveClient={handleSaveClient}
            onDeleteClient={handleDeleteClient}
          />
        );

      case 'finance':
        return (
          <FinancePage
            bookings={bookings}
            expenses={expenses}
            onSaveExpense={handleSaveExpense}
            onDeleteExpense={handleDeleteExpense}
          />
        );

      case 'more':
        return (
          <MorePage
            onNavigateTab={(target) => {
              if (target === 'profile' || target === 'price_list' || target === 'analytics' || target === 'followup' || target === 'todo' || target === 'notifications' || target === 'quick_action_settings' || target === 'invoice') {
                if (target === 'invoice' && bookings.length > 0) {
                  setSelectedBooking(bookings[0]);
                }
                setSubView(target);
              } else {
                setSubView('main');
                setActiveTab(target);
              }
            }}
          />
        );

      case 'daymode':
        return (
          <McDayModePage
            booking={selectedBooking}
            allBookings={bookings}
            onBack={() => setActiveTab('home')}
            onSelectBooking={(b) => setSelectedBooking(b)}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] dark:bg-slate-900 flex text-[#0F172A] dark:text-slate-100 transition-colors">
      {/* Desktop Sidebar */}
      {activeTab !== 'daymode' && (
        <Sidebar
          activeTab={activeTab}
          onChangeTab={(t) => {
            setSubView('main');
            setActiveTab(t);
          }}
          onOpenCreateJob={() => {
            setSubView('main');
            setActiveTab('agenda');
          }}
          isDarkMode={isDarkMode}
          onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
        />
      )}

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {activeTab !== 'daymode' && (
          <Navbar
            isDarkMode={isDarkMode}
            onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
            title={getPageTitle()}
          />
        )}

        <main className="flex-1 p-4 md:p-8 pb-24 md:pb-8 overflow-y-auto">
          {renderContent()}
        </main>
      </div>

      {/* Mobile Floating 5-Tab Bottom Navigation */}
      {activeTab !== 'daymode' && subView === 'main' && (
        <BottomNav
          activeTab={activeTab}
          onChangeTab={(t) => {
            setSubView('main');
            setActiveTab(t);
          }}
        />
      )}
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
