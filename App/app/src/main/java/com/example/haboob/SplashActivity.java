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

public class SplashActivity extends AppCompatActivity {
    private String deviceId;
    private FirebaseFirestore db;
    private CollectionReference usersRef;


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
        usersRef = db.collection("users");


        // Query the db to check if a user with the given deviceId is already registered or not
        usersRef.whereEqualTo("device_id", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User is already registered - transition them to MainActivity
                        // TODO: Make sure this properly transitions to main activity
                        Intent intent = new Intent(SplashActivity.this, OrganizerMainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // User doesn't exist - go to RegisterActivity
                        // TODO: Make sure this properly transitions to register activity
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
