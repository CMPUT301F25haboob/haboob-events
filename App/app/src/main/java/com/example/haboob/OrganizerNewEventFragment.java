package com.example.haboob;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class OrganizerNewEventFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_new_event_fragment, container, false);

        // Get which user (Organizer) is creating the event
        OrganizerMainActivity parent = (OrganizerMainActivity) getActivity();
        Organizer currentOrganizer = parent.getCurrentOrganizer();

        // Set up checks for date selected
        final boolean[] startDateSelected = {false};
        final boolean[] endDateSelected = {false};
        final Date[] startDate = {null};
        final Date[] endDate = {null};

        // Get all input fields
        EditText etTitle = view.findViewById(R.id.event_title);
        EditText etDescription = view.findViewById(R.id.event_details);
        EditText etCapacity = view.findViewById(R.id.num_selected);
        EditText etSignupLimit = view.findViewById(R.id.num_allowed_signup);
        CalendarView signupStartView = view.findViewById(R.id.start_date);
        CalendarView signupEndView = view.findViewById(R.id.end_date);
        Switch geoSwitch = view.findViewById(R.id.geo_data_required);

        // Set up listeners
        signupStartView.setOnDateChangeListener((startCalendar, year, month, dayOfMonth) -> {
            startDateSelected[0] = true;
            Calendar start = Calendar.getInstance();
            start.set(year, month, dayOfMonth, 0, 0, 0);
            startDate[0] = start.getTime();
        });

        signupEndView.setOnDateChangeListener((endCalendar, year, month, dayOfMonth) -> {
            endDateSelected[0] = true;
            Calendar end = Calendar.getInstance();
            end.set(year, month, dayOfMonth, 0, 0, 0);
            endDate[0] = end.getTime();
        });

        // TODO: Still need to implement poster, QRcode, tags, and geo data

        Button confirmEventbutton = view.findViewById(R.id.confirm_event);
        confirmEventbutton.setOnClickListener(v1 -> {

            // Get user-inputted data
            String eventTitle = etTitle.getText().toString();
            String eventDetails = etDescription.getText().toString();
            String eventCapacity = etCapacity.getText().toString();
            int capacity = 0;
            String signupLimit = etSignupLimit.getText().toString();
            int limit = -1;
            Date signupStart = startDate[0];
            Date signupEnd = endDate[0];
            boolean geoData = geoSwitch.isChecked();

            // Check that fields are filled
            if (eventTitle.isEmpty() || eventDetails.isEmpty() || eventCapacity.isEmpty() || signupStart == null || signupEnd == null) {
                Toast.makeText(requireContext(), "Please fill in all required fields and select valid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Then try to parse integer
            try {
                capacity = Integer.parseInt(eventCapacity);
                limit = Integer.parseInt(signupLimit);
                if (limit < capacity || capacity < 0) {
                    Toast.makeText(requireContext(), "Limit must be no less than capacity and capacity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter in an integer number for capacity and/or limit", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that dates are selected
            if (!startDateSelected[0] || !endDateSelected[0]) {
                Toast.makeText(requireContext(), "Please select valid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that dates are valid
            if (signupEnd.before(signupStart)) {
                Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set the current date in here since the user could press the button before/after midnight
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Ensure can't select a period before today
            if (startDate[0].before(today.getTime()) || endDate[0].before(today.getTime())) {
                Toast.makeText(requireContext(), "Dates cannot be before today", Toast.LENGTH_SHORT).show();
                return;
            }


            // Create new Event object (pass in dummy data for now)
            QRCode qrCode = new QRCode("test");
            Poster poster = new Poster("test");
            ArrayList<String> tags = new ArrayList<>();
            tags.add("test");
            Event newEvent = new Event(currentOrganizer.getOrganizerID(), signupStart, signupEnd, eventTitle, eventDetails, geoData, capacity, limit, qrCode, poster, tags);

            // Add Event to organizer's eventList
            String eventID = currentOrganizer.getEventList().addEvent(newEvent);
            newEvent.setEventID(eventID);


            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
