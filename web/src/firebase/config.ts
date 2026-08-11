import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore, initializeFirestore, persistentLocalCache, persistentMultipleTabManager } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: "AIzaSyCaATep711foYXIgzJJCjSwGX3HHfR5yDQ",
  authDomain: "mcjobs-8ce49.firebaseapp.com",
  projectId: "mcjobs-8ce49",
  storageBucket: "mcjobs-8ce49.firebasestorage.app",
  messagingSenderId: "638482981265",
  appId: "1:638482981265:web:5f3a8b2c1d4e9f7a"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

// Use new persistent cache API (replaces deprecated enableIndexedDbPersistence)
export const db = (() => {
  try {
    return initializeFirestore(app, {
      localCache: persistentLocalCache({
        tabManager: persistentMultipleTabManager()
      })
    });
  } catch (e) {
    // Already initialized or not supported — fall back to default Firestore
    return getFirestore(app);
  }
})();

export const storage = getStorage(app);

export default app;
