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
 * Adapter modified to display Event items in the Admin view.
 */
// Renamed class internally to AdminEventAdapter for logical clarity
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final OnEventClickListener listener;

    // Interface for click handling updated for Event
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public AdminEventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_poster_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Update to use Event getters
        holder.posterTitle.setText(event.getEventTitle()); // Uses getEventTitle()

        // Placeholder Status: Assuming getEventID() can serve as a temporary status/detail
        holder.posterStatus.setText("ID: " + event.getEventID());

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

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class remains the same as it references UI elements
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView posterCard;
        public final ImageView posterImage;
        public final TextView posterTitle;
        public final TextView posterStatus;

        public ViewHolder(View view) {
            super(view);
            posterCard = view.findViewById(R.id.card_admin_poster);
            posterImage = view.findViewById(R.id.poster_image_view);
            posterTitle = view.findViewById(R.id.event_title);
            posterStatus = view.findViewById(R.id.poster_select_checkbox);
        }
    }
}