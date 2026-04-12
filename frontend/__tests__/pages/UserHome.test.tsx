import React from 'react';
import { render, fireEvent, waitFor, act } from '@testing-library/react-native';

jest.mock('../../config/firebaseConfig', () => ({
  __esModule: true,
  default: {},
  auth: { currentUser: { uid: 'test-uid', email: 'user@test.com' } },
  db: {},
}));

jest.mock('firebase/firestore', () => ({
  collection: jest.fn(() => 'events-ref'),
  doc: jest.fn((_db: any, _col: string, id: string) => ({ _id: id })),
  getDocs: jest.fn(() =>
    Promise.resolve({
      docs: [
        {
          id: '1',
          data: () => ({
            title: 'ZonUHacks 2026',
            description: 'A hackathon',
            date: 'Jan 01, 2026',
            location: 'Montreal',
          }),
        },
        {
          id: '2',
          data: () => ({
            title: 'Michael Jackson Concert 2026',
            description: 'A concert',
            date: 'Feb 01, 2026',
            location: 'Toronto',
          }),
        },
      ],
    })
  ),
  getDoc: jest.fn(() =>
    Promise.resolve({
      exists: () => true,
      data: () => ({ joinedEventIds: [] }),
    })
  ),
  setDoc: jest.fn(() => Promise.resolve()),
}));

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

import UserHome from '../../pages/UserHome';

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
    mockLogoutUser.mockReset();
  });

  //ui tests
  it('renders Events title', () => {
    const { getByText } = render(<UserHome navigation={navigation as any} />);
    expect(getByText('Events')).toBeTruthy();
  });

  it('renders Log out button', () => {
    const { getByText } = render(<UserHome navigation={navigation as any} />);
    expect(getByText('Log out')).toBeTruthy();
  });

  it('renders sort dropdown with default Recent Date option', () => {
    const { getByText } = render(<UserHome navigation={navigation as any} />);
    expect(getByText('Sort by:')).toBeTruthy();
    expect(getByText('Recent Date')).toBeTruthy();
  });

  // everything async storage related
  it('renders sample events', async () => {
    const { findByText } = render(<UserHome navigation={navigation as any} />);
    expect(await findByText('ZonUHacks 2026')).toBeTruthy();
    expect(await findByText('Michael Jackson Concert 2026')).toBeTruthy();
  });

  it('filters events by search text', async () => {
    const { findByPlaceholderText, findByText, queryByText } = render(
      <UserHome navigation={navigation as any} />
    );
    await findByText('ZonUHacks 2026'); // wait for initial load
    fireEvent.changeText(
      await findByPlaceholderText('Search events...'),
      'ZonU'
    );
    expect(await findByText('ZonUHacks 2026')).toBeTruthy();
    expect(queryByText('Michael Jackson Concert 2026')).toBeNull();
  });

  it('shows no events found when search has no match', async () => {
    const { findByPlaceholderText, findByText } = render(
      <UserHome navigation={navigation as any} />
    );
    await findByText('ZonUHacks 2026');
    fireEvent.changeText(
      await findByPlaceholderText('Search events...'),
      'xyznonexistent'
    );
    expect(await findByText('No events found.')).toBeTruthy();
  });

  it('toggles join status on an event', async () => {
    const { findAllByText, getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    const joinButtons = await findAllByText('Join Event');
    expect(joinButtons.length).toBe(2);
    await act(async () => {
      fireEvent.press(joinButtons[0]);
    });
    expect(getByText('Joined ✓')).toBeTruthy();
  });

  it('unjoins an event when pressed again', async () => {
    const { findAllByText, getAllByText } = render(
      <UserHome navigation={navigation as any} />
    );
    await findAllByText('Join Event'); // wait for load
    await act(async () => {
      fireEvent.press(getAllByText('Join Event')[0]);
    });
    await act(async () => {
      fireEvent.press(getAllByText('Joined ✓')[0]);
    });
    expect(getAllByText('Join Event').length).toBe(2);
  });

  it('shows empty message when My Events is selected with no joined events', async () => {
    const { findByText, getByText } = render(
      <UserHome navigation={navigation as any} />
    );
    await findByText('ZonUHacks 2026'); // wait for both loads to complete
    fireEvent.press(getByText('My Events'));
    expect(getByText("You haven't joined any events yet.")).toBeTruthy();
  });

  it('shows only joined events in My Events tab', async () => {
    const { findAllByText, getAllByText, getByText, queryByText } = render(
      <UserHome navigation={navigation as any} />
    );
    await findAllByText('Join Event'); // wait for load
    await act(async () => {
      fireEvent.press(getAllByText('Join Event')[0]);
    });
    expect(getByText('Joined ✓')).toBeTruthy();
    fireEvent.press(getByText('My Events'));
    expect(queryByText("You haven't joined any events yet.")).toBeNull();
  });


  it('calls logoutUser and navigates to Login on logout', async () => {
    mockLogoutUser.mockResolvedValue(undefined);
    const { getByText } = render(<UserHome navigation={navigation as any} />);
    fireEvent.press(getByText('Log out'));
    await waitFor(() => {
      expect(mockLogoutUser).toHaveBeenCalled();
      expect(navigation.replace).toHaveBeenCalledWith('Login');
    });
  });
});