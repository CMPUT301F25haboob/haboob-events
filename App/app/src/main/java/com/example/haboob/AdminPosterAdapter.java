package com.example.haboob;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // New Import
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter displays Event posters for admin management.
 * It holds a list of Events but binds the associated Poster data.
 */
public class AdminPosterAdapter extends RecyclerView.Adapter<AdminPosterAdapter.ViewHolder> {

    // --- MOCK POSTER URL -> RESOURCE MAP ---
    private static final Map<String, Integer> URL_KEYWORD_RESOURCES = new HashMap<>();
    static {
        URL_KEYWORD_RESOURCES.put("hockey", R.drawable.hockey_ex);
        URL_KEYWORD_RESOURCES.put("swim", R.drawable.swimming_lessons);
        URL_KEYWORD_RESOURCES.put("ross", R.drawable.bob_ross);
        URL_KEYWORD_RESOURCES.put("clash", R.drawable.clash_royale);
    }
    // --- END MOCK POSTER URL -> RESOURCE MAP ---

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
                .inflate(R.layout.admin_poster_single, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        Poster poster = event.getPoster();
        String imageUrl = (poster != null) ? poster.getImageUrl() : null;

        // Bind data using Event details
        holder.posterTitle.setText(event.getEventTitle());

        // CheckBox is used for selection, not status display. Set its initial state.
        holder.posterSelectCheckbox.setChecked(false); // Default to unchecked

        // Image loading simulation using URL keyword matching
        Integer imageResId = null;
        if (imageUrl != null) {
            String lowerCaseUrl = imageUrl.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, Integer> entry : URL_KEYWORD_RESOURCES.entrySet()) {
                if (lowerCaseUrl.contains(entry.getKey())) {
                    imageResId = entry.getValue();
                    break;
                }
            }
        }

        if (imageResId != null) {
            holder.posterImage.setImageResource(imageResId);
        } else {
            // Fallback for URLs not matching keywords or null posters
            holder.posterImage.setImageResource(R.drawable.shrug);
        }

        // The click passes the parent Event object
        holder.posterCard.setOnClickListener(v -> listener.onPosterClick(event));

        // OPTIONAL: If the checkbox itself needs a listener for bulk actions, implement it here.
        holder.posterSelectCheckbox.setOnClickListener(v -> {
            // Example: Log or track the selection status of the Event object here
            Log.d("Adapter", "Checkbox clicked for event: " + event.getEventID() + ". New state: " + holder.posterSelectCheckbox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class updated for CheckBox
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView posterCard;
        public final ImageView posterImage;
        public final TextView posterTitle;
        public final CheckBox posterSelectCheckbox; // Changed from TextView to CheckBox

        public ViewHolder(View view) {
            super(view);
            posterCard = view.findViewById(R.id.card_admin_poster);
            posterImage = view.findViewById(R.id.poster_image_preview);
            posterTitle = view.findViewById(R.id.poster_detail_title);
            posterSelectCheckbox = view.findViewById(R.id.poster_select_checkbox); // Updated ID
        }
    }
}