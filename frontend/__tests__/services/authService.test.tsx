import * as SecureStore from 'expo-secure-store';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  sendEmailVerification,
  signOut,
} from 'firebase/auth';

jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn(),
  setItemAsync: jest.fn(),
  deleteItemAsync: jest.fn(),
}));

jest.mock('firebase/auth', () => ({
  createUserWithEmailAndPassword: jest.fn(),
  signInWithEmailAndPassword: jest.fn(),
  sendEmailVerification: jest.fn(),
  signOut: jest.fn(),
  getAuth: jest.fn(() => ({})),
}));

jest.mock('../../config/firebaseConfig', () => ({
  __esModule: true,
  default: { options: {} },
  auth: {},
}));

jest.mock('@env', () => ({
  API_BASE_URL: 'http://localhost:8080',
}), { virtual: true });

const mockFetch = jest.fn();
(global as any).fetch = mockFetch;

import {
  registerWithEmail,
  loginUser,
  logoutUser,
  getSession,
} from '../../services/authService';

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('registerWithEmail', () => {
    it('creates user with Firebase and sends verification email', async () => {
      const mockUser = {
        getIdToken: jest.fn().mockResolvedValue('firebase-token'),
        email: 'test@test.com',
      };
      (createUserWithEmailAndPassword as jest.Mock).mockResolvedValue({
        user: mockUser,
      });
      (sendEmailVerification as jest.Mock).mockResolvedValue(undefined);
      mockFetch.mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve({
            token: 'jwt',
            role: 'client',
            email: 'test@test.com',
            id: 1,
          }),
      });
      (SecureStore.setItemAsync as jest.Mock).mockResolvedValue(undefined);

      await registerWithEmail('Test User', 'test@test.com', 'password123', 'client');

      expect(createUserWithEmailAndPassword).toHaveBeenCalledWith(
        expect.anything(),
        'test@test.com',
        'password123'
      );
      expect(sendEmailVerification).toHaveBeenCalledWith(mockUser);
    });

    it('throws when Firebase createUser fails', async () => {
      (createUserWithEmailAndPassword as jest.Mock).mockRejectedValue(
        new Error('Email already in use')
      );

      await expect(
        registerWithEmail('Test', 'test@test.com', 'pass123', 'client')
      ).rejects.toThrow('Email already in use');
    });
  });

  describe('loginUser', () => {
    it('authenticates with Firebase and returns auth response', async () => {
      const mockUser = {
        getIdToken: jest.fn().mockResolvedValue('firebase-token'),
        email: 'test@test.com',
      };
      (signInWithEmailAndPassword as jest.Mock).mockResolvedValue({
        user: mockUser,
      });
      mockFetch.mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve({
            token: 'jwt',
            role: 'client',
            email: 'test@test.com',
            id: 1,
          }),
      });
      (SecureStore.setItemAsync as jest.Mock).mockResolvedValue(undefined);

      const result = await loginUser('test@test.com', 'password123');

      expect(signInWithEmailAndPassword).toHaveBeenCalledWith(
        expect.anything(),
        'test@test.com',
        'password123'
      );
      expect(result.email).toBe('test@test.com');
      expect(result.role).toBe('client');
    });

    it('returns admin role when backend returns admin', async () => {
      const mockUser = {
        getIdToken: jest.fn().mockResolvedValue('firebase-token'),
        email: 'admin@test.com',
      };
      (signInWithEmailAndPassword as jest.Mock).mockResolvedValue({
        user: mockUser,
      });
      mockFetch.mockResolvedValue({
        ok: true,
        json: () =>
          Promise.resolve({
            token: 'jwt',
            role: 'ADMIN',
            email: 'admin@test.com',
            id: 2,
          }),
      });
      (SecureStore.setItemAsync as jest.Mock).mockResolvedValue(undefined);

      const result = await loginUser('admin@test.com', 'password123');
      expect(result.role).toBe('admin');
    });

    it('throws when Firebase signIn fails', async () => {
      (signInWithEmailAndPassword as jest.Mock).mockRejectedValue(
        new Error('Wrong password')
      );

      await expect(
        loginUser('test@test.com', 'wrong')
      ).rejects.toThrow('Wrong password');
    });

    it('falls back to Firebase session when backend is unreachable', async () => {
      const mockUser = {
        getIdToken: jest.fn().mockResolvedValue('firebase-token'),
        email: 'test@test.com',
      };
      (signInWithEmailAndPassword as jest.Mock).mockResolvedValue({
        user: mockUser,
      });
      mockFetch.mockRejectedValue(new Error('Network error'));
      (SecureStore.setItemAsync as jest.Mock).mockResolvedValue(undefined);

      const result = await loginUser('test@test.com', 'password123');

      // Should fall back to Firebase token
      expect(result.token).toBe('firebase-token');
      expect(result.email).toBe('test@test.com');
    });
  });

  describe('logoutUser', () => {
    it('signs out from Firebase and clears all stored session keys', async () => {
      (signOut as jest.Mock).mockResolvedValue(undefined);
      (SecureStore.deleteItemAsync as jest.Mock).mockResolvedValue(undefined);

      await logoutUser();

      expect(signOut).toHaveBeenCalled();
      expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('authToken');
      expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('userRole');
      expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('userId');
      expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('userEmail');
    });

    it('does not throw when signOut fails', async () => {
      (signOut as jest.Mock).mockRejectedValue(new Error('Sign out error'));

      await expect(logoutUser()).resolves.toBeUndefined();
    });
  });

  describe('getSession', () => {
    it('returns stored session when all values exist', async () => {
      (SecureStore.getItemAsync as jest.Mock).mockImplementation(
        (key: string) => {
          const store: Record<string, string> = {
            authToken: 'token123',
            userRole: 'admin',
            userId: '42',
            userEmail: 'admin@test.com',
          };
          return Promise.resolve(store[key] || null);
        }
      );

      const session = await getSession();
      expect(session).toEqual({
        token: 'token123',
        role: 'admin',
        userId: '42',
        email: 'admin@test.com',
      });
    });

    it('returns null when token is missing', async () => {
      (SecureStore.getItemAsync as jest.Mock).mockResolvedValue(null);

      const session = await getSession();
      expect(session).toBeNull();
    });

    it('returns null when role is missing', async () => {
      (SecureStore.getItemAsync as jest.Mock).mockImplementation(
        (key: string) => {
          if (key === 'authToken') return Promise.resolve('token');
          return Promise.resolve(null);
        }
      );

      const session = await getSession();
      expect(session).toBeNull();
    });
  });
});
