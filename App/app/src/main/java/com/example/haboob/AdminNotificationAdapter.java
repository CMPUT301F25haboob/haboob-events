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
 * @author Haboob Team
 * @version 1.0
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private final List<NotificationWithUser> notifications;
    private final OnNotificationClickListener listener;

    /**
     * Interface for handling notification click events
     */
    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationWithUser notification);
    }

    /**
     * Wrapper class to store notification with user information
     */
    public static class NotificationWithUser {
        private final Notification notification;
        private final String recipientId;
        private final String recipientName;

        public NotificationWithUser(Notification notification, String recipientId, String recipientName) {
            this.notification = notification;
            this.recipientId = recipientId;
            this.recipientName = recipientName;
        }

        public Notification getNotification() {
            return notification;
        }

        public String getRecipientId() {
            return recipientId;
        }

        public String getRecipientName() {
            return recipientName;
        }
    }

    /**
     * Constructs an AdminNotificationAdapter
     *
     * @param listener Click listener for notifications
     */
    public AdminNotificationAdapter(OnNotificationClickListener listener) {
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the adapter with new notification data
     *
     * @param newNotifications List of notifications with user info
     */
    public void setNotifications(List<NotificationWithUser> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_notification_content, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * ViewHolder for notification items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView recipientTextView;
        TextView eventIdTextView;
        TextView timestampTextView;
        View readStatusView;

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
