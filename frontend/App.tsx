import React, { useEffect, useState } from 'react';
import { registerRootComponent } from 'expo';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './config/firebaseConfig';

import { Role } from './services/authService';
import Login from './pages/Login';
import Register from './pages/Registration';
import AdminHome from './pages/AdminHome';
import UserHome from './pages/UserHome';

export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  AdminHome: undefined;
  UserHome: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

const App = () => {
  const [isLoading, setIsLoading] = useState(true);
  const [initialRoute, setInitialRoute] = useState<keyof RootStackParamList>('Login');
  
  useEffect(() => {
    const checkSession = async () => {
      try {
        const token = await SecureStore.getItemAsync('authToken');
        const role = await SecureStore.getItemAsync('userRole');

        if (token && role) {
          setInitialRoute(role === 'admin' ? 'AdminHome' : 'UserHome');
        } else {
          setInitialRoute('Login');
        }
      } catch (e) {
        console.error('Session check failed:', e);
        setInitialRoute('Login');
      }
      setIsLoading(false);
    };

    checkSession();
  }, []);

  if (isLoading) {
    return (
      <View style={styles.splash}>
        <ActivityIndicator size="large" color="#f5a623" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={initialRoute}
        screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Login" component={Login} />
        <Stack.Screen name="Register" component={Register} />
        <Stack.Screen name="AdminHome" component={AdminHome} />
        <Stack.Screen name="UserHome" component={UserHome} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    backgroundColor: '#0d0d0d',
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default App;
registerRootComponent(App);