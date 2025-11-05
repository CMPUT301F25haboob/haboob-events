package com.example.haboob;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QRCode class for generating and managing QR codes based on event IDs
 * Uses ZXing library to generate QR code bitmaps
 */
public class QRCode {
    private String eventID;
    private transient Bitmap qrBitmap; // transient so it won't be serialized to Firestore

    /**
     * Empty constructor for Firebase Firestore serialization
     */
    public QRCode() {
        // Empty Constructor for Firebase
    }

    /**
     * Constructor that takes event ID and generates QR code
     * @param eventID The unique event ID to encode in the QR code
     */
    public QRCode(String eventID) {
        this.eventID = eventID;
        this.qrBitmap = null; // Will be generated on demand
    }

    /**
     * Generates a deep link URI for the event that can be scanned to open the app
     * Format: haboob://event?id={eventID}
     * @return The deep link URI string
     */
    private String generateDeepLink() {
        return "haboob://event?id=" + eventID;
    }

    /**
     * Generates a QR code bitmap from the event ID as a deep link
     * The QR code encodes a URI that will open the app to this specific event
     * @param size The width and height of the QR code in pixels (recommended: 512 or higher)
     * @return Bitmap of the QR code, or null if generation fails
     */
    public Bitmap generateQRCode(int size) {
        if (eventID == null || eventID.isEmpty()) {
            return null;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            // Encode the deep link instead of just the eventID
            String deepLink = generateDeepLink();
            BitMatrix bitMatrix = writer.encode(deepLink, BarcodeFormat.QR_CODE, size, size);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Convert BitMatrix to Bitmap
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            this.qrBitmap = bitmap;
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the cached QR code bitmap, or generates it if not already created
     * @param size The width and height of the QR code in pixels
     * @return Bitmap of the QR code
     */
    public Bitmap getBitmap(int size) {
        if (qrBitmap == null) {
            return generateQRCode(size);
        }
        return qrBitmap;
    }

    /**
     * Gets the cached QR code bitmap without generating a new one
     * @return Bitmap of the QR code, or null if not yet generated
     */
    public Bitmap getCachedBitmap() {
        return qrBitmap;
    }

    /**
     * Gets the event ID encoded in this QR code
     * @return The event ID string
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Sets the event ID for this QR code
     * Note: This will invalidate any cached QR code bitmap
     * @param eventID The event ID to encode
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
        this.qrBitmap = null; // Invalidate cached bitmap
    }

    /**
     * Checks if the QR code has valid data
     * @return true if event ID is not null and not empty
     */
    public boolean hasValidData() {
        return eventID != null && !eventID.isEmpty();
    }

    /**
     * Regenerates the QR code bitmap with a new size
     * @param size The new width and height in pixels
     * @return The newly generated bitmap
     */
    public Bitmap regenerate(int size) {
        this.qrBitmap = null;
        return generateQRCode(size);
    }
}
