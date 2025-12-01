package com.example.haboob;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Handles creating, sending, logging, and retrieving {@link Notification} objects
 * for users and organizers using Firestore.
 */
public class NotificationManager {

    private final FirebaseFirestore db;


    /**
     * Constructor for testing
     */
    public NotificationManager(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Initializes the NotificationManager with a Firestore instance.
     */
    public NotificationManager() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Callback for returning user notifications or handling errors.
     */
    public interface NotificationsCallback {
        void onSuccess(ArrayList<Notification> notifications);
        void onError(Exception e);
    }

    /**
     * Sends a notification to a single user by writing it to
     * users/{recipientId}/notifications/{notificationId}.
     *
     * @param notification the notification to send (must have recipientId set)
     */
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

    /**
     * Sends the given notification to every user ID in the list.
     * Each user receives their own copy under their notifications subcollection.
     *
     * @param recipientIds list of user IDs to receive the notification
     * @param organizerId  ID of the sender
     * @param notification base notification to duplicate for each user
     */
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

    /**
     * Logs a copy of a notification under the organizer's sentNotifications
     * subcollection for record-keeping.
     *
     * @param organizerId ID of the organizer sending the notification
     * @param notification the notification to log
     */
    public void logOrganizerNotification(String organizerId, Notification notification) {
        if (notification == null) {
            Log.w("NotificationManager", "Cannot log: notification is null.");
            return;
        }

        // ensure the model records the sender
        if (notification.getOrganizerId() == null || notification.getOrganizerId().isEmpty()) {
            notification.setOrganizerId(organizerId);
        }

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

    /**
     * Fetches all notifications for a user, ordered newest first.
     *
     * @param userId   the user's ID
     * @param callback callback returning the list or an error
     */
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
}
