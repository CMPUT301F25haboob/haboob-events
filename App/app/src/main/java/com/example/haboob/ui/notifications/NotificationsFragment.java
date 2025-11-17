package com.example.haboob.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.haboob.EventsList;
import com.example.haboob.Notification;
import com.example.haboob.NotificationManager;
import com.example.haboob.R;
import android.widget.Switch;


import java.util.ArrayList;

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
        EventsList eventsList = new EventsList(new EventsList.OnEventsLoadedListener() {
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
                    "Clicked: " + notification.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();

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

        /* TEST NOTIFICATION (sends notification whenever user navigates to notification fragment)
        // Build a notification
        Notification n = new Notification(
                "TEST_EVENT_NAME",
                "DEMO_ORG_ID",
                userId,
                "Welcome! This is a test notification."
        );

        NotificationManager nm = new NotificationManager();
        nm.sendToUser(n);  // write to: users/{deviceId}/notifications/{notificationId}
        */

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
