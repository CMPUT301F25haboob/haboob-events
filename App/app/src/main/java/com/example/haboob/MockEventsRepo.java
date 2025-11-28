package com.example.haboob;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class MockEventsRepo {

    private final FirebaseFirestore db;

    public MockEventsRepo(FirebaseFirestore db) {
        this.db = db;
    }

    public void addfakeEvent(Event event, OnCompleteListener<Void> listener) {
        db.collection("events")
                .document(event.getEventID())
                .set(event)
                .addOnCompleteListener(listener);
    }


}
