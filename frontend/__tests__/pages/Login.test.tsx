import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import LoginScreen from '../../pages/Login';

jest.mock('react-native-safe-area-context', () => {
  const { View } = require('react-native');
  return {
    SafeAreaView: View,
    SafeAreaProvider: View,
    useSafeAreaInsets: () => ({ top: 0, bottom: 0, left: 0, right: 0 }),
  };
});

const mockLoginUser = jest.fn();
jest.mock('../../services/authService', () => ({
  loginUser: (...args: any[]) => mockLoginUser(...args),
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

describe('LoginScreen', () => {
  let navigation: ReturnType<typeof createMockNavigation>;

  beforeEach(() => {
    navigation = createMockNavigation();
    jest.clearAllMocks();
  });

  it('renders the Sign In title', () => {
    const { getAllByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    expect(getAllByText('Sign In').length).toBeGreaterThanOrEqual(1);
  });

  it('renders email and password labels', () => {
    const { getByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    expect(getByText('Email')).toBeTruthy();
    expect(getByText('Password')).toBeTruthy();
  });

  it('renders Register Account link', () => {
    const { getByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    expect(getByText('Register Account')).toBeTruthy();
  });

  it('navigates to Register screen when link is pressed', () => {
    const { getByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.press(getByText('Register Account'));
    expect(navigation.navigate).toHaveBeenCalledWith('Register');
  });

  it('toggles password visibility when Show/Hide is pressed', () => {
    const { getByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    expect(getByText('Show')).toBeTruthy();
    fireEvent.press(getByText('Show'));
    expect(getByText('Hide')).toBeTruthy();
    fireEvent.press(getByText('Hide'));
    expect(getByText('Show')).toBeTruthy();
  });

  it('shows email required error on empty submission', async () => {
    const { getAllByText, findByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    const signInTexts = getAllByText('Sign In');
    fireEvent.press(signInTexts[1]);
    expect(await findByText('Email is required.')).toBeTruthy();
  });

  it('shows password required error on empty submission', async () => {
    const { getAllByText, findByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.press(getAllByText('Sign In')[1]);
    expect(await findByText('Password is required.')).toBeTruthy();
  });

  it('shows invalid email error for malformed email', async () => {
    const { getAllByText, getAllByDisplayValue, findByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getAllByDisplayValue('')[0], 'not-an-email');
    fireEvent.press(getAllByText('Sign In')[1]);
    expect(await findByText('Enter a valid email.')).toBeTruthy();
  });

  it('calls loginUser and navigates to UserHome on client login', async () => {
    mockLoginUser.mockResolvedValue({ role: 'client' });
    const { getAllByText, getAllByDisplayValue } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getAllByDisplayValue('')[0], 'test@example.com');
    fireEvent.changeText(getAllByDisplayValue('')[0], 'password123');
    fireEvent.press(getAllByText('Sign In')[1]);

    await waitFor(() => {
      expect(mockLoginUser).toHaveBeenCalledWith('test@example.com', 'password123');
    });
    await waitFor(() => {
      expect(navigation.replace).toHaveBeenCalledWith('UserHome');
    });
  });

  it('navigates to AdminHome when login returns admin role', async () => {
    mockLoginUser.mockResolvedValue({ role: 'admin' });
    const { getAllByText, getAllByDisplayValue } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getAllByDisplayValue('')[0], 'admin@example.com');
    fireEvent.changeText(getAllByDisplayValue('')[0], 'password123');
    fireEvent.press(getAllByText('Sign In')[1]);

    await waitFor(() => {
      expect(navigation.replace).toHaveBeenCalledWith('AdminHome');
    });
  });

  it('displays API error message on login failure', async () => {
    mockLoginUser.mockRejectedValue(new Error('Invalid credentials'));
    const { getAllByText, getAllByDisplayValue, findByText } = render(
      <LoginScreen navigation={navigation as any} />
    );
    fireEvent.changeText(getAllByDisplayValue('')[0], 'test@example.com');
    fireEvent.changeText(getAllByDisplayValue('')[0], 'wrongpassword');
    fireEvent.press(getAllByText('Sign In')[1]);

    expect(await findByText('Invalid credentials')).toBeTruthy();
  });
});
