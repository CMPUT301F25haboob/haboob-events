package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

    /* Event has an organizer, as well as lists for all:
     * tags, entrants, invitedUsers, enrolledUsers, and cancelledUsers.
     * We might need to store a QRCode object
     * We need to store the Poster and Map including geolocation data
     */
    // Database reference
    private FirebaseFirestore db;

    // The organizer of the event
    private String organizerID;

    // The ID of the event in firebase
    private String eventID;

    // Details of event
    private Date registrationStartDate;
    private Date registrationEndDate;
    private String eventTitle;
    private String eventDescription;
    private boolean geoLocationRequired;
    private int lotterySampleSize;
    private int optionalWaitingListSize;
    private GeoLocationMap geoLocationMap;
    private QRCode qrCode;
    private Poster poster;

    // All of the lists that events have (all other than tags will have the entries as strings of user IDs)
    private ArrayList<String> tags;
    private ArrayList<String> entrants;
    private ArrayList<String> invitedEntrants;
    private ArrayList<String> waitingEntrants;
    private ArrayList<String> enrolledEntrants;
    private ArrayList<String> cancelledEntrants;

    // to store the entrants that are in the lottery
    private ArrayList<String> entrant_ids_for_lottery;
    private String event_image;


    // Constructor for an event
    public Event() {
        // Constructor for firebase only
        this.db = FirebaseFirestore.getInstance();
        this.initLists();
    }

    public Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, int optionalWaitingListSize, QRCode qrCode, Poster poster, ArrayList<String> tags) {
        this.db = FirebaseFirestore.getInstance();
        this.organizerID = organizer;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.geoLocationRequired = geoLocationRequired;
        this.lotterySampleSize = lotterySampleSize;
        this.optionalWaitingListSize = optionalWaitingListSize;
        this.qrCode = qrCode;
        this.poster = poster;
        this.tags = tags;
        this.initLists();
    }

    public void initLists() {
        // Initialize all the lists to use/populate later
        this.tags = new ArrayList<String>();
        this.entrants = new ArrayList<String>();
        this.invitedEntrants = new ArrayList<String>();
        this.waitingEntrants = new ArrayList<String>();
        this.enrolledEntrants = new ArrayList<String>();
        this.cancelledEntrants = new ArrayList<String>();
    }

    public void addEntrantToEntrants(String userID) {
        this.entrants.add(userID);
        db.collection("events").document(eventID).update("entrants", FieldValue.arrayUnion(userID));
    }

    public void addEntrantToInvitedEntrants(String userID) {
        this.invitedEntrants.add(userID);
        db.collection("events").document(eventID).update("invitedEntrants", FieldValue.arrayUnion(userID));
    }

    public void addEntrantToWaitingEntrants(String userID) {
        this.waitingEntrants.add(userID);
        db.collection("events").document(eventID).update("waitingEntrants", FieldValue.arrayUnion(userID));
    }

    public void addEntrantToEnrolledEntrants(String userID) {
        this.enrolledEntrants.add(userID);
        db.collection("events").document(eventID).update("enrolledEntrants", FieldValue.arrayUnion(userID));
    }

    public void addEntrantToCancelledEntrants(String userID) {
        this.cancelledEntrants.add(userID);
        db.collection("events").document(eventID).update("cancelledEntrants", FieldValue.arrayUnion(userID));
    }


    public void logEventLists() {
        // TESTING FUNCTION
        Log.d("Event", "Entrants: " + this.entrants);
        Log.d("Event", "Invited Entrants: " + this.invitedEntrants);
        Log.d("Event", "Waiting Entrants: " + this.waitingEntrants);
        Log.d("Event", "Enrolled Entrants: " + this.enrolledEntrants);
        Log.d("Event", "Cancelled Entrants: " + this.cancelledEntrants);
    }
    // different constructor for tags2, which just is a list of strings instead of an EventTagList, works better in fireBase
    public Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, QRCode qrCode, Poster poster, ArrayList<String> tags, ArrayList<String> entrant_ids_for_lottery) {
        this.organizerID = organizer;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.geoLocationRequired = geoLocationRequired;
        this.lotterySampleSize = lotterySampleSize;
        this.optionalWaitingListSize = -1;
        this.qrCode = qrCode;
        this.poster = poster;
        this.tags = tags;
        this.entrant_ids_for_lottery = entrant_ids_for_lottery;
    }


        // GETTER METHODS BELOW
    public String getOrganizer() {
    	return this.organizerID;
    }

    public ArrayList<String> getEntrant_ids_for_lottery() {
        return this.entrant_ids_for_lottery;
    }

    public String getEventID() { return this.eventID; }

    public Date getRegistrationStartDate() {
    	return this.registrationStartDate;
    }

    public Date getRegistrationEndDate() {
    	return this.registrationEndDate;
    }

    public String getEventTitle() {
    	return this.eventTitle;
    }

    public String getEventDescription() {
        return this.eventDescription;
    }

    public boolean getGeoLocationRequired() {
        return this.geoLocationRequired;
    }

    public int getLotterySampleSize() {
        return this.lotterySampleSize;
    }

    public int getOptionalWaitingListSize() { return this.optionalWaitingListSize; }

    public GeoLocationMap getGeoLocationMap() {
        return this.geoLocationMap;
    }

    public QRCode getQRCode() {
        return this.qrCode;
    }

    public Poster getPoster() {
        return this.poster;
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public ArrayList<String> getEntrants() {
        return this.entrants;
    }

    public ArrayList<String> getInvitedEntrants() {
        return this.invitedEntrants;
    }

    public ArrayList<String> getWaitingEntrants() {
        return this.waitingEntrants;
    }

    public ArrayList<String> getEnrolledEntrants() {
        return this.enrolledEntrants;
    }

    public ArrayList<String> getCancelledEntrants() {
        return this.cancelledEntrants;
    }

    // SETTER METHODS BELOW:
    public void setOrganizer(String organizer) {
        this.organizerID = organizer;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public void setRegistrationStartDate(Date registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public void setRegistrationEndDate(Date registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setGeoLocationRequired(boolean geoLocationRequired) {
        this.geoLocationRequired = geoLocationRequired;
    }

    public void setLotterySampleSize(int lotterySampleSize) {
        this.lotterySampleSize = lotterySampleSize;
    }

    public void setOptionalWaitingListSize(int optionalWaitingListSize) {
        this.optionalWaitingListSize = optionalWaitingListSize;
    }

    public void setGeoLocationMap(GeoLocationMap geoLocationMap) {
        this.geoLocationMap = geoLocationMap;
    }

    public void setQRCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public void setPoster(Poster poster) {
        this.poster = poster;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setEntrantsList(ArrayList<String> entrants) {
        this.entrants = entrants;
    }

    public void setInvitedEntrantsList(ArrayList<String> invitedEntrants) {
        this.invitedEntrants = invitedEntrants;
    }

    public void setWaitingEntrants(ArrayList<String> waitingEntrants) {
        this.waitingEntrants = waitingEntrants;
    }

    public void setEnrolledEntrantsList(ArrayList<String> enrolledEntrants) {
        this.enrolledEntrants = enrolledEntrants;
    }

    public void setCancelledEntrantsList(ArrayList<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;;
    }
}
