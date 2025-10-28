package com.example.haboob;

public abstract class User {
/* Users will have the following info inside Firebase:
 * Collection:
 *  "users"
 * Fields:
 *  "user_id": The unique device ID associated with the user
 *  "first_name": The users first name
 *  "last_name": The users last name
 *  "email": The users email
 *  "phone": The users phone number (optional so may be an empty string)
 *  "account_type": This will be Entrant, Organizer, or Admin
 */


    private String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone; // Optional - constructor should reflect this
    private String accountType; // TODO: may not need this later??

    public User(String firstName, String lastName, String email, String accountType)
    {
        // This constructor is for if the user does NOT provide a phone number
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = ""; // Phone number is an empty string
        this.accountType = accountType;
    }

    public User(String firstName, String lastName, String email, String phoneNumber, String accountType)
    {
        // This constructor is for if the user DOES provide a phone number
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phoneNumber;
        this.accountType = accountType;
    }



}
