package com.example.haboob;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.example.haboob.ui.home.EntrantMainFragment;
import com.example.haboob.ui.home.EventViewerFragment;
//import com.example.haboob.ui.home.myCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.haboob.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Main activity for the Haboob application.
 * This activity serves as the container for navigation between different fragments
 * and handles deep link navigation from QR code scans.
 *
 * @author Haboob Team
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * View binding object that provides access to views in activity_main.xml.
     */
    private ActivityMainBinding binding;

    /**
     * EventsList instance that can be shared across all fragments.
     */
    private EventsList eventsList;

    /**
     * The event ID extracted from deep links.
     */
    String eventID;

    /**
     * Returns the EventsList instance for use by fragments.
     *
     * @return The EventsList object containing all events
     */
    public EventsList getEventsList() {
        return eventsList;
    }

    /**
     * Refreshes the EventsList by reloading data from Firebase.
     */
    public void refreshEventsList() {
        if (eventsList != null) {
            eventsList.loadEventsList();
        }
    }

    /**
     * Called when the activity is first created.
     * Initializes the EventsList, sets up view binding, configures navigation,
     * and handles any deep links from the intent.
     *
     * @param savedInstanceState Previously saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Make the eventsList ready BEFORE inflating layout (which creates the fragment)
        eventsList = new EventsList(/* maybe getApplicationContext() if needed */);

        // Creates the binding object by inflating activity_main.xml
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Sets the activity's content to the root view of that inflated layout. From this point on, the activity is showing activity_main.xml.
        setContentView(binding.getRoot());

        // Gets a reference to your bottom nav bar defined in activity_main.xml
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // Put here the fragment IDs that map to bottom-nav tabs.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // If we scanned a qrcode, we need to handle that by naviagting to the correct
        // event fragment
        handleDeepLink(getIntent());
    }

    /**
     * Called when a new intent is delivered to the activity.
     * Handles deep links when the activity is already running.
     *
     * @param intent The new intent that was started for the activity
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    /**
     * Handles deep link navigation from QR code scans.
     * Extracts the event ID from the intent and navigates to the EventViewerFragment.
     * Expected format: haboob://event?id={eventID}
     *
     * @param intent The intent containing the deep link data
     * @author Dan
     */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "haboob".equals(data.getScheme()) && "event".equals(data.getHost())) {
            String eventId = data.getQueryParameter("id");

            if (eventId != null && !eventId.isEmpty()) {
                Log.d("MainActivity", "Deep link detected for event: " + eventId);

                // Navigate to EventViewerFragment with the event ID
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

                // Create a bundle with the event ID
                Bundle args = new Bundle();
                args.putString(EventViewerFragment.ARG_EVENT_ID, eventId);

                // Navigate to the event viewer fragment
                navController.navigate(R.id.entrant_event_view, args);
            } else {
                Log.e("MainActivity", "Deep link missing event ID parameter");
            }
        }
    }

}