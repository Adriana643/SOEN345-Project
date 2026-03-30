package com.soen345.Ticket.Reservation.Application.service;

import com.soen345.Ticket.Reservation.Application.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("EventService Tests")
class EventServiceTest {

    private EventService eventService;

    private Event buildEvent(String title, String date, String location,
                             String category, String description) {
        Event e = new Event();
        e.setTitle(title);
        e.setDate(date);
        e.setLocation(location);
        e.setCategory(category);
        e.setDescription(description);
        return e;
    }

    @BeforeEach
    void setUp() {
        // Fresh service (and fresh in-memory store) before every test
        eventService = new EventService();
    }

    @Test
    @DisplayName("createEvent assigns an auto-generated id")
    void createEvent_assignsId() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show");

        Event saved = eventService.createEvent(event);

        assertNotNull(saved.getId(), "Created event must have a non-null id");
    }

    @Test
    @DisplayName("createEvent stores the event so it can be retrieved afterwards")
    void createEvent_eventIsRetrievableAfterCreation() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show");

        Event saved   = eventService.createEvent(event);
        Event fetched = eventService.getEventById(saved.getId());

        assertEquals(saved.getId(), fetched.getId(), "Fetched event must match the saved one");
    }

    @Test
    @DisplayName("createEvent increments id for each new event")
    void createEvent_idsAreUnique() {
        Event first  = eventService.createEvent(buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show 1"));
        Event second = eventService.createEvent(buildEvent("Game",    "2025-12-02", "Toronto",  "Sports","Show 2"));

        assertNotEquals(first.getId(), second.getId(), "Each event must receive a unique id");
    }

    @Test
    @DisplayName("editEvent updates all mutable fields")
    void editEvent_updatesAllFields() {
        Event saved   = eventService.createEvent(
                buildEvent("Old Title", "2025-01-01", "Old City", "Old Cat", "Old Desc"));
        Event updates = buildEvent("New Title", "2026-06-15", "New City", "New Cat", "New Desc");

        Event result = eventService.editEvent(saved.getId(), updates);

        assertAll("Every editable field must reflect the update",
                () -> assertEquals("New Title", result.getTitle(),       "title not updated"),
                () -> assertEquals("2026-06-15", result.getDate(),       "date not updated"),
                () -> assertEquals("New City",  result.getLocation(),    "location not updated"),
                () -> assertEquals("New Cat",   result.getCategory(),    "category not updated"),
                () -> assertEquals("New Desc",  result.getDescription(), "description not updated")
        );
    }

    @Test
    @DisplayName("editEvent throws RuntimeException when event does not exist")
    void editEvent_throwsWhenEventNotFound() {
        Event updates = buildEvent("Title", "2025-01-01", "City", "Cat", "Desc");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.editEvent("nonexistent-id", updates));

        assertTrue(ex.getMessage().contains("Event not found"),
                "Exception message must mention 'Event not found'");
    }

    @Test
    @DisplayName("editEvent throws RuntimeException when event is already cancelled")
    void editEvent_throwsWhenEventIsCancelled() {
        Event saved = eventService.createEvent(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show"));
        eventService.cancelEvent(saved.getId());

        Event updates = buildEvent("New Title", "2026-01-01", "City", "Cat", "Desc");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.editEvent(saved.getId(), updates));

        assertTrue(ex.getMessage().contains("Cannot edit a cancelled event"),
                "Exception message must mention 'Cannot edit a cancelled event'");
    }

    @Test
    @DisplayName("cancelEvent sets event status to 'cancelled'")
    void cancelEvent_setsStatusToCancelled() {
        Event saved = eventService.createEvent(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show"));

        Event cancelled = eventService.cancelEvent(saved.getId());

        assertEquals("cancelled", cancelled.getStatus(), "Status must be 'cancelled' after cancellation");
    }

    @Test
    @DisplayName("cancelEvent throws RuntimeException when event does not exist")
    void cancelEvent_throwsWhenEventNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.cancelEvent("nonexistent-id"));

        assertTrue(ex.getMessage().contains("Event not found"),
                "Exception message must mention 'Event not found'");
    }

    @Test
    @DisplayName("cancelEvent throws RuntimeException when event is already cancelled")
    void cancelEvent_throwsWhenAlreadyCancelled() {
        Event saved = eventService.createEvent(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show"));
        eventService.cancelEvent(saved.getId());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.cancelEvent(saved.getId()));

        assertTrue(ex.getMessage().contains("Event is already cancelled"),
                "Exception message must mention 'Event is already cancelled'");
    }

    @Test
    @DisplayName("getAllEvents returns empty list when no events exist")
    void getAllEvents_returnsEmptyListInitially() {
        List<Event> events = eventService.getAllEvents();

        assertNotNull(events, "Result list must not be null");
        assertTrue(events.isEmpty(), "List must be empty when no events have been created");
    }

    @Test
    @DisplayName("getAllEvents returns all created events")
    void getAllEvents_returnsAllCreatedEvents() {
        eventService.createEvent(buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"));
        eventService.createEvent(buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2"));
        eventService.createEvent(buildEvent("Trip",    "2025-12-03", "Ottawa",   "Travel", "Show 3"));

        List<Event> events = eventService.getAllEvents();

        assertEquals(3, events.size(), "getAllEvents must return exactly 3 events");
    }


    @Test
    @DisplayName("getEventById returns the correct event")
    void getEventById_returnsCorrectEvent() {
        Event saved = eventService.createEvent(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show"));

        Event fetched = eventService.getEventById(saved.getId());

        assertEquals(saved.getId(), fetched.getId(), "Fetched id must match saved id");
        assertEquals("Concert", fetched.getTitle(), "Fetched title must match saved title");
    }

    @Test
    @DisplayName("getEventById throws RuntimeException when event does not exist")
    void getEventById_throwsWhenEventNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.getEventById("nonexistent-id"));

        assertTrue(ex.getMessage().contains("Event not found"),
                "Exception message must mention 'Event not found'");
    }
}