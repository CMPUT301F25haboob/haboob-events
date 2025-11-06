package com.example.haboob.ui.home;

import android.os.Bundle;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    //    private String deviceId = "9662d2bd2595742d";
    private String deviceId;
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

        Bundle args = getArguments();
        if (args == null) {
            // choose one: show empty state, toast, or go back
           Log.d("TAG", "The bundle passed to EventViewerFragment from EntrantMainFragment was NULL");
//            return new View(requireContext()); // or inflate a placeholder
        }
        // unpack the bundle:
        String eventId = requireArguments().getString(ARG_EVENT_ID);
        deviceId = requireArguments().getString("device_id");
        Log.d("TAG", "deviceId from bundle: " + deviceId);

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

        // TODO: set registration start + end date, actual event date, and lottery draw date:

        // set date:
        dateView = view.findViewById(R.id.valueDateTime);
//        String dateString = even


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

        MaterialButton acceptInvitationButton = view.findViewById(R.id.btnAccept);
        MaterialButton leaveWaitlistButton = view.findViewById(R.id.btnLeaveWaitlist);

        assert eventId != null;
        boolean fromMy = getArguments().getBoolean("from_my_events", false);
        acceptInvitationButton.setVisibility(fromMy ? View.GONE : View.VISIBLE);
        leaveWaitlistButton.setVisibility(fromMy ? View.VISIBLE : View.GONE);

        // set an onClicklistener for accepting joining the waitlist
        acceptInvitationButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Joined waitlist! ", Toast.LENGTH_SHORT).show();

            acceptInvitationButton.setText("Joined!");
            acceptInvitationButton.setBackgroundColor(getResources().getColor(R.color.accept_green));

            assert eventId != null;
            DocumentReference ref = FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(eventId);

            // add the device ID to the  Event entrant_ids_for_lottery list in the database:
            ref.update("entrant_ids_for_lottery", FieldValue.arrayUnion(deviceId))
                    .addOnSuccessListener(unused -> {
                        // NOTE: It's okay if it the deviceID is already in the list, firebase won't add another, but the toast still runs
                        Toast.makeText(v.getContext(), "Joined lottery!", Toast.LENGTH_SHORT).show();
                        acceptInvitationButton.setText("Accepted!");
                        getParentFragmentManager().setFragmentResult("USER_JOINED_WAITLIST", new Bundle());
                    })
                    .addOnFailureListener(e -> {
                        acceptInvitationButton.setEnabled(true);
                        Toast.makeText(v.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // set an onClicklistener for leaving the waitlist
        leaveWaitlistButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Left waitlist! ", Toast.LENGTH_SHORT).show();

            assert eventId != null;
            DocumentReference ref = FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(eventId);

            // add the device ID to the  Event entrant_ids_for_lottery list in the database:
            ref.update("entrant_ids_for_lottery", FieldValue.arrayRemove(deviceId))
                    .addOnSuccessListener(unused -> {
                        // NOTE: It's okay if it the deviceID is not in the list, firebase wont error, but the toast still runs
                        Toast.makeText(v.getContext(), "Left waitlist!", Toast.LENGTH_SHORT).show();
                        acceptInvitationButton.setText("Join Waitlist");
                        acceptInvitationButton.setBackgroundColor(
                                ContextCompat.getColor(v.getContext(), R.color.accept_green)
                        );

                        leaveWaitlistButton.setText("Left waitlist!");
                        leaveWaitlistButton.setBackgroundColor(getResources().getColor(R.color.black));

                        // notify EntrantMainFragment to update carousels, as the user left the list
                        getParentFragmentManager().setFragmentResult("USER_LEFT_WAITLIST", new Bundle());
                    })
                    .addOnFailureListener(e -> {
                        acceptInvitationButton.setEnabled(true);
                        Toast.makeText(v.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });



        return view;
    }


}
