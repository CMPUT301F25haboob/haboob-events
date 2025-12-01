package com.example.haboob;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Holds a global list of preset event tags that organizers can choose from when
 * creating or filtering events.
 */
public class PresetTags {

    /**
     * List of predefined tags available for use across the app.
     */
    public static final ArrayList<String> PRESET_TAGS =
            new ArrayList<>(Arrays.asList(
                    "sports",
                    "fitness",
                    "kids",
                    "adults",
                    "gaming",
                    "family",
                    "seniors",
                    "art",
                    "music",
                    "indoor",
                    "outdoor",
                    "free"
            ));
}
