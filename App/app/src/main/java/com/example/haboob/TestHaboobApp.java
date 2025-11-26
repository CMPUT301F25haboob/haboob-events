package com.example.haboob;

import android.app.Application;
import android.util.Log;

import com.google.firebase.BuildConfig;
import com.google.firebase.firestore.FirebaseFirestore;


// Author: David T
// This class is for initializing Firebase in debug mode, for creating events that don't go to the real firestore, they go to a mock, fake one.

public class TestHaboobApp extends Application {

    boolean test_mode = false;

    // this code wont run if you dont "firebase init emulators" from terminal - use homebrew to install firebase CLI if not working
    // every time you test, you need to run in terminal: "firebase emulators:start --only firestore"
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("TestHaboobApp", "App started, test_mode = " + test_mode);

        if (test_mode) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.useEmulator("10.0.2.2", 8080);
            Log.d("TestHaboobApp", "Using FIRESTORE EMULATOR");
        } else {
            Log.d("TestHaboobApp", "Using REAL Firestore");
        }

        // to clear the emulator db: ctrl + c, and restart: firebase emulators:start --only firestore

        // if test_mode == true, then the emulator will connect to the fake firestore database emulator, not the real one, and no changes will be made to the fs database
        if (test_mode) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 10.0.2.2 = your computer's localhost, *from inside the Android emulator*
            db.useEmulator("10.0.2.2", 8080);
            // “Instead of using the real Firestore servers, connect to my laptop’s Firestore emulator running on port 8080.”
        }
    }
}

