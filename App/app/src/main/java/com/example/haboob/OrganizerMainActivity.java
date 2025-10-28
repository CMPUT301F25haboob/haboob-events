package com.example.haboob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ContentResolver;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrganizerMainActivity extends AppCompatActivity {
    /* This class will be in charge of all of the goings-on for Organizers including:
     * -> Creating events
     * -> Viewing all events they created
     * -> View all different lists (EntrantsList, WaitingList, InviteList, CancelledList, EnrolledList)
     * -> Send notifications to different lists
     * -> See where people register from
     * -> Update poster image of events
     */
    // Get ID from database
    // Set to current user
    // Continue
    private FirebaseFirestore db;
    private Organizer currentOrganizer;
    private Button createEventButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_organizer_activity);

        // Set up database and organizer using app
        db = FirebaseFirestore.getInstance();
        currentOrganizer = new Organizer("CURRENT_ORGANIZER", "Organizer", "test@gmail.com", "Organizer", "780-000-0000");

        // Create event button
        createEventButton = findViewById(R.id.create_event);

        // Create onclick method
        createEventButton.setOnClickListener(v -> {
            // Open up new fragment for creating event
            setContentView(R.layout.organizer_new_event_fragment);

            // Set onclick listener for create event button, check all fields are filled
            Button confirmEventbutton = findViewById(R.id.confirm_event);
            confirmEventbutton.setOnClickListener(v1 -> {

                // Get all input fields
                EditText etTitle = findViewById(R.id.event_title);
                EditText etDescription = findViewById(R.id.event_details);
                EditText etCapacity = findViewById(R.id.num_selected);
                EditText etSignupLimit = findViewById(R.id.num_allowed_signup);
                CalendarView signupStartView = findViewById(R.id.start_date);
                CalendarView signupEndView = findViewById(R.id.end_date);
                Switch geoSwitch = findViewById(R.id.geo_data_required);

                // Get user-inputted data
                String eventTitle = etTitle.getText().toString();
                String eventDetails = etDescription.getText().toString();
                String eventCapacity = etCapacity.getText().toString();
                int capacity = 0;
                String signupLimit = etSignupLimit.getText().toString();
                int limit = -1;
                Date signupStart = new Date(signupStartView.getDate());
                Date signupEnd = new Date(signupEndView.getDate());
                boolean geoData = geoSwitch.isChecked();

                // Check that fields are filled
                if (eventTitle.isEmpty() || eventDetails.isEmpty() || eventCapacity.isEmpty() || signupStart == null || signupEnd == null) {
                    Toast.makeText(this, "Please fill in all required fields and select account type", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Then try to parse integer
                try {
                    capacity = Integer.parseInt(eventCapacity);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter in an integer number for capacity and/or limit", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If they input any limit then try to parse the int
                if (!signupLimit.isEmpty()) {
                    try {
                        limit = Integer.parseInt(signupLimit);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter in an integer number for capacity and/or limit", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                // Create new Event object
                QRCode qrCode = new QRCode("test");
                Poster poster = new Poster("test");
                List<String> tags = new ArrayList<>();
                tags.add("test");
                EventTagList eventTags = new EventTagList(tags);
                Event newEvent = new Event(currentOrganizer.getOrganizerID(), signupStart, signupEnd, eventTitle, eventDetails, geoData, capacity, limit, qrCode, poster, eventTags);

                // Add Event to organizer's eventList
                currentOrganizer.getEventList().addEvent(newEvent);
            });
        });

        // Return to the main screen
        setContentView(R.layout.main_organizer_activity);


        // TODO: EDIT THIS CODE WHEN WE HAVE THE DB WORKING WITH deviceID AND OBJECT!
//        Intent intent = getIntent();
//        if (intent != null) {
//            // Get the organizerID from intent
//            String organizerID = intent.getStringExtra("organizerID");
//
//            // Create a new Organizer object with the organizerID
//            currentOrganizer = new Organizer();
//        }
    }

}
