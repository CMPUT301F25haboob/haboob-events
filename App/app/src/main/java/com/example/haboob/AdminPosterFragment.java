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
 * Fragment to display a filtered list of events (only those with posters) for admin management.
 */
public class AdminPosterFragment extends Fragment implements AdminPosterAdapter.OnPosterClickListener {

    private static final String TAG = "AdminPosterFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AdminPosterAdapter adapter;
    // The list now stores Events, but filtered to those with posters
    private List<Event> eventList;
    private FirebaseFirestore db;

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

        // Setup Toolbar Navigation
        toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        // Setup RecyclerView
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

        // Initialize list and adapter with Event type
        eventList = new ArrayList<>();
        adapter = new AdminPosterAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadEventsWithPosters();

        return view;
    }

    /**
     * Loads the list of events, but only includes those that have a Poster attached.
     */
    private void loadEventsWithPosters() {
        progressBar.setVisibility(View.VISIBLE);

        // --- MOCK DATA FOR DEMONSTRATION ---
        // Create a Poster object to attach to events
        Poster mockPoster = new Poster("P1");

        // Mock Events with IDs matching the map in AdminPosterAdapter.java
        // These IDs trigger specific drawable loading in the adapter
        Event mockEvent1 = new Event(); mockEvent1.setEventID("E1_HOCKEY"); mockEvent1.setEventTitle("Youth Hockey Tournament"); mockEvent1.setPoster(mockPoster);
        Event mockEvent2 = new Event(); mockEvent2.setEventID("E2_SWIM"); mockEvent2.setEventTitle("Local Swimming Meet"); mockEvent2.setPoster(mockPoster);
        Event mockEvent3 = new Event(); mockEvent3.setEventID("E3_ART"); mockEvent3.setEventTitle("Bob Ross Painting Class"); mockEvent3.setPoster(mockPoster);
        Event mockEvent4 = new Event(); mockEvent4.setEventID("E4_CLASH"); mockEvent4.setEventTitle("Clash Royale Esports Open"); mockEvent4.setPoster(mockPoster);
        Event mockEvent5 = new Event(); mockEvent5.setEventID("E5_NOPOSTER"); mockEvent5.setEventTitle("Board Game Night"); // No Poster

        // Filter logic: Only add events with a poster object.
        if (mockEvent1.getPoster() != null) eventList.add(mockEvent1);
        if (mockEvent2.getPoster() != null) eventList.add(mockEvent2);
        if (mockEvent3.getPoster() != null) eventList.add(mockEvent3);
        if (mockEvent4.getPoster() != null) eventList.add(mockEvent4);
        // mockEvent5 is filtered out because getPoster() returns null.

        // --- END MOCK DATA ---

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "Events with Posters loaded: " + eventList.size());
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

        // You must replace R.id.navigation_admin_poster_detail with the actual ID
        // of your AdminPosterDetailFragment destination in your mobile_navigation.xml
        // NavHostFragment.findNavController(this).navigate(R.id.navigation_admin_poster_detail, bundle);

        Log.d(TAG, "Attempting to navigate to detail for Event ID: " + eventId);
    }
}