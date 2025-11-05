package com.example.haboob.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.haboob.Event;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

// Author: David Tyrrell on Oct 28, 2025
// this fragment hold the functionality for viewing the events
public class EventViewerFragment extends Fragment {

    private String eventID;
    public static final String ARG_EVENT_ID = "arg_event_id";
    private TextView dateView, locView; // declare the view buttons we'll need to update
    private ImageView event_image;
    private MaterialToolbar toolbar;

    public EventViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event_view, container, false);

        // unpack the bundle:
        String eventId = requireArguments().getString(ARG_EVENT_ID);

        // grab the details of event:
        EventsList eventsList = ((MainActivity) getActivity()).getEventsList();
        Event eventToDisplay = eventsList.getEventByID(eventId);

        // set title:
        toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setTitle(eventToDisplay.getEventTitle());

        // set image using the event URL:
        event_image = view.findViewById(R.id.heroImage);
        String event_url = eventToDisplay.getPoster().getData();
        Glide.with(event_image.getContext())
                .load(event_url)
                .placeholder(R.drawable.shrug)
                .error(R.drawable.shrug )
                .into(event_image);

        // handle navigation back to mainEntrantView on back button click
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_home);
                return true;
            }

            return false;
        });

        Button acceptInvitationButton = view.findViewById(R.id.btnAccept);


        // set an onClicklistener toast for accepting invitation
        acceptInvitationButton.setOnClickListener(v -> {
        Toast.makeText(v.getContext(), "Accepted invitation! ", Toast.LENGTH_SHORT).show();

        acceptInvitationButton.setText("Accepted!");
        acceptInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));

    });

        return view;
    }


}
