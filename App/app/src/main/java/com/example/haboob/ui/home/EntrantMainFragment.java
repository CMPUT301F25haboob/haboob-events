package com.example.haboob.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.haboob.Event;
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
//    private String deviceId = "9662d2bd2595742d";
    private String deviceId;
    String userID = "davids_id";
    // prepare a list of sample imageURLs:
//    List<String> imageURLs = Arrays.asList("https://letsenhance.io/static/73136da51c245e80edc6ccfe44888a99/396e9/MainBefore.jpg", "https://blog.en.uptodown.com/files/2017/08/clash-royale-consejos-novato-featured.jpg", "https://media.cnn.com/api/v1/images/stellar/prod/130214161738-01-michael-jordan.jpg?q=w_3072,h_1728,x_0,y_0,c_fill");
    List<String> imageURLs = new ArrayList<>();
    EventImageAdapter imageAdapter = new EventImageAdapter(imageURLs);
    EventImageAdapter imageAdapter2 = new EventImageAdapter(imageURLs);
    boolean createDummyEvent = false;

    List<Event> listOfEvents = new ArrayList<>(); // making a List<Event> So I can iterate through the events, cant do that with an EventsList object
    private EventsList eventsList3; // declare the eventsList object

    public EntrantMainFragment() {
        // Required empty public constructor
    }

    @Override
    // runs ONCE as opposed to onCreateView, so not making a new dummy variable every time we navigate back to main screen
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (createDummyEvent){
            createDummyEvent();
        }
    }

    // queries for the DeviceID, runs BEFORE onCreate and onCreateView
    @SuppressLint("HardwareIds")
    @Override
    public void onAttach(@NonNull Context ctx) {

        super.onAttach(ctx);
        deviceId = Settings.Secure.getString(
                ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (deviceId == null) deviceId = "unknown";
    }

    // queries the dataBase, relies on a callback to adds events to local EventList Array, updates the imageAdapter with the new images from the database
    private void loadEventsForUser(String userId) {

            Log.d("TAG", "device ID: " + deviceId);

            listOfEvents.clear(); // discard duplicate events

          eventsList3 = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() { // the callback function calls this when events are loaded

                listOfEvents = eventsList3.getEventsList();
                List<Event> listOfFILTEREDEvents = new ArrayList<>();

//                for (Event event: listOfEvents){
//                    if (event.getEntrant_ids_for_lottery() != null)
//                        Log.d("TAG", "Device ID: "+  event.getEntrant_ids_for_lottery().toString());
//                }

                for (Event event: listOfEvents){
                    if ((event.getEntrant_ids_for_lottery() != null) && (!event.getEntrant_ids_for_lottery().isEmpty()) && (event != null))
                    {
                        if (event.getEntrant_ids_for_lottery().contains(deviceId)) {
                            listOfFILTEREDEvents.add(event);
                        }
                    } else {
                        Log.d("TAG", "SOMETHING IS NULL");
                    }
                }

//                for (Event event: listOfFILTEREDEvents){
//                   Log.d("TAG", "Device ID: "+  event.getEntrant_ids_for_lottery().toString());
//                }
                Log.d("TAG", "FILTERED EVENTSLIST SIZE: " + listOfFILTEREDEvents.size());

                // runs AFTER the database is done querying:
//                Log.d("TAG", "EVENTSLIST 4 SIZE: " + listOfEvents.size());
                List<String> imageURLs = new ArrayList<>();
                List<String> eventIDs = new ArrayList<>();

                addEventImagesLocally(listOfFILTEREDEvents, imageURLs); // imageURLS <- list of imageURLs from query
                addEventIDsLocally(listOfFILTEREDEvents, eventIDs); // eventIDs <- list of eventIDs from query

                // replace the placeholder images after query is done:
                imageAdapter.replaceItems(imageURLs);
                // input the IDs of the same images into the imageAdapter
                imageAdapter.inputIDs(eventIDs);
                Log.d("TAG", "ImageAdapter images Replaced");

                // to see ALL events for testing:
                addEventImagesLocally(listOfEvents, imageURLs);
                addEventIDsLocally(listOfEvents, eventIDs);
                // replace the placeholder images
                imageAdapter2.replaceItems(imageURLs);
                // input the IDs of the same images into the imageAdapter
                imageAdapter2.inputIDs(eventIDs);
                Log.d("TAG", "ImageAdapter images Replaced");

            }
            @Override
            public void onError(Exception err) {
                Log.e("TAG", "Failed loading events", err);
            }
        });
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

//        for (Event event : listOfEvents) {
//                                Log.d("TAG", event.getEventTitle() + "desc: " + event.getEventDescription());
//                            }

        Log.d("TAG", "ListOf events size: " + listOfEvents.size());

       // Turns the XML file entrant_main.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_main, container, false);

        // *****  ***** ***** ***** First carousel - My upcoming events ***** ***** ***** ***** ***** ***** *****

        RecyclerView recyclerView = view.findViewById(R.id.entrant_rv_upcoming);
        // Prepare a list of example images from drawable
//        List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);

        // Set up LayoutManager for horizontal scrolling, tells the RecyclerView how to position items, essential for actual rendering
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(imageAdapter);

        // Clicks on the viewBindHolder calls back to this onItemCLick, and this finds the event
        // associated with the click and starts a new fragment with the event data
        imageAdapter.setOnItemClick(eventId -> {

            Log.d("TAG", "The callback worked, event ID = " + eventId);
//            Log.d("TAG", "ListOf events size: " + listOfEvents.size());

//            EventsList eventsList = new EventsList(dummyString);
            // find the event clicked(the new events list should be updated with the database data):
            for (Event event : listOfEvents) {
                if (event.getEventID().equals(eventId)) {

                    Log.d("TAG", "GOT PAST IF ");
                    Log.d("TAG", "Event clicked: " + event.getEventTitle());

                    // Create a Bundle to pass data to the EventViewerFragment
                    Bundle args = new Bundle();
                    args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);
                    args.putString("device_id", deviceId);
                    args.putBoolean("from_my_events", true); // sets JoinEvent button invisible

                    // navigate to the EventViewerFragment using the NavController
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_event_view, args);

                }
            }
        });

        // *****  ***** ***** ***** ***** ***** Second carousel - My Open Waitlists ***** ***** ***** ***** ***** ***** ***** *****
        RecyclerView rvWaitlists = view.findViewById(R.id.entrant_rv_waitlists);
        rvWaitlists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWaitlists.setNestedScrollingEnabled(false);
        List<Integer> waitlistImages = Arrays.asList(
                R.drawable.clash_royale, R.drawable.clash_royale, R.drawable.clash_royale
        );
        rvWaitlists.setAdapter(imageAdapter2);
//
//        // set the onClickListener for the second carousel:
        imageAdapter2.setOnItemClick(eventId -> {

            Log.d("TAG", "The callback worked, event ID = " + eventId + "Event title: " + eventsList3.getEventByID(eventId).getEventTitle());
//            Log.d("TAG", "ListOf events size: " + listOfEvents.size());

//            EventsList eventsList = new EventsList(dummyString);
            // find the event clicked(the new events list should be updated with the database data):
            for (Event event : listOfEvents) {
                if (event.getEventID().equals(eventId)) {

                    Log.d("TAG", "GOT PAST IF ");
                    Log.d("TAG", "Event clicked: " + event.getEventTitle());

                    // Create a Bundle to pass data to the EventViewerFragment
                    Bundle args = new Bundle();
                    args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);
                    args.putString("device_id", deviceId);
                    args.putBoolean("from_my_events", false); // sets leaveEvent button invisible

                    // navigate to the EventViewerFragment using the NavController
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_event_view, args);

                }
            }
        });
//
//        // Return the inflated view
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // set a listener that listens to EventViewerFragment, if a user ID got added to an event entrant_ids_for_lottery, update the image carousels
        getParentFragmentManager().setFragmentResultListener(
                "USER_JOINED_WAITLIST", this, (reqKey, bundle) -> loadEventsForUser(userID)
        );

        // set a listener that listens to EventViewerFragment, if a user ID got added to an event entrant_ids_for_lottery, update the image carousels
        getParentFragmentManager().setFragmentResultListener(
                "USER_LEFT_WAITLIST", this, (reqKey, bundle) -> loadEventsForUser(userID)
        );


    }

    public void createDummyEvent() {
        // *********** create a new dummy event: ********************************************************************
        MainActivity mainAct = (MainActivity) getActivity(); // find the instance of mainActivity thats currently running
        EventsList eventsList = mainAct.getEventsList();
        if (eventsList == null) {
            Log.w("EntrantMainFragment", "eventsList was null");
            return;
        }

        // Build registration dates
        Date regStart = new GregorianCalendar(2025, Calendar.NOVEMBER, 1).getTime();
        Date regEnd   = new GregorianCalendar(2025, Calendar.NOVEMBER, 15).getTime();

        // Create supporting objects
        QRCode qrCode = new QRCode("idk lol");
        Poster poster = new Poster("https://shapes.inc/api/public/avatar/johnpork-qwb7");

        // Create a list of tags for this event
        List<String> tagStrings = new ArrayList<>();
        tagStrings.add("festival");
        tagStrings.add("outdoor");
        tagStrings.add("family");
//        EventTagList tags = new EventTagList(tagStrings);

        ArrayList<String> tagslist2 = new ArrayList<>();
        tagslist2.add("Spongebob");
        tagslist2.add("lol");
        tagslist2.add("Guy");

        // create a list of dummy entrant Ids for this event:
        ArrayList<String> event_entrant_ids = new ArrayList<>();

        event_entrant_ids.add(deviceId);


        // Finally, create your dummy Event using your constructor
        Event dummyEvent = new Event(
                "org12345",                                  // organizer
                regStart,                                    // registrationStartDate
                regEnd,                                      // registrationEndDate
                "John Pork Meet N Greet",                          // eventTitle
                "RIP jon :(",             // eventDescription
                true,                                        // geoLocationRequired
                100,                                         // lotterySampleSize
                qrCode,                                      // QRCode object
                poster,                                      // Poster object
                tagslist2,                // tagsList<String>
                event_entrant_ids                             // entrant event ids
        );

//        Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, QRCode qrCode, Poster poster, ArrayList<String> tags, ArrayList<String> entrant_ids_for_lottery) {

        if (eventsList != null){
            eventsList.addEvent(dummyEvent);
            Log.d("TAG", "eventsList is not null");
        }
        else{
            Log.d("TAG", "eventsList is null");
        }

    }

    // adds the strings of the events to the local list of images (images2)
    public void addEventImagesLocally(List<Event> eventsList2, List<String> imageURLs) {

        if (eventsList2 == null) {
            Log.w("EntrantMainFragment", "eventsList2 was null");
            return;
        }

        imageURLs.clear(); // clear placeholder images
        for (Event event : eventsList2) {
            if (event == null) continue;

            Poster poster = event.getPoster();
            if (poster != null && poster.getData() != null && !poster.getData().isEmpty()) {
                imageURLs.add(poster.getData());
            } else {
                Log.w("EntrantMainFragment",
                        "Missing poster for event ID:" + event.getEventID() + ", title=" + event.getEventTitle());
                // either SKIP or add a placeholder entry your adapter can render
                 imageURLs.add("https://media.tenor.com/hG6eR9HM_fkAAAAM/the-simpsons-homer-simpson.gif");
            }
        }
    }

    // adds the strings of the events to the local list of images (images2)
    public void addEventIDsLocally(List<Event> eventsList2, List<String> eventIDs) {
        eventIDs.clear();
        for (Event event : eventsList2) {
            eventIDs.add(event.getEventID());
//            Log.d("TAG", "Event: " + event.getEventTitle() + " ID string: " + event.getEventID());
        }
    }
}