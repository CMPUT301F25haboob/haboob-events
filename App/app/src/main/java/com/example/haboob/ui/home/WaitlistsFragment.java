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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class WaitlistsFragment extends Fragment {

    private WaitlistAdapter adapter;
    private EventsList eventsList = new EventsList(); // declare the eventsList object
    private List<Event> entrantWaitList = new ArrayList<>();
    public static final String ARG_DEVICE_ID = "device_id";
    public String deviceId;

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

        backButton.setOnClickListener(v1 -> {
            Navigation.findNavController(v).navigateUp();
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

    // queries the dataBase, relies on a callback to adds events to local EventList Array, updates the imageAdapter with the new images from the database
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