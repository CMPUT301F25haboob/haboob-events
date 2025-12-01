package com.example.haboob;

/**
 * Represents an Entrant user in the Haboob application.
 * Entrants are users who participate in events by joining waitlists
 * and can be selected through lottery drawings to attend events.
 *
 * This class extends the User class and automatically sets the account type to "Entrant".
 *
 * @author Haboob Team
 * @version 1.0
 * @see User
 */
public class Entrant extends User{
    /**
     * Constructs a new Entrant with the specified information.
     * The account type is automatically set to "Entrant".
     *
     * @param firstName The entrant's first name
     * @param lastName The entrant's last name
     * @param email The entrant's email address
     * @param phoneNumber The entrant's phone number
     */
    public Entrant(String firstName, String lastName, String email, String phoneNumber) {
        super(firstName, lastName, email, phoneNumber, "Entrant");
    }
}