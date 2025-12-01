package com.example.haboob;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Fragment that displays and manages QR codes for events.
 * This fragment generates a QR code based on an event ID and allows users
 * to view and save the QR code image to their device.
 *
 * Features:
 * - Generates QR code from event ID
 * - Displays QR code image
 * - Saves QR code to device storage (Pictures/HaboobQRs)
 * - Handles navigation from both organizer and entrant views
 *
 * @author Dan
 * @version 1.0
 */
public class EventQRCodeFragment extends Fragment {

    /**
     * Argument key for passing the event ID to this fragment.
     */
    public static final String ARG_EVENT_ID = "arg_event_id";

    /**
     * The ID of the event for which to generate a QR code.
     */
    private String eventId;

    /**
     * ImageView for displaying the generated QR code.
     */
    private ImageView qrCodeImageView;

    /**
     * TextView for displaying the event ID text.
     */
    private TextView eventIdValueTextView;

    /**
     * Toolbar for navigation controls.
     */
    private MaterialToolbar toolbar;

    /**
     * Bitmap representation of the generated QR code.
     */
    private Bitmap qrBitmap;

    /**
     * Button for saving the QR code to device storage.
     */
    private Button saveQRButton;


    /**
     * Required empty public constructor.
     */
    public EventQRCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes all UI components, generates the QR code, and sets up event listeners.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that this fragment's UI should be attached to
     * @param savedInstanceState Previously saved state of the fragment
     * @return The View for the fragment's UI, or the view if event ID is missing
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_qr_code, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.topAppBar);
        qrCodeImageView = view.findViewById(R.id.qrCodeImage);
        eventIdValueTextView = view.findViewById(R.id.eventIdValue);
        saveQRButton = view.findViewById(R.id.saveButton);

        // Get event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            eventIdValueTextView.setText(eventId);
        } else {
            Log.e("EventQRCodeFragment", "No event ID provided");
            Toast.makeText(getContext(), "Error: No event ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Generate and display QR code
        generateAndDisplayQRCode(eventId);

        // Handle saving to device
        saveQRButton.setOnClickListener(v -> {
            if (qrBitmap != null) {
                saveImage();
            } else {
                Toast.makeText(getContext(), "QR code not generated", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle back navigation
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_goBack) {

                String source = getArguments().getString("source");
                if (source.equals("organizer")) {
                    getParentFragmentManager().popBackStack();
                } else {
                    NavHostFragment.findNavController(this).navigateUp();
                    return true;
                }
            }
            return false;
        });

        return view;
    }

    /**
     * Generates and displays the QR code for the event.
     * Creates a QRCode object with the event ID, generates a 512x512 pixel bitmap,
     * and displays it in the ImageView. Handles errors gracefully with appropriate
     * user feedback.
     *
     * @param eventId The event ID to encode in the QR code
     */
    private void generateAndDisplayQRCode(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("EventQRCodeFragment", "Cannot generate QR code: eventId is null or empty");
            qrCodeImageView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create QRCode object with the eventID
            QRCode qrCode = new QRCode(eventId);

            // Generate the QR code bitmap (512x512 pixels for good quality)
            qrBitmap = qrCode.generateQRCode(512);

            if (qrBitmap != null) {
                // Display the QR code
                qrCodeImageView.setImageBitmap(qrBitmap);
                qrCodeImageView.setVisibility(View.VISIBLE);
                Log.d("EventQRCodeFragment", "QR code generated successfully for event: " + eventId);
            } else {
                Log.e("EventQRCodeFragment", "Failed to generate QR code bitmap");
                qrCodeImageView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EventQRCodeFragment", "Error generating QR code: " + e.getMessage());
            e.printStackTrace();
            qrCodeImageView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the QR code image to the device's storage for future sharing.
     * The image is saved to the Pictures/HaboobQRs directory with the filename
     * format "qrcode_{eventId}.jpg". Uses MediaStore API for Android 10+ compatibility.
     * Displays a toast message indicating success or failure.
     */
    public void saveImage() {
        if (qrBitmap == null) {
            Toast.makeText(getContext(), "QR bitmap is null", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "qrcode_" + eventId + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HaboobQRs");

        ContentResolver resolver = requireContext().getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream out = resolver.openOutputStream(imageUri);
            qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            Toast.makeText(getContext(), "Saved to Pictures/HaboobQRs", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed saving image", Toast.LENGTH_LONG).show();
        }
    }


}