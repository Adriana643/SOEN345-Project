package com.soen345.Ticket.Reservation.Application.controller;


import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.service.EventService;
import com.soen345.Ticket.Reservation.Application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    private String resolveUserId(String authHeader) {
        return userService.getUserIdFromToken(authHeader);
    }

    /* ── POST /api/events ────────────────────────────────────── */
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Event event) {

        String userId = resolveUserId(token);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Missing or invalid session token."));
        }

        try {
            Event created = eventService.createEvent(event, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("admin")
                    ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        }
    }

    /* ── PUT /api/events/{eventId} ───────────────────────────── */
    @PutMapping("/{eventId}")
    public ResponseEntity<?> editEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String eventId,
            @RequestBody Event updatedEvent) {

        String userId = resolveUserId(token);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Missing or invalid session token."));
        }

        try {
            Event edited = eventService.editEvent(eventId, updatedEvent, userId);
            return ResponseEntity.ok(edited);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("admin")
                    ? HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        }
    }

    /* ── PATCH /api/events/{eventId}/cancel ──────────────────── */
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<?> cancelEvent(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String eventId) {

        String userId = resolveUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Missing or invalid session token."));
        }

        try {
            Event cancelled = eventService.cancelEvent(eventId, userId);
            return ResponseEntity.ok(cancelled);
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("admin")
                    ? HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        }
    }

    /* ── GET /api/events/search?date=&location=&category= ───── */

    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title){

        return ResponseEntity.ok(eventService.searchEvents(date, location, category, title));
    }

    /* ── GET /api/events ─────────────────────────────────────── */

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /* ── GET /api/events/{eventId} ───────────────────────────── */
    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable String eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventById(eventId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

