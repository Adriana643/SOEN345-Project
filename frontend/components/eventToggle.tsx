import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';

type Props = {
  showMyEvents: boolean;
  onToggle: (value: boolean) => void;
  allLabel?: string;
  myLabel?: string;
};

const EventToggleBar = ({
  showMyEvents,
  onToggle,
  allLabel = 'All Events',
  myLabel = 'My Events',
}: Props) => {
  return (
    <View style={s.toggleRow}>
      <TouchableOpacity
        style={[s.toggleBtn, !showMyEvents && s.toggleBtnActive]}
        onPress={() => onToggle(false)}
        activeOpacity={0.8}>
        <Text style={[s.toggleText, !showMyEvents && s.toggleTextActive]}>
          {allLabel}
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={[s.toggleBtn, showMyEvents && s.toggleBtnActive]}
        onPress={() => onToggle(true)}
        activeOpacity={0.8}>
        <Text style={[s.toggleText, showMyEvents && s.toggleTextActive]}>
          {myLabel}
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const s = StyleSheet.create({
  toggleRow: {
    flexDirection: 'row',
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
    marginBottom: 16,
    overflow: 'hidden',
  },
  toggleBtn: {
    flex: 1,
    paddingVertical: 11,
    alignItems: 'center',
  },
  toggleBtnActive: {
    backgroundColor: '#2b7cd3',
  },
  toggleText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#90b8d8',
  },
  toggleTextActive: {
    color: '#ffffff',
  },
});

export default EventToggleBar;