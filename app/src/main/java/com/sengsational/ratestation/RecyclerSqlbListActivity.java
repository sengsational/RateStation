package com.sengsational.ratestation;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RecyclerSqlbListActivity extends AppCompatActivity {
    private static final String TAG = RecyclerSqlbListActivity.class.getSimpleName();

    // Callback related
    public static final int DETAIL_REQUEST = 0;
    public MybCursorRecyclerViewAdapter cursorRecyclerViewAdapter;
    private String mQueryTextContent = "";
    private String mQueryButtonText;
    private String mLastSortFieldName;
    private String mLastSortAscendingDescending;
    private boolean mSingleItemFired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "RecyclerSqlbListActivity.onCreate() running.");
        setContentView(R.layout.activity_recycler_list);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            RatstatDatabaseAdapter.open(this);

            Log.v(TAG, "RecyclerSqlbListActivity.onCreate() is pulling records from the database.");

            // This intent carries with it the details of how to query
            Intent intent = getIntent();

            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                Log.v("sengsational", "THIS NEVER RUNS - onCreate() ACTION_SEARCH!!! " + query);
            } else {
                String selectionFields = intent.getStringExtra("selectionFields");
                String[] selectionArgs = intent.getStringArrayExtra("selectionArgs");
                final String[] pullFields = intent.getStringArrayExtra("pullFields");
                //Log.v(TAG, "pullFields in RecyclerSqlbListActivity " + pullFields);
                final String orderBy = intent.getStringExtra("orderBy");
                boolean showDateInList = intent.getBooleanExtra("showDateInList", false);
                boolean hideMixesAndFlights = intent.getBooleanExtra("hideMixesAndFlights", true);
                mQueryButtonText = intent.getStringExtra("queryButtonText");
                mQueryTextContent = intent.getStringExtra("queryTextContent"); // DRS 20171021 - Will be null the first time, but on screen reorientation will have previous query
                if (mQueryTextContent == null) mQueryTextContent = "";
                for (int i = 0 ; i < selectionArgs.length; i++){
                    Log.v(TAG, ">>>>>> Selection Args " + selectionArgs[i]);
                }

                QueryPkg usedInFetch = new QueryPkg(RatstatDatabaseAdapter.MAIN_TABLE, pullFields, selectionFields, selectionArgs, null, "T", null, false, null, this);
                if (pullFields != null) {
                    Log.v(TAG, "RecyclerSqlbListActivity.Creating a cursor with 'fetch'");
                    Cursor aCursor = RatstatDatabaseAdapter.fetch("queryPackage", this);
                    RatstatApplication.setCursor(aCursor); // DRS 20161201 - Added 1 - Cursors only in application class, save query package for reQuery
                    boolean hasRecords = aCursor.moveToFirst();
                    if (aCursor.getCount() != 0) {
                        Log.v(TAG, "RecyclerSqlbListActivity.onCreate() running.  Cursor " + (hasRecords?"has records":"has NO RECORDS"));
                        cursorRecyclerViewAdapter = new MybCursorRecyclerViewAdapter(this, aCursor, showDateInList, QueryPkg.includesSelection("HIGHLIGHTED", this));
                        cursorRecyclerViewAdapter.hasStableIds();
                        recyclerView.setAdapter(cursorRecyclerViewAdapter);
                        recyclerView.hasFixedSize();
                    } else {
                        Log.v(TAG, "RecyclerSqlbListActivity.onCreate() running.  Cursor " + aCursor.getCount() + " records.");
                        manageToasts();
                    }
                } else {
                    Toast toast = Toast.makeText(this, "No Selection", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        } catch (Exception e) {
            Log.v(TAG, "Exception in onCreate " + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.v("sengsational", "RecyclerSqlbListActivity.onActivityResult running with requestCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == RecyclerSqlbListActivity.DETAIL_REQUEST) {
            Log.v(TAG, "onActivityResult fired <<<<<<<<<< resultCode:" + resultCode + " CLOSING CURSOR WILL PROBABLY BREAK STUFF");
            // CLOSING CURSOR

            RatstatApplication.getCursor("queryPackage", getApplicationContext()).close();
            cursorRecyclerViewAdapter.changeCursor(RatstatApplication.getCursor("queryPackage", getApplicationContext()));  // DRS 20161201 - Added 1 - Cursors only in application class
        } else {
            String intentName = "not available";
            if (intent != null) {
                intentName = intent.getClass().getName();
            }
            Log.v(TAG, "onActivityresult with " + requestCode + ", " + resultCode + ", " + intentName);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v("sengsational", "THIS DOES NOT RUN - onNewIntent() ");
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v("sengsational", "onNewIntent() ACTION_SEARCH!!! " + query);
        } else {
            Log.v("sengsational", "onNewIntent() some other intent");
        }
    }
    // DRS20171019 - Added method
    @Override
    public boolean onSearchRequested() {
        Log.v("sengsational", "THIS DOES NOT RUN  - onSearchRequested");
        return super.onSearchRequested();
    }

    private void manageToasts() {
        try {
            Toast toast = Toast.makeText(this, "The list was empty.......", Toast.LENGTH_SHORT);
            toast.show();
            if(QueryPkg.includesSelection("HIGHLIGHTED", this)){
                AlertDialog.Builder emptyListDialog = new AlertDialog.Builder(this);
                emptyListDialog.setMessage("There are currently no beers flagged.  But if you want to flag a beer, get into the beer's details and long-press the description.  You'll see a marker appear.");
                emptyListDialog.setCancelable(true);
                emptyListDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                });
                emptyListDialog.create().show();
            }
            if(QueryPkg.includesSelection("NEW_ARRIVAL", this)){
                AlertDialog.Builder emptyListDialog = new AlertDialog.Builder(this);
                emptyListDialog.setMessage("You may need to use the 'Refresh Active' function that's using the menu that is activated by clicking the three dots in the upper right corner of the screen.");
                emptyListDialog.setCancelable(true);
                emptyListDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                });
                emptyListDialog.create().show();
            }
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database Unavailable", Toast.LENGTH_SHORT);
            toast.show();
        } finally {
            Log.v("sengsational","Not closing cursor.  Will do in onDestroy().");
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("sengsational", "Closing cursor in onDestroy().");
        RatstatApplication.closeCursor();
        RatstatDatabaseAdapter.close();
    }

    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent(RecyclerSqlbListActivity.this, BeerSlideActivity.class);
        //listView.getFirstVisiblePosition();
        Log.v("sengsational", "BeerListActivity.onListItemClick() position:" + position + " id:" + id);

        intent.putExtra(BeerSlideActivity.EXTRA_POSITION, (int) position);
        //intent.putExtra(BeerSlideActivity.EXTRA_FIRST_VISIBLE_POSITION, (int) listView.getFirstVisiblePosition());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("sengsational", "onResume() running  <<<<<<<<<<<<<<<<<<<<<< LIST DONE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // If there is a single item, open it, but only the first time.
        if (this.cursorRecyclerViewAdapter != null && this.cursorRecyclerViewAdapter.getCursor().getCount() == 1 && !mSingleItemFired) {
            Log.v(TAG, "Automatically 'clicking' the only item in the list.");
            mSingleItemFired = true;
            Intent intent = new Intent(RecyclerSqlbListActivity.this, BeerSlideActivity.class);
            intent.putExtra(BeerSlideActivity.EXTRA_POSITION, 1);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("sengsational", "onPause() running");
        if (!"".equals(mQueryTextContent)) getIntent().putExtra("queryTextContent", mQueryTextContent); // DRS 20171021 - keep text query from getting lost on screen reorientation
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        // Here we try to limit showing the sort by price and glass size to times when it's appropriate
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RecyclerSqlbListActivity.this);
        String hideTitle1 = "";
        String hideTitle2 = "";
        String hideTitle3 = "";

        for (int i = 0; i < menu.size(); i++){
            MenuItem anItem = menu.getItem(i);
            if (hideTitle1.equals(anItem.getTitle()) || hideTitle2.equals(anItem.getTitle()) || hideTitle3.equals(anItem.getTitle())) {
                menu.getItem(i).setVisible(false);
            } else {
                menu.getItem(i).setVisible(true);
            }
        }

        //DRS20171019 ADDED all to end of method - add search widget per https://developer.android.com/guide/topics/search/search-dialog.html#UsingSearchWidget
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        Log.v("sengsational", "searchManager.toString(): " + searchManager);
        MenuItem item = menu.findItem(R.id.menu_search);
        Log.v("sengsational", "item.toString(): " + item);


        //SearchView searchView = (SearchView) item.getActionView();
        //Log.v("sengsational", "searchView.toString(): " + searchView); // <<<<<<<<<searchView is null
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(true);
        searchView.setIconifiedByDefault(true);
        MenuItemCompat.setActionView(item, searchView);
        String searchHint = "Search...";
        if (mQueryButtonText != null && mQueryButtonText.length() > 5) {
            searchHint = "Search" + mQueryButtonText.substring(4); // remove "List" and replace with "Search"
        }
        searchView.setQueryHint(searchHint);

        final Context lContext = this;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.v(TAG, "onQueryTextSubmit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.v(TAG, "onQueryTextChange [" + newText + "]");
                mQueryTextContent = newText;
                QueryPkg.setFullTextSearch(newText, lContext);
                Cursor aCursor = RatstatDatabaseAdapter.fetch("queryPackage", getApplicationContext());
                RatstatApplication.setCursor(aCursor);
                if (cursorRecyclerViewAdapter != null) {
                    cursorRecyclerViewAdapter.setQueryText(newText);
                    cursorRecyclerViewAdapter.changeCursor(aCursor);
                }

                return false;
            }
        });

        // Assumes current activity is the searchable activity
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        Log.v("sengsational", "searchView isIconified " + searchView.isIconified());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.v("sengsational","The item selected: " + item.getTitle() + " id:" + id);
        if (id == R.id.action_sort_abv) {
            manageSort(StationDbItem.ABV, true);
        } else if (id == R.id.action_sort_name) {
            manageSort(StationDbItem.BEER_NAME, false);
        } else if (id == R.id.action_sort_style) {
            manageSort(StationDbItem.STYLE_CODE, false);
        } else {
            Log.v("sengsational", "the id: " + id);
        }
        return super.onOptionsItemSelected(item);
    }

    private void manageSearch(int name) {
        Toast.makeText(this, "Not implemented yet.",Toast.LENGTH_SHORT);
    }

    private void manageSort(String fieldName, boolean castInteger) {
        Log.v(TAG, "manageSort() with " + fieldName);
        String ascendingDescending = "ASC";
        // override the ascending descending passed in if
        // two sorts of the same thing result in swapping asc to desc or vica versa
        // if the same sort field comes through as last time...
        Log.v(TAG, "The field being sorted..." + mLastSortFieldName + " " + fieldName);
        if (fieldName.equals(mLastSortFieldName)){
            // make the sort opposite of last time
            if (mLastSortAscendingDescending.equals("ASC")) {
                ascendingDescending = "DESC";
            } else {
                ascendingDescending = "ASC";
            }
        }

        // set up the SQL, honoring the the castInteger
        String sortParameter = fieldName + " " + ascendingDescending;
        if (castInteger) {
            sortParameter = "CAST (" +  fieldName + " AS FLOAT) " + ascendingDescending;
        }

        QueryPkg.setOrderBy(sortParameter, this);

        if (!"".equals(mQueryTextContent)) QueryPkg.setFullTextSearch(mQueryTextContent, this);
        Cursor aCursor = RatstatDatabaseAdapter.fetch("queryPackage", this);
        RatstatApplication.setCursor(aCursor);
        if (cursorRecyclerViewAdapter != null) cursorRecyclerViewAdapter.changeCursor(aCursor);

        mLastSortFieldName = fieldName;
        mLastSortAscendingDescending = ascendingDescending;
    }

    private String getBrewIdsListFromCursor() {
        StringBuilder builder = new StringBuilder();
        Cursor cursor = RatstatDatabaseAdapter.getCursor();
        if (!cursor.moveToFirst()) return ""; // The list was empty
        do {
            int column = cursor.getColumnIndex(StationDbItem.EVENT_BEER_ID);
            builder.append(cursor.getString(column)).append(",");
        } while (cursor.moveToNext());
        Log.v(TAG, "brewIdsList [" + builder.toString() + "]");
        return builder.toString();
    }

    private String getCsvFromViewCursor() {
        StringBuilder builder = new StringBuilder();
        builder.append("\"Name\",\"Style\",\"ABV\",\"Tasted\",\"Flagged\"\n");
        String createdString;
        String highlightedString;
        Cursor cursor = RatstatDatabaseAdapter.getCursor();
        if (!cursor.moveToFirst()) return "The list was empty.....";
        do {
            builder.append("\"").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.BEER_NAME))).append("\",");
            builder.append("\"").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.STYLE_CODE))).append("\",");
            builder.append("\"").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ABV))).append("\",");
            createdString = cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ID));
            builder.append("\"").append((createdString==null)?"":createdString).append("\",");
            highlightedString = cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.BEER_CODE));
            builder.append("\"").append((highlightedString==null)?"":highlightedString).append("\"\n");
        } while (cursor.moveToNext());
        return builder.toString();
    }

    // The emails refuse to be formatted as table with the default Android email stuff
    private android.text.Spanned getHtmlFromViewCursor() {
        StringBuilder builder = new StringBuilder();
        builder.append("<span><table><tr><th>Name</th><th><th>Style</th><th><th>ABV</th><th><th>Tasted</th></tr>").append("\n");
        Cursor cursor = RatstatDatabaseAdapter.getCursor();
        String createdString;
        if (!cursor.moveToFirst()) return Html.fromHtml("The list was empty.....");
        do {
            builder.append("<tr><td>").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.BEER_NAME))).append("</td>");
            builder.append("<td>").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.STYLE_CODE))).append("</td>");
            builder.append("<td>").append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ABV))).append("</td>");
            createdString = cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ID));
            builder.append("<td>").append(createdString==null?"":createdString).append("</td></tr>").append("\n");
        } while (cursor.moveToNext());
        builder.append("</table></span>");
        return Html.fromHtml(builder.toString());
    }

    private android.text.Spanned getTextFromViewCursor() {
        StringBuilder builder = new StringBuilder();
        builder.append("<span>\nName  /  Style  /  ABV  /  Tasted<br>").append("\n");
        Cursor cursor = RatstatDatabaseAdapter.getCursor();
        String createdString;
        String abvString;
        if (!cursor.moveToFirst()) return Html.fromHtml("The list was empty.....");
        do {
            builder.append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.BEER_NAME))).append("  /  ");
            builder.append(cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.STYLE_CODE))).append("  /  ");
            abvString = cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ABV));
            builder.append("0".equals(abvString)?"":abvString).append("  /  ");
            createdString = cursor.getString(RatstatDatabaseAdapter.getOffsetForFieldname(StationDbItem.ID));
            builder.append(createdString==null?"":createdString).append("<br>").append("\n");
        } while (cursor.moveToNext());
        builder.append("</span>");
        return Html.fromHtml(builder.toString());
    }

    private Uri getListContentUri(String csvFromViewCursor) {
        try {
            File tempDir = getApplicationContext().getExternalCacheDir();
            String timeStamp = new SimpleDateFormat("MM-dd-mmss").format(new Date());
            File mTempFile = File.createTempFile("RateStation Beer List-" + timeStamp, ".csv", tempDir);
            FileWriter out = new FileWriter(mTempFile);
            out.write(csvFromViewCursor);
            out.close();
            // DRS 20181201 - Comment 1, Add 1
            // return Uri.fromFile(mTempFile);
            Log.e(TAG, "This would not compile so I cut it out.... BuildConfig.APPLICATION_ID");
            //return FileProvider.getUriForFile(this, "BuildConfig.APPLICATION_ID + .provider", mTempFile);
            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", mTempFile);
        } catch (Throwable t) {
            Log.v(TAG, "Failed to getListContentUri " + t.getMessage());
        }
        return null;
    }


}

