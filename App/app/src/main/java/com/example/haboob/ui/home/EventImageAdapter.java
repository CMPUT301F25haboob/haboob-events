package com.example.haboob.ui.home;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bumptech.glide.Glide;
import com.example.haboob.R;

// Author: David T
// This code is an ImageAdapter for the RecyclerViews in entrant_main.xml

public class EventImageAdapter extends RecyclerView.Adapter<EventImageAdapter.ViewHolder> {


    public interface OnItemClick { void onClick(String eventId); }     // setting up the callback
    private final List<String> imageUrls = new ArrayList<>(); // always mutable
    private List<String> eventIDs = new ArrayList<>();
    private OnItemClick onItemClick;
    public EventImageAdapter(OnItemClick onItemClick) {     // Primary actor: caller supplies the click callback
        this.onItemClick = onItemClick;
    }
    public EventImageAdapter() { }// empty constructor}

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }
    // copy the input when constructed:
    public EventImageAdapter(List<String> initial) {
        if (initial != null) imageUrls.addAll(initial);     // copy into mutable list
    }
    public void replaceItems(Collection<String> newItems) {
        imageUrls.clear(); // we can only use clear() on a MUTABLE list, so imageURLs has to be mutable
        if (newItems != null) imageUrls.addAll(newItems);
        notifyDataSetChanged();
    }

    // ViewHolder class holds each ImageView
    // ViewHolder: a lightweight wrapper around the item view, caching child views so RecyclerView can scroll efficiently.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_event);
        }

    }

    // when querying the data is done, replace the items in the recyclerView with the new items from the database
    public void replaceItems(List<String> newItems) {
        imageUrls.clear();
        imageUrls.addAll(newItems);
        notifyDataSetChanged();
    }

    // when querying the data is done, replace the items in the recyclerView with the new items from the database
    public void inputIDs(List<String> eventIDs) {
        this.eventIDs.clear();
        this.eventIDs.addAll(eventIDs);
        notifyDataSetChanged();
        Log.d("TAG", "InputIDs ran, size: " + eventIDs.size() + " userIDs: " + eventIDs.toString());
    }

    @NonNull
    @Override
    // Create a new ViewHolder for each item in the RecyclerView
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_event_image.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // recyclerView binding data to each viewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind image resource to ImageView
//        holder.imageView.setImageResource(images.get(position));

        String url = imageUrls.get(position);
        Glide.with(holder.imageView.getContext())
                .load(url)
                .placeholder(R.drawable.shrug)
                .error(R.drawable.shrug )
                .into(holder.imageView);

        // set an onClicklistener callback:
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Clicked " + position, Toast.LENGTH_SHORT).show();

            // navigate using an action
            Navigation.findNavController(v).navigate(R.id.action_mainEntrantView___to___EventView);

            // call back to the fragment, with the position data passed:
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < eventIDs.size()) {
                String eventId = eventIDs.get(pos);

                if (onItemClick != null) {              // 'onItemClick' is the  instance field
                    onItemClick.onClick(eventIDs.get(pos));   // invoke the actual callback
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

}
