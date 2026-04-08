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

  // success screen
  successContainer: {
    flex: 1,
    paddingHorizontal: 28,
    paddingTop: 80,
    alignItems: 'center',
  },
  successIcon: {
    fontSize: 56,
    marginBottom: 24,
  },
  successTitle: {
    fontSize: 28,
    fontWeight: '800',
    color: '#2b7cd3',
    letterSpacing: -0.5,
    marginBottom: 16,
    textAlign: 'center',
  },
  successSubtitle: {
    fontSize: 15,
    color: '#6b8ea8',
    textAlign: 'center',
    marginBottom: 12,
    lineHeight: 22,
  },
  successEmail: {
    fontWeight: '700',
    color: '#2b7cd3',
  },

  toggleRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 20,
  },
  toggleCard: {
    flex: 1,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#90b8d8',
    paddingVertical: 14,
    alignItems: 'center',
  },
  toggleCardActive: {
    borderColor: '#2b7cd3',
    backgroundColor: '#e8f2fb',
  },
  toggleLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#90b8d8',
  },
  toggleLabelActive: {
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

  infoBox: {
    backgroundColor: '#fff8e1',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#ffe082',
    padding: 13,
    marginBottom: 4,
  },
  infoBoxText: {
    color: '#b07d00',
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
    marginTop: 16,
  },
  hint: {
    marginTop: 5,
    fontSize: 12,
    color: '#b0c8de',
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

  modePill: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: 10,
    gap: 2,
  },
  pillBtn: {
    paddingHorizontal: 8,
    paddingVertical: 6,
    borderRadius: 8,
  },
  pillBtnActive: {
    backgroundColor: '#e8f2fb',
  },
  divider: {
    width: 1,
    height: 20,
    backgroundColor: '#d0e4f0',
    marginHorizontal: 6,
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
    marginTop: 28,
    paddingVertical: 16,
    alignItems: 'center',
    width: '100%',
  },
  submitBtnDisabled: {
    backgroundColor: '#90b8d8',
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