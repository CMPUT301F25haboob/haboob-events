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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.Event;
import com.example.haboob.EventTagList;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;
import com.google.android.material.appbar.MaterialToolbar;

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

    public EventViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event_view, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);

        // handle navigation back to mainEntrantView on back button click
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this).navigateUp();
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
