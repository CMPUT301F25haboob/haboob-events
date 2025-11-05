package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/*
 * This class holds a list of all events and manages the events database on Firestore
 * getEventsList() Return a list of all events objects
 * loadEventsList() Loads events collection of events from Firestore into eventsList
 * addEvent(Event e) add an event object to Firestore and assign unique Firestore ID to the event
 * deleteEvent(Event e) delete and event from Firestore
 * getEventByID(String eventID)
 * filterEvents(ArrayList<String> tags) return a list of filtered events based of a list of tags
 * getOrganizerEvents(Organizer o) // Returns a list of events had by a specific Organizer ID
 */
public class EventsList  {
    private ArrayList<Event> eventsList;
    private FirebaseFirestore db;
    private CollectionReference eventsListRef;
    private boolean isLoaded = false;



    // Callback interface for async operations
    public interface OnEventsLoadedListener {
        void onEventsLoaded();
        void onError(Exception e);
    }


    // Constructor - automatically loads events on creation
    public EventsList() {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events");

        // Automatically load events when EventsList is created
        this.loadEventsList();
    }

    public EventsList(OnEventsLoadedListener listener) {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events");
        this.loadEventsList(listener);
    }

    // Constructor used for testers so tests don't interact with firestore
    public EventsList(boolean inMemoryOnly) {
        eventsList = new ArrayList<>();
        if (!inMemoryOnly) {
            db = FirebaseFirestore.getInstance();
            eventsListRef = db.collection("events");
        }
    }

    // Return a list of all events
    public ArrayList<Event> getEventsList() {
        return eventsList;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void loadEventsList(OnEventsLoadedListener listener) {
        eventsListRef.get()
                .addOnSuccessListener(snapshots -> {
                    eventsList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        eventsList.add(doc.toObject(Event.class));
                    }
                    isLoaded = true;
                    if (listener != null) {
                        listener.onEventsLoaded();
                    }
                    Log.d("EventsList", "Successfully loaded " + eventsList.size() + " events");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventsList", "Failed to load events", e);
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }

    public void loadEventsList() {
        loadEventsList(null);
    }

    // Add event to db and set events unique Firestore ID
    public String addEvent(Event e, OnEventsLoadedListener listener) {
        eventsListRef.add(e)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    e.setEventID(id);

                    db.collection("events").document(id).set(e)
                            .addOnSuccessListener(aVoid -> {
                                eventsList.add(e);
                                Log.d("EventsList", "Added event with ID: " + id);
                                if (listener != null) {
                                    listener.onEventsLoaded();
                                }
                            })
                            .addOnFailureListener(ex -> {
                                Log.e("EventsList", "Failed to update event with ID", ex);
                                if (listener != null) {
                                    listener.onError(ex);
                                }
                            });
                })
                .addOnFailureListener(ex -> {
                    Log.e("EventsList", "Failed to add event", ex);
                    if (listener != null) {
                        listener.onError(ex);
                    }
                });
        return e.getEventID();
    }

    // Backward compatible version
    public String addEvent(Event e) {
        return addEvent(e, null);
    }

    // Delete event from db using its unique Firestore ID
    public void deleteEvent(Event e, OnEventsLoadedListener listener) {
        // Check for empty list
        if (eventsList == null || eventsList.isEmpty()) {
            throw new IllegalStateException("Cannot delete from an empty events list");
        }

        // Check for null Firestore reference FOR TESTING
        if (eventsListRef == null) {
            eventsList.remove(e);
            Log.d("EventsList", "Deleted event locally (no Firestore)");
            if (listener != null) {
                listener.onEventsLoaded();
            }
            return;
        }

        if (e.getEventID() == null || e.getEventID().isEmpty()) {
            Log.w("EventsList", "Cannot delete event: missing ID");
            if (listener != null) {
                listener.onError(new IllegalArgumentException("Event ID is missing"));
            }
            return;
        }

        eventsListRef.document(e.getEventID()).delete()
                .addOnSuccessListener(aVoid -> {
                    eventsList.remove(e);
                    Log.d("EventsList", "Deleted event with ID: " + e.getEventID());
                    if (listener != null) {
                        listener.onEventsLoaded();
                    }
                })
                .addOnFailureListener(ex -> {
                    Log.e("EventsList", "Failed to delete event", ex);
                    if (listener != null) {
                        listener.onError(ex);
                    }
                });
    }

    public void deleteEvent(Event e) {
        deleteEvent(e, null);
    }

    // Find event by ID and return it
    public Event getEventByID(String eventID) {
        for (Event e: eventsList) {
            if (e.getEventID().equals(eventID)) {
                return e;
            }
        }
        return null;
    }

    // Return list of all events that have the same tag(s) as the input given
    public ArrayList<Event> filterEvents(List<String> tags) {
        if (tags == null || tags.isEmpty()) return new ArrayList<>(eventsList);

        // Lowercase
        ArrayList<String> lowerTags = new ArrayList<>(tags.size());
        for (String t : tags) lowerTags.add(t.toLowerCase());

        ArrayList<Event> filteredEventList = new ArrayList<>();
        for (Event e : eventsList) {
            ArrayList<String> eventTagsLower = new ArrayList<>(e.getTags().size());
            for (String t : e.getTags()) eventTagsLower.add(t.toLowerCase());

            if (eventTagsLower.containsAll(lowerTags)) filteredEventList.add(e);
        }
        return filteredEventList;
    }

    // Returns a list of events had by a specific Organizer ID
    public ArrayList<Event> getOrganizerEvents(String organizerID) {
        ArrayList<Event> organizerEventList = new ArrayList<>();

        if (organizerID == null || organizerID.isEmpty()) {
            return organizerEventList;
        }

        for (Event e : eventsList) {
            if (e != null && organizerID.equals(e.getOrganizer())) {
                organizerEventList.add(e);
            }
        }

        return organizerEventList;
    }

    // Return all events the given entrant is a part of
    public ArrayList<Event> getEntrantEvents(String entrantID) {
        ArrayList<Event> entrantEventList = new ArrayList<>();

        for (Event e: eventsList) {
            // If given entrants
            if (e.getEntrants().contains(entrantID)) {
                entrantEventList.add(e);
            }
        }

        return entrantEventList;
    }

    // Return all events the given entrant is waitlisted for
    public ArrayList<Event> getEntrantWaitlistedEvents(String entrantID) {
        ArrayList<Event> waitlistedEventList = new ArrayList<>();

        for (Event e: eventsList) {
            // If given entrants
            if (e.getWaitingEntrants().contains(entrantID)) {
                waitlistedEventList.add(e);
            }
        }

        return waitlistedEventList;
    }


}