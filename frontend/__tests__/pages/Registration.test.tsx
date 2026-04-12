import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import RegisterScreen from '../../pages/Registration';

jest.mock('react-native-safe-area-context', () => {
  const { View } = require('react-native');
  return {
    SafeAreaView: View,
    SafeAreaProvider: View,
    useSafeAreaInsets: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
  };
});

jest.mock('@expo/vector-icons', () => {
  const React = require('react');
  const { Text } = require('react-native');
  return {
    Ionicons: ({ name }: any) => React.createElement(Text, null, name),
  };
});

jest.mock('expo-firebase-recaptcha', () => {
  const React = require('react');
  const { View } = require('react-native');
  return {
    FirebaseRecaptchaVerifierModal: React.forwardRef((_props: any, ref: any) =>
      React.createElement(View, { ref })
    ),
  };
});

jest.mock('../../config/firebaseConfig', () => ({
  __esModule: true,
  default: { options: {} },
  auth: {},
}));

const mockRegisterWithEmail = jest.fn();
jest.mock('../../services/authService', () => ({
  registerWithEmail: (...args: any[]) => mockRegisterWithEmail(...args),
}));

const createMockNavigation = () => ({
  navigate: jest.fn(),
  replace: jest.fn(),
  goBack: jest.fn(),
  dispatch: jest.fn(),
  reset: jest.fn(),
  isFocused: jest.fn(),
  canGoBack: jest.fn(),
  getParent: jest.fn(),
  getState: jest.fn(),
  setParams: jest.fn(),
  setOptions: jest.fn(),
  addListener: jest.fn(),
  removeListener: jest.fn(),
  getId: jest.fn(),
});

describe('RegisterScreen', () => {
  let navigation: ReturnType<typeof createMockNavigation>;

  beforeEach(() => {
    navigation = createMockNavigation();
    jest.clearAllMocks();
  });

  it('renders the Register title', () => {
    const { getByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    expect(getByText('Register')).toBeTruthy();
  });

  it('renders Client and Admin role toggle options', () => {
    const { getByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    expect(getByText('Client')).toBeTruthy();
    expect(getByText('Admin')).toBeTruthy();
  });

  it('renders form fields with correct placeholders', () => {
    const { getByPlaceholderText, getAllByPlaceholderText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    expect(getByPlaceholderText('Full Name')).toBeTruthy();
    expect(getByPlaceholderText('you@example.com')).toBeTruthy();
    expect(getAllByPlaceholderText('******').length).toBe(2);
  });

  it('shows all validation errors on empty submission', async () => {
    const { getByText, findByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.press(getByText('Create Account'));
    expect(await findByText('Full name is required.')).toBeTruthy();
    expect(await findByText('Email is required.')).toBeTruthy();
    expect(await findByText('Password is required.')).toBeTruthy();
    expect(await findByText('Please confirm your password.')).toBeTruthy();
  });

  it('shows error for invalid email format', async () => {
    const { getByText, getByPlaceholderText, getAllByPlaceholderText, findByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getByPlaceholderText('Full Name'), 'John Doe');
    fireEvent.changeText(getByPlaceholderText('you@example.com'), 'invalid-email');
    fireEvent.changeText(getAllByPlaceholderText('******')[0], 'password123');
    fireEvent.changeText(getAllByPlaceholderText('******')[1], 'password123');
    fireEvent.press(getByText('Create Account'));
    expect(await findByText('Enter a valid email address.')).toBeTruthy();
  });

  it('shows error when passwords do not match', async () => {
    const { getByText, getByPlaceholderText, getAllByPlaceholderText, findByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getByPlaceholderText('Full Name'), 'John Doe');
    fireEvent.changeText(getByPlaceholderText('you@example.com'), 'john@example.com');
    fireEvent.changeText(getAllByPlaceholderText('******')[0], 'password123');
    fireEvent.changeText(getAllByPlaceholderText('******')[1], 'differentpassword');
    fireEvent.press(getByText('Create Account'));
    expect(await findByText('Passwords do not match.')).toBeTruthy();
  });

  it('navigates to Login when Go Back is pressed', () => {
    const { getByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.press(getByText('Go Back'));
    expect(navigation.navigate).toHaveBeenCalledWith('Login');
  });

  it('shows success screen after successful email registration', async () => {
    mockRegisterWithEmail.mockResolvedValue(undefined);
    const { getByText, getByPlaceholderText, getAllByPlaceholderText, findByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getByPlaceholderText('Full Name'), 'John Doe');
    fireEvent.changeText(getByPlaceholderText('you@example.com'), 'john@example.com');
    fireEvent.changeText(getAllByPlaceholderText('******')[0], 'password123');
    fireEvent.changeText(getAllByPlaceholderText('******')[1], 'password123');
    fireEvent.press(getByText('Create Account'));
    expect(await findByText('Check your email!')).toBeTruthy();
  });

  it('switches to phone mode and shows unavailable notice', () => {
    const { getByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    // The mocked Ionicons renders icon name as text; "call" is the phone icon
    fireEvent.press(getByText('call'));
    expect(
      getByText(
        'SMS verification is not available at this time. Please register using your email address.'
      )
    ).toBeTruthy();
  });

  it('disables submit button in phone mode and shows Unavailable', () => {
    const { getByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.press(getByText('call'));
    expect(getByText('Unavailable')).toBeTruthy();
  });

  it('toggles password visibility for password field', () => {
    const { getAllByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    const showButtons = getAllByText('Show');
    expect(showButtons.length).toBe(2);
    fireEvent.press(showButtons[0]);
    expect(getAllByText('Hide').length).toBeGreaterThanOrEqual(1);
  });

  it('displays API error when registration fails', async () => {
    mockRegisterWithEmail.mockRejectedValue(new Error('Email already in use'));
    const { getByText, getByPlaceholderText, getAllByPlaceholderText, findByText } = render(
      <RegisterScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getByPlaceholderText('Full Name'), 'John Doe');
    fireEvent.changeText(getByPlaceholderText('you@example.com'), 'john@example.com');
    fireEvent.changeText(getAllByPlaceholderText('******')[0], 'password123');
    fireEvent.changeText(getAllByPlaceholderText('******')[1], 'password123');
    fireEvent.press(getByText('Create Account'));
    expect(await findByText('Email already in use')).toBeTruthy();
  });
});
