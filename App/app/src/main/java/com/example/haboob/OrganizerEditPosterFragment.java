package com.example.haboob;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

/**
 * Fragment that lets an organizer preview and update the poster image for a given {@link Event}.
 * <p>
 * This screen:
 * </p>
 * <ul>
 *     <li>Retrieves the {@link Event} from the fragment arguments.</li>
 *     <li>Displays the current poster (if any) in an {@link ImageView}.</li>
 *     <li>Allows the organizer to pick a new image from local storage.</li>
 *     <li>Uploads the selected image to Cloudinary and updates the event's {@link Poster}.</li>
 * </ul>
 * <p>
 * Image loading is handled via Glide, while image upload is delegated to Cloudinary's
 * {@link MediaManager}. Persisting the updated poster to Firestore is expected to be handled
 * elsewhere (e.g., through {@code EventsList} logic).
 * </p>
 */
public class OrganizerEditPosterFragment extends Fragment {

    private Event event;                    // Event being edited
    private ImageView posterImageView;      // Shows current poster
    private Button changePosterButton;      // Opens picker

    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri = null;

    /**
     * Simple callback interface used to report the secure URL of an image
     * after a successful Cloudinary upload.
     */
    private interface OnImageUploadedListener {
        void onUploaded(String url);
    }

    /**
     * Uploads the given local image {@link Uri} to Cloudinary using an unsigned preset and
     * reports the resulting secure URL through the provided callback.
     * <p>
     * This method shows basic user feedback via {@link Toast} messages and logs the
     * upload lifecycle events (start, success, error, reschedule). On success, the
     * {@code secure_url} returned by Cloudinary is passed to the supplied
     * {@link OnImageUploadedListener}.
     * </p>
     *
     * @param uri      the local {@link Uri} of the image to upload
     * @param listener callback invoked with the secure Cloudinary URL once the upload succeeds
     */
    private void uploadImageToCloudinary(Uri uri, OnImageUploadedListener listener) {
        MediaManager.get().upload(uri)
                .unsigned("haboob_unsigned") // your unsigned preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started");
                        Toast.makeText(requireContext(), "Uploading poster...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // optional
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
     * Initializes the fragment by retrieving the {@link Event} from the arguments
     * and registering an {@link ActivityResultLauncher} for image selection.
     * <p>
     * When the user picks an image, this method:
     * </p>
     * <ul>
     *     <li>Stores the selected {@link Uri}.</li>
     *     <li>Triggers a Cloudinary upload via {@link #uploadImageToCloudinary(Uri, OnImageUploadedListener)}.</li>
     *     <li>Updates the event's {@link Poster} with the returned URL.</li>
     *     <li>Refreshes the poster preview using Glide, if the {@link ImageView} is available.</li>
     * </ul>
     *
     * @param savedInstanceState saved state bundle, or {@code null} for a fresh creation
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Event from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        // Image picker callback
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Toast.makeText(requireContext(), "Poster selected", Toast.LENGTH_SHORT).show();

                        uploadImageToCloudinary(selectedImageUri, url -> {
                            // Update event poster object
                            Poster p = new Poster(url);
                            event.setPoster(p);      // you’ll handle Firestore via EventsList logic

                            // Update UI
                            if (posterImageView != null) {
                                Glide.with(requireContext())
                                        .load(url)
                                        .into(posterImageView);
                            }

                            Toast.makeText(requireContext(), "Poster updated", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    /**
     * Inflates the organizer poster-editing layout, binds UI elements, and wires up button
     * behavior for navigating back and changing the poster.
     * <p>
     * If the current {@link Event} already has a {@link Poster} with a non-empty URL, the
     * image is loaded into the poster preview using Glide. The "Change Poster" button opens
     * the image picker (configured in {@link #onCreate(Bundle)}), while the back button
     * simply pops the fragment back stack.
     * </p>
     *
     * @param inflater  the {@link LayoutInflater} used to inflate the fragment layout
     * @param container the parent {@link ViewGroup} for the fragment's UI, or {@code null}
     * @param savedInstanceState saved state bundle, or {@code null} if none
     * @return the inflated root {@link View} for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_edit_fragment, container, false);

        posterImageView = view.findViewById(R.id.poster_image_view);
        changePosterButton = view.findViewById(R.id.change_poster_button);
        Button backButton = view.findViewById(R.id.back_button_edit_poster);

        // Show current poster if it exists
        if (event != null && event.getPoster() != null) {
            // adjust this line to your Poster API
            String url = event.getPoster().getData(); // or getUrl()
            if (url != null && !url.isEmpty()) {
                Glide.with(this)
                        .load(url)
                        .into(posterImageView);
            }
        }

        backButton.setOnClickListener(v ->  {

            // Just return to previous fragment
            getParentFragmentManager().popBackStack();

        });

        changePosterButton.setOnClickListener(v -> {
            if (event == null) {
                Toast.makeText(requireContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedImageUri = null;
            imagePickerLauncher.launch("image/*");
        });

        return view;
    }
}
