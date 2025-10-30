package com.example.haboob.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haboob.Event;
import com.example.haboob.EventTagList;
import com.example.haboob.EventsList;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

// Author: David Tyrrell on Oct 28, 2025
// this fragment hold the functionality for viewing the events
public class EventViewerFragment extends Fragment {

    public EventViewerFragment() {
        // Required empty public constructor
    }

    // When this Fragment becomes visible, create its UI from entrant_main.xml and attach it to the container.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Turns the XML file entrant_event_view.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_event_view, container, false);
        
//        view.findViewById(R.id.appbar).setOnClickListener();

        // Return the inflated view
        return view;
    }




}
