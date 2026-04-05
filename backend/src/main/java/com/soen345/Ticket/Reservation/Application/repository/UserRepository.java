package com.soen345.Ticket.Reservation.Application.repository;

import com.google.cloud.firestore.*;
import com.soen345.Ticket.Reservation.Application.model.Role;
import com.soen345.Ticket.Reservation.Application.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "users";

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public User save(User user) {
        try {
            firestore.collection(COLLECTION).document(user.getId()).set(user).get();
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public Optional<User> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            return doc.exists() ? Optional.ofNullable(doc.toObject(User.class)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by ID", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .get().get();
            if (snapshot.isEmpty()) return Optional.empty();
            return Optional.of(snapshot.getDocuments().get(0).toObject(User.class));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    public boolean existsByEmail(String email) {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .get().get();
            return !snapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
    }

    public List<User> findByRole(Role role) {
        try {
            return firestore.collection(COLLECTION)
                    .whereEqualTo("role", role.name())
                    .get().get()
                    .getDocuments().stream()
                    .map(d -> d.toObject(User.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding users by role", e);
        }
    }

    public List<User> findAll() {
        try {
            return firestore.collection(COLLECTION).get().get()
                    .getDocuments().stream()
                    .map(d -> d.toObject(User.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
    }

    public void deleteAll() {
        try {
            for (DocumentReference doc : firestore.collection(COLLECTION).listDocuments()) {
                doc.delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting all users", e);
        }
    }
}


