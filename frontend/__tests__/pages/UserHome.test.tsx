import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import UserHome from '../../pages/UserHome';

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

const mockLogoutUser = jest.fn();
jest.mock('../../services/authService', () => ({
  logoutUser: () => mockLogoutUser(),
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

describe('UserHome', () => {
  let navigation: ReturnType<typeof createMockNavigation>;

  beforeEach(() => {
    navigation = createMockNavigation();
    jest.clearAllMocks();
  });

  it('renders Events title', () => {
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    expect(getByText('Events')).toBeTruthy();
  });

  it('renders Log out button', () => {
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    expect(getByText('Log out')).toBeTruthy();
  });

  it('renders sample events', () => {
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    expect(getByText('ZonUHacks 2026')).toBeTruthy();
    expect(getByText('Michael Jackson Concert 2026')).toBeTruthy();
  });

  it('filters events by search text', () => {
    const { getByPlaceholderText, queryByText } = render(
      <UserHome navigation={navigation as any} />
    );
    fireEvent.changeText(
      getByPlaceholderText('Search events...'),
      'ZonU'
    );
    expect(queryByText('ZonUHacks 2026')).toBeTruthy();
    expect(queryByText('Michael Jackson Concert 2026')).toBeNull();
  });

  it('shows no events found when search has no match', () => {
    const { getByPlaceholderText, getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    fireEvent.changeText(
      getByPlaceholderText('Search events...'),
      'xyznonexistent'
    );
    expect(getByText('No events found.')).toBeTruthy();
  });

  it('toggles join status on an event', () => {
    const { getAllByText, getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    const joinButtons = getAllByText('Join Event');
    expect(joinButtons.length).toBe(2);
    fireEvent.press(joinButtons[0]);
    expect(getByText('Joined ✓')).toBeTruthy();
  });

  it('unjoins an event when pressed again', () => {
    const { getAllByText } = render(
      <UserHome navigation={navigation as any} />
    );
    // Join the first event
    fireEvent.press(getAllByText('Join Event')[0]);
    // Now unjoin it
    fireEvent.press(getAllByText('Joined ✓')[0]);
    // Should be back to two "Join Event" buttons
    expect(getAllByText('Join Event').length).toBe(2);
  });

  it('shows empty message when My Events is selected with no joined events', () => {
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('My Events'));
    expect(getByText("You haven't joined any events yet.")).toBeTruthy();
  });

  it('shows only joined events in My Events tab', () => {
    const { getByText, getAllByText, queryByText } = render(
      <UserHome navigation={navigation as any} />
    );
    // Join first event
    fireEvent.press(getAllByText('Join Event')[0]);
    // Switch to My Events
    fireEvent.press(getByText('My Events'));
    expect(queryByText('ZonUHacks 2026')).toBeTruthy();
    expect(queryByText('Michael Jackson Concert 2026')).toBeNull();
  });

  it('renders sort dropdown with default Recent Date option', () => {
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    expect(getByText('Sort by:')).toBeTruthy();
    expect(getByText('Recent Date')).toBeTruthy();
  });

  it('calls logoutUser and navigates to Login on logout', async () => {
    mockLogoutUser.mockResolvedValue(undefined);
    const { getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Log out'));
    await waitFor(() => {
      expect(mockLogoutUser).toHaveBeenCalled();
      expect(navigation.replace).toHaveBeenCalledWith('Login');
    });
  });
});
