import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StatusBar, FlatList } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import EventToggleBar from '../components/eventToggle';
import s from './styles/UserHomeStyles';
import { Ionicons } from '@expo/vector-icons';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'UserHome'>;
};

type Event = {
  id: string;
  title: string;
  description: string;
  location: string;
  date: string;
};

const SAMPLE_EVENTS: Event[] = [
  { id: '1', title: 'ConUHacks 2026', description: 'Come code for 24h only to not submit anything.', date: 'Jan 02, 2026', location: 'Concordia University, SGW' },
  { id: '2', title: 'Michael Jackson Concert 2026', description: "He's alive guys", date: 'Apr 18, 2026', location: 'Bell Centre, Montreal' },
];

const UserHome = ({ navigation }: Props) => {
  const [search, setSearch] = useState('');
  const [joined, setJoined] = useState<string[]>([]);
  const [showMyEvents, setShowMyEvents] = useState(false);

  const toggleJoin = (id: string) => {
    setJoined(prev =>
      prev.includes(id) ? prev.filter(j => j !== id) : [...prev, id]
    );
  };

  const baseList = showMyEvents
    ? SAMPLE_EVENTS.filter(e => joined.includes(e.id))
    : SAMPLE_EVENTS;

  const filtered = baseList.filter(e =>
    e.title.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
      <View style={s.container}>

        <View style={s.header}>
          <Text style={s.title}>Events</Text>
          <TouchableOpacity onPress={() => navigation.replace('Login')}>
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

        <FlatList
          data={filtered}
          keyExtractor={item => item.id}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={s.list}
          ListEmptyComponent={
            <Text style={s.emptyText}>
              {showMyEvents ? "You haven't joined any events yet." : 'No events found.'}
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
                activeOpacity={0.8}>
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