package com.example.haboob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* Author: Dan
* This class is almost like a helper class for Event which will handle the lottery
* logic for an event
*/
public class LotterySampler {

    /**
     * Performs lottery sampling on an event's entrants list.
     * Randomly selects entrants up to the event's lottery sample size and moves them to invitedEntrants.
     * Remaining entrants are moved to waitingEntrants.
     *
     * @param event The event to perform lottery sampling on
     * @throws IllegalArgumentException if event is null or has no entrants
     */
    public void performLottery(Event event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        ArrayList<String> entrants = event.getWaitingEntrants();
        if (entrants == null || entrants.isEmpty()) {
            throw new IllegalArgumentException("Event has no entrants to sample from");
        }

        // Get the event capacity which is the number of entrants to sample
        int sampleSize = event.getLotterySampleSize() - (event.getInvitedEntrants().size() + event.getEnrolledEntrants().size());
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Lottery sample size must be greater than 0");
        }

        // Select the first numToSelect entrants after shuffling
        List<String> selectedEntrants = sampleEntrants(entrants, sampleSize);

        // Add selected entrants to invitedEntrants
        for (String entrantId : selectedEntrants) {
            // Move from waiting to invited
            event.addEntrantToInvitedEntrants(entrantId);
            event.removeEntrantFromWaitingEntrants(entrantId);
            // TODO: Send a notification saying they were picked
        }

    }

    /**
     * Performs lottery sampling and returns the selected entrant IDs without modifying the event.
     * Useful for preview or testing purposes.
     *
     * @param entrants List of entrant IDs to sample from
     * @param sampleSize Number of entrants to select
     * @return List of randomly selected entrant IDs
     */
    public List<String> sampleEntrants(List<String> entrants, int sampleSize) {
        if (entrants == null || entrants.isEmpty()) {
            return new ArrayList<>();
        }

        if (sampleSize <= 0) {
            return new ArrayList<>();
        }

        List<String> shuffledEntrants = new ArrayList<>(entrants);
        Collections.shuffle(shuffledEntrants);

        int numToSelect = Math.min(sampleSize, shuffledEntrants.size());
        return new ArrayList<>(shuffledEntrants.subList(0, numToSelect));
    }

    /**
     * Fills a vacancy by selecting one entrant from the waiting list.
     * Called when an invited entrant cancels their invitation.
     *
     * @param event The event to fill a vacancy for
     * @return The ID of the newly selected entrant, or null if no one is waiting
     * @throws IllegalArgumentException if event is null
     */
    public String fillVacancyFromWaitlist(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        ArrayList<String> waitingEntrants = event.getWaitingEntrants();
        if (waitingEntrants == null || waitingEntrants.isEmpty()) {
            return null; // No one waiting
        }

        // Randomly select one entrant from the waiting list
        List<String> selected = sampleEntrants(waitingEntrants, 1);

        if (!selected.isEmpty()) {
            String selectedEntrantId = selected.get(0);

            // Move from waiting to invited
            event.addEntrantToInvitedEntrants(selectedEntrantId);
            event.removeEntrantFromWaitingEntrants(selectedEntrantId);
            // TODO: Send notification to the newly selected entrant

            return selectedEntrantId;
        }

        return null;
    }
}
