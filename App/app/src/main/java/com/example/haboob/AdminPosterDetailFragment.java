package com.example.haboob;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment to display the full details of a poster for administrative review.
 */
public class AdminPosterDetailFragment extends Fragment {

    private static final String TAG = "AdminPosterDetailFrag";

    private MaterialToolbar toolbar;
    private TextView titleTextView;
    private TextView statusTextView;
    private ImageView fullImageView;
    private MaterialButton removeButton; // Renamed from rejectButton

    private String posterId; // The ID of the poster passed from the previous fragment

    public AdminPosterDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Safely retrieve the posterId passed via arguments
        if (getArguments() != null) {
            posterId = getArguments().getString("poster_id");
            Log.d(TAG, "Received Poster ID: " + posterId);
        }
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
        fullImageView = view.findViewById(R.id.poster_full_image);
        removeButton = view.findViewById(R.id.btn_remove_poster); // Updated ID

        // Set up listeners
        toolbar.setNavigationOnClickListener(v -> {
            // Handle the back button click
            NavHostFragment.findNavController(this).popBackStack();
        });

        removeButton.setOnClickListener(v -> handleRemoval()); // Updated listener

        // Load content based on posterId
        if (posterId != null) {
            loadPosterDetails(posterId);
        } else {
            titleTextView.setText("Error: No Poster Selected");
            Toast.makeText(getContext(), "Error: Poster ID missing.", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    /**
     * Placeholder method to fetch and display poster details from a database (e.g., Firebase).
     * @param id The ID of the poster to load.
     */
    private void loadPosterDetails(String id) {
        // TODO: Replace this mock data with actual Firestore fetching logic using 'id'
        Log.d(TAG, "Fetching details for poster ID: " + id);

        // --- MOCK DATA SIMULATION ---
        String mockTitle = "Community Coding Workshop";
        String mockStatus = "Pending Review | Submitted by Organizer A";
        String mockImageUrl = "https://placehold.co/600x800/2ecc71/white?text=Poster+Image";

        titleTextView.setText(mockTitle);
        statusTextView.setText(mockStatus);

        // Load the image into the ImageView
        // Use a standard Android resource as a placeholder since an external library is not used.
        // TODO: Implement a custom image loading solution (e.g., using HttpURLConnection and AsyncTask)
        //       to fetch the image from mockImageUrl in a background thread without a library.
        fullImageView.setImageResource(R.drawable.shrug);
        // --- END MOCK DATA ---
    }


    /**
     * Handles the removal process for the poster. (Renamed from handleRejection)
     */
    private void handleRemoval() {
        if (posterId != null) {
            Log.d(TAG, "Removing poster: " + posterId);
            Toast.makeText(getContext(), "Poster " + posterId + " Removed!", Toast.LENGTH_SHORT).show();
            // TODO: Implement actual database update for removal
            NavHostFragment.findNavController(this).popBackStack();
        }
    }
}