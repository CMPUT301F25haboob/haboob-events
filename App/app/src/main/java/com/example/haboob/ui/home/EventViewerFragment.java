package com.example.haboob.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haboob.LotterySampler;
import com.example.haboob.Notification;
import com.example.haboob.NotificationManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.haboob.Event;
import com.example.haboob.EventQRCodeFragment;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Displays the details of a single event, including title, hero image, and actions
 * (join/leave waitlist, leave event, view QR code). Pulls data either from the in-memory
 * {@link EventsList} or, if missing, loads directly from Firebase.
 *
 * <p><b>Lifecycle & data flow:</b>
 * <ol>
 *   <li>{@link #onAttach(Context)} resolves {@code deviceId} (ANDROID_ID).</li>
 *   <li>{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} inflates layout, extracts
 *       {@link #ARG_EVENT_ID}, configures buttons via {@link #setButtons(Bundle)}, and either
 *       displays the event or loads it from Firebase with {@link #loadEventFromFirebase(String, View)}.</li>
 *   <li>{@link #displayEvent(Event, View, String)} binds UI, wires actions and navigation.</li>
 * </ol>
 *
 * <p><b>Arguments:</b> Requires a non-null {@link #ARG_EVENT_ID} passed via fragment arguments.</p>
 *
 * <p><b>Author:</b> David Tyrrell — Oct 28, 2025.</p>
 */
public class EventViewerFragment extends Fragment {

    private String eventID;
    public static final String ARG_EVENT_ID = "arg_event_id";
    private TextView dateView, locView; // declare the view buttons we'll need to update
    private ImageView event_image;
    private MaterialToolbar toolbar;
    private FirebaseFirestore db;
    private String deviceId;
    MaterialButton acceptWaitListInvitationButton, leaveWaitlistButton, leaveEventButton, joinEventButton,
    declineInvitationButton;
    TextView userWaitListStatus;
    private NotificationManager notificationManager;
    EventsList eventsList;
    Event eventToDisplay;

    public EventViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Resolves the device's ANDROID_ID for use in join/leave actions.
     *
     * <p><b>Lifecycle:</b> Runs before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.</p>
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
     * Inflates the event view, initializes UI, reads the required {@link #ARG_EVENT_ID},
     * configures buttons based on status, and displays the event (from {@link EventsList} if present,
     * otherwise via a Firebase fetch).
     *
     * @param inflater  layout inflater
     * @param container parent container
     * @param savedInstanceState saved state (unused)
     * @return inflated root view
     * @throws AssertionError if {@link #ARG_EVENT_ID} is missing in arguments
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event_view, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        toolbar = view.findViewById(R.id.topAppBar);
        event_image = view.findViewById(R.id.heroImage);
        acceptWaitListInvitationButton = view.findViewById(R.id.btnAcceptWaitlistInvite);
        leaveWaitlistButton = view.findViewById(R.id.btnLeaveWaitlist);
        userWaitListStatus = view.findViewById(R.id.userWaitListStatus);
        leaveEventButton = view.findViewById(R.id.btnLeaveEvent);
        joinEventButton = view.findViewById(R.id.btnAcceptEventInvite);
        declineInvitationButton = view.findViewById(R.id.btnDeclineEventInvite);

        db = FirebaseFirestore.getInstance();
        notificationManager = new NotificationManager();

        // unpack the bundle:
        String eventId = requireArguments().getString(ARG_EVENT_ID);

        // grab the details of event:
        assert getActivity() != null;
        assert eventId != null;
        eventsList = ((MainActivity) getActivity()).getEventsList();
        eventToDisplay = eventsList.getEventByID(eventId);

        // Dan
        // If event is not in EventsList yet, load from Firebase
        // This is to get around the EventsList taking along time to load and causing errors
        if (eventToDisplay == null) {
            Log.d("EventViewerFragment", "Event not in list, loading from Firebase: " + eventId);
            loadEventFromFirebase(eventId, view);
        } else {
            Log.d("EventViewerFragment", "Event found in list, displaying: " + eventId);
            displayEvent(eventToDisplay, view, eventId);
        }

//        assert getArguments() != null;
//        Bundle args = getArguments();
//        setButtons(args); // sets fragment buttons based on the current waitlist status

        // ---- DISPLAY EVENT DETAILS DYNAMICALLY HERE ----
        TextView regStartText = view.findViewById(R.id.valueRegistrationStart);
        regStartText.setText(eventToDisplay.getRegistrationStartDate().toString());

        TextView regEndText = view.findViewById(R.id.valueRegistrationEnd);
        regEndText.setText(eventToDisplay.getRegistrationEndDate().toString());

        TextView detailsText = view.findViewById(R.id.valueEventDetails);
        detailsText.setText(eventToDisplay.getEventDescription());

        TextView limitText = view.findViewById(R.id.textNewInfo);
        if (eventToDisplay.getOptionalWaitingListSize() < 0) {
            limitText.setText("Uncapped");
        } else {
            limitText.setText(String.valueOf(eventToDisplay.getOptionalWaitingListSize()));
        }

        TextView lotteryAmountText = view.findViewById(R.id.valueAmount);
        lotteryAmountText.setText(String.valueOf(eventToDisplay.getWaitingEntrants().size()));

        return view;
    }

    /**
     * (Author: <b>David Tyrrell</b> — Oct 28, 2025)
     * Sets button visibility/labels based on the user's current relationship to the event.
     *
     * <p>Looks for optional flags in {@code args}:</p>
     * <ul>
     *   <li>{@code in_waitlist} – user currently on waitlist</li>
     *   <li>{@code from_enrolledEvents} – user is enrolled (came from enrolled carousel)</li>
     * </ul>
     *
//     * @param args the fragment arguments bundle (must include {@link #ARG_EVENT_ID})
     */
    public void setButtons(String ARG_EVENT_ID){
        //TODO: set the buttons based on the current waitlist status
//        assert args != null;
//        String eventId = args.getString(ARG_EVENT_ID);

        assert ARG_EVENT_ID != null;
        EventsList eventsList = new EventsList();
        //eventToDisplay = eventsList.getEventByID(ARG_EVENT_ID);

        List<Event> list = eventsList.getEventsList();
        // list should be populated by the time setButtons() has been called

        for (Event event : list) {
            Log.d("EventViewerFragment", "Event " + event.getEventTitle() + " " + event.getEventID());
        }

        assert eventToDisplay != null;


//        boolean currentlyInWaitlist = args.getBoolean("in_waitlist", false);
//        boolean currentlyEnrolled = args.getBoolean("from_enrolledEvents", false);
//        boolean wonLottery = args.getBoolean("won_lottery", false);
//        String type = args.getString("waitlist_notif");
//        if (type == null)
//            type = "no_type";

        String userStatus = findUserStatus(deviceId, eventToDisplay); // returns "in_waitlist", "enrolled_in_event", "not_in_waitlist_or_event", or "won_lottery"

        // set the buttons based on the status of the user, as given to us by entrantMainFragment's bundle
        if (Objects.equals(userStatus, "in_waitlist")) {
            Log.d("TAG", "user is in the WAITLIST for: " + eventToDisplay.getEventTitle());
            leaveWaitlistButton.setVisibility(View.VISIBLE); // set leavewaitlist to visible

            // If user is also invited, hide the "Joined!" button and show the accept invitation button
            boolean isInvited = eventToDisplay.getInvitedEntrants() != null &&
                               eventToDisplay.getInvitedEntrants().contains(deviceId);

            if (isInvited) {
                acceptWaitListInvitationButton.setVisibility(View.GONE);
                joinEventButton.setVisibility(View.VISIBLE); // Show accept invitation button
                declineInvitationButton.setVisibility(View.VISIBLE); // Show decline invitation button
            } else {
                acceptWaitListInvitationButton.setText("Joined!"); // set accept to joined
                acceptWaitListInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));
                joinEventButton.setVisibility(View.GONE);
                declineInvitationButton.setVisibility(View.GONE);
            }

            userWaitListStatus.setText(R.string.waitlist_status_registered);
            leaveEventButton.setVisibility(View.GONE);
        }
        else if (Objects.equals(userStatus, "enrolled_in_event")){
            Log.d("TAG", "user is enrolled in the EVENT for: " + eventToDisplay.getEventTitle());
            acceptWaitListInvitationButton.setVisibility(View.GONE);
            leaveWaitlistButton.setVisibility(View.INVISIBLE);
            userWaitListStatus.setText(R.string.enrolledInEvent);
            joinEventButton.setVisibility(View.GONE);
        }
        else if (Objects.equals(userStatus, "won_lottery")){
            Log.d("TAG", "user is invited by the LOTTERY to join the event for:  " + eventToDisplay.getEventTitle());
            acceptWaitListInvitationButton.setVisibility(View.GONE);
            leaveWaitlistButton.setVisibility(View.GONE);
            leaveEventButton.setVisibility(View.GONE);

        }
//        else if (type.equals("waitlist_left")){
//            // if the click came from the notifications fragment that deals with leaving the event waitlist
//            joinEventButton.setVisibility(View.GONE);
//            leaveEventButton.setVisibility(View.GONE);
//            leaveWaitlistButton.setVisibility(View.GONE);
//        }
//        else if (type.equals("waitlist_joined")){
//            // if the click came from the notifications fragment that deals with leaving the event waitlist
//            joinEventButton.setVisibility(View.GONE);
//            leaveEventButton.setVisibility(View.GONE);
////            acceptWaitListInvitationButton.se
////
//        }
        else{
            leaveWaitlistButton.setVisibility(View.INVISIBLE);
            userWaitListStatus.setVisibility(View.INVISIBLE);
            leaveEventButton.setVisibility(View.GONE);
            joinEventButton.setVisibility(View.GONE);
        }
    }

    /**
     * (Author: <b>David Tyrrell</b> — Nov 20, 2025)
     * Finds user status of the event, ex) currently in waitlist, enrolled in event, etc
     * @param deviceId The device ID of the user
     * @param eventToDisplay The event to display
     * @return String: The user's status in the event
     *
     */
    // TODO: I think we can get rid of the notification type attribute,
    //  here is where I check what the user status is, and I setButtons based on this status, not the notif attribute
    public String findUserStatus(String deviceId, Event eventToDisplay){
        eventToDisplay = eventsList.getEventByID(eventToDisplay.getEventID()); // update eventToDisplay, it may not have been updated by this point?
        assert (eventToDisplay != null);

        if (eventToDisplay.getWaitingEntrants().contains(deviceId)) {
            return "in_waitlist";
        }
        else if (eventToDisplay.getEnrolledEntrants().contains(deviceId)) {
            return "enrolled_in_event";
        }

        else if (eventToDisplay.getInvitedEntrants().contains(deviceId)){
            return "won_lottery";
        }
        // TODO: if past the registration date for joining an event, display no details, then the last if statement should be join waitlist
        else {
            return "not_in_waitlist_or_event";
        }
    }


    /**
     * Author: Dan
     * Loads event from Firebase when not available in EventsList
     * @param eventId The event ID to load
     * @param view The fragment view
     */
    private void loadEventFromFirebase(String eventId, View view) {
        toolbar.setTitle("Loading...");

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            event.setEventID(documentSnapshot.getId());
                            Log.d("EventViewerFragment", "Event loaded from Firebase: " + eventId);
                            displayEvent(event, view, eventId);
                        } else {
                            showError("Event data is invalid");
                        }
                    } else {
                        showError("Event not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventViewerFragment", "Error loading event: " + e.getMessage());
                    showError("Failed to load event");
                });
    }

    /**
     * Author: Dan
     * Displays the event information in the UI
     * @param event The event to display
     * @param view The fragment view
     * @param eventId The event ID
     */
    private void displayEvent(Event event, View view, String eventId) {

        setButtons(eventId); // set the buttons based on the current status of the user

        // set title:
        toolbar.setTitle(event.getEventTitle());

        // set image using the event URL:
        if (event.getPoster() != null && event.getPoster().getData() != null) {
            String event_url = event.getPoster().getData();
            Glide.with(event_image.getContext())
                    .load(event_url)
                    .placeholder(R.drawable.shrug)
                    .error(R.drawable.shrug)
                    .into(event_image);
        } else {
            event_image.setImageResource(R.drawable.shrug);
        }

        // handle navigation back to mainEntrantView on back button click
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this).popBackStack();
                return true;
            }
            return false;
        });

        // View QR Code button
        Button viewQRCodeButton = view.findViewById(R.id.btnViewQRCode);
        viewQRCodeButton.setOnClickListener(v -> {
            // Navigate to EventQRCodeFragment with event ID
            Bundle args = new Bundle();
            args.putString(EventQRCodeFragment.ARG_EVENT_ID, eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventViewer_to_eventQRCode, args);
        });

        // Join WAITLIST OnclickListener
        assert acceptWaitListInvitationButton != null;
        acceptWaitListInvitationButton.setOnClickListener(v -> {

            // First check that the user is allowed to join the waitlist
            if (eventToDisplay.getWaitingEntrants().size() == eventToDisplay.getOptionalWaitingListSize()) {
                Toast.makeText(v.getContext(), "Waitlist is full! ", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(v.getContext(), "Accepted invitation! ", Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "Joined waitlist! ", Toast.LENGTH_SHORT).show();

            acceptWaitListInvitationButton.setText("Joined!");
            acceptWaitListInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));
            leaveWaitlistButton.setVisibility(View.VISIBLE);
            leaveWaitlistButton.setText("Leave Waitlist");
            leaveWaitlistButton.setBackgroundColor(getResources().getColor(R.color.leaving_red));
            userWaitListStatus.setVisibility(View.VISIBLE);
            userWaitListStatus.setText(R.string.waitlist_status_registered);

            eventToDisplay.addEntrantToWaitingEntrants(deviceId); // add the device ID to the waitingEntrantsList for the lottery
            getParentFragmentManager().setFragmentResult("USER_JOINED_WAITLIST", new Bundle());

            Notification notification = new Notification(
                    eventToDisplay.getEventID(),
                    eventToDisplay.getOrganizer(),
                    deviceId,
                    "You joined the waitlist for: " + eventToDisplay.getEventTitle(),
                    "waitlist_joined"
            );

            Log.d("EventViewerFragment", "ORG ID: " + eventToDisplay.getOrganizer());

            notificationManager.sendToUser(notification);
        });

        // Leave WAITLIST OnclickListener
        assert leaveWaitlistButton != null;
        leaveWaitlistButton.setOnClickListener(v -> {
//            Toast.makeText(v.getContext(), "Left waitlist! ", Toast.LENGTH_SHORT).show();

            eventToDisplay.removeEntrantFromWaitingEntrants(deviceId); // remove the device ID from the waitingEntrantsList for the lottery
            leaveWaitlistButton.setText("Left Waitlist!");
            leaveWaitlistButton.setBackgroundColor(getResources().getColor(R.color.black));
            userWaitListStatus.setVisibility(View.INVISIBLE);
            acceptWaitListInvitationButton.setText("Join Waitlist");
            acceptWaitListInvitationButton.setBackgroundColor(getResources().getColor(android.R.color.white));

//             notify EntrantMainFragment to update carousels, as the user left the list
            getParentFragmentManager().setFragmentResult("USER_LEFT_WAITLIST", new Bundle());

            Notification notification = new Notification(
                    eventToDisplay.getEventID(),
                    eventToDisplay.getOrganizer(),
                    deviceId,
                    "You left the waitlist for: " + eventToDisplay.getEventTitle(),
                    "waitlist_left"
            );

            notificationManager.sendToUser(notification);
        });

        // join EVENT OnclickListener
        assert joinEventButton != null;
        joinEventButton.setOnClickListener(v -> {

            // First check that the user is allowed to join the event
            // TODO: change this check from waitlist to event capacity checker
//            if (eventToDisplay.getWaitingEntrants().size() == eventToDisplay.getOptionalWaitingListSize()) {
//                Toast.makeText(v.getContext(), "Waitlist is full! ", Toast.LENGTH_SHORT).show();
//                return;
//            }
            
            Toast.makeText(v.getContext(), "Joined Event! ", Toast.LENGTH_SHORT).show();

            joinEventButton.setText("Joined!");
            joinEventButton.setBackgroundColor(getResources().getColor(R.color.accept_green));
            leaveEventButton.setVisibility(View.VISIBLE);
            leaveWaitlistButton.setVisibility(View.GONE); // Hide leave waitlist button after accepting
            declineInvitationButton.setVisibility(View.GONE);

            // Move user from invited to enrolled (does NOT trigger vacancy filling)
            eventToDisplay.moveEntrantFromInvitedToEnrolled(deviceId);

            getParentFragmentManager().setFragmentResult("USER_JOINED_EVENT", new Bundle());

            Notification notification = new Notification(
                    eventToDisplay.getEventID(),
                    eventToDisplay.getOrganizer(),
                    deviceId,
                    "You joined the event: " + eventToDisplay.getEventTitle()
            );

            notificationManager.sendToUser(notification);
        });
        // Leave EVENT OnclickListener
        assert leaveEventButton != null;
        leaveEventButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Left Event! ", Toast.LENGTH_SHORT).show();

            eventToDisplay.removeEntrantFromEnrolledEntrants(deviceId); // remove the device ID from the waitingEntrantsList for the lottery
            eventToDisplay.addEntrantToCancelledEntrants(deviceId); // add the device ID to the cancelledEntrants
            leaveEventButton.setText("Left event!");
            userWaitListStatus.setVisibility(View.INVISIBLE);
            acceptWaitListInvitationButton.setVisibility(View.INVISIBLE);

//             notify EntrantMainFragment to update carousels, as the user left the list
//            getParentFragmentManager().setFragmentResult("USER_LEFT_EVENT", new Bundle());

            NavHostFragment.findNavController(this).navigateUp();
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.navigation_home);


            // Owen Notification stuff
            Notification notification = new Notification(
                    eventToDisplay.getEventID(),
                    eventToDisplay.getOrganizer(),
                    deviceId,
                    "You left the event: " + eventToDisplay.getEventTitle()
            );
            notificationManager.sendToUser(notification);
        });
    }

    /**
     * Author: Dan
     * Shows an error message to the user if the event id is null
     * Basically displays a default event instead of crashing
     * @param message The error message to display
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.e("EventViewerFragment", message);
        }
        toolbar.setTitle("Error");
    }
}
