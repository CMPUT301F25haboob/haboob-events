package com.example.haboob;

public class Organizer extends User {

    private EventsList events;
    private String organizerID;

    // Constructors for subclass
    // Empty for serialization
    public Organizer(String orgID, String firstName, String lastName, String email, String accountType) {
        super();
        this.events = new EventsList();
    }

    // With phone number
    public Organizer(String orgID, String firstName, String lastName, String email, String phoneNumber, String accountType) {
        super(firstName, lastName, email, phoneNumber, accountType);
        this.events = new EventsList();
        this.organizerID = orgID;
        // SHOULD PULL FROM DATABASE TO SET EVENTSLIST
    }

//    // constructor that avoids Firestore for tests - Owen
//    public Organizer(String orgID, String firstName, String lastName, String email, String accountType, boolean inMemoryOnly) {
//        super(firstName, lastName, email, accountType);
//        this.organizerID = orgID;
//        this.events = new EventsList(inMemoryOnly);
//    }

    public EventsList getEventList() {
        return this.events;
    }

    public String getOrganizerID() {
        return this.organizerID;
    }

    // Setter for serialization from Firestore
    public void setOrganizerID(String orgID) {
        this.organizerID = orgID;
    }

    public void setEventList(EventsList events) {
        this.events = events;
    }
}
