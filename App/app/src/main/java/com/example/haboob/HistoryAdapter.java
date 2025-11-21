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
 * Adapter for displaying event history with images and titles
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Event> events;
    private OnEventClickListener clickListener;

    public interface OnEventClickListener {
        void onEventClick(String eventId);
    }

    public HistoryAdapter(List<Event> events, OnEventClickListener clickListener) {
        this.events = events != null ? events : new ArrayList<>();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_event, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventDescription;

        ViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
        }
    }
}
