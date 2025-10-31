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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import kotlinx.serialization.descriptors.PrimitiveKind;


// Author: David T, created on Sunday, oct 26 2025
// this fragment represents the main fragment that the entrant will see when entering the app

public class EntrantMainFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userID = "davids_id";

    // will hold the local Events list, query will add to this eventsList
    private final List<Event> eventsList2 = new ArrayList<>();

    List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);

    public EntrantMainFragment() {
        // Required empty public constructor
    }
    // TODO: this fragment needs to query the database and show all events that the user is registered for,
    // and then once a user clicks one, use an intent to start a new fragment that shows the event details


    // queries the dataBase, adds events to local EventList Array, note that this takes a while to run!!
    private void loadEventsForUser(String userId) {
        CollectionReference col = db.collection("events");
        Query q = col;

        // Clear current list to avoid duplicates on refresh
        eventsList2.clear();

        // DataBase query -> grabs all event items, adds them to local EnventsList, where they are now local Events()
        q.get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            // Keep Firestore id if useful
                            try { e.setEventID(doc.getId()); } catch (Exception ignored) {}
                            eventsList2.add(e);
                            Log.d("TAG", "event added");
                        } else {
                            Log.w("TAG", "Skipping unmappable doc: " + doc.getId() + " -> " + doc.getData());
                        }
                    }

                    // runs AFTER the database is done querying:
                    Log.d("TAG", "EventsList size: " + eventsList2.size());
                    List<String> imageURLs = new ArrayList<>();
                    addEventImagesLocally(eventsList2, imageURLs); // imageURLS <- list of imageURLs from query



                    for (String image : imageURLs) {
                        Log.d("TAG", "Event image string: " + image);
                    }

                })
                .addOnFailureListener(err -> Log.e("TAG", "Failed loading events", err));
    }

    // When this Fragment becomes visible, create its UI from entrant_main.xml and attach it to the container
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        loadEventsForUser(userID);

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

       // Turns the XML file entrant_main.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_main, container, false);

        // ***** First carousel - My upcoming events *****
        // Find RecyclerView by ID
        RecyclerView recyclerView = view.findViewById(R.id.entrant_rv_upcoming);
        // Prepare a list of example images from drawable
//        List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);

        // prepare a list of imageURLs:
        List<String> imageURLs = Arrays.asList("https://letsenhance.io/static/73136da51c245e80edc6ccfe44888a99/396e9/MainBefore.jpg", "https://blog.en.uptodown.com/files/2017/08/clash-royale-consejos-novato-featured.jpg", "https://media.cnn.com/api/v1/images/stellar/prod/130214161738-01-michael-jordan.jpg?q=w_3072,h_1728,x_0,y_0,c_fill");

        // Set up LayoutManager for horizontal scrolling, tells the RecyclerView how to position items, essential for actual rendering
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // Attach the adapter, its the bridge between the data of the images to the actual UI
        recyclerView.setAdapter(new EventImageAdapter(imageURLs));

//        // ***** Second carousel - My Open Waitlists *****
//        RecyclerView rvWaitlists = view.findViewById(R.id.entrant_rv_waitlists);
//        rvWaitlists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        rvWaitlists.setNestedScrollingEnabled(false);
//        List<Integer> waitlistImages = Arrays.asList(
//                R.drawable.clash_royale, R.drawable.clash_royale, R.drawable.clash_royale
//        );
//        rvWaitlists.setAdapter(new EventImageAdapter(waitlistImages));

        // Return the inflated view
        return view;
    }

    public void createDummyEvent() {

        // *********** create a new dummy event: ********************************************************************
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
        if (eventsList2 != null){
//            eventsList2.add(dummyEvent);
            Log.d("TAG", "eventsList is not null");
        }
        else{
            Log.d("TAG", "eventsList is null");
        }

    }

    // adds the strings of the events to the local list of images (images2)
    public void addEventImagesLocally(List<Event> eventsList2, List<String> imageURLs) {
        for (Event event : eventsList2) {
            imageURLs.add(event.getPoster().getData());
            Log.d("TAG", "Poster data string: " + event.getPoster().getData());
        }
    }

}