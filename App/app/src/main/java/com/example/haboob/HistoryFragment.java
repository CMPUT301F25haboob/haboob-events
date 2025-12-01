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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays all events the user has ever joined a waitlist for.
 * This fragment retrieves the user's event history from Firestore and displays
 * it in a RecyclerView with event details and images.
 *
 * @author Dan
 * @version 1.0
 */
public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private MaterialToolbar toolbar;

    private String deviceId;
    private EventsList eventsList;
    private List<Event> historyEvents;

    /**
     * Required empty public constructor.
     */
    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes all UI components and sets up the RecyclerView for displaying history.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that this fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment
     * @return The View for the fragment's UI
     */
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
     * Loads all events from the user's event_history_list from Firestore.
     * First retrieves the list of event IDs from the user's entrant document,
     * then loads the full event details for each ID.
     */
    private void loadHistoryEvents() {
        historyEvents = new ArrayList<>();

        // First, get the user's event_history_list from the entrant collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrant")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> eventHistoryList = (List<String>) documentSnapshot.get("event_history_list");

                        if (eventHistoryList == null || eventHistoryList.isEmpty()) {
                            Log.d(TAG, "No events in history");
                            updateAdapter();
                            return;
                        }

                        Log.d(TAG, "Found " + eventHistoryList.size() + " events in history list");

                        // Load EventsList to get event details
                        eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
                            @Override
                            public void onEventsLoaded() {
                                // Get events matching the IDs in the history list
                                for (String eventId : eventHistoryList) {
                                    Event event = eventsList.getEventByID(eventId);
                                    if (event != null) {
                                        historyEvents.add(event);
                                    }
                                }

                                Log.d(TAG, "Loaded " + historyEvents.size() + " event details");
                                updateAdapter();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error loading events list", e);
                                Toast.makeText(getContext(), "Failed to load event details: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.d(TAG, "User document not found");
                        updateAdapter();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user history", e);
                    Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the RecyclerView adapter with the loaded history events.
     */
    private void updateAdapter() {
        historyAdapter.updateEvents(historyEvents);
    }
}