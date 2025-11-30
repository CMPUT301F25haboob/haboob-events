package com.example.haboob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying Event items in RecyclerView within the Admin view.
 * This adapter binds Event objects to card views for administrative management.
 *
 * <p>This adapter is specifically designed for admin functionality where events
 * need to be displayed with their titles, IDs, and poster images in a grid or list format.</p>
 *
 * @author Jeff
 * @version 1.0
 * @see Event
 * @see RecyclerView.Adapter
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    /** List of Event objects to be displayed in the RecyclerView */
    private final List<Event> eventList;

    /** Listener for handling click events on event cards */
    private final OnEventClickListener listener;

    /**
     * Interface for handling click events on Event items.
     * Implement this interface to respond to user clicks on event cards.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event card is clicked.
         *
         * @param event The Event object that was clicked
         */
        void onEventClick(Event event);
    }

    /**
     * Constructs an AdminEventAdapter with the specified event list and click listener.
     *
     * @param eventList The list of Event objects to display
     * @param listener The click listener for handling event card clicks
     */
    public AdminEventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder when RecyclerView needs a new item view.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused in this adapter)
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_poster_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to the specified ViewHolder.
     *
     * <p>This method sets the event title, displays the event ID as status,
     * loads a placeholder poster image, and sets up the click listener.</p>
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Update to use Event getters
        holder.posterTitle.setText(event.getEventTitle()); // Uses getEventTitle()


        // Placeholder for image loading: Event is assumed to have a getPoster() method
        // which contains the image data (based on Event.class bytecode)
        if (event.getPoster() != null) {
            // Since we cannot use an external library like Picasso, we set a default drawable
            holder.posterImage.setImageResource(R.drawable.shrug);
        } else {
            holder.posterImage.setImageResource(R.drawable.shrug);
        }

        // Set the click listener on the entire card, updated for Event
        holder.posterCard.setOnClickListener(v -> listener.onEventClick(event));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of Event items
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for caching view references within each event card.
     * This improves scrolling performance by avoiding repeated findViewById calls.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The card view containing all event information
         */
        public final MaterialCardView posterCard;
        /**
         * ImageView for displaying the event's poster image
         */
        public final ImageView posterImage;
        /**
         * TextView for displaying the event title
         */
        public final TextView posterTitle;
        /**
         * TextView for displaying event status or ID information
         */

        /**
         * Constructs a ViewHolder and caches all child view references.
         *
         * @param view The root view of the item layout
         */
        public ViewHolder(View view) {
            super(view);
            posterCard = view.findViewById(R.id.card_admin_poster);
            posterImage = view.findViewById(R.id.poster_image_view);
            posterTitle = view.findViewById(R.id.event_title);
        }
    }
}