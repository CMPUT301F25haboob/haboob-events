package com.example.haboob;

import static android.view.View.INVISIBLE;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
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

        // TODO: Functionality of this when we click an element in the certain lists
        cancelEntrantButton.setVisibility(INVISIBLE);

        // Create onClick listeners for all buttons:
        backButton.setOnClickListener(v ->  {

            // Just return to previous fragment
            getParentFragmentManager().popBackStack();

        });

        csvDataButton.setOnClickListener(v -> {

            // Generate CSV text
            String csv = generateCsv(selectedEvent);  // event you're exporting

            // 2. Save it to a file
            String fileName = "entrants_" + selectedEvent.getEventID() + ".csv";
            Uri uri = saveCsvToFile(getContext(), csv, fileName);

            if (uri != null) {
                // 3. Launch sharing dialog
                shareCsv(getContext(), uri);
            } else {
                Toast.makeText(getContext(), "Failed to create CSV file", Toast.LENGTH_SHORT).show();
            }
        });

        cancelEntrantButton.setOnClickListener(v -> {

            // TODO: need to get the entrant from in the list to remove from that list
            Toast.makeText(this.getContext(), "Cancel entrant not implemented yet sorry", Toast.LENGTH_SHORT).show();
        });




    }

    // CSV-related functions
    private String generateCsv(Event event) {
        StringBuilder sb = new StringBuilder();

        // Header row
        sb.append("Enrolled Entrant IDs\n");

        // Add each enrolled entrant
        for (String entrant : event.getEnrolledEntrants()) {
            sb.append(entrant).append("\n");
        }

        return sb.toString();
    }

    private Uri saveCsvToFile(Context context, String csvText, String fileName) {
        try {
            File path = context.getExternalFilesDir(null); // app external directory
            File file = new File(path, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvText.getBytes());
            fos.close();

            // Return a sharable URI
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void shareCsv(Context context, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Export Entrants CSV"));
    }
}
