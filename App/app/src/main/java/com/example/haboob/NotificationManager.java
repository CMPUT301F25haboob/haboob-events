package com.example.haboob;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationManager {

    private final FirebaseFirestore db;

    public NotificationManager() {
        db = FirebaseFirestore.getInstance();
    }

    public interface NotificationsCallback {
        void onSuccess(ArrayList<Notification> notifications);
        void onError(Exception e);
    }

    // Send notification to a single user by adding it to that user's notifications sub collection
    // NOTIFICATION OBJECT PASSED TO THIS METHOD MUST HAVE A SET RECIPIENT ID
    public void sendToUser(Notification notification) {
        if (notification == null) {
            Log.w("NotificationManager", "Cannot send notification: notification is null.");
            return;
        }
        if (notification.getRecipientId() == null ||
                notification.getRecipientId().trim().isEmpty() ||
                Notification.DEFAULT_RECIPIENT_ID.equals(notification.getRecipientId())) {
            Log.w("NotificationManager", "Cannot send: recipientId is missing/DEFAULT.");
            return;
        }
        logOrganizerNotification(notification.getOrganizerId(), notification);

        // Build the Firestore path users/{userId}/notifications/{notificationId} and set to notiRef
        DocumentReference notifRef = db.collection("users")
                .document(notification.getRecipientId())
                .collection("notifications")
                .document(notification.getNotificationId());

        // Write the notification to firestore through notiRef reference
        notifRef.set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d("NotificationManager", "Notification sent to user: " + notification.getRecipientId());
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationManager", "Failed to send notification to user: " + notification.getRecipientId(), e);
                });
    }

    // Send notifications to all users in a given list by adding it to the users notifications sub collection
    public void sendToList(ArrayList<String> recipientIds, String organizerId, Notification notification) {
        for (String id : recipientIds) {
            Notification userNotification = new Notification(
                    notification.getEventId(),
                    notification.getOrganizerId(),
                    id,
                    notification.getMessage()
            );
            userNotification.setNotificationId(notification.getNotificationId());

            sendToUser(userNotification);
        }
    }

    // Log a sent notification in an organizer's notification subcollection
    public void logOrganizerNotification(String organizerId, Notification notification) {
        if (notification == null) {
            Log.w("NotificationManager", "Cannot log: notification is null.");
            return;
        }

        // ensure the model records the sender
        if (notification.getOrganizerId() == null || notification.getOrganizerId().isEmpty()) {
            notification.setOrganizerId(organizerId);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference logRef = db.collection("users")
                .document(organizerId)
                .collection("sentNotifications")
                .document(notification.getNotificationId());

        logRef.set(notification)
                .addOnSuccessListener(aVoid ->
                        Log.d("NotificationManager", "Logged organizer notification: " + notification.getNotificationId()))
                .addOnFailureListener(e ->
                        Log.e("NotificationManager", "Failed to log organizer notification.", e));
    }

    // fetch a user's notifications, newest first to display
    public void getUserNotifications(@NonNull String userId, @NonNull NotificationsCallback callback) {
        if (userId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("userId is empty"));
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timeCreated", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    ArrayList<Notification> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) { list.add(n); }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    // Mark a users notification as read
    public void markAsRead(String recipientId, String notificationId) {
        // TODO
    }









}
