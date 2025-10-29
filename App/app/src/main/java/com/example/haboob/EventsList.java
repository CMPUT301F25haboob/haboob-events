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

    // Constructor
    public EventsList() {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events");
    }

    // Return a list of all events
    public ArrayList<Event> getAllEventsList() {
        return eventsList;
    }

    public void loadEventsList() {
        eventsListRef.get().addOnSuccessListener(snapshots -> {
            eventsList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                eventsList.add(doc.toObject(Event.class));
            }
        });
    }

    // Add event to db and set events unique Firestore ID
    public void addEvent(Event e) {
        eventsListRef.add(e)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    e.setEventID(id);
                    
                    db.collection("events").document(id).set(e);

                    eventsList.add(e); // Add to local eventsList
                    Log.d("TAG", "Added event with ID: " + id);
                })
                .addOnFailureListener(ex -> {
                    Log.e("TAG", "Failed to add event", ex);
                });

    }

    // Delete event from db using its unique Firestore ID
    public void deleteEvent(Event e) {
        if (e.getEventID() == null || e.getEventID().isEmpty()) {
            Log.w("TAG", "Cannot delete event: missing ID");
            return;
        }

        eventsListRef.document(e.getEventID()).delete()
                .addOnSuccessListener(aVoid -> {
                    eventsList.remove(e); // Remove from to local eventsList
                    Log.d("TAG", "Deleted event with ID: " + e.getEventID());
                })
                .addOnFailureListener(ex -> {
                    Log.e("TAG", "Failed to delete event", ex);
                });
    }

    // Find event by ID and return it
    public Event getEventByID(String eventID) {
        for (Event e: eventsList) {
            if (e.getEventID() == eventID) {
                return e;
            }
        }
        return null;
    }

    // Return list of all events that have the same tag(s) as the input given
    public ArrayList<Event> filterEvents(List<String> tags) {
        ArrayList<Event> filteredEventList = new ArrayList<>();

        // If tags is null or empty list return all events
        if (tags == null || tags.isEmpty()) return new ArrayList<>(eventsList);

        // Iterate through all events in eventsList
        for (Event e: eventsList) {
            // If current event's (e) tags are not null and contain all the given tags add to list
            if (e.getTags() != null && e.getTags().containsAll(tags)) {
                filteredEventList.add(e);
            }
        }

        return filteredEventList;
    }

    // Returns a list of events had by a specific Organizer ID
    public ArrayList<Event> getOrganizerEvents(Organizer o) {
        ArrayList<Event> organizerEventList = new ArrayList<>();

        if (o == null || o.getOrganizerID() == null) return organizerEventList;

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