import * as SecureStore from 'expo-secure-store';
import { API_BASE_URL } from '@env';

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

export const loginUser = async (
  email: string,
  password: string,
  role: Role,
): Promise<AuthResponse> => {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, role }),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Login failed.');
  }

  const data = await res.json();

  const normalizedRole: Role =
    data.role?.toLowerCase() === 'admin' ? 'admin' : 'client';

  const response: AuthResponse = {
    token: data.token,
    role: normalizedRole,
    email: data.email,
    id: data.id,
  };

  await saveSession(response);

  return response;
};

export const registerUser = async (
  name: string,
  email: string,
  password: string,
  role: Role,
): Promise<AuthResponse> => {
  const res = await fetch(`${BASE_URL}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password, role }),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Registration failed. Please try again.');
  }

  const data = await res.json();

  const normalizedRole: Role =
    data.role?.toLowerCase() === 'admin' ? 'admin' : 'client';

  const response: AuthResponse = {
    token: data.token,
    role: normalizedRole,
    email: data.email,
    id: data.id,
  };

  await saveSession(response);

  return response;
};

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

    return {
      token,
      role: role as Role,
      userId: userId ?? '',
      email: email ?? '',
    };
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