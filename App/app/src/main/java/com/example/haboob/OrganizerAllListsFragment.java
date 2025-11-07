package com.example.haboob;

import static android.view.View.INVISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class OrganizerAllListsFragment extends Fragment {

    // Inflate new view
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private ArrayList<String> expandableListTitle;
    private HashMap<String, ArrayList<String>> expandableListDetail;
    private Event selectedEvent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organizer_all_lists_layout, container, false);

        // Unpack the bundle to get the event
        if (getArguments() != null) {
            selectedEvent = (Event) getArguments().getSerializable("event");

            if (selectedEvent != null) {

                // Everything working as intended, set up view
                displayLists(view);

            } else {
                // Handle the case where selectedEvent is null -> throw error and return
                Log.d("OrganizerAllListsFragment", "Event is null");
                getParentFragmentManager().popBackStack();
            }
        } else {
            // Handle the case where getArguments() is null -> throw error and return
            Log.d("OrganizerAllListsFragment", "Arguments are null, did not append event to bundle");
            getParentFragmentManager().popBackStack();
        }

        return view;
    }

    public void displayLists(View view) {

        // Display the lists for <event_name>
        TextView eventTitle = view.findViewById(R.id.event_title);
        if (eventTitle != null) {
            eventTitle.setText(selectedEvent.getEventTitle());
        }

        // TESTING -> ADDING TO LISTS TO SEE IF THEY DISPLAY
//        String eventID = selectedEvent.getEventID();
//
//        if (eventID != null) {
//            selectedEvent.addEntrantToWaitingEntrants("TEST_USER_ID1");
//            selectedEvent.addEntrantToWaitingEntrants("TEST_USER_ID2");
//        } else {
//            Log.d("OrganizerAllListsFragment", "Event ID is null");
//        }



        // Expandable list display to screen
        expandableListView = view.findViewById(R.id.expandable_list_view);
        expandableListDetail = OrganizerExpandableListsData.getListsToDisplay(selectedEvent);
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new OrganizerExpandableListsAdapter(this.getContext(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);


        // Button functionality, cancel entrant should be hidden until user in list is selected
        Button backButton = view.findViewById(R.id.back_button);
        Button csvDataButton = view.findViewById(R.id.csv_data_button);
        Button cancelEntrantButton = view.findViewById(R.id.cancel_entrant_button);


        // Create onClick listeners for all buttons:
        backButton.setOnClickListener(v ->  {

            // Just return to previous fragment
            getParentFragmentManager().popBackStack();

        });

        csvDataButton.setOnClickListener(v -> {

            // TODO: Call this function to export csv data of final enrolled lists
            Toast.makeText(this.getContext(), "CSV data not implemented yet sorry", Toast.LENGTH_SHORT).show();
        });

        cancelEntrantButton.setOnClickListener(v -> {

            // TODO: need to get the entrant from in the list to remove from that list
            Toast.makeText(this.getContext(), "Cancel entrant not implemented yet sorry", Toast.LENGTH_SHORT).show();
        });


        // TODO: Functionality of this when we click an element in the certain lists
        cancelEntrantButton.setVisibility(INVISIBLE);

    }
}
