import { StyleSheet } from 'react-native';

export default StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  scroll: {
    flexGrow: 1,
  },
  container: {
    paddingHorizontal: 28,
    paddingTop: 52,
    paddingBottom: 40,
  },
  title: {
    fontSize: 36,
    fontWeight: '800',
    color: '#2b7cd3',
    letterSpacing: -1,
    marginBottom: 32,
  },
  roleRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 28,
  },
  roleCard: {
    flex: 1,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
    paddingVertical: 14,
    alignItems: 'center',
  },
  roleCardActive: {
    borderColor: '#2b7cd3',
    backgroundColor: '#e8f2fb',
  },
  roleLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#90b8d8',
  },
  roleLabelActive: {
    color: '#2b7cd3',
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
    color: '#cc3333',
    fontSize: 13,
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
  inputErr: {
    borderColor: '#e05555',
  },
  input: {
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 15,
    color: '#2b7cd3',
  },
  eyeBtn: {
    paddingHorizontal: 16,
  },
  eyeText: {
    fontSize: 12,
    color: '#90b8d8',
    fontWeight: '600',
  },
  fieldErr: {
    marginTop: 5,
    fontSize: 12,
    color: '#e05555',
  },
  submitBtn: {
    backgroundColor: '#2b7cd3',
    borderRadius: 12,
    marginTop: 25,
    paddingVertical: 16,
    alignItems: 'center',
  },
  submitText: {
    fontSize: 15,
    fontWeight: '800',
    color: '#ffffff',
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 32,
  },
  footerLink: {
    fontSize: 14,
    color: '#2b7cd3',
    fontWeight: '700',
  },
});