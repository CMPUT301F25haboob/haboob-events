/**
 Adapter to displays all notifications for an admin
 Copyright (C) 2025  jeff

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package com.example.haboob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying all notifications in the admin view.
 * Shows notifications from all users with recipient information.
 *
 * <p>This adapter displays notifications system-wide, including information about
 * who each notification was sent to, when it was sent, and whether it has been read.
 * It supports click handling for individual notifications.</p>
 *
 * @author Jeff
 * @version 1.0
 * @see Notification
 * @see RecyclerView.Adapter
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    /** List of notifications with associated user information */
    private final List<NotificationWithUser> notifications;

    /** Listener for handling notification click events */
    private final OnNotificationClickListener listener;

    /**
     * Interface for handling notification click events.
     * Implement this interface to respond to clicks on notification items.
     */
    public interface OnNotificationClickListener {
        /**
         * Called when a notification is clicked.
         *
         * @param notification The NotificationWithUser object that was clicked
         */
        void onNotificationClick(NotificationWithUser notification);
    }

    /**
     * Wrapper class to store notification with user information.
     * This class associates a notification with its recipient's ID and name.
     */
    public static class NotificationWithUser {
        /** The notification object */
        private final Notification notification;

        /** The device ID of the notification recipient */
        private final String recipientId;

        /** The display name of the notification recipient */
        private final String recipientName;

        /**
         * Constructs a NotificationWithUser wrapper.
         *
         * @param notification The notification object
         * @param recipientId The recipient's device ID
         * @param recipientName The recipient's display name
         */
        public NotificationWithUser(Notification notification, String recipientId, String recipientName) {
            this.notification = notification;
            this.recipientId = recipientId;
            this.recipientName = recipientName;
        }

        /**
         * Gets the notification object.
         *
         * @return The notification
         */
        public Notification getNotification() {
            return notification;
        }

        /**
         * Gets the recipient's device ID.
         *
         * @return The recipient ID
         */
        public String getRecipientId() {
            return recipientId;
        }

        /**
         * Gets the recipient's display name.
         *
         * @return The recipient name
         */
        public String getRecipientName() {
            return recipientName;
        }
    }

    /**
     * Constructs an AdminNotificationAdapter.
     *
     * @param listener Click listener for notifications
     */
    public AdminNotificationAdapter(OnNotificationClickListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter with new notification data.
     * Clears existing notifications and replaces them with the provided list.
     *
     * @param newNotifications List of notifications with user info to display
     */
    public void setNotifications(List<NotificationWithUser> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder when RecyclerView needs a new item view.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder for notification items
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_notification_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds notification data to the specified ViewHolder.
     *
     * <p>This method populates the ViewHolder with:</p>
     * <ul>
     *   <li>Notification message text</li>
     *   <li>Recipient name or ID</li>
     *   <li>Associated event ID</li>
     *   <li>Formatted timestamp</li>
     *   <li>Read status indicator</li>
     * </ul>
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationWithUser item = notifications.get(position);
        Notification notification = item.getNotification();

        // Set message
        String message = notification.getMessage();
        holder.messageTextView.setText(message != null ? message : "Empty Notification");

        // Set recipient info
        String recipientInfo = item.getRecipientName() != null
                ? item.getRecipientName()
                : item.getRecipientId();
        holder.recipientTextView.setText("To: " + recipientInfo);

        // Set event ID
        String eventId = notification.getEventId();
        holder.eventIdTextView.setText("Event: " + (eventId != null ? eventId : "N/A"));

        // Set timestamp
        Date timeCreated = notification.getTimeCreated();
        if (timeCreated != null) {
            String formatted = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT,
                    Locale.getDefault()
            ).format(timeCreated);
            holder.timestampTextView.setText(formatted);
        } else {
            holder.timestampTextView.setText("");
        }

        // Set read status indicator
        holder.readStatusView.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    /**
     * Returns the total number of notifications in the adapter.
     *
     * @return The number of notification items
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * ViewHolder for notification items.
     * Caches references to all views in a notification item for efficient recycling.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the notification message */
        TextView messageTextView;

        /** TextView displaying the recipient information */
        TextView recipientTextView;

        /** TextView displaying the associated event ID */
        TextView eventIdTextView;

        /** TextView displaying the notification timestamp */
        TextView timestampTextView;

        /** View indicating whether the notification has been read */
        View readStatusView;

        /**
         * Constructs a ViewHolder and caches all child view references.
         *
         * @param itemView The root view of the notification item layout
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_admin_notif_message);
            recipientTextView = itemView.findViewById(R.id.tv_admin_notif_recipient);
            eventIdTextView = itemView.findViewById(R.id.tv_admin_notif_event);
            timestampTextView = itemView.findViewById(R.id.tv_admin_notif_timestamp);
            readStatusView = itemView.findViewById(R.id.view_admin_notif_read_status);
        }
    }
}
