package com.example.haboob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class OrganizerEditPosterFragment extends Fragment {

    // TODO: figure out picture uploads
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
