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

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying all events in the system for administrative management.
 * Shows a grid of events with their posters (or a placeholder if no poster exists).
 */
public class AdminPosterFragment extends Fragment implements AdminPosterAdapter.OnPosterClickListener {

    private static final String TAG = "AdminPosterFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AdminPosterAdapter adapter;
    private List<Event> eventList;
    private EventsList eventsListManager;

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

        // Initialize EventsList with inMemoryOnly = false to connect to Firestore
        eventsListManager = new EventsList(false);

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data whenever fragment becomes visible
        loadAllEvents();
    }

    /**
     * Loads ALL events from Firestore.
     * Events without posters will simply show the placeholder image in the adapter.
     */
    private void loadAllEvents() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        eventsListManager.loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                eventList.clear();

                // CHANGED: We now add ALL events, regardless of whether they have a poster.
                // This ensures events remain visible (with a placeholder) after their poster is removed.
                for (Event event : eventsListManager.getEventsList()) {
                    if (event != null) {
                        eventList.add(event);
                    }
                }

                if (adapter != null) adapter.notifyDataSetChanged();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Total events loaded: " + eventList.size());
            }

            @Override
            public void onError(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading events: " + e.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPosterClick(Event event) {
        String eventId = event.getEventID();
        if (eventId == null) return;

        Log.d(TAG, "Navigating to detail for Event ID: " + eventId);

        Bundle bundle = new Bundle();
        bundle.putString("poster_id", eventId);

        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_admin_poster_detail, bundle);
    }
}