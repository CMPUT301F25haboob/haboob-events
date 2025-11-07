package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;
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
                        Event e = doc.toObject(Event.class); // Turn data back into object
                        eventsList.add(e);
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
    public String addEvent(Event e, OnEventsLoadedListener listener) {
        eventsListRef.add(e)
                .addOnSuccessListener(docRef -> {
                    eventsList.add(e);
                    Log.d("EventsList", "Added event with ID: " + e.getEventID());
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

        return e.getEventID();
    }

    // Backward compatible version
    public String addEvent(Event e) {
        return addEvent(e, null);
    }

    // Delete event from db using its unique Firestore ID
    public void deleteEvent(Event e, OnEventsLoadedListener listener) {

        if (eventsList == null || eventsList.isEmpty()) {
            throw new IllegalStateException("Cannot delete from an empty events list");
        }

        if (e.getEventID() == null || e.getEventID().isEmpty()) {
            if (listener != null) {
                listener.onError(new IllegalArgumentException("Event ID is missing"));
            }
            return;
        }

        // Delete the Firestore document using its ID
        db.collection("events")
                .whereEqualTo("eventID", e.getEventID())
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // Remove from local list only after Firestore success
                    eventsList.remove(e);

                    // Delete the firestore document
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


    // Backward compatible version
    public void deleteEvent(Event e) {
        deleteEvent(e, null);
    }

    // Find event by ID and return it
    public Event getEventByID(String eventID) {
        if (eventID == null) return null;
        for (Event e : eventsList) {
            if (e != null && eventID.equals(e.getEventID())) return e;
        }
        return null;
    }

    // Return list of all events that have the same tag(s) as the input given
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

    // Returns a list of events had by a specific Organizer ID
    public ArrayList<Event> getOrganizerEvents(String organizerID) {
        ArrayList<Event> out = new ArrayList<>();
        if (organizerID == null || organizerID.isEmpty()) return out;
        for (Event e : eventsList) {
            if (e != null && organizerID.equals(e.getOrganizer())) out.add(e);
        }
        return out;
    }


    // Return all events the given entrant is waitlisted for
    public ArrayList<Event> getEntrantWaitlistEvents(String entrantID) {
        ArrayList<Event> out = new ArrayList<>();
        if (entrantID == null) return out;
        for (Event e : eventsList) {
            List<String> waiting = (e != null) ? e.getWaitingEntrants() : null;
            if (waiting != null && waiting.contains(entrantID)) out.add(e);
        }
        return out;
    }

    // Return all events the given entrant is waitlisted for
    public ArrayList<Event> getEntrantEnrolledEvents(String entrantID) {
        ArrayList<Event> EnrolledEventList = new ArrayList<>();

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

}