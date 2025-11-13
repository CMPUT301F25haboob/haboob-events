package com.example.haboob.ui.notifications;

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

import com.example.haboob.Notification;
import com.example.haboob.NotificationManager;
import com.example.haboob.R;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private NotificationManager notificationManager;
    private String userId;

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

        recyclerView = view.findViewById(R.id.notifications_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new NotificationsAdapter(notification -> {
            // On item click
            Toast.makeText(requireContext(),
                    "Clicked: " + notification.getMessage(),
                    Toast.LENGTH_SHORT).show();

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

        /* TEST NOTIFICATION
        // Build a notification
        Notification n = new Notification(
                "DEMO_EVENT_ID",
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
