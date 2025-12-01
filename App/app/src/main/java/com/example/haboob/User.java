package com.example.haboob;

import java.util.List;

/**
 * Abstract base class representing a user in the Haboob application.
 *
 * Users are stored in Firebase Firestore with the following structure:
 * <ul>
 * <li>Collection: "users"</li>
 * <li>Fields:
 *     <ul>
 *     <li>"user_id": The unique device ID associated with the user</li>
 *     <li>"first_name": The user's first name</li>
 *     <li>"last_name": The user's last name</li>
 *     <li>"email": The user's email address</li>
 *     <li>"phone": The user's phone number (optional, may be empty string)</li>
 *     <li>"account_type": User role - Entrant, Organizer, or Admin</li>
 *     </ul>
 * </li>
 * </ul>
 *
 * @author David
 * @version 1.0
 */
public abstract class User {

    /**
     * The unique device ID for this user.
     */
    private String deviceId;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's phone number (optional field).
     */
    private String phone;

    /**
     * The account type - either Entrant, Organizer, or Admin.
     */
    private String accountType;

    /**
     * List of event IDs representing the user's event history.
     * TODO: May be unused
     */
    private List<String> eventHistoryList;


    /**
     * Empty constructor required for Firebase deserialization.
     */
    public User()
    {
        // Empty constructor for firebase
    }

    /**
     * Constructor with phone number.
     * Use this constructor when the user provides a phone number.
     *
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param email The user's email address
     * @param phoneNumber The user's phone number
     * @param accountType The account type (Entrant, Organizer, or Admin)
     */
    public User(String firstName, String lastName, String email, String phoneNumber, String accountType)
    {
        // This constructor is for if the user DOES provide a phone number
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phoneNumber;
        this.accountType = accountType;
    }

    /**
     * Constructor without phone number.
     * Use this constructor when the user does not provide a phone number.
     *
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param email The user's email address
     * @param accountType The account type (Entrant, Organizer, or Admin)
     */
    public User(String firstName, String lastName, String email, String accountType) {
    }

    /**
     * Sets the device ID for this user.
     *
     * @param deviceId The unique device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName The first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName The last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone The phone number (can be null or empty)
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the user's account type.
     *
     * @param accountType The account type (Entrant, Organizer, or Admin)
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * Adds an event ID to the user's event history.
     *
     * @param eventId The ID of the event to add
     */
    public void addEventToHistory(String eventId)
    {
        eventHistoryList.add(eventId);
    }

    /**
     * Gets the user's device ID.
     *
     * @return The device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the user's first name.
     *
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the user's last name.
     *
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The phone number (may be null or empty)
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the user's account type.
     *
     * @return The account type (Entrant, Organizer, or Admin)
     */
    public String getAccountType() {
        return accountType;
    }

}