package com.example.haboob;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link LotterySampler}.
 *
 * <p>Important: {@link Event} methods like
 * {@link Event#addEntrantToInvitedEntrants(String)} and the various
 * {@code removeEntrantFrom*} normally touch Firestore. Local JVM tests
 * don’t have Android/Firebase; to avoid NPEs, we use a {@code FakeEvent}
 * that overrides those methods to mutate in-memory lists only.</p>
 */
public class LotterySamplerTest {

    private LotterySampler sampler;
    private FakeEvent event; // in-memory test double

    /**
     * Test-double for Event that never touches Firestore.
     * It only mutates the local lists via getters.
     */
    private static class FakeEvent extends Event {
        FakeEvent() { super(true); }

        @Override
        public void addEntrantToInvitedEntrants(String userID) {
            if (userID == null) return;
            if (!getInvitedEntrants().contains(userID)) {
                getInvitedEntrants().add(userID);
            }
        }

        @Override
        public void removeEntrantFromWaitingEntrants(String userID) {
            if (userID == null) return;
            getWaitingEntrants().remove(userID);
        }

        @Override
        public void removeEntrantFromInvitedEntrants(String userID) {
            if (userID == null) return;
            getInvitedEntrants().remove(userID);
        }

        @Override
        public void removeEntrantFromEnrolledEntrants(String userID) {
            if (userID == null) return;
            getEnrolledEntrants().remove(userID);
        }

        @Override
        public void addEntrantToCancelledEntrants(String userID) {
            if (userID == null) return;
            if (!getCancelledEntrants().contains(userID)) {
                getCancelledEntrants().add(userID);
            }
        }
    }

    @Before
    public void setUp() {
        sampler = new LotterySampler();
        event = new FakeEvent();

        // Seed the waiting entrants list
        ArrayList<String> entrants = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            entrants.add("user" + i);
        }

        event.setWaitingEntrants(entrants);
        event.setInvitedEntrantsList(new ArrayList<>());
        event.setLotterySampleSize(3);
    }

    // ---------- sampleEntrants tests (no Firestore) ----------

    @Test
    public void sampleEntrants_returnsRandomSubsetWithinBounds() {
        List<String> entrants = List.of("a", "b", "c", "d");
        List<String> sampled = sampler.sampleEntrants(entrants, 2);

        assertEquals(2, sampled.size());
        assertTrue(entrants.containsAll(sampled));
    }

    @Test
    public void sampleEntrants_handlesEmptyOrNullInput() {
        assertTrue(sampler.sampleEntrants(null, 3).isEmpty());
        assertTrue(sampler.sampleEntrants(new ArrayList<>(), 3).isEmpty());
    }

    @Test
    public void sampleEntrants_handlesNonPositiveSampleSize() {
        List<String> entrants = List.of("x", "y", "z");
        assertTrue(sampler.sampleEntrants(entrants, 0).isEmpty());
        assertTrue(sampler.sampleEntrants(entrants, -1).isEmpty());
    }

    // ---------- performLottery tests (use FakeEvent) ----------

    @Test(expected = IllegalArgumentException.class)
    public void performLottery_throwsIfEventIsNull() {
        sampler.performLottery(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void performLottery_throwsIfNoEntrants() {
        event.setWaitingEntrants(new ArrayList<>());
        sampler.performLottery(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void performLottery_throwsIfInvalidSampleSize() {
        event.setLotterySampleSize(0);
        sampler.performLottery(event);
    }

    @Test
    public void performLottery_movesSampledEntrants() {
        sampler.performLottery(event);

        // Invited should contain up to 3 entrants
        assertEquals(3, event.getInvitedEntrants().size());

        // Waiting list should shrink accordingly
        assertEquals(2, event.getWaitingEntrants().size());

        // No overlap between invited and waiting
        for (String id : event.getInvitedEntrants()) {
            assertFalse(event.getWaitingEntrants().contains(id));
        }
    }

    // ---------- fillVacancyFromWaitlist tests (use FakeEvent) ----------

    @Test(expected = IllegalArgumentException.class)
    public void fillVacancyFromWaitlist_throwsIfEventNull() {
        sampler.fillVacancyFromWaitlist(null);
    }

    @Test
    public void fillVacancyFromWaitlist_returnsNullIfEmpty() {
        event.setWaitingEntrants(new ArrayList<>());
        String result = sampler.fillVacancyFromWaitlist(event);
        assertNull(result);
    }

    @Test
    public void fillVacancyFromWaitlist_selectsAndMovesOne() {
        String selected = sampler.fillVacancyFromWaitlist(event);

        assertNotNull(selected);
        assertTrue(event.getInvitedEntrants().contains(selected));
        assertFalse(event.getWaitingEntrants().contains(selected));
    }
}
