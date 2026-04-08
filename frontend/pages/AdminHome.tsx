import React, { useState, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, StatusBar, FlatList, Modal, Alert, Animated } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import EventToggleBar from '../components/eventToggle';
import s from './styles/AdminHomeStyles';
import { Ionicons } from '@expo/vector-icons';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'AdminHome'>;
};

type Event = {
  id: string;
  title: string;
  description: string;
  date: string;
  location: string;
  createdByAdmin?: boolean;
};

const SAMPLE_EVENTS: Event[] = [
  { id: '1', title: 'ZonUHacks 2026', description: 'Come code for 24h only to not submit anything.', date: 'Jan 02, 2026', location: 'Concordia University, SGW', createdByAdmin: false },
  { id: '2', title: 'Michael Jackson Concert 2026', description: "He's alive guys", date: 'Apr 18, 2026', location: 'Bell Centre, Montreal', createdByAdmin: false },
];

const SHEET_HEIGHT = 580;

const AdminHome = ({ navigation }: Props) => {
  const [editingEventId, setEditingEventId] = useState<string | null>(null);
  const [events, setEvents] = useState<Event[]>(SAMPLE_EVENTS);
  const [search, setSearch] = useState('');
  const [showMyEvents, setShowMyEvents] = useState(false);
  const [sortBy, setSortBy] = useState<'date' | 'alphabetical'>('date');
  const [showSortDropdown, setShowSortDropdown] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newDate, setNewDate] = useState('');
  const [newLocation, setNewLocation] = useState('');
  const [formErr, setFormErr] = useState('');

  const slideAnim = useRef(new Animated.Value(SHEET_HEIGHT)).current;
  const fadeAnim = useRef(new Animated.Value(0)).current;

  const openModal = (event?: Event) => {
    if (event) {
      setEditingEventId(event.id);
      setNewTitle(event.title);
      setNewDesc(event.description);
      setNewDate(event.date);
      setNewLocation(event.location);
    } else {
      setEditingEventId(null);
      setNewTitle('');
      setNewDesc('');
      setNewDate('');
      setNewLocation('');
    }

    setModalVisible(true);
    Animated.parallel([
      Animated.timing(fadeAnim, { toValue: 1, duration: 250, useNativeDriver: true }),
      Animated.timing(slideAnim, { toValue: 0, duration: 300, useNativeDriver: true }),
    ]).start();
  };

  const closeModal = () => {
    Animated.parallel([
      Animated.timing(fadeAnim, { toValue: 0, duration: 200, useNativeDriver: true }),
      Animated.timing(slideAnim, { toValue: SHEET_HEIGHT, duration: 250, useNativeDriver: true }),
    ]).start(() => {
      setModalVisible(false);
      setFormErr('');
      setEditingEventId(null);
      setNewTitle('');
      setNewDesc('');
      setNewDate('');
      setNewLocation('');
    });
  };

  const baseList = showMyEvents ? events.filter(e => e.createdByAdmin) : events;
  const filtered = baseList.filter(e =>
    e.title.toLowerCase().includes(search.toLowerCase())
  );

  const parseDate = (dateStr: string): Date => {
    // Assuming format like 'Jan 02, 2026'
    return new Date(dateStr);
  };

  const sorted = [...filtered].sort((a, b) => {
    if (sortBy === 'date') {
      return parseDate(b.date).getTime() - parseDate(a.date).getTime(); // recent first
    } else {
      return a.title.localeCompare(b.title); // alphabetical
    }
  });

  const handleSortSelect = (value: 'date' | 'alphabetical') => {
    setSortBy(value);
    setShowSortDropdown(false);
  };

  const sortOptions = [
    { label: 'Recent Date', value: 'date' as const },
    { label: 'Alphabetical', value: 'alphabetical' as const },
  ];

  const handleSaveEvent = () => {
    if (!newTitle.trim() || !newDesc.trim() || !newDate.trim() || !newLocation.trim()) {
      setFormErr('All fields are required.');
      return;
    }

    if (editingEventId) {
      setEvents(prev =>
        prev.map(event =>
          event.id === editingEventId
            ? {
                ...event,
                title: newTitle.trim(),
                description: newDesc.trim(),
                date: newDate.trim(),
                location: newLocation.trim(),
              }
            : event
        )
      );
    } else {
      setEvents(prev => [
        {
          id: Date.now().toString(),
          title: newTitle.trim(),
          description: newDesc.trim(),
          date: newDate.trim(),
          location: newLocation.trim(),
          createdByAdmin: true,
        },
        ...prev,
      ]);
    }

    closeModal();
  };

  const handleDelete = (id: string) => {
    Alert.alert('Delete Event', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => setEvents(prev => prev.filter(e => e.id !== id)) },
    ]);
  };

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
      <View style={s.container}>

        <View style={s.header}>
          <Text style={s.title}>Manage Events</Text>
          <TouchableOpacity onPress={() => navigation.replace('Login')}>
            <Text style={s.logoutText}>Log out</Text>
          </TouchableOpacity>
        </View>

        <EventToggleBar showMyEvents={showMyEvents} onToggle={setShowMyEvents} />

        <View style={s.toolbar}>
          <View style={s.searchWrapper}>
            <TextInput
              style={s.searchInput}
              value={search}
              onChangeText={setSearch}
              autoCorrect={false}
            />
          </View>
          <TouchableOpacity style={s.addBtn} onPress={openModal} activeOpacity={0.8}>
            <Text style={s.addBtnText}>Add</Text>
          </TouchableOpacity>
        </View>

        <View style={s.sortWrapper}>
          <Text style={s.sortLabel}>Sort by:</Text>
          <View style={s.dropdownContainer}>
            <TouchableOpacity
              style={s.dropdownTrigger}
              onPress={() => setShowSortDropdown(!showSortDropdown)}
              activeOpacity={0.8}
            >
              <Text style={s.dropdownTriggerText}>
                {sortBy === 'date' ? 'Recent Date' : 'Alphabetical'}
              </Text>
              <Ionicons
                name={showSortDropdown ? 'chevron-up' : 'chevron-down'}
                size={16}
                color="#2b7cd3"
              />
            </TouchableOpacity>
            {showSortDropdown && (
              <>
                <TouchableOpacity
                  style={s.dropdownOverlay}
                  onPress={() => setShowSortDropdown(false)}
                  activeOpacity={1}
                />
                <View style={s.dropdownList}>
                  {sortOptions.map((option) => (
                    <TouchableOpacity
                      key={option.value}
                      style={[
                        s.dropdownItem,
                        sortBy === option.value && s.dropdownItemSelected,
                      ]}
                      onPress={() => handleSortSelect(option.value)}
                      activeOpacity={0.8}
                    >
                      <Text
                        style={[
                          s.dropdownItemText,
                          sortBy === option.value && s.dropdownItemTextSelected,
                        ]}
                      >
                        {option.label}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>
              </>
            )}
          </View>
        </View>

        <FlatList
          data={sorted}
          keyExtractor={item => item.id}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={s.list}
          ListEmptyComponent={
            <Text style={s.emptyText}>
              {showMyEvents ? "You haven't created any events yet." : 'No events found.'}
            </Text>
          }
          renderItem={({ item }) => (
            <View style={s.card}>
              <View style={s.cardTop}>
                <Text style={s.cardTitle}>{item.title}</Text>
                <Text style={s.cardDate}>{item.date}</Text>
              </View>
              <Text style={s.cardDesc}>{item.description}</Text>
              <View style={s.locationRow}>
                <Ionicons name="location-outline" size={12} color="#90b8d8" />
                <Text style={s.cardLocation}>{item.location}</Text>
              </View>
              <View style={{ flexDirection: 'row', marginTop: 10 }}>
                <TouchableOpacity
                  style={[s.deleteBtn, { flex: 1, marginRight: 5 }]}
                  onPress={() => openModal(item)}
                  activeOpacity={0.8}
                >
                  <Text style={s.deleteText}>Edit</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={[s.deleteBtn, { flex: 1, marginLeft: 5 }]}
                  onPress={() => handleDelete(item.id)}
                  activeOpacity={0.8}
                >
                  <Text style={s.deleteText}>Delete</Text>
                </TouchableOpacity>
              </View>
            </View>
          )}
        />

      </View>

      <Modal visible={modalVisible} animationType="none" transparent>
        <Animated.View style={[s.modalOverlay, { opacity: fadeAnim }]}>
          <TouchableOpacity style={{ flex: 1 }} activeOpacity={1} onPress={closeModal} />
        </Animated.View>

        <Animated.View style={[s.modalCard, { transform: [{ translateY: slideAnim }] }]}>
          <Text style={s.modalTitle}>
            {editingEventId ? 'Edit Event' : 'New Event'}
          </Text>

          {formErr ? (
            <View style={s.errorBox}>
              <Text style={s.errorBoxText}>{formErr}</Text>
            </View>
          ) : null}

          <Text style={s.label}>Title</Text>
          <View style={s.inputWrapper}>
            <TextInput
              testID="admin-title-input"
              style={s.input}
              value={newTitle}
              onChangeText={t => { setNewTitle(t); setFormErr(''); }}
            />
          </View>

          <Text style={[s.label, s.labelSpacing]}>Description</Text>
          <View style={[s.inputWrapper, s.inputMultiline]}>
            <TextInput
              testID="admin-description-input"
              style={[s.input, { flex: 1 }]}
              value={newDesc}
              onChangeText={t => { setNewDesc(t); setFormErr(''); }}
              multiline
            />
          </View>

          <Text style={[s.label, s.labelSpacing]}>Date</Text>
          <View style={s.inputWrapper}>
            <TextInput
              testID="admin-date-input"
              style={s.input}
              value={newDate}
              onChangeText={t => { setNewDate(t); setFormErr(''); }}
            />
          </View>

          <Text style={[s.label, s.labelSpacing]}>Location</Text>
          <View style={s.inputWrapper}>
            <TextInput
              testID="admin-location-input"
              style={s.input}
              value={newLocation}
              onChangeText={t => { setNewLocation(t); setFormErr(''); }}
            />
          </View>

          <TouchableOpacity style={s.submitBtn} onPress={handleSaveEvent}>
            <Text style={s.submitText}>
              {editingEventId ? 'Save Changes' : 'Create Event'}
            </Text>
          </TouchableOpacity>
          <TouchableOpacity style={s.cancelBtn} onPress={closeModal}>
            <Text style={s.cancelText}>Cancel</Text>
          </TouchableOpacity>
        </Animated.View>
      </Modal>

    </SafeAreaView>
  );
};

export default AdminHome;