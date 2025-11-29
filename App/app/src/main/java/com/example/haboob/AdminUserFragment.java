package com.example.haboob;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying all user profiles from the database in the admin view.
 * Allows administrators to view all users and delete user accounts.
 *
 * @author Haboob Team
 * @version 1.0
 */
public class AdminUserFragment extends Fragment implements AdminUserAdapter.OnUserActionListener {

    private static final String TAG = "AdminUserFragment";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private MaterialToolbar toolbar;

    private AdminUserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public AdminUserFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment's layout and initializes all UI components.
     *
     * @param inflater The LayoutInflater object to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment, if any
     * @return The root View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_users_fragment, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_admin_users);
        progressBar = view.findViewById(R.id.progress_bar_admin_users);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        toolbar = view.findViewById(R.id.usersTopAppBar);

        // Setup toolbar back navigation
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.navigation_admin);
                return true;
            }
            return false;
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        // Load users from Firestore
        loadUsersFromFirestore();

        return view;
    }

    /**
     * Loads all users from the Firestore "users" collection.
     * Shows progress bar during loading and handles empty state.
     */
    private void loadUsersFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Create a User object from the document
                            String deviceId = document.getString("device_id");
                            String firstName = document.getString("first_name");
                            String lastName = document.getString("last_name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String accountType = document.getString("account_type");

                            // Create appropriate User subclass based on account type
                            User user;
                            if ("Organizer".equals(accountType)) {
                                user = new Organizer(deviceId, firstName, lastName, email, phone, "Organizer");
                            } else {
                                // Default to base User for Entrants and others
                                user = new Entrant(firstName, lastName, email, phone);
                                user.setDeviceId(deviceId);
                            }

                            userList.add(user);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user document: " + document.getId(), e);
                        }
                    }

                    // Update UI based on results
                    progressBar.setVisibility(View.GONE);

                    if (userList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }

                    Log.d(TAG, "Loaded " + userList.size() + " users from Firestore");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading users from Firestore", e);
                    Toast.makeText(getContext(),
                            "Error loading users: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Called when the delete button is clicked for a user.
     * Shows a confirmation dialog before deleting.
     *
     * @param user The user to delete
     * @param position The position of the user in the list
     */
    @Override
    public void onDeleteClick(User user, int position) {
        showDeleteConfirmationDialog(user, position);
    }

    /**
     * Shows an AlertDialog to confirm user deletion.
     *
     * @param user The user to delete
     * @param position The position of the user in the list
     */
    private void showDeleteConfirmationDialog(User user, int position) {
        String userName = user.getFirstName() + " " + user.getLastName();

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + userName + "?\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a user from Firestore and updates the UI.
     *
     * @param user The user to delete
     * @param position The position of the user in the list
     */
    private void deleteUser(User user, int position) {
        String deviceId = user.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete from users collection
        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Also delete from account-specific collection if applicable
                    String accountType = user.getAccountType();
                    if (accountType != null && !accountType.isEmpty()) {
                        String collection = accountType.toLowerCase();

                        // Query and delete from the account-specific collection
                        db.collection(collection)
                                .whereEqualTo("device_id", deviceId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        String docId = querySnapshot.getDocuments().get(0).getId();
                                        db.collection(collection).document(docId).delete();
                                    }
                                });
                    }

                    // Remove from local list and update adapter
                    userList.remove(position);
                    adapter.notifyItemRemoved(position);

                    // Show empty state if no users left
                    if (userList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }

                    Toast.makeText(getContext(),
                            "User " + user.getFirstName() + " " + user.getLastName() + " deleted",
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Successfully deleted user: " + deviceId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user: " + deviceId, e);
                    Toast.makeText(getContext(),
                            "Failed to delete user: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Called when a user card is clicked.
     * Can be used to navigate to user details in the future.
     *
     * @param user The user that was clicked
     */
    @Override
    public void onUserClick(User user) {
        // TODO: Navigate to user detail view if needed
        Toast.makeText(getContext(),
                "Clicked: " + user.getFirstName() + " " + user.getLastName(),
                Toast.LENGTH_SHORT).show();
    }
}