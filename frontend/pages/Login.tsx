import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet, StatusBar, ActivityIndicator, ScrollView,} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import { loginUser, Role } from '../services/AuthService';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Login'>;
};

const LoginScreen = ({ navigation }: Props) => {
  const [role, setRole] = useState<Role>('client');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{ email?: string; password?: string; api?: string }>({});
  const [loading, setLoading] = useState(false);

  const validate = (): boolean => {
    const e: typeof errors = {};
    if (!email.trim()) {
      e.email = 'Email is required.';
    }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      e.email = 'Enter a valid email.';
    }

    if (!password){
      e.password = 'Password is required.';
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleLogin = async () => {
    if (!validate()) return;
    setLoading(true);
    setErrors({});
    try {
      const { role: returnedRole } = await loginUser(email, password, role);
      navigation.replace(returnedRole === 'admin' ? 'AdminHome' : 'UserHome');
    } catch (e: any) {
      setErrors({ api: e.message || 'Invalid email or password.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor={'#ffffff'} />
        <ScrollView contentContainerStyle={s.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
          <View style={s.container}>

            <Text style={s.title}>Sign In</Text>

            {/* Role */}
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

            {/* API Error */}
            {errors.api && (
              <View style={s.errorBox}>
                <Text style={s.errorBoxText}>{errors.api}</Text>
              </View>
            )}

            {/* Email */}
            <Text style={s.label}>Email</Text>
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
            {errors.email && <Text style={s.fieldErr}>{errors.email}</Text>}


            <Text style={[s.label, { marginTop: 14 }]}>Password</Text>
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
            {errors.password && <Text style={s.fieldErr}>{errors.password}</Text>}

            <TouchableOpacity
              style={[s.submitBtn, loading && { opacity: 0.55 }]}
              onPress={handleLogin}
              activeOpacity={0.85}
              disabled={loading}>
              {loading
                ? <ActivityIndicator color={'#ffffff'} size="small" />
                : <Text style={s.submitText}>Sign in</Text>
              }
            </TouchableOpacity>


          <View >
            <Text>For testing (just click on whichever page u wanna view, erase after):</Text>
            <View>
              <TouchableOpacity onPress={() => navigation.replace('UserHome')}>
                <Text >User View</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => navigation.replace('AdminHome')}>
                <Text>Admin View</Text>
              </TouchableOpacity>
            </View>
          </View>


            <View style={s.footer}>
              <TouchableOpacity onPress={() => navigation.navigate('Register')}>
                <Text style={s.footerLink}>Register Account</Text>
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
  flex: {
    flex: 1,
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
  footerText: {
    fontSize: 14,
    color: '#90b8d8',
  },
  footerLink: {
    fontSize: 14,
    color: '#2b7cd3',
    fontWeight: '700',
  },
});

export default LoginScreen;