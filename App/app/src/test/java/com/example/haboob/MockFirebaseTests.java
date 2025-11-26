package com.example.haboob;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests using Mockito to verify Firestore behavior WITHOUT
 * touching the real network or the Firebase emulator.
 *
 * These tests run on the JVM (src/test/java).
 */
@RunWith(MockitoJUnitRunner.class)
public class MockFirebaseTests {

    @Mock
    FirebaseFirestore mockDb;

    @Mock
    CollectionReference mockCollection;

    @Mock
    DocumentReference mockDoc;

    @Mock
    Task<Void> mockWriteTask;

    private MockEventsRepo repository;

    @Before
    public void setup() {
        // Configure how Firestore mocks behave
        Mockito.when(mockDb.collection("events")).thenReturn(mockCollection);
        Mockito.when(mockCollection.document(Mockito.anyString())).thenReturn(mockDoc);
        Mockito.when(mockDoc.set(Mockito.any())).thenReturn(mockWriteTask);

        repository = new MockEventsRepo(mockDb);
    }

    // --------------------------------------------------------------------
    // TEST 1 — Verify repository.addFakeEvent() writes to correct document
    // --------------------------------------------------------------------

    /**
     * Ensures that when addFakeEvent() is called,
     * - the repo calls events/<eventId>
     * - and writes the Event object to Firestore.
     */
    @Test
    public void addEvent_callsSetOnCorrectDocument() {
        Event fakeEvent = new Event(true);
        fakeEvent.setEventTitle("The fakest event");
        String eventId = fakeEvent.getEventID();

        repository.addfakeEvent(fakeEvent, task -> {});

        Mockito.verify(mockCollection).document(eventId);
        Mockito.verify(mockDoc).set(fakeEvent);
    }

    // --------------------------------------------------------------------
    // TEST 2 — Verify repository adds "enrolledEntrants" field correctly
    // --------------------------------------------------------------------

    /**
     * Ensures that enrolledEntrants list is saved correctly
     * when creating a new fake event.
     */
//    @Test
//    public void addEvent_savesEnrolledEntrantsList() {
//        Event fakeEvent = new Event(true);
//        fakeEvent.setEventTitle("Enrollment Test");
//        fakeEvent.setEnrolledEntrantsList(Arrays.asList("device123", "deviceABC"));
//
//        repository.addfakeEvent(fakeEvent, task -> {});
//
//        Mockito.verify(mockDoc).set(Mockito.argThat(event ->
//                event instanceof Event &&
//                        ((Event) event).getEnrolledEntrants().size() == 2
//        ));
//    }

    // --------------------------------------------------------------------
    // TEST 3 — Verify that writing an empty event does not crash
    // --------------------------------------------------------------------

    /**
     * Ensures that the repository does not crash when writing
     * an empty Event object.
     */
    @Test
    public void addEvent_handlesEmptyEvent() {
        Event emptyEvent = new Event(true);

        repository.addfakeEvent(emptyEvent, task -> {});

        Mockito.verify(mockDoc).set(emptyEvent);
    }

    // --------------------------------------------------------------------
    // TEST 4 — Verify correct Firestore path: events/<id>
    // --------------------------------------------------------------------

    /**
     * Makes sure the repo writes to "events" collection,
     * never any other path.
     */
    @Test
    public void addEvent_targetsCorrectCollection() {
        Event fakeEvent = new Event(true);

        repository.addfakeEvent(fakeEvent, task -> {});

        Mockito.verify(mockDb, Mockito.times(1)).collection("events");
    }

    // --------------------------------------------------------------------
    // TEST 5 — Verify callback is invoked after write
    // --------------------------------------------------------------------

    /**
     * Ensures that the completion callback is triggered
     * once set() is called.
     */
    @Test
    public void addEvent_invokesCompletionCallback() {
        Event fakeEvent = new Event(true);

        final boolean[] callbackInvoked = { false };

        repository.addfakeEvent(fakeEvent, task -> {
            callbackInvoked[0] = true;
        });

        // Simulate Firestore completing successfully.
        // (We don't need to modify the Task mock.)
        Mockito.verify(mockDoc).set(fakeEvent);

        assert callbackInvoked[0];
    }
}
