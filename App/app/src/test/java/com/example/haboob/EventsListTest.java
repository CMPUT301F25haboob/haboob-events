package com.example.haboob;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class EventsListTest {
    private Event makeEvent(String id, String organizerId, ArrayList<String> tags) {
        Event e = new Event(
                organizerId, new Date(), new Date(),
                "Title " + id, "Desc " + id,
                false, 0, -1,
                null, null,
                tags
        );
        e.setEventID(id);
        return e;
    }

    @Test
    public void addEventTest() {
        EventsList eventsList = new EventsList(true);
        Event event = makeEvent("A1", "ORG1", new ArrayList<>(Arrays.asList("music")));

        eventsList.getEventsList().add(event);

        assertEquals(1, eventsList.getEventsList().size());
        assertTrue(eventsList.getEventsList().contains(event));
    }

    @Test
    public void deleteEventTest() {
        EventsList eventsList = new EventsList(true);
        Event event = makeEvent("A1", "ORG1", new ArrayList<>(Arrays.asList("music")));
        // Expect IllegalStateException when deleting from empty list
        assertThrows(IllegalStateException.class, () -> {
            eventsList.deleteEvent(event);
        });

        eventsList.getEventsList().add(event);
        eventsList.getEventsList().remove(event);
        assertTrue(eventsList.getEventsList().isEmpty());
    }
    @Test
    public void getEventByIDTest() {
        EventsList eventsList = new EventsList(true);
        eventsList.getEventsList().add(makeEvent("A1", "ORG1", new ArrayList<>(Arrays.asList("x"))) );

        assertNull(eventsList.getEventByID("ZZZ"));
    }

    @Test
    public void filterEventsTest() {
        EventsList eventsList = new EventsList(true);
        Event a = makeEvent("A1", "ORG1", new ArrayList<>(Arrays.asList("music", "free")));
        Event b = makeEvent("B2", "ORG2", new ArrayList<>(Arrays.asList("sports")));
        Event c = makeEvent("C3", "ORG3", new ArrayList<>(Arrays.asList("music", "free", "outdoor"))); // superset of tags
        Event d = makeEvent("D4", "ORG4", new ArrayList<>()); // null tags
        eventsList.getEventsList().add(a);
        eventsList.getEventsList().add(b);
        eventsList.getEventsList().add(c);
        eventsList.getEventsList().add(d);

        // Null returns all
        ArrayList<Event> filteredEventsNull = eventsList.filterEvents(null);
        assertEquals(4, filteredEventsNull.size());
        assertTrue(filteredEventsNull.containsAll(Arrays.asList(a, b, c, d)));

        // Empty returns all
        ArrayList<Event> filteredEventsEmpty = eventsList.filterEvents(new ArrayList<>());
        assertEquals(4, filteredEventsEmpty.size());
        assertTrue(filteredEventsEmpty.containsAll(Arrays.asList(a, b, c, d)));

        // Single tag "music" returns a and c only
        ArrayList<Event> filteredEventsTag = eventsList.filterEvents(new ArrayList<>(Arrays.asList("music")));
        assertEquals(2, filteredEventsTag.size());
        assertTrue(filteredEventsTag.contains(a));
        assertTrue(filteredEventsTag.contains(c));
        assertFalse(filteredEventsTag.contains(b));
        assertFalse(filteredEventsTag.contains(d));

        // must contain ALL tags "music" and "free" returns a and c
        ArrayList<Event> filteredAllTags = eventsList.filterEvents(new ArrayList<>(Arrays.asList("music", "free")));
        assertEquals(2, filteredAllTags.size());
        assertTrue(filteredAllTags.contains(a));
        assertTrue(filteredAllTags.contains(c));

        // tag not found returns empty list
        ArrayList<Event> filteredNotFound = eventsList.filterEvents(new ArrayList<>(Arrays.asList("watersports")));
        assertTrue(filteredNotFound.isEmpty());

        // Case sensitivity "MuSIC" should match
        ArrayList<Event> filteredCase = eventsList.filterEvents(new ArrayList<>(Arrays.asList("MuSIC")));
        assertEquals(2, filteredCase.size());
        assertTrue(filteredCase.contains(a));
        assertTrue(filteredCase.contains(c));

        // Empty source list behavior
        EventsList emptyList = new EventsList(true);
        ArrayList<Event> fromEmpty = emptyList.filterEvents(new ArrayList<>(Arrays.asList("music")));
        assertTrue(fromEmpty.isEmpty());
    }

    @Test
    public void getOrganizerEventsTest() {
        EventsList eventsList = new EventsList(true);

        // Create sample events
        Event e1 = makeEvent("A1", "ORG1", new ArrayList<>(Arrays.asList("music")));
        Event e2 = makeEvent("B2", "ORG2", new ArrayList<>(Arrays.asList("sports")));
        Event e3 = makeEvent("C3", "ORG1", new ArrayList<>(Arrays.asList("art")));
        eventsList.getEventsList().addAll(Arrays.asList(e1, e2, e3));

        // Organizer with matching ID
        ArrayList<Event> org1Events = eventsList.getOrganizerEvents("ORG1");
        assertEquals(2, org1Events.size());
        assertTrue(org1Events.contains(e1));
        assertTrue(org1Events.contains(e3));

        // Organizer with no matches
        ArrayList<Event> org3Events = eventsList.getOrganizerEvents("ORG3");
        assertTrue(org3Events.isEmpty());

        // Null or empty ID should return empty list
        assertTrue(eventsList.getOrganizerEvents(null).isEmpty());
        assertTrue(eventsList.getOrganizerEvents("").isEmpty());
    }
}