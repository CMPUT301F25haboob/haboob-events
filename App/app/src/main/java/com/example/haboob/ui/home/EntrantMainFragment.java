package com.example.haboob.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.haboob.Event;
import com.example.haboob.EventTagList;
import com.example.haboob.EventsList;
import com.example.haboob.GeoLocationMap;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


// Author: David T, created on Sunday, oct 26 2025
// this fragment represents the main fragment that the entrant will see when entering the app

public class EntrantMainFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // will hold the local Events list, query will add to this eventsList
    private final List<Event> eventsList2 = new ArrayList<>();

    List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);


    public EntrantMainFragment() {
        // Required empty public constructor
    }
    // TODO: this fragment needs to query the database and show all events that the user is registered for,
    // and then once a user clicks one, use an intent to start a new fragment that shows the event details


    // queries the dataBase, adds events to local EventList Array
    private void loadEventsForUser(String userId) {
        // Example paths — pick ONE that matches your schema:
        // CollectionReference col = db.collection("events");                                      // top-level
        // CollectionReference col = db.collection("cities").document("BJ").collection("events"); // subcollection
        CollectionReference col = db.collection("events");

        // If you want only the events the user is registered for (array of ids):
        // Remove this where clause if you want ALL events.
        Query q = col;

        // Clear current list to avoid duplicates on refresh
        eventsList2.clear();

        q.get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            // Keep Firestore id if useful
                            try { e.setEventID(doc.getId()); } catch (Exception ignored) {}
                            eventsList2.add(e);
                        } else {
                            Log.w("TAG", "Skipping unmappable doc: " + doc.getId() + " -> " + doc.getData());
                        }
                    }
//                    if (adapter != null) adapter.notifyDataSetChanged();
                    Log.d("TAG", "Loaded " + eventsList2.size() + " events");
                })
                .addOnFailureListener(err -> Log.e("TAG", "Failed loading events", err));
    }


    // “When this Fragment becomes visible, create its UI from entrant_main.xml and attach it to the container.”
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // access mainActivity to access the EventsList
        MainActivity mainActivity = (MainActivity) getActivity();
        EventsList eventsList = mainActivity.getEventsList();

        String userID = "davids_id";
//        loadEventsForUser(userID);

//        for (Event event : eventsList) {
//                Log.w("TAG", "Event ID: " + event.getEventID() + ", Event Title: " + event.getEventTitle());
//        }

//        Log.d("TAG", "ThE FRAGMENT LAUNCHED");

        // DEBUG: Logging each ID in the database:
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });




//        // *********** create a new dummy event: ********************************************************************
//
        // Build registration dates
        Date regStart = new GregorianCalendar(2025, Calendar.NOVEMBER, 1).getTime();
        Date regEnd   = new GregorianCalendar(2025, Calendar.NOVEMBER, 15).getTime();

        // Create supporting objects
        QRCode qrCode = new QRCode("https://haboob.app/events/event001");
        Poster poster = new Poster("bob_ross.webp");

        // Create a list of tags for this event
        List<String> tagStrings = new ArrayList<>();
        tagStrings.add("festival");
        tagStrings.add("outdoor");
        tagStrings.add("family");
        EventTagList tags = new EventTagList(tagStrings);

//        List<String> tagslist2 = new ArrayList<>();
//        tagslist2.add("Bob");
//        tagslist2.add("Ross");
//        tagslist2.add("Art");

        // create a list of dummy entrant Ids for this event:
        ArrayList<String> event_entrant_ids = new ArrayList<>();

        event_entrant_ids.add("david's_id");

        // Finally, create your dummy Event using your constructor
        Event dummyEvent = new Event(
                "org12345",                                  // organizer
                regStart,                                    // registrationStartDate
                regEnd,                                      // registrationEndDate
                "Bob Ross Art Class",                          // eventTitle
                "Art class hosted by Bob Ross!",             // eventDescription
                true,                                        // geoLocationRequired
                100,                                         // lotterySampleSize
                qrCode,                                      // QRCode object
                poster,                                      // Poster object
                tags,                                         // EventTagList
                event_entrant_ids                             // entrant event ids
        );

        // the actual creation of the event: commented out for now so we're not
        // overpopulating the database
        if (eventsList != null){
            eventsList.addEvent(dummyEvent);
            Log.d("TAG", "eventsList is not null");
        }
        else{
            Log.d("TAG", "eventsList is null");
        }

                // Turns the XML file entrant_main.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_main, container, false);

        // ***** First carousel - My upcoming events *****
        // Find RecyclerView by ID
        RecyclerView recyclerView = view.findViewById(R.id.entrant_rv_upcoming);
        // Prepare a list of example images from drawable
        List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);
        // Set up LayoutManager for horizontal scrolling, tells the RecyclerView how to position items, essential for actual rendering
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // Attach the adapter, its the bridge between the data of the images to the actual UI
        recyclerView.setAdapter(new EventImageAdapter(images));

        // ***** Second carousel - My Open Waitlists *****
        RecyclerView rvWaitlists = view.findViewById(R.id.entrant_rv_waitlists);
        rvWaitlists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWaitlists.setNestedScrollingEnabled(false);
        List<Integer> waitlistImages = Arrays.asList(
                R.drawable.clash_royale, R.drawable.clash_royale, R.drawable.clash_royale
        );
        rvWaitlists.setAdapter(new EventImageAdapter(waitlistImages));

        // Return the inflated view
        return view;
    }

}