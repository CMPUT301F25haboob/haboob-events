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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Fragment used by organizers to review and manage all entrant-related lists for a single
 * {@link Event}, and to visualize entrant signup locations on a Google Map.
 * <p>
 * The fragment expects an {@link Event} instance passed in the arguments under the key
 * {@code "event"} as a {@link java.io.Serializable}. If the event cannot be recovered,
 * the fragment logs an error and immediately pops the back stack.
 * </p>
 *
 * <p>Core responsibilities include:</p>
 * <ul>
 *     <li>Displaying grouped entrant lists (invited, waiting, enrolled, cancelled) in an
 *         {@link ExpandableListView} using {@link OrganizerExpandableListsAdapter}.</li>
 *     <li>Rendering a {@link GoogleMap} and placing markers for each entrant location.</li>
 *     <li>Allowing organizers to cancel entrants from the enrolled list and notifying them.</li>
 *     <li>Exporting the enrolled entrant list as a CSV file that can be shared externally.</li>
 *     <li>Sending notifications to specific entrant groups via {@link NotificationManager}.</li>
 *     <li>Navigating to auxiliary views such as the event QR code screen.</li>
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
     * Inflates the organizer lists layout, attaches the {@link SupportMapFragment} for displaying
     * entrant locations, and unpacks the selected {@link Event} from the fragment arguments.
     * <p>
     * If a valid {@link Event} is found, this method delegates to {@link #displayLists(View)} to
     * initialize all UI elements. If no event can be recovered, the method logs a diagnostic message
     * and pops the fragment back stack to return to the previous screen.
     * </p>
     *
     * @param inflater  the {@link LayoutInflater} used to inflate the fragment layout
     * @param container the parent {@link ViewGroup} into which the fragment view will be placed
     * @param savedInstanceState saved instance state bundle, or {@code null} if none
     * @return the root {@link View} for this fragment's UI
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
     * Callback invoked when the {@link GoogleMap} instance is ready for use.
     * <p>
     * Stores the provided map reference, places markers for each entrant location by calling
     * {@link #setPinsOnMap()}, centers the camera on Edmonton with a default zoom level, and
     * enables basic map UI controls (such as zoom buttons).
     * </p>
     *
     * @param map the fully initialized {@link GoogleMap} instance attached to this fragment
     */

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        // Add a marker in Sydney and move the camera
        LatLng edmonton = new LatLng(53.5462, -113.4937);

        // Put all pins of user signup locations on the map
        setPinsOnMap();

        // Add dummy marker for now
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 10));

        // Show the map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    /**
     * Iterates over all entrant locations associated with {@link #selectedEvent} and adds
     * a {@link com.google.android.gms.maps.model.Marker} for each one to the attached
     * {@link GoogleMap}.
     * <p>
     * Each stored {@link GeoPoint} is converted into a {@link LatLng} before being rendered.
     * This method assumes that both {@link #selectedEvent} and {@link #googleMap} are non-null
     * and have been initialized prior to invocation.
     * </p>
     */
    public void setPinsOnMap() {
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

        // Force re-measure on expand/collapse
        expandableListView.setOnGroupExpandListener(groupPosition -> {
            expandableListView.post(() -> {
                expandableListView.requestLayout();
                ViewGroup parent = (ViewGroup) expandableListView.getParent();
                if (parent != null) {
                    parent.requestLayout();
                }
            });
        });

        expandableListView.setOnGroupCollapseListener(groupPosition -> {
            expandableListView.post(() -> {
                expandableListView.requestLayout();
                ViewGroup parent = (ViewGroup) expandableListView.getParent();
                if (parent != null) {
                    parent.requestLayout();
                }
            });
        });


        // Button functionality, cancel entrant should be hidden until user in list is selected
        Button backButton = view.findViewById(R.id.back_button);
        Button csvDataButton = view.findViewById(R.id.csv_data_button);
        Button sendMsgButton = view.findViewById(R.id.send_message_button);
        ImageButton qrCodeButton = view.findViewById(R.id.qr_button);

        qrCodeButton.setOnClickListener(v -> {
            // Take user to QR code fragment -> able to save as picture
            // Navigate to EventQRCodeFragment with event ID
            Bundle args = new Bundle();
            args.putString(EventQRCodeFragment.ARG_EVENT_ID, selectedEvent.getEventID());
            args.putString("source", "organizer");

            EventQRCodeFragment fragment = new EventQRCodeFragment();
            fragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        });

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

    /**
     * Builds a CSV-formatted {@link String} representing the enrolled entrants for the given
     * {@link Event}.
     * <p>
     * The first line contains a header, followed by one line per enrolled entrant ID.
     * This text can be written to a file and shared externally using
     * {@link #saveCsvToFile(Context, String, String)} and {@link #shareCsv(Context, Uri)}.
     * </p>
     *
     * @param event the {@link Event} whose enrolled entrant IDs should be exported
     * @return CSV text containing a header row and one row per enrolled entrant ID
     */
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
     * Displays a confirmation dialog asking the organizer whether they want to cancel the
     * specified entrant.
     * <p>
     * If the organizer confirms, {@link #cancelUser(String)} is invoked to perform the
     * cancellation and notify the user. If the organizer declines, the dialog is simply
     * dismissed and no changes are made.
     * </p>
     *
     * @param entrantID the ID of the entrant to potentially cancel from the event
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

    /**
     * Cancels the specified entrant by moving them from the invited list to the cancelled list
     * in the {@link Event}, refreshes the expandable list UI, and sends a notification to the
     * affected user.
     * <p>
     * Internally, this method:
     * </p>
     * <ul>
     *     <li>Updates the {@link Event} model (invited → cancelled).</li>
     *     <li>Rebuilds {@link #expandableListDetail} and {@link #expandableListAdapter} so that
     *         the UI reflects the latest data.</li>
     *     <li>Re-attaches the child click listener that triggers
     *         {@link #showCancellationDialog(String)} for future clicks.</li>
     *     <li>Constructs and dispatches a {@link Notification} via {@link NotificationManager}
     *         to inform the entrant that they have been cancelled.</li>
     * </ul>
     *
     * @param entrantID the ID of the entrant being cancelled
     */
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

    /**
     * Writes the provided CSV text to a file in the app's external files directory and returns
     * a {@link Uri} that can be shared with other apps via a {@link FileProvider}.
     * <p>
     * The method uses the application-specific external storage returned by
     * {@link Context#getExternalFilesDir(String)} and wraps the resulting file inside a
     * {@link FileProvider} using the authority {@code &lt;packageName&gt;.provider}. Any
     * exceptions encountered during file creation or writing result in a {@code null} return
     * value.
     * </p>
     *
     * @param context  a valid {@link Context}, typically the hosting activity
     * @param csvText  the CSV content to be written to disk
     * @param fileName the desired file name (e.g., {@code "entrants_eventId.csv"})
     * @return a shareable {@link Uri} pointing to the written file, or {@code null} if creation fails
     */
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

    /**
     * Launches a system share sheet for the given CSV file {@link Uri} using an
     * {@link Intent#ACTION_SEND} intent.
     * <p>
     * The intent is configured with MIME type {@code "text/csv"}, attaches the file as
     * {@link Intent#EXTRA_STREAM}, and grants temporary read permission via
     * {@link Intent#FLAG_GRANT_READ_URI_PERMISSION}. The user is prompted to choose
     * an appropriate app for handling the exported CSV file.
     * </p>
     *
     * @param context a valid {@link Context} used to start the chooser activity
     * @param fileUri the {@link Uri} of the CSV file to share
     */
    private void shareCsv(Context context, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Export Entrants CSV"));
    }
}
