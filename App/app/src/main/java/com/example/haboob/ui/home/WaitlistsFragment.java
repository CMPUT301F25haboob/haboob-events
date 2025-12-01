package com.example.haboob.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Fragment responsible for displaying all available waitlists (events that
 * the entrant can browse or join). Provides functionality for:
 *
 * <ul>
 *     <li>Loading events asynchronously from Firestore (via {@link EventsList})</li>
 *     <li>Displaying events in a {@link ListView}</li>
 *     <li>Searching events by title or tag using a {@link SearchView}</li>
 *     <li>Filtering events via Material toggle buttons (tags)</li>
 *     <li>Navigating to an event viewer when a list item is selected</li>
 * </ul>
 *
 * <p>This fragment expects a device ID argument passed from
 * EntrantMainFragment. The device ID is used to determine which waitlists
 * are relevant to the user.</p>
 */

public class WaitlistsFragment extends Fragment {

    private WaitlistAdapter adapter;
    private EventsList eventsList = new EventsList(); // declare the eventsList object
    private List<Event> entrantWaitList = new ArrayList<>();
    public static final String ARG_DEVICE_ID = "device_id";
    public String deviceId;

    /**
     * Inflates the Waitlists UI, sets up the search bar, tag filters,
     * list adapter, back navigation, and fires the initial event load.
     *
     * @param inflater  LayoutInflater for inflating XML.
     * @param container Parent view container for this fragment.
     * @param savedInstanceState Saved state bundle.
     * @return the inflated fragment root view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // unpack the bundle from entrantMainFragment(called on waitlist button click):
        deviceId = getArguments() != null
                ? getArguments().getString(ARG_DEVICE_ID)
                : null;

        loadEventsForUser(); // load events for the waitListAdapter, resets the adapter's list of events, replaces it with all events

        // Inflate the layout for this fragment (fragment_waitlists.xml)
        View v = inflater.inflate(R.layout.fragment_waitlists, container, false);
        ListView list = v.findViewById(R.id.waitlistsListView);
        SearchView search = v.findViewById(R.id.searchView);
        Button backButton = v.findViewById(R.id.back_button);
        MaterialButtonToggleGroup tagGroup = v.findViewById(R.id.tag_filter_group); // grab the tagroup for filtering

        backButton.setOnClickListener(v1 -> {
            Navigation.findNavController(v).navigateUp();
        });

//        entrantWaitList = eventsList.getEventsList(); // grab all events available for browsing

        Map<Integer, String> tagMap = new HashMap<>();
        tagMap.put(R.id.filter_all, "");
        tagMap.put(R.id.filter_adults, "adults");
        tagMap.put(R.id.filter_kids, "music");
        tagMap.put(R.id.filter_gaming, "gaming");
        tagMap.put(R.id.filter_fitness, "fitness");
        tagMap.put(R.id.filter_sports, "sports");
        tagMap.put(R.id.filter_family, "family");
        tagMap.put(R.id.filter_seniors, "seniors");
        tagMap.put(R.id.filter_art, "art");
        tagMap.put(R.id.filter_free, "free");
        tagMap.put(R.id.filter_indoor, "indoor");
        tagMap.put(R.id.filter_outdoor, "outdoor");
        tagMap.put(R.id.filter_music, "music");

        // Tag filter -> adapter filter

        tagGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            String query = tagMap.get(checkedId);
            if (query != null) {
                search.setQuery(query, true);
                search.clearFocus();
            }
        });

        adapter = new WaitlistAdapter(requireContext(), new ArrayList<>(entrantWaitList));
        list.setAdapter(adapter);

        // Search -> adapter filter
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) {
                adapter.getFilter().filter(q);
                list.clearFocus();   // collapse keyboard
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // Click -> open EventViewerFragment with selected event
        list.setOnItemClickListener((parent, view1, position, id) -> {
            Event selected = adapter.getItem(position);
            Log.d("WaitlistsFragment", "Item clicked at position: " + position);
            if (selected == null)
            {
                Log.e("WaitlistsFragment", "Selected event is NULL at position: " + position);
                return;
            }

            Log.d("WaitlistsFragment", "Navigating to event: " + selected.getEventID() + " - " + selected.getEventTitle());
            Bundle args = new Bundle();
            args.putString(EventViewerFragment.ARG_EVENT_ID, selected.getEventID());
            Navigation.findNavController(v).navigate(R.id.entrant_event_view, args);
        });

        return v;
    }


    /**
     * Initiates asynchronous loading of events from Firestore via {@link EventsList}.
     * Uses the {@link EventsList.OnEventsLoadedListener} callback to receive the
     * loaded events, then refreshes the adapter with the new data.
     *
     * Queries the dataBase, relies on a callback to adds events to local EventList Array,
     * updates the imageAdapter with the new images from the database
     *
     * <p>This method sets up:</p>
     * <ul>
     *     <li>onEventsLoaded → populates entrantWaitList and updates adapter</li>
     *     <li>onError → logs Firestore load failure</li>
     * </ul>
     */
    private void loadEventsForUser() {


        eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() { // the callback function calls this when events are loaded

//                entrantWaitList = eventsList.getEventsList();
                entrantWaitList = eventsList.getLiveEvents();
                Log.d("TAG", "waitList EVENTSLIST SIZE: " + entrantWaitList.size());

                adapter.replaceAll(entrantWaitList);
                Log.d("TAG", "ImageAdapter images Replaced");

            }
            @Override
            public void onError(Exception err) {
                Log.e("TAG", "Failed loading events", err);
            }
        });
    }

    // When your Firestore query finishes, call:
    public void onWaitlistsLoaded(List<Event> fromDb) {
        if (adapter != null) adapter.replaceAll(fromDb);
    }
}