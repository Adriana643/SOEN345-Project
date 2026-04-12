import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import * as SecureStore from 'expo-secure-store';

jest.mock('expo', () => ({
  registerRootComponent: jest.fn(),
}));

jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn(),
  setItemAsync: jest.fn(),
  deleteItemAsync: jest.fn(),
}));

jest.mock('firebase/auth', () => ({
  onAuthStateChanged: jest.fn(),
  getAuth: jest.fn(() => ({})),
}));

jest.mock('../config/firebaseConfig', () => ({
  __esModule: true,
  default: { options: {} },
  auth: {},
}));

jest.mock('@react-navigation/native', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    NavigationContainer: ({ children }: any) =>
      React.createElement(View, null, children),
  };
});

jest.mock('@react-navigation/native-stack', () => {
  const React = require('react');
  const { View, Text } = require('react-native');
  return {
    createNativeStackNavigator: () => ({
      Navigator: ({ children, initialRouteName }: any) =>
        React.createElement(
          View,
          null,
          React.createElement(Text, null, `Route:${initialRouteName}`),
          children
        ),
      Screen: ({ name }: any) =>
        React.createElement(Text, null, `Screen:${name}`),
    }),
  };
});

jest.mock('react-native-safe-area-context', () => {
  const { View } = require('react-native');
  return {
    SafeAreaView: View,
    SafeAreaProvider: View,
    useSafeAreaInsets: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
  };
});

jest.mock('../pages/Login', () => {
  const React = require('react');
  const { Text } = require('react-native');
  return () => React.createElement(Text, null, 'LoginMock');
});

jest.mock('../pages/Registration', () => {
  const React = require('react');
  const { Text } = require('react-native');
  return () => React.createElement(Text, null, 'RegisterMock');
});

jest.mock('../pages/AdminHome', () => {
  const React = require('react');
  const { Text } = require('react-native');
  return () => React.createElement(Text, null, 'AdminHomeMock');
});

jest.mock('../pages/UserHome', () => {
  const React = require('react');
  const { Text } = require('react-native');
  return () => React.createElement(Text, null, 'UserHomeMock');
});

import App from '../App';

describe('App', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders without crashing', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockResolvedValue(null);
    const { findByText } = render(<App />);
    await waitFor(() => {
      expect(findByText('Route:Login')).toBeTruthy();
    });
  });

  it('navigates to Login when no stored session exists', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockResolvedValue(null);
    const { findByText } = render(<App />);
    expect(await findByText('Route:Login')).toBeTruthy();
  });

  it('navigates to AdminHome when admin session is stored', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockImplementation(
      (key: string) => {
        if (key === 'authToken') return Promise.resolve('token123');
        if (key === 'userRole') return Promise.resolve('admin');
        return Promise.resolve(null);
      }
    );
    const { findByText } = render(<App />);
    expect(await findByText('Route:AdminHome')).toBeTruthy();
  });

  it('navigates to UserHome when client session is stored', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockImplementation(
      (key: string) => {
        if (key === 'authToken') return Promise.resolve('token123');
        if (key === 'userRole') return Promise.resolve('client');
        return Promise.resolve(null);
      }
    );
    const { findByText } = render(<App />);
    expect(await findByText('Route:UserHome')).toBeTruthy();
  });

  it('defaults to Login when session check throws an error', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockRejectedValue(
      new Error('SecureStore error')
    );
    const { findByText } = render(<App />);
    expect(await findByText('Route:Login')).toBeTruthy();
  });

  it('renders all four screen definitions', async () => {
    (SecureStore.getItemAsync as jest.Mock).mockResolvedValue(null);
    const { findByText } = render(<App />);
    await waitFor(async () => {
      expect(await findByText('Screen:Login')).toBeTruthy();
      expect(await findByText('Screen:Register')).toBeTruthy();
      expect(await findByText('Screen:AdminHome')).toBeTruthy();
      expect(await findByText('Screen:UserHome')).toBeTruthy();
    });
  });
});
