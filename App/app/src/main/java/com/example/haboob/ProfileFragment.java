package com.example.haboob;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ProfileFragment allows users to view and update their account information
 * Author: Dan
 *
 * Features:
 * - Load current user data from Firebase
 * - Edit profile information (first name, last name, email, phone)
 * - Save changes to Firebase
 * - Input validation
 * - Delete profile (optional)
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextView accountTypeTextView;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;
    private MaterialToolbar toolbar;

    // Firebase
    private FirebaseFirestore db;
    private String deviceId;
    private String accountType;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Initialize views
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        accountTypeTextView = view.findViewById(R.id.accountTypeTextView);
        saveButton = view.findViewById(R.id.btnSaveProfile);
        deleteButton = view.findViewById(R.id.btnDeleteProfile);
        toolbar = view.findViewById(R.id.topAppBar);

        // Load user data
        loadUserData();

        // Set up button listeners
        saveButton.setOnClickListener(v -> saveProfileChanges());
        deleteButton.setOnClickListener(v -> confirmDeleteProfile());

        // Set up admin textview button listener
        accountTypeTextView.setOnClickListener(v -> goToAdmin());

        // Reused code from EventViewerFragment for navigating back to home
        // when the back button is pressed
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_home);
                return true;
            }

            return false;
        });

        return view;
    }

    /**
     * Loads the current user's data from Firebase
     */
    private void loadUserData() {
        Log.d(TAG, "Loading user data for device: " + deviceId);

        db.collection("users").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate fields with existing data
                        firstNameEditText.setText(documentSnapshot.getString("first_name"));
                        lastNameEditText.setText(documentSnapshot.getString("last_name"));
                        emailEditText.setText(documentSnapshot.getString("email"));

                        String phone = documentSnapshot.getString("phone");
                        phoneEditText.setText(phone != null ? phone : "");

                        accountType = documentSnapshot.getString("account_type");
                        accountTypeTextView.setText(accountType != null ? accountType : "Unknown");

                        Log.d(TAG, "User data loaded successfully");
                    } else {
                        Log.e(TAG, "User document does not exist");
                        Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Validates and saves the profile changes to Firebase
     */
    private void saveProfileChanges() {
        // Get values from input fields
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(firstName, lastName, email)) {
            return;
        }

        // Disable button while saving
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("email", email);
        updates.put("phone", phone);

        // Update in users collection
        db.collection("users").whereEqualTo("device_id", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String docId = doc.getId(); // Firestore-generated document ID

                        db.collection("users").document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (accountType != null) {
                                        updateAccountTypeCollection(updates, accountType);
                                    } else {
                                        onSaveSuccess();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating profile", e);
                                    Toast.makeText(getContext(), "Failed to save changes: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    saveButton.setEnabled(true);
                                    saveButton.setText("Save Changes");
                                });
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding user", e);
                    Toast.makeText(getContext(), "Failed to find user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the account-specific collection (entrant or organizer)
     */
    private void updateAccountTypeCollection(Map<String, Object> updates, String accountType) {
        String collection = accountType.toLowerCase();

        db.collection(collection).document(deviceId)
                .update(updates)
                .addOnSuccessListener(aVoid -> onSaveSuccess())
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Could not update " + collection + " collection", e);
                    // Still consider it a success if main users collection was updated
                    onSaveSuccess();
                });
    }

    /**
     * Called when save operation succeeds
     */
    private void onSaveSuccess() {
        Log.d(TAG, "Profile updated successfully");
        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(true);
        saveButton.setText("Save Changes");
    }

    /**
     * Validates user inputs
     */
    private boolean validateInputs(String firstName, String lastName, String email) {
        // Validate first name
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }

        // Phone is optional, no validation needed
        return true;
    }

    /**
     * Shows confirmation dialog before deleting profile
     */
    private void confirmDeleteProfile() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the user's profile from Firebase
     * TODO: We need this to delete the users from all events, waitlists, etc.
     */
    private void deleteProfile() {
        deleteButton.setEnabled(false);
        deleteButton.setText("Deleting...");

        // Delete from users collection
        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Also delete from account-specific collection
                    if (accountType != null) {
                        String collection = accountType.toLowerCase();
                        db.collection(collection).document(deviceId)
                                .delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(getContext(), "Profile deleted successfully",
                                            Toast.LENGTH_SHORT).show();
                                    // You might want to navigate to login/register screen here
                                })
                                .addOnFailureListener(e -> Log.w(TAG, "Could not delete from " +
                                        collection, e));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting profile", e);
                    Toast.makeText(getContext(), "Failed to delete profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    deleteButton.setEnabled(true);
                    deleteButton.setText("Delete Profile");
                });
    }


    private void goToAdmin(){
        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_admin);
    }




}
