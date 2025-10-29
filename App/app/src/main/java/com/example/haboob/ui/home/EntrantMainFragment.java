package com.example.haboob.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.haboob.Event;
import com.example.haboob.EventTagList;
import com.example.haboob.EventsList;
import com.example.haboob.GeoLocationMap;
import com.example.haboob.MainActivity;
import com.example.haboob.Poster;
import com.example.haboob.QRCode;
import com.example.haboob.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


// Author: David T, created on Sunday, oct 26 2025
// this fragment represents the main fragment that the entrant will see when entering the app

public class EntrantMainFragment extends Fragment {
    public EntrantMainFragment() {
        // Required empty public constructor
    }

    // “When this Fragment becomes visible, create its UI from entrant_main.xml and attach it to the container.”
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // access mainActivity to access the EventsList
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        EventsList eventsList = mainActivity.getEventsList();

        Log.d("TAG", "ThE FRAGMENT LAUNCHED");

        // *********** create a new dummy event: ********************************************************************

        // Build registration dates
        Date regStart = new GregorianCalendar(2025, Calendar.NOVEMBER, 1).getTime();
        Date regEnd   = new GregorianCalendar(2025, Calendar.NOVEMBER, 15).getTime();

        // Create supporting objects
        QRCode qrCode = new QRCode("https://haboob.app/events/event001");
        Poster poster = new Poster("pumpkin_festival_poster.webp");

        // Create a list of tags for this event
        List<String> tagStrings = new ArrayList<>();
        tagStrings.add("festival");
        tagStrings.add("outdoor");
        tagStrings.add("family");
        EventTagList tags = new EventTagList(tagStrings);

        // Finally, create your dummy Event using your constructor
        Event dummyEvent = new Event(
                "org12345",                                  // organizer
                regStart,                                    // registrationStartDate
                regEnd,                                      // registrationEndDate
                "Pumpkin Festival 2025",                     // eventTitle
                "A fun outdoor festival with local food, music, and pumpkin carving contests.", // eventDescription
                true,                                        // geoLocationRequired
                100,                                         // lotterySampleSize
                qrCode,                                      // QRCode object
                poster,                                      // Poster object
                tags                                         // EventTagList
        );

        if (eventsList != null){
            eventsList.addEvent(dummyEvent);
            Log.d("TAG", "eventsList is not null");
        }
        else{
            Log.d("TAG", "eventsList is null");
        }

                // Turns the XML file entrant_main.xml into actual View objects in memory.
        View view = inflater.inflate(R.layout.entrant_main, container, false);

        // ***** First carousel - My upcoming events *****
        // Find RecyclerView by ID
        RecyclerView recyclerView = view.findViewById(R.id.entrant_rv_upcoming);
        // Prepare a list of example images from drawable
        List<Integer> images = Arrays.asList(R.drawable.hockey_ex, R.drawable.bob_ross, R.drawable.clash_royale, R.drawable.swimming_lessons);
        // Set up LayoutManager for horizontal scrolling, tells the RecyclerView how to position items, essential for actual rendering
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // Attach the adapter, its the bridge between the data of the images to the actual UI
        recyclerView.setAdapter(new EventImageAdapter(images));

        // ***** Second carousel - My Open Waitlists *****
        RecyclerView rvWaitlists = view.findViewById(R.id.entrant_rv_waitlists);
        rvWaitlists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWaitlists.setNestedScrollingEnabled(false);
        List<Integer> waitlistImages = Arrays.asList(
                R.drawable.clash_royale, R.drawable.clash_royale, R.drawable.clash_royale
        );
        rvWaitlists.setAdapter(new EventImageAdapter(waitlistImages));

        // Return the inflated view
        return view;
    }

}