package com.example.haboob.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Notification;
import com.example.haboob.NotificationManager;
import com.example.haboob.R;
import com.example.haboob.ui.home.EventViewerFragment;

import android.widget.Switch;


import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private NotificationManager notificationManager;
    private String userId;

    // Mute toggle + prefs
    private Switch muteSwitch;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String KEY_MUTED = "notifications_muted";
    EventsList eventsList; //  David - init eventsList because java gets angry if I dont

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // SharedPreferences for mute state
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Mute toggle setup
        muteSwitch = view.findViewById(R.id.switch_mute_notifications);
        boolean isMuted = prefs.getBoolean(KEY_MUTED, false);
        muteSwitch.setChecked(isMuted);

        muteSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.edit().putBoolean(KEY_MUTED, checked).apply();

            if (checked) {
                // Mute ON -> fill display with empty list
                adapter.setData(new ArrayList<>());
                Toast.makeText(requireContext(), "Notifications muted", Toast.LENGTH_SHORT).show();
            } else {
                // Mute OFF -> load notifications from firestore
                loadNotifications();
                Toast.makeText(requireContext(), "Notifications unmuted", Toast.LENGTH_SHORT).show();
            }
        });

        // Event data is now in memory; refresh rows to show titles
        eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                // Event data is now in memory; refresh rows to show titles
                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) { /* log */ }
        });


        recyclerView = view.findViewById(R.id.notifications_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new NotificationsAdapter(eventsList, (com.example.haboob.Notification notification) -> {
            Toast.makeText(
                    requireContext(),
                    "Clicked Notification",
                    Toast.LENGTH_SHORT
            ).show();

            // TODO: David implements navigating to the event in EntrantMainFragment to accept the event invitation
            // find the event clicked(the new events list should be updated with the database data):

            String eventID = notification.getEventId(); // grab the eventID corresponding to the notification
            assert eventID != null;
            assert eventsList != null;
            assert userId != null;

            // search through the eventsList, find the event with the matching event that matches the notification
            for (Event event : eventsList.getEventsList()) {
                if (event.getEventID().equals(eventID)) {

                    Log.d("TAG", "Event clicked equalled event ID in notificationFragment, Event clicked: " + event.getEventTitle());

                    // Create a Bundle to pass data to the EventViewerFragment
                    Bundle args = new Bundle();
                    args.putString(EventViewerFragment.ARG_EVENT_ID, eventID);
                    args.putString("device_id", userId);

//                    if (notification.getType().equals("waitlist_left")) {
//                        args.putString("waitlist_notif", "waitlist_left"); // enables waitlist buttons for joining
//                    }
//                    else if (notification.getType().equals("waitlist_joined")){
//                        args.putString("waitlist_notif", "waitlist_joined"); // enables waitlist buttons for leaving
//                    }
//                    args.putBoolean("won_lottery", true); // sets JoinEvent button invisible

                    // navigate to the EventViewerFragment using the NavController
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.entrant_event_view, args);
                }
            }

            // TODO: Later -> navigate to NotificationDetailFragment
            // Bundle args = new Bundle();
            // args.putString("message", notification.getMessage());
            // args.putString("eventId", notification.getEventId());
            // args.putString("organizerId", notification.getOrganizerId());
            // args.putLong("timeCreated", notification.getTimeCreated().getTime());
            // Navigation.findNavController(view)
            //      .navigate(R.id.action_notificationsFragment_to_notificationDetailFragment, args);
        });

        recyclerView.setAdapter(adapter);
        notificationManager = new NotificationManager();

//        TEST NOTIFICATION (sends notification whenever user navigates to notification fragment)
//         Build a notification
//        Notification n = new Notification(
//                "9b776c05-910f-4102-8342-2a921bc42d04",
//                "DEMO_ORG_ID",
//                userId,
//                "You're invited to join this event!"
//        );

//        NotificationManager nm = new NotificationManager();
//        nm.sendToUser(n);  // write to: users/{deviceId}/notifications/{notificationId}


        loadNotifications();
    }

    private void loadNotifications() {
        boolean muted = prefs.getBoolean(KEY_MUTED, false);

        if (muted) {
            // If muted keep the list empty
            adapter.setData(new ArrayList<>());
            return;
        }

        notificationManager.getUserNotifications(userId, new NotificationManager.NotificationsCallback() {
            @Override
            public void onSuccess(ArrayList<Notification> notifications) {
                adapter.setData(notifications);
                Log.d("NotificationsFragment", "Loaded " + notifications.size() + " notifications");
            }

            @Override
            public void onError(Exception e) {
                Log.e("NotificationsFragment", "Failed to load notifications", e);
                Toast.makeText(requireContext(),
                        "Failed to load notifications.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
