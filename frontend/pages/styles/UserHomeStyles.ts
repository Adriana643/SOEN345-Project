import { StyleSheet } from 'react-native';

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
  cardLocation: {
    fontSize: 12,
    fontWeight: '800',
    color: '#80aedf',
    letterSpacing: -0.5,
    marginBottom: 20,
  },
  locationRow: {
    flexDirection: 'row',
    gap: 4,
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

export default s;