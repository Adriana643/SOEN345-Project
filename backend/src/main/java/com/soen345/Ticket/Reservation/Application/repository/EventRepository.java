package com.soen345.Ticket.Reservation.Application.repository;

import com.google.cloud.firestore.*;
import com.soen345.Ticket.Reservation.Application.model.Event;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class EventRepository {

    private final Firestore firestore;
    private static final String COLLECTION = "events";

    public EventRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Event save(Event event) {
        try {
            firestore.collection(COLLECTION).document(event.getId()).set(event).get();
            return event;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving event", e);
        }
    }

    public Optional<Event> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            return doc.exists() ? Optional.ofNullable(doc.toObject(Event.class)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding event by ID", e);
        }
    }

    public List<Event> findAll() {
        try {
            return firestore.collection(COLLECTION).get().get()
                    .getDocuments().stream()
                    .map(d -> d.toObject(Event.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching all events", e);
        }
    }

    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting event: " + id, e);
        }
    }

    public void deleteAll() {
        try {
            for (DocumentReference doc : firestore.collection(COLLECTION).listDocuments()) {
                doc.delete().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting all events", e);
        }
    }


    // Returns non-cancelled events
    public List<Event> findAllActive() {
        try {
            return firestore.collection(COLLECTION)
                    .whereEqualTo("status", Event.STATUS_ACTIVE)
                    .get().get()
                    .getDocuments().stream()
                    .map(d -> d.toObject(Event.class))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching active events", e);
        }
    }
}