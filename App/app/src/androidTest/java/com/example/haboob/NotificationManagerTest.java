package com.example.haboob;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationManager}.
 *
 * <p>This test class validates the notification sending, logging, and retrieval
 * functionality of the NotificationManager using Mockito to mock Firebase Firestore
 * dependencies. These tests ensure that:</p>
 *
 * <ul>
 *   <li>Notifications are correctly sent to individual users</li>
 *   <li>Notifications are properly broadcast to multiple recipients</li>
 *   <li>Organizer notifications are logged for record-keeping</li>
 *   <li>User notifications are retrieved with proper ordering</li>
 *   <li>Edge cases (null values, empty IDs) are handled gracefully</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This test requires {@link NotificationManager} to have
 * a constructor that accepts a {@link FirebaseFirestore} parameter for dependency
 * injection.</p>
 *
 * @author Test Suite
 * @version 1.0
 */
public class NotificationManagerTest {

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private CollectionReference mockUsersCollection;

    @Mock
    private DocumentReference mockUserDocument;

    @Mock
    private CollectionReference mockNotificationsCollection;

    @Mock
    private CollectionReference mockSentNotificationsCollection;

    @Mock
    private DocumentReference mockNotificationDocument;

    @Mock
    private Query mockQuery;

    @Mock
    private Task<Void> mockVoidTask;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    private NotificationManager notificationManager;
    private Notification testNotification;

    /**
     * Sets up the test environment before each test execution.
     * Initializes mocks and creates a test NotificationManager instance
     * with injected mock Firestore dependency.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a test notification
        testNotification = new Notification(
                "event123",
                "organizer456",
                "user789",
                "Test notification message"
        );
        testNotification.setNotificationId("notif001");

        // Set up Firestore mock chain
        when(mockDb.collection("users")).thenReturn(mockUsersCollection);
        when(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument);
        when(mockUserDocument.collection("notifications")).thenReturn(mockNotificationsCollection);
        when(mockUserDocument.collection("sentNotifications")).thenReturn(mockSentNotificationsCollection);
        when(mockNotificationsCollection.document(anyString())).thenReturn(mockNotificationDocument);
        when(mockSentNotificationsCollection.document(anyString())).thenReturn(mockNotificationDocument);
        when(mockNotificationDocument.set(any())).thenReturn(mockVoidTask);

        // Set up task success/failure listeners
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);

        // CRITICAL: Create NotificationManager with mocked Firestore
        notificationManager = new NotificationManager(mockDb);
    }

    // ==================== SEND TO USER TESTS ====================

    /**
     * Verifies that a valid notification is successfully sent to a user.
     *
     * <p>Business Rule: Notifications must be written to the Firestore path
     * users/{recipientId}/notifications/{notificationId}.</p>
     *
     * <p>Expected behavior: The notification is set in Firestore and success
     * listeners are registered.</p>
     */
    @Test
    public void sendToUser_withValidNotification_shouldSendSuccessfully() {
        // Act
        notificationManager.sendToUser(testNotification);

        // Assert - verify Firestore interactions
        verify(mockDb, atLeastOnce()).collection("users");
        verify(mockUsersCollection).document("user789");
        verify(mockUserDocument).collection("notifications");
        verify(mockNotificationsCollection).document("notif001");
        verify(mockNotificationDocument, atLeastOnce()).set(testNotification);
    }

    /**
     * Verifies that sending a null notification is handled gracefully.
     *
     * <p>Business Rule: Null notifications should be rejected with a warning
     * logged, and no Firestore operations should be performed.</p>
     *
     * <p>Expected behavior: No exception is thrown, and no Firestore write occurs.</p>
     */
    @Test
    public void sendToUser_withNullNotification_shouldNotCrash() {
        // Act
        notificationManager.sendToUser(null);

        // Assert - verify no Firestore operations occurred
        verify(mockNotificationDocument, never()).set(any());
    }

    /**
     * Verifies that notifications with null recipient IDs are rejected.
     *
     * <p>Business Rule: A notification must have a valid recipient ID
     * to be sent. Null or empty recipient IDs should be rejected.</p>
     *
     * <p>Expected behavior: The notification is not sent and a warning is logged.</p>
     */
    @Test
    public void sendToUser_withNullRecipientId_shouldNotSend() {
        // Arrange
        Notification invalidNotification = new Notification(
                "event123",
                "organizer456",
                null,
                "Test message"
        );

        // Act
        notificationManager.sendToUser(invalidNotification);

        // Assert
        verify(mockNotificationDocument, never()).set(any());
    }

    /**
     * Verifies that notifications with empty recipient IDs are rejected.
     *
     * <p>Business Rule: Empty or whitespace-only recipient IDs are invalid.</p>
     *
     * <p>Expected behavior: The notification is not sent.</p>
     */
    @Test
    public void sendToUser_withEmptyRecipientId_shouldNotSend() {
        // Arrange
        Notification invalidNotification = new Notification(
                "event123",
                "organizer456",
                "   ",
                "Test message"
        );
        invalidNotification.setNotificationId("test123");

        // Act
        notificationManager.sendToUser(invalidNotification);

        // Assert
        verify(mockNotificationDocument, never()).set(any());
    }

    /**
     * Verifies that notifications with the default recipient ID are rejected.
     *
     * <p>Business Rule: The default recipient ID constant should not be used
     * for actual notification sends.</p>
     *
     * <p>Expected behavior: Notifications with DEFAULT_RECIPIENT_ID are not sent.</p>
     */
    @Test
    public void sendToUser_withDefaultRecipientId_shouldNotSend() {
        // Arrange
        Notification invalidNotification = new Notification(
                "event123",
                "organizer456",
                Notification.DEFAULT_RECIPIENT_ID,
                "Test message"
        );
        invalidNotification.setNotificationId("test123");

        // Act
        notificationManager.sendToUser(invalidNotification);

        // Assert
        verify(mockNotificationDocument, never()).set(any());
    }

    // ==================== SEND TO LIST TESTS ====================

    /**
     * Verifies that a notification is sent to multiple recipients.
     *
     * <p>Business Rule: When sending to a list, each recipient should receive
     * their own copy of the notification with their ID set as the recipient.</p>
     *
     * <p>Expected behavior: sendToUser is called once for each recipient ID
     * in the list.</p>
     */
    @Test
    public void sendToList_withMultipleRecipients_shouldSendToEach() {
        // Arrange - create a spy on our mocked instance
        NotificationManager spyManager = spy(notificationManager);

        ArrayList<String> recipients = new ArrayList<>(Arrays.asList(
                "user1", "user2", "user3"
        ));

        Notification baseNotification = new Notification(
                "event123",
                "organizer456",
                "placeholder",
                "Broadcast message"
        );
        baseNotification.setNotificationId("broadcast001");

        // Act
        spyManager.sendToList(recipients, "organizer456", baseNotification);

        // Assert - sendToUser should be called 3 times
        verify(spyManager, times(3)).sendToUser(any(Notification.class));
    }

    /**
     * Verifies that sending to an empty list completes without errors.
     *
     * <p>Business Rule: An empty recipient list should not cause errors.</p>
     *
     * <p>Expected behavior: No notifications are sent, no exceptions thrown.</p>
     */
    @Test
    public void sendToList_withEmptyList_shouldNotCrash() {
        // Arrange
        ArrayList<String> emptyList = new ArrayList<>();

        // Act
        notificationManager.sendToList(emptyList, "organizer456", testNotification);

        // Assert - no Firestore operations should occur
        verify(mockNotificationDocument, never()).set(any());
    }

    /**
     * Verifies that each recipient receives a notification with their own ID.
     *
     * <p>Business Rule: Each notification copy should have the correct
     * recipient ID, not the placeholder from the base notification.</p>
     *
     * <p>Expected behavior: Each created notification has a unique recipient ID
     * matching one from the list.</p>
     */
    @Test
    public void sendToList_eachNotificationHasCorrectRecipientId() {
        // Arrange
        NotificationManager spyManager = spy(notificationManager);

        ArrayList<String> recipients = new ArrayList<>(Arrays.asList(
                "user1", "user2"
        ));

        Notification baseNotification = new Notification(
                "event123",
                "organizer456",
                "placeholder",
                "Message"
        );
        baseNotification.setNotificationId("shared123");

        // Act
        spyManager.sendToList(recipients, "organizer456", baseNotification);

        // Capture the notifications that were sent
        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(spyManager, times(2)).sendToUser(notificationCaptor.capture());

        List<Notification> sentNotifications = notificationCaptor.getAllValues();

        // Assert each notification has the correct recipient
        assertEquals("user1", sentNotifications.get(0).getRecipientId());
        assertEquals("user2", sentNotifications.get(1).getRecipientId());
    }

    // ==================== LOG ORGANIZER NOTIFICATION TESTS ====================

    /**
     * Verifies that organizer notifications are logged correctly.
     *
     * <p>Business Rule: A copy of each sent notification should be logged
     * under the organizer's sentNotifications subcollection for record-keeping.</p>
     *
     * <p>Expected behavior: The notification is written to
     * users/{organizerId}/sentNotifications/{notificationId}.</p>
     */
    @Test
    public void logOrganizerNotification_withValidNotification_shouldLog() {
        // Act
        notificationManager.logOrganizerNotification("organizer456", testNotification);

        // Assert
        verify(mockDb, atLeastOnce()).collection("users");
        verify(mockUsersCollection).document("organizer456");
        verify(mockUserDocument).collection("sentNotifications");
        verify(mockSentNotificationsCollection).document("notif001");
        verify(mockNotificationDocument).set(testNotification);
    }

    /**
     * Verifies that logging a null notification is handled gracefully.
     *
     * <p>Business Rule: Null notifications should not be logged.</p>
     *
     * <p>Expected behavior: A warning is logged and no Firestore operation occurs.</p>
     */
    @Test
    public void logOrganizerNotification_withNullNotification_shouldNotCrash() {
        // Act
        notificationManager.logOrganizerNotification("organizer123", null);

        // Assert
        verify(mockNotificationDocument, never()).set(any());
    }

    /**
     * Verifies that organizer ID is set on notification if missing.
     *
     * <p>Business Rule: If a notification's organizer ID is null or empty,
     * it should be set from the method parameter before logging.</p>
     *
     * <p>Expected behavior: The notification's organizer ID is updated.</p>
     */
    @Test
    public void logOrganizerNotification_setsOrganizerIdIfMissing() {
        // Arrange
        Notification notification = new Notification(
                "event123",
                null,  // No organizer ID
                "user789",
                "Message"
        );
        notification.setNotificationId("test123");

        // Before logging
        assertNull(notification.getOrganizerId());

        // Act
        notificationManager.logOrganizerNotification("organizer999", notification);

        // Assert - the notification object should have the organizer ID set
        assertEquals("organizer999", notification.getOrganizerId());
    }

    /**
     * Verifies that empty organizer ID is also set.
     *
     * <p>Business Rule: Empty string organizer IDs should also be replaced.</p>
     *
     * <p>Expected behavior: The notification's organizer ID is updated.</p>
     */
    @Test
    public void logOrganizerNotification_setsOrganizerIdIfEmpty() {
        // Arrange
        Notification notification = new Notification(
                "event123",
                "",  // Empty organizer ID
                "user789",
                "Message"
        );
        notification.setNotificationId("test123");

        // Act
        notificationManager.logOrganizerNotification("organizer999", notification);

        // Assert
        assertEquals("organizer999", notification.getOrganizerId());
    }

    // ==================== GET USER NOTIFICATIONS TESTS ====================

    /**
     * Verifies that user notifications are retrieved successfully.
     *
     * <p>Business Rule: User notifications should be fetched from Firestore
     * and ordered by creation time (newest first).</p>
     *
     * <p>Expected behavior: The callback receives a list of notifications
     * in descending order by timeCreated.</p>
     */
    @Test
    public void getUserNotifications_withValidUserId_shouldReturnNotifications() {
        // Arrange
        when(mockNotificationsCollection.orderBy(eq("timeCreated"), eq(Query.Direction.DESCENDING)))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockQuerySnapshot);
            return mockQueryTask;
        });
        when(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask);

        List<DocumentSnapshot> documents = Arrays.asList(mockDocumentSnapshot);
        when(mockQuerySnapshot.getDocuments()).thenReturn(documents);
        when(mockDocumentSnapshot.toObject(Notification.class)).thenReturn(testNotification);

        final ArrayList<Notification>[] result = new ArrayList[]{null};
        NotificationManager.NotificationsCallback callback = new NotificationManager.NotificationsCallback() {
            @Override
            public void onSuccess(ArrayList<Notification> notifications) {
                result[0] = notifications;
            }

            @Override
            public void onError(Exception e) {
                fail("Should not call onError: " + e.getMessage());
            }
        };

        // Act
        notificationManager.getUserNotifications("user123", callback);

        // Assert
        verify(mockNotificationsCollection).orderBy("timeCreated", Query.Direction.DESCENDING);
        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
        assertEquals(testNotification, result[0].get(0));
    }

    /**
     * Verifies that empty user ID triggers an error.
     *
     * <p>Business Rule: User ID must not be empty when fetching notifications.</p>
     *
     * <p>Expected behavior: The error callback is invoked with an
     * IllegalArgumentException.</p>
     */
    @Test
    public void getUserNotifications_withEmptyUserId_shouldCallOnError() {
        // Arrange
        final Exception[] caughtException = {null};

        NotificationManager.NotificationsCallback callback = new NotificationManager.NotificationsCallback() {
            @Override
            public void onSuccess(ArrayList<Notification> notifications) {
                fail("Should not call onSuccess with empty userId");
            }

            @Override
            public void onError(Exception e) {
                caughtException[0] = e;
            }
        };

        // Act
        notificationManager.getUserNotifications("   ", callback);

        // Assert
        assertNotNull(caughtException[0]);
        assertTrue(caughtException[0] instanceof IllegalArgumentException);
        assertEquals("userId is empty", caughtException[0].getMessage());
    }

    /**
     * Verifies that null documents in query results are filtered out.
     *
     * <p>Business Rule: If Firestore returns documents that cannot be converted
     * to Notification objects, they should be skipped rather than causing errors.</p>
     *
     * <p>Expected behavior: Only valid Notification objects are added to the result list.</p>
     */
    @Test
    public void getUserNotifications_filtersNullDocuments() {
        // Arrange
        when(mockNotificationsCollection.orderBy(eq("timeCreated"), eq(Query.Direction.DESCENDING)))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockQuerySnapshot);
            return mockQueryTask;
        });
        when(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask);

        // Create two documents: one valid, one null
        DocumentSnapshot nullDoc = mock(DocumentSnapshot.class);
        when(nullDoc.toObject(Notification.class)).thenReturn(null);

        List<DocumentSnapshot> documents = Arrays.asList(mockDocumentSnapshot, nullDoc);
        when(mockQuerySnapshot.getDocuments()).thenReturn(documents);
        when(mockDocumentSnapshot.toObject(Notification.class)).thenReturn(testNotification);

        final ArrayList<Notification>[] result = new ArrayList[]{null};
        NotificationManager.NotificationsCallback callback = new NotificationManager.NotificationsCallback() {
            @Override
            public void onSuccess(ArrayList<Notification> notifications) {
                result[0] = notifications;
            }

            @Override
            public void onError(Exception e) {
                fail("Should not call onError");
            }
        };

        // Act
        notificationManager.getUserNotifications("user123", callback);

        // Assert - only the valid notification should be in the result
        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
        assertEquals(testNotification, result[0].get(0));
    }

    // ==================== INTEGRATION TESTS ====================

    /**
     * Verifies that notification IDs are preserved when broadcasting.
     *
     * <p>Business Rule: When sending to multiple users, all copies should
     * share the same notification ID for tracking purposes.</p>
     *
     * <p>Expected behavior: Each recipient receives a notification with
     * the same notificationId as the base notification.</p>
     */
    @Test
    public void sendToList_preservesNotificationId() {
        // Arrange
        NotificationManager spyManager = spy(notificationManager);

        ArrayList<String> recipients = new ArrayList<>(Arrays.asList("user1", "user2"));

        Notification baseNotification = new Notification(
                "event123",
                "organizer456",
                "placeholder",
                "Message"
        );
        baseNotification.setNotificationId("shared123");

        // Act
        spyManager.sendToList(recipients, "organizer456", baseNotification);

        // Capture
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(spyManager, times(2)).sendToUser(captor.capture());

        // Assert - all notifications should have the same ID
        for (Notification sent : captor.getAllValues()) {
            assertEquals("shared123", sent.getNotificationId());
        }
    }
}