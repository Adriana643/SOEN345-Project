import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity,
  StyleSheet, StatusBar,FlatList,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'UserHome'>;
};

type Event = {
  id: string;
  title: string;
  description: string;
  date: string;
};

const SAMPLE_EVENTS: Event[] = [
  { id: '1', title: 'ConUHacks 2026', description: 'Come code for 24h only to not submit anything.', date: 'Jan02, 2026' },
  { id: '2', title: 'Michael Jackson Concert 2026', description: "He's alive guys", date: 'Apr 18, 2026' }
];

const UserHome = ({ navigation }: Props) => {
  const [search, setSearch] = useState('');
  const [joined, setJoined] = useState<string[]>([]);

  const filtered = SAMPLE_EVENTS.filter(e =>
    e.title.toLowerCase().includes(search.toLowerCase())
  );

  const toggleJoin = (id: string) => {
    setJoined(prev =>
      prev.includes(id) ? prev.filter(j => j !== id) : [...prev, id]
    );
  };

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor={'#ffffff'} />
      <View style={s.container}>

        <View style={s.header}>
          <Text style={s.title}>Events</Text>
          <TouchableOpacity onPress={() => navigation.replace('Login')}>
            <Text style={s.logoutText}>Log out</Text>
          </TouchableOpacity>
        </View>

        <View style={s.searchWrapper}>
          <TextInput
            style={s.searchInput}
            placeholder="Search events..."
            placeholderTextColor={'#90b8d8'}
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
          ListEmptyComponent={<Text style={s.emptyText}>No events found.</Text>}
          renderItem={({ item }) => (
            <View style={s.card}>
              <View style={s.cardTop}>
                <Text style={s.cardTitle}>{item.title}</Text>
                <Text style={s.cardDate}>{item.date}</Text>
              </View>
              <Text style={s.cardDesc}>{item.description}</Text>
              <TouchableOpacity
                style={[s.joinBtn, joined.includes(item.id) && s.joinBtnActive]}
                onPress={() => toggleJoin(item.id)}
                activeOpacity={0.8}>
                <Text style={[s.joinText, joined.includes(item.id) && s.joinTextActive]}>
                  {joined.includes(item.id) ? 'Joined' : 'Join Event'}
                </Text>
              </TouchableOpacity>
            </View>
          )}
        />

      </View>
    </SafeAreaView>
  );
};

const s = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  container: {
    flex: 1,
    paddingHorizontal: 28,
    paddingTop: 40,
  },

  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  title: {
    fontSize: 36,
    fontWeight: '800',
    color: '#2b7cd3',
    letterSpacing: -1,
  },
  logoutText: {
    fontSize: 13,
    color: '#90b8d8',
    fontWeight: '600',
  },

  searchWrapper: {
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
    marginBottom: 20,
  },
  searchInput: {
    paddingHorizontal: 16,
    paddingVertical: 13,
    fontSize: 15,
    color: '#2b7cd3',
  },

  list: {
    paddingBottom: 40,
  },
  emptyText: {
    textAlign: 'center',
    color: '#90b8d8',
    marginTop: 40,
    fontSize: 14,
  },

  card: {
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
    padding: 18,
    marginBottom: 14,
  },
  cardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2b7cd3',
    flex: 1,
    marginRight: 8,
  },
  cardDate: {
    fontSize: 11,
    color: '#90b8d8',
    fontWeight: '600',
  },
  cardDesc: {
    fontSize: 13,
    color: '#5a85a8',
    lineHeight: 19,
    marginBottom: 14,
  },

  joinBtn: {
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#2b7cd3',
    paddingVertical: 9,
    alignItems: 'center',
  },
  joinBtnActive: {
    backgroundColor: '#2b7cd3',
  },
  joinText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#2b7cd3',
  },
  joinTextActive: {
    color: '#ffffff',
  },
});

export default UserHome;