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
 */
public class AdminPosterDetailFragment extends Fragment {

    private static final String TAG = "AdminPosterDetailFrag";

    private MaterialToolbar toolbar;
    private View backButton;
    private TextView titleTextView;
    private TextView statusTextView;

    // Details Views
    private TextView descriptionTextView;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private TextView limitTextView;
    private TextView waitlistSizeTextView;

    private ImageView fullImageView;
    private MaterialButton removeImageButton;
    private MaterialButton deleteEventButton;

    private String posterId;
    private FirebaseFirestore db;

    public AdminPosterDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            posterId = getArguments().getString("poster_id");
            Log.d(TAG, "Received Event ID: " + posterId);
        }
        db = FirebaseFirestore.getInstance();
    }

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