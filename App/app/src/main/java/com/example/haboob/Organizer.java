package com.example.haboob;

public class Organizer extends User {

    // Constructors for subclass
    // Without phone number
    public Organizer(String firstName, String lastName, String email, String accountType) {
        super(firstName, lastName, email, accountType);
    }

    // With phone number
    public Organizer(String firstName, String lastName, String email, String phoneNumber, String accountType) {
        super(firstName, lastName, email, phoneNumber, accountType);
    }
}
