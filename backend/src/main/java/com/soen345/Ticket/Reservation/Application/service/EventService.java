package com.soen345.Ticket.Reservation.Application.service;

import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.repository.EventRepository;
import com.soen345.Ticket.Reservation.Application.repository.UserRepository;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;

    public EventService(EventRepository eventRepository,UserRepository userRepository,UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    /* CRUD operations */

    // Admin can create event
    public Event createEvent(Event event, String userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("Only admins can create events");
        }
        event.setId(UUID.randomUUID().toString()); // replaces idCounter
        return eventRepository.save(event);
    }

    // Admin can edit event
    public Event editEvent(String eventId, Event updatedEvent, String userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("Only admins can edit events.");
        }

        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (Event.STATUS_CANCELLED.equals(existing.getStatus())) {
            throw new RuntimeException("Cannot edit a cancelled event");
        }

        existing.setTitle(updatedEvent.getTitle());
        existing.setDate(updatedEvent.getDate());
        existing.setLocation(updatedEvent.getLocation());
        existing.setCategory(updatedEvent.getCategory());
        existing.setDescription(updatedEvent.getDescription());

        return eventRepository.save(existing);
    }

    // Admin can cancel event
    public Event cancelEvent(String eventId, String userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("Only admins can cancel events.");
        }
        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (Event.STATUS_CANCELLED.equals(existing.getStatus())) {
            throw new RuntimeException("Event is already cancelled");
        }

        existing.setStatus(Event.STATUS_CANCELLED);
        return eventRepository.save(existing);
    }
    //Admin can delete event
    public void deleteEvent(String eventId, String userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("Only admins can delete events.");
        }
        eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        eventRepository.deleteById(eventId);
    }

    public List<Event> searchEvents(String date, String location, String category,String title) {
        return eventRepository.findAllActive().stream()
                .filter(e -> isBlank(date)     ||  e.getDate().toLowerCase().contains(date.toLowerCase()))
                .filter(e -> isBlank(location) ||  e.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(e -> isBlank(category) ||  e.getCategory().toLowerCase().contains(category.toLowerCase()))
                .filter(e -> isBlank(title) || e.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
    }


    /* Helper methods */

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
