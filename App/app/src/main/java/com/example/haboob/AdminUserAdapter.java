package com.example.haboob;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Random;

/**
 * Adapter for displaying User objects in a RecyclerView within the Admin view.
 * Binds user data to card views and handles click events for user actions.
 *
 * @author Haboob Team
 * @version 1.0
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener listener;

    /**
     * Interface for handling user action events.
     */
    public interface OnUserActionListener {
        /**
         * Called when the delete button is clicked for a user.
         *
         * @param user The user to delete
         * @param position The position in the list
         */
        void onDeleteClick(User user, int position);

        /**
         * Called when a user card is clicked.
         *
         * @param user The user that was clicked
         */
        void onUserClick(User user);
    }

    /**
     * Constructs an AdminUserAdapter with the specified user list and listener.
     *
     * @param userList The list of User objects to display
     * @param listener The listener for handling user actions
     */
    public AdminUserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder when RecyclerView needs a new item view.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_users_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds user data to the specified ViewHolder.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user name
        String fullName = user.getFirstName() + " " + user.getLastName();
        holder.userName.setText(fullName);

        // Set user email
        holder.userEmail.setText(user.getEmail());

        // user Id
        holder.userId.setText(user.getDeviceId());

        // Set user phone (hide if empty)
        String phone = user.getPhone();
        if (phone != null && !phone.isEmpty()) {
            holder.userPhone.setText(phone);
            holder.userPhone.setVisibility(View.VISIBLE);
        } else {
            holder.userPhone.setVisibility(View.GONE);
        }

        // Set account type chip
        String accountType = user.getAccountType();
        holder.userAccountType.setText(accountType != null ? accountType : "Unknown");

        // Set avatar with user initials
        String initials = getInitials(user.getFirstName(), user.getLastName());
        holder.userAvatar.setText(initials);

        // Set a color for the avatar based on the user's name (consistent color per user)
        int color = generateColorFromString(fullName);
        holder.userAvatar.setBackgroundColor(color);

        // Set click listener for the card
        holder.userCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(user, holder.getBindingAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The total number of User items
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Gets the initials from first and last name.
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Two-character initials
     */
    private String getInitials(String firstName, String lastName) {
        String first = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
        String last = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    /**
     * Generates a consistent color from a string (for avatar backgrounds).
     *
     * @param text The text to generate color from
     * @return A color integer
     */
    private int generateColorFromString(String text) {
        // Generate a hash from the string
        int hash = text.hashCode();

        // Use the hash to create a consistent but varied color
        Random random = new Random(hash);

        // Generate colors with good saturation and brightness for readability
        float hue = random.nextFloat() * 360;
        float saturation = 0.6f + random.nextFloat() * 0.2f; // 0.6 to 0.8
        float brightness = 0.7f + random.nextFloat() * 0.2f; // 0.7 to 0.9

        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }

    /**
     * ViewHolder class for caching view references within each user card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView userCard;
        public final TextView userAvatar;
        public final TextView userName;
        public final TextView userEmail;
        public final TextView userPhone;
        public final Chip userAccountType;
        public final MaterialButton deleteButton;

        public final TextView userId;
        /**
         * Constructs a ViewHolder and caches all child view references.
         *
         * @param view The root view of the item layout
         */
        public ViewHolder(View view) {
            super(view);
            userCard = view.findViewById(R.id.card_admin_user);
            userAvatar = view.findViewById(R.id.user_avatar);
            userName = view.findViewById(R.id.user_name);
            userEmail = view.findViewById(R.id.user_email);
            userPhone = view.findViewById(R.id.user_phone);
            userAccountType = view.findViewById(R.id.user_account_type);
            deleteButton = view.findViewById(R.id.btn_user_delete);
            userId = view.findViewById(R.id.user_id);
        }
    }
}