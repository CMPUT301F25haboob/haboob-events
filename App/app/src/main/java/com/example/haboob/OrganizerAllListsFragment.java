package com.example.haboob;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.haboob.databinding.ActivityOrganizerViewMapsBinding;
import com.google.firebase.firestore.GeoPoint;

import static android.view.View.INVISIBLE;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * {@code OrganizerAllListsFragment} displays all entrant-related lists for a selected {@link Event}
 * (e.g., invited, waiting, enrolled, cancelled) in an {@link ExpandableListView}.
 * <p>
 * The fragment expects an {@link Event} instance to be supplied via arguments under the key
 * {@code "event"} (as a {@link java.io.Serializable}). If the argument or event is missing,
 * it logs an error and pops the back stack.
 * <p>
 * UI controls include:
 * <ul>
 *   <li>Back button – returns to previous fragment</li>
 *   <li>CSV button –  export final enrolled list data</li>
 *   <li>Cancel entrant – remove a selected entrant from a list</li>
 * </ul>
 */
public class OrganizerAllListsFragment extends Fragment implements OnMapReadyCallback {

    // Inflate new view
    /** Expandable list UI component for grouped entrant lists. */
    private ExpandableListView expandableListView;

    /** Adapter to bind titles and children to the {@link ExpandableListView}. */
    private OrganizerExpandableListsAdapter expandableListAdapter;

    /** Group titles for the expandable list (e.g., "Invited", "Waiting"). */
    private ArrayList<String> expandableListTitle;

    /** Mapping of group title to list of entrant IDs for each category. */
    private HashMap<String, ArrayList<String>> expandableListDetail;

    /** The event whose lists are being displayed. */
    private Event selectedEvent;

    /** Map of user signups for this event */
    private GoogleMap googleMap;

    /**
     * Inflates the layout and unpacks the selected {@link Event} from arguments.
     * If the event is present, initializes the expandable lists; otherwise, navigates back.
     *
     * @param inflater  LayoutInflater to inflate the view
     * @param container parent view group
     * @param savedInstanceState saved instance state, if any
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organizer_all_lists_layout, container, false);
        // Attach the map fragment programmatically
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

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

    /**
     * Called when the map is ready to be used
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Sydney and move the camera
        LatLng edmonton = new LatLng(53.5462, -113.4937);

        // Put all pins of user signup locations on the map
        setPinsOnMap(googleMap);

        // Add dummy marker for now
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(edmonton));

        // Show the map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    /**
     * Called to populate all of the pins from entrants onto the map
     * @param googleMap
     */
    public void setPinsOnMap(GoogleMap googleMap) {

        // Create a new pin for each entrant
        for (GeoPoint point : selectedEvent.getEntrantLocations()) {

            // Refactor geopoint into latlong position to display to map
            LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    /**
     * Binds the event title and sets up the {@link ExpandableListView} with lists
     * returned by {@link OrganizerExpandableListsData#getListsToDisplay(Event)}.
     * Also wires up the Back, CSV, and Cancel Entrant buttons (CSV/Cancel are TODO).
     *
     * @param view the inflated root view
     */
    public void displayLists(View view) {

        // Display the event name at the top
        TextView eventTitle = view.findViewById(R.id.event_title);
        if (eventTitle != null) {
            eventTitle.setText(selectedEvent.getEventTitle());
        }

        // Expandable list display to screen
        expandableListView = view.findViewById(R.id.expandable_list_view);
        expandableListDetail = OrganizerExpandableListsData.getListsToDisplay(selectedEvent);
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new OrganizerExpandableListsAdapter(this.getContext(), expandableListTitle, expandableListDetail);

        // Parent group click listener
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // Check if the group has no children
                ArrayList<String> children = expandableListDetail.get(expandableListTitle.get(groupPosition));
                if (children == null || children.isEmpty()) {
                    Date today = new Date();
                    new AlertDialog.Builder(requireContext())
                            .setTitle("No Data")
                            .setMessage(today.before(selectedEvent.getRegistrationEndDate()) ? "Lottery has not occurred yet" : "No entrants in list")
                            .setPositiveButton("OK", null)
                            .show();
                    return true; // Consume the click, prevent expansion
                }
                return false; // Allow normal expand/collapse behavior
            }
        });

        // Child click listener
        expandableListAdapter.setOnChildItemClickListener(new OrganizerExpandableListsAdapter.OnChildItemClickListener() {
            @Override
            public void onChildItemClick(int groupPosition, int childPosition, String entrantID) {
                Log.d("OrganizerAllListsFragment", "Callback triggered! Group: " + groupPosition);
                if (groupPosition == 2) {
                    showCancellationDialog(entrantID);
                }
            }
        });
        expandableListView.setAdapter(expandableListAdapter);


        // Button functionality, cancel entrant should be hidden until user in list is selected
        Button backButton = view.findViewById(R.id.back_button);
        Button csvDataButton = view.findViewById(R.id.csv_data_button);
        Button sendMsgButton = view.findViewById(R.id.send_message_button);

        // Author: Owen - Dialogue for sending a notification to a selected list of entrants
        sendMsgButton.setOnClickListener(v -> {
            // Inflate dialogue view
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialogue_send_notification, null, false);
            EditText etMessage = dialogView.findViewById(R.id.et_message);
            Spinner spList = dialogView.findViewById(R.id.sp_list);

            // Dropdown options lists from OrganizerExpandableListsData class
            String[] groups = new String[] { "Invite list", "Waiting list", "Enrolled list", "Cancelled list" };
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    groups
            );
            spList.setAdapter(spinnerAdapter);

            // Build and show dialog to cancel the entrant
            new AlertDialog.Builder(requireContext())
                    .setTitle("Send Notification")
                    .setView(dialogView)
                    .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                    .setPositiveButton("Send", (d, which) -> {
                        String message = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
                        if (message.isEmpty()) {
                            Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String selectedGroup = (String) spList.getSelectedItem();

                        // Get recipient IDs from your existing map (OrganizerExpandableListsData result)
                        // Keys must match what you used to build expandableListDetail.
                        ArrayList<String> recipientIds = expandableListDetail.get(selectedGroup);
                        if (recipientIds == null || recipientIds.isEmpty()) {
                            Toast.makeText(requireContext(), "No recipients in " + selectedGroup, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Prepare notification (recipientId handled by sendToList)
                        String organizerId = selectedEvent.getOrganizer();
                        Notification notification = new Notification(
                                selectedEvent.getEventID(),
                                organizerId,
                                message
                        );

                        // Send to list using NotificationManager
                        NotificationManager nm = new NotificationManager();
                        nm.sendToList(recipientIds, organizerId, notification);

                        Toast.makeText(requireContext(), "Sending to " + selectedGroup + "…", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

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


    /**
     * Shows a confirmation dialog to cancel an entrant from the invite list
     * @param entrantID the ID of the entrant to cancel
     */
    private void showCancellationDialog(String entrantID) {
        // Show new alert
        new AlertDialog.Builder(getContext())
                .setTitle("Cancel User")
                .setMessage("Are you sure you want to cancel entrant with ID: '" + entrantID + "'?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform the cancel action here
                        cancelUser(entrantID);
                        Toast.makeText(getContext(), entrantID + " has been cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked No, just dismiss the dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void cancelUser(String entrantID) {
        // Remove user from invitedList, add to cancelledList
        selectedEvent.removeEntrantFromInvitedEntrants(entrantID);
        selectedEvent.addEntrantToCancelledEntrants(entrantID);

        // Refresh the expandable list data
        expandableListDetail = OrganizerExpandableListsData.getListsToDisplay(selectedEvent);
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());

        // Update the adapter with new data
        expandableListAdapter = new OrganizerExpandableListsAdapter(getContext(), expandableListTitle, expandableListDetail);

        // Reset the click listener for next time
        expandableListAdapter.setOnChildItemClickListener(new OrganizerExpandableListsAdapter.OnChildItemClickListener() {
            @Override
            public void onChildItemClick(int groupPosition, int childPosition, String entrantID) {
                Log.d("OrganizerAllListsFragment", "Callback triggered! Group: " + groupPosition);
                if (groupPosition == 2) {
                    showCancellationDialog(entrantID);
                }
            }
        });

        // Set the new adapter
        expandableListView.setAdapter(expandableListAdapter);

        // Send out a notification to the user that they've been cancelled from the event
        Notification cancelNotif = new Notification(selectedEvent.getEventID(), selectedEvent.getOrganizer(), entrantID, "You have been cancelled from: " + selectedEvent.getEventTitle() + "");
        NotificationManager nm = new NotificationManager();
        nm.sendToUser(cancelNotif);
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
