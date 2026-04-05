package com.soen345.Ticket.Reservation.Application.service;

import com.soen345.Ticket.Reservation.Application.model.Event;
import com.soen345.Ticket.Reservation.Application.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    private static final String ADMIN_ID  = "admin-123";
    private static final String CLIENT_ID = "client-456";

    private Event buildEvent(String title, String date, String location,
                             String category, String description) {
        Event e = new Event();
        e.setTitle(title);
        e.setDate(date);
        e.setLocation(location);
        e.setCategory(category);
        e.setDescription(description);
        e.setStatus("active");
        return e;
    }

    @BeforeEach
    void setUp() {
        // Admin check: ADMIN_ID → true, CLIENT_ID → false
        lenient().when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        lenient().when(userService.isAdmin(CLIENT_ID)).thenReturn(false);
    }

    /* ── createEvent ─────────────────────────────────────────── */

    @Test
    @DisplayName("createEvent assigns a non-null UUID id")
    void createEvent_assignsId() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show");
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event saved = eventService.createEvent(event, ADMIN_ID);

        assertNotNull(saved.getId(), "Created event must have a non-null id");
    }

    @Test
    @DisplayName("createEvent generates unique ids for different events")
    void createEvent_idsAreUnique() {
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event first  = eventService.createEvent(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"), ADMIN_ID);
        Event second = eventService.createEvent(
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2"), ADMIN_ID);

        assertNotEquals(first.getId(), second.getId(), "Each event must receive a unique id");
    }

    @Test
    @DisplayName("createEvent throws when caller is not an admin")
    void createEvent_throwsWhenNotAdmin() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "A great show");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.createEvent(event, CLIENT_ID));

        assertTrue(ex.getMessage().contains("admin"),
                "Exception message must mention 'admin'");
        verify(eventRepository, never()).save(any());
    }

    /* ── editEvent ───────────────────────────────────────────── */

    @Test
    @DisplayName("editEvent updates all mutable fields")
    void editEvent_updatesAllFields() {
        Event existing = buildEvent("Old Title", "2025-01-01", "Old City", "Old Cat", "Old Desc");
        existing.setId("event-1");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event updates = buildEvent("New Title", "2026-06-15", "New City", "New Cat", "New Desc");
        Event result  = eventService.editEvent("event-1", updates, ADMIN_ID);

        assertAll("Every editable field must reflect the update",
                () -> assertEquals("New Title",  result.getTitle(),       "title not updated"),
                () -> assertEquals("2026-06-15", result.getDate(),        "date not updated"),
                () -> assertEquals("New City",   result.getLocation(),    "location not updated"),
                () -> assertEquals("New Cat",    result.getCategory(),    "category not updated"),
                () -> assertEquals("New Desc",   result.getDescription(), "description not updated")
        );
    }

    @Test
    @DisplayName("editEvent throws when event does not exist")
    void editEvent_throwsWhenEventNotFound() {
        when(eventRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.editEvent("nonexistent-id",
                        buildEvent("T", "2025-01-01", "C", "C", "D"), ADMIN_ID));

        assertTrue(ex.getMessage().contains("Event not found"),
                "Exception message must mention 'Event not found'");
    }

    @Test
    @DisplayName("editEvent throws when event is already cancelled")
    void editEvent_throwsWhenEventIsCancelled() {
        Event cancelled = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show");
        cancelled.setId("event-1");
        cancelled.setStatus("cancelled");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(cancelled));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.editEvent("event-1",
                        buildEvent("New", "2026-01-01", "City", "Cat", "Desc"), ADMIN_ID));

        assertTrue(ex.getMessage().contains("Cannot edit a cancelled event"),
                "Exception message must mention 'Cannot edit a cancelled event'");
    }

    @Test
    @DisplayName("editEvent throws when caller is not an admin")
    void editEvent_throwsWhenNotAdmin() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.editEvent("event-1",
                        buildEvent("T", "2025-01-01", "C", "C", "D"), CLIENT_ID));

        assertTrue(ex.getMessage().contains("admin"));
        verify(eventRepository, never()).findById(any());
    }

    /* ── cancelEvent ─────────────────────────────────────────── */

    @Test
    @DisplayName("cancelEvent sets event status to 'cancelled'")
    void cancelEvent_setsStatusToCancelled() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show");
        event.setId("event-1");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.cancelEvent("event-1", ADMIN_ID);

        assertEquals("cancelled", result.getStatus(),
                "Status must be 'cancelled' after cancellation");
    }

    @Test
    @DisplayName("cancelEvent throws when event does not exist")
    void cancelEvent_throwsWhenEventNotFound() {
        when(eventRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.cancelEvent("nonexistent-id", ADMIN_ID));

        assertTrue(ex.getMessage().contains("Event not found"));
    }

    @Test
    @DisplayName("cancelEvent throws when event is already cancelled")
    void cancelEvent_throwsWhenAlreadyCancelled() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show");
        event.setId("event-1");
        event.setStatus("cancelled");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.cancelEvent("event-1", ADMIN_ID));

        assertTrue(ex.getMessage().contains("Event is already cancelled"));
    }

    @Test
    @DisplayName("cancelEvent throws when caller is not an admin")
    void cancelEvent_throwsWhenNotAdmin() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.cancelEvent("event-1", CLIENT_ID));

        assertTrue(ex.getMessage().contains("admin"));
        verify(eventRepository, never()).findById(any());
    }

    /* ── getAllEvents ─────────────────────────────────────────── */

    @Test
    @DisplayName("getAllEvents returns all events from repository")
    void getAllEvents_returnsAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> events = eventService.getAllEvents();

        assertEquals(2, events.size(), "Must return exactly 2 events");
    }

    @Test
    @DisplayName("getAllEvents returns empty list when no events exist")
    void getAllEvents_returnsEmptyList() {
        when(eventRepository.findAll()).thenReturn(List.of());

        List<Event> events = eventService.getAllEvents();

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    /* ── getEventById ────────────────────────────────────────── */

    @Test
    @DisplayName("getEventById returns the correct event")
    void getEventById_returnsCorrectEvent() {
        Event event = buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show");
        event.setId("event-1");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));

        Event fetched = eventService.getEventById("event-1");

        assertEquals("event-1", fetched.getId());
        assertEquals("Concert", fetched.getTitle());
    }

    @Test
    @DisplayName("getEventById throws when event does not exist")
    void getEventById_throwsWhenNotFound() {
        when(eventRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.getEventById("nonexistent-id"));

        assertTrue(ex.getMessage().contains("Event not found"));
    }

    /* ── searchEvents ────────────────────────────────────────── */

    @Test
    @DisplayName("searchEvents returns all active events when no filters applied")
    void searchEvents_noFilters_returnsAllActive() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> results = eventService.searchEvents(null, null, null, null);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("searchEvents filters by category")
    void searchEvents_filterByCategory() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> results = eventService.searchEvents(null, null, "Music", null);

        assertEquals(1, results.size());
        assertEquals("Concert", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchEvents filters by location")
    void searchEvents_filterByLocation() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> results = eventService.searchEvents(null, "Toronto", null, null);

        assertEquals(1, results.size());
        assertEquals("Game", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchEvents filters by date")
    void searchEvents_filterByDate() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> results = eventService.searchEvents("2025-12-01", null, null, null);

        assertEquals(1, results.size());
        assertEquals("Concert", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchEvents filters by title")
    void searchEvents_filterByTitle() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-02", "Toronto",  "Sports", "Show 2")
        ));

        List<Event> results = eventService.searchEvents(null, null, null, "Game");

        assertEquals(1, results.size());
        assertEquals("Game", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchEvents filters by all three fields combined")
    void searchEvents_filterByAllFields() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music",  "Show 1"),
                buildEvent("Game",    "2025-12-01", "Montreal", "Sports", "Show 2"),
                buildEvent("Trip",    "2025-12-02", "Toronto",  "Travel", "Show 3")
        ));

        List<Event> results = eventService.searchEvents("2025-12-01", "Montreal", "Music", "Concert");

        assertEquals(1, results.size());
        assertEquals("Concert", results.get(0).getTitle());
    }

    @Test
    @DisplayName("searchEvents returns empty list when no events match filters")
    void searchEvents_noMatches_returnsEmpty() {
        when(eventRepository.findAllActive()).thenReturn(List.of(
                buildEvent("Concert", "2025-12-01", "Montreal", "Music", "Show 1")
        ));

        List<Event> results = eventService.searchEvents(null, "Vancouver", null, null);

        assertTrue(results.isEmpty());
    }
}