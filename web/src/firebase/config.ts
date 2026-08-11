import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore, enableIndexedDbPersistence } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: "AIzaSyCaATep711foYXIgzJJCjSwGX3HHfR5yDQ",
  authDomain: "mcjobs-8ce49.firebaseapp.com",
  projectId: "mcjobs-8ce49",
  storageBucket: "mcjobs-8ce49.firebasestorage.app",
  messagingSenderId: "638482981265",
  appId: "1:638482981265:web:mcjobid"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
export const db = getFirestore(app);
export const storage = getStorage(app);

// Enable offline persistence for PWA/Mobile experience
try {
  enableIndexedDbPersistence(db).catch((err) => {
    if (err.code === 'failed-precondition') {
      console.warn('Persistence failed: Multiple tabs open');
    } else if (err.code === 'unimplemented') {
      console.warn('Persistence not supported by browser');
    }
  });
} catch (e) {
  console.log('IndexedDB persistence init note:', e);
}

export default app;
