package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Main administrative panel fragment providing navigation to various admin sections.
 *
 * <p>This fragment serves as the central hub for administrative functions, offering
 * buttons to navigate to different management areas such as:</p>
 * <ul>
 *   <li>Viewing and managing event posters</li>
 *   <li>Viewing and managing events</li>
 *   <li>Viewing and managing users (commented out)</li>
 *   <li>Sending notifications to users (commented out)</li>
 * </ul>
 *
 * @author Jeff
 * @version 1.0
 * @see Fragment
 */
public class AdminMainFragment extends Fragment {

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminMainFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment's layout from XML.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_main_fragment, container, false);
    }

    /**
     * Sets up UI components and click listeners after the view is created.
     *
     * <p>This method initializes all navigation buttons and sets up their click
     * listeners to navigate to appropriate admin sections. Some features are
     * currently commented out (users, notifications).</p>
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState Previously saved state of the fragment, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find buttons
        Button viewPostersButton = view.findViewById(R.id.admin_view_posters_button);
        Button viewEventsButton = view.findViewById(R.id.admin_view_events_button);
        Button viewUsersButton = view.findViewById(R.id.admin_view_users_button);
        Button sendNotificationsButton = view.findViewById(R.id.admin_view_notifications_button);
        ImageButton viewBackButton = view.findViewById(R.id.btn_admin_back);

        viewBackButton.setOnClickListener(v-> goBack());

        // Set click listener for View Posters
        viewPostersButton.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.navigation_admin_posters);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to posters failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "View Posters (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for View Events
        viewEventsButton.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.navigation_admin_posters);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to events failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "View Events (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for View Users
        viewUsersButton.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.navigation_admin_users);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to events failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "View Events (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates back to the profile fragment.
     * This method is called when the back button is pressed.
     */
    private void goBack(){
        NavHostFragment.findNavController(this)
                .navigate(R.id.profile_fragment);
    }
}