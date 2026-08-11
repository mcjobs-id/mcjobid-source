import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { BottomNav, type TabType } from './components/BottomNav';

import { LoginPage } from './pages/LoginPage';
import { WizardPage } from './pages/WizardPage';
import { HomePage } from './pages/HomePage';
import { BookingsPage } from './pages/BookingsPage';
import { BookingDetailPage } from './pages/BookingDetailPage';
import { McDayModePage } from './pages/McDayModePage';
import { InvoicePage } from './pages/InvoicePage';
import { FinancePage } from './pages/FinancePage';
import { ClientsPage } from './pages/ClientsPage';
import { PriceListPage } from './pages/PriceListPage';
import { ProfilePage } from './pages/ProfilePage';

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
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const [subView, setSubView] = useState<'main' | 'booking_detail' | 'invoice' | 'clients' | 'price_list'>('main');

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

  // Render Page Content based on tab & subView
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

    if (subView === 'clients') {
      return (
        <ClientsPage
          clients={clients}
          onSaveClient={handleSaveClient}
          onDeleteClient={handleDeleteClient}
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

    // Main Tabs
    switch (activeTab) {
      case 'home':
        return (
          <HomePage
            bookings={bookings}
            onNavigateTab={(tab) => {
              if (tab === 'clients') setSubView('clients');
              else if (tab === 'price_list') setSubView('price_list');
              else setActiveTab(tab);
            }}
            onOpenCreateJob={() => {
              setActiveTab('bookings');
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

      case 'bookings':
        return (
          <BookingsPage
            bookings={bookings}
            onSaveBooking={handleSaveBooking}
            onOpenDetail={(b) => {
              setSelectedBooking(b);
              setSubView('booking_detail');
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

      case 'finance':
        return (
          <FinancePage
            bookings={bookings}
            expenses={expenses}
            onSaveExpense={handleSaveExpense}
            onDeleteExpense={handleDeleteExpense}
          />
        );

      case 'profile':
        return <ProfilePage />;

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between">
      {activeTab !== 'daymode' && (
        <Navbar
          isDarkMode={isDarkMode}
          onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
        />
      )}

      <main className="flex-1 px-4 pt-4 pb-20">{renderContent()}</main>

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
