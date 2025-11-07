package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying a filtered list of events with posters for administrative management.
 *
 * <p>This fragment:</p>
 * <ul>
 *   <li>Displays only events that have associated poster images</li>
 *   <li>Uses a grid layout (2 columns) for poster display</li>
 *   <li>Loads event data asynchronously from Firestore</li>
 *   <li>Provides navigation back to the admin main screen</li>
 *   <li>Handles click events on individual posters</li>
 * </ul>
 *
 * <p>The fragment uses {@link EventsList} to manage event data and filters
 * out events that don't have a Poster object attached.</p>
 *
 * @author Haboob Team
 * @version 1.0
 * @see Fragment
 * @see AdminPosterAdapter
 * @see EventsList
 */
public class AdminPosterFragment extends Fragment implements AdminPosterAdapter.OnPosterClickListener {

    /** Tag for logging purposes */
    private static final String TAG = "AdminPosterFragment";

    /** RecyclerView for displaying the grid of posters */
    private RecyclerView recyclerView;

    /** ProgressBar shown while data is loading */
    private ProgressBar progressBar;

    /** Adapter for binding event data to the RecyclerView */
    private AdminPosterAdapter adapter;

    /** List of events with posters to be displayed */
    private List<Event> eventList;

    /** Firestore database instance for data access */
    private FirebaseFirestore db;

    /** EventsList manager for loading and managing event data */
    private EventsList eventsListManager;

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminPosterFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment's layout and initializes all UI components and data loading.
     *
     * <p>This method sets up:</p>
     * <ul>
     *   <li>RecyclerView with GridLayoutManager (2 columns)</li>
     *   <li>Toolbar with back navigation</li>
     *   <li>Progress bar for loading indication</li>
     *   <li>Firestore connection</li>
     *   <li>EventsList loading with callback</li>
     * </ul>
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_poster_fragment, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_admin_posters);
        progressBar = view.findViewById(R.id.progress_bar_admin_posters);
        MaterialToolbar toolbar = view.findViewById(R.id.posterTopAppBar);

        // Setup Firestore
        db = FirebaseFirestore.getInstance();

        eventsListManager = new EventsList(true);

        // Back navigation listener
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_admin);
                return true;
            }

            return false;
        });

        // Setup RecyclerView
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

        // Initialize list and adapter with Event type
        eventList = new ArrayList<>();
        adapter = new AdminPosterAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        // Start loading data from Firestore using the EventsList constructor with a listener
        progressBar.setVisibility(View.VISIBLE);

        eventsListManager = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                // Data loading succeeded. Filter and update UI on the main thread.
                eventList.clear();

                // Filter logic: Only add events that have a Poster object (not null).
                for (Event event : eventsListManager.getEventsList()) {
                    if (event.getPoster() != null) {
                        eventList.add(event);
                    }
                }

                // Update UI
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Events with Posters loaded: " + eventList.size());
            }

            @Override
            public void onError(Exception e) {
                // Handle the loading failure
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading events: " + e.getMessage());
                Toast.makeText(getContext(), "Error loading posters: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    /**
     * Loads the list of events from Firestore, filtering to include only events with posters.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Shows a progress bar while loading</li>
     *   <li>Uses EventsList to fetch data asynchronously</li>
     *   <li>Filters events to include only those with non-null Poster objects</li>
     *   <li>Updates the RecyclerView adapter with filtered results</li>
     *   <li>Hides the progress bar when complete</li>
     * </ol>
     *
     * <p>If an error occurs during loading, a Toast message is displayed to the user.</p>
     */
    private void loadEventsWithPosters() {
        progressBar.setVisibility(View.VISIBLE);

        // Use EventsList to load data asynchronously
        eventsListManager.loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                // 1. Clear the current list
                eventList.clear();

                // 2. Iterate over all events loaded from Firestore
                for (Event event : eventsListManager.getEventsList()) {
                    // 3. Filter logic: Only add events that have a Poster object (not null).
                    if (event.getPoster() != null) {
                        eventList.add(event);
                    }
                }

                // 4. Update UI
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Events with Posters loaded: " + eventList.size());
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading events: " + e.getMessage());
                Toast.makeText(getContext(), "Error loading posters.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles click events on individual poster cards.
     *
     * <p>When a poster is clicked, this method:</p>
     * <ul>
     *   <li>Displays a Toast with the event title</li>
     *   <li>Logs the navigation attempt</li>
     *   <li>Prepares a Bundle with the event ID</li>
     *   <li>Would navigate to detail view (currently commented out)</li>
     * </ul>
     *
     * @param event The Event object whose poster was clicked
     * :TODO Uncomment navigation code to enable detail view navigation
     */
    @Override
    public void onPosterClick(Event event) {
        String eventId = event.getEventID();

        Toast.makeText(getContext(), "Reviewing Poster for Event: " + event.getEventTitle(), Toast.LENGTH_SHORT).show();

        // Navigation logic to the detail fragment
        Bundle bundle = new Bundle();
        bundle.putString("poster_id", eventId);

        // NavHostFragment.findNavController(this).navigate(R.id.navigation_admin_poster_detail, bundle);

        Log.d(TAG, "Attempting to navigate to detail for Event ID: " + eventId);
    }
}