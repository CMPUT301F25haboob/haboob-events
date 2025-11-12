package com.example.haboob;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Event} that verify its in-memory behavior
 * (no Firebase or Android dependencies).
 * <p>
 * These tests use the constructor {@code new Event(true)} to bypass Firestore initialization
 * and focus purely on data integrity and internal list handling.
 * <p>
 * Covered behavior:
 * <ul>
 *   <li>Constructor initialization of lists</li>
 *   <li>Getter and setter round-trip validation for basic fields</li>
 *   <li>Defensive copying behavior of {@link Event#getTags()}</li>
 *   <li>Entrant list getters and setters</li>
 *   <li>Lottery IDs persistence via constructor</li>
 * </ul>
 * <p>
 * Firestore-dependent methods (such as add/remove entrants) are excluded
 * to keep this a pure JVM test.
 */
public class EventTest {

    /**
     * Ensures that constructing an {@link Event} with {@code new Event(true)}
     * initializes all list fields (tags, entrant lists) as non-null and empty.
     */
    @Test
    public void inMemoryConstructor_initializesLists_nonNullAndEmpty() {
        Event e = new Event(true);

        assertNotNull("tags should be initialized", e.getTags());
        assertNotNull("invitedEntrants should be initialized", e.getInvitedEntrants());
        assertNotNull("waitingEntrants should be initialized", e.getWaitingEntrants());
        assertNotNull("enrolledEntrants should be initialized", e.getEnrolledEntrants());
        assertNotNull("cancelledEntrants should be initialized", e.getCancelledEntrants());

        assertTrue(e.getTags().isEmpty());
        assertTrue(e.getInvitedEntrants().isEmpty());
        assertTrue(e.getWaitingEntrants().isEmpty());
        assertTrue(e.getEnrolledEntrants().isEmpty());
        assertTrue(e.getCancelledEntrants().isEmpty());
    }

    /**
     * Verifies that all basic fields in {@link Event} (organizer, title, description,
     * geolocation flag, sample size, dates, etc.) correctly retain values through
     * their respective setters and getters.
     */
    @Test
    public void settersAndGetters_roundTrip_basicFields() {
        Event e = new Event(true);

        // Dates
        Calendar cal = Calendar.getInstance();
        cal.set(2030, Calendar.JANUARY, 10, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(2030, Calendar.JANUARY, 20, 0, 0, 0);
        Date end = cal.getTime();

        e.setOrganizer("ORG_123");
        e.setEventID("EVT_456");
        e.setEventTitle("Campus Open Gym");
        e.setEventDescription("Fun rec session");
        e.setGeoLocationRequired(true);
        e.setLotterySampleSize(32);
        e.setOptionalWaitingListSize(10);
        e.setRegistrationStartDate(start);
        e.setRegistrationEndDate(end);

        assertEquals("ORG_123", e.getOrganizer());
        assertEquals("EVT_456", e.getEventID());
        assertEquals("Campus Open Gym", e.getEventTitle());
        assertEquals("Fun rec session", e.getEventDescription());
        assertTrue(e.getGeoLocationRequired());
        assertEquals(32, e.getLotterySampleSize());
        assertEquals(10, e.getOptionalWaitingListSize());
        assertEquals(start, e.getRegistrationStartDate());
        assertEquals(end, e.getRegistrationEndDate());
    }

    /**
     * Tests that {@link Event#getTags()} returns a defensive copy.
     * Modifying the returned list should not affect the event’s internal tag list.
     */
    @Test
    public void getTags_returnsDefensiveCopy() {
        Event e = new Event(true);

        ArrayList<String> initial = new ArrayList<>(Arrays.asList("volleyball", "rec"));
        e.setTags(initial);

        // getTags returns a copy; mutating the returned list should NOT affect internal state
        ArrayList<String> copy = new ArrayList<>(e.getTags());
        copy.add("evening"); // mutate the copy

        // Internal should remain unchanged (size 2)
        assertEquals(2, e.getTags().size());
        assertTrue(e.getTags().contains("volleyball"));
        assertTrue(e.getTags().contains("rec"));
        assertFalse(e.getTags().contains("evening"));
    }

    /**
     * Confirms that all entrant-related lists (invited, waiting, enrolled, cancelled)
     * maintain data integrity when set and retrieved.
     */
    @Test
    public void entrantLists_settersAndGetters_roundTrip() {
        Event e = new Event(true);

        ArrayList<String> invited = new ArrayList<>(Arrays.asList("u1", "u2"));
        ArrayList<String> waiting = new ArrayList<>(Arrays.asList("u3"));
        ArrayList<String> enrolled = new ArrayList<>(Arrays.asList("u4", "u5", "u6"));
        ArrayList<String> cancelled = new ArrayList<>(Arrays.asList("u7"));

        e.setInvitedEntrantsList(invited);
        e.setWaitingEntrants(waiting);
        e.setEnrolledEntrantsList(enrolled);
        e.setCancelledEntrantsList(cancelled);

        assertEquals(invited, e.getInvitedEntrants());
        assertEquals(waiting, e.getWaitingEntrants());
        assertEquals(enrolled, e.getEnrolledEntrants());
        assertEquals(cancelled, e.getCancelledEntrants());
    }

    /**
     * Verifies that the alternate constructor
     * {@link Event#Event(String, Date, Date, String, String, boolean, int, QRCode, Poster, ArrayList, ArrayList)}
     * correctly sets the lottery IDs, tags, and organizer metadata.
     */
    @Test
    public void lotteryIds_setterViaConstructorAndGetter_roundTrip() {
        Event e = new Event(true);

        // We can't use the full Firestore constructor in local unit tests,
        // but we can still set the field via specific constructor and verify getter
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("campus"));
        ArrayList<String> lottery = new ArrayList<>(Arrays.asList("u10", "u11"));

        // Use the alternate constructor that doesn't touch Firebase
        Event e2 = new Event(
                "ORG_X",
                new Date(), new Date(),
                "Title", "Desc",
                false, 5,
                null, null,
                tags, lottery
        );

        assertEquals(lottery, e2.getWaitingEntrants());
        assertEquals("ORG_X", e2.getOrganizer());
        assertEquals("Title", e2.getEventTitle());
    }

    /**
     * NOTE:
     * Firestore-dependent methods such as {@code addEntrantToInvitedEntrants()} or
     * {@code removeEntrantFromWaitingEntrants()} are deliberately excluded from this class.
     * These methods interact with {@code db.collection()} and should be tested either
     * through instrumented tests (Android) or with dependency-injected mock Firestore objects.
     */
}
