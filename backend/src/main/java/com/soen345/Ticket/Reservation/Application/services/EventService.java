package com.soen345.Ticket.Reservation.Application.services;

import com.soen345.Ticket.Reservation.Application.models.Event;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    // in-memory storage
    private final Map<String, Event> eventStore = new HashMap<>();
    private int idCounter = 1;


    public Event createEvent(Event event) {
        String id = String.valueOf(idCounter++);
        event.setId(id);
        eventStore.put(id, event);
        return event;
    }


    public Event editEvent(String eventId, Event updatedEvent) {
        Event existing = eventStore.get(eventId);

        if (existing == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }
        if ("cancelled".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot edit a cancelled event");
        }

        existing.setTitle(updatedEvent.getTitle());
        existing.setDate(updatedEvent.getDate());
        existing.setLocation(updatedEvent.getLocation());
        existing.setCategory(updatedEvent.getCategory());
        existing.setDescription(updatedEvent.getDescription());

        eventStore.put(eventId, existing);
        return existing;
    }


    public Event cancelEvent(String eventId) {
        Event existing = eventStore.get(eventId);

        if (existing == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }
        if ("cancelled".equals(existing.getStatus())) {
            throw new RuntimeException("Event is already cancelled");
        }

        existing.setStatus("cancelled");
        eventStore.put(eventId, existing);
        return existing;
    }


    public List<Event> getAllEvents() {
        return new ArrayList<>(eventStore.values());
    }


    public Event getEventById(String eventId) {
        Event event = eventStore.get(eventId);
        if (event == null) {
            throw new RuntimeException("Event not found: " + eventId);
        }
        return event;
    }
}
