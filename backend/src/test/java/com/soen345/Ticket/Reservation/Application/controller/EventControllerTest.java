package com.soen345.Ticket.Reservation.Application.controller;

import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.service.EventService;
import com.soen345.Ticket.Reservation.Application.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("EventController Tests")
class EventControllerTest {

    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;
    @InjectMocks
    private EventController eventController;


    private static final String ADMIN_TOKEN = "admin-test-token";
    private static final String CUSTOMER_TOKEN = "customer-token";
    private static final String ADMIN_ID = "admin-123";
    private static final String CUSTOMER_ID = "customer-456";


    private Event sampleEvent() {
        Event e = new Event(
                "event-1",
                "Concert",
                "2025-12-01",
                "Montreal",
                "Music",
                "A great show");
        return e;
    }

    @BeforeEach
    void setUp() {
        // Admin token resolves to ADMIN_ID, customer token resolves to CUSTOMER_ID
        lenient().when(userService.getUserIdFromToken(ADMIN_TOKEN)).thenReturn(ADMIN_ID);
        lenient().when(userService.getUserIdFromToken(CUSTOMER_TOKEN)).thenReturn(CUSTOMER_ID);
        lenient().when(userService.getUserIdFromToken(null)).thenReturn(null);
    }


    private Event createSampleEvent() {
        ResponseEntity<?> response = eventController
                .createEvent(ADMIN_TOKEN, sampleEvent());
        return (Event) response.getBody();
    }

    /* Create events */
    @Test
    @DisplayName("Admin can create an event and receives 201 CREATED")
    void adminCanCreateEvent() {
        when(eventService.createEvent(any(Event.class), eq(ADMIN_ID)))
                .thenReturn(sampleEvent());

        ResponseEntity<?> response = eventController
                .createEvent(ADMIN_TOKEN, sampleEvent());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Created event has an auto-assigned id and correct title")
    void createdEventHasIdAndCorrectTitle() {
        when(eventService.createEvent(any(Event.class), eq(ADMIN_ID)))
                .thenReturn(sampleEvent());

        ResponseEntity<?> response = eventController
                .createEvent(ADMIN_TOKEN, sampleEvent());

        Event created = (Event) response.getBody();

        assertAll("Created event body must be populated",
                () -> assertNotNull(created, "body must not be null"),
                () -> assertNotNull(created.getId(), "id must be assigned"),
                () -> assertEquals("Concert", created.getTitle(), "title must match")
        );
    }

    @Test
    @DisplayName("Customer token cannot create an event — 403 FORBIDDEN")
    void customerCannotCreateEvent() {
        when(eventService.createEvent(any(Event.class), eq(CUSTOMER_ID)))
                .thenThrow(new RuntimeException("Only admins can create events."));

        ResponseEntity<?> response = eventController.createEvent(CUSTOMER_TOKEN, sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    @DisplayName("Null token cannot create an event — 401 UNAUTHORIZED")
    void nullTokenCannotCreateEvent() {
        ResponseEntity<?> response = eventController.createEvent(null, sampleEvent());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /* Edit events */
    @Test
    @DisplayName("Admin can edit an event and receives 200 OK")
    void adminCanEditEvent() {

        Event updated = sampleEvent();
        updated.setTitle("Updated Concert");
        updated.setDate("2025-12-21");
        updated.setLocation("Ottawa");
        updated.setCategory("Music");
        updated.setDescription("Even better show");

        when(eventService.editEvent(eq(updated.getId()), any(Event.class), eq(ADMIN_ID)))
                .thenReturn(updated);

        ResponseEntity<?> response = eventController.editEvent(ADMIN_TOKEN, updated.getId(), updated);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Edited event body reflects updated fields")
    void editedEventBodyReflectsUpdates() {
        Event updated = sampleEvent();
        updated.setTitle("Updated Concert");
        updated.setDate("2025-12-15");
        updated.setLocation("Toronto");
        updated.setCategory("Music");
        updated.setDescription("Even better show");

        when(eventService.editEvent(eq(updated.getId()), any(Event.class), eq(ADMIN_ID)))
                .thenReturn(updated);

        ResponseEntity<?> response = eventController
                .editEvent(ADMIN_TOKEN, updated.getId(), updated);

        Event edited = (Event) response.getBody();

        assertAll("Edited event body must reflect every updated field",
                () -> assertEquals("Updated Concert", edited.getTitle(), "title not updated"),
                () -> assertEquals("2025-12-15", edited.getDate(), "date not updated"),
                () -> assertEquals("Toronto", edited.getLocation(), "location not updated"),
                () -> assertEquals("Music", edited.getCategory(), "category not updated"),
                () -> assertEquals("Even better show", edited.getDescription(), "description not updated")
        );
    }


    @Test
    @DisplayName("Customer token cannot edit an event — 403 FORBIDDEN")
    void customerCannotEditEvent() {
        Event event = sampleEvent();
        when(eventService.editEvent(any(), any(Event.class), eq(CUSTOMER_ID)))
                .thenThrow(new RuntimeException("Only admins can edit events."));

        ResponseEntity<?> response = eventController.editEvent(CUSTOMER_TOKEN, event.getId(), event);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Null token cannot edit an event — 401 UNAUTHORIZED")
    void nullTokenCannotEditEvent() {
        Event event = sampleEvent();

        ResponseEntity<?> response = eventController.editEvent(null, event.getId(), event);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    @DisplayName("Editing a non-existent event returns 404 NOT FOUND")
    void cannotEditNonExistentEvent() {
        when(eventService.editEvent(eq("999"), any(Event.class), eq(ADMIN_ID)))
                .thenThrow(new RuntimeException("Event not found: 999"));

        ResponseEntity<?> response = eventController.editEvent(ADMIN_TOKEN, "999", sampleEvent());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /* Cancel events */

    @Test
    @DisplayName("Admin can cancel an event and receives 200 OK")
    void adminCanCancelEvent() {
        Event cancelled = sampleEvent();
        cancelled.setStatus("cancelled");
        when(eventService.cancelEvent(eq("event-1"), eq(ADMIN_ID)))
                .thenReturn(cancelled);

        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, cancelled.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Cancelled event body has status 'cancelled'")
    void cancelledEventBodyHasCorrectStatus() {
        Event cancelled = sampleEvent();
        cancelled.setStatus("cancelled");
        when(eventService.cancelEvent(eq("event-1"), eq(ADMIN_ID)))
                .thenReturn(cancelled);

        ResponseEntity<?> response = eventController.cancelEvent(ADMIN_TOKEN, cancelled.getId());
        Event body = (Event) response.getBody();

        assertEquals("cancelled", body.getStatus(), "Status must be 'cancelled'");
    }


    @Test
    @DisplayName("Customer token cannot cancel an event — 403 FORBIDDEN")
    void customerCannotCancelEvent() {
        Event event = sampleEvent();
        when(eventService.cancelEvent(any(), eq(CUSTOMER_ID)))
                .thenThrow(new RuntimeException("Only admins can cancel events."));

        ResponseEntity<?> response = eventController.cancelEvent(CUSTOMER_TOKEN, event.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }


    @Test
    @DisplayName("Null token cannot cancel an event — 401 UNAUTHORIZED")
    void nullTokenCannotCancelEvent() {
        Event event = sampleEvent();

        ResponseEntity<?> response = eventController
                .cancelEvent(null, event.getId());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    @DisplayName("Cancelling a non-existent event returns 404 NOT FOUND")
    void cannotCancelNonExistentEvent() {
        when(eventService.cancelEvent(eq("999"), eq(ADMIN_ID)))
                .thenThrow(new RuntimeException("Event not found: 999"));

        ResponseEntity<?> response = eventController
                .cancelEvent(ADMIN_TOKEN, "999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Cancelling an already-cancelled event returns 404 NOT FOUND")
    void cannotCancelAlreadyCancelledEvent() {
        Event event = sampleEvent();
        when(eventService.cancelEvent(eq(event.getId()), eq(ADMIN_ID)))
                .thenThrow(new RuntimeException("Event is already cancelled"));

        ResponseEntity<?> response = eventController
                .cancelEvent(ADMIN_TOKEN, event.getId());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /* Get all events */
    @Test
    @DisplayName("getAllEvents returns 200 OK")
    void anyoneCanGetAllEvents() {
        when(eventService.getAllEvents())
                .thenReturn(List.of(sampleEvent()));

        ResponseEntity<?> response = eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getAllEvents response body contains all created events")
    void getAllEventsBodyContainsCreatedEvents() {
        when(eventService.getAllEvents())
                .thenReturn(List.of(sampleEvent(), sampleEvent()));

        ResponseEntity<?> response = eventController.getAllEvents();
        List<Event> events = (List<Event>) response.getBody();

        assertEquals(2, events.size(), "Response body must contain exactly 2 events");
    }


    /* Get events by id */

    @Test
    @DisplayName("getEventById returns 200 OK for an existing event")
    void anyoneCanGetEventById() {
        Event event = sampleEvent();
        when(eventService.getEventById(event.getId()))
                .thenReturn(sampleEvent());

        ResponseEntity<?> response = eventController.getEventById(event.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getEventById response body matches the requested event")
    void getEventByIdBodyMatchesCreatedEvent() {
        Event event = sampleEvent();
        when(eventService.getEventById(event.getId()))
                .thenReturn(sampleEvent());

        ResponseEntity<?> response = eventController.getEventById(event.getId());
        Event fetched = (Event) response.getBody();

        assertAll("Fetched event must match the created one",
                () -> assertEquals(event.getId(), fetched.getId(), "id mismatch"),
                () -> assertEquals(event.getTitle(), fetched.getTitle(), "title mismatch")
        );
    }

    @Test
    @DisplayName("getEventById returns 404 NOT FOUND for missing event")
    void getEventByIdReturnsNotFound() {
        when(eventService.getEventById("999"))
                .thenThrow(new RuntimeException("Event not found: 999"));

        ResponseEntity<?> response = eventController.getEventById("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /* Search events */

    @Test
    @DisplayName("searchEvents returns 200 OK with matching events")
    void searchEventsReturnsOk() {
        when(eventService.searchEvents("2025-12-01", "Montreal", "Music", null))
                .thenReturn(List.of(sampleEvent()));

        ResponseEntity<?> response = eventController.searchEvents("2025-12-01", "Montreal", "Music", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("searchEvents with no filters returns all active events")
    void searchEventsNoFilters() {
        when(eventService.searchEvents(null, null, null, null))
                .thenReturn(List.of(sampleEvent(), sampleEvent()));

        ResponseEntity<?> response = eventController.searchEvents(null, null, null, null);
        List<Event> results = (List<Event>) response.getBody();

        assertEquals(2, results.size());
    }
}
