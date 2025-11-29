package com.example.haboob;

/**
 * {@code Organizer} represents a user with permissions to create and manage events.
 * <p>
 * It extends {@link User} and maintains an {@link EventsList} containing all events
 * owned by the organizer, along with a unique {@code organizerID}.
 */
public class Organizer extends User {

    /** The collection of events managed by this organizer. */
    private EventsList events;

    /** Unique identifier for the organizer (stored alongside user data). */
    private String organizerID;

    // Constructors for subclass
    // Empty for serialization

    /**
     * Minimal constructor intended for serialization frameworks.
     * Initializes an empty {@link EventsList}. User fields are not populated here.
     *
     * @param orgID       organizer's unique ID (may be set later via {@link #setOrganizerID(String)})
     * @param firstName   organizer first name (ignored by this constructor)
     * @param lastName    organizer last name (ignored by this constructor)
     * @param email       organizer email (ignored by this constructor)
     * @param accountType account type (ignored by this constructor)
     */
    public Organizer(String orgID, String firstName, String lastName, String email, String accountType) {
        super();
        this.events = new EventsList();
    }

    // With phone number

    /**
     * Full constructor to create an organizer with contact information.
     * Initializes the {@link EventsList} and stores the {@code organizerID}.
     *
     * @param orgID        organizer's unique ID
     * @param firstName    organizer first name
     * @param lastName     organizer last name
     * @param email        organizer email
     * @param phoneNumber  organizer phone number
     * @param accountType  account type for this user
     */
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

    /**
     * Returns the organizer's event list.
     *
     * @return the {@link EventsList} managed by this organizer
     */
    public EventsList getEventList() {
        return this.events;
    }

    /**
     * Returns the unique ID for this organizer.
     *
     * @return the organizer ID
     */

    @Override
    public String getDeviceId(){return this.organizerID; }

    public String getOrganizerID() {
        return this.organizerID;
    }

    /**
     * Sets the unique ID for this organizer.
     * <p>
     * Intended primarily for deserialization from Firestore.
     *
     * @param orgID the organizer ID to set
     */
    // Setter for serialization from Firestore
    public void setOrganizerID(String orgID) {
        this.organizerID = orgID;
    }

    /**
     * Replaces the event list managed by this organizer.
     *
     * @param events a new {@link EventsList} instance
     */
    public void setEventList(EventsList events) {
        this.events = events;
    }
}
