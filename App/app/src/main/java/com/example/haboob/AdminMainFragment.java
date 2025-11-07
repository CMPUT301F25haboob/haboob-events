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
        return inflater.inflate(R.layout.admin_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find buttons
        Button viewPostersButton = view.findViewById(R.id.admin_view_posters_button);
        Button viewEventsButton = view.findViewById(R.id.admin_view_events_button);
        Button viewUsersButton = view.findViewById(R.id.admin_view_users_button);
        Button sendNotificationsButton = view.findViewById(R.id.admin_view_notifications_button);
        ImageButton viewBackButton = view.findViewById(R.id.btn_admin_back);

        viewBackButton.setOnClickListener(v-> goToMain());





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


        /* tmp
        // Set click listener for View Events
        viewEventsButton.setOnClickListener(v -> {
            try {
                // TODO: Replace with your actual action ID from mobile_navigation.xml
                NavHostFragment.findNavController(AdminMainFragment.this)
                        .navigate(R.id.navigation_admin_events);
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
                        .navigate(R.id.navigation_admin_users);
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
                        .navigate(R.id.navigation_admin_notifications);
            } catch (Exception e) {
                Log.e("AdminMainFragment", "Navigation to notifications failed. Is the action ID set?", e);
                Toast.makeText(getContext(), "Send Notifications (Not Implemented)", Toast.LENGTH_SHORT).show();
            }
        }); */
    }


    private void goToMain(){
        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_home);
    }
}