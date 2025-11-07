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

// Helper class to simulate the object returned by Poster.getImageSource().
// This class must be defined here so the MockPoster can use it.
class MockImageObject {
    public final int imageResId;
    public MockImageObject(int imageResId) { this.imageResId = imageResId; }
}


/**
 * Fragment to display a filtered list of events (only those with posters) for admin management.
 */
public class AdminPosterFragment extends Fragment implements AdminPosterAdapter.OnPosterClickListener {

    private static final String TAG = "AdminPosterFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AdminPosterAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    private EventsList eventsListManager;
    // Helper class to mock the Poster methods required by the Adapter
    // NOTE: This assumes your actual Poster class returns an Object for getImageSource().
    private static class MockPoster extends Poster {
        private final MockImageObject imageSource;

        public MockPoster(String posterId, int imageResId) {
            super(posterId);
            this.imageSource = new MockImageObject(imageResId);
        }

        // Mock implementation of the required method, returning the mock picture object
        public Object getImageSource() {
            return imageSource;
        }
    }


    public AdminPosterFragment() {
        // Required empty public constructor
    }

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

        // **FIX: Ensure this back navigation listener is correctly set.**
        // **FIX: Use navigateUp() for the definitive "Up" navigation action.**
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

        // FIX: Start loading data from Firestore using the EventsList constructor with a listener.
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
     * Loads the list of events, but only includes those that have a Poster attached.
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
     * Handles the click event for an individual event poster item.
     * Navigates to the AdminPosterDetailFragment, passing the Event ID.
     * @param event The Event object whose poster was clicked.
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