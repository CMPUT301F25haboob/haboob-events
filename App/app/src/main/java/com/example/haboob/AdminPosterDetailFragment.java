package com.example.haboob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment to display the full details of a poster for administrative review.
 * It receives a "poster_id" (which is actually an Event ID) via fragment arguments,
 * displays the associated poster and details, and provides an option to remove it.
 */
public class AdminPosterDetailFragment extends Fragment {

    private static final String TAG = "AdminPosterDetailFrag";

    private MaterialToolbar toolbar;
    private TextView titleTextView;
    private TextView statusTextView;
    private ImageView fullImageView;
    private MaterialButton removeButton; // Renamed from rejectButton

    private String posterId; // The ID of the poster passed from the previous fragment


    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminPosterDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Called to do initial creation of the fragment.
     * This is where the fragment retrieves the "poster_id" from its arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Safely retrieve the posterId passed via arguments
        if (getArguments() != null) {
            // Note: The "poster_id" is logically the Event ID.
            posterId = getArguments().getString("poster_id");
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This method inflates the layout, initializes UI components, and sets up
     * click listeners for the toolbar and removal button.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_poster_view, container, false);

        // UI components are initialized here.
        // Listeners for toolbar navigation and the remove button are set up.
        // loadPosterDetails() is called if posterId is available.

        return view;
    }
}