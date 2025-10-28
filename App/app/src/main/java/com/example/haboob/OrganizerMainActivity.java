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

        // New fragment to show the buttons for organizer
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerOptionsFragment()) // e.g. your main screen
                    .commit();
        }
    }

    // Getter function so that later fragments can still access the app through logged-in user
    public Organizer getCurrentOrganizer() {
        return currentOrganizer;
    }

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


