package com.sengsational.ratestation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Owner on 5/13/2016.
 */
public class RatstatDatabaseAdapter {
    private static final String TAG = RatstatDatabaseAdapter.class.getSimpleName();
    public static final String DB_NAME = "RS_DB_A";
    public static final String MAIN_TABLE = "RS_ITEMS";
    private static final int DB_VERSION = 3;
    private static DatabaseHelper DB_HELPER;
    private static SQLiteDatabase SQL_DB;
    private static Cursor SQL_CURSOR;
    private static final HashSet<Integer> POSITION_SET = new HashSet<>();
    private static RatstatDatabaseAdapter RS_DB_ADAPTER;
    private static String[] lastPullFields = {};

    public RatstatDatabaseAdapter() {
        this.RS_DB_ADAPTER = this;
    }

    public static RatstatDatabaseAdapter open(Context context) throws SQLException {
        DB_HELPER = DatabaseHelper.getInstance(context);
        SQL_DB = DB_HELPER.getWritableDatabase();
        return RS_DB_ADAPTER;
    }

    public SQLiteDatabase openDb(Context context) throws SQLException {
        Log.v(TAG, "openDb()");
        DB_HELPER = DatabaseHelper.getInstance(context);
        SQL_DB = DB_HELPER.getWritableDatabase();
        return SQL_DB;
    }

    public static void close() {
        Log.v(TAG, "close() being called.  Closing DB_HELPER.");
        /*
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < 6; i++){
            Log.v(TAG, stackTraceElements[i].getClassName() + ": " + stackTraceElements[i].getLineNumber());
        }
        */
        if (DB_HELPER != null) {
            DB_HELPER.close();
        }
    }

    public Cursor query(String table, String[] pullFields, String selectionFields, String[] selectionArgs) {
        if (SQL_DB != null)  {
            lastPullFields = pullFields;
            return SQL_DB.query(table, pullFields, selectionFields, selectionArgs, null, null, null);
        }
        Log.v(TAG, "Tried to query without database being available.");
        return null;
    }

    public Cursor query(String table, String[] pullFields, String selectionFields, String[] selectionArgs, Object o, Object o1, String sortParameter) {
        if (SQL_DB != null) {
            lastPullFields = pullFields;
            return SQL_DB.query(table, pullFields, selectionFields, selectionArgs, null, null, sortParameter);
        }
        Log.v(TAG, "Tried to query without database being available.");
        return null;
    }


    public static void update(StationItem model, int position, Context context) {
        Log.v(TAG, "update(model, position) is adding " + position + " to the POSITION_SET.");
        POSITION_SET.add(position);
        update(model, context);
    }

    public static void update(StationItem model, Context context) {
        ContentValues values = model.fillModelValues(model);
        Log.v(TAG, "Working on model that looks like this: " + model);
        Log.v(TAG, "Updating record " + StationDbItem.ID + "=" + model.getId() + " in the database.");
        if (!SQL_DB.isOpen()) openDatabase(context);
        int ok = SQL_DB.update(MAIN_TABLE, values, StationDbItem.ID + "=?", new String[] {"" + model.getId()});
        StationItem resultInDb = getById("" + model.getId());
        Log.v(TAG, "after update, resultInDb: " + resultInDb);
    }

    private static void openDatabase(Context context) {
        try {
            DB_HELPER = DatabaseHelper.getInstance(context);
            SQL_DB = DB_HELPER.getWritableDatabase();
        } catch (Throwable t) {
            Log.e(TAG, "Unable fix a closed database problem.");
        }
    }

    public static Integer[] getChangedPositions() {
        Integer[] positions = POSITION_SET.toArray(new Integer[0]);
        POSITION_SET.clear();
        return positions;
    }

    static StationItem getById(String id) {
        Cursor cursor = SQL_DB.query("RS_ITEMS", null, "_id=?", new String[]{id}, null, null, null);
        if (cursor != null) {
            lastPullFields = StationItem.FIELDS_ALL;
            cursor.moveToFirst();
        }
        StationItem model = new StationItem(cursor);
        cursor.close();
        return model;
    }

    public ArrayList<Cursor> getData(String Query, Context context){
        //get writable database
        SQLiteDatabase sqlDB = DatabaseHelper.getInstance(context).getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

    public static Cursor fetch(String queryType, Context context) {
        Log.v(TAG, "fetch with queryType "  + queryType);
        String[] pullFields =  QueryPkg.getPullFields(context);

        ArrayList<String> selectionArgsArray = new ArrayList<String>();
        StringBuffer localSelectionFieldsBuf = new StringBuffer();
        String localSelectionFields = null;
        String[] localSelectionArgs = selectionArgsArray.toArray(new String[0]);
        String orderByFieldName = "";

        if ("queryPackage".equals(queryType)) {
            String[] selectionArgs = QueryPkg.getSelectionArgs(context);
            for (String arg: selectionArgs) {
                Log.v(TAG, "arg [" + arg + "]");
            }
            Log.v(TAG, "fields " + QueryPkg.getSelectionFields(context));
            Log.v(TAG, "---------------------");
            selectionArgsArray.addAll(Arrays.asList(selectionArgs));
            localSelectionFieldsBuf = new StringBuffer(QueryPkg.getSelectionFields(context));

            String fullTextSearch = QueryPkg.getFullTextSearch(context);
            Log.v(TAG, "fullTextSearch [" + fullTextSearch + "]");
            if (!"".equals(fullTextSearch)) {
                if (selectionArgs.length == 0) {
                    localSelectionFieldsBuf.append(" (");
                } else {
                    localSelectionFieldsBuf.append(" AND (");
                }
                localSelectionFieldsBuf.append(StationItem.SEARCH_CONCAT);
                localSelectionFieldsBuf.append(" LIKE ?");
                localSelectionFieldsBuf.append(")");
                selectionArgsArray.add("%" + fullTextSearch + "%");
                QueryPkg.setFullTextSearch("", context);
            }

            if (localSelectionFieldsBuf.length() != 0) localSelectionFields = localSelectionFieldsBuf.toString();
            localSelectionArgs = selectionArgsArray.toArray(new String[0]);
            if (localSelectionArgs.length == 0) localSelectionArgs = null;

            orderByFieldName = QueryPkg.getOrderByFieldName(context);

        } else {
            // select all records

            // order by vote, stars, random
            orderByFieldName = StationItem.USER_VOTE + " DESC, " + StationItem.USER_STARS + " DESC, " + StationItem.USER_REVIEW + " DESC, " + StationItem.RANDOM_SORT;
            Log.v(TAG, "Setting order by as vote, stars, random");
        }

        if (selectionArgsArray.size() == 0) {
            localSelectionArgs = null;
        } else {
            Log.v(TAG, "selectionArgsArray.size() " + selectionArgsArray.size());
        }


        if(SQL_CURSOR != null) {
            Log.v(TAG, "The cursor was not null!");
            try {
                Log.v(TAG, "closing the database now now.");
                SQL_CURSOR.close();
            } catch (Throwable t){}
        }
        if (SQL_DB == null) {
            if (RS_DB_ADAPTER == null) {
                new RatstatDatabaseAdapter();
            }
            SQL_DB = RS_DB_ADAPTER.openDb(context);
        }
        if (!SQL_DB.isOpen()) openDatabase(context);
        try {
            Log.v("sengsational", "pullFields: " + Arrays.toString(QueryPkg.getPullFields(context)));
            Log.v("sengsational", "localSelectionFields: " + localSelectionFields);
            Log.v("sengsational", "localSelectionArgs: " + Arrays.toString(localSelectionArgs));
            Log.v("sengsational", "orderBy: " + orderByFieldName);

            SQL_CURSOR = SQL_DB.query("RS_ITEMS", pullFields, localSelectionFields, localSelectionArgs, null, null, orderByFieldName);
            //SQL_CURSOR = SQL_DB.query("RS_ITEMS", pullFields, null, null, null, null, orderByFieldName);
        } catch (Throwable t) {
           t.printStackTrace();
        }

        boolean hasRecords = SQL_CURSOR.moveToFirst();
        lastPullFields = pullFields;
        Log.v("sengsational", "RatstatDatabaseAdapter.fetch() cursor created. " + SQL_CURSOR + " and " + (hasRecords?"has records":"has NO RECORDS"));

        return SQL_CURSOR;
    }

    public static int getOffsetForFieldname(String fieldName) {
        for (int i = 0; i < lastPullFields.length; i++) {
            if (lastPullFields[i].equals(fieldName)) return i;
        }
        return 0;
    }

    /**
     * This copies data from the menu scanning table to the main table.  Called when menu scanning is closed.
     *
     */

    public static String getUserInputJson(Context context) {
        if(SQL_CURSOR != null) {
            Log.v(TAG, "The cursor was not null!");
            try {
                Log.v(TAG, "closing the database now now.");
                SQL_CURSOR.close();
            } catch (Throwable t){}
        }
        if (SQL_DB == null) {
            if (RS_DB_ADAPTER == null) {
                new RatstatDatabaseAdapter();
            }
            SQL_DB = RS_DB_ADAPTER.openDb(context);
        }
        if (!SQL_DB.isOpen()) openDatabase(context);

        StringBuffer selectionFieldsBuf = new StringBuffer();
        ArrayList<String> selectionArgsArrayList = new ArrayList<String>();

        // If the record has a vote
        selectionFieldsBuf.append(StationItem.USER_VOTE).append(" = ? OR ");
        selectionArgsArrayList.add("T");
        // or if the record has stars
        selectionFieldsBuf.append(StationItem.USER_STARS).append(" != ? OR ");
        selectionArgsArrayList.add("0.0");
        // or if the record has a review
        selectionFieldsBuf.append(StationItem.USER_REVIEW).append(" != ?");
        selectionArgsArrayList.add("");

        try {
            SQL_CURSOR = SQL_DB.query("RS_ITEMS", QueryPkg.getPullFields(context), selectionFieldsBuf.toString(), selectionArgsArrayList.toArray(new String[0]), null, null, null);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        boolean hasRecords = SQL_CURSOR.moveToFirst();
        SQL_CURSOR.moveToPrevious();
        Log.v("sengsational", "RatstatDatabaseAdapter.fetch() cursor created. " + SQL_CURSOR + " and " + (hasRecords?"has records":"has NO RECORDS"));
        if (!hasRecords) return "ERROR: No votes or ratings found.";

        StringBuffer jsonBuf = new StringBuffer("[");
        while(SQL_CURSOR.moveToNext()) {
            StationItem item = new StationItem(SQL_CURSOR);
            jsonBuf.append(item.getUserDataJson(context)).append(",");
            //Log.v(TAG,"stars:" + item.getUserStars() + " vote: " + item.hasVote() + " review: " + item.getUserReview());
        }
        jsonBuf.setLength(jsonBuf.length()-1); // trailing comma
        jsonBuf.append("]");
        return jsonBuf.toString();
    }

    public static int countVotedItems(Context context) {
        SQLiteDatabase db = new RatstatDatabaseAdapter().openDb(context);
        SQLiteStatement countTapItems =  db.compileStatement("SELECT COUNT(*) FROM RS_ITEMS WHERE " + StationItem.USER_VOTE + "='T'");
        int count = (int)countTapItems.simpleQueryForLong();
        db.close();
        return count;
    }

    public static int countMenuItems(String storeNumber, Context context) {
        SQLiteDatabase db = new RatstatDatabaseAdapter().openDb(context);
        SQLiteStatement countMenuItems =  db.compileStatement("SELECT COUNT(*) FROM RS_ITEMS WHERE ACTIVE='T' AND CONTAINER='draught' AND STYLE<>'Mix' AND STYLE<>'Flight' AND STORE_ID='" + storeNumber + "' AND (GLASS_SIZE IS NOT NULL OR GLASS_PRICE IS NOT NULL)");
        int count = (int)countMenuItems.simpleQueryForLong();
        db.close();
        return count;
    }

    public static float fractionTapsWithMenuData(String storeNumber, Context context) {
        return ((float)countMenuItems(storeNumber, context))/((float) countVotedItems(context));
    }

    public boolean cursorHasRecords() {
        if (SQL_CURSOR == null) return false;
        return SQL_CURSOR.getCount() > 0;
    }

    public static Cursor getCursor() {
        if (SQL_CURSOR != null) return SQL_CURSOR;
        else return SQL_DB.query("RS_ITEMS", null, null, null, null, null, null);
    }

    // TEMPORARY METHOD
    public static String[] getColumnNames(Context context) {
        SQLiteDatabase glassDb = new RatstatDatabaseAdapter().openDb(context);
        Cursor cursor = glassDb.rawQuery("SELECT * FROM RS_ITEMS LIMIT 1", null);
        String[] names = cursor.getColumnNames();
        for(String name: names) {
            Log.v(TAG, name);
        }
        Log.v(TAG, "------------");
        cursor.close();
        glassDb.close();
        return names;
    }

    // INNER CLASS DatabaseHelper
    static class DatabaseHelper extends SQLiteOpenHelper {
        private static DatabaseHelper mDatabaseHelper;

        // SINGLETON PATTERN
        private DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            Log.v(TAG, "constructor with DB_NAME " + DB_NAME + " and DB_VERSION " + DB_VERSION);
            mDatabaseHelper = this;
        }

        static synchronized DatabaseHelper getInstance(Context context) {
            if (mDatabaseHelper == null) {
                mDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
            }
            return mDatabaseHelper;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "onCreate()");
            db.execSQL(StationItem.getDatabaseTableCreationCommand());
            Log.v(TAG, "onCreate() - Table created under version " + DB_VERSION + "\n" + StationItem.getDatabaseTableCreationCommand() );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN " + StationItem.USER_REVIEW + " TEXT");
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN " + StationItem.USER_STARS + " TEXT");
                case 2:
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN " + StationItem.USER_VOTE + " TEXT");
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN " + StationItem.RANDOM_SORT + " TEXT");
                case 3:
                /*
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN NEW_ARRIVAL TEXT");
                    db.execSQL("ALTER TABLE RS_ITEMS ADD COLUMN IS_IMPORT TEXT");
                    db.execSQL(StationItem.getDatabaseAppendTableCreationCommand());
                 */
            }

            Log.v(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion + " complete.");
        }

        @Override
        public synchronized void close() {
            //Log.v(TAG, "close database requested.");
            //StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            //Log.v(TAG, trace[2].getClassName() + "," + trace[2].getMethodName() + " (" + trace[2].getLineNumber() + ")");
            //Log.v(TAG, trace[3].getClassName() + "," + trace[3].getMethodName() + " (" + trace[3].getLineNumber() + ")");
            //Log.v(TAG, trace[4].getClassName() + "," + trace[4].getMethodName() + " (" + trace[4].getLineNumber() + ")");
            //Log.v(TAG, trace[5].getClassName() + "," + trace[5].getMethodName() + " (" + trace[5].getLineNumber() + ")");
            super.close();
        }

        public static int getCount(SQLiteDatabase db, String tableName){
            Cursor cursor = db.query(tableName, new String[]{"COUNT(_id) AS count"}, null, null, null, null, null);
            int count = -1;
            if (cursor.moveToFirst()){
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;
        }

        public static boolean fieldExists(SQLiteDatabase db, String tableName, String fieldName) {
            boolean isExisting = false;
            Cursor res = null;
            try {
                res = db.rawQuery("Select * from "+ tableName +" limit 1", null);
                int colIndex = res.getColumnIndex(fieldName);
                if (colIndex!=-1) isExisting = true;
            } catch (Exception e) {
                Log.v(TAG,"Failed to ascertain if " + fieldName + " existed in " + tableName + " with error " + e.getClass().getName() + " " + e.getMessage());
            } finally {
                try { if (res !=null){ res.close(); } } catch (Exception e1) {}
            }
            return isExisting;
        }
    }
}
