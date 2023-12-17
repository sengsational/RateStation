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
import android.widget.ListView;
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

public class RecyclerSqlcListActivity extends AppCompatActivity {
    private static final String TAG = RecyclerSqlcListActivity.class.getSimpleName();

    // Callback related
    public static final int DETAIL_REQUEST = 0;
    private String mLastSortFieldName;
    private String mLastSortAscendingDescending;
    FestivalEvent mFestivalEvent = new FestivalEvent(); // Populated in onCreate(), used in determining valid voting location.

    public MycCursorRecyclerViewAdapter cursorRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "RecyclerSqlcListActivity.onCreate() running.");
        setContentView(R.layout.activity_recycler_list);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            RatstatDatabaseAdapter.open(this);

            Log.v(TAG, "RecyclerSqlcListActivity.onCreate() is pulling records from the database.");

            // This intent carries with it the details of how to query
            Intent intent = getIntent();

            // Get FestivalEvent from extras
            final Object objReceived = ((ObjectWrapper)getIntent().getExtras().getBinder("EVENT_EXTRA")).getData();
            Log.d(TAG, "received object=" + objReceived);
            if (objReceived instanceof FestivalEvent) {
                mFestivalEvent = (FestivalEvent) objReceived;
            }



            String selectionFields = intent.getStringExtra("selectionFields");
            String[] selectionArgs = intent.getStringArrayExtra("selectionArgs");
            final String[] pullFields = intent.getStringArrayExtra("pullFields");
            if (pullFields != null) {
                Log.v(TAG, "RecyclerSqlcListActivity.Creating a cursor with 'fetch'");
                Cursor aCursor = RatstatDatabaseAdapter.fetch("rateList", this);
                RatstatApplication.setCursor(aCursor); // DRS 20161201 - Added 1 - Cursors only in application class, save query package for reQuery
                boolean hasRecords = aCursor.moveToFirst();
                if (aCursor.getCount() != 0) {
                    Log.v(TAG, "RecyclerSqlcListActivity.onCreate() running.  Cursor " + (hasRecords?"has records":"has NO RECORDS"));
                    cursorRecyclerViewAdapter = new MycCursorRecyclerViewAdapter(this, aCursor);
                    cursorRecyclerViewAdapter.hasStableIds();
                    recyclerView.setAdapter(cursorRecyclerViewAdapter);
                    recyclerView.hasFixedSize();
                } else {
                    Log.v(TAG, "RecyclerSqlcListActivity.onCreate() running.  Cursor " + aCursor.getCount() + " records.");
                    manageToasts();
                }
            } else {
                Toast toast = Toast.makeText(this, "No Selection", Toast.LENGTH_SHORT);
                toast.show();
            }

        } catch (Exception e) {
            Log.v(TAG, "Exception in onCreate " + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.v(TAG, "RecyclerSqlcListActivity.onActivityResult running with requestCode " + requestCode + ", resultCode " + resultCode);
        if (requestCode == RecyclerSqlbListActivity.DETAIL_REQUEST) {
            Log.v(TAG, "onActivityResult fired <<<<<<<<<< resultCode:" + resultCode + " CLOSING CURSOR WILL PROBABLY BREAK STUFF");
            // CLOSING CURSOR

            RatstatApplication.getCursor("rateList", getApplicationContext()).close();
            cursorRecyclerViewAdapter.changeCursor(RatstatApplication.getCursor("rateList", getApplicationContext()));  // DRS 20161201 - Added 1 - Cursors only in application class
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
        Log.v("sengsational", "onNewIntent() some other intent");
    }
    // DRS20171019 - Added method
    @Override
    public boolean onSearchRequested() {
        Log.e(TAG, "THIS DOES NOT RUN  - onSearchRequested");
        return super.onSearchRequested();
    }

    private void manageToasts() {
        try {
            Toast toast = Toast.makeText(this, "The list was empty.......", Toast.LENGTH_SHORT);
            toast.show();
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
        Intent intent = new Intent(RecyclerSqlcListActivity.this, BeerSlideActivity.class);
        //listView.getFirstVisiblePosition();
        Log.v("sengsational", "BeerListActivity.onListItemClick() position:" + position + " id:" + id);

        intent.putExtra(BeerSlideActivity.EXTRA_POSITION, (int) position);
        //intent.putExtra(BeerSlideActivity.EXTRA_FIRST_VISIBLE_POSITION, (int) listView.getFirstVisiblePosition());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume() running  <<<<<<<<<<<<<<<<<<<<<< LIST DONE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause() running");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.rate_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.v("sengsational","The item selected: " + item.getTitle() + " id:" + id);
        if (id == R.id.action_submit_votes) {
            final Bundle bundle = new Bundle();
            bundle.putBinder("EVENT_EXTRA", new ObjectWrapper(mFestivalEvent));
            startActivity(new Intent(this, VoteActivity.class).putExtras(bundle));

        } else {
            Log.v("sengsational", "the id: " + id);
        }
        return super.onOptionsItemSelected(item);
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
}

