package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/*
 * This class holds and manage a list of all events on the app.
 * getEventsList() Return a list of all events objects
 * loadEventsList() Loads events_list collection of events from Firebase into eventsList
 * addEvent(Event) add an event object to firebase
 * deleteEvent(Event) delete and event from firebase
 * filterEvents(List<String> labels) return a list of filtered events based of a list of tags
 */
public class EventsList {
    private ArrayList<Event> eventsList;
    private FirebaseFirestore db;
    private CollectionReference eventsListRef;

    // Constructor
    public EventsList() {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventsListRef = db.collection("events_list");
    }

    public ArrayList<Event> getEventsList() {
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

    // Add event to db and set events unique Firebase ID
    public void addEvent(Event e) {
        eventsListRef.add(e)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    e.setEventID(id);
                    eventsList.add(e); // Add to local eventsList
                    Log.d("TAG", "Added event with ID: " + id);
                })
                .addOnFailureListener(ex -> {
                    Log.e("TAG", "Failed to add event", ex);
                });
    }

    // Delete event from db using its unique Firebase ID
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

}