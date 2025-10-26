package com.example.haboob;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Fields
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneNumberInput;
    private MaterialButton entrantButton;
    private MaterialButton organizerButton;
    private MaterialButton registerButton;

    private String userAccountType; // This will either be "Entrant" or "Organizer" based on the user input
    private String deviceId;
    private FirebaseFirestore db;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Display the corresponding UI

        // Initialize Firebase and get device ID
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Find all the components we need
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        phoneNumberInput = findViewById(R.id.phone_input);
        entrantButton = findViewById(R.id.entrant_button);
        organizerButton = findViewById(R.id.organizer_button);
        registerButton = findViewById(R.id.register_button);

        // When the entrant button is clicked, we want to make the button look selected
        entrantButton.setOnClickListener(v -> {
            // Make the button look selected
            entrantButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
            entrantButton.setTextColor(getResources().getColor(android.R.color.white, null));

            // Make the organizer button look unselected
            organizerButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
            organizerButton.setTextColor(getResources().getColor(R.color.purple_500, null));

            // Set the userAccountType to "Entrant"
            userAccountType = "Entrant";
        });

        // When the organizer button is clicked, we want to make the button look selected
        organizerButton.setOnClickListener(v -> {
            // Make the button look selected
            organizerButton.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
            organizerButton.setTextColor(getResources().getColor(android.R.color.white, null));

            // Make the entrant button look unselected
            entrantButton.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
            entrantButton.setTextColor(getResources().getColor(R.color.purple_500, null));

            // Set the userAccountType to "Organizer"
            userAccountType = "Organizer";
        });

        // When the register button is clicked, we want to update our database by adding this user with all the info
        registerButton.setOnClickListener(v -> {
            // Get the input text field values
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phoneNumber = phoneNumberInput.getText().toString().trim();

            // Make sure user has inputted all required fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || userAccountType == null) {
                Toast.makeText(this, "Please fill in all required fields and select account type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Entrant or Organizer object based on which account type was selected
            // TODO: Where are we storing these objects? Do we even need them if we are storing user info in the db?

            // Update the db by adding to the "users" collection a new user with all the info
            Map<String, Object> user = new HashMap<>(); // Using a hashmap for easy inserting into the db

            // Add all the user info to the hash map. The hashmap will just be treated as a json document for db purposes
            user.put("device_id", deviceId);
            user.put("first_name", firstName);
            user.put("last_name", lastName);
            user.put("email", email);
            user.put("phone", phoneNumber);
            user.put("account_type", userAccountType);

            db.collection("users").add(user).addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // After the user is added to the db, we want to navigate to the activity based on the users account type
                        // Navigate to MainActivity if the user is an Entrant
                        if (userAccountType.equals("Entrant")) {
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        if (userAccountType.equals("Organizer")) {
                            // TODO: Navigate to OrganizerMainActivity if the user is an Organizer (not implemented yet)
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
