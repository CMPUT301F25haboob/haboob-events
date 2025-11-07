package com.example.haboob;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Event implements Serializable {

    /* Event has an organizer, as well as lists for all:
     * tags, entrants, invitedUsers, enrolledUsers, and cancelledUsers.
     * We might need to store a QRCode object
     * We need to store the Poster and Map including geolocation data
     */
    // Database reference
    private FirebaseFirestore db;
    private DocumentReference docRef;
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
    private ArrayList<String> tags;  // -> List of tags associated to the event
    private ArrayList<String> invitedEntrants;  // -> List of all entrants who got selected for the lottery
    private ArrayList<String> waitingEntrants;  // -> List of all entrants who were not selected for the lottery, didn't cancel, and are waiting to fill in upon entrant cancellation
    private ArrayList<String> enrolledEntrants;  // -> List of all entrants who accepted their invite
    private ArrayList<String> cancelledEntrants;  // -> List of all entrants who cancelled their invite or were cancelled by the organizer

    // to store the entrants that are in the lottery
//    private ArrayList<String> entrant_ids_for_lottery; deprecated by david
    private String event_image;


    // Constructor for an event
    public Event() {
        // Constructor for firebase only
        this.db = FirebaseFirestore.getInstance();
        this.initLists();
    }

    public Event(String organizer, Date registrationStartDate, Date registrationEndDate, String eventTitle, String eventDescription, boolean geoLocationRequired, int lotterySampleSize, int optionalWaitingListSize, QRCode qrCode, Poster poster, ArrayList<String> tags) {
        this.eventID = UUID.randomUUID().toString();
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
        this.invitedEntrants = new ArrayList<String>();
        this.waitingEntrants = new ArrayList<String>();
        this.enrolledEntrants = new ArrayList<String>();
        this.cancelledEntrants = new ArrayList<String>();
    }

    public void addEntrantToInvitedEntrants(String userID) {
        this.invitedEntrants.add(userID);

        db.collection("events")
                .whereEqualTo("eventID", eventID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the actual document ID from the query result
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document using its ID
                        db.collection("events")
                                .document(documentId)
                                .update("invitedEntrants", FieldValue.arrayUnion(userID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Event", "Successfully added user to invitedEntrants");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error updating invitedEntrants", e);
                                });
                    } else {
                        Log.e("Event", "No event found with eventID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Error querying event", e);
                });
    }

    public void addEntrantToWaitingEntrants(String userID) {
        this.invitedEntrants.add(userID);

        db.collection("events")
                .whereEqualTo("eventID", eventID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the actual document ID from the query result
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document using its ID
                        db.collection("events")
                                .document(documentId)
                                .update("waitingEntrants", FieldValue.arrayUnion(userID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Event", "Successfully added user to waitingEntrants");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error updating waitingEntrants", e);
                                });
                    } else {
                        Log.e("Event", "No event found with eventID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Error querying event", e);
                });
    }

    public void addEntrantToEnrolledEntrants(String userID) {
        this.invitedEntrants.add(userID);

        db.collection("events")
                .whereEqualTo("eventID", eventID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the actual document ID from the query result
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document using its ID
                        db.collection("events")
                                .document(documentId)
                                .update("enrolledEntrants", FieldValue.arrayUnion(userID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Event", "Successfully added user to enrolledEntrants");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error updating enrolledEntrants", e);
                                });
                    } else {
                        Log.e("Event", "No event found with eventID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Error querying event", e);
                });
    }

    public void addEntrantToCancelledEntrants(String userID) {
        this.cancelledEntrants.add(userID);
        db.collection("events")
                .whereEqualTo("eventID", eventID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the actual document ID from the query result
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document using its ID
                        db.collection("events")
                                .document(documentId)
                                .update("cancelledEntrants", FieldValue.arrayUnion(userID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Event", "Successfully added user to cancelledEntrants");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error updating cancelledEntrants", e);
                                });
                    } else {
                        Log.e("Event", "No event found with eventID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Error querying event", e);
                });
    }

    // Remove user from lists:
    public void removeEntrantFromWaitingEntrants(String userID) {

        // Remove locally
        if (this.waitingEntrants != null) {
            this.waitingEntrants.remove(userID);
        }

        // Safety check — can't query without eventID
        if (this.eventID == null || this.eventID.isEmpty()) {
            Log.w("Event", "removeEntrantFromWaitingEntrants: eventID is null or empty");
            return;
        }

        // Find the document where eventID == this.eventID
        db.collection("events")
                .whereEqualTo("eventID", this.eventID)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        Log.w("Event", "No event document found matching eventID=" + this.eventID);
                        return;
                    }

                    // We expect exactly 1 document
                    String docId = querySnapshot.getDocuments().get(0).getId();

                    // Step 2: Remove the user from waitingEntrants using arrayRemove
                    db.collection("events")
                            .document(docId)
                            .update("waitingEntrants", FieldValue.arrayRemove(userID))
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Event", "Successfully removed " + userID + " from waitingEntrants in Firebase")
                            )
                            .addOnFailureListener(e ->
                                    Log.e("Event", "Error updating waitingEntrants", e)
                            );

                })
                .addOnFailureListener(e ->

                    Log.e("Event", "Failed to query event document by eventID", e)
                );
    }

    public void removeEntrantFromInvitedEntrants(String userID) {

            // Remove locally
            if (this.invitedEntrants != null) {
                this.invitedEntrants.remove(userID);
            }

            // Safety check — can't query without eventID
            if (this.eventID == null || this.eventID.isEmpty()) {
                Log.w("Event", "removeEntrantFromInvitedEntrants: eventID is null or empty");
                return;
            }

            // Find the document where eventID == this.eventID
            db.collection("events")
                    .whereEqualTo("eventID", this.eventID)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        if (querySnapshot.isEmpty()) {
                            Log.w("Event", "No event document found matching eventID=" + this.eventID);
                            return;
                        }

                        // We expect exactly 1 document
                        String docId = querySnapshot.getDocuments().get(0).getId();

                        // Step 2: Remove the user from waitingEntrants using arrayRemove
                        db.collection("events")
                                .document(docId)
                                .update("invitedEntrants", FieldValue.arrayRemove(userID))
                                .addOnSuccessListener(aVoid ->
                                        Log.d("Event", "Successfully removed " + userID + " from invitedEntrants in Firebase")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("Event", "Error updating invitedEntrants", e)
                                );

                    })
                    .addOnFailureListener(e ->
                            Log.e("Event", "Failed to query event document by eventID", e)
                    );
    }
    public void removeEntrantFromEnrolledEntrants(String userID) {
        // Remove locally
        if (this.enrolledEntrants != null) {
            this.enrolledEntrants.remove(userID);
        }

        // Safety check — can't query without eventID
        if (this.eventID == null || this.eventID.isEmpty()) {
            Log.w("Event", "removeEntrantFromEnrolledEntrants: eventID is null or empty");
            return;
        }

        // Find the document where eventID == this.eventID
        db.collection("events")
                .whereEqualTo("eventID", this.eventID)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        Log.w("Event", "No event document found matching eventID=" + this.eventID);
                        return;
                    }

                    // We expect exactly 1 document
                    String docId = querySnapshot.getDocuments().get(0).getId();

                    // Step 2: Remove the user from waitingEntrants using arrayRemove
                    db.collection("events")
                            .document(docId)
                            .update("enrolledEntrants", FieldValue.arrayRemove(userID))
                            .addOnSuccessListener(aVoid ->
                                    Log.d("Event", "Successfully removed " + userID + " from enrolledEntrants in Firebase")
                            )
                            .addOnFailureListener(e ->
                                    Log.e("Event", "Error updating enrolledEntrants", e)
                            );

                })
                .addOnFailureListener(e ->
                        Log.e("Event", "Failed to query event document by eventID", e)
                );
    }
    public void removeEntrantFromCancelledEntrants(String userID) {
        this.cancelledEntrants.add(userID);
        db.collection("events")
                .whereEqualTo("eventID", eventID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the actual document ID from the query result
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document using its ID
                        db.collection("events")
                                .document(documentId)
                                .update("cancelledEntrants", FieldValue.arrayUnion(userID))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Event", "Successfully added user to cancelledEntrants");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Event", "Error updating cancelledEntrants", e);
                                });
                    } else {
                        Log.e("Event", "No event found with eventID: " + eventID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Event", "Error querying event", e);
                });
    }


    public void logEventLists() {
        // TESTING FUNCTION
        Log.d("Event", "Invited Entrants: " + this.invitedEntrants);
        Log.d("Event", "Waiting Entrants: " + this.waitingEntrants);
        Log.d("Event", "Enrolled Entrants: " + this.enrolledEntrants);
        Log.d("Event", "Cancelled Entrants: " + this.cancelledEntrants);
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
//        this.entrant_ids_for_lottery = entrant_ids_for_lottery;
    }


        // GETTER METHODS BELOW
    public String getOrganizer() {
    	return this.organizerID;
    }

//    public ArrayList<String> getEntrant_ids_for_lottery() {
//        return this.entrant_ids_for_lottery;
//    }

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
