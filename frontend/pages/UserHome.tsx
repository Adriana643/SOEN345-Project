import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StatusBar, FlatList, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import EventToggleBar from '../components/eventToggle';
import s from './styles/UserHomeStyles';
import { Ionicons } from '@expo/vector-icons';
import { logoutUser } from '../services/authService';
import { auth, db } from '../config/firebaseConfig';
import {
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
} from 'firebase/firestore';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'UserHome'>;
};

type Event = {
  id: string;
  title: string;
  description: string;
  location: string;
  date: string;
  createdByAdmin?: boolean;
  createdBy?: string;
};

const UserHome = ({ navigation }: Props) => {
  const [search, setSearch] = useState('');
  const [joined, setJoined] = useState<string[]>([]);
  const [events, setEvents] = useState<Event[]>([]);
  const [showMyEvents, setShowMyEvents] = useState(false);
  const [sortBy, setSortBy] = useState<'date' | 'alphabetical'>('date');
  const [showSortDropdown, setShowSortDropdown] = useState(false);
  const [loadingJoined, setLoadingJoined] = useState(true);
  const [loadingEvents, setLoadingEvents] = useState(true);

  useEffect(() => {
    loadEvents();
    loadJoinedEvents();
  }, []);

  const loadEvents = async () => {
    try {
      setLoadingEvents(true);

      const snapshot = await getDocs(collection(db, 'events'));
      const loadedEvents: Event[] = snapshot.docs.map((docSnap) => ({
        id: docSnap.id,
        ...(docSnap.data() as Omit<Event, 'id'>),
      }));

      setEvents(loadedEvents);
    } catch (error) {
      console.error('Error loading events:', error);
      Alert.alert('Error', 'Could not load events.');
    } finally {
      setLoadingEvents(false);
    }
  };

  const loadJoinedEvents = async () => {
    try {
      setLoadingJoined(true);

      const currentUser = auth.currentUser;

      if (!currentUser) {
        setJoined([]);
        return;
      }

      const userRef = doc(db, 'users', currentUser.uid);
      const userSnap = await getDoc(userRef);

      if (userSnap.exists()) {
        const data = userSnap.data();
        setJoined(Array.isArray(data.joinedEventIds) ? data.joinedEventIds : []);
      } else {
        await setDoc(
          userRef,
          {
            email: currentUser.email ?? '',
            joinedEventIds: [],
          },
          { merge: true }
        );
        setJoined([]);
      }
    } catch (error) {
      console.error('Error loading joined events:', error);
      Alert.alert('Error', 'Could not load your joined events.');
    } finally {
      setLoadingJoined(false);
    }
  };

  const saveJoinedEvents = async (updatedJoined: string[]) => {
    const currentUser = auth.currentUser;

    if (!currentUser) {
      throw new Error('No authenticated user found.');
    }

    const userRef = doc(db, 'users', currentUser.uid);

    await setDoc(
      userRef,
      {
        email: currentUser.email ?? '',
        joinedEventIds: updatedJoined,
      },
      { merge: true }
    );
  };

  const toggleJoin = async (id: string) => {
    try {
      const updatedJoined = joined.includes(id)
        ? joined.filter(j => j !== id)
        : [...joined, id];

      setJoined(updatedJoined);
      await saveJoinedEvents(updatedJoined);
    } catch (error) {
      console.error('Error updating joined events:', error);
      Alert.alert('Error', 'Could not update your reservation.');
    }
  };

  const parseDate = (dateStr: string): Date => {
    // Assuming format like 'Jan 02, 2026'
    return new Date(dateStr);
  };

  const baseList = showMyEvents
    ? events.filter(e => joined.includes(e.id))
    : events;

  const filtered = baseList.filter(e =>
    e.title.toLowerCase().includes(search.toLowerCase())
  );

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
  
  const isLoading = loadingEvents || loadingJoined;

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
      <View style={s.container}>

        <View style={s.header}>
          <Text style={s.title}>Events</Text>
          <TouchableOpacity
            onPress={async () => {
              await logoutUser();
              navigation.replace('Login');
            }}
          >
            <Text style={s.logoutText}>Log out</Text>
          </TouchableOpacity>
        </View>

        <EventToggleBar showMyEvents={showMyEvents} onToggle={setShowMyEvents} />

        <View style={s.searchWrapper}>
          <TextInput
            style={s.searchInput}
            placeholder="Search events..."
            placeholderTextColor="#90b8d8"
            value={search}
            onChangeText={setSearch}
            autoCorrect={false}
          />
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
              {isLoading
                ? 'Loading...'
                : showMyEvents
                  ? "You haven't joined any events yet."
                  : 'No events found.'}
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
              <TouchableOpacity
                style={[s.joinBtn, joined.includes(item.id) && s.joinBtnActive]}
                onPress={() => toggleJoin(item.id)}
                activeOpacity={0.8}
              >
                <Text style={[s.joinText, joined.includes(item.id) && s.joinTextActive]}>
                  {joined.includes(item.id) ? 'Joined ✓' : 'Join Event'}
                </Text>
              </TouchableOpacity>
            </View>
          )}
        />

      </View>
    </SafeAreaView>
  );
};

export default UserHome;