package com.example.haboob;
import java.util.Date;
import java.util.UUID;

public class Notification {
    public static final String DEFAULT_MESSAGE = "Empty Notification";
    public static final String DEFAULT_RECIPIENT_ID = "NONE";
    public static final String DEFAULT_EVENT_ID = "NONE";


    private String notificationId;
    private String eventId;
    private String organizerId; // sender ID
    private String recipientId; // target device ID for entrant

    private String message;
    private boolean read;
    private Date timeCreated;

    // Empty constructor
    public Notification() {
        this.read = false;
        this.timeCreated = new Date();
        this.notificationId = UUID.randomUUID().toString();
        this.message = DEFAULT_MESSAGE;
        this.recipientId = DEFAULT_RECIPIENT_ID;
        this.eventId = DEFAULT_EVENT_ID;
    }

    public Notification(String eventId, String organizerId, String recipientId, String message) {
        this();
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.recipientId = recipientId;
        setMessage(message);
    }

    // GETTERS
    public String getNotificationId() {
        return notificationId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    // SETTERS
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            this.message = DEFAULT_MESSAGE;
        } else {
            this.message = message.trim();
        }
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

}
