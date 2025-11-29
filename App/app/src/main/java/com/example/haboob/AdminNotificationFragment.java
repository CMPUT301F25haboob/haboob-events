package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying all notifications from all users in the system.
 * Allows administrators to view and monitor all notification activity.
 *
 * @author Haboob Team
 * @version 1.0
 */
public class AdminNotificationFragment extends Fragment {

    private static final String TAG = "AdminNotificationFrag";

    private RecyclerView recyclerView;
    private AdminNotificationAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private List<AdminNotificationAdapter.NotificationWithUser> allNotifications;

    public AdminNotificationFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_notification_fragment, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        allNotifications = new ArrayList<>();

        // Initialize views
        recyclerView = view.findViewById(R.id.admin_notifications_recycler);
        progressBar = view.findViewById(R.id.progress_bar_admin_notifications);
        emptyTextView = view.findViewById(R.id.tv_admin_notifications_empty);
        toolbar = view.findViewById(R.id.notificationTopAppBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminNotificationAdapter(notification -> {
            // Handle notification click
            Toast.makeText(getContext(),
                    "Notification to: " + notification.getRecipientName(),
                    Toast.LENGTH_SHORT).show();

            // Optional: Navigate to event details or user details
            // You can implement navigation here if needed
        });
        recyclerView.setAdapter(adapter);

        // Setup toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_admin);
                return true;
            }
            return false;
        });

        // Load all notifications
        loadAllNotifications();

        return view;
    }

    /**
     * Loads all notifications from all users in the database.
     * First fetches all users, then fetches notifications for each user.
     */
    private void loadAllNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // First, get all users
        db.collection("users")
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    List<DocumentSnapshot> users = userSnapshot.getDocuments();
                    int totalUsers = users.size();
                    final int[] processedUsers = {0};

                    Log.d(TAG, "Found " + totalUsers + " users");

                    // For each user, fetch their notifications
                    for (DocumentSnapshot userDoc : users) {
                        String userId = userDoc.getId();
                        String firstName = userDoc.getString("first_name");
                        String lastName = userDoc.getString("last_name");
                        String userName = (firstName != null && lastName != null)
                                ? firstName + " " + lastName
                                : userId;

                        // Get notifications for this user
                        db.collection("users")
                                .document(userId)
                                .collection("notifications")
                                .get()
                                .addOnSuccessListener(notificationSnapshot -> {
                                    // Process notifications for this user
                                    for (DocumentSnapshot notifDoc : notificationSnapshot.getDocuments()) {
                                        Notification notification = notifDoc.toObject(Notification.class);
                                        if (notification != null) {
                                            AdminNotificationAdapter.NotificationWithUser withUser =
                                                    new AdminNotificationAdapter.NotificationWithUser(
                                                            notification, userId, userName);
                                            allNotifications.add(withUser);
                                        }
                                    }

                                    // Check if we've processed all users
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        displayNotifications();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading notifications for user: " + userId, e);
                                    processedUsers[0]++;
                                    if (processedUsers[0] == totalUsers) {
                                        displayNotifications();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Failed to load users: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Displays the loaded notifications in the RecyclerView.
     * Sorts notifications by timestamp (newest first).
     */
    private void displayNotifications() {
        progressBar.setVisibility(View.GONE);

        if (allNotifications.isEmpty()) {
            showEmptyState();
            return;
        }

        // Sort by timestamp (newest first)
        allNotifications.sort((n1, n2) -> {
            if (n1.getNotification().getTimeCreated() == null) return 1;
            if (n2.getNotification().getTimeCreated() == null) return -1;
            return n2.getNotification().getTimeCreated()
                    .compareTo(n1.getNotification().getTimeCreated());
        });

        adapter.setNotifications(allNotifications);
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        Log.d(TAG, "Displaying " + allNotifications.size() + " total notifications");
    }

    /**
     * Shows the empty state when no notifications are found
     */
    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("No notifications found");
    }
}