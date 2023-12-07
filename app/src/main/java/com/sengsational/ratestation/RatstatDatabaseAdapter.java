package com.sengsational.ratestation;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;
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
    private static final int DB_VERSION = 2;
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

    public void close() {
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
        Log.v(TAG, "update(model, position) is adding to the POSITION_SET.");
        POSITION_SET.add(position);
        update(model, context);
    }

    static void update(StationItem model, Context context) {
        ContentValues values = model.fillModelValues(model);
        Log.v(TAG, "Working on model that looks like this: " + model);
        Log.v(TAG, "Updating record " + StationDbItem.ID + "=" + model.getId() + " in the database.");
        if (!SQL_DB.isOpen()) openDatabase(context);
        SQL_DB.update(MAIN_TABLE, values, StationDbItem.ID + "=?", new String[] {"" + model.getId()});
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

    public static Cursor fetch(Context context) {

        ArrayList<String> selectionArgsArray = new ArrayList<String>();
        selectionArgsArray.addAll(Arrays.asList(QueryPkg.getSelectionArgs(context)));

        StringBuffer localSelectionFields = new StringBuffer(QueryPkg.getSelectionFields(context));

        String fullTextSearch = QueryPkg.getFullTextSearch(context);
        Log.v(TAG, "fullTextSearch [" + fullTextSearch + "]");
        if (!"".equals(fullTextSearch)) {
            localSelectionFields.append(" AND (");
                localSelectionFields.append(StationItem.SEARCH_CONCAT); localSelectionFields.append(" LIKE ?");
            localSelectionFields.append(")");
            selectionArgsArray.add("%" + fullTextSearch + "%");
            QueryPkg.setFullTextSearch("", context);
        }

        //QueryPkg.selectionArgs = selectionArgsArray.toArray(new String[0]);
        String[] localSelectionArgs = selectionArgsArray.toArray(new String[0]);
        Log.v("sengsational", "selectionArgs: " + Arrays.toString(localSelectionArgs));
        Log.v("sengsational", "selectionFields: " + localSelectionFields);
        Log.v("sengsational", "pullFields: " + Arrays.toString(QueryPkg.getPullFields(context)));
        Log.v("sengsational", "orderBy: " + QueryPkg.getOrderBy(context));

        String orderByDirective = QueryPkg.getOrderBy(context);
        //Log.v(TAG, "Second order by is not being used.");
        //orderByDirective += " " + QueryPkg.getSecondOrderBy(context);

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
        boolean pullOk = false;
        try {
            SQL_CURSOR = SQL_DB.query("RS_ITEMS", QueryPkg.getPullFields(context), localSelectionFields.toString(), localSelectionArgs, null, null, orderByDirective);
            pullOk = true;
        } catch (Throwable t) {
           t.printStackTrace();
        }

        // Not sure why some databases don't contain some fields.
        String[] pullFields =  QueryPkg.getPullFields(context);  // seems to occasionally have some field not in the table, like GLASS_SIZE.
        if (!pullOk) {
            String[] columnNames = SQL_DB.query("RS_ITEMS", null, null, null, null, null, null).getColumnNames();
            for(String columnName: columnNames) {
                boolean columnFound = false;
                for (String field: pullFields ) {
                    if (field.equals(columnName)) {
                        columnFound = true;
                        continue;
                    }
                }
                if (!columnFound) {
                    Log.v(TAG, "the column " + columnName + "was not found in the table RS_ITEMS.");

                }
            }

        }

        boolean hasRecords = SQL_CURSOR.moveToFirst();
        lastPullFields = pullFields;
        Log.v("sengsational", "RatstatDatabaseAdapter.fetch() cursor created. " + SQL_CURSOR + " and " + (hasRecords?"has records":"has NO RECORDS"));

        return SQL_CURSOR;
    }

    public int getOffsetForFieldname(String fieldName) {
        for (int i = 0; i < lastPullFields.length; i++) {
            if (lastPullFields[i].equals(fieldName)) return i;
        }
        return 0;
    }

    /**
     * This copies data from the menu scanning table to the main table.  Called when menu scanning is closed.
     *
     */

    public static void copyMenuData(Context context) {
        SQLiteDatabase glassDb = new RatstatDatabaseAdapter().openDb(context);
        Cursor cursor = glassDb.query("RS_ITEMSLOCAL", null,null, null, null, null, null);
        boolean hasRecords =cursor.moveToFirst();
        if (hasRecords) {
            RatstatDatabaseAdapter repository = new RatstatDatabaseAdapter();
            SQLiteDatabase db = repository.openDb(context);

            // Loop through all records in RS_ITEMSLOCAL table (gathered from Untappd) and add to main RS_ITEMS table
            int updated = 0;
            int updatedCount = 0;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                // Inputs to find record in the main RS_ITEMS table
                String storeId = cursor.getString(cursor.getColumnIndexOrThrow("STORE_ID"));
                String brewId = cursor.getString(cursor.getColumnIndexOrThrow("BREW_ID"));

                // Data to add to the RS_ITEMS table
                String glassSize = cursor.getString(cursor.getColumnIndexOrThrow("GLASS_SIZE"));
                String glassPrice = cursor.getString(cursor.getColumnIndexOrThrow("GLASS_PRICE"));
                String abv = cursor.getString(cursor.getColumnIndexOrThrow("ABV"));
                String beerNumber = cursor.getString(cursor.getColumnIndexOrThrow("UNTAPPD_BEER"));
                String breweryNumber = cursor.getString(cursor.getColumnIndexOrThrow("UNTAPPD_BREWERY"));

                ContentValues values = new ContentValues();
                values.put("GLASS_SIZE", glassSize);
                values.put("GLASS_PRICE", glassPrice);
                values.put("UNTAPPD_BEER", beerNumber);
                values.put("UNTAPPD_BREWERY", breweryNumber);
                updated = db.update("RS_ITEMS", values, "STORE_ID=? AND BREW_ID=?", new String[]{storeId, brewId});
                if (updated == 0) {
                    // This is OK because if the beer is not active, it does not go away...it stays in there 'forever'.  That way, if it comes back, the user will not need to scan it again.
                } else if(updated !=1) {
                    Log.e(TAG, "Expected to update 1 record, but updated " + updated + " records. name [" + cursor.getString(cursor.getColumnIndexOrThrow("NAME")) + "]");
                } else {
                    updatedCount += updated;
                }

                /* Check to see if ABV is empty and can be updated DRS 20220726 */
                String[] pullFields = new String[]{"ABV"}; //was: StationItem.ABV
                String selectionFields = "STORE_ID=? AND BREW_ID=?";
                String[] selectionArgs = new String[]{storeId, brewId};

                Cursor mainTableCursor = db.query("RS_ITEMS", pullFields, selectionFields, selectionArgs, null, null, null);
                lastPullFields = pullFields;
                if (mainTableCursor != null) {
                    for (mainTableCursor.moveToFirst(); !mainTableCursor.isAfterLast(); mainTableCursor.moveToNext()) {
                        String abvFromMain = mainTableCursor.getString(0);
                        //Log.v(TAG, "abvFromMain was " + abvFromMain + " and abv from Untappd was " + abv + " on " + brewId);
                        if ("0".equals(abvFromMain) && abv != null && !"".equals(abv)) {
                            values = new ContentValues();
                            values.put("ABV", abv);
                            updated = db.update("RS_ITEMS", values, "STORE_ID=? AND BREW_ID=?", new String[]{storeId, brewId});
                            if (!(updated == 1)) Log.v(TAG, "Unable to update single brew_id with ABV. " + updated + " records updated.");
                        }
                    }
                }
            }
            repository.close();
            db.close();
            Log.v(TAG, "There were " + updatedCount + " records updated with glass size, glass price, untapped beer number and brewery number.");
        } else {
            Log.v(TAG, "The cursor had no records in copyMenuData() method.");
        }
        cursor.close();
        glassDb.close();
    }
    public static int countTapItems(String storeNumber, Context context) {
        SQLiteDatabase db = new RatstatDatabaseAdapter().openDb(context);
        SQLiteStatement countTapItems =  db.compileStatement("SELECT COUNT(*) FROM RS_ITEMS WHERE ACTIVE='T' AND CONTAINER='draught' AND STYLE<>'Mix' AND STYLE<>'Flight' AND STORE_ID='" + storeNumber + "'");
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
        return ((float)countMenuItems(storeNumber, context))/((float)countTapItems(storeNumber, context));
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
