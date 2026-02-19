import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyA2uDoqtgd2Fp9w48dHUCss9aXfGM_j5vs",
  authDomain: "",
  projectId: "soen-345",
  storageBucket: "soen-345.firebasestorage.app",
  
  appId: "1:134305855941:android:f9fc6789144c606b7273ac"
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
