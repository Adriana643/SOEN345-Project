jest.mock('firebase/app', () => {
  const mockApp = { name: 'mock-app', options: {} };
  return {
    initializeApp: jest.fn(() => mockApp),
    getApps: jest.fn(() => []),
  };
});

jest.mock('firebase/auth', () => ({
  getAuth: jest.fn(() => ({ currentUser: null })),
}));

jest.mock('firebase/firestore', () => ({
  getFirestore: jest.fn(() => ({})),
}));

describe('firebaseConfig', () => {
  it('exports a default Firebase app instance', () => {
    const config = require('../../config/firebaseConfig');
    expect(config.default).toBeDefined();
  });

  it('exports an auth instance', () => {
    const config = require('../../config/firebaseConfig');
    expect(config.auth).toBeDefined();
  });

  it('calls initializeApp when no apps exist', () => {
    const { initializeApp } = require('firebase/app');
    expect(initializeApp).toHaveBeenCalled();
  });

  it('calls getApps to check for existing apps', () => {
    const { getApps } = require('firebase/app');
    expect(getApps).toHaveBeenCalled();
  });

  it('calls getAuth to obtain the auth object', () => {
    const { getAuth } = require('firebase/auth');
    expect(getAuth).toHaveBeenCalled();
  });

  it('reuses existing app when one is already initialized', () => {
    jest.resetModules();

    const existingApp = { name: 'existing-app', options: {} };

    jest.doMock('firebase/app', () => ({
      initializeApp: jest.fn(),
      getApps: jest.fn(() => [existingApp]),
    }));
    jest.doMock('firebase/auth', () => ({
      getAuth: jest.fn(() => ({ currentUser: null })),
    }));
    jest.doMock('firebase/firestore', () => ({
      getFirestore: jest.fn(() => ({})),
    }));

    const config = require('../../config/firebaseConfig');
    const { initializeApp } = require('firebase/app');

    expect(config.default).toBe(existingApp);
    expect(initializeApp).not.toHaveBeenCalled();
  });
});