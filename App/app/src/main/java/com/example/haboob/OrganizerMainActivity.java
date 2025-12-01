package com.example.haboob;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ContentResolver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@code OrganizerMainActivity} is the entry point for organizer workflows.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Navigating to organizer options (create/view events, manage lists, edit posters)</li>
 *   <li>Initializing Firestore and the current {@link Organizer}</li>
 *   <li>Hosting organizer-related fragments within {@code R.id.organizer_fragment_container}</li>
 * </ul>
 */
public class OrganizerMainActivity extends AppCompatActivity {
    /** Firestore database handle for organizer data operations. */
    private FirebaseFirestore db;

    /** The currently authenticated/active organizer user. */
    private Organizer currentOrganizer;

    /** Button reference for potential top-level create-event actions (if needed in this activity). */
    private Button createEventButton;

    /**
     * Initializes the activity UI, Firestore, and a placeholder {@link Organizer}.
     * <p>
     * If {@code savedInstanceState} is null, loads {@link OrganizerOptionsFragment} into
     * {@code R.id.organizer_fragment_container}.
     *
     * @param savedInstanceState previously saved state, or {@code null} on first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_organizer_activity);

        initCloudinary();

        // Set up database and organizer using app
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();

        if (intent != null) {
            // Get the organizerID from intent
            String organizerID = intent.getStringExtra("device_id");
            String organizerFirstName = intent.getStringExtra("first_name");
            String organizerLastName =  intent.getStringExtra("last_name");
            String organizerEmail = intent.getStringExtra("email");
            String organizerPhone = intent.getStringExtra("phone");
            String accountType = intent.getStringExtra("account_type");


            // Create a new Organizer object with the organizerID
            currentOrganizer = new Organizer(organizerID, organizerFirstName, organizerLastName, organizerEmail, accountType, organizerPhone);
            goToOrganizerOptions(savedInstanceState);
        }
    }

    /**
     * Initializes Cloudinary for the organizer workflow using a basic config map.
     * Must be called before any Cloudinary uploads occur.
     */
    private void initCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "dxu3r4bi5");
        MediaManager.init(this, config);
    }

    /**
     * Loads the organizer options screen if this is the first creation of the activity.
     *
     * @param savedInstanceState saved state bundle, or null on first load
     */
    public void goToOrganizerOptions(Bundle savedInstanceState) {
        // New fragment to show the buttons for organizer
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.organizer_fragment_container, new OrganizerOptionsFragment()) // e.g. your main screen
                    .commit();
        }
    }

    /**
     * Returns the active {@link Organizer} instance for use by hosted fragments.
     *
     * @return the current organizer
     */
    public Organizer getCurrentOrganizer() {
        return currentOrganizer;
    }
}
