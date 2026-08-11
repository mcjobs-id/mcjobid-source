import React, { createContext, useContext, useEffect, useState, useCallback, useRef } from 'react';
import type { User } from 'firebase/auth';
import { onAuthStateChanged, signOut } from 'firebase/auth';
import { auth } from '../firebase/config';
import { getUserProfile, saveUserProfile, subscribeUserProfile } from '../services/firebaseService';
import type { UserProfile } from '../types';

// ── Auth States (explicit, never ambiguous) ──────────────────
export type AuthState =
  | 'LOADING'          // Firebase SDK initializing, don't know yet
  | 'UNAUTHENTICATED'  // Confirmed: no user
  | 'PROFILE_LOADING'  // User authenticated, fetching profile from Firestore
  | 'PROFILE_READY'    // User + profile fully loaded — APP IS USABLE
  | 'PROFILE_ERROR';   // Profile fetch failed

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  authState: AuthState;
  loading: boolean;
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
  const [loginError] = useState<string | null>(null);
  const sessionVersionRef = useRef(0);

  const loading = authState === 'LOADING' || authState === 'PROFILE_LOADING';

  const fetchAndInitProfile = useCallback(async (user: User, expectedSession: number): Promise<void> => {
    setAuthState('PROFILE_LOADING');
    try {
      let profile = await getUserProfile(user.uid);
      if (!profile) {
        profile = {
          uid: user.uid,
          displayName: user.displayName || 'MC Professional',
          name: user.displayName || 'MC Professional',
          email: user.email || '',
          photoUrl: user.photoURL || undefined,
          photoUri: user.photoURL || undefined,
          profileCompleted: false,
          defaultDpPercentage: 30,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        await saveUserProfile(profile);
      } else {
        profile = { ...profile, uid: user.uid };
      }

      if (sessionVersionRef.current !== expectedSession) return;

      setUserProfile(profile);
      try {
        localStorage.setItem('mcjobid_user_profile', JSON.stringify(profile));
      } catch (_) { /* ignore */ }
      setAuthState('PROFILE_READY');
    } catch (err) {
      if (sessionVersionRef.current !== expectedSession) return;
      console.error('AuthContext: Failed to fetch/init profile:', err);
      setUserProfile(null);
      setAuthState('PROFILE_ERROR');
    }
  }, []);

  useEffect(() => {
    // Safety timeout: if Firebase auth doesn't respond in 8s, treat as unauthenticated
    const timeout = setTimeout(() => {
      setAuthState(prev => prev === 'LOADING' ? 'UNAUTHENTICATED' : prev);
    }, 8000);

    let profileUnsub: (() => void) | null = null;

    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      clearTimeout(timeout);
      sessionVersionRef.current += 1;
      const currentSession = sessionVersionRef.current;
      setCurrentUser(user);

      if (profileUnsub) {
        profileUnsub();
        profileUnsub = null;
      }

      if (user) {
        setAuthState('PROFILE_LOADING');
        profileUnsub = subscribeUserProfile(user.uid, (profile) => {
          if (sessionVersionRef.current !== currentSession) return;
          if (profile) {
            setUserProfile(profile);
            try {
              localStorage.setItem('mcjobid_user_profile', JSON.stringify(profile));
            } catch (_) {}
            setAuthState('PROFILE_READY');
          } else {
            // Profile document doesn't exist yet, initialize it
            fetchAndInitProfile(user, currentSession);
          }
        });
      } else {
        setUserProfile(null);
        setAuthState('UNAUTHENTICATED');
      }
    });

    return () => {
      clearTimeout(timeout);
      if (profileUnsub) profileUnsub();
      unsubscribe();
    };
  }, [fetchAndInitProfile]);

  const logout = async () => {
    sessionVersionRef.current += 1;
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
    setUserProfile(prev => {
      if (!prev) return null;
      const next = { ...prev, ...updated };
      try {
        localStorage.setItem('mcjobid_user_profile', JSON.stringify(next));
      } catch (_) { /* ignore */ }
      return next;
    });
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
