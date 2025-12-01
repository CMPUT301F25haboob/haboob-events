package com.example.haboob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying event history with images and titles.
 * This adapter manages the display of Event objects in a RecyclerView,
 * showing event posters, titles, and descriptions.
 *
 * @author Dan
 * @version 1.0
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Event> events;
    private OnEventClickListener clickListener;

    /**
     * Interface for handling event click callbacks.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param eventId The ID of the clicked event
         */
        void onEventClick(String eventId);
    }

    /**
     * Constructs a new HistoryAdapter.
     *
     * @param events The list of events to display
     * @param clickListener The listener for handling event clicks
     */
    public HistoryAdapter(List<Event> events, OnEventClickListener clickListener) {
        this.events = events != null ? events : new ArrayList<>();
        this.clickListener = clickListener;
    }

    /**
     * Creates a new ViewHolder for event items.
     *
     * @param parent The parent ViewGroup
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View for an event item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at the specified position.
     * Sets the event title, description, image, and click listener.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        // Set event title
        holder.eventTitle.setText(event.getEventTitle());

        // Set event description
        String description = event.getEventDescription();
        if (description != null && !description.isEmpty()) {
            holder.eventDescription.setVisibility(View.VISIBLE);
            holder.eventDescription.setText(description);
        } else {
            holder.eventDescription.setVisibility(View.GONE);
        }

        // Load event image
        Poster poster = event.getPoster();
        if (poster != null && poster.getData() != null && !poster.getData().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(poster.getData())
                    .placeholder(R.drawable.ic_search_24)
                    .into(holder.eventImage);
        } else {
            // Load placeholder
            Glide.with(holder.itemView.getContext())
                    .load("https://media.tenor.com/hG6eR9HM_fkAAAAM/the-simpsons-homer-simpson.gif")
                    .into(holder.eventImage);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onEventClick(event.getEventID());
            }
        });
    }

    /**
     * Returns the total number of events in the adapter.
     *
     * @return The number of events
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Updates the adapter with a new list of events.
     *
     * @param newEvents The new list of events to display
     */
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for event history items.
     * Holds references to the views for each event item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventDescription;

        /**
         * Constructs a ViewHolder and initializes view references.
         *
         * @param itemView The view for the event item
         */
        ViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
        }
    }
}