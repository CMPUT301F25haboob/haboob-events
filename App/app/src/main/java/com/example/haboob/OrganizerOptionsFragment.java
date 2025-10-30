package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class OrganizerOptionsFragment extends Fragment {

    // On screen attributes
    private ListView organizerEventsView;
    private ArrayList<Event> organizerEvents;
    private ArrayList<String> eventNames;
    private ArrayAdapter<String> organizerEventsAdapter;
    private int selectedPosition = -1;
    private Organizer currentOrganizer;
    private ProgressBar loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_screen, container, false);

        // Get current organizer with logging
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

        // Initialize collections
        organizerEvents = new ArrayList<>();
        eventNames = new ArrayList<>();

        // Set up adapter
        organizerEventsAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, eventNames);

        // Set up ListView
        organizerEventsView = view.findViewById(R.id.organizer_events);
        if (organizerEventsView == null) {
            Log.e("OrganizerOptions", "ListView not found in layout!");
            return view;
        }
        organizerEventsView.setAdapter(organizerEventsAdapter);

        // Optional: Add a ProgressBar to your layout and reference it here
        // loadingIndicator = view.findViewById(R.id.loading_indicator);

        // Set up create event button
        Button createEventButton = view.findViewById(R.id.create_event);
        if (createEventButton != null) {
            createEventButton.setOnClickListener(v -> {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.organizer_fragment_container, new OrganizerNewEventFragment())
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            Log.e("OrganizerOptions", "Create event button not found!");
        }

        // Load events from Firestore
        try {
            loadEventsFromFirestore();
        } catch (Exception e) {
            Log.e("OrganizerOptions", "Error loading events", e);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void loadEventsFromFirestore() {
        Log.d("OrganizerOptions", "loadEventsFromFirestore called");

        if (currentOrganizer == null) {
            Log.e("OrganizerOptions", "currentOrganizer is null in loadEventsFromFirestore");
            return;
        }

        if (currentOrganizer.getEventList() == null) {
            Log.e("OrganizerOptions", "EventList is null! Check your Organizer class initialization");
            Toast.makeText(getContext(), "Error: Event list not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("OrganizerOptions", "Checking if events are loaded...");

        // Check if events are already loaded
        if (currentOrganizer.getEventList().isLoaded()) {
            Log.d("OrganizerOptions", "Events already loaded, refreshing UI");
            // Data is already there, just refresh the UI immediately
            refreshEventList();
            return;
        }

        Log.d("OrganizerOptions", "Events not loaded yet, waiting for Firestore...");

        // Load events with callback
        currentOrganizer.getEventList().loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                Log.d("OrganizerOptions", "onEventsLoaded callback triggered");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Just tells the app to refresh
                        refreshEventList();
                    });
                } else {
                    Log.e("OrganizerOptions", "Activity is null in onEventsLoaded");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("OrganizerOptions", "Error loading events", e);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Failed to load events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // Force reload from Firestore to get fresh data
    private void reloadEventsFromFirestore() {
        Log.d("OrganizerOptions", "reloadEventsFromFirestore called - forcing fresh data");

        if (currentOrganizer == null || currentOrganizer.getEventList() == null) {
            return;
        }


        // Always reload from Firestore to get latest data
        currentOrganizer.getEventList().loadEventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                Log.d("OrganizerOptions", "Fresh data loaded from Firestore");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        refreshEventList();
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("OrganizerOptions", "Error reloading events", e);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Failed to reload events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // When we return from creating an event, force reload to get fresh data
        reloadEventsFromFirestore();
    }

    private void refreshEventList() {
        Log.d("OrganizerOptions", "refreshEventList called");

        if (currentOrganizer == null) {
            Log.e("OrganizerOptions", "currentOrganizer is null in refreshEventList");
            return;
        }

        if (currentOrganizer.getEventList() == null) {
            Log.e("OrganizerOptions", "EventList is null in refreshEventList");
            return;
        }

        try {
            // Get the updated events list
            organizerEvents = currentOrganizer.getEventList().getOrganizerEvents(currentOrganizer);
            Log.d("OrganizerOptions", "Found " + organizerEvents.size() + " events for organizer");

            // Update event names
            eventNames.clear();
            for (Event e : organizerEvents) {
                if (e != null && e.getEventTitle() != null) {
                    eventNames.add(e.getEventTitle());
                } else {
                    Log.w("OrganizerOptions", "Found null event or event title");
                }
            }

            // Notify adapter of changes
            if (organizerEventsAdapter != null) {
                organizerEventsAdapter.notifyDataSetChanged();
                Log.d("OrganizerOptions", "Adapter updated with " + eventNames.size() + " event names");
            } else {
                Log.e("OrganizerOptions", "Adapter is null!");
            }
        } catch (Exception e) {
            Log.e("OrganizerOptions", "Error in refreshEventList", e);
            Toast.makeText(getContext(), "Error refreshing events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}