package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

/**
 * A simple {@link Fragment} subclass for the main admin panel.
 * It provides buttons to navigate to other admin sections.
 */
public class AdminMainFragment extends Fragment {

    public AdminMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find buttons
        Button viewPostersButton = view.findViewById(R.id.admin_view_posters_button);
        Button viewEventsButton = view.findViewById(R.id.admin_view_events_button);
        Button viewUsersButton = view.findViewById(R.id.admin_view_users_button);
        Button sendNotificationsButton = view.findViewById(R.id.admin_view_notifications_button);

        // Set click listener for View Posters
        viewPostersButton.setOnClickListener(v -> {
            try {
                // TODO: Replace with your actual action ID from mobile_navigation.xml
                // Example: <action android:id="@+id/action_adminMainFragment_to_adminPostersFragment" ... />
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
                // TODO: Replace with your actual action ID from mobile_navigation.xml
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.action_adminMainFragment_to_adminEventsFragment);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to events failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "View Events (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for View Users
        viewUsersButton.setOnClickListener(v -> {
            try {
                // TODO: Replace with your actual action ID from mobile_navigation.xml
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.action_adminMainFragment_to_adminUsersFragment);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to users failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "View Users (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for Send Notifications
        sendNotificationsButton.setOnClickListener(v -> {
            try {
                // TODO: Replace with your actual action ID from mobile_navigation.xml
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.action_adminMainFragment_to_adminNotificationsFragment);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to notifications failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "Send Notifications (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}