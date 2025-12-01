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


/**
 * Adapter responsible for displaying Event objects inside a ListView
 * in the Waitlists screen. It renders the event title, poster image,
 * and associated tags using a Flexbox layout. The adapter also supports
 * dynamic text filtering on both event titles and tags.
 *
 * <p>This adapter keeps two lists:</p>
 * <ul>
 *     <li><b>original</b> – the complete, unfiltered list</li>
 *     <li><b>filtered</b> – the list currently displayed to the user</li>
 * </ul>
 *
 * <p>Filtering never mutates the original list. A search query generates
 * a subset placed in {@code filtered}, and the ListView is refreshed.</p>
 */
public class WaitlistAdapter extends ArrayAdapter<Event> implements Filterable {
    private final List<Event> original;   // full list
    private List<Event> filtered;         // shown list

    /**
     * Creates a new WaitlistAdapter for displaying Event items.
     *
     * @param ctx  the Context used to inflate views and load resources.
     * @param data the initial list of Event objects to display.
     */
    public WaitlistAdapter(@NonNull Context ctx, @NonNull List<Event> data) {
        super(ctx, 0, data);
        original = new ArrayList<>(data);
        filtered = data;
    }

    /**
     * Provides the view for one row in the ListView. This method reuses
     * recycled views when possible for performance reasons.
     *
     * <p>Each row displays:</p>
     * <ul>
     *     <li>Event title</li>
     *     <li>Event poster image (via Glide)</li>
     *     <li>A list of tags inside a Flexbox layout</li>
     * </ul>
     *
     * @param position     the index of the item in the current filtered list.
     * @param convertView  an existing view to reuse, if available.
     * @param parent       the parent ViewGroup.
     * @return the fully populated row view for the Event.
     */

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

    /**
     * @return the number of items currently visible (after filtering).
     */
    @Override public int getCount() { return filtered.size(); }

    /**
     * Returns the Event at a given position from the filtered list.
     *
     * @param position index in the filtered list.
     * @return the Event at that position.
     */
    @Override public Event getItem(int position) { return filtered.get(position); }

    /**
     * Provides a filtering mechanism for matching user search queries
     * against event titles and tags. Filtering:
     *
     * <ul>
     *     <li>Converts query to lowercase</li>
     *     <li>Matches if title contains query</li>
     *     <li>If title doesn't match, tag list is checked</li>
     *     <li>Returns a list of all matching events</li>
     * </ul>
     *
     * <p>Filtering does not affect the original list.</p>
     *
     * @return a new Filter object used by Android's ListView filtering system.
     */

    @Override public Filter getFilter() {
        return new Filter() {

            /**
             * Performs the filtering on a background thread.
             *
             * @param constraint the user-typed search text.
             * @return FilterResults containing the filtered event list.
             */
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

            /**
             * Publishes filtered results to the UI thread and refreshes the adapter.
             *
             * @param cs      the original query text.
             * @param results the filtered results from performFiltering().
             */
            @Override protected void publishResults(CharSequence cs, FilterResults results) {
                //noinspection unchecked
                filtered = (List<Event>) results.values;
                clear();
                addAll(filtered);
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Replaces the entire dataset with a fresh list, typically after a
     * Firestore reload. Both the original and filtered lists are replaced,
     * and the ListView is refreshed.
     *
     * @param fresh the new full list of Event objects.
     */
    public void replaceAll(List<Event> fresh) {
        original.clear(); original.addAll(fresh);
        filtered = new ArrayList<>(fresh);
        clear(); addAll(filtered);
        notifyDataSetChanged();
    }
}