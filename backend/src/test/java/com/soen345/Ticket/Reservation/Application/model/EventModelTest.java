package com.soen345.Ticket.Reservation.Application.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Event Model Tests")
class EventModelTest {

    private static final String ID          = "1";
    private static final String TITLE       = "Concert";
    private static final String DATE        = "2025-12-01";
    private static final String LOCATION    = "Montreal";
    private static final String CATEGORY    = "Music";
    private static final String DESCRIPTION = "A great show";
    private static final String ACTIVE      = "active";
    private static final String CANCELLED   = "cancelled";

    @Test
    @DisplayName("Parameterised constructor sets all fields correctly")
    void eventConstructorSetsAllFields() {
        Event event = new Event(ID, TITLE, DATE, LOCATION, CATEGORY, DESCRIPTION);

        assertAll("All fields must match constructor arguments",
                () -> assertEquals(ID,          event.getId(),          "id mismatch"),
                () -> assertEquals(TITLE,       event.getTitle(),       "title mismatch"),
                () -> assertEquals(DATE,        event.getDate(),        "date mismatch"),
                () -> assertEquals(LOCATION,    event.getLocation(),    "location mismatch"),
                () -> assertEquals(CATEGORY,    event.getCategory(),    "category mismatch"),
                () -> assertEquals(DESCRIPTION, event.getDescription(), "description mismatch")
        );
    }

    @Test
    @DisplayName("Parameterised constructor sets status to 'active' by default")
    void parameterisedConstructorSetsStatusToActive() {
        Event event = new Event(ID, TITLE, DATE, LOCATION, CATEGORY, DESCRIPTION);

        assertEquals(ACTIVE, event.getStatus(),
                "Status must be 'active' when using the parameterised constructor");
    }

    @Test
    @DisplayName("Parameterised constructor sets status to 'active' even when id is null")
    void newEventStatusIsActiveByDefaultWhenIdIsNull() {
        Event event = new Event(null, TITLE, DATE, LOCATION, CATEGORY, DESCRIPTION);

        assertEquals(ACTIVE, event.getStatus(),
                "Status must be 'active' regardless of id value");
    }


    @Test
    @DisplayName("Default constructor leaves all fields null")
    void defaultConstructorCreatesEventWithAllNullFields() {
        Event event = new Event();

        assertAll("All fields must be null after no-arg construction",
                () -> assertNull(event.getId(),          "id should be null"),
                () -> assertNull(event.getTitle(),       "title should be null"),
                () -> assertNull(event.getDate(),        "date should be null"),
                () -> assertNull(event.getLocation(),    "location should be null"),
                () -> assertNull(event.getCategory(),    "category should be null"),
                () -> assertNull(event.getDescription(), "description should be null"),
                () -> assertNull(event.getStatus(),      "status should be null")
        );
    }


    @Test
    @DisplayName("Setters update every field independently")
    void settersUpdateAllFields() {
        Event event = new Event();

        event.setId(ID);
        event.setTitle(TITLE);
        event.setDate(DATE);
        event.setLocation(LOCATION);
        event.setCategory(CATEGORY);
        event.setDescription(DESCRIPTION);
        event.setStatus(ACTIVE);

        assertAll("Each setter must persist its value",
                () -> assertEquals(ID,          event.getId(),          "setId failed"),
                () -> assertEquals(TITLE,       event.getTitle(),       "setTitle failed"),
                () -> assertEquals(DATE,        event.getDate(),        "setDate failed"),
                () -> assertEquals(LOCATION,    event.getLocation(),    "setLocation failed"),
                () -> assertEquals(CATEGORY,    event.getCategory(),    "setCategory failed"),
                () -> assertEquals(DESCRIPTION, event.getDescription(), "setDescription failed"),
                () -> assertEquals(ACTIVE,      event.getStatus(),      "setStatus failed")
        );
    }

    @Test
    @DisplayName("Status can be changed from 'active' to 'cancelled'")
    void statusCanBeSetToCancelled() {
        Event event = new Event(ID, TITLE, DATE, LOCATION, CATEGORY, DESCRIPTION);

        event.setStatus(CANCELLED);

        assertEquals(CANCELLED, event.getStatus(),
                "setStatus('cancelled') must update the status field");
    }

    @Test
    @DisplayName("Status can be reset back to 'active' after being cancelled")
    void statusCanBeResetToActive() {
        Event event = new Event(ID, TITLE, DATE, LOCATION, CATEGORY, DESCRIPTION);
        event.setStatus(CANCELLED);

        event.setStatus(ACTIVE);

        assertEquals(ACTIVE, event.getStatus(),
                "Status should be settable back to 'active'");
    }

    @Test
    @DisplayName("setStatus via default constructor then set to 'active'")
    void defaultConstructorEventCanHaveStatusSetToActive() {
        Event event = new Event();

        event.setStatus(ACTIVE);

        assertEquals(ACTIVE, event.getStatus(),
                "A default-constructed event should accept 'active' status");
    }
}