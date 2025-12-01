package com.example.haboob;

import static android.view.View.INVISIBLE;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * {@code OrganizerOptionsFragment} is the main fragment displayed for organizers
 * after logging in. It allows organizers to:
 * <ul>
 *     <li>View all events they have created</li>
 *     <li>Create new events</li>
 *     <li>Edit event posters</li>
 *     <li>View event entrant lists</li>
 *     <li>Draw lotteries for events with randomized selection</li>
 * </ul>
 *
 * <p>This fragment communicates with the {@link OrganizerMainActivity} to access the
 * currently logged-in {@link Organizer} and their associated {@link EventsList}.
 * It also handles loading event data from Firestore and refreshing the UI when new
 * events are added or edited.</p>
 */
public class OrganizerOptionsFragment extends Fragment {

    // ---------- UI COMPONENTS ----------
    /** ListView that displays all events created by the organizer. */
    private ListView organizerUpcomingEventsView;
    private ListView organizerCurrentEventsView;

    /** List of events belonging to the organizer. */
    private EventsList organizerEventsList;
    private ArrayList<Event> organizerEvents;
    private ArrayList<Event> organizerUpcomingEvents;
    private ArrayList<Event> organizerCurrentEvents;

    /** Adapter that connects event titles to the ListView. */
    private EventAdapter organizerUpcomingEventsAdapter;
    private EventAdapter organizerCurrentEventsAdapter;

    /** Keeps track of which event (position) was clicked in the list. */
    private int selectedPosition = -1;

    // ---------- STATE ----------
    /** The currently active organizer (retrieved from OrganizerMainActivity). */
    private Organizer currentOrganizer;

    /** The event currently selected by the user in the ListView. */
    private Event clickedEvent;
    private Date date;
    // NOTE: Can check LogCat to help debug processes


    /**
     * Inflates the organizer options UI, initializes components, and sets up event button logic.
     *
     * @param inflater  LayoutInflater for inflating views
     * @param container Parent view group
     * @param savedInstanceState Saved state bundle
     * @return The inflated fragment view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_screen, container, false);

        // Retrieve the organizer from the parent activity
        OrganizerMainActivity parent = (OrganizerMainActivity) getActivity();
        if (parent == null) {
            Log.e("OrganizerOptions", "Parent activity is null");
            return view;
        }

        currentOrganizer = parent.getCurrentOrganizer();
        if (currentOrganizer == null) {
            Log.e("OrganizerOptions", "Current organizer is null");
            Toast.makeText(getContext(), "Error: Organizer not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        Log.d("OrganizerOptions", "Organizer (" + currentOrganizer.getOrganizerID() + ") loaded successfully");

        // Initialize local lists
        organizerUpcomingEvents = new ArrayList<>();
        organizerCurrentEvents = new ArrayList<>();


        // Attach adapter to the ListViews using custom CardView row
        // Create and set adapters using custom EventAdapter (To be able to add the red dot)
        organizerCurrentEventsAdapter = new EventAdapter(getContext(), organizerCurrentEvents);
        organizerUpcomingEventsAdapter = new EventAdapter(getContext(), organizerUpcomingEvents);

        organizerCurrentEventsView = view.findViewById(R.id.organizer_events);
        organizerCurrentEventsView.setAdapter(organizerCurrentEventsAdapter);

        organizerUpcomingEventsView = view.findViewById(R.id.upcoming_events_list);
        organizerUpcomingEventsView.setAdapter(organizerUpcomingEventsAdapter);

        // Retrieve buttons
        Button createEventButton = view.findViewById(R.id.create_event);
        Button editPosterButton = view.findViewById(R.id.edit_poster_button);
        Button viewListsButton = view.findViewById(R.id.view_lists_button);
        Button drawLotteryButton = view.findViewById(R.id.draw_lottery_button);

        // Hide event action buttons until an event is selected
        editPosterButton.setVisibility(INVISIBLE);
        viewListsButton.setVisibility(INVISIBLE);
        drawLotteryButton.setVisibility(INVISIBLE);

        organizerUpcomingEventsView.setOnItemClickListener((parent1, view1, position, id) -> {
            clickedEvent = organizerUpcomingEvents.get(position);

            // Highlight in upcoming list
            organizerUpcomingEventsAdapter.setSelectedIndex(position);
            organizerUpcomingEventsAdapter.notifyDataSetChanged();

            // Clear highlight in current events list
            organizerCurrentEventsAdapter.setSelectedIndex(-1);
            organizerCurrentEventsAdapter.notifyDataSetChanged();

            editPosterButton.setVisibility(View.VISIBLE);
            viewListsButton.setVisibility(View.VISIBLE);
            drawLotteryButton.setVisibility(View.VISIBLE);
        });

        organizerCurrentEventsView.setOnItemClickListener((parent1, view1, position, id) -> {
            clickedEvent = organizerCurrentEvents.get(position);

            // Highlight in current list
            organizerCurrentEventsAdapter.setSelectedIndex(position);
            organizerCurrentEventsAdapter.notifyDataSetChanged();

            // Clear highlight in upcoming events list
            organizerUpcomingEventsAdapter.setSelectedIndex(-1);
            organizerUpcomingEventsAdapter.notifyDataSetChanged();

            editPosterButton.setVisibility(View.VISIBLE);
            viewListsButton.setVisibility(View.VISIBLE);
            drawLotteryButton.setVisibility(View.VISIBLE);
        });


        // --- Button Logic ---

        // Create new event
        createEventButton.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerNewEventFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Edit event poster
        editPosterButton.setOnClickListener(v -> {
            if (clickedEvent == null) {
                Toast.makeText(requireContext(), "Please select an event first", Toast.LENGTH_SHORT).show();
                return;
            }

            OrganizerEditPosterFragment editPosterFragment = new OrganizerEditPosterFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", clickedEvent);
            editPosterFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, editPosterFragment)
                    .addToBackStack(null)
                    .commit();
        });


        // View entrant lists for selected event
        viewListsButton.setOnClickListener(v -> {
            OrganizerAllListsFragment allListsFragment = new OrganizerAllListsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("event", clickedEvent);
            allListsFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, allListsFragment)
                    .addToBackStack(null)
                    .commit();

        });

        // Draw lottery for selected event
        drawLotteryButton.setOnClickListener(v -> {

            date = new Date();
            if (clickedEvent.getRegistrationEndDate().after(date)) {
                Toast.makeText(getContext(), "Cannot draw yet, registration has not closed", Toast.LENGTH_LONG).show();
                return;
            }
            LotterySampler sampler  = new LotterySampler(new NotificationManager());
            try {
                sampler.performLottery(clickedEvent);
                Toast.makeText(parent, "Lottery Drawn!", Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(parent, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // Refresh the UI (Adapters)
            organizerUpcomingEventsAdapter.notifyDataSetChanged();
            organizerCurrentEventsAdapter.notifyDataSetChanged();
        });

        // Load events for the organizer
        try {
            loadEventsFromFirestore();
        } catch (Exception e) {
            Log.e("OrganizerOptions", "Error loading events", e);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    /**
     * Loads the organizer’s events from Firestore into memory.
     * <p>
     * If events are already loaded, it immediately refreshes the UI. Otherwise,
     * it registers an {@link EventsList.OnEventsLoadedListener} to wait until
     * Firestore loading completes.
     */
    private void loadEventsFromFirestore() {

        if (currentOrganizer == null) {
            Log.e("OrganizerOptions", "currentOrganizer is null in loadEventsFromFirestore");
            return;
        }

        if (currentOrganizer.getEventList() == null) {
            Log.e("OrganizerOptions", "EventList is null! Check your Organizer class initialization");
            Toast.makeText(getContext(), "Error: Event list not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentOrganizer.getEventList().isLoaded()) {
            refreshEventList();
            return;
        }

        Log.d("OrganizerOptions", "Events not loaded yet, waiting for Firestore...");

        currentOrganizer.getEventList().loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                Log.d("OrganizerOptions", "onEventsLoaded callback triggered");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(OrganizerOptionsFragment.this::refreshEventList);
                } else {
                    Log.e("OrganizerOptions", "Activity is null in onEventsLoaded");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("OrganizerOptions", "Error loading events", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Failed to load events: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Forces a full reload of all organizer events from Firestore.
     * <p>
     * This method is called in {@link #onResume()} to ensure the list is up to date
     * after returning from creating or editing an event.
     */
    private void reloadEventsFromFirestore() {
        Log.d("OrganizerOptions", "reloadEventsFromFirestore called - forcing fresh data");

        if (currentOrganizer == null || currentOrganizer.getEventList() == null) {
            return;
        }

        currentOrganizer.getEventList().loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                Log.d("OrganizerOptions", "Fresh data loaded from Firestore");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(OrganizerOptionsFragment.this::refreshEventList);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("OrganizerOptions", "Error reloading events", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(),
                                    "Failed to reload events: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Called when the fragment becomes visible again.
     * <p>
     * Ensures that any newly created or modified events are reflected
     * in the organizer’s list by calling {@link #reloadEventsFromFirestore()}.
     */
    @Override
    public void onResume() {
        super.onResume();
        reloadEventsFromFirestore();
    }

    /**
     * Refreshes the event list UI using the latest data from {@link EventsList}.
     * <p>
     * Filters events belonging to the current organizer, updates displayed event
     * titles, and notifies the adapter to refresh the {@link ListView}.
     */
    private void refreshEventList() {
        Log.d("OrganizerOptions", "refreshEventList called");

        if (currentOrganizer == null || currentOrganizer.getEventList() == null) {
            Log.e("OrganizerOptions", "Organizer or EventList is null during refresh");
            return;
        }

        try {
            organizerEventsList = currentOrganizer.getEventList();
            organizerEvents = organizerEventsList.getOrganizerEvents(currentOrganizer.getOrganizerID());

            Log.d("OrganizerOptions", "Found " + organizerEvents.size() + " events for organizer");

            // Get the filtered lists
            ArrayList<Event> upcomingEvents = organizerEventsList.getOrganizerUpcomingEvents(organizerEvents);
            ArrayList<Event> currentEvents = organizerEventsList.getOrganizerCurrentEvents(organizerEvents);

            // IMPORTANT: Clear and repopulate the existing lists (don't create new ones!)
            organizerUpcomingEvents.clear();
            organizerUpcomingEvents.addAll(upcomingEvents);

            organizerCurrentEvents.clear();
            organizerCurrentEvents.addAll(currentEvents);

            if (organizerUpcomingEventsAdapter != null && organizerCurrentEventsAdapter != null) {
                organizerUpcomingEventsAdapter.notifyDataSetChanged();
                organizerCurrentEventsAdapter.notifyDataSetChanged();
                Log.d("OrganizerOptions", "Adapters refreshed -> current: " + organizerCurrentEvents.size() + ", upcoming: " + organizerUpcomingEvents.size());
            } else {
                Log.e("OrganizerOptions", "Null Adapter(s)!");
            }
        } catch (Exception e) {
            Log.e("OrganizerOptions", "Error in refreshEventList", e);
            Toast.makeText(getContext(),
                    "Error refreshing events: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
