import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import AdminHome from '../../pages/AdminHome';

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

describe('AdminHome', () => {
  let navigation: ReturnType<typeof createMockNavigation>;

  beforeEach(() => {
    navigation = createMockNavigation();
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert').mockImplementation(() => {});
  });

  it('renders Manage Events title', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    expect(getByText('Manage Events')).toBeTruthy();
  });

  it('renders sample events with delete buttons', () => {
    const { getByText, getAllByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    expect(getByText('ZonUHacks 2026')).toBeTruthy();
    expect(getByText('Michael Jackson Concert 2026')).toBeTruthy();
    expect(getAllByText('Delete').length).toBe(2);
  });

  it('renders Add button', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    expect(getByText('Add')).toBeTruthy();
  });

  it('opens the New Event modal when Add is pressed', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Add'));
    expect(getByText('New Event')).toBeTruthy();
    expect(getByText('Create Event')).toBeTruthy();
    expect(getByText('Cancel')).toBeTruthy();
  });

  it('shows form error when creating event with empty fields', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Add'));
    fireEvent.press(getByText('Create Event'));
    expect(getByText('All fields are required.')).toBeTruthy();
  });

  it('creates a new event when all fields are filled', () => {
    const { getByText, getAllByDisplayValue, queryByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Add'));
    // After opening modal: inputs[0]=search, inputs[1]=title, inputs[2]=desc, inputs[3]=date, inputs[4]=location
    const inputs = getAllByDisplayValue('');
    fireEvent.changeText(inputs[1], 'New Test Event');
    fireEvent.changeText(inputs[2], 'A new event description');
    fireEvent.changeText(inputs[3], 'May 01, 2026');
    fireEvent.changeText(inputs[4], 'Montreal, QC');
    fireEvent.press(getByText('Create Event'));
    expect(queryByText('New Test Event')).toBeTruthy();
  });

  it('triggers delete confirmation alert when Delete is pressed', () => {
    const { getAllByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getAllByText('Delete')[0]);
    expect(Alert.alert).toHaveBeenCalledWith(
      'Delete Event',
      'Are you sure?',
      expect.any(Array)
    );
  });

  it('deletes event when alert is confirmed', () => {
    (Alert.alert as jest.Mock).mockImplementation(
      (_title: string, _msg: string, buttons: any[]) => {
        const deleteBtn = buttons.find((b: any) => b.text === 'Delete');
        deleteBtn?.onPress?.();
      }
    );
    const { getAllByText, queryByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getAllByText('Delete')[0]);
    expect(queryByText('ZonUHacks 2026')).toBeNull();
  });

  it('shows empty message when My Events is selected with no admin-created events', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('My Events'));
    expect(getByText("You haven't created any events yet.")).toBeTruthy();
  });

  it('renders sort dropdown with default option', () => {
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    expect(getByText('Sort by:')).toBeTruthy();
    expect(getByText('Recent Date')).toBeTruthy();
  });

  it('calls logoutUser and navigates to Login on logout', async () => {
    mockLogoutUser.mockResolvedValue(undefined);
    const { getByText } = render(
      <AdminHome navigation={navigation as any} />
    );
    fireEvent.press(getByText('Log out'));
    await waitFor(() => {
      expect(mockLogoutUser).toHaveBeenCalled();
      expect(navigation.replace).toHaveBeenCalledWith('Login');
    });
  });
});
