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

        loadEventsForUser("davids_id"); // load events for thw waitListAdapter, resets the adapter's list of events

        // Inflate the layout for this fragment (fragment_waitlists.xml)
        View v = inflater.inflate(R.layout.fragment_waitlists, container, false);
        ListView list = v.findViewById(R.id.waitlistsListView);
        SearchView search = v.findViewById(R.id.searchView);
        Button backButton = v.findViewById(R.id.back_button);

        backButton.setOnClickListener(v1 -> {
            Navigation.findNavController(v).navigateUp();
        });



        entrantWaitList = eventsList.getEntrantWaitlistEvents(deviceId);

//        List<Event> listOfEvents = eventsList.getEventsList(); // making a List<Event> So I can iterate through the events, cant do that with an EventsList object

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

                if (ARG_DEVICE_ID == "device_id")
                    Log.d("TAG", "In the waitlists fragment, device ID is the default, so device ID is not being fetched correctly.");
                entrantWaitList = eventsList.getEntrantWaitlistEvents(ARG_DEVICE_ID);
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