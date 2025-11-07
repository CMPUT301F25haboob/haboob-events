package com.example.haboob;

import java.util.ArrayList;
import java.util.HashMap;

public class OrganizerExpandableListsData {

    // Create getter function for expandable lists
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
