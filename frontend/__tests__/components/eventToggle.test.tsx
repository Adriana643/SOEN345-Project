import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import EventToggleBar from '../../components/eventToggle';

describe('EventToggleBar', () => {
  it('renders default labels correctly', () => {
    const { getByText } = render(
      <EventToggleBar showMyEvents={false} onToggle={jest.fn()} />
    );
    expect(getByText('All Events')).toBeTruthy();
    expect(getByText('My Events')).toBeTruthy();
  });

  it('renders custom labels when provided', () => {
    const { getByText } = render(
      <EventToggleBar
        showMyEvents={false}
        onToggle={jest.fn()}
        allLabel="Browse"
        myLabel="Saved"
      />
    );
    expect(getByText('Browse')).toBeTruthy();
    expect(getByText('Saved')).toBeTruthy();
  });

  it('calls onToggle with false when All Events is pressed', () => {
    const onToggle = jest.fn();
    const { getByText } = render(
      <EventToggleBar showMyEvents={true} onToggle={onToggle} />
    );
    fireEvent.press(getByText('All Events'));
    expect(onToggle).toHaveBeenCalledWith(false);
  });

  it('calls onToggle with true when My Events is pressed', () => {
    const onToggle = jest.fn();
    const { getByText } = render(
      <EventToggleBar showMyEvents={false} onToggle={onToggle} />
    );
    fireEvent.press(getByText('My Events'));
    expect(onToggle).toHaveBeenCalledWith(true);
  });

  it('does not call onToggle with wrong argument', () => {
    const onToggle = jest.fn();
    const { getByText } = render(
      <EventToggleBar showMyEvents={false} onToggle={onToggle} />
    );
    fireEvent.press(getByText('My Events'));
    expect(onToggle).not.toHaveBeenCalledWith(false);
  });

  it('matches snapshot when All Events is selected', () => {
    const tree = render(
      <EventToggleBar showMyEvents={false} onToggle={jest.fn()} />
    ).toJSON();
    expect(tree).toMatchSnapshot();
  });

  it('matches snapshot when My Events is selected', () => {
    const tree = render(
      <EventToggleBar showMyEvents={true} onToggle={jest.fn()} />
    ).toJSON();
    expect(tree).toMatchSnapshot();
  });
});
