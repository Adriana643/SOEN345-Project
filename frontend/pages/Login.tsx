import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StatusBar, ActivityIndicator, ScrollView,} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';
import { loginUser } from '../services/authService';
import s from './styles/LoginStyles';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Login'>;
};

const LoginScreen = ({ navigation }: Props) => {  
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
      const { role: returnedRole } = await loginUser(email, password);
      navigation.replace(returnedRole === 'admin' ? 'AdminHome' : 'UserHome');
    } catch (e: any) {
      setErrors({ api: e.message || 'Invalid email or password.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
      <ScrollView
        contentContainerStyle={s.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>
        <View style={s.container}>

        <Text style={s.title}>Sign In</Text>

          {/* API error */}
          {!!(errors.api) && (
            <View style={s.errorBox}>
              <Text style={s.errorBoxText}>{errors.api}</Text>
            </View>
          )}

          {/* Email */}
          <Text style={s.label}>Email</Text>
          <View style={[s.inputWrapper, errors.email ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={email}
              onChangeText={t => { setEmail(t); setErrors(e => ({ ...e, email: undefined })); }}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
            />
          </View>
          {!!(errors.email) && <Text style={s.fieldErr}>{errors.email}</Text>}

          {/* Password */}
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

          <TouchableOpacity
            style={[s.submitBtn, loading && { opacity: 0.55 }]}
            onPress={handleLogin}
            activeOpacity={0.85}
            disabled={loading}>
            {loading
              ? <ActivityIndicator color="#ffffff" size="small" />
              : <Text style={s.submitText}>Sign In</Text>
            }
          </TouchableOpacity>

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

export default LoginScreen;