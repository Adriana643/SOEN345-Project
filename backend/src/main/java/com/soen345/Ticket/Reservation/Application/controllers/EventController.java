package com.soen345.Ticket.Reservation.Application.controllers;


import com.soen345.Ticket.Reservation.Application.models.Event;
import com.soen345.Ticket.Reservation.Application.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // replace with real JWT token check later
    private boolean isAdmin(String token) {
        return token != null && token.equals("admin-test-token");
    }


    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Event event) {

        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Admins only");
        }

        try {
            Event created = eventService.createEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PutMapping("/{eventId}")
    public ResponseEntity<?> editEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String eventId,
            @RequestBody Event updatedEvent) {

        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Admins only");
        }

        try {
            Event edited = eventService.editEvent(eventId, updatedEvent);
            return ResponseEntity.ok(edited);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<?> cancelEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String eventId) {

        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Admins only");
        }

        try {
            Event cancelled = eventService.cancelEvent(eventId);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }


    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventById(eventId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
