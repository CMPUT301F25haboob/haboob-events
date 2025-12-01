package com.example.haboob.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.Notification;
import com.example.haboob.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying notifications.
 * Shows notification messages, associated event titles, and timestamps.
 *
 * Features:
 * - Displays notification message, event name, and creation time
 * - Formats timestamps using locale-specific formatting
 * - Handles click events on notifications
 * - Dynamically updates notification list
 *
 * @author Owen
 * @version 1.0
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    /**
     * List of notifications to display.
     */
    private final ArrayList<Notification> data = new ArrayList<>();

    /**
     * Listener for handling notification click events.
     */
    private final OnNotificationClickListener listener;

    /**
     * EventsList for looking up event details.
     */
    private final EventsList eventsList;

    /**
     * Interface for handling notification click callbacks.
     */
    public interface OnNotificationClickListener {
        /**
         * Called when a notification item is clicked.
         *
         * @param notification The notification that was clicked
         */
        void onNotificationClick(com.example.haboob.Notification notification);
    }

    /**
     * Constructs a new NotificationsAdapter.
     *
     * @param eventsList The EventsList for looking up event information
     * @param listener The listener for handling notification clicks
     */
    public NotificationsAdapter(EventsList eventsList, OnNotificationClickListener listener) {
        this.eventsList = eventsList;
        this.listener = listener;
    }

    /**
     * Updates the adapter with a new list of notifications.
     *
     * @param newData The new list of notifications to display
     */
    public void setData(List<Notification> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for notification items.
     *
     * @param parent The parent ViewGroup
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View for a notification item
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    /**
     * Binds notification data to the ViewHolder at the specified position.
     * Sets the notification message, event title, timestamp, and click listener.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification n = data.get(position);
        holder.tvMessage.setText(n.getMessage() == null ? "Empty Notification" : n.getMessage());

        Date time = n.getTimeCreated();
        String formatted = time == null ? "" :
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(time);
        holder.tvTime.setText(formatted);

        // event title via in-memory list
        String title = "—";
        if (eventsList != null && n.getEventId() != null) {
            Event e = eventsList.getEventByID(n.getEventId());
            if (e != null && e.getEventTitle() != null && !e.getEventTitle().trim().isEmpty()) {
                title = e.getEventTitle();
            }
        }
        holder.tvEvent.setText("Event: " + title);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(n);
        });
    }

    /**
     * Returns the total number of notifications in the adapter.
     *
     * @return The number of notifications
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * ViewHolder class for notification items.
     * Holds references to the views for each notification.
     */
    static class VH extends RecyclerView.ViewHolder {
        /**
         * TextView displaying the notification message.
         */
        TextView tvMessage;

        /**
         * TextView displaying the creation timestamp.
         */
        TextView tvTime;

        /**
         * TextView displaying the associated event title.
         */
        TextView tvEvent;

        /**
         * Constructs a ViewHolder and initializes view references.
         *
         * @param itemView The view for the notification item
         */
        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvEvent = itemView.findViewById(R.id.tv_event);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}