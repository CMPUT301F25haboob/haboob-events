package com.example.haboob.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


// Author: David T, created on Sunday, oct 26 2025
// this fragment represents the main fragment that the entrant will see when entering the app

/**
 * Displays the entrant home screen containing two carousels:
 * <ul>
 *   <li><b>My upcoming events</b> – events the current device/user is enrolled in</li>
 *   <li><b>My open waitlists</b> – events the user is currently waiting on (or all for testing)</li>
 * </ul>
 *
 * <p>This Fragment:
 * <ol>
 *   <li>Resolves a stable device identifier in {@link #onAttach(Context)}</li>
 *   <li>Loads events asynchronously via {@link EventsList} and the
 *       {@link EventsList.OnEventsLoadedListener} callback</li>
 *   <li>Transforms loaded events into image URLs and event IDs for two {@link EventImageAdapter}s</li>
 *   <li>Navigates to {@code EventViewerFragment} when a carousel item is tapped</li>
 *   <li>Listens for result events (join/leave) and refreshes data</li>
 * </ol>
 *
 * <p><b>Lifecycle notes:</b> Heavy work (Firestore/EventsList) is triggered from
 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} via {@link #loadEventsForUser(String)}.
 * The device ID is prepared earlier in {@link #onAttach(Context)}.</p>
 *
 * <p>Author: David T, created on Sunday, Oct 26, 2025.</p>
 */
public class EntrantMainFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private String deviceId = "9662d2bd2595742d";
    private String deviceId;
    String userID = "davids_id";
    // prepare a list of sample imageURLs:
//    List<String> imageURLs = Arrays.asList("https://letsenhance.io/static/73136da51c245e80edc6ccfe44888a99/396e9/MainBefore.jpg", "https://blog.en.uptodown.com/files/2017/08/clash-royale-consejos-novato-featured.jpg", "https://media.cnn.com/api/v1/images/stellar/prod/130214161738-01-michael-jordan.jpg?q=w_3072,h_1728,x_0,y_0,c_fill");
    List<String> imageURLs = new ArrayList<>();
    EventImageAdapter enrolledEventsAdapter = new EventImageAdapter(imageURLs);
    EventImageAdapter waitListsAdapter = new EventImageAdapter(imageURLs);
    boolean createDummyEvent = false;

    List<Event> waitListEvents = new ArrayList<>(); // making a List<Event> So I can iterate through the events, cant do that with an EventsList object
    List<Event> enrolledEventsList = new ArrayList<>(); // has a list of events the user is enrolled in

    public EventsList getEventsList3() {
        return eventsList3;
    }

    private EventsList eventsList3; // declare the eventsList object
    private Button myWaitlists;

    public EntrantMainFragment() {
        // Required empty public constructor
    }

    /**
     * Called once for the fragment’s creation. Optionally creates a debug dummy event if
     * {@link #createDummyEvent} is enabled.
     *
     * @param savedInstanceState previously saved state (unused)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (createDummyEvent){
            createDummyEvent();
        }
    }

    /**
     * Earliest lifecycle hook with a valid {@link Context}. Retrieves ANDROID_ID and stores it
     * in {@link #deviceId} for filtering events by device.
     *
     * <p>Runs before {@link #onCreate(Bundle)} and {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.</p>
     *
     * @param ctx host context
     */
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
    /**
     * Loads events for the given user/device asynchronously.
     *
     * <p>Side effects:</p>
     * <ul>
     *   <li>Clears local event caches</li>
     *   <li>Constructs a new {@link EventsList} which fetches data</li>
     *   <li>On callback, populates {@link #enrolledEventsList} and {@link #waitListEvents}</li>
     *   <li>Builds image URL and ID lists and updates both adapters via
     *       {@link EventImageAdapter#replaceItems(List)} and {@link EventImageAdapter#inputIDs(List)}</li>
     * </ul>
     *
     * @param userId logical user identifier; current filtering relies on {@link #deviceId}
     */
    private void loadEventsForUser(String userId) {

            Log.d("TAG", "device ID: " + deviceId);

            waitListEvents.clear(); // discard duplicate events locally

            eventsList3 = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() { // the callback function calls this when eventsList3 are loaded

                waitListEvents = eventsList3.getEventsList();
                waitListEvents = eventsList3.getEntrantWaitlistEvents(deviceId);
                enrolledEventsList = eventsList3.getEntrantEnrolledEvents(deviceId);

                Log.d("TAG", "Enrolled EVENTSLIST SIZE: " + enrolledEventsList.size());

                // runs AFTER the database is done querying:
//                Log.d("TAG", "EVENTSLIST 4 SIZE: " + listOfEvents.size());
                List<String> imageURLs = new ArrayList<>();
                List<String> eventIDs = new ArrayList<>();

                addEventImagesLocally(enrolledEventsList, imageURLs); // imageURLS <- list of imageURLs from query
                addEventIDsLocally(enrolledEventsList, eventIDs); // eventIDs <- list of eventIDs from query

                // ********** Enrolled events image adapter: ****************
                // replace the placeholder images after query is done:
                enrolledEventsAdapter.replaceItems(imageURLs);
                // input the IDs of the same images into the imageAdapter
                enrolledEventsAdapter.inputIDs(eventIDs);
                Log.d("TAG", "ImageAdapter images Replaced");

                // ********** waitlist Events image adapter: ****************
                // to see ALL events for testing:
                addEventImagesLocally(waitListEvents, imageURLs); // imageURLS <- list of imageURLs from query
                addEventIDsLocally(waitListEvents, eventIDs); // eventIDs <- list of eventIDs from query
                // replace the placeholder images
                waitListsAdapter.replaceItems(imageURLs);
                // input the IDs of the same images into the imageAdapter
                waitListsAdapter.inputIDs(eventIDs);
                Log.d("TAG", "ImageAdapter images Replaced");

            }
            @Override
            public void onError(Exception err) {
                Log.e("TAG", "Failed loading events", err);
            }
        });
    }

    /**
     * Inflates the entrant main layout, wires RecyclerViews/adapters, starts data loading,
     * and attaches item click handlers that navigate to {@code EventViewerFragment}.
     *
     * @param inflater  layout inflater
     * @param container parent container
     * @param savedInstanceState saved state (unused)
     * @return the inflated root {@link View}
     */ @Override
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

        Log.d("TAG", "ListOf events size: " + waitListEvents.size());

       // Turns the XML file entrant_main.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_main, container, false);


        // Dan
        // ***** Profile Button Navigation *****
        Button profileButton = view.findViewById(R.id.btn_profile);
        profileButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_entrantMain_to_profile);
        });

        // *****  ***** ***** ***** First carousel - My upcoming events ***** ***** ***** ***** ***** ***** *****
        // Find RecyclerView by ID
        RecyclerView recyclerView = view.findViewById(R.id.entrant_rv_upcoming);
        // Prepare a list of example images from drawable

        // Set up LayoutManager for horizontal scrolling, tells the RecyclerView how to position items, essential for actual rendering
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(enrolledEventsAdapter);

        // Clicks on the viewBindHolder calls back to this onItemCLick, and this finds the event
        // associated with the click and starts a new fragment with the event data
        enrolledEventsAdapter.setOnItemClick(eventId -> {

            Log.d("TAG", "The callback worked, event ID = " + eventId);
//            Log.d("TAG", "ListOf events size: " + listOfEvents.size());

            // find the event clicked(the new events list should be updated with the database data):
            for (Event event : enrolledEventsList) {
                if (event.getEventID().equals(eventId)) {

                    Log.d("TAG", "Event clicked equalled event ID, Event clicked: " + event.getEventTitle());

                    // Create a Bundle to pass data to the EventViewerFragment
                    Bundle args = new Bundle();
                    args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);
                    args.putString("device_id", deviceId);
                    args.putBoolean("from_enrolledEvents", true); // sets JoinEvent button invisible

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
        rvWaitlists.setAdapter(waitListsAdapter);

        // set the onClickListener for the second carousel:
        waitListsAdapter.setOnItemClick(eventId -> {

            // find the event clicked(the new events list should be updated with the database data):
            for (Event event : waitListEvents) {
                if (event.getEventID().equals(eventId)) {

                    Log.d("TAG", "Callback for open waitlists carousel worked:  " + eventId + "Event title: " + eventsList3.getEventByID(eventId).getEventTitle());

                    // Create a Bundle to pass data to the EventViewerFragment
                    Bundle args = new Bundle();
                    args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);
                    args.putString("device_id", deviceId);
                    args.putBoolean("from_my_events", false); // sets leaveEvent button invisible

                    if (event.getWaitingEntrants().contains(deviceId)) {
                        args.putBoolean("in_waitlist", true); // sets leaveEvent button invisible
                    }
                    // navigate to the EventViewerFragment using the NavController
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_event_view, args);

                }
            }
        });

//     // Return the inflated view
        return view;
    }

    /**
     * Called after the view hierarchy has been created. Registers fragment result listeners
     * so that when the user joins/leaves waitlists or leaves an event, the carousels refresh.
     *
     * @param view root view
     * @param savedInstanceState saved state (unused)
     */
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

        // set a listener that listens to EventViewerFragment, if a user ID got deleted from enrolledEvents, update image carousels
        getParentFragmentManager().setFragmentResultListener(
                "USER_LEFT_EVENT", this, (reqKey, bundle) -> loadEventsForUser(userID)
        );

        // set a listener that listens for myWaitlists button click, on navigate, it navigates there
        myWaitlists = view.findViewById(R.id.btn_my_waitlists);
        Bundle args = new Bundle();
        args.putString("device_id", deviceId); // args <- deviceID

        myWaitlists.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.waitlists_view_fragment, args)
        );

    }

    /**
     * Debug helper to create and add a single dummy {@link Event} to the shared {@link EventsList}
     * owned by {@link MainActivity}. No-ops if the list cannot be retrieved.
     *
     * <p>Uses current {@link #deviceId} to seed entrant IDs.</p>
     */
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
        Poster poster = new Poster("https://www.seriouseats.com/thmb/LoXQL7Yp_uXxtipH8cCp_LGVg5E=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__recipes__images__2014__08__20140810-workhorse-bread-vicky-wasik-3-3a86ee51da2e4a7b8239ceb62d8d8d17.jpg");

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
                "bread making",                          // eventTitle
                "dough",             // eventDescription
                true,                                        // geoLocationRequired
                100,                                         // lotterySampleSize
                200,                                            // optionalWaitingListSize
                qrCode,                                      // QRCode object
                poster,                                      // Poster object
                tagslist2                // tagsList<String
        );

//         public Event( QRCode qrCode, Poster poster, ArrayList<String> tags) {
//            this.db = FirebaseFirestore.getInstance();
//            this.organizerID = organizer;
//            this.registrationStartDate = registrationStartDate;
//            this.registrationEndDate = registrationEndDate;
//            this.eventTitle = eventTitle;
//            this.eventDescription = eventDescription;
//            this.geoLocationRequired = geoLocationRequired;
//            this.lotterySampleSize = lotterySampleSize;
//            this.optionalWaitingListSize = optionalWaitingListSize;
//            this.qrCode = qrCode;
//            this.poster = poster;
//            this.tags = tags;
//            this.initLists();
//        }

//        Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, QRCode qrCode, Poster poster, ArrayList<String> tags, ArrayList<String> entrant_ids_for_lottery) {

        if (eventsList != null){
            eventsList.addEvent(dummyEvent);
            Log.d("TAG", "eventsList is not null");
        }
        else{
            Log.d("TAG", "eventsList is null");
        }

    }

    /**
     * Converts a list of {@link Event} objects into a list of poster image URLs suitable for
     * submission to an {@link EventImageAdapter}. Missing posters are replaced with a placeholder.
     *
     * @param eventsList2 source events (may be {@code null})
     * @param imageURLs   destination list; cleared before population
     */
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

    /**
     * Extracts {@link Event#getEventID()} from each event into {@code eventIDs}, preserving order.
     *
     * @param eventsList2 source events
     * @param eventIDs    destination list; cleared before population
     */
    public void addEventIDsLocally(List<Event> eventsList2, List<String> eventIDs) {
        eventIDs.clear();
        for (Event event : eventsList2) {
            eventIDs.add(event.getEventID());
//            Log.d("TAG", "Event: " + event.getEventTitle() + " ID string: " + event.getEventID());
        }
    }
}