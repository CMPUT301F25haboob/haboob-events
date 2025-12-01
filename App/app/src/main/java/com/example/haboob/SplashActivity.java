package com.example.haboob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;

/**
 * Splash screen activity that serves as the entry point of the application.
 * Checks if the user is already registered and routes them to the appropriate activity
 * based on their account type (Entrant or Organizer).
 *
 * Features:
 * - Retrieves unique device ID
 * - Queries Firebase to check user registration status
 * - Routes to MainActivity (Entrants) or OrganizerMainActivity (Organizers)
 * - Routes to RegisterActivity for new users
 *
 * @author Dan
 * @version 1.0
 */
public class SplashActivity extends AppCompatActivity {
    /**
     * The unique device ID for this user.
     */
    private String deviceId;

    /**
     * Firebase Firestore instance for database operations.
     */
    private FirebaseFirestore db;

    /**
     * Reference to the 'users' collection in Firestore.
     */
    private CollectionReference usersRef;


    /**
     * Called when the activity is first created.
     * Initializes Firebase, retrieves device ID, and checks user registration status.
     * Routes the user to the appropriate activity based on whether they're registered
     * and their account type.
     *
     * @param savedInstanceState Previously saved state of the activity
     */
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Displays the corresponding xml file

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Get a reference to the users collection so we can query it
        // TODO: We need the users collections back so that we can query the account type instead
        //       getting both collections and checking for the user
        usersRef = db.collection("users");


        // Query the db to check if a user with the given deviceId is already registered or not
        usersRef.whereEqualTo("device_id", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User is already registered, check their account_type
                        String accountType = queryDocumentSnapshots.getDocuments().get(0).getString("account_type");

                        Intent intent;
                        if ("Organizer".equals(accountType)) {
                            // Route organizers to OrganizerMainActivity -> pass in the reference to current organizer
                            intent = new Intent(SplashActivity.this, OrganizerMainActivity.class);
                        } else {
                            // Route entrants (and others) to MainActivity
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                        intent.putExtra("device_id", deviceId);
                        startActivity(intent);
                        finish();
                    } else {
                        // User doesn't exist - go to RegisterActivity
                        Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(SplashActivity.this,
                            "Error checking user: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: Retry? Go to registration here???
                });

    }
}