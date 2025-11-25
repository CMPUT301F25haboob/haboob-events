package com.example.haboob.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.haboob.Event;
import com.example.haboob.Poster;
import com.example.haboob.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

public class WaitlistAdapter extends ArrayAdapter<Event> implements Filterable {
    private final List<Event> original;   // full list
    private List<Event> filtered;         // shown list

    public WaitlistAdapter(@NonNull Context ctx, @NonNull List<Event> data) {
        super(ctx, 0, data);
        original = new ArrayList<>(data);
        filtered = data;
    }

    @NonNull @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // on start, the ListView calls getView repeatedly, once for every visible item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_waitlist, parent, false);
        }

        Event e = getItem(position);


        // =============== Updated by Dan ===============
        // The events shown when searching now look more like the ones in the history view
        // for consistency and visual purposes
        // Set title
        TextView title = convertView.findViewById(R.id.title);
        title.setText(e != null ? e.getEventTitle() : "");

        // Load event image
        ImageView eventImage = convertView.findViewById(R.id.eventImage);
        if (e != null) {
            Poster poster = e.getPoster();
            if (poster != null && poster.getData() != null && !poster.getData().isEmpty()) {
                Glide.with(getContext())
                        .load(poster.getData())
                        .placeholder(R.drawable.ic_search_24)
                        .into(eventImage);
            } else {
                // Load placeholder
                Glide.with(getContext())
                        .load("https://media.tenor.com/hG6eR9HM_fkAAAAM/the-simpsons-homer-simpson.gif")
                        .into(eventImage);
            }
        }
        // ===============================================

        FlexboxLayout tagContainer = convertView.findViewById(R.id.tagContainer);
        tagContainer.removeAllViews(); // clear old tags

        // add set tag
        assert e != null;
        for (String tag : e.getTags()) {
            TextView tagView = new TextView(getContext());
            tagView.setText(tag);
            tagView.setBackgroundResource(R.drawable.tag_background);
            tagView.setTextColor(Color.BLACK);
            tagView.setTextSize(12);
            tagView.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 8, 8);
            tagView.setLayoutParams(lp);

            tagContainer.addView(tagView);
        }


        return convertView;
    }

    @Override public int getCount() { return filtered.size(); }
    @Override public Event getItem(int position) { return filtered.get(position); }

    @Override public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint == null ? "" : constraint.toString().trim().toLowerCase();
                List<Event> out;
                if (q.isEmpty()) {
                    out = new ArrayList<>(original);
                } else {
                    out = new ArrayList<>();

                    for (Event e : original) {
                        boolean matches = false;

                        // 1) Match title
                        String title = e.getEventTitle();
                        if (title != null &&
                                title.toLowerCase().contains(q)) {
                            matches = true;
                        }

                        // 2) Match tags
                        if (!matches) { // only check tags if title didn't already match
                            List<String> tags = e.getTags(); // <-- change to your real accessor
                            if (tags != null) {
                                for (String tag : tags) {
                                    if (tag != null &&
                                            tag.toLowerCase().contains(q)) {
                                        matches = true;
                                        break;          // no need to check more tags
                                    }
                                }
                            }
                        }
                        if (matches) {
                            out.add(e);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = out;
                fr.count = out.size();
                return fr;
            }
            @Override protected void publishResults(CharSequence cs, FilterResults results) {
                //noinspection unchecked
                filtered = (List<Event>) results.values;
                clear();
                addAll(filtered);
                notifyDataSetChanged();
            }
        };
    }

    // Call this when you reload from Firestore:
    public void replaceAll(List<Event> fresh) {
        original.clear(); original.addAll(fresh);
        filtered = new ArrayList<>(fresh);
        clear(); addAll(filtered);
        notifyDataSetChanged();
    }
}