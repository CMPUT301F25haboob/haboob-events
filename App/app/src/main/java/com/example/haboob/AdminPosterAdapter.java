package com.example.haboob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter displays Event posters for admin management.
 * It now uses a library (Glide) to load image URLs asynchronously.
 */
public class AdminPosterAdapter extends RecyclerView.Adapter<AdminPosterAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final OnPosterClickListener listener;

    public interface OnPosterClickListener {
        void onPosterClick(Event event);
    }

    public AdminPosterAdapter(List<Event> eventList, OnPosterClickListener listener) {
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
        Poster poster = event.getPoster();

        // Default image resource ID
        int defaultImageResId = R.drawable.shrug;

        // Bind data using Event details
        holder.posterTitle.setText(event.getEventTitle());
        holder.posterSelectCheckbox.setChecked(false);

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
        holder.posterSelectCheckbox.setOnClickListener(v -> {
            Log.d("Adapter", "Checkbox clicked for event: " + event.getEventID() + ". New state: " + holder.posterSelectCheckbox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class remains the same
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView posterCard;
        public final ImageView posterImage;
        public final TextView posterTitle;
        public final CheckBox posterSelectCheckbox;

        public ViewHolder(View view) {
            super(view);
            posterCard = view.findViewById(R.id.card_admin_poster);
            posterImage = view.findViewById(R.id.poster_image_view);
            posterTitle = view.findViewById(R.id.poster_title_text_view);
            posterSelectCheckbox = view.findViewById(R.id.poster_select_checkbox);
        }
    }
}