package com.example.haboob;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * {@code OrganizerNewEventFragment} is responsible for creating a new {@link Event}
 * in the organizer’s account. It handles user input for event title, description,
 * capacity, registration limits, tags, and registration dates.
 *
 * <p>The fragment validates all inputs (dates, numeric fields, and required text)
 * before creating an {@link Event} object and adding it to the current organizer’s
 * {@link EventsList}. The user can specify optional geolocation requirements
 * using a {@link Switch} and tag the event for later filtering.</p>
 *
 * <p>Once an event is successfully created, it is added to Firestore via
 * {@link EventsList#addEvent(Event)} and the fragment returns to the previous
 * screen in the navigation stack.</p>
 */
public class OrganizerNewEventFragment extends Fragment {
    // For image gallery picker
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri = null;

    /**
     * Callback used to receive the Cloudinary image URL once an upload completes.
     */
    private interface OnImageUploadedListener {
        void onUploaded(String url);
    }

    /**
     * Uploads the selected image to Cloudinary and returns the uploaded URL
     * through the provided callback listener.
     */
    private void uploadImageToCloudinary(Uri uri, OnImageUploadedListener listener) {
        MediaManager.get().upload(uri)
                .unsigned("haboob_unsigned")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started");
                        Toast.makeText(requireContext(), "Uploading poster...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Optional: you could show progress here
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        Log.d("Cloudinary", "Upload success: " + secureUrl);

                        if (listener != null) {
                            listener.onUploaded(secureUrl);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Upload failed: " + error.getDescription());
                        Toast.makeText(requireContext(), "Poster upload failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("Cloudinary", "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Sets up the image picker launcher used to select a poster image from the device gallery.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Toast.makeText(requireContext(), "Poster selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Inflates the UI for creating a new event and initializes all form elements,
     * including title, description, capacity, signup limits, tags, registration dates,
     * geo-data requirements, and optional poster image selection.
     * <p>
     * Sets up listeners for date pickers, tag selection, image picking, and the
     * confirm button. Performs full validation on all user inputs before constructing
     * a new {@link Event} object, attaching an optional poster image, and saving it
     * to the organizer’s {@link EventsList}. On success, the fragment returns to the
     * previous screen.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_new_event_fragment, container, false);

        // Get which user (Organizer) is creating the event
        OrganizerMainActivity parent = (OrganizerMainActivity) getActivity();
        Organizer currentOrganizer = parent.getCurrentOrganizer();

        // Set up checks for date selected
        final boolean[] startDateSelected = {false};
        final boolean[] endDateSelected = {false};
        final Date[] startDate = {null};
        final Date[] endDate = {null};

        // Get all input fields
        EditText etTitle = view.findViewById(R.id.event_title);
        EditText etDescription = view.findViewById(R.id.event_details);
        EditText etCapacity = view.findViewById(R.id.num_selected);
        EditText etSignupLimit = view.findViewById(R.id.num_allowed_signup);
        Button btnTags = view.findViewById(R.id.tags_button);
        CalendarView signupStartView = view.findViewById(R.id.start_date);
        CalendarView signupEndView = view.findViewById(R.id.end_date);
        Switch geoSwitch = view.findViewById(R.id.geo_data_required);
        Button backButton = view.findViewById(R.id.back_button);
        Button uploadPosterButton = view.findViewById(R.id.upload_picture_button);

        // Set up listeners
        signupStartView.setOnDateChangeListener((startCalendar, year, month, dayOfMonth) -> {
            startDateSelected[0] = true;
            Calendar start = Calendar.getInstance();
            start.set(year, month, dayOfMonth, 0, 0, 0);
            startDate[0] = start.getTime();
        });

        signupEndView.setOnDateChangeListener((endCalendar, year, month, dayOfMonth) -> {
            endDateSelected[0] = true;
            Calendar end = Calendar.getInstance();
            end.set(year, month, dayOfMonth, 0, 0, 0);
            endDate[0] = end.getTime();
        });

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Author: Owen - Open gallery on uploadPosterButton click
        uploadPosterButton.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        // Author: Owen - On click of tags button show dropdown dialog and save selected tags
        ArrayList<String> selectedTags = new ArrayList<>();

        btnTags.setOnClickListener(v -> {
            // Use global preset tags
            ArrayList<String> presetTags = PresetTags.PRESET_TAGS;

            // Convert to CharSequence[] for the dialog
            CharSequence[] tagOptions = presetTags.toArray(new CharSequence[0]);

            // Track which items are checked
            boolean[] checkedItems = new boolean[tagOptions.length];

            // Pre-check previously selected tags
            for (int i = 0; i < presetTags.size(); i++) {
                checkedItems[i] = selectedTags.contains(presetTags.get(i));
            }

            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext());

            builder.setTitle("Select Tags");

            builder.setMultiChoiceItems(tagOptions, checkedItems, (dialog, which, isChecked) -> {
                String tag = presetTags.get(which);  // get tag by index from list

                if (isChecked) {
                    if (!selectedTags.contains(tag)) {
                        selectedTags.add(tag);
                    }
                } else {
                    selectedTags.remove(tag);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                // keep button text constant
                btnTags.setText("Select Tags");
            });

            builder.setNegativeButton("Cancel", null);

            builder.show();
        });

        Button confirmEventbutton = view.findViewById(R.id.confirm_event);
        confirmEventbutton.setOnClickListener(v1 -> {

            // Get user-inputted data
            String eventTitle = etTitle.getText().toString();
            String eventDetails = etDescription.getText().toString();

            String eventCapacity = etCapacity.getText().toString();
            int capacity = 0;
            String signupLimit = etSignupLimit.getText().toString();
            int limit;
            Date signupStart = startDate[0];
            Date signupEnd = endDate[0];
            boolean geoData = geoSwitch.isChecked();

            // Check that fields are filled
            if (eventTitle.isEmpty() || eventDetails.isEmpty() || eventCapacity.isEmpty() || signupStart == null || signupEnd == null) {
                Toast.makeText(requireContext(), "Please fill in all required fields and select valid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Author: Owen - Check that at least one tag is selected
            if (selectedTags.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one tag", Toast.LENGTH_SHORT).show();
                return;
            }

            // Then try to parse integer
            try {
                capacity = Integer.parseInt(eventCapacity);
                if (signupLimit.isEmpty()) {
                    limit = -1;
                } else {

                    // Try to parse limit for number
                    try {
                        limit = Integer.parseInt(signupLimit);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Please enter in an integer number > capacity for limit or leave blank otherwise", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (capacity <= 0 || ((limit != -1) && (limit < capacity))) {
                    Toast.makeText(requireContext(), "Limit must be no less than capacity and capacity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter in an integer number for capacity and/or limit", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that dates are selected
            if (!startDateSelected[0] || !endDateSelected[0]) {
                Toast.makeText(requireContext(), "Please select valid dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that dates are valid
            if (signupEnd.before(signupStart)) {
                Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set the current date in here since the user could press the button before/after midnight
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Ensure can't select a period before today
            if (startDate[0].before(today.getTime()) || endDate[0].before(today.getTime())) {
                Toast.makeText(requireContext(), "Dates cannot be before today", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new Event object (pass in dummy data for now)
            Event newEvent = new Event(currentOrganizer.getOrganizerID(), signupStart, signupEnd, eventTitle, eventDetails, geoData, capacity, limit, selectedTags);

            // If no image selected, just save the event directly with the dummy image url
            if (selectedImageUri == null) {
                // attach the "no image available" URL to the event
                Poster p = new Poster();
                newEvent.setPoster(p);

                // Save the event
                currentOrganizer.getEventList().addEvent(newEvent);
                getParentFragmentManager().popBackStack();
                return;
            }

            // If an image was selected, upload to Cloudinary first
            uploadImageToCloudinary(selectedImageUri, new OnImageUploadedListener() {
                @Override
                public void onUploaded(String url) {
                    // attach the URL to the Event
                    Poster p = new Poster(url);
                    newEvent.setPoster(p);

                    // Save the event
                    currentOrganizer.getEventList().addEvent(newEvent);
                    getParentFragmentManager().popBackStack();
                }
            });
        });

        return view;
    }
}
