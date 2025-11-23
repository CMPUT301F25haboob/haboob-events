package com.example.haboob;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.ui.home.EventViewerFragment;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * HistoryFragment displays all events the user has ever joined a waitlist for
 * Author: Dan
 */
public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private MaterialToolbar toolbar;

    private String deviceId;
    private EventsList eventsList;
    private List<Event> historyEvents;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Get device ID
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Initialize views
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        toolbar = view.findViewById(R.id.topAppBar);

        // Set up RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(new ArrayList<>(), eventId -> {
            // Navigate to EventViewerFragment when an event is clicked
            Bundle args = new Bundle();
            args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.entrant_event_view, args);
        });
        historyRecyclerView.setAdapter(historyAdapter);

        // Set up back button in toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.profile_fragment);
                return true;
            }
            return false;
        });

        // Load history events
        loadHistoryEvents();

        return view;
    }

    /**
     * Loads all events the user has joined waitlists for
     * TODO: Logic needs to be reworked. Instead, when a user joins an event, it should be added to the history
     * TODO: This is handled elsewhere. We will also need a new field for the history which will store event ids
     */
    private void loadHistoryEvents() {
        eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                // Get all events where user has been in any list
                ArrayList<Event> allEvents = eventsList.getEventsList();
                historyEvents = new ArrayList<>();

                for (Event event : allEvents) {
                    if (event == null) continue;

                    // Check if user was ever in waiting list
                    boolean inWaiting = event.getWaitingEntrants() != null &&
                                       event.getWaitingEntrants().contains(deviceId);

                    // Check if user was ever enrolled
                    boolean inEnrolled = event.getEnrolledEntrants() != null &&
                                        event.getEnrolledEntrants().contains(deviceId);

                    // Check if user was ever invited
                    boolean inInvited = event.getInvitedEntrants() != null &&
                                       event.getInvitedEntrants().contains(deviceId);

                    // Check if user was ever cancelled
                    boolean inCancelled = event.getCancelledEntrants() != null &&
                                         event.getCancelledEntrants().contains(deviceId);

                    // If user was in any list, add to history
                    if (inWaiting || inEnrolled || inInvited || inCancelled) {
                        historyEvents.add(event);
                    }
                }

                Log.d(TAG, "Found " + historyEvents.size() + " events in history");

                // Update adapter with history events
                updateAdapter();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading history events", e);
                Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the RecyclerView adapter with events
     */
    private void updateAdapter() {
        historyAdapter.updateEvents(historyEvents);
    }
}
