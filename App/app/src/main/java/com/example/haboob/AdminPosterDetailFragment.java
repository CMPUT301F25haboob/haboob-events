/**
 Fragment to displays all posters details for an admin
 Copyright (C) 2025  jeff

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package com.example.haboob;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

/**
 * Fragment to display the full details of a poster/event for administrative review.
 *
 * <p>This fragment allows administrators to:</p>
 * <ul>
 *   <li>View complete event details including description, dates, and limits</li>
 *   <li>View the full-size event poster image</li>
 *   <li>Remove the poster image from an event</li>
 *   <li>Delete the entire event from the system</li>
 * </ul>
 *
 * <p>The fragment loads event data from Firestore based on the event ID passed
 * via navigation arguments. It displays event information, poster image, and
 * provides administrative actions with appropriate confirmation dialogs.</p>
 *
 * @author Jeff
 * @version 1.0
 * @see Event
 * @see Poster
 */
public class AdminPosterDetailFragment extends Fragment {

    /** Tag for logging */
    private static final String TAG = "AdminPosterDetailFrag";

    /** Toolbar with navigation controls */
    private MaterialToolbar toolbar;

    /** Back button for navigation */
    private View backButton;

    /** TextView displaying the event title */
    private TextView titleTextView;

    /** TextView displaying status information (organizer ID) */
    private TextView statusTextView;

    /** TextView displaying the event description */
    private TextView descriptionTextView;

    /** TextView displaying the registration start date */
    private TextView startDateTextView;

    /** TextView displaying the registration end date */
    private TextView endDateTextView;

    /** TextView displaying the registration limit */
    private TextView limitTextView;

    /** TextView displaying the current waitlist size */
    private TextView waitlistSizeTextView;

    /** ImageView displaying the full poster image */
    private ImageView fullImageView;

    /** Button to remove the poster image */
    private MaterialButton removeImageButton;

    /** Button to delete the entire event */
    private MaterialButton deleteEventButton;

    /** The ID of the poster/event being viewed */
    private String posterId;

    /** Firestore database instance */
    private FirebaseFirestore db;

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminPosterDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     * Retrieves the poster/event ID from navigation arguments.
     *
     * @param savedInstanceState Previously saved state of the fragment, if any
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            posterId = getArguments().getString("poster_id");
            Log.d(TAG, "Received Event ID: " + posterId);
        }
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Inflates the fragment's layout and initializes all UI components.
     * Sets up click listeners for buttons and loads event details if an ID was provided.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_poster_view, container, false);

        // Initialize UI components
        toolbar = view.findViewById(R.id.posterViewTopAppBar);
        titleTextView = view.findViewById(R.id.poster_detail_title);
        statusTextView = view.findViewById(R.id.poster_detail_status);

        descriptionTextView = view.findViewById(R.id.poster_detail_description);
        startDateTextView = view.findViewById(R.id.poster_detail_start_date);
        endDateTextView = view.findViewById(R.id.poster_detail_end_date);
        limitTextView = view.findViewById(R.id.poster_detail_limit);
        waitlistSizeTextView = view.findViewById(R.id.poster_detail_waitlist_size);

        fullImageView = view.findViewById(R.id.poster_full_image);
        removeImageButton = view.findViewById(R.id.btn_remove_poster_image);
        deleteEventButton = view.findViewById(R.id.btn_delete_event);
        backButton = view.findViewById(R.id.admin_poster_back_btn);

        // Set up listeners
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                NavHostFragment.findNavController(this).popBackStack();
            });
        }

        removeImageButton.setOnClickListener(v -> handleRemoveImage());
        deleteEventButton.setOnClickListener(v -> handleDeleteEvent());

        if (posterId != null) {
            loadEventDetails(posterId);
        } else {
            titleTextView.setText("Error: No Event Selected");
            removeImageButton.setEnabled(false);
            deleteEventButton.setEnabled(false);
        }

        return view;
    }

    /**
     * Loads event details from Firestore and populates the UI.
     *
     * <p>Queries the events collection for the event with the matching ID,
     * then populates all text fields and loads the poster image using Glide.
     * If the event has no image, disables the remove image button.</p>
     *
     * @param id The event ID to load details for
     */
    private void loadEventDetails(String id) {
        db.collection("events")
                .whereEqualTo("eventID", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            titleTextView.setText(event.getEventTitle());
                            statusTextView.setText("Organizer ID: " + event.getOrganizer());

                            // Description
                            descriptionTextView.setText(event.getEventDescription() != null ? event.getEventDescription() : "No description");

                            // Dates
                            Date start = event.getRegistrationStartDate();
                            Date end = event.getRegistrationEndDate();
                            startDateTextView.setText("Registration Start: " + (start != null ? start.toString() : "N/A"));
                            endDateTextView.setText("Registration End: " + (end != null ? end.toString() : "N/A"));

                            // Limits and Counts
                            int limit = event.getOptionalWaitingListSize();
                            if (limit < 0) {
                                limitTextView.setText("Registration Limit: Uncapped");
                            } else {
                                limitTextView.setText("Registration Limit: " + limit);
                            }

                            int waitlistCount = (event.getWaitingEntrants() != null) ? event.getWaitingEntrants().size() : 0;
                            waitlistSizeTextView.setText("Amount in Lottery (Waitlist): " + waitlistCount);

                            // Image
                            if (event.getPoster() != null && event.getPoster().getData() != null && !event.getPoster().getData().isEmpty()) {
                                Glide.with(this)
                                        .load(event.getPoster().getData())
                                        .placeholder(R.drawable.shrug)
                                        .error(R.drawable.shrug)
                                        .into(fullImageView);
                            } else {
                                fullImageView.setImageResource(R.drawable.shrug);
                                removeImageButton.setEnabled(false); // Disable if no image
                            }
                        }
                    } else {
                        titleTextView.setText("Event not found");
                        removeImageButton.setEnabled(false);
                        deleteEventButton.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching event details", e);
                    Toast.makeText(getContext(), "Failed to load details.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles the removal of a poster image from an event.
     *
     * <p>Updates the Firestore document to set the poster data field to an empty string,
     * effectively removing the image. Shows a success toast and navigates back to the
     * previous screen upon completion.</p>
     */
    private void handleRemoveImage() {
        if (posterId == null) return;

        db.collection("events").whereEqualTo("eventID", posterId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("events").document(docId)
                                .update("poster.data", "")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Poster image removed.", Toast.LENGTH_SHORT).show();
                                    NavHostFragment.findNavController(this).popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing poster image", e);
                                    Toast.makeText(getContext(), "Failed to remove image.", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    /**
     * Handles the deletion of an entire event from the database.
     *
     * <p>Shows a confirmation dialog before deleting. If confirmed, removes the event
     * document from Firestore and navigates back to the previous screen. The deletion
     * is permanent and cannot be undone.</p>
     */
    private void handleDeleteEvent() {
        if (posterId == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("events").whereEqualTo("eventID", posterId).get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String docId = querySnapshot.getDocuments().get(0).getId();
                                    db.collection("events").document(docId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Event deleted.", Toast.LENGTH_SHORT).show();
                                                NavHostFragment.findNavController(this).popBackStack();
                                            });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}