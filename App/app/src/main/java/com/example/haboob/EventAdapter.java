package com.example.haboob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * ArrayAdapter for displaying Event objects in a simple list view.
 * Shows each event's title and marks events requiring a lottery with a red indicator.
 */
public class EventAdapter extends ArrayAdapter<Event> {
    /**
     * Creates an EventAdapter for displaying the given list of events.
     *
     * @param context the hosting context
     * @param events  list of events to display
     */
    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    /**
     * Returns the list item view for an Event, showing its title and applying
     * a red indicator icon when a lottery can be drawn for that event.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        // Display the event name
        textView.setText(event.getEventTitle());

        // Check if we can draw a lottery -> (end date passed + total capacity not met + people still in waitlist)
        Date today = new Date();
        if (event.getRegistrationEndDate().before(today) && (event.getEnrolledEntrants().size() + event.getInvitedEntrants().size() < event.getLotterySampleSize()) && !event.getWaitingEntrants().isEmpty()) {
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_dot, 0, 0, 0);
            textView.setCompoundDrawablePadding(12);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        return convertView;
    }
}