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

import java.util.List;

import com.example.haboob.R;

// Author: David T
// This code is an ImageAdapter for the RecyclerViews in entrant_main.xml

public class EventImageAdapter extends RecyclerView.Adapter<EventImageAdapter.ViewHolder> {

    private final List<Integer> images;

    // Constructor: pass a list of drawable resource IDs, and then stores those ids in a list
    public EventImageAdapter(List<Integer> images) {
        this.images = images;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_event_image.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind image resource to ImageView
        holder.imageView.setImageResource(images.get(position));

        // set an onClicklistener toast for the item at the position
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Clicked " + position, Toast.LENGTH_SHORT).show();

            // navigate using an action
            Navigation.findNavController(v).navigate(R.id.action_mainEntrantView___to___EventView);
        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}
