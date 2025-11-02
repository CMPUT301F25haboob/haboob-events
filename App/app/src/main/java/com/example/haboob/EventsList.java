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
public class EventsList {
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

    // Optional: Constructor with callback for when you need to know when loading completes
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

    // Updated loadEventsList with callback
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

    // Overloaded version without callback for backward compatibility
    public void loadEventsList() {
        loadEventsList(null);
    }

    // Add event to db and set events unique Firestore ID
    public void addEvent(Event e, OnEventsLoadedListener listener) {
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
    }

    // Backward compatible version
    public void addEvent(Event e) {
        addEvent(e, null);
    }

    // Delete event from db using its unique Firestore ID
    public void deleteEvent(Event e, OnEventsLoadedListener listener) {
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

    // Backward compatible version
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
        ArrayList<Event> filteredEventList = new ArrayList<>();

        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>(eventsList);
        }

        // Make input tags all lowercase
        List<String> lowerTags = new ArrayList<>();
        for (String tag : tags) {
            if (tag != null) lowerTags.add(tag.toLowerCase());
        }

        // Iterate through all events in eventsList
        for (Event e: eventsList) {
            List<String> eventTags = e.getTags();
            if (eventTags == null) continue;

            // Make event's tags lowercase
            List<String> lowerEventTags = new ArrayList<>();
            for (String tag: eventTags) {
                if (tag != null) lowerEventTags.add(tag.toLowerCase());
            }

            // If event contains all the given tags
            if (lowerEventTags.containsAll(lowerTags)) {
                filteredEventList.add(e);
            }
        }

        return filteredEventList;
    }

    // Returns a list of events had by a specific Organizer ID
    public ArrayList<Event> getOrganizerEvents(Organizer o) {
        ArrayList<Event> organizerEventList = new ArrayList<>();

        if (o == null || o.getOrganizerID() == null) {
            return organizerEventList;
        }

        // Iterate through all events in eventsList
        for (Event e: eventsList) {
            // If event e has the given organizer ID
            if (e.getOrganizer().equals(o.getOrganizerID())) {
                organizerEventList.add(e);
            }
        }

        return organizerEventList;
    }
}