import * as SecureStore from 'expo-secure-store';
import { API_BASE_URL } from '@env';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signInWithPhoneNumber,
  sendEmailVerification,
  User,
  ApplicationVerifier,
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

let phoneConfirmationResult: any = null;

// Rejects after ms milliseconds
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
  const res = await withTimeout(
    fetch(`${BASE_URL}/api/auth/firebase`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ idToken, role, name }),
    }),
    10000, // 10 second timeout
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
};

export const registerWithEmail = async (
  name: string,
  email: string,
  password: string,
  role: Role,
): Promise<void> => {
  const credential = await createUserWithEmailAndPassword(auth, email, password);
  await sendEmailVerification(credential.user);
  // Sync with backend in background — don't block the user
  syncWithBackend(credential.user, role, name).catch(err =>
    console.warn('Backend sync failed (will retry on login):', err.message),
  );
};

export const loginWithEmail = async (
  email: string,
  password: string,
  role: Role,
): Promise<AuthResponse> => {
  const credential = await signInWithEmailAndPassword(auth, email, password);
  return syncWithBackend(credential.user, role, '');
};

export const sendPhoneOTP = async (
  phoneNumber: string,
  recaptchaVerifier: ApplicationVerifier,
): Promise<void> => {
  phoneConfirmationResult = await signInWithPhoneNumber(auth, phoneNumber, recaptchaVerifier);
};

export const verifyPhoneOTP = async (
  code: string,
  name: string,
  role: Role,
): Promise<AuthResponse> => {
  if (!phoneConfirmationResult) throw new Error('No OTP pending. Please request a new code.');
  const credential = await phoneConfirmationResult.confirm(code);
  phoneConfirmationResult = null;
  return syncWithBackend(credential.user, role, name);
};

export const loginUser = async (
  email: string,
  password: string,
  role: Role,
): Promise<AuthResponse> => loginWithEmail(email, password, role);

export const logoutUser = async (): Promise<void> => {
  await SecureStore.deleteItemAsync('authToken');
  await SecureStore.deleteItemAsync('userRole');
  await SecureStore.deleteItemAsync('userId');
  await SecureStore.deleteItemAsync('userEmail');
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