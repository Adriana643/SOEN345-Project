package com.soen345.Ticket.Reservation.Application;

import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.soen345.Ticket.Reservation.Application.controller.EventController;

import static org.junit.jupiter.api.Assertions.*;

class EventControllerTest {

    private EventController eventController;
    private EventService eventService;


    @BeforeEach
    void setUp() {
        eventService = new EventService();
        eventController = new EventController(eventService);
    }


    private Event sampleEvent() {
        return new Event(null, "Concert", "2025-12-01",
                "Montreal", "Music", "A great show");
    }



    @Test
    void adminCanCreateEvent() {
        ResponseEntity<?> response = eventController
                .createEvent("admin-test-token", sampleEvent());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Event created = (Event) response.getBody();
        assertNotNull(created.getId());
        assertEquals("Concert", created.getTitle());
    }

    @Test
    void adminCanEditEvent() {

        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();


        Event updated = new Event(null, "Updated Concert", "2025-12-15",
                "Toronto", "Music", "Even better show");
        ResponseEntity<?> editResponse = eventController
                .editEvent("admin-test-token", created.getId(), updated);

        assertEquals(HttpStatus.OK, editResponse.getStatusCode());
        Event edited = (Event) editResponse.getBody();
        assertEquals("Updated Concert", edited.getTitle());
        assertEquals("Toronto", edited.getLocation());
    }

    @Test
    void adminCanCancelEvent() {

        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();


        ResponseEntity<?> cancelResponse = eventController
                .cancelEvent("admin-test-token", created.getId());

        assertEquals(HttpStatus.OK, cancelResponse.getStatusCode());
        Event cancelled = (Event) cancelResponse.getBody();
        assertEquals("cancelled", cancelled.getStatus());
    }



    @Test
    void customerCannotCreateEvent() {
        ResponseEntity<?> response = eventController
                .createEvent("customer-token", sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void customerCannotEditEvent() {

        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();


        ResponseEntity<?> response = eventController
                .editEvent("customer-token", created.getId(), sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void customerCannotCancelEvent() {

        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();


        ResponseEntity<?> response = eventController
                .cancelEvent("customer-token", created.getId());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }



    @Test
    void noTokenCannotCreateEvent() {
        ResponseEntity<?> response = eventController
                .createEvent(null, sampleEvent());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }



    @Test
    void cannotEditNonExistentEvent() {
        Event updated = sampleEvent();
        ResponseEntity<?> response = eventController
                .editEvent("admin-test-token", "999", updated);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void cannotCancelNonExistentEvent() {
        ResponseEntity<?> response = eventController
                .cancelEvent("admin-test-token", "999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void cannotCancelAlreadyCancelledEvent() {

        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();
        eventController.cancelEvent("admin-test-token", created.getId());


        ResponseEntity<?> response = eventController
                .cancelEvent("admin-test-token", created.getId());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void anyoneCanGetAllEvents() {
        eventController.createEvent("admin-test-token", sampleEvent());

        ResponseEntity<?> response = eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void anyoneCanGetEventById() {
        ResponseEntity<?> createResponse = eventController
                .createEvent("admin-test-token", sampleEvent());
        Event created = (Event) createResponse.getBody();

        ResponseEntity<?> response = eventController
                .getEventById(created.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}