package com.example.haboob;

/**
 * Simple model class representing an event poster, storing a URL or image data string.
 */
public class Poster {
    private String data;

    /**
     * Creates an empty Poster with no data assigned.
     */
    public Poster() {}

    /**
     * Creates a Poster containing the given image data or URL.
     *
     * @param data poster image data or URL
     */
    public Poster(String data) {
        this.data = data;
    }

    /**
     * Returns the poster’s stored data string.
     *
     * @return image data or URL
     */
    public String getData() {
        return data;
    }

    /**
     * Updates the poster’s stored image data or URL.
     *
     * @param data new poster data
     */
    public void setData(String data) {
        this.data = data;
    }
}

