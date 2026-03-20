import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, StatusBar, FlatList, Modal, Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'AdminHome'>;
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

const AdminHome = ({ navigation }: Props) => {
  const [events, setEvents] = useState<Event[]>(SAMPLE_EVENTS);
  const [search, setSearch] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newDate, setNewDate] = useState('');
  const [formErr, setFormErr] = useState('');

  const filtered = events.filter(e => e.title.toLowerCase().includes(search.toLowerCase()));

  const handleAdd = () => {
    if (!newTitle.trim() || !newDesc.trim() || !newDate.trim()) {
      setFormErr('All fields are required.');
      return;
    }
    setEvents(prev => [{ id: Date.now().toString(), title: newTitle.trim(), description: newDesc.trim(), date: newDate.trim() }, ...prev]);
    setNewTitle('');
    setNewDesc('');
    setNewDate('');
    setFormErr('');
    setModalVisible(false);
  };

  const handleDelete = (id: string) => {
    Alert.alert('Delete Event', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => setEvents(prev => prev.filter(e => e.id !== id)) },
    ]);
  };

  const closeModal = () => {
    setModalVisible(false);
    setFormErr('');
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

        <View style={s.toolbar}>
          <View style={s.searchWrapper}>
            <TextInput
              style={s.searchInput}
              value={search}
              onChangeText={setSearch}
              autoCorrect={false}
            />
          </View>
          <TouchableOpacity style={s.addBtn} onPress={() => setModalVisible(true)} activeOpacity={0.8}>
            <Text style={s.addBtnText}>Add</Text>
          </TouchableOpacity>
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
              <TouchableOpacity style={s.deleteBtn} onPress={() => handleDelete(item.id)} activeOpacity={0.8}>
                <Text style={s.deleteText}>Delete</Text>
              </TouchableOpacity>
            </View>
          )}
        />

      </View>

      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={s.modalOverlay}>
          <View style={s.modalCard}>
            <Text style={s.modalTitle}>New Event</Text>

            {formErr ? (
              <View style={s.errorBox}>
                <Text style={s.errorBoxText}>{formErr}</Text>
              </View>
            ) : null}

            <Text style={s.label}>Title</Text>
            <View style={s.inputWrapper}>
              <TextInput style={s.input} value={newTitle} onChangeText={t => { setNewTitle(t); setFormErr(''); }}/>
            </View>

            <Text style={[s.label, s.labelSpacing]}>Description</Text>
            <View style={[s.inputWrapper, s.inputMultiline]}>
              <TextInput style={[s.input, {flex: 1 }]} value={newDesc} onChangeText={t => { setNewDesc(t); setFormErr(''); }} multiline/>
            </View>

            <Text style={[s.label, s.labelSpacing]}>Date</Text>
            <View style={s.inputWrapper}>
              <TextInput style={s.input} value={newDate} onChangeText={t => { setNewDate(t); setFormErr(''); }} />
            </View>

            <TouchableOpacity style={s.submitBtn} onPress={handleAdd}>
              <Text style={s.submitText}>Create Event</Text>
            </TouchableOpacity>
            <TouchableOpacity style={s.cancelBtn} onPress={closeModal}>
              <Text style={s.cancelText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

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
    fontWeight: '600',
    color: '#90b8d8',
  },
  toolbar: {
    flexDirection: 'row',
    gap: 10,
    alignItems: 'center',
    marginBottom: 20,
  },
  searchWrapper: {
    flex: 1,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
  },
  searchInput: {
    paddingHorizontal: 16,
    paddingVertical: 13,
    fontSize: 15,
    color: '#2b7cd3',
  },
  addBtn: {
    backgroundColor: '#2b7cd3',
    borderRadius: 12,
    paddingVertical: 13,
    paddingHorizontal: 18,
  },
  addBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#ffffff',
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
    flex: 1,
    fontSize: 15,
    fontWeight: '700',
    color: '#2b7cd3',
    marginRight: 8,
  },
  cardDate: {
    fontSize: 11,
    fontWeight: '600',
    color: '#90b8d8',
  },
  cardDesc: {
    fontSize: 13,
    color: '#5a85a8',
    lineHeight: 19,
    marginBottom: 14,
  },
  deleteBtn: {
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#e05555',
    paddingVertical: 9,
    alignItems: 'center',
  },
  deleteText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#e05555',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.3)',
    justifyContent: 'flex-end',
  },
  modalCard: {
    backgroundColor: '#ffffff',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: 28,
    paddingBottom: 40,
  },
  modalTitle: {
    fontSize: 24,
    fontWeight: '800',
    color: '#2b7cd3',
    letterSpacing: -0.5,
    marginBottom: 20,
  },
  errorBox: {
    backgroundColor: '#fff0f0',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#f5c0c0',
    padding: 13,
    marginBottom: 16,
  },
  errorBoxText: {
    fontSize: 13,
    color: '#cc3333',
  },
  label: {
    fontSize: 11,
    fontWeight: '700',
    color: '#90b8d8',
    letterSpacing: 1.5,
    textTransform: 'uppercase',
    marginBottom: 8,
  },
  labelSpacing: {
    marginTop: 14,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
  },
  inputMultiline: {
    minHeight: 80,
    alignItems: 'flex-start',
  },
  input: {
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 15,
    color: '#2b7cd3',
  },
  submitBtn: {
    backgroundColor: '#2b7cd3',
    borderRadius: 12,
    marginTop: 24,
    paddingVertical: 16,
    alignItems: 'center',
  },
  submitText: {
    fontSize: 15,
    fontWeight: '800',
    color: '#ffffff',
  },
  cancelBtn: {
    marginTop: 12,
    paddingVertical: 12,
    alignItems: 'center',
  },
  cancelText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#90b8d8',
  },
});

export default AdminHome;