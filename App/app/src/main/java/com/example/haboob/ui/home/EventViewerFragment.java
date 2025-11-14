package com.example.haboob.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.haboob.Event;
import com.example.haboob.EventQRCodeFragment;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    MaterialButton acceptInvitationButton, leaveWaitlistButton, leaveEventButton;
    TextView userWaitListStatus;

    Event eventToDisplay;
    private String currentWaitListStatus;

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
        acceptInvitationButton = view.findViewById(R.id.btnAccept);
        leaveWaitlistButton = view.findViewById(R.id.btnLeaveWaitlist);
        userWaitListStatus = view.findViewById(R.id.userWaitListStatus);
        leaveEventButton = view.findViewById(R.id.btnLeaveEvent);



        assert getArguments() != null;
        Bundle args = getArguments();

        setButtons(args); // sets buttons based on the current waitlist status

        // unpack the bundle:
        String eventId = requireArguments().getString(ARG_EVENT_ID);

        // grab the details of event:
        EventsList eventsList = ((MainActivity) getActivity()).getEventsList();
        eventToDisplay = eventsList.getEventByID(eventId);

        assert eventId != null;

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
     * @param args the fragment arguments bundle (must include {@link #ARG_EVENT_ID})
     */
    public void setButtons(Bundle args){
        //TODO: set the buttons based on the current waitlist status
        assert args != null;
        String eventId = args.getString(ARG_EVENT_ID);

        EventsList eventsList = new EventsList();
        Event eventToDisplay = eventsList.getEventByID(eventId);

        boolean currentlyInWaitlist = args.getBoolean("in_waitlist", false);
        boolean currentlyEnrolled = args.getBoolean("from_enrolledEvents", false);

        if (currentlyInWaitlist) {
            leaveWaitlistButton.setVisibility(View.VISIBLE); // set leavewaitlist to visible
            acceptInvitationButton.setText("Joined!"); // set accept to joined
            acceptInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));
            userWaitListStatus.setText(R.string.waitlist_status_registered);
            leaveEventButton.setVisibility(View.GONE);
        }
        else if (currentlyEnrolled){
            acceptInvitationButton.setVisibility(View.GONE);
            leaveWaitlistButton.setVisibility(View.INVISIBLE);
            userWaitListStatus.setText(R.string.enrolledInEvent);
        }
        else{
            leaveWaitlistButton.setVisibility(View.INVISIBLE);
            userWaitListStatus.setVisibility(View.INVISIBLE);
        }



//        acceptInvitationButton.setVisibility(fromWaitListCarousel ? View.GONE : View.VISIBLE);
//        boolean currentlyInWaitlist = args.getBoolean("in_waitlist", false);


//        currentWaitListStatus = eventToDisplay.getWaitlist_status();

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
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_home);
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

        // set an onClicklistener for accepting joining the waitlist
        assert acceptInvitationButton != null;
        acceptInvitationButton.setOnClickListener(v -> {

            // First check that the user is allowed to join the waitlist
            if (eventToDisplay.getWaitingEntrants().size() == eventToDisplay.getOptionalWaitingListSize()) {
                Toast.makeText(v.getContext(), "Waitlist is full! ", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(v.getContext(), "Accepted invitation! ", Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "Joined waitlist! ", Toast.LENGTH_SHORT).show();

            acceptInvitationButton.setText("Joined!");
            acceptInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));
            leaveWaitlistButton.setVisibility(View.VISIBLE);
            leaveWaitlistButton.setText("Leave Waitlist");
            leaveWaitlistButton.setBackgroundColor(getResources().getColor(R.color.leaving_red));
            userWaitListStatus.setVisibility(View.VISIBLE);
            userWaitListStatus.setText(R.string.waitlist_status_registered);

            eventToDisplay.addEntrantToWaitingEntrants(deviceId); // add the device ID to the waitingEntrantsList for the lottery
            getParentFragmentManager().setFragmentResult("USER_JOINED_WAITLIST", new Bundle());

        });

        // Leave WAITLIST OnclickListener
        assert leaveWaitlistButton != null;
        leaveWaitlistButton.setOnClickListener(v -> {
//            Toast.makeText(v.getContext(), "Left waitlist! ", Toast.LENGTH_SHORT).show();

            eventToDisplay.removeEntrantFromWaitingEntrants(deviceId); // remove the device ID from the waitingEntrantsList for the lottery
            leaveWaitlistButton.setText("Left Waitlist!");
            leaveWaitlistButton.setBackgroundColor(getResources().getColor(R.color.black));
            userWaitListStatus.setVisibility(View.INVISIBLE);
            acceptInvitationButton.setText("Join Waitlist");

//             notify EntrantMainFragment to update carousels, as the user left the list
            getParentFragmentManager().setFragmentResult("USER_LEFT_WAITLIST", new Bundle());

        });

        // Leave EVENT OnclickListener
        assert leaveEventButton != null;
        leaveEventButton.setOnClickListener(v -> {
//            Toast.makeText(v.getContext(), "Left waitlist! ", Toast.LENGTH_SHORT).show();

            eventToDisplay.removeEntrantFromEnrolledEntrants(deviceId); // remove the device ID from the waitingEntrantsList for the lottery
            leaveEventButton.setText("Left event!");
            userWaitListStatus.setVisibility(View.INVISIBLE);
            acceptInvitationButton.setVisibility(View.INVISIBLE);

//             notify EntrantMainFragment to update carousels, as the user left the list
            getParentFragmentManager().setFragmentResult("USER_LEFT_EVENT", new Bundle());

//            NavHostFragment.findNavController(this).navigateUp();
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.navigation_home);

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
