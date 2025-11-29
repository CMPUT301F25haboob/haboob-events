package com.example.haboob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

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