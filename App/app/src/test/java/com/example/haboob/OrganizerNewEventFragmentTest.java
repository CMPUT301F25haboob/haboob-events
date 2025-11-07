package com.example.haboob;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link OrganizerNewEventFragment#createTagsList(String)}.
 * <p>
 * These tests validate the helper method used to transform a comma-separated string
 * of user-entered tags into a normalized list of lowercase, trimmed, non-empty values.
 * <p>
 * The logic is purely string-based and can be tested on the JVM with no Android dependencies.
 */
public class OrganizerNewEventFragmentTest {

    /**
     * Verifies that {@link OrganizerNewEventFragment#createTagsList(String)}
     * properly trims whitespace, lowercases all tags, and skips empty entries.
     */
    @Test
    public void createTagsList_trimsLowersAndSkipsEmpty() {
        OrganizerNewEventFragment f = new OrganizerNewEventFragment();

        ArrayList<String> tags = f.createTagsList("  Music ,  OUTDOOR, , workshop  ,  ");
        assertEquals(3, tags.size());
        assertEquals("music", tags.get(0));
        assertEquals("outdoor", tags.get(1));
        assertEquals("workshop", tags.get(2));
    }

    /**
     * Confirms that {@link OrganizerNewEventFragment#createTagsList(String)}
     * safely handles null or blank input by returning an empty list instead of crashing.
     */
    @Test
    public void createTagsList_handlesNullOrBlank() {
        OrganizerNewEventFragment f = new OrganizerNewEventFragment();

        assertTrue(f.createTagsList(null).isEmpty());
        assertTrue(f.createTagsList("   ").isEmpty());
    }
}

