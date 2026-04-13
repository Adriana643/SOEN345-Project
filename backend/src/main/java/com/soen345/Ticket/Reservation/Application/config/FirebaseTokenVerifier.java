package com.soen345.Ticket.Reservation.Application.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenVerifier {

    /**
     * Verifies a Firebase ID token and returns the decoded token.
     * Returns null if the token is invalid or missing.
     */
    public FirebaseToken verify(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String idToken = authHeader.substring(7);
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            return null;
        }
    }
}