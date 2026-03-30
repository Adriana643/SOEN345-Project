package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventController Tests")
class EventControllerTest {


    private EventController eventController;
    private EventService    eventService;

    private static final String ADMIN_TOKEN    = "admin-test-token";
    private static final String CUSTOMER_TOKEN = "customer-token";

    @BeforeEach
    void setUp() {
        eventService    = new EventService();
        eventController = new EventController(eventService);
    }

    private Event sampleEvent() {
        return new Event(null, "Concert", "2025-12-01",
                "Montreal", "Music", "A great show");
    }

    private Event createSampleEvent() {
        ResponseEntity<?> response = eventController.createEvent(ADMIN_TOKEN, sampleEvent());
        return (Event) response.getBody();
    }


    @Test
    @DisplayName("Admin can create an event and receives 201 CREATED")
    void adminCanCreateEvent() {
        ResponseEntity<?> response = eventController.createEvent(ADMIN_TOKEN, sampleEvent());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Created event has an auto-assigned id and correct title")
    void createdEventHasIdAndCorrectTitle() {
        ResponseEntity<?> response = eventController.createEvent(ADMIN_TOKEN, sampleEvent());
        Event created = (Event) response.getBody();

        assertAll("Created event body must be populated",
                () -> assertNotNull(created,            "body must not be null"),
                () -> assertNotNull(created.getId(),    "id must be assigned"),
                () -> assertEquals("Concert", created.getTitle(), "title must match")
        );
    }


    @Test
    @DisplayName("Admin can edit an event and receives 200 OK")
    void adminCanEditEvent() {
        Event created = createSampleEvent();
        Event updates = new Event(null, "Updated Concert", "2025-12-15",
                "Toronto", "Music", "Even better show");

        ResponseEntity<?> response = eventController.editEvent(ADMIN_TOKEN, created.getId(), updates);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Edited event body reflects updated fields")
    void editedEventBodyReflectsUpdates() {
        Event created = createSampleEvent();
        Event updates = new Event(null, "Updated Concert", "2025-12-15",
                "Toronto", "Music", "Even better show");

        ResponseEntity<?> response = eventController.editEvent(ADMIN_TOKEN, created.getId(), updates);
        Event edited = (Event) response.getBody();

        assertAll("Edited event body must reflect every updated field",
                () -> assertEquals("Updated Concert", edited.getTitle(),    "title not updated"),
                () -> assertEquals("2025-12-15",      edited.getDate(),     "date not updated"),
                () -> assertEquals("Toronto",         edited.getLocation(), "location not updated")
        );
    }


    @Test
    @DisplayName("Admin can cancel an event and receives 200 OK")
    void adminCanCancelEvent() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, created.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Cancelled event body has status 'cancelled'")
    void cancelledEventBodyHasCorrectStatus() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, created.getId());
        Event cancelled = (Event) response.getBody();

        assertEquals("cancelled", cancelled.getStatus(), "Status must be 'cancelled'");
    }

    @Test
    @DisplayName("Customer token cannot create an event — 403 FORBIDDEN")
    void customerCannotCreateEvent() {
        ResponseEntity<?> response = eventController.createEvent(CUSTOMER_TOKEN, sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Customer token cannot edit an event — 403 FORBIDDEN")
    void customerCannotEditEvent() {
        Event created  = createSampleEvent();

        ResponseEntity<?> response = eventController.editEvent(CUSTOMER_TOKEN, created.getId(), sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Customer token cannot cancel an event — 403 FORBIDDEN")
    void customerCannotCancelEvent() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.cancelEvent(CUSTOMER_TOKEN, created.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    @DisplayName("Null token cannot create an event — 403 FORBIDDEN")
    void nullTokenCannotCreateEvent() {
        ResponseEntity<?> response = eventController.createEvent(null, sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Null token cannot edit an event — 403 FORBIDDEN")
    void nullTokenCannotEditEvent() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.editEvent(null, created.getId(), sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Null token cannot cancel an event — 403 FORBIDDEN")
    void nullTokenCannotCancelEvent() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.cancelEvent(null, created.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Editing a non-existent event returns 404 NOT FOUND")
    void cannotEditNonExistentEvent() {
        ResponseEntity<?> response = eventController.editEvent(ADMIN_TOKEN, "999", sampleEvent());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Cancelling a non-existent event returns 404 NOT FOUND")
    void cannotCancelNonExistentEvent() {
        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, "999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Cancelling an already-cancelled event returns 404 NOT FOUND")
    void cannotCancelAlreadyCancelledEvent() {
        Event created = createSampleEvent();
        eventController.cancelEvent(ADMIN_TOKEN, created.getId()); // first cancel

        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, created.getId());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Getting a non-existent event by id returns 404 NOT FOUND")
    void getEventByIdReturnsNotFoundForMissingEvent() {
        ResponseEntity<?> response = eventController.getEventById("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("getAllEvents returns 200 OK")
    void anyoneCanGetAllEvents() {
        createSampleEvent();

        ResponseEntity<?> response = eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getAllEvents response body contains all created events")
    void getAllEventsBodyContainsCreatedEvents() {
        createSampleEvent();
        createSampleEvent();

        ResponseEntity<?> response = eventController.getAllEvents();
        List<Event> events = (List<Event>) response.getBody();

        assertEquals(2, events.size(), "Response body must contain exactly 2 events");
    }


    @Test
    @DisplayName("getEventById returns 200 OK for an existing event")
    void anyoneCanGetEventById() {
        Event created = createSampleEvent();

        ResponseEntity<?> response = eventController.getEventById(created.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getEventById response body matches the requested event")
    void getEventByIdBodyMatchesCreatedEvent() {
        Event created = createSampleEvent();

        ResponseEntity<?> response  = eventController.getEventById(created.getId());
        Event             fetched   = (Event) response.getBody();

        assertAll("Fetched event must match the created one",
                () -> assertEquals(created.getId(),    fetched.getId(),    "id mismatch"),
                () -> assertEquals(created.getTitle(), fetched.getTitle(), "title mismatch")
        );
    }
}