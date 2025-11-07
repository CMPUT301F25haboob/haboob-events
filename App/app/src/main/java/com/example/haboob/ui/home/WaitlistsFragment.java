package com.example.haboob.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WaitlistsFragment extends Fragment {

    private WaitlistAdapter adapter;
    private EventsList eventsList = new EventsList(); // declare the eventsList object
    private List<Event> listOfEvents = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        loadEventsForUser("davids_id"); // load events for thw waitListAdapter, resets the adapter's list of events

        // Inflate the layout for this fragment (fragment_waitlists.xml)
        View v = inflater.inflate(R.layout.fragment_waitlists, container, false);
        ListView list = v.findViewById(R.id.waitlistsListView);
        SearchView search = v.findViewById(R.id.searchView);

        List<Event> listOfEvents = eventsList.getEventsList(); // making a List<Event> So I can iterate through the events, cant do that with an EventsList object

        adapter = new WaitlistAdapter(requireContext(), new ArrayList<>(listOfEvents));
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
            if (selected == null) return;

            Bundle args = new Bundle();
            args.putString("eventID", selected.getEventID()); // or parcelable Event if you prefer
            NavHostFragment.findNavController(this)
                    .navigate(R.id.entrant_event_view, args);
        });

        return v;
    }

    // queries the dataBase, relies on a callback to adds events to local EventList Array, updates the imageAdapter with the new images from the database
    private void loadEventsForUser(String userId) {

        eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() { // the callback function calls this when events are loaded

                listOfEvents = eventsList.getEventsList();
                Log.d("TAG", "waitList EVENTSLIST SIZE: " + listOfEvents.size());

                adapter.replaceAll(listOfEvents);
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