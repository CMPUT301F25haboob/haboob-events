package com.example.haboob;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EntrantEventsUiTest {

    private static final String TEST_EVENT_TITLE = "UI Test Event";

    @Before
    public void setUpFirestoreWithTestEvent() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();

        // Ensure Firebase is initialized
        try {
            FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            FirebaseOptions options = FirebaseOptions.fromResource(ctx);
            FirebaseApp.initializeApp(ctx, options);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // point this test at the emulator:
        db.useEmulator("10.0.2.2", 8080);

        String deviceId = Settings.Secure.getString(
                ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if (deviceId == null) deviceId = "unknown";

        String docId = "ui-test-event-" + deviceId;

        // wipe old test doc if any
        Tasks.await(db.collection("events").document(docId).delete());

        Event event = new Event(true);
        event.setEventTitle(TEST_EVENT_TITLE);
        event.setEventDescription("This is a test event");
        event.addEntrantToEnrolledEntrantsTESTING(deviceId);

        Tasks.await(db.collection("events").document(docId).set(event));
    }

    @Test
    public void eventWithCurrentDeviceId_isShownOnScreen() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            // navigate if needed (e.g. tap "My Events" tab)

            SystemClock.sleep(3000); // simple wait for Firestore listener

            onView(withText(TEST_EVENT_TITLE))
                    .check(matches(isDisplayed()));
        }
    }
}
