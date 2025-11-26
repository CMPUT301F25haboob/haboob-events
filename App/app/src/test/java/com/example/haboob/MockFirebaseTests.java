package com.example.haboob;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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

@RunWith(MockitoJUnitRunner.class)
public class MockFirebaseTests {

    @Mock
    FirebaseFirestore mockDb;

    @Mock
    CollectionReference mockCollection;

    @Mock
    DocumentReference mockDoc;

    @Mock
    Task<Void> mockTask;

    private MockEventsRepo repository;

    @Before
    public void setup() {
        // Set up how the mocks should behave
        Mockito.when(mockDb.collection("events")).thenReturn(mockCollection);
        Mockito.when(mockCollection.document(Mockito.anyString())).thenReturn(mockDoc);
        Mockito.when(mockDoc.set(Mockito.any())).thenReturn(mockTask);

        repository = new MockEventsRepo(mockDb);
    }

    @Test
    public void addEvent_callsSetOnCorrectDocument() {
        Event fakeEvent = new Event(true);
        fakeEvent.setEventTitle("The fakest event");
        String eventId = fakeEvent.getEventID();

        repository.addfakeEvent(fakeEvent, task -> { /* no-op */ });

        // Verify the correct calls were made
        Mockito.verify(mockCollection).document(eventId);
        Mockito.verify(mockDoc).set(fakeEvent);
    }
}
