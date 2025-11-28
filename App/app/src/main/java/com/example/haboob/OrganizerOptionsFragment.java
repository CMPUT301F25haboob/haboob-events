package com.example.haboob;

import static android.view.View.INVISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Date;

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
    private ListView organizerEventsView;

    /** List of events belonging to the organizer. */
    private ArrayList<Event> organizerEvents;

    /** List of event titles (displayed in the ListView). */
    private ArrayList<String> eventNames;

    /** Adapter that connects event titles to the ListView. */
    private ArrayAdapter<String> organizerEventsAdapter;

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
        organizerEvents = new ArrayList<>();
        eventNames = new ArrayList<>();

        // Attach adapter to the ListView using custom CardView row
        organizerEventsAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.organizer_event_item, // CardView row XML
                R.id.tv_event_name, // TextView inside that layout
                eventNames
        );
        organizerEventsView = view.findViewById(R.id.organizer_events);
        organizerEventsView.setAdapter(organizerEventsAdapter);

        // Retrieve buttons
        Button createEventButton = view.findViewById(R.id.create_event);
        Button editPosterButton = view.findViewById(R.id.edit_poster_button);
        Button viewListsButton = view.findViewById(R.id.view_lists_button);
        Button drawLotteryButton = view.findViewById(R.id.draw_lottery_button);

        // Hide event action buttons until an event is selected
        editPosterButton.setVisibility(INVISIBLE);
        viewListsButton.setVisibility(INVISIBLE);
        drawLotteryButton.setVisibility(INVISIBLE);

        // --- Event Selection Logic ---
        organizerEventsView.setOnItemClickListener((parent1, view1, position, id) -> {
            clickedEvent = organizerEvents.get(position);
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
            // TODO: Just calls a function (Dan made?)
            date = new Date();
            if (clickedEvent.getRegistrationEndDate().after(date)) {
                Toast.makeText(getContext(), "Cannot draw yet, registration has not closed", Toast.LENGTH_LONG).show();
                return;
            }
            LotterySampler sampler  = new LotterySampler();
            try {
                sampler.performLottery(clickedEvent);
            } catch (IllegalArgumentException e) {
                Toast.makeText(parent, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
            organizerEvents = currentOrganizer.getEventList()
                    .getOrganizerEvents(currentOrganizer.getOrganizerID());

            Log.d("OrganizerOptions", "Found " + organizerEvents.size() + " events for organizer");

            eventNames.clear();
            for (Event e : organizerEvents) {
                if (e != null && e.getEventTitle() != null) {
                    eventNames.add(e.getEventTitle());
                } else {
                    Log.w("OrganizerOptions", "Found null event or event title");
                }
            }

            if (organizerEventsAdapter != null) {
                organizerEventsAdapter.notifyDataSetChanged();
                Log.d("OrganizerOptions", "Adapter updated with " + eventNames.size() + " event names");
            } else {
                Log.e("OrganizerOptions", "Adapter is null!");
            }
        } catch (Exception e) {
            Log.e("OrganizerOptions", "Error in refreshEventList", e);
            Toast.makeText(getContext(),
                    "Error refreshing events: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
