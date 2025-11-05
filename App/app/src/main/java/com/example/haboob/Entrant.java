package com.example.haboob;

public class Entrant extends User {

    private EventsList events;
    private String entrantID;

    // Constructors for subclass
    // Empty for serialization
    public Entrant(String orgID, String firstName, String lastName, String email, String accountType) {
        super();
        this.events = new EventsList();
    }

    // With phone number
    public Entrant(String entrantID, String firstName, String lastName, String email, String phoneNumber, String accountType) {
        super(firstName, lastName, email, phoneNumber, accountType);
        this.events = new EventsList();
        this.entrantID = entrantID;
        // SHOULD PULL FROM DATABASE TO SET EVENTSLIST
    }

    // constructor that avoids Firestore for tests - Owen
    public Entrant(String orgID, String firstName, String lastName, String email, String accountType, boolean inMemoryOnly) {
        super(firstName, lastName, email, accountType);
        this.entrantID = orgID;
        this.events = new EventsList(inMemoryOnly);
    }

    public EventsList getEventList() {
        return this.events;
    }

    public String getEntrantID() {
        return this.entrantID;
    }

    // Setter for serialization from Firestore
    public void setEntrantID(String entrantID) {
        this.entrantID = entrantID;
    }

    public void setEventList(EventsList events) {
        this.events = events;
    }
}