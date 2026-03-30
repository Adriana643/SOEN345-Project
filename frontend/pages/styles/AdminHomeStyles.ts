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
    position: 'absolute',
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.3)',
  },
  modalCard: {
    position: 'absolute',
    bottom: 0, left: 0, right: 0,
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

export default s;