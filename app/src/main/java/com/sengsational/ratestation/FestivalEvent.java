package com.sengsational.ratestation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FestivalEvent {
    public static final String TAG = FestivalEvent.class.getSimpleName();

    //////////////////////////////////////////////
    // PREFERENCE FIELD NAMES
    //////////////////////////////////////////////
    public static final String EVENT_HASH_PREF = "EVENT_HASH_PREF";
    public static final String EVENT_NAME_PREF = "EVENT_NAME_PREF";
    public static final String EVENT_DATE_PREF = "EVENT_DATE_PREF";
    public static final String GEOLOCATION_PREF = "GEOLOCATION_PREF";
    public static final String EVENT_ADDRESS_PREF = "EVENT_ADDRESS_PREF";
    public static final String EVENT_TEST_FLAG_PREF = "EVENT_TEST_FLAG_PREF";
    public static final String EVENT_MAX_VOTES_PREF = "EVENT_MAX_VOTES_PREF";


    //////////////////////////////////////////////
    // JSON FIELD NAMES
    //////////////////////////////////////////////
    public static final String EVENT_DATE = "event_date";
    public static final String EVENT_HASH = "event_hash";
    public static final String EVENT_NAME = "event_name";
    public static final String GEO_LOCATION = "geo_location";
    public static final String EVENT_ADDRESS = "event_address";


    //             unused JSON fields
    public static final String ACTIVE = "active";
    public static final String CREATION_DATE = "creationDate";
    public static final String EVENT_DESCRIPTION = "event_description";
    public static final String EVENT_ID = "eventID";
    public static final String LAST_CHANGE_DATE = "lastChangeDate";
    public static final String ORGANIZATION_ID = "organizationID";
    public static final String EVENT_TESTING_FLAG = "event_testing_flag";

    // For testing - use to open voting mile radius and event duration
    public static final String TESTING_EVENT_HASH = "Gv7p2eRz"; // TODO: remove when Brad starts sending the eventTestFlag
    public static final String EVENT_MAX_VOTES = "5"; // TODO: remove when Brad starts sending the max votes count
    private static final float TOLERANCE_DISTANCE_MILES = 1.0f;

    //////////////////////////////////////////////
    // INSTANCE VARIABLES FROM JSON
    //////////////////////////////////////////////
    private String eventHash;
    private String eventName;
    private String eventDate;
    private String geoLocation;
    private String eventAddress;
    private String eventTestFlag; //TODO: Get Brad to send this. Used to ignore date and geolocation validation
    private String eventMaxVotes;

    //////////////////////////////////////////////
    // DERIVED INSTANCE VARIABLES
    //////////////////////////////////////////////
    private String venueLatitudeString;
    private String venueLongitudeString;

    public FestivalEvent() {
        //Constructor to build object before populated with web data
    }

    //Constructor for typical situation (event already loaded in previous session)
    public FestivalEvent(SharedPreferences prefs) {
        this.eventHash = prefs.getString(EVENT_HASH_PREF, "");
        this.eventName = prefs.getString(EVENT_NAME_PREF, "");
        this.eventDate = prefs.getString(EVENT_DATE_PREF, "");
        this.geoLocation = prefs.getString(GEOLOCATION_PREF, "");
        this.eventAddress = prefs.getString(EVENT_ADDRESS_PREF, "");
        this.eventTestFlag = prefs.getString(EVENT_TEST_FLAG_PREF, "");
        this.eventMaxVotes = prefs.getString(EVENT_MAX_VOTES_PREF, "5");

        // Derived
        this.venueLatitudeString = getDerivedLatitude(this.geoLocation);
        this.venueLongitudeString = getDerivedLongitude(this.geoLocation);
    }

    public void saveObjectToPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(EVENT_HASH_PREF, getEventHash());
        prefsEditor.putString(EVENT_NAME_PREF, getEventName());
        prefsEditor.putString(EVENT_DATE_PREF, getEventDate());
        prefsEditor.putString(GEOLOCATION_PREF, getGeoLocation());
        prefsEditor.putString(EVENT_ADDRESS_PREF, getEventAddress());
        prefsEditor.putString(EVENT_TEST_FLAG_PREF, getDerivedTestFlag(getEventHash()));
        prefsEditor.putString(EVENT_MAX_VOTES_PREF, getEventMaxVotes());
        prefsEditor.apply();
    }

    //////////////////////////////////////////////
    // INSTANCE LOADING METHODS
    //////////////////////////////////////////////

    public void loadFromJson(String jsonString) {
        StringBuffer buf = new StringBuffer(jsonString);
        if (buf.substring(0,2).equals("[{")){
            buf.delete(0,2);
        }
        parse(buf.toString());
    }
    public void parse(String rawInputString) {
        if (rawInputString == null) {
            System.out.println("nothing to parse");
            return;
        }
        //Every name and value are surrounded by quotes, except sometimes the word `null` is there, but without quotes.  This screws things up, so fix that.
        rawInputString = rawInputString.replaceAll("\"\\:null,", "\"\\:\"null\",");
        //There might be escaped quote marks `\"`.  Those can be replaced by single ticks
        rawInputString = rawInputString.replaceAll("\\\\\"", "'");
        String[] nvpa = rawInputString.split("\",\"");
        for (String nvpString : nvpa) {
            String[] nvpItem = nvpString.split("\":\"");
            if (nvpItem.length < 2) continue;
            String identifier = nvpItem[0].replaceAll("\"", "");
            String content = nvpItem[1].replace("\\\"u", "u"); // "backslash quote u" will become "backslash u", so can't have that.
            content = content.replaceAll("\"", ""); // Remove quotes from within the content.  I don't know why we're doing this any more.
            //content = unescapeUnicode(content);
            switch (identifier) {
                case EVENT_DATE:
                    this.eventDate = content;
                    break;
                case EVENT_HASH:
                    this.eventHash = content;
                    this.eventTestFlag = getDerivedTestFlag(this.eventHash);
                    break;
                case EVENT_NAME:
                    this.eventName = content;
                    break;
                case EVENT_TESTING_FLAG:
                    this.eventTestFlag = content;
                    break;
                case EVENT_MAX_VOTES:
                    this.eventMaxVotes = content;
                    break;
                case GEO_LOCATION:
                    this.geoLocation = content;
                    this.venueLatitudeString = getDerivedLatitude(content);
                    this.venueLongitudeString = getDerivedLongitude(content);
                    break;
                case EVENT_ADDRESS:
                    this.eventAddress = content;
                    break;
                case ACTIVE:
                case CREATION_DATE:
                case EVENT_DESCRIPTION:
                case EVENT_ID:
                case LAST_CHANGE_DATE:
                case ORGANIZATION_ID:
                    break;
                default:
                    System.out.println("nowhere to put [" + nvpItem[0] + "] " + nvpString + " raw: " + rawInputString);
                    break;
            }
        }
    }

    private String getDerivedLatitude(String content) {
        try {
            String[] gpsValues = this.geoLocation.split(",");
            return gpsValues[0];
        } catch (Throwable t) {
            Log.e(TAG, "UNABLE TO PARSE GEO_LOCATION " + content);
        }
        return "";
    }

    private String getDerivedLongitude(String content) {
        try {
            String[] gpsValues = this.geoLocation.split(",");
            return gpsValues[1];
        } catch (Throwable t) {
            Log.e(TAG, "UNABLE TO PARSE GEO_LOCATION" + content);
        }
        return "";
    }

    private String getDerivedTestFlag(String content) {
        if (this.eventHash.equals(TESTING_EVENT_HASH)) {
            return "Y";
        } else {
            return "N";
        }
    }

    //////////////////////////////////////////////
    // GETTERS
    //////////////////////////////////////////////
    public String getEventHash() {
        return eventHash;
    }
    public String getEventName() {
        return eventName;
    }
    public String getEventDate() {
        return eventDate;
    }
    public String getGeoLocation() { return geoLocation; }
    public String getEventAddress() { return eventAddress; }
    public String getEventMaxVotes() {return eventMaxVotes;}
    public boolean isTestEvent() {
        //return false;
        return this.eventTestFlag.equalsIgnoreCase("Y");
    }
    public boolean isToday() {
        if (isTestEvent()) return true;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
            Date date = format.parse(getEventDate());
            return DateUtils.isToday(date.getTime());
        } catch (Throwable t) {
            Log.e(TAG, "Unable to format event date [" + getEventDate() + "]");
        }
        return false;
    }
    public boolean isWithinRadius(double latitude, double longitude) {
        double milesFromCenter = getMilesFromCenter(latitude, longitude);
        Log.v(TAG, "Device is " + milesFromCenter + " miles away from the event location.");
        if (milesFromCenter < TOLERANCE_DISTANCE_MILES) {
            return true;
        } else if (isTestEvent()) {
            Log.v(TAG, "This is a test event, so isWithinRadius() is automatically true.");
            return true;
        }
        else return false;
    }

    public String getVoteRadiusMessage(double latitude, double longitude) {
        double milesFromCenter = getMilesFromCenter(latitude, longitude);
        boolean isTestEvent = isTestEvent();
        String milesFromCenterFormatted = String.format("%d",(long)milesFromCenter);
        if (isTestEvent) {
            return getEventName() + " has been designated for testing, so being " + milesFromCenterFormatted + " miles away from the festival grounds doesn't matter.";
        } else if (milesFromCenter < TOLERANCE_DISTANCE_MILES) {
            return "You are at " + getEventName() + " location.  Your votes are being accepted.";
        } else {
            return "It looks like you're not located at the " + getEventName() + " event location.  Voting is for people that are attending the event.  Why don't you come to " + getEventAddress() + "?";
        }
    }

    public double getMilesFromCenter(double deviceLatitude, double deviceLongitude) {
        double venueLatitude = 0d;
        double venueLongitude = 0d;
        try {
            venueLatitude = Double.parseDouble(venueLatitudeString);
            venueLongitude = Double.parseDouble(venueLongitudeString);
        } catch (Throwable t) {
            Log.e(TAG, "UNABLE TO PARSE LONG/LAT STRINGS TO NUMBERS [" + geoLocation + "]");
        }
        return SingleShotLocationProvider.gps2miles(deviceLatitude, deviceLongitude, venueLatitude, venueLongitude);
    }
}
