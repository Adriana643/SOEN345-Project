import React, { useRef, useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StatusBar, ActivityIndicator, ScrollView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { FirebaseRecaptchaVerifierModal } from 'expo-firebase-recaptcha';
import app from '../config/firebaseConfig';
import { RootStackParamList } from '../App';
import { registerWithEmail, Role } from '../services/authService';
import s from './styles/RegistrationStyles';
import { Ionicons } from '@expo/vector-icons';

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, 'Register'>;
};

type Mode = 'email' | 'phone';

type FormErrors = {
  name?: string;
  contact?: string;
  password?: string;
  confirm?: string;
  api?: string;
};

const RegisterScreen = ({ navigation }: Props) => {
  const recaptchaVerifier = useRef<any>(null);

  const [mode, setMode] = useState<Mode>('email');
  const [role, setRole] = useState<Role>('client');
  const [name, setName] = useState('');
  const [contact, setContact] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

  const clearErr = (key: keyof FormErrors) =>
    setErrors(e => ({ ...e, [key]: undefined }));

  const switchMode = (m: Mode) => {
    setMode(m);
    setContact('');
    clearErr('contact');
  };

  const validate = (): boolean => {
    const e: FormErrors = {};
    if (!name.trim()) e.name = 'Full name is required.';
    if (mode === 'email') {
      if (!contact.trim()) e.contact = 'Email is required.';
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact))
        e.contact = 'Enter a valid email address.';
    } else {
      if (!contact.trim()) e.contact = 'Phone number is required.';
      else if (!/^\+[1-9]\d{7,14}$/.test(contact.replaceAll(/\s/g, '')))
        e.contact = 'Include country code, e.g. +15141234567.';
    }
    if (!password) e.password = 'Password is required.';
    if (!confirm) e.confirm = 'Please confirm your password.';
    else if (confirm !== password) e.confirm = 'Passwords do not match.';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) return;
    setLoading(true);
    setErrors({});
    try {
      if (mode === 'email') {
        await registerWithEmail(name, contact, password, role);
        setEmailSent(true);
      }
    } catch (e: any) {
      setErrors({ api: e.message || 'Error creating account.' });
    } finally {
      setLoading(false);
    }
  };

  // success after email is sent
  if (emailSent) {
    return (
      <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
        <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />
        <View style={s.successContainer}>
          <Text style={s.successTitle}>Check your email!</Text>
          <Text style={s.successSubtitle}>
            We sent a verification link to{'\n'}
            <Text style={s.successEmail}>{contact}</Text>
          </Text>
          <TouchableOpacity
            style={s.submitBtn}
            onPress={() => navigation.navigate('Login')}
            activeOpacity={0.85}>
            <Text style={s.submitText}>Go to Login</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={s.safe} edges={['top', 'bottom']}>
      <StatusBar barStyle="dark-content" backgroundColor="#ffffff" />

      <FirebaseRecaptchaVerifierModal
        ref={recaptchaVerifier}
        firebaseConfig={app.options}
        attemptInvisibleVerification
      />

      <ScrollView
        contentContainerStyle={s.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>
        <View style={s.container}>
          <Text style={s.title}>Register</Text>

          {/* role toggle */}
          <View style={s.toggleRow}>
            {(['client', 'admin'] as Role[]).map(r => (
              <TouchableOpacity
                key={r}
                style={[s.toggleCard, role === r && s.toggleCardActive]}
                onPress={() => setRole(r)}
                activeOpacity={0.8}>
                <Text style={[s.toggleLabel, role === r && s.toggleLabelActive]}>
                  {r === 'client' ? 'Client' : 'Admin'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* api error msg */}
          {!!(errors.api) && (
            <View style={s.errorBox}>
              <Text style={s.errorBoxText}>{errors.api}</Text>
            </View>
          )}

          {/* full name */}
          <Text style={[s.label, s.labelSpacing]}>Full Name</Text>
          <View style={[s.inputWrapper, errors.name ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={name}
              onChangeText={t => { setName(t); clearErr('name'); }}
              autoCapitalize="words"
              autoCorrect={false}
              placeholder="Full Name"
              placeholderTextColor="#c0d8ec"
            />
          </View>
          {!!(errors.name) && <Text style={s.fieldErr}>{errors.name}</Text>}

          {/* email or phone nb */}
          <Text style={[s.label, s.labelSpacing]}>
            {mode === 'email' ? 'Email' : 'Phone Number'}
          </Text>
          <View style={[s.inputWrapper, errors.contact ? s.inputErr : null]}>
            <View style={s.modePill}>
              <TouchableOpacity
                style={[s.pillBtn, mode === 'email' && s.pillBtnActive]}
                onPress={() => switchMode('email')}
                activeOpacity={0.7}>
                <Ionicons name="mail" size={18} color={mode === 'email' ? '#2b7cd3' : '#90b8d8'} />
              </TouchableOpacity>
              <TouchableOpacity
                style={[s.pillBtn, mode === 'phone' && s.pillBtnActive]}
                onPress={() => switchMode('phone')}
                activeOpacity={0.7}>
                <Ionicons name="call" size={18} color={mode === 'phone' ? '#2b7cd3' : '#90b8d8'} />
              </TouchableOpacity>
            </View>
            <View style={s.divider} />
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={contact}
              onChangeText={t => { setContact(t); clearErr('contact'); }}
              keyboardType={mode === 'email' ? 'email-address' : 'phone-pad'}
              autoCapitalize="none"
              autoCorrect={false}
              placeholder={mode === 'email' ? 'you@example.com' : '+1 514 123 4567'}
              placeholderTextColor="#c0d8ec"
            />
          </View>
          {!!(errors.contact) && <Text style={s.fieldErr}>{errors.contact}</Text>}

          {/* SMS unavailable notice */}
          {mode === 'phone' && (
            <View style={s.infoBox}>
              <Text style={s.infoBoxText}>
                SMS verification is not available at this time. Please register using your email address.
              </Text>
            </View>
          )}

          {/* password */}
          <Text style={[s.label, s.labelSpacing]}>Password</Text>
          <View style={[s.inputWrapper, errors.password ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={password}
              onChangeText={t => { setPassword(t); clearErr('password'); }}
              secureTextEntry={!showPassword}
              autoCapitalize="none"
              placeholder="******"
              placeholderTextColor="#c0d8ec"
            />
            <TouchableOpacity onPress={() => setShowPassword(v => !v)} style={s.eyeBtn}>
              <Text style={s.eyeText}>{showPassword ? 'Hide' : 'Show'}</Text>
            </TouchableOpacity>
          </View>
          {!!(errors.password) && <Text style={s.fieldErr}>{errors.password}</Text>}

          {/* confirmed password */}
          <Text style={[s.label, s.labelSpacing]}>Confirm Password</Text>
          <View style={[s.inputWrapper, errors.confirm ? s.inputErr : null]}>
            <TextInput
              style={[s.input, { flex: 1 }]}
              value={confirm}
              onChangeText={t => { setConfirm(t); clearErr('confirm'); }}
              secureTextEntry={!showConfirm}
              autoCapitalize="none"
              placeholder="******"
              placeholderTextColor="#c0d8ec"
            />
            <TouchableOpacity onPress={() => setShowConfirm(v => !v)} style={s.eyeBtn}>
              <Text style={s.eyeText}>{showConfirm ? 'Hide' : 'Show'}</Text>
            </TouchableOpacity>
          </View>
          {!!(errors.confirm) && <Text style={s.fieldErr}>{errors.confirm}</Text>}

          <TouchableOpacity
            style={[s.submitBtn, (loading || mode === 'phone') && s.submitBtnDisabled]}
            onPress={handleSubmit}
            activeOpacity={0.85}
            disabled={loading || mode === 'phone'}>
            {loading
              ? <ActivityIndicator color="#ffffff" size="small" />
              : <Text style={s.submitText}>
                  {mode === 'phone' ? 'Unavailable' : 'Create Account'}
                </Text>
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

export default RegisterScreen;