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
    private Boolean admin;

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

        db.collection("users")
                .whereEqualTo("device_id", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // User already registered — retrieve their account type
                        String accountType = querySnapshot.getDocuments()
                                .get(0)
                                .getString("account_type");

                        Intent intent;
                        if ("Entrant".equals(accountType)) {
                            intent = new Intent(RegisterActivity.this, MainActivity.class);
                        } else {
                            intent = new Intent(RegisterActivity.this, OrganizerMainActivity.class);
                        }

                        // Pass the data if needed
                        intent.putExtra("device_id", deviceId);
                        startActivity(intent);
                        finish(); // Don’t show Register screen again
                    } else {
                        // No user found — show the registration UI
                        showRegistrationUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking registration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showRegistrationUI(); // Still show the registration screen if check fails
                });

    }

    private void showRegistrationUI()
    {
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

            // Validate email format (must contain @ and .com)
            if (!email.contains("@") || !email.contains(".com")) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Entrant or Organizer object based on which account type was selected
            // TODO: Where are we storing these objects? Do we even need them if we are storing user info in the db?

            // Update the db by adding to the "users" collection a new user with all the info
            Map<String, Object> user = new HashMap<>(); // Using a hashmap for easy inserting into the db

            // Add all the user info to the hash map. The hashmap will just be treated as a json document for db purposes
            // NOTE: Can we instead just do
            user.put("device_id", deviceId);
            user.put("first_name", firstName);
            user.put("last_name", lastName);
            user.put("email", email);
            user.put("phone", phoneNumber);
            user.put("account_type", userAccountType);


            // Add the user to 'users' collection first
            db.collection("users").document(deviceId).set(user)
                    .addOnSuccessListener(userDocRef -> {
                        // After successfully adding to 'users', add to the specific collection
                        db.collection(userAccountType.toLowerCase()).add(user)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                    // After the user is added to the db, we want to navigate to the activity based on the users account type
                                    // Navigate to MainActivity if the user is an Entrant
                                    if (userAccountType.equals("Entrant")) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.putExtra("device_id", deviceId);
                                        intent.putExtra("first_name", firstName);
                                        intent.putExtra("last_name", lastName);
                                        intent.putExtra("email", email);
                                        intent.putExtra("phone", phoneNumber);
                                        intent.putExtra("account_type", userAccountType);
                                        startActivity(intent);
                                        finish();
                                    }
                                    if (userAccountType.equals("Organizer")) {
                                        Intent intent = new Intent(RegisterActivity.this, OrganizerMainActivity.class);
                                        intent.putExtra("device_id", deviceId);
                                        intent.putExtra("first_name", firstName);
                                        intent.putExtra("last_name", lastName);
                                        intent.putExtra("email", email);
                                        intent.putExtra("phone", phoneNumber);
                                        intent.putExtra("account_type", userAccountType);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
