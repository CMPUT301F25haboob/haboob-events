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

/**
 * RecyclerView adapter that renders a horizontal carousel of event posters by URL.
 *
 * <p>Usage:</p>
 * <ol>
 *   <li>Create an instance, optionally passing a click callback via
 *   {@link EventImageAdapter#EventImageAdapter(OnItemClick)} or
 *   {@link #setOnItemClick(OnItemClick)}.</li>
 *   <li>Populate items using {@link #replaceItems(Collection)} or {@link #replaceItems(List)} and
 *   pass a parallel list of event IDs with {@link #inputIDs(List)}.</li>
 *   <li>Handle item taps in the hosting Fragment/Activity by implementing {@link OnItemClick}.</li>
 * </ol>
 *
 * <p><b>Data contracts:</b> The position of each image URL is assumed to align with the same index
 * in {@code eventIDs}. Clicks use {@link RecyclerView.ViewHolder#getBindingAdapterPosition()} to
 * resolve the event ID safely at bind-time.</p>
 * Author: David T, Oct 25, 2025
 */
public class EventImageAdapter extends RecyclerView.Adapter<EventImageAdapter.ViewHolder> {


    public interface OnItemClick { void onClick(String eventId); }     // setting up the callback
    private final List<String> imageUrls = new ArrayList<>(); // always mutable
    private List<String> eventIDs = new ArrayList<>();
    private List<String> invitedEventIDs = new ArrayList<>(); // IDs of events that should show red dot
    private OnItemClick onItemClick;
    public EventImageAdapter(OnItemClick onItemClick) {     // Primary actor: caller supplies the click callback
        this.onItemClick = onItemClick;
    }
    public EventImageAdapter() { }// empty constructor}

    /**
     * Sets or replaces the click callback handler.
     *
     * @param onItemClick callback to invoke on item tap; may be {@code null} to disable
     */
    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    /**
     * Constructs an adapter, copying an initial collection of image URLs.
     *
     * @param initial initial URLs; if {@code null}, the adapter starts empty
     */
    public EventImageAdapter(List<String> initial) {
        if (initial != null) imageUrls.addAll(initial);     // copy into mutable list
    }
    /**
     * Replaces all items with {@code newItems} and refreshes the list.
     *
     * <p>Note: Also see the {@link #replaceItems(List)} overload. Consider keeping only one
     * overload to avoid ambiguity.</p>
     *
     * @param newItems new set of image URLs; {@code null} clears the list
     */
    public void replaceItems(Collection<String> newItems) {
        imageUrls.clear(); // we can only use clear() on a MUTABLE list, so imageURLs has to be mutable
        if (newItems != null) imageUrls.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder holding the poster {@link ImageView} and red dot indicator.
     *
     * <p>Acts as a lightweight cache of child views to support smooth scrolling.</p>
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View redDotIndicator;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_event);
            redDotIndicator = itemView.findViewById(R.id.red_dot_indicator);
        }

    }

    // when querying the data is done, replace the items in the recyclerView with the new items from the database
    public void replaceItems(List<String> newItems) {
        imageUrls.clear();
        imageUrls.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Supplies the parallel list of event IDs corresponding to the current {@link #imageUrls}.
     * The order must match the image list for click handling to resolve the correct ID.
     *
     * @param eventIDs event identifiers aligned by index with {@link #imageUrls}
     */
    public void inputIDs(List<String> eventIDs) {
        this.eventIDs.clear();
        this.eventIDs.addAll(eventIDs);
        notifyDataSetChanged();
        Log.d("TAG", "InputIDs ran, size: " + eventIDs.size() + " EventIDs: " + eventIDs.toString());
    }

    /**
     * Sets which event IDs should display a red dot indicator (user has been invited).
     *
     * @param invitedEventIDs list of event IDs that should show the red dot
     */
    public void setInvitedEventIDs(List<String> invitedEventIDs) {
        this.invitedEventIDs.clear();
        if (invitedEventIDs != null) {
            this.invitedEventIDs.addAll(invitedEventIDs);
        }
        notifyDataSetChanged();
    }

    /**
     * Inflates {@code item_event_image.xml} and creates a new {@link ViewHolder}.
     *
     * @param parent   RecyclerView parent
     * @param viewType view type (unused; single view type)
     * @return a new {@link ViewHolder}
     */
    @NonNull
    @Override
    // Create a new ViewHolder for each item in the RecyclerView
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_event_image.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_image, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the URL at {@code position} into the given holder using Glide and attaches a click
     * listener that resolves the matching event ID (if present) and invokes {@link OnItemClick}.
     *
     * <p><b>Image loading:</b> Glide handles memory/disk caching. A placeholder and error
     * drawable are provided for better UX.</p>
     *
     * @param holder   the view holder to bind into
     * @param position adapter position
     */
    @Override
    // recyclerView binding data to each viewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // TODO: OPTIONAL: the fact that it has to load the images each time is a bit inefficient, maybe its a good idea
        // TODO: to somehow save the photos on first load and then use those?

        // Bind image resource to ImageView
//        holder.imageView.setImageResource(images.get(position));

        String url = imageUrls.get(position);
        Glide.with(holder.imageView.getContext())
                .load(url)
                .placeholder(R.drawable.shrug)
                .error(R.drawable.shrug )
                .into(holder.imageView);

        // Show/hide red dot indicator based on whether user is invited to this event
        if (position < eventIDs.size()) {
            String eventId = eventIDs.get(position);
            boolean isInvited = invitedEventIDs.contains(eventId);
            holder.redDotIndicator.setVisibility(isInvited ? View.VISIBLE : View.GONE);
        } else {
            holder.redDotIndicator.setVisibility(View.GONE);
        }

        // set an onClicklistener callback:
        holder.itemView.setOnClickListener(v -> {
            Log.d("TAG", "eventIDS size: " + eventIDs.size());

            Toast.makeText(v.getContext(), "Clicked " + position, Toast.LENGTH_SHORT).show();

            // navigate using an action
//            Navigation.findNavController(v).navigate(R.id.action_mainEntrantView___to___EventView);

            // call back to the fragment, with the position data passed:
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < eventIDs.size()) {
                String eventId = eventIDs.get(pos);

                if (onItemClick != null) {              // 'onItemClick' is the  instance field
                    onItemClick.onClick(eventIDs.get(pos));   // invoke the actual callback
                }
                else{
                    Log.d("TAG", "onItemClick was null");
                }
            }
        });

    }

    /**
     * @return the number of items currently bound to the adapter
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

}
