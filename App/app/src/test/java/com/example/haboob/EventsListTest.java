package com.example.haboob;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link EventsList} logic that can be executed without Firestore or Android.
 * <p>
 * This test suite validates all in-memory behaviors of {@code EventsList}, including:
 * <ul>
 *   <li>Finding events by ID</li>
 *   <li>Filtering events by tags (case-insensitive)</li>
 *   <li>Retrieving events by organizer</li>
 *   <li>Finding waitlist membership by entrant</li>
 *   <li>Determining which events are still "live"</li>
 * </ul>
 * <p>
 * The tests use the in-memory constructor {@code new EventsList(true)} to avoid
 * Firebase initialization, and mock {@link Event} objects created directly with setters.
 * <p>
 * Place this file under:
 * <pre>src/test/java/com/example/haboob/EventsListTest.java</pre>
 */
public class EventsListTest {

    /** The in-memory instance of EventsList used for testing. */
    private EventsList eventsList;

    // Test fixtures
    /** Event with future end date, tags ["Music", "Outdoor"], organizer "orgA", waitlist ["u1"]. */
    private Event e1;
    /** Event with null end date (treated as live), tags ["workshop"], organizer "orgB", waitlist ["u2","u3"]. */
    private Event e2;
    /** Event with past end date, tags ["MUSIC","indoor"], organizer "orgA", waitlist []. */
    private Event e3;

    /**
     * Initializes a new in-memory {@link EventsList} and populates it
     * with three mock {@link Event} objects for testing.
     */
    @Before
    public void setUp() {
        // Use in-memory mode so we don't touch Firestore
        eventsList = new EventsList(true);

        // Build some dates: past (-1 day) and future (+1 day)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +1);
        Date future = cal.getTime();
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date past = cal.getTime();

        // Create events (assumes Event has no-arg ctor and relevant setters)
        e1 = makeEvent(
                "E1",
                arrayList("Music", "Outdoor"),
                "orgA",
                arrayList("u1"),
                future
        );

        e2 = makeEvent(
                "E2",
                arrayList("workshop"),
                "orgB",
                arrayList("u2", "u3"),
                null // no end date => considered live
        );

        e3 = makeEvent(
                "E3",
                arrayList("MUSIC", "indoor"),
                "orgA",
                new ArrayList<>(),
                past
        );

        // Seed internal list via the exposed getter (returns backing list)
        eventsList.getEventsList().add(e1);
        eventsList.getEventsList().add(e2);
        eventsList.getEventsList().add(e3);
    }

    /**
     * Verifies that {@link EventsList#getEventByID(String)} successfully retrieves
     * an event when the ID exists in the list.
     */
    @Test
    public void testGetEventByID_found() {
        Event found = eventsList.getEventByID("E2");
        assertNotNull(found);
        assertEquals("E2", found.getEventID());
    }

    /**
     * Verifies that {@link EventsList#getEventByID(String)} returns {@code null}
     * for unknown or null event IDs.
     */
    @Test
    public void testGetEventByID_notFound() {
        assertNull(eventsList.getEventByID("NOPE"));
        assertNull(eventsList.getEventByID(null));
    }

    /**
     * Tests {@link EventsList#filterEvents(java.util.List)} for:
     * <ul>
     *   <li>Case-insensitive tag matching</li>
     *   <li>Requirement that all tags in the input are present in an event</li>
     *   <li>Handling of empty or null input (returns copy of full list)</li>
     * </ul>
     */
    @Test
    public void testFilterEvents_caseInsensitive_andContainsAll() {
        // Should match e1 (Music + Outdoor), but not e3 (no "Outdoor")
        var result1 = eventsList.filterEvents(arrayList("music", "outdoor"));
        assertEquals(1, result1.size());
        assertEquals("E1", result1.get(0).getEventID());

        // Single tag "music" should match e1 and e3 (case-insensitive)
        var result2 = eventsList.filterEvents(arrayList("MuSiC"));
        assertEquals(2, result2.size());
        assertTrue(containsEventIds(result2, "E1", "E3"));

        // Empty or null tags => copy of all events (not the same instance)
        var result3 = eventsList.filterEvents(new ArrayList<>());
        assertEquals(3, result3.size());
        assertNotSame(eventsList.getEventsList(), result3);

        var result4 = eventsList.filterEvents(null);
        assertEquals(3, result4.size());
    }

    /**
     * Tests {@link EventsList#getOrganizerEvents(String)} to ensure it returns
     * only the events belonging to the specified organizer.
     * <p>Also validates behavior for null and empty IDs.</p>
     */
    @Test
    public void testGetOrganizerEvents() {
        var orgAEvents = eventsList.getOrganizerEvents("orgA");
        assertEquals(2, orgAEvents.size());
        assertTrue(containsEventIds(orgAEvents, "E1", "E3"));

        var orgBEvents = eventsList.getOrganizerEvents("orgB");
        assertEquals(1, orgBEvents.size());
        assertEquals("E2", orgBEvents.get(0).getEventID());

        var none = eventsList.getOrganizerEvents("orgZ");
        assertTrue(none.isEmpty());

        var empty = eventsList.getOrganizerEvents("");
        assertTrue(empty.isEmpty());

        var nullRes = eventsList.getOrganizerEvents(null);
        assertTrue(nullRes.isEmpty());
    }

    /**
     * Tests {@link EventsList#getEntrantWaitlistEvents(String)} to ensure
     * that it correctly identifies events where a user appears on the waitlist.
     */
    @Test
    public void testGetEntrantWaitlistEvents() {
        var u1 = eventsList.getEntrantWaitlistEvents("u1");
        assertEquals(1, u1.size());
        assertEquals("E1", u1.get(0).getEventID());

        var u2 = eventsList.getEntrantWaitlistEvents("u2");
        assertEquals(1, u2.size());
        assertEquals("E2", u2.get(0).getEventID());

        var none = eventsList.getEntrantWaitlistEvents("nobody");
        assertTrue(none.isEmpty());

        var nullId = eventsList.getEntrantWaitlistEvents(null);
        assertTrue(nullId.isEmpty());
    }

    /**
     * Tests {@link EventsList#getLiveEvents()} to confirm that:
     * <ul>
     *   <li>Events with null end date are treated as live</li>
     *   <li>Events with future end date are live</li>
     *   <li>Events with past end date are not included</li>
     * </ul>
     */
    @Test
    public void testGetLiveEvents_nullOrFutureEndDateAreLive() {
        var live = eventsList.getLiveEvents();
        // e1 (future) and e2 (null) are live; e3 is past -> not live
        assertEquals(2, live.size());
        assertTrue(containsEventIds(live, "E1", "E2"));
    }

    // -------------------- Helper methods --------------------

    /**
     * Convenience method to build an {@link ArrayList} from varargs.
     */
    private static ArrayList<String> arrayList(String... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    /**
     * Helper to assert that a given list of events contains all the provided event IDs.
     *
     * @param list the list of events to check
     * @param ids  the event IDs expected to appear
     * @return true if all IDs exist in the event list, false otherwise
     */
    private static boolean containsEventIds(ArrayList<Event> list, String... ids) {
        var set = new java.util.HashSet<String>();
        for (Event e : list) set.add(e.getEventID());
        for (String id : ids) {
            if (!set.contains(id)) return false;
        }
        return true;
    }

    /**
     * Builds a minimal {@link Event} instance for unit testing with specific IDs,
     * tags, organizer, waiting list, and end date.
     *
     * @param id event ID
     * @param tags list of tags
     * @param organizerId organizer ID
     * @param waitingEntrants list of entrant IDs on waitlist
     * @param registrationEndDate end date for registration (may be null)
     * @return constructed Event object
     */
    private static Event makeEvent(String id,
                                   ArrayList<String> tags,
                                   String organizerId,
                                   ArrayList<String> waitingEntrants,
                                   Date registrationEndDate) {
        Event e = new Event(true); // no Firebase in unit tests
        e.setEventID(id);
        e.setTags(tags);
        e.setOrganizer(organizerId);
        e.setWaitingEntrants(waitingEntrants);
        e.setRegistrationEndDate(registrationEndDate);
        return e;
    }
}
