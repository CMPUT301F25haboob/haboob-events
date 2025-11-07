package com.example.haboob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * {@code OrganizerEditPosterFragment} provides a screen for organizers to edit or
 * manage their event posters.
 * <p>
 * The fragment retrieves the selected {@link Event} passed through arguments
 * (under the key {@code "event"}) and displays its title for confirmation or editing.
 * <p>
 * Future functionality will include image uploads and poster updates.
 */
public class OrganizerEditPosterFragment extends Fragment {

    // TODO: figure out picture uploads

    /**
     * Inflates the poster editing layout and populates the title with the selected event’s name.
     * <p>
     * The event is obtained from the fragment’s arguments bundle.
     *
     * @param inflater  LayoutInflater to inflate the layout XML
     * @param container parent view that contains this fragment
     * @param savedInstanceState saved instance state, if any
     * @return the inflated view with the event title displayed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_edit_poster, container, false);
        TextView text = view.findViewById(R.id.poster_text);

        if (getArguments() != null) {
            Event selectedEvent = (Event) getArguments().getSerializable("event");
            text.setText(selectedEvent.getEventTitle());
        }

        return view;
    }
}

