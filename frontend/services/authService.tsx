import * as SecureStore from 'expo-secure-store';
import { API_BASE_URL } from '@env';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  sendEmailVerification,
  User,
  signOut
} from 'firebase/auth';
import { auth } from '../config/firebaseConfig';

const BASE_URL = API_BASE_URL;

export type Role = 'client' | 'admin';

export type AuthResponse = {
  token: string;
  role: Role;
  email: string;
  id: number;
};

export type StoredSession = {
  token: string;
  role: Role;
  email: string;
  userId: string;
};

// Rejects after ms
const withTimeout = (promise: Promise<any>, ms: number) =>
  Promise.race([
    promise,
    new Promise((_, reject) =>
      setTimeout(() => reject(new Error('Request timed out.')), ms),
    ),
  ]);

const syncWithBackend = async (
  firebaseUser: User,
  role: Role,
  name: string,
): Promise<AuthResponse> => {
  const idToken = await firebaseUser.getIdToken();

  try {
    const res = await withTimeout(
      fetch(`${BASE_URL}/api/auth/firebase`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idToken, role, name }),
      }),
      10000,
    ) as Response;

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Backend sync failed.');
    }

    const data = await res.json();
    const normalizedRole: Role = data.role?.toLowerCase() === 'admin' ? 'admin' : 'client';
    const response: AuthResponse = {
      token: data.token,
      role: normalizedRole,
      email: data.email ?? '',
      id: data.id,
    };
    await saveSession(response);
    return response;

  } catch (err: any) {
    // if backend times out
    console.warn('Backend sync failed, using Firebase session as fallback:', err.message);
    const fallback: AuthResponse = {
      token: idToken,
      role,
      email: firebaseUser.email ?? '',
      id: 0,
    };
    await saveSession(fallback);
    return fallback;
  }
};

export const registerWithEmail = async (
  name: string,
  email: string,
  password: string,
  role: Role,
): Promise<void> => {
  const credential = await createUserWithEmailAndPassword(auth, email, password);
  await sendEmailVerification(credential.user);
  // Sync with backend in background
  syncWithBackend(credential.user, role, name).catch(err =>
    console.warn('Backend sync failed (will retry on login):', err.message),
  );
};

export const loginWithEmail = async (
  email: string,
  password: string,
): Promise<AuthResponse> => {
  const credential = await signInWithEmailAndPassword(auth, email, password);
  return syncWithBackend(credential.user, 'client', '');
};

export const loginUser = async (
  email: string,
  password: string,
): Promise<AuthResponse> => loginWithEmail(email, password);

export const logoutUser = async (): Promise<void> => {
  try {
    await signOut(auth);
    console.log('firebase signed out');
    await SecureStore.deleteItemAsync('authToken');
    await SecureStore.deleteItemAsync('userRole');
    await SecureStore.deleteItemAsync('userId');
    await SecureStore.deleteItemAsync('userEmail');
    console.log('tokens deleted and logout complete');
  } catch (e) {
    console.error('Logout error:', e);
  }
};

export const getSession = async (): Promise<StoredSession | null> => {
  try {
    const token = await SecureStore.getItemAsync('authToken');
    const role = await SecureStore.getItemAsync('userRole');
    const userId = await SecureStore.getItemAsync('userId');
    const email = await SecureStore.getItemAsync('userEmail');
    if (!token || !role) return null;
    return { token, role: role as Role, userId: userId ?? '', email: email ?? '' };
  } catch {
    return null;
  }
};

const saveSession = async (data: AuthResponse): Promise<void> => {
  await SecureStore.setItemAsync('authToken', data.token);
  await SecureStore.setItemAsync('userRole', data.role);
  await SecureStore.setItemAsync('userId', String(data.id));
  await SecureStore.setItemAsync('userEmail', data.email);
};