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

public class OrganizerEditPosterFragment extends Fragment {

    private Event event;                    // Event being edited
    private ImageView posterImageView;      // Shows current poster
    private Button changePosterButton;      // Opens picker

    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri = null;

    // Callback for uploaded URL
    private interface OnImageUploadedListener {
        void onUploaded(String url);
    }

    // Cloudinary upload helper
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
