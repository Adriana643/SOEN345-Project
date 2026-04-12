import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';

let mockStore: Array<{
  id: string;
  title: string;
  description: string;
  date: string;
  location: string;
  createdByAdmin?: boolean;
  createdBy?: string;
}> = [];

jest.mock('../../config/firebaseConfig', () => ({
  __esModule: true,
  default: {},
  auth: { currentUser: { uid: 'test-uid', email: 'admin@test.com' } },
  db: {},
}));

jest.mock('firebase/firestore', () => ({
  collection: jest.fn(() => 'events-ref'),
  // Stores the id so deleteDoc can filter by it
  doc: jest.fn((_db: any, _col: string, id: string) => ({ _id: id })),
  getDocs: jest.fn(() =>
    Promise.resolve({
      docs: mockStore.map(e => ({ id: e.id, data: () => ({ ...e }) })),
    })
  ),
  addDoc: jest.fn((_ref: any, data: any) => {
    const newId = `new-${mockStore.length}`;
    mockStore.push({ id: newId, ...data });
    return Promise.resolve({ id: newId });
  }),
  deleteDoc: jest.fn((docRef: any) => {
    mockStore = mockStore.filter(e => e.id !== docRef._id);
    return Promise.resolve();
  }),
  updateDoc: jest.fn(() => Promise.resolve()),
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

import AdminHome from '../../pages/AdminHome';

const seedEvents = () => [
  {
    id: '1',
    title: 'ZonUHacks 2026',
    description: 'A hackathon',
    date: 'Jan 01, 2026',
    location: 'Montreal',
    createdByAdmin: true,
    createdBy: 'test-uid',
  },
  {
    id: '2',
    title: 'Michael Jackson Concert 2026',
    description: 'A concert',
    date: 'Feb 01, 2026',
    location: 'Toronto',
    createdByAdmin: true,
    createdBy: 'test-uid',
  },
];

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

describe('AdminHome', () => {
  let navigation: ReturnType<typeof createMockNavigation>;

  beforeEach(() => {
    mockStore = seedEvents();
    navigation = createMockNavigation();
    mockLogoutUser.mockReset();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
  });

  it('renders Manage Events title', () => {
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    expect(getByText('Manage Events')).toBeTruthy();
  });

  it('renders Add button', () => {
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    expect(getByText('Add')).toBeTruthy();
  });

  it('renders sort dropdown with default option', () => {
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    expect(getByText('Sort by:')).toBeTruthy();
    expect(getByText('Recent Date')).toBeTruthy();
  });

  it('opens the New Event modal when Add is pressed', () => {
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    fireEvent.press(getByText('Add'));
    expect(getByText('New Event')).toBeTruthy();
    expect(getByText('Create Event')).toBeTruthy();
    expect(getByText('Cancel')).toBeTruthy();
  });

  it('shows form error when creating event with empty fields', () => {
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    fireEvent.press(getByText('Add'));
    fireEvent.press(getByText('Create Event'));
    expect(getByText('All fields are required.')).toBeTruthy();
  });

  it('renders sample events with delete buttons', async () => {
    const { findByText, getAllByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    expect(await findByText('ZonUHacks 2026')).toBeTruthy();
    expect(await findByText('Michael Jackson Concert 2026')).toBeTruthy();
    expect(getAllByText('Delete').length).toBe(2);
  });

  it('creates a new event when all fields are filled', async () => {
    const { getByText, getByTestId, findByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Add'));
    fireEvent.changeText(getByTestId('admin-title-input'), 'New Test Event');
    fireEvent.changeText(getByTestId('admin-description-input'), 'A new event description');
    fireEvent.changeText(getByTestId('admin-date-input'), 'May 01, 2026');
    fireEvent.changeText(getByTestId('admin-location-input'), 'Montreal, QC');
    fireEvent.press(getByText('Create Event'));
    expect(await findByText('New Test Event')).toBeTruthy();
  });

  it('triggers delete confirmation alert when Delete is pressed', async () => {
    const { findAllByText } = render(<AdminHome navigation={navigation as any} />);
    const deleteButtons = await findAllByText('Delete');
    fireEvent.press(deleteButtons[0]);
    expect(Alert.alert).toHaveBeenCalledWith(
      'Delete Event',
      'Are you sure?',
      expect.any(Array)
    );
  });

  it('deletes event when alert is confirmed', async () => {
    jest.mocked(Alert.alert).mockImplementation(
      (_title: string, _msg: string, buttons: any[]) => {
        const deleteBtn = buttons.find((b: any) => b.text === 'Delete');
        deleteBtn?.onPress?.();
      }
    );
    const { findAllByText, getAllByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    const deleteButtons = await findAllByText('Delete');
    expect(deleteButtons.length).toBe(2);
    fireEvent.press(deleteButtons[0]);
    await waitFor(() => {
      expect(getAllByText('Delete').length).toBe(1);
    });
  });

  it('shows empty message when My Events is selected with no admin-created events', async () => {
    mockStore = [
      {
        id: '1',
        title: 'ZonUHacks 2026',
        description: 'A hackathon',
        date: 'Jan 01, 2026',
        location: 'Montreal',
        createdByAdmin: true,
        createdBy: 'other-uid',
      },
    ];
    const { getByText, findByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    await findByText('ZonUHacks 2026'); // wait for initial load
    fireEvent.press(getByText('My Events'));
    expect(getByText("You haven't created any events yet.")).toBeTruthy();
  });

  it('calls logoutUser and navigates to Login on logout', async () => {
    mockLogoutUser.mockResolvedValue(undefined);
    const { getByText } = render(<AdminHome navigation={navigation as any} />);
    fireEvent.press(getByText('Log out'));
    await waitFor(() => {
      expect(mockLogoutUser).toHaveBeenCalled();
      expect(navigation.replace).toHaveBeenCalledWith('Login');
    });
  });
});