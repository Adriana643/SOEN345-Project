import AsyncStorage from '@react-native-async-storage/async-storage';

const BASE_URL = '';

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
  await AsyncStorage.removeItem('authToken');
  await AsyncStorage.removeItem('userRole');
  await AsyncStorage.removeItem('userId');
  await AsyncStorage.removeItem('userEmail');
};

export const getSession = async (): Promise<StoredSession | null> => {
  try {
    const token = await AsyncStorage.getItem('authToken');
    const role = await AsyncStorage.getItem('userRole');
    const userId = await AsyncStorage.getItem('userId');
    const email = await AsyncStorage.getItem('userEmail');

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
  await AsyncStorage.setItem('authToken', data.token);
  await AsyncStorage.setItem('userRole', data.role);
  await AsyncStorage.setItem('userId', String(data.id));
  await AsyncStorage.setItem('userEmail', data.email);
};