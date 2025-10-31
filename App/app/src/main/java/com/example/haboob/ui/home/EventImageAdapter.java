package com.example.haboob.ui.home;

import android.content.Intent;
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

//    private final List<Integer> images;
//    private List<String> imageUrls;
    private final List<String> imageUrls = new ArrayList<>(); // always mutable

    public EventImageAdapter() { }

    // copy the input when constructed:
    public EventImageAdapter(List<String> initial) {
        if (initial != null) imageUrls.addAll(initial);     // copy into mutable list
    }
    public void replaceItems(Collection<String> newItems) {
        imageUrls.clear(); // we can only use clear() on a MUTABLE list, so imageURLs has to be muable
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

    public void replaceItems(List<String> newItems) {
        imageUrls.clear();
        imageUrls.addAll(newItems);
        notifyDataSetChanged();
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

        // set an onClicklistener toast for the item at the position
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Clicked " + position, Toast.LENGTH_SHORT).show();

            // navigate using an action
            Navigation.findNavController(v).navigate(R.id.action_mainEntrantView___to___EventView);
        });

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

}
