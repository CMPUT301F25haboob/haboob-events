/**
 Fragment to displays all posters for an admin
 Copyright (C) 2025  jeff

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


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
 *
 * <p>This fragment provides administrators with a visual overview of all events in
 * the system. Events are displayed in a grid layout with their poster images.
 * If an event has no poster or the poster is removed, a placeholder image is shown
 * to ensure the event remains visible to administrators.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Displays all events from Firestore in a grid layout</li>
 *   <li>Shows poster images or placeholders for each event</li>
 *   <li>Handles loading states with progress indicator</li>
 *   <li>Supports click navigation to event detail view</li>
 *   <li>Automatically refreshes when fragment becomes visible</li>
 * </ul>
 *
 * @author Jeff
 * @version 1.0
 * @see AdminPosterAdapter
 * @see Event
 * @see EventsList
 */
public class AdminPosterFragment extends Fragment implements AdminPosterAdapter.OnPosterClickListener {

    /** Tag for logging */
    private static final String TAG = "AdminPosterFragment";

    /** RecyclerView for displaying event posters in a grid */
    private RecyclerView recyclerView;

    /** Progress bar shown during data loading */
    private ProgressBar progressBar;

    /** Adapter for the RecyclerView */
    private AdminPosterAdapter adapter;

    /** List of all events loaded from Firestore */
    private List<Event> eventList;

    /** Manager for loading events from Firestore */
    private EventsList eventsListManager;

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminPosterFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment's layout and initializes all UI components.
     *
     * <p>Sets up the RecyclerView with a GridLayoutManager for two-column display,
     * initializes the EventsList manager with Firestore connectivity, and configures
     * the toolbar navigation.</p>
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

    /**
     * Called when the fragment becomes visible to the user.
     * Reloads all event data to ensure the display is up-to-date.
     *
     * <p>This ensures that any changes made to events (such as poster removal
     * or event deletion) are reflected when returning to this fragment.</p>
     */
    @Override
    public void onResume() {
        super.onResume();
        // Reload data whenever fragment becomes visible
        loadAllEvents();
    }

    /**
     * Loads ALL events from Firestore and displays them.
     *
     * <p>This method loads every event in the system, regardless of whether they
     * have a poster image. Events without posters will display a placeholder image
     * in the adapter, ensuring administrators can still see and manage all events.</p>
     *
     * <p>Shows a progress bar during loading and handles errors appropriately by
     * logging them and displaying user-friendly error messages.</p>
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

    /**
     * Called when a poster card is clicked.
     * Navigates to the detailed view for the selected event.
     *
     * <p>Passes the event ID as a navigation argument so the detail fragment
     * can load and display complete information about the event.</p>
     *
     * @param event The Event object whose poster was clicked
     */
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