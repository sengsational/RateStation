package com.sengsational.ratestation;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.jsoup.parser.Parser;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class StationDbItem {
    public static final String TAG = StationDbItem.class.getSimpleName();

    private String mRawInputString;

    //////////////////////////////////////////////
    // CLASS INSTANCE VARIABLES FROM THE DATABASE //
    //////////////////////////////////////////////
    Long _id; //automatically assigned

    String abv;
    String beerCode;
    String beerName;
    String bjcpCategory;
    String bjcpCode;
    String brewerName;
    String eventBeerId;
    String eventHash;
    String styleCode;
    String tentSpace;
    String vendorAbbr;
    String vendorName;
    String voteGroup;

    //////////////////////////////////////////////
    // DATABASE FIELD NAMES
    //////////////////////////////////////////////
    public static final String ID = "_id";// INTEGER PRIMARY KEY AUTOINCREMENT, " +
    public static final String ABV =  "abv";
    public static final String BEER_CODE =  "beer_code";
    public static final String BEER_NAME =  "beer_name";
    public static final String BJCP_CATEGORY =  "bjcp_category";
    public static final String BJCP_CODE =  "bjcp_code";
    public static final String BREWER_NAME =  "brewer_name";
    public static final String EVENT_BEER_ID =  "eventBeerID";
    public static final String EVENT_HASH =  "event_hash";
    public static final String STYLE_CODE =  "style_code";
    public static final String TENT_SPACE =  "tent_space";
    public static final String VENDOR_ABBR =  "vendor_abbr";
    public static final String VENDOR_NAME =  "vendor_name";
    public static final String VOTE_GROUP =  "vote_group";

    public static final String[] FIELDS = {ID, ABV, BEER_CODE, BEER_NAME, BJCP_CATEGORY, BJCP_CODE, BREWER_NAME, EVENT_BEER_ID, EVENT_HASH, STYLE_CODE, TENT_SPACE, VENDOR_ABBR, VENDOR_NAME, VOTE_GROUP};

    public StationDbItem(long l) {
        _id = l;
    }

    public StationDbItem() {
    }

    /**
     * Uses static final FIELDS[] to make an SQL fragment.
     * @return Returns a String like "FIELD1 TEXT, FIELD2 TEXT, FIELD3 TEXT" (no leading or trailing comma)
     */
    public static String getDatabaseTableCreationFields() {
        StringBuffer buf = new StringBuffer();
        buf.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        for (int i = 1; i < FIELDS.length; i++) {
            buf.append(FIELDS[i]).append(" TEXT, ");
        }
        buf.delete(buf.length()-2, buf.length());
        return buf.toString();
    }

    public static boolean hasField(String columnName) {
        for (int i = 0; i < FIELDS.length; i++) {
            if (FIELDS[i].equals(columnName)) return true;
        }
        return false;
    }

    public StationDbItem populate(Cursor cursor) {
        try {
            String[] columnNames = cursor.getColumnNames();
            for (String columnName: columnNames) {
                if (StationItem.isLocalOnly(columnName)) continue;
                switch  (columnName) {
                    case ABV:
                        abv = cursor.getString(cursor.getColumnIndexOrThrow(ABV));
                        break;
                    case BEER_CODE:
                        beerCode = cursor.getString(cursor.getColumnIndexOrThrow(BEER_CODE));
                        break;
                    case BEER_NAME:
                        beerName = cursor.getString(cursor.getColumnIndexOrThrow(BEER_NAME));
                        break;
                    case BJCP_CATEGORY:
                        bjcpCategory = cursor.getString(cursor.getColumnIndexOrThrow(BJCP_CATEGORY));
                        break;
                    case BJCP_CODE:
                        bjcpCode = cursor.getString(cursor.getColumnIndexOrThrow(BJCP_CODE));
                        break;
                    case BREWER_NAME:
                        brewerName = cursor.getString(cursor.getColumnIndexOrThrow(BREWER_NAME));
                        break;
                    case EVENT_BEER_ID:
                        eventBeerId = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_BEER_ID));
                        break;
                    case EVENT_HASH:
                        eventHash = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_HASH));
                        break;
                    case STYLE_CODE:
                        styleCode = cursor.getString(cursor.getColumnIndexOrThrow(STYLE_CODE));
                        break;
                    case TENT_SPACE:
                        tentSpace = cursor.getString(cursor.getColumnIndexOrThrow(TENT_SPACE));
                        break;
                    case VENDOR_ABBR:
                        vendorAbbr = cursor.getString(cursor.getColumnIndexOrThrow(VENDOR_ABBR));
                        break;
                    case VENDOR_NAME:
                        vendorName = cursor.getString(cursor.getColumnIndexOrThrow(VENDOR_NAME));
                        break;
                    case VOTE_GROUP:
                        voteGroup = cursor.getString(cursor.getColumnIndexOrThrow(VOTE_GROUP));
                        break;
                    case ID:
                        _id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
                        break;
                    default:
                        Log.e(TAG, "Not sure what to do about " + columnName);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to complete model item from database cursor. " + t.getMessage())    ;
        }
        return this;
    }
    public ContentValues getContentValues(ContentValues values) {
        if (abv != null) values.put(ABV, abv);
        if (beerCode != null) values.put(BEER_CODE, beerCode);
        if (beerName != null) values.put(BEER_NAME, beerName);
        if (bjcpCategory != null) values.put(BJCP_CATEGORY, bjcpCategory);
        if (bjcpCode != null) values.put(BJCP_CODE, bjcpCode);
        if (brewerName != null) values.put(BREWER_NAME, brewerName);
        if (eventBeerId != null) values.put(EVENT_BEER_ID, eventBeerId);
        if (eventHash != null) values.put(EVENT_HASH, eventHash);
        if (styleCode != null) values.put(STYLE_CODE, styleCode);
        if (tentSpace != null) values.put(TENT_SPACE, tentSpace);
        if (vendorAbbr != null) values.put(VENDOR_ABBR, vendorAbbr);
        if (vendorName != null) values.put(VENDOR_NAME, vendorName);
        if (voteGroup != null) values.put(VOTE_GROUP, voteGroup);
        return values;
    }
    public static ContentValues fillModelValues(StationDbItem model, ContentValues values) {
        values.put(ABV, model.abv);
        values.put(BEER_CODE, model.beerCode);
        values.put(BEER_NAME, model.beerName);
        values.put(BJCP_CATEGORY, model.bjcpCategory);
        values.put(BJCP_CODE, model.bjcpCode);
        values.put(BREWER_NAME, model.brewerName);
        values.put(EVENT_BEER_ID, model.eventBeerId);
        values.put(EVENT_HASH, model.eventHash);
        values.put(STYLE_CODE, model.styleCode);
        values.put(TENT_SPACE, model.tentSpace);
        values.put(VENDOR_ABBR, model.vendorAbbr);
        values.put(VENDOR_NAME, model.vendorName);
        values.put(VOTE_GROUP, model.voteGroup);
        return values;
    }

    public void loadFromJson(String jsonString) {
        StringBuffer buf = new StringBuffer(jsonString);
        if (buf.substring(0,2).equals("[{")){
            buf.delete(0,2);
        }
        mRawInputString = buf.toString();
        parse();
    }

    public static String unescapeUnicode(String unescaped) {
        String escaped = unescaped;
        int uLoc = escaped.indexOf("\\u");
        while (uLoc > -1) {
            String replacement = "";
            try {
                String unicode = escaped.substring(uLoc + 2, uLoc+6);
                System.out.println("unicode [" + unicode + "]");
                replacement = new String(Character.toChars(Integer.parseInt(unicode, 16)));
            } catch (Throwable t) {
                //nevermind
            }
            escaped = escaped.substring(0, uLoc) + replacement + escaped.substring(uLoc+6);
            uLoc = escaped.indexOf("\\u");
        }
        return escaped;
    }

    public void parse() {
        if (mRawInputString == null) {
                System.out.println("nothing to parse");
                return;
        }
        //Every name and value are surrounded by quotes, except sometimes the word `null` is there, but without quotes.  This screws things up, so fix that.
        mRawInputString = mRawInputString.replaceAll("\"\\:null,", "\"\\:\"null\",");
        //There might be escaped quote marks `\"`.  Those can be replaced by single ticks
        mRawInputString = mRawInputString.replaceAll("\\\\\"", "'");
        String[] nvpa = mRawInputString.split("\",\"");
        for (String nvpString : nvpa) {
            String[] nvpItem = nvpString.split("\":\"");
            if (nvpItem.length < 2) continue;
            String identifier = nvpItem[0].replaceAll("\"", "");
            String content = nvpItem[1].replace("\\\"u", "u"); // "backslash quote u" will become "backslash u", so can't have that.
            content = content.replaceAll("\"", ""); // Remove quotes from within the content.  I don't know why we're doing this any more.
            content = unescapeUnicode(content);
            switch (identifier) {
                case ABV:
                    this.abv = content;
                    break;
                case BEER_CODE:
                    this.beerCode = content;
                    break;
                case BEER_NAME:
                    this.beerName = content;
                    break;
                case BJCP_CATEGORY:
                    this.bjcpCategory = content;
                    break;
                case BJCP_CODE:
                    this.bjcpCode = content;
                    break;
                case BREWER_NAME:
                    this.brewerName = content;
                    break;
                case EVENT_BEER_ID:
                    this.eventBeerId = content;
                    break;
                case EVENT_HASH:
                    this.eventHash = content;
                    break;
                case STYLE_CODE:
                    this.styleCode = content;
                    break;
                case TENT_SPACE:
                    this.tentSpace = content;
                    break;
                case VENDOR_ABBR:
                    this.vendorAbbr = content;
                    break;
                case VENDOR_NAME:
                    this.vendorName = content;
                    break;
                case VOTE_GROUP:
                    this.voteGroup = content;
                    break;
                default:
                    System.out.println("nowhere to put [" + nvpItem[0] + "] " + nvpString + " raw: " + mRawInputString);
                    break;
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(ID).append(": ").append(_id).append(", ");
        buf.append(ABV).append(": ").append(abv).append(",");
        buf.append(BEER_CODE).append(": ").append(beerCode).append(",");
        buf.append(BEER_NAME).append(": ").append(beerName).append(",");
        buf.append(BJCP_CATEGORY).append(": ").append(bjcpCategory).append(",");
        buf.append(BJCP_CODE).append(": ").append(bjcpCode).append(",");
        buf.append(BREWER_NAME).append(": ").append(brewerName).append(",");
        buf.append(EVENT_BEER_ID).append(": ").append(eventBeerId).append(",");
        buf.append(EVENT_HASH).append(": ").append(eventHash).append(",");
        buf.append(STYLE_CODE).append(": ").append(styleCode).append(",");
        buf.append(TENT_SPACE).append(": ").append(tentSpace).append(",");
        buf.append(VENDOR_ABBR).append(": ").append(vendorAbbr).append(",");
        buf.append(VENDOR_NAME).append(": ").append(vendorName).append(",");
        buf.append(VOTE_GROUP).append(": ").append(voteGroup).append(",");
        return buf.toString();
    }

    ///////////////////////////////////////////////////////////////
    //Convenience Methods
    ///////////////////////////////////////////////////////////////

    public String getId() {
        return "" + _id;
    }

    ///////////////////////////////////////////////////////////////
    //Gets and Sets (generated)
    ///////////////////////////////////////////////////////////////

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getAbv() {
        String formattedAbv = abv;
        while (formattedAbv.endsWith("0") || formattedAbv.endsWith(".")) {
            formattedAbv = formattedAbv.substring(0, formattedAbv.length()-1);
        }
        return formattedAbv + "%";
    }

    public void setAbv(String abv) {
        this.abv = abv;
    }

    public String getBeerCode() {
        return beerCode;
    }

    public void setBeerCode(String beerCode) {
        this.beerCode = beerCode;
    }

    public String getBeerName() {
        return beerName;
    }

    public void setBeerName(String beerName) {
        this.beerName = beerName;
    }

    public String getBjcpCategory() {
        return bjcpCategory;
    }

    public void setBjcpCategory(String bjcpCategory) {
        this.bjcpCategory = bjcpCategory;
    }

    public String getBjcpCode() {
        return bjcpCode;
    }

    public void setBjcpCode(String bjcpCode) {
        this.bjcpCode = bjcpCode;
    }

    public String getBrewerName() {
        return brewerName;
    }

    public void setBrewerName(String brewerName) {
        this.brewerName = brewerName;
    }

    public String getEventBeerId() {
        return eventBeerId;
    }

    public void setEventBeerId(String eventBeerId) {
        this.eventBeerId = eventBeerId;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }

    public String getStyleCode() {
        return styleCode;
    }

    public void setStyleCode(String styleCode) {
        this.styleCode = styleCode;
    }

    public String getTentSpace() {
        return "Tent " + tentSpace;
    }

    public void setTentSpace(String tentSpace) {
        this.tentSpace = tentSpace;
    }

    public String getVendorAbbr() {
        return vendorAbbr;
    }

    public void setVendorAbbr(String vendorAbbr) {
        this.vendorAbbr = vendorAbbr;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVoteGroup() {
        return voteGroup;
    }

    public void setVoteGroup(String voteGroup) {
        this.voteGroup = voteGroup;
    }


}
