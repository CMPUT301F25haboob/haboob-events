package com.example.haboob;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link OrganizerExpandableListsData}, which maps an {@link Event}'s
 * entrant lists into a {@link HashMap} for display in an {@link android.widget.ExpandableListView}.
 * <p>
 * These tests verify:
 * <ul>
 *   <li>All four list categories ("Waiting", "Invite", "Enrolled", "Cancelled") are included</li>
 *   <li>The mapping preserves references to the event’s lists (not copies)</li>
 *   <li>Empty lists are handled safely</li>
 * </ul>
 * <p>
 * This test class is located in <code>src/test/java/com/example/haboob/</code>
 * and runs entirely on the JVM (no Android or Firestore required).
 */
public class OrganizerExpandableListsDataTest {

    /**
     * Verifies that {@link OrganizerExpandableListsData#getListsToDisplay(Event)}
     * correctly populates all four lists with the expected keys and values.
     */
    @Test
    public void getListsToDisplay_mapsAllFourLists() {
        Event e = new Event(true); // In-memory, no Firebase
        e.setEventTitle("Title");
        e.setWaitingEntrants(new ArrayList<>(Arrays.asList("w1", "w2")));
        e.setInvitedEntrantsList(new ArrayList<>(Arrays.asList("i1")));
        e.setEnrolledEntrantsList(new ArrayList<>(Arrays.asList("en1", "en2", "en3")));
        e.setCancelledEntrantsList(new ArrayList<>(Arrays.asList("c1")));

        HashMap<String, ArrayList<String>> map = OrganizerExpandableListsData.getListsToDisplay(e);

        // Keys present
        assertTrue(map.containsKey("Waiting list"));
        assertTrue(map.containsKey("Invite list"));
        assertTrue(map.containsKey("Enrolled list"));
        assertTrue(map.containsKey("Cancelled list"));

        // Values correct
        assertEquals(Arrays.asList("w1", "w2"), map.get("Waiting list"));
        assertEquals(Arrays.asList("i1"), map.get("Invite list"));
        assertEquals(Arrays.asList("en1", "en2", "en3"), map.get("Enrolled list"));
        assertEquals(Arrays.asList("c1"), map.get("Cancelled list"));
    }

    /**
     * Ensures that {@link OrganizerExpandableListsData#getListsToDisplay(Event)}
     * handles empty lists safely and returns non-null empty collections for all categories.
     */
    @Test
    public void getListsToDisplay_handlesEmptyLists() {
        Event e = new Event(true);
        e.setWaitingEntrants(new ArrayList<>());
        e.setInvitedEntrantsList(new ArrayList<>());
        e.setEnrolledEntrantsList(new ArrayList<>());
        e.setCancelledEntrantsList(new ArrayList<>());

        HashMap<String, ArrayList<String>> map = OrganizerExpandableListsData.getListsToDisplay(e);

        assertNotNull(map.get("Waiting list"));
        assertNotNull(map.get("Invite list"));
        assertNotNull(map.get("Enrolled list"));
        assertNotNull(map.get("Cancelled list"));

        assertTrue(map.get("Waiting list").isEmpty());
        assertTrue(map.get("Invite list").isEmpty());
        assertTrue(map.get("Enrolled list").isEmpty());
        assertTrue(map.get("Cancelled list").isEmpty());
    }

    /**
     * Confirms that the returned map references the same underlying lists
     * as the {@link Event} object, allowing real-time updates to propagate to the UI.
     */
    @Test
    public void getListsToDisplay_returnsBackedLists_notCopies() {
        // Ensures adapter sees live mutations
        ArrayList<String> waiting = new ArrayList<>();
        Event e = new Event(true);
        e.setWaitingEntrants(waiting);
        e.setInvitedEntrantsList(new ArrayList<>());
        e.setEnrolledEntrantsList(new ArrayList<>());
        e.setCancelledEntrantsList(new ArrayList<>());

        HashMap<String, ArrayList<String>> map = OrganizerExpandableListsData.getListsToDisplay(e);

        // mutate original
        waiting.add("w1");
        assertEquals(1, map.get("Waiting list").size());
        assertEquals("w1", map.get("Waiting list").get(0));
    }
}
