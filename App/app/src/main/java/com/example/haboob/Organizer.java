package com.example.haboob;

public class Organizer extends User {

    private EventsList events;
    private String organizerID;

    // Constructors for subclass
    // Without phone number
    public Organizer(String orgID, String firstName, String lastName, String email, String accountType) {
        super(firstName, lastName, email, accountType);
        this.events = new EventsList();
        this.organizerID = orgID;
        // SHOULD PULL FROM DATABASE TO SET EVENTSLIST
    }

    // With phone number
    public Organizer(String orgID, String firstName, String lastName, String email, String phoneNumber, String accountType) {
        super(firstName, lastName, email, phoneNumber, accountType);
        this.events = new EventsList();
        this.organizerID = orgID;
        // SHOULD PULL FROM DATABASE TO SET EVENTSLIST
    }

    // constructor that avoids Firestore for tests - Owen
    public Organizer(String orgID, String firstName, String lastName, String email, String accountType, boolean inMemoryOnly) {
        super(firstName, lastName, email, accountType);
        this.organizerID = orgID;
        this.events = new EventsList(inMemoryOnly);
    }

    public EventsList getEventList() {
        return this.events;
    }

    public String getOrganizerID() {
        return this.organizerID;
    }
}
