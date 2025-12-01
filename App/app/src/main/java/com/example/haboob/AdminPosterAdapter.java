/**
 Adapter to displays all posters for an admin
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter for displaying Event posters in a RecyclerView for administrative management.
 * This adapter uses the Glide library to asynchronously load poster images from URLs.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Asynchronous image loading with Glide</li>
 *   <li>Placeholder and error image handling</li>
 *   <li>Click handling for individual poster cards</li>
 *   <li>Automatic fallback to default image if loading fails</li>
 * </ul>
 *
 * <p>The adapter retrieves image URLs from the Poster's getData() field and loads them
 * asynchronously. If an image cannot be loaded or does not exist, a default placeholder
 * is displayed instead.</p>
 *
 * @author Jeff
 * @version 1.0
 * @see Event
 * @see Poster
 * @see RecyclerView.Adapter
 */
public class AdminPosterAdapter extends RecyclerView.Adapter<AdminPosterAdapter.ViewHolder> {

    /** List of Event objects whose posters will be displayed */
    private final List<Event> eventList;

    /** Listener for handling click events on poster cards */
    private final OnPosterClickListener listener;

    /**
     * Interface for handling click events on poster cards.
     * Implement this interface to respond to user clicks on poster items.
     */
    public interface OnPosterClickListener {
        /**
         * Called when a poster card is clicked.
         *
         * @param event The Event object whose poster was clicked
         */
        void onPosterClick(Event event);
    }

    /**
     * Constructs an AdminPosterAdapter with the specified event list and click listener.
     *
     * @param eventList The list of Event objects with posters to display
     * @param listener The click listener for handling poster card clicks
     */
    public AdminPosterAdapter(List<Event> eventList, OnPosterClickListener listener) {
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
     * Binds event data to the ViewHolder, including loading poster images via Glide.
     *
     * <p>This method performs the following operations:</p>
     * <ul>
     *   <li>Sets the event title in the title TextView</li>
     *   <li>Retrieves the poster image URL from the Poster's getData() field</li>
     *   <li>Uses Glide to asynchronously load the image from the URL</li>
     *   <li>Applies placeholder and error images as needed</li>
     *   <li>Sets up click listeners for the poster card</li>
     * </ul>
     *
     * <p>If the poster URL is invalid, empty, or fails to load, a default shrug
     * drawable is displayed. All loading errors are logged for debugging.</p>
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        Poster poster = event.getPoster();

        // Default image resource ID
        int defaultImageResId = R.drawable.shrug;

        // Bind data using Event details
        holder.posterTitle.setText(event.getEventTitle());

        // Always set the default image or clear the view before attempting to load a new one
        holder.posterImage.setImageResource(defaultImageResId);

        if (poster != null) {
            // FIX: Get the URL string from the getData() field, not the getImgSource() field.
            Object imageSource = poster.getData();

            if (imageSource instanceof String && !((String) imageSource).isEmpty()) {
                String imageUrl = (String) imageSource;

                // Use Glide to load the image URL
                try {
                    Glide.with(holder.posterImage.getContext())
                            .load(imageUrl)
                            .placeholder(defaultImageResId)
                            .error(defaultImageResId)
                            .into(holder.posterImage);
                    Log.d("AdminAdapter", "Loading image URL: " + imageUrl);
                } catch (Exception e) {
                    Log.e("AdminAdapter", "Glide failed to load image for " + event.getEventID(), e);
                    // Fallback to default if image loading fails
                    holder.posterImage.setImageResource(defaultImageResId);
                }
            } else {
                Log.w("AdminAdapter", "Poster image source for " + event.getEventID() + " is null or not a valid URL string in data field. Using default shrug.");
            }
        }

        // The click passes the parent Event object
        holder.posterCard.setOnClickListener(v -> listener.onPosterClick(event));

        // Checkbox listener

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of Event items with posters
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class for caching view references within each poster card.
     * Improves scrolling performance by avoiding repeated findViewById calls.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** The card view containing all poster information */
        public final MaterialCardView posterCard;

        /** ImageView for displaying the poster image */
        public final ImageView posterImage;

        /** TextView for displaying the poster/event title */
        public final TextView posterTitle;

        /**
         * Constructs a ViewHolder and caches all child view references.
         *
         * @param view The root view of the item layout
         */
        public ViewHolder(View view) {
            super(view);
            posterCard = view.findViewById(R.id.card_admin_poster);
            posterImage = view.findViewById(R.id.poster_image_view);
            posterTitle = view.findViewById(R.id.poster_title_text_view);

        }
    }
}