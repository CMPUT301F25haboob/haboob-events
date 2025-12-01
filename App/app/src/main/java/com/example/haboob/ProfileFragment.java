package com.example.haboob;

import android.app.AlertDialog;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows users to view and update their account information.
 *
 * Features:
 * - Load current user data from Firebase
 * - Edit profile information (first name, last name, email, phone)
 * - Save changes to Firebase
 * - Input validation
 * - Delete profile (optional)
 *
 * @author Dan
 * @version 1.0
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
    private MaterialButton historyButton;
    private MaterialToolbar toolbar;
    private TextView accountTextTextView;

    // Firebase
    private FirebaseFirestore db;
    private String deviceId;
    private String accountType;

    /**
     * Required empty public constructor.
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes Firebase, loads user data, and sets up all UI components and listeners.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that this fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment
     * @return The View for the fragment's UI
     */
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
        historyButton = view.findViewById(R.id.btnHistory);
        toolbar = view.findViewById(R.id.topAppBar);
        accountTextTextView = view.findViewById(R.id.SCIbutton);

        // Load user data
        loadUserData();

        // Set up button listeners
        saveButton.setOnClickListener(v -> saveProfileChanges());
        deleteButton.setOnClickListener(v -> confirmDeleteProfile());
        historyButton.setOnClickListener(v -> goToHistory());

        // Set up admin textview button listener
        accountTextTextView.setOnClickListener(v -> {
            int len = accountType.length();

            if(accountType.charAt(len-1) == '+'){
                goToAdmin();
            }
        });

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
     * Loads the current user's data from Firebase.
     * Retrieves user information from the 'users' collection and populates the UI fields.
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
     * Validates and saves the profile changes to Firebase.
     * Updates both the 'users' collection and the account-specific collection (entrant/organizer).
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
        updates.put("first_name", firstName);
        updates.put("last_name", lastName);
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
     * Updates the account-specific collection (entrant or organizer) with profile changes.
     *
     * @param updates Map containing the updated profile fields
     * @param accountType The type of account (Entrant or Organizer)
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
     * Called when save operation succeeds.
     * Displays a success message and re-enables the save button.
     */
    private void onSaveSuccess() {
        Log.d(TAG, "Profile updated successfully");
        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(true);
        saveButton.setText("Save Changes");
    }

    /**
     * Validates user inputs for profile updates.
     * Checks that required fields are filled and meet format requirements.
     *
     * @param firstName The first name to validate
     * @param lastName The last name to validate
     * @param email The email to validate
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs(String firstName, String lastName, String email) {
        // Validate first name
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return false;
        }

        // Validate that first name doesn't contain numbers
        if (firstName.matches(".*\\d.*")) {
            firstNameEditText.setError("First name cannot contain numbers");
            firstNameEditText.requestFocus();
            return false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return false;
        }

        // Validate that last name doesn't contain numbers
        if (lastName.matches(".*\\d.*")) {
            lastNameEditText.setError("Last name cannot contain numbers");
            lastNameEditText.requestFocus();
            return false;
        }

        // Validate email
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        // Validate email format (must contain @ and .com)
        if (!email.contains("@") || !email.contains(".com")) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return false;
        }

        // Phone is optional, no validation needed
        return true;
    }

    /**
     * Shows confirmation dialog before deleting profile.
     * Prompts the user to confirm deletion before proceeding.
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
     * Deletes the user's profile from Firebase.
     * First removes the user from all event lists, then deletes their profile data.
     */
    private void deleteProfile() {
        deleteButton.setEnabled(false);
        deleteButton.setText("Deleting...");

        // Before deleting the user from users, delete them from all events
        final EventsList[] eventsListWrapper = new EventsList[1];
        eventsListWrapper[0] = new EventsList(new EventsList.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded() {
                // Get all events
                ArrayList<Event> allEvents = eventsListWrapper[0].getEventsList();

                // Remove this user from all event lists (waiting, enrolled, invited, cancelled)
                for (Event event : allEvents) {
                    if (event == null) continue;

                    // Check and remove from waiting list
                    if (event.getWaitingEntrants() != null && event.getWaitingEntrants().contains(deviceId)) {
                        event.removeEntrantFromWaitingEntrants(deviceId);
                        Log.d(TAG, "Removed user from waiting list of event: " + event.getEventID());
                    }

                    // Check and remove from enrolled list
                    if (event.getEnrolledEntrants() != null && event.getEnrolledEntrants().contains(deviceId)) {
                        event.removeEntrantFromEnrolledEntrants(deviceId);
                        Log.d(TAG, "Removed user from enrolled list of event: " + event.getEventID());
                    }

                    // Check and remove from invited list
                    if (event.getInvitedEntrants() != null && event.getInvitedEntrants().contains(deviceId)) {
                        event.removeEntrantFromInvitedEntrants(deviceId);
                        Log.d(TAG, "Removed user from invited list of event: " + event.getEventID());
                    }

                    // Check and remove from cancelled list
                    if (event.getCancelledEntrants() != null && event.getCancelledEntrants().contains(deviceId)) {
                        event.removeEntrantFromCancelledEntrants(deviceId);
                        Log.d(TAG, "Removed user from cancelled list of event: " + event.getEventID());
                    }
                }

                // After removing from all events, proceed with deleting the user
                deleteUserFromFirestore();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading events for deletion", e);
                Toast.makeText(getContext(), "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                deleteButton.setEnabled(true);
                deleteButton.setText("Delete Profile");
            }
        });
    }

    /**
     * Deletes the user from Firestore collections.
     * Removes the user from both the 'users' collection and their account-specific collection.
     */
    private void deleteUserFromFirestore() {
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
                                    navigateToRegisterActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Could not delete from " + collection, e);
                                    // Still navigate even if account-specific deletion fails
                                    navigateToRegisterActivity();
                                });
                    } else {
                        // No account type, just navigate after users deletion
                        navigateToRegisterActivity();
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

    /**
     * Navigates to RegisterActivity and finishes the current activity.
     * Used after successful profile deletion to redirect to registration.
     */
    private void navigateToRegisterActivity() {
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    /**
     * Navigates to the admin screen.
     * Only accessible if the account type ends with '+'.
     */
    private void goToAdmin(){
        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_admin);
    }

    /**
     * Navigates to the history screen to view past events.
     */
    private void goToHistory(){
        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_history);
    }

}