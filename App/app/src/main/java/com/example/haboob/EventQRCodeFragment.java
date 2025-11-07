package com.example.haboob;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment that displays a QR code for an event
 * Author: Dan
 */
public class EventQRCodeFragment extends Fragment {

    public static final String ARG_EVENT_ID = "arg_event_id";

    private String eventId;
    private ImageView qrCodeImageView;
    private TextView eventIdValueTextView;
    private MaterialToolbar toolbar;
    private Bitmap qrBitmap;

    public EventQRCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_qr_code, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.topAppBar);
        qrCodeImageView = view.findViewById(R.id.qrCodeImage);
        eventIdValueTextView = view.findViewById(R.id.eventIdValue);

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

        // Handle back navigation
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_goBack) {
                NavHostFragment.findNavController(this).navigateUp();
                return true;
            }
            return false;
        });

        return view;
    }

    /**
     * Generates and displays the QR code for the event
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

}
