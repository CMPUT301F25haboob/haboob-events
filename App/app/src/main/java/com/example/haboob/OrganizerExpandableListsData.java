package com.example.haboob;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@code OrganizerExpandableListsData} is a utility class that prepares
 * entrant-related lists for display in an {@link android.widget.ExpandableListView}.
 * <p>
 * It converts an {@link Event}'s different entrant categories (waiting, invited,
 * enrolled, cancelled) into a {@link HashMap} suitable for use with
 * {@link OrganizerExpandableListsAdapter}.
 */
public class OrganizerExpandableListsData {

    /**
     * Builds a {@link HashMap} of entrant lists categorized by type, for the given {@link Event}.
     * <p>
     * The returned map contains the following keys:
     * <ul>
     *   <li>"Waiting list" – entrants waiting for an open slot</li>
     *   <li>"Invite list" – entrants invited to join</li>
     *   <li>"Enrolled list" – entrants who accepted invitations</li>
     *   <li>"Cancelled list" – entrants who withdrew or were removed</li>
     * </ul>
     *
     * @param currentEvent the {@link Event} whose entrant lists are to be displayed
     * @return a mapping from list title to corresponding entrant IDs or names
     */
    public static HashMap<String, ArrayList<String>> getListsToDisplay(Event currentEvent) {
        HashMap<String, ArrayList<String>> expandableLists = new HashMap<>();

        // Set all the different lists into the expandable lists
        ArrayList<String> waitList = currentEvent.getWaitingEntrants();
        ArrayList<String> inviteList = currentEvent.getInvitedEntrants();
        ArrayList<String> enrolledList = currentEvent.getEnrolledEntrants();
        ArrayList<String> cancelledList = currentEvent.getCancelledEntrants();

        // Set into hashmap to display
        expandableLists.put("Waiting list", waitList);
        expandableLists.put("Invite list", inviteList);
        expandableLists.put("Enrolled list", enrolledList);
        expandableLists.put("Cancelled list", cancelledList);

        // Return the hashmap
        return expandableLists;
    }
}
