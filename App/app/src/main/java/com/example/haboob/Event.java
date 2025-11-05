package com.example.haboob;

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

    // All of the lists that events have
    private ArrayList<String> tags;
//    private EntrantList entrants;
//    private InviteList invitedUsers;
//    private WaitingList waitingUsers;
//    private EnrolledList enrolledUsers;
//    private CancelledList cancelledUsers;

    // to store the entrants that are in the lottery
    private ArrayList<String> entrant_ids_for_lottery;
    private String event_image;


    // Constructor for an event
    public Event() {
        // Empty constructor for firestore
    }

    public Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, int optionalWaitingListSize, QRCode qrCode, Poster poster, ArrayList<String> tags) {
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
        this.tags = (tags == null) ? new ArrayList<>() : new ArrayList<>(tags);
    }

    // For EventsListTest
    public Event(String organizerId, Date date, Date date1, String s, String s1, boolean b, int i, Object o, Object o1, List<String> tags) {
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

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

//    public EntrantList getEntrants() {
//        return this.entrants;
//    }

//    public InviteList getInvitedUsers() {
//        return this.invitedUsers;
//    }

//    public WaitingList getWaitingUsers() {
//        return this.waitingUsers;
//    }

//    public EnrolledList getEnrolledUsers() {
//        return this.enrolledUsers;
//    }

//    public CancelledList getCancelledUsers() {
//        return this.cancelledUsers;
//    }

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
}
