package com.example.haboob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OrganizerOptionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_screen, container, false);

        Button createEventButton = view.findViewById(R.id.create_event);
        createEventButton.setOnClickListener(v -> {
            // Replace the current fragment with the "New Event" fragment
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerNewEventFragment())
                    .addToBackStack(null) // allows back navigation
                    .commit();
        });

        return view;
    }
}
