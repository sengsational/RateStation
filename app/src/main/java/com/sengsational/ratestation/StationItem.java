package com.sengsational.ratestation;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Owner on 5/13/2016.
 */
public class StationItem {
    //////////////////////////////////////////////
    // STATIC ITEMS
    //////////////////////////////////////////////
    static final String TAG = StationItem.class.getSimpleName();

    //////////////////////////////////////////////
    // MODEL ONLY FIELD NAMES
    //////////////////////////////////////////////
    public static final String MODEL_ONLY_EXAMPLE1 = "MODEL_ONLY_EXAMPLE1";
    public static final String MODEL_ONLY_EXAMPLE2 = "MODEL_ONLY_EXAMPLE2";
    public static final String SEARCH_CONCAT = "SEARCH_CONCAT";
    public static final String USER_REVIEW = "USER_REVIEW";
    public static final String USER_STARS = "USER_STARS";
    public static final String[] FIELDS = {MODEL_ONLY_EXAMPLE1, MODEL_ONLY_EXAMPLE2, SEARCH_CONCAT, USER_REVIEW, USER_STARS};
    public static final String[] FIELDS_ALL = Stream.concat(Arrays.stream(StationDbItem.FIELDS), Arrays.stream(StationItem.FIELDS)).toArray(String[]::new);


    //////////////////////////////////////////////
    // CONSTRUCTORS //
    //////////////////////////////////////////////
    public StationItem() {
        this.stationDbItem = new StationDbItem(0L);
    }

    public StationItem(Cursor cursor) {
        populate(cursor); // Creates new stationDbItem
    }

    //////////////////////////////////////////////
    // INSTANCE VARIABLES
    //////////////////////////////////////////////
    StationDbItem stationDbItem; //fields owned by the external database
    String modelOnlyExample1; // field not in the external database
    String modelOnlyExample2; // field not in the external database
    String searchConcat;
    String userReview;
    String userStars;

    //////////////////////////////////////////////
    // DATABASE TABLE DEFINITIONS
    //////////////////////////////////////////////

    public static String getDatabaseTableCreationCommand() {
        return "CREATE TABLE " + RatstatDatabaseAdapter.MAIN_TABLE + " (" +
                StationDbItem.getDatabaseTableCreationFields() +
                ", " +
                StationItem.getDatabaseTableCreationFields() +
                ")";
    }

    /**
     * Uses static final FIELDS[] to make an SQL fragment.
     * @return Returns a String like "FIELD1 TEXT, FIELD2 TEXT, FIELD3 TEXT" (no leading or trailing comma)
     */
    public static String getDatabaseTableCreationFields() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < FIELDS.length; i++) {
            buf.append(FIELDS[i]).append(" TEXT, ");
        }
        buf.delete(buf.length()-2, buf.length());
        return buf.toString();
    }

    public static boolean isLocalOnly(String columnName) {
        boolean found = false;
        for (int i = 0; i < FIELDS.length; i++) {
            if (FIELDS[i].equals(columnName)) {
                found = true;
                break;
            }
        }
        return found;
    }

    //////////////////////////////////////////////
    // DATABASE UTILITY METHODS
    //////////////////////////////////////////////
    public void loadFromJson(String jsonString) {
        this.stationDbItem.loadFromJson(jsonString);
        loadDefaultLocal();
    }

    private void loadDefaultLocal() {
        // Build contents for the search field
        StringBuffer buf = new StringBuffer();
        buf.append(stationDbItem.getBeerCode()).append(" ");
        buf.append(stationDbItem.getBeerName()).append(" ");
        buf.append(stationDbItem.getBjcpCategory()).append(" ");
        buf.append(stationDbItem.getBjcpCode()).append(" ");
        buf.append(stationDbItem.getBrewerName()).append(" ");
        buf.append(stationDbItem.getStyleCode()).append(" ");
        buf.append(stationDbItem.getTentSpace()).append(" ");
        buf.append(stationDbItem.getVendorAbbr()).append(" ");
        buf.append(stationDbItem.getVendorName()).append(" ");
        this.searchConcat = buf.toString();

        // put blank in for everything else
        this.modelOnlyExample1 = "";
        this.modelOnlyExample2 = "";
        this.userReview = "";
        this.userStars = "";
    }

    // Normally the only thing that changes (changed by the user) is the highlighted, but we put everything back except null items
    public static ContentValues fillModelValues(StationItem model){
        ContentValues values = new ContentValues();
        values = StationDbItem.fillModelValues(model.stationDbItem, values);
        values.put(MODEL_ONLY_EXAMPLE1, model.modelOnlyExample1);
        values.put(MODEL_ONLY_EXAMPLE2, model.modelOnlyExample2);
        values.put(SEARCH_CONCAT, model.searchConcat);
        values.put(USER_REVIEW, model.userReview);
        values.put(USER_STARS, model.userStars);
        Iterator<String> iterator = values.keySet().iterator();
        ArrayList<String> removeList = new ArrayList<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (null == values.get(key)) removeList.add(key);
        }
        for (String removeItem: removeList) {
            values.remove(removeItem);
        }
        return values;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values = stationDbItem.getContentValues(values);
        if (this.modelOnlyExample1 != null) values.put(MODEL_ONLY_EXAMPLE1, modelOnlyExample1);
        if (this.modelOnlyExample2 != null) values.put(MODEL_ONLY_EXAMPLE2, modelOnlyExample2);
        if (this.searchConcat != null) values.put(SEARCH_CONCAT, searchConcat);
        if (this.userReview != null) values.put(USER_REVIEW, userReview);
        if (this.userStars != null) values.put(USER_STARS, userStars);

        /*
        Set<String> keys = values.keySet();
        for (String key : keys) {
            Log.v(TAG, "Content value " + key + " = " + values.get(key));
        }
        */
        return values;
    }
    public StationItem populate(Cursor cursor) {
        try {
            // Clear all values before populating from cursor
            modelOnlyExample1 = null;
            modelOnlyExample2 = null;
            searchConcat = null;
            userReview = null;
            userStars = null;
            stationDbItem = new StationDbItem();
            stationDbItem.populate(cursor);

            // Loop column names for model only instance variables
            String[] columnNames = cursor.getColumnNames();
            for (String columnName: columnNames) {
                //columnNamesStringBuff.append(columnName).append(",");
                switch  (columnName) {
                    case MODEL_ONLY_EXAMPLE1:
                        modelOnlyExample1 = cursor.getString(cursor.getColumnIndexOrThrow(MODEL_ONLY_EXAMPLE1));
                        break;
                    case MODEL_ONLY_EXAMPLE2:
                        modelOnlyExample2 = cursor.getString(cursor.getColumnIndexOrThrow(MODEL_ONLY_EXAMPLE2));
                        break;
                    case SEARCH_CONCAT:
                        searchConcat = cursor.getString(cursor.getColumnIndexOrThrow(SEARCH_CONCAT));
                        break;
                    case USER_REVIEW:
                        userReview = cursor.getString(cursor.getColumnIndexOrThrow(USER_REVIEW));
                        break;
                    case USER_STARS:
                        userStars = cursor.getString(cursor.getColumnIndexOrThrow(USER_STARS));
                        break;
                    default:
                        if (!StationDbItem.hasField(columnName)) Log.v(TAG, "Not sure what to do about " + columnName);
                }
            }
        } catch (Throwable t) {
            Log.v(TAG, "Failed to complete model item from database cursor. " + t.getMessage())    ;
        }
        return this;
    }

    //////////////////////////////////////////////
    // UTILITY METHODS
    //////////////////////////////////////////////

    public String getId() {
        return this.stationDbItem.getId();
    }

    public StationDbItem getStationDbItem() {
        return stationDbItem;
    }

    public String getEventBeerId() {
        return this.stationDbItem.getEventBeerId();
    }

    public String getNarrativeDescription(boolean includeBrewer) {
        StringBuffer buf = new StringBuffer();
        buf.append("This beer");
        if (includeBrewer) {
            buf.append(", brewed by ");
            buf.append(stationDbItem.getBrewerName());
            buf.append(", a member of ");
        } else {
            buf.append( ", presented by ");
        }
        buf.append(stationDbItem.getVendorName());
        buf.append( " (");
        buf.append(stationDbItem.getVendorAbbr());
        buf.append("), is classified as \'");
        buf.append(stationDbItem.getBjcpCategory());
        buf.append("\' (");
        buf.append(stationDbItem.getBjcpCode());
        buf.append(").  ");
        buf.append(BjcpHelper.getOriginalImpression(stationDbItem.getBjcpCode()));
        return buf.toString();
    }


    public String toString() {
        return stationDbItem.toString()
                + modelOnlyExample1 + ", "
                + modelOnlyExample2 + ", "
                + searchConcat + ", "
                + userReview + ", "
                + userStars;
    }

    public String getUserReview() {
        return userReview;
    }

    public String getUserStars() {
        return userStars;
    }

    public void setUserReview(String ratingText) {
        this.userReview = ratingText;
    }

    public void setUserStars(String s) {
        this.userStars = s;
    }

}
