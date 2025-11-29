package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@code EventsList} manages all {@link Event} objects in the Firestore "events" collection.
 * <p>
 * It provides methods to:
 * <ul>
 *     <li>Load events from Firestore into memory</li>
 *     <li>Add and delete events from Firestore</li>
 *     <li>Filter or query locally loaded events by various attributes</li>
 * </ul>
 * <p>
 * This class can be instantiated in two ways:
 * <ul>
 *     <li>Connected to Firestore (default constructor)</li>
 *     <li>In-memory only (for unit tests)</li>
 * </ul>
 * It also supports optional callbacks via {@link OnEventsLoadedListener} for asynchronous operations.
 */
public class EventsList {

    /** List containing all events currently loaded in memory. */
    private ArrayList<Event> eventsList;

    /** Reference to Firestore database. */
    private FirebaseFirestore db;

    /** Reference to the Firestore "events" collection. */
    private CollectionReference eventsListRef;

    /** Flag indicating whether the events list has finished loading from Firestore. */
    private boolean isLoaded = false;

    /**
     * Callback interface for asynchronous Firestore operations.
     */
    public interface OnEventsLoadedListener {
        /**
         * Called when the requested Firestore operation completes successfully.
         */
        void onEventsLoaded();

        /**
         * Called when the requested Firestore operation fails.
         *
         * @param e Exception thrown by Firestore
         */
        void onError(Exception e);
    }

    /**
     * Default constructor.
     * <p>
     * Initializes Firestore and automatically loads all events
     * from the "events" collection into {@link #eventsList}.
     */
    public EventsList() {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events");
        loadEventsList();
    }

    /**
     * Constructor that also accepts a listener for completion notifications.
     *
     * @param listener Listener called after Firestore load completes or fails
     */
    public EventsList(OnEventsLoadedListener listener) {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events");
        loadEventsList(listener);
    }

    /**
     * Constructor used for unit testing.
     * <p>
     * When {@code inMemoryOnly} is true, this instance operates without touching Firestore.
     *
     * @param inMemoryOnly true for local-only mode, false for Firestore-connected mode
     */
    public EventsList(boolean inMemoryOnly) {
        eventsList = new ArrayList<>();
        if (!inMemoryOnly) {
            db = FirebaseFirestore.getInstance();
            eventsListRef = db.collection("events");
        }
    }

    /**
     * Returns the current in-memory list of all events.
     *
     * @return ArrayList of {@link Event} objects
     */
    public ArrayList<Event> getEventsList() {
        return eventsList;
    }

    /**
     * Indicates whether the list of events has finished loading from Firestore.
     *
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Loads all events from the Firestore "events" collection into memory.
     * <p>
     * This operation is asynchronous; use {@link OnEventsLoadedListener} to be notified when complete.
     *
     * @param listener Optional callback to handle success or failure
     */
    public void loadEventsList(OnEventsLoadedListener listener) {
        eventsListRef.get()
                .addOnSuccessListener(snapshots -> {
                    eventsList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Event e = doc.toObject(Event.class);
                        eventsList.add(e);
                    }
                    isLoaded = true;
                    if (listener != null) listener.onEventsLoaded();
                    Log.d("EventsList", "Successfully loaded " + eventsList.size() + " events");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventsList", "Failed to load events", e);
                    if (listener != null) listener.onError(e);
                });
    }

    /**
     * Loads all events from Firestore without using a callback listener.
     */
    public void loadEventsList() {
        loadEventsList(null);
    }

    /**
     * Adds a new {@link Event} to Firestore and appends it to the local list upon success.
     * <p>
     * Firestore automatically generates a unique document ID, while the {@code eventID}
     * is managed internally by the {@link Event} class.
     *
     * @param e        The {@link Event} to add
     * @param listener Optional listener for asynchronous completion
     * @return The event's internal {@code eventID}
     */
    public String addEvent(Event e, OnEventsLoadedListener listener) {
        eventsListRef.add(e)
                .addOnSuccessListener(docRef -> {
                    eventsList.add(e);
                    Log.d("EventsList", "Added event with ID: " + e.getEventID());
                    if (listener != null) listener.onEventsLoaded();
                })
                .addOnFailureListener(ex -> {
                    Log.e("EventsList", "Failed to update event with ID", ex);
                    if (listener != null) listener.onError(ex);
                });
        return e.getEventID();
    }

    /**
     * Adds a new event without specifying a listener.
     *
     * @param e The event to add
     * @return The event's internal {@code eventID}
     */
    public String addEvent(Event e) {
        return addEvent(e, null);
    }

    /**
     * Deletes the specified event from Firestore and removes it from the local list.
     * <p>
     * The event is matched in Firestore by its {@code eventID} field.
     *
     * @param e        The {@link Event} to delete
     * @param listener Optional callback for completion
     * @throws IllegalStateException if {@link #eventsList} is empty
     */
    public void deleteEvent(Event e, OnEventsLoadedListener listener) {
        if (eventsList == null || eventsList.isEmpty()) {
            throw new IllegalStateException("Cannot delete from an empty events list");
        }
        if (e.getEventID() == null || e.getEventID().isEmpty()) {
            if (listener != null) listener.onError(new IllegalArgumentException("Event ID is missing"));
            return;
        }

        db.collection("events")
                .whereEqualTo("eventID", e.getEventID())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    eventsList.remove(e);
                    String docId = querySnapshot.getDocuments().get(0).getId();
                    db.collection("events").document(docId).delete();
                    Log.d("EventsList", "Event deleted from Firestore and local list");
                    if (listener != null) listener.onEventsLoaded();
                })
                .addOnFailureListener(e2 -> {
                    Log.e("EventsList", "Failed to delete event", e2);
                    if (listener != null) listener.onError(e2);
                });
    }

    /**
     * Deletes an event from Firestore without specifying a listener.
     *
     * @param e The event to delete
     */
    public void deleteEvent(Event e) {
        deleteEvent(e, null);
    }

    /**
     * Retrieves an {@link Event} from the in-memory list by its eventID.
     *
     * @param eventID Unique event identifier
     * @return Matching {@link Event}, or {@code null} if not found
     */
    public Event getEventByID(String eventID) {
        if (eventID == null) return null;
        for (Event e : eventsList) {
            if (e != null && eventID.equals(e.getEventID())) return e;
        }
        return null;
    }

    /**
     * Filters events by a list of tag strings.
     * <p>
     * Matching is case-insensitive, and all provided tags must be present
     * in the event’s tag list.
     *
     * @param tags List of tags to match; if null or empty, returns all events
     * @return Filtered list of {@link Event} objects
     */
    public ArrayList<Event> filterEvents(List<String> tags) {
        if (tags == null || tags.isEmpty()) return new ArrayList<>(eventsList);
        ArrayList<String> lowerTags = new ArrayList<>(tags.size());
        for (String t : tags) lowerTags.add(t.toLowerCase());

        ArrayList<Event> filtered = new ArrayList<>();
        for (Event e : eventsList) {
            if (e == null) continue;
            List<String> eTags = e.getTags();
            if (eTags == null) continue;
            ArrayList<String> eLower = new ArrayList<>(eTags.size());
            for (String t : eTags) eLower.add(t.toLowerCase());
            if (eLower.containsAll(lowerTags)) filtered.add(e);
        }
        return filtered;
    }

    /**
     * Returns all events created by a specific organizer.
     *
     * @param organizerID The organizer’s unique ID
     * @return List of events associated with that organizer
     */
    public ArrayList<Event> getOrganizerEvents(String organizerID) {
        ArrayList<Event> out = new ArrayList<>();
        if (organizerID == null || organizerID.isEmpty()) return out;
        for (Event e : eventsList) {
            if (e != null && organizerID.equals(e.getOrganizer())) out.add(e);
        }
        return out;
    }

    /**
     * Returns all events where a given entrant is currently on the waiting list.
     *
     * @param entrantID The entrant’s unique user ID
     * @return List of events for which this entrant is waitlisted
     */
    public ArrayList<Event> getEntrantWaitlistEvents(String entrantID) {
        ArrayList<Event> out = new ArrayList<>();
        if (entrantID == null) return out;
        for (Event e : eventsList) {
            List<String> waiting = (e != null) ? e.getWaitingEntrants() : null;
            if (waiting != null && waiting.contains(entrantID)) out.add(e);
        }
        return out;
    }
    
    /**
     * Return a list of events that the given entrant is waitlisted for
     *
     * @return List of currently enrolled events
     */
    public ArrayList<Event> getEntrantEnrolledEvents(String entrantID) {
        ArrayList<Event> EnrolledEventList = new ArrayList<>();

        Log.d("TAG", "EntrantID: " + entrantID);

        for (Event e: eventsList) {
            if (e.getEnrolledEntrants() == null) continue; // David: if the list is null, there's no event ids in it, so continue
            // If given entrants
            if (e.getEnrolledEntrants().contains(entrantID)) {
                EnrolledEventList.add(e);
            }
        }

        return EnrolledEventList;
    }

    // Return a list of events that aren't past their registration end date
    /**
     * Returns all “live” events — events that either have no registration end date
     * or whose registration end date is after the current date.
     *
     * @return List of currently active events
     */
    public ArrayList<Event> getLiveEvents() {
        ArrayList<Event> live = new ArrayList<>();
        Date now = new Date();
        for (Event e : eventsList) {
            if (e == null) continue;
            Date end = e.getRegistrationEndDate();
            if (end == null || end.after(now)) live.add(e);
        }
        return live;
    }

    // Functions to get the organizer's upcoming and current events
    public ArrayList<Event> getOrganizerCurrentEvents(ArrayList<Event> orgEventsList) {
        ArrayList<Event> current = new ArrayList<>();
        Date now = new Date();
        for (Event e : orgEventsList) {
            if (e == null) continue;
            Date end = e.getRegistrationEndDate();
            if (end == null || end.before(now)) current.add(e);
        }
        return current;
    }

    public ArrayList<Event> getOrganizerUpcomingEvents(ArrayList<Event> orgEventsList) {
        ArrayList<Event> upcoming = new ArrayList<>();
        Date now = new Date();
        for (Event e : orgEventsList) {
            if (e == null) continue;
            Date end = e.getRegistrationEndDate();
            if (end == null || end.after(now)) upcoming.add(e);
        }
        return upcoming;
    }
}
