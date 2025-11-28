package com.example.haboob;

import com.google.android.gms.tasks.OnCompleteListener;
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

import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        // in @Before setup, after you stub mockDoc.set(...)
        Mockito.when(mockDoc.set(Mockito.any(Event.class))).thenReturn(mockWriteTask);

        // When addOnCompleteListener is called, immediately invoke the listener
        Mockito.doAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(mockWriteTask);    // this will flip callbackInvoked[0] to true
            return mockWriteTask;                  // addOnCompleteListener usually returns the Task
        }).when(mockWriteTask).addOnCompleteListener(Mockito.any());


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
    @Test
    public void addEvent_savesEnrolledEntrantsList() {
        Event fakeEvent = new Event(true);
        fakeEvent.setEventTitle("Enrollment Test");
        ArrayList<String> deviceIds = new ArrayList<>(Arrays.asList("device123", "deviceABC"));
        fakeEvent.setEnrolledEntrantsList(deviceIds);

        repository.addfakeEvent(fakeEvent, task -> {});

        Mockito.verify(mockDoc).set(Mockito.argThat(event ->
                event instanceof Event &&
                        ((Event) event).getEnrolledEntrants().size() == 2
        ));
    }

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
        Event fakeEvent = new Event(true); // however your actor looks

        final boolean[] callbackInvoked = { false };

        repository.addfakeEvent(fakeEvent, task -> {
            callbackInvoked[0] = true;
        });

        Mockito.verify(mockDoc).set(fakeEvent);
        assert callbackInvoked[0];  // or assertTrue(callbackInvoked[0]);
    }

    /**
     * Ensures that addfakeEvent() registers an OnCompleteListener
     * on the Task returned from mockDoc.set().
     */
    @Test
    public void addEvent_registersOnCompleteListenerOnTask() {
        Event fakeEvent = new Event(true);

        repository.addfakeEvent(fakeEvent, task -> {});

        // Verify that the repository added some OnCompleteListener to the write Task
        Mockito.verify(mockWriteTask).addOnCompleteListener(Mockito.any());
    }

    /**
     * Confirms that the repository:
     *  1) calls doc.set(event)
     *  2) then registers the completion listener on the returned Task.
     *
     * This guards against subtle bugs where a listener might be attached
     * to the wrong Task instance or in the wrong order.
     */
    @Test
    public void addEvent_callsSetThenRegistersListener_inOrder() {
        Event fakeEvent = new Event(true);

        repository.addfakeEvent(fakeEvent, task -> {});

        // Verify the order: set(...) must happen before addOnCompleteListener(...)
        InOrder inOrder = Mockito.inOrder(mockDoc, mockWriteTask);
        inOrder.verify(mockDoc).set(fakeEvent);
        inOrder.verify(mockWriteTask).addOnCompleteListener(Mockito.any());
    }

    /**
     * Ensures that the Task passed into the completion callback
     * is exactly the same Task instance returned by mockDoc.set().
     */
    @Test
    public void addEvent_passesSameTaskInstanceToCallback() {
        Event fakeEvent = new Event(true);

        // Holder so we can inspect the Task inside the lambda
        final Task<?>[] receivedTask = { null };

        repository.addfakeEvent(fakeEvent, task -> {
            receivedTask[0] = task;
        });

        // Our setup() doAnswer() calls listener.onComplete(mockWriteTask),
        // so the callback should see that same Task instance.
        assert receivedTask[0] == mockWriteTask;
    }



    /**
     * Ensures that multiple calls to addfakeEvent() write each event
     * to its own Firestore document using the correct IDs.
     */
    @Test
    public void addEvent_multipleEventsWriteToSeparateDocuments() {
        Event firstEvent = new Event(true);
        firstEvent.setEventTitle("First fake event");
        String firstId = firstEvent.getEventID();

        Event secondEvent = new Event(true);
        secondEvent.setEventTitle("Second fake event");
        String secondId = secondEvent.getEventID();

        repository.addfakeEvent(firstEvent, task -> {});
        repository.addfakeEvent(secondEvent, task -> {});

        // Each event should be written to events/<that event's ID>
        Mockito.verify(mockCollection).document(firstId);
        Mockito.verify(mockCollection).document(secondId);

        // And each document should receive the correct Event object
        Mockito.verify(mockDoc, Mockito.times(1)).set(firstEvent);
        Mockito.verify(mockDoc, Mockito.times(1)).set(secondEvent);
    }


    /**
     * Ensures that even when the Event has various lists populated
     * (e.g., enrolled entrants), the completion callback is still invoked.
     */
    @Test
    public void addEvent_withPopulatedLists_stillInvokesCallback() {
        Event fakeEvent = new Event(true);
        fakeEvent.setEventTitle("Populated event");

        ArrayList<String> enrolled = new ArrayList<>(Arrays.asList("dev1", "dev2", "dev3"));
        fakeEvent.setEnrolledEntrantsList(enrolled);

        final boolean[] callbackInvoked = { false };

        repository.addfakeEvent(fakeEvent, task -> {
            callbackInvoked[0] = true;
        });

        Mockito.verify(mockDoc).set(fakeEvent);
        assert callbackInvoked[0];
    }
}
