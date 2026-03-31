import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet, StatusBar, ActivityIndicator, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import { registerUser, Role } from '../services/authService';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Register'>;
};

type FormErrors = {
  name?: string;
  email?: string;
  password?: string;
  confirm?: string;
  api?: string;
};

const RegisterScreen = ({ navigation }: Props) => {
  const [role, setRole] = useState<Role>('client');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  const validate = (): boolean => {
    const e: FormErrors = {};

    if (!name.trim()) {
      e.name = 'Full name is required.';
    } 
    if (!email.trim()){
      e.email    = 'Email is required.';
    }               
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      e.email = 'Enter a valid email.';
    }

    if (!password) {
      e.password = 'Password is required.';
    }

    if (!confirm){
      e.confirm  = 'Please confirm your password.';
    }
    else if (confirm !== password){
      e.confirm  = 'Passwords do not match.';
    }

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleRegister = async () => {
    if (!validate()) return;
    setLoading(true);
    setErrors({});
    try {
      const { role: returnedRole } = await registerUser(name, email, password, role);
      navigation.replace(returnedRole === 'admin' ? 'AdminHome' : 'UserHome');
    } catch (e: any) {
      setErrors({ api: e.message || 'Error creating account.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor={'#ffffff'} />
      <ScrollView contentContainerStyle={s.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        <View style={s.container}>

          <Text style={s.title}>Register</Text>

          <View style={s.roleRow}>
            {(['client', 'admin'] as Role[]).map(r => (
              <TouchableOpacity
                key={r}
                style={[s.roleCard, role === r && s.roleCardActive]}
                onPress={() => setRole(r)}
                activeOpacity={0.8}>
                <Text style={[s.roleLabel, role === r && s.roleLabelActive]}>
                  {r === 'client' ? 'Client' : 'Admin'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {!!(errors.api) && (
            <View style={s.errorBox}>
              <Text style={s.errorBoxText}>{errors.api}</Text>
            </View>
          )}

          <Text style={[s.label, s.labelSpacing]}>Email</Text>
          <View style={[s.inputWrapper, errors.email ? s.inputErr : null]}>
            <TextInput
              style={s.input}
              value={email}
              onChangeText={t => { setEmail(t); setErrors(e => ({ ...e, email: undefined })); }}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
            />
          </View>
          {!!(errors.email) && <Text style={s.fieldErr}>{errors.email}</Text>}

          <Text style={[s.label, s.labelSpacing]}>Password</Text>
          <View style={[s.inputWrapper, errors.password ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={password}
              onChangeText={t => { setPassword(t); setErrors(e => ({ ...e, password: undefined })); }}
              secureTextEntry={!showPassword}
              autoCapitalize="none"
            />
            <TouchableOpacity onPress={() => setShowPassword(v => !v)} style={s.eyeBtn}>
              <Text style={s.eyeText}>{showPassword ? 'Hide' : 'Show'}</Text>
            </TouchableOpacity>
          </View>
          {!!(errors.password) && <Text style={s.fieldErr}>{errors.password}</Text>}

          <Text style={[s.label, s.labelSpacing]}>Confirm password</Text>
          <View style={[s.inputWrapper, errors.confirm ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={confirm}
              onChangeText={t => { setConfirm(t); setErrors(e => ({ ...e, confirm: undefined })); }}
              secureTextEntry={!showConfirm}
              autoCapitalize="none"
            />
            <TouchableOpacity onPress={() => setShowConfirm(v => !v)} style={s.eyeBtn}>
              <Text style={s.eyeText}>{showConfirm ? 'Hide' : 'Show'}</Text>
            </TouchableOpacity>
          </View>
          {!!(errors.confirm) && <Text style={s.fieldErr}>{errors.confirm}</Text>}

          {/* Submit */}
          <TouchableOpacity
            style={[s.submitBtn, loading && { opacity: 0.55 }]}
            onPress={handleRegister}
            activeOpacity={0.85}
            disabled={loading}>
            {loading
              ? <ActivityIndicator color={'#ffffff'} size="small" />
              : <Text style={s.submitText}>Create Account</Text>
            }
          </TouchableOpacity>

          <View style={s.footer}>
            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
              <Text style={s.footerLink}>Go Back</Text>
            </TouchableOpacity>
          </View>

        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const s = StyleSheet.create({
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

export default RegisterScreen;