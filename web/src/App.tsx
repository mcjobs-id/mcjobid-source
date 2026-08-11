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
  deleteRateCard,
  saveUserProfile
} from './services/firebaseService';

const MainApp: React.FC = () => {
  const { currentUser, userProfile, loading, updateContextProfile } = useAuth();
  
  // 5 Main Tabs matching Android app: 'home' | 'agenda' | 'clients' | 'finance' | 'more' | 'daymode'
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

  // Wizard modal state
  const [showWizardModal, setShowWizardModal] = useState(false);

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

  // Sync state with window.location.hash for browser history support & direct URLs
  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash.replace('#', '');
      if (!hash) return;
      
      const validTabs: TabType[] = ['home', 'agenda', 'clients', 'finance', 'more', 'daymode'];
      const validSubViews = ['booking_detail', 'invoice', 'price_list', 'profile', 'analytics', 'followup', 'todo', 'notifications', 'quick_action_settings'];

      if (validTabs.includes(hash as TabType)) {
        setActiveTab(hash as TabType);
        setSubView('main');
      } else if (validSubViews.includes(hash as any)) {
        setSubView(hash as any);
      }
    };

    window.addEventListener('hashchange', handleHashChange);
    handleHashChange();
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const navigateToTab = (tab: TabType) => {
    setSubView('main');
    setActiveTab(tab);
    window.location.hash = tab;
  };

  const navigateToSubView = (sv: typeof subView) => {
    setSubView(sv);
    window.location.hash = sv;
  };

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
      <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center space-y-3" style={{minHeight:'100vh', display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', background:'var(--bg-app)'}}>
        <div style={{width:'48px', height:'48px', borderRadius:'14px', background:'var(--primary)', display:'flex', alignItems:'center', justifyContent:'center', fontWeight:'800', color:'white', fontSize:'18px', boxShadow:'0 4px 14px rgba(79,70,229,0.3)'}}>
          MC
        </div>
        <p style={{fontSize:'13px', fontWeight:'600', color:'var(--text-3)'}}>Memuat mcjob.id Web App...</p>
      </div>
    );
  }

  if (!currentUser) {
    return <LoginPage />;
  }

  // Handlers for Save/Delete
  const handleSaveBooking = async (b: Booking) => {
    if (currentUser) {
      await saveBooking({ ...b, ownerId: currentUser.uid });
    }
  };

  const handleDeleteBooking = async (id: string) => {
    await deleteBooking(id);
    navigateToSubView('main');
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

  // Profile setup handler for onboarding wizard
  const handleCompleteOnboarding = async (initialBooking?: Booking) => {
    if (userProfile && currentUser) {
      const updatedProfile = { ...userProfile, profileCompleted: true };
      await saveUserProfile(updatedProfile);
      updateContextProfile(updatedProfile);
    }
    if (initialBooking && currentUser) {
      await handleSaveBooking(initialBooking);
    }
    setShowWizardModal(false);
    navigateToTab('home');
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
    if (subView === 'quick_action_settings') return 'Pengaturan Pintasan Dasbor';

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
          onBack={() => navigateToSubView('main')}
          onEdit={(b) => {
            handleSaveBooking(b);
            setSelectedBooking(b);
          }}
          onDelete={handleDeleteBooking}
          onOpenMcDayMode={(b) => {
            setSelectedBooking(b);
            navigateToTab('daymode');
          }}
          onOpenInvoice={(b) => {
            setSelectedBooking(b);
            navigateToSubView('invoice');
          }}
        />
      );
    }

    if (subView === 'invoice') {
      const activeBooking = selectedBooking || (bookings.length > 0 ? bookings[0] : null);
      return (
        <InvoicePage
          booking={activeBooking}
          onBack={() => {
            if (selectedBooking) navigateToSubView('booking_detail');
            else navigateToSubView('main');
          }}
        />
      );
    }

    if (subView === 'price_list') {
      return (
        <PriceListPage
          rateCards={rateCards}
          onBack={() => navigateToSubView('main')}
        />
      );
    }

    if (subView === 'profile') {
      return <ProfilePage onBack={() => navigateToSubView('main')} />;
    }

    if (subView === 'analytics') {
      return (
        <AnalyticsPage
          bookings={bookings}
          expenses={expenses}
          onBack={() => navigateToSubView('main')}
        />
      );
    }

    if (subView === 'followup') {
      return (
        <FollowUpPage
          bookings={bookings}
          clients={clients}
          onBack={() => navigateToSubView('main')}
        />
      );
    }

    if (subView === 'todo') {
      return <TodoPage onBack={() => navigateToSubView('main')} />;
    }

    if (subView === 'notifications') {
      return <NotificationPage bookings={bookings} onBack={() => navigateToSubView('main')} />;
    }

    if (subView === 'quick_action_settings') {
      return <QuickActionSettingsPage onBack={() => navigateToSubView('main')} />;
    }

    // 5 Main Tabs
    switch (activeTab) {
      case 'home':
        return (
          <HomePage
            bookings={bookings}
            onNavigateTab={(target) => {
              if (['price_list', 'testimonial', 'notifications', 'invoice', 'profile', 'analytics', 'followup', 'todo'].includes(target)) {
                navigateToSubView(target);
              } else {
                navigateToTab(target);
              }
            }}
            onOpenCreateJob={() => setShowWizardModal(true)}
            onOpenBookingDetail={(b) => {
              setSelectedBooking(b);
              navigateToSubView('booking_detail');
            }}
            onOpenDayMode={(b) => {
              setSelectedBooking(b);
              navigateToTab('daymode');
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
              navigateToSubView('booking_detail');
            }}
            onOpenCreateJob={() => setShowWizardModal(true)}
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
              if (['profile', 'price_list', 'analytics', 'followup', 'todo', 'notifications', 'quick_action_settings', 'invoice'].includes(target)) {
                if (target === 'invoice' && bookings.length > 0 && !selectedBooking) {
                  setSelectedBooking(bookings[0]);
                }
                navigateToSubView(target as any);
              } else {
                navigateToTab(target as any);
              }
            }}
          />
        );

      case 'daymode':
        return (
          <McDayModePage
            booking={selectedBooking}
            allBookings={bookings}
            onBack={() => navigateToTab('home')}
            onSelectBooking={(b) => setSelectedBooking(b)}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className="app-layout">
      {/* Desktop Sidebar — hidden on mobile */}
      {activeTab !== 'daymode' && (
        <Sidebar
          activeTab={activeTab}
          onChangeTab={(t) => navigateToTab(t)}
          onOpenCreateJob={() => setShowWizardModal(true)}
          isDarkMode={isDarkMode}
          onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
        />
      )}

      {/* Main Content Area */}
      <div className="main-area">
        {activeTab !== 'daymode' && (
          <Navbar
            isDarkMode={isDarkMode}
            onToggleDarkMode={() => setIsDarkMode(!isDarkMode)}
            title={getPageTitle()}
          />
        )}

        <main className="content-area">
          {renderContent()}
        </main>
      </div>

      {/* Mobile Bottom Navigation */}
      {activeTab !== 'daymode' && subView === 'main' && (
        <div className="md:hidden" style={{display:'block'}}>
          <BottomNav
            activeTab={activeTab}
            onChangeTab={(t) => navigateToTab(t)}
          />
        </div>
      )}

      {/* Wizard Modal for Creating Job */}
      {showWizardModal && (
        <WizardPage
          onClose={() => setShowWizardModal(false)}
          onSave={async (b) => {
            await handleSaveBooking(b);
            setShowWizardModal(false);
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
