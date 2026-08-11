import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import type { User } from 'firebase/auth';
import { onAuthStateChanged, signOut } from 'firebase/auth';
import { auth } from '../firebase/config';
import { getUserProfile, saveUserProfile } from '../services/firebaseService';
import type { UserProfile } from '../types';

// ── Auth States (explicit, never ambiguous) ──────────────────
export type AuthState =
  | 'LOADING'          // Firebase SDK initializing, don't know yet
  | 'UNAUTHENTICATED'  // Confirmed: no user
  | 'PROFILE_LOADING'  // User authenticated, fetching profile from Firestore
  | 'PROFILE_READY'    // User + profile fully loaded — APP IS USABLE
  | 'PROFILE_ERROR';   // Profile fetch failed, user is authenticated but profile unavailable

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  authState: AuthState;
  loading: boolean;          // true when LOADING or PROFILE_LOADING
  logout: () => Promise<void>;
  refreshProfile: () => Promise<void>;
  updateContextProfile: (updated: Partial<UserProfile>) => void;
  loginError: string | null;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [authState, setAuthState] = useState<AuthState>('LOADING');
  const [loginError, setLoginError] = useState<string | null>(null);

  const loading = authState === 'LOADING' || authState === 'PROFILE_LOADING';

  const fetchAndInitProfile = useCallback(async (user: User, expectedSession: number): Promise<void> => {
    setAuthState('PROFILE_LOADING');
    try {
      let profile = await getUserProfile(user.uid);
      if (!profile) {
        // First-time user without profile: create a default one
        profile = {
          uid: user.uid,
          displayName: user.displayName || 'MC Professional',
          name: user.displayName || 'MC Professional',
          email: user.email || '',
          photoUrl: user.photoURL || undefined,
          profileCompleted: false,
          defaultDpPercentage: 30,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        await saveUserProfile(profile);
      } else {
        // Ensure uid is always present in profile
        profile = { ...profile, uid: user.uid };
      }
      
      // Generation guard: abort if the session has changed while we were fetching
      if (sessionVersionRef.current !== expectedSession) return;
      
      setUserProfile(profile);
      setAuthState('PROFILE_READY');
    } catch (err) {
      if (sessionVersionRef.current !== expectedSession) return;
      console.error('AuthContext: Failed to fetch/init profile:', err);
      // DO NOT set a fake usable profile on error. Let the app handle the PROFILE_ERROR state.
      setUserProfile(null);
      setAuthState('PROFILE_ERROR');
    }
  }, []);

  // Add a session version ref to guard against race conditions
  const sessionVersionRef = React.useRef(0);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      // Increment session version whenever auth changes to invalidate pending work
      sessionVersionRef.current += 1;
      const currentSession = sessionVersionRef.current;
      
      setCurrentUser(user);
      if (user) {
        await fetchAndInitProfile(user, currentSession);
      } else {
        setUserProfile(null);
        setAuthState('UNAUTHENTICATED');
      }
    });

    return () => unsubscribe();
  }, [fetchAndInitProfile]);

  const logout = async () => {
    sessionVersionRef.current += 1; // Invalidate any pending profile tasks
    await signOut(auth);
    setCurrentUser(null);
    setUserProfile(null);
    setAuthState('UNAUTHENTICATED');
  };

  const refreshProfile = async () => {
    if (currentUser) {
      const currentSession = sessionVersionRef.current;
      await fetchAndInitProfile(currentUser, currentSession);
    }
  };

  const updateContextProfile = useCallback((updated: Partial<UserProfile>) => {
    setUserProfile(prev => prev ? { ...prev, ...updated } : null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        userProfile,
        authState,
        loading,
        logout,
        refreshProfile,
        updateContextProfile,
        loginError
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
