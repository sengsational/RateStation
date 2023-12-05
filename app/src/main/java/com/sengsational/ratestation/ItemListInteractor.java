package com.sengsational.ratestation;


import static com.sengsational.ratestation.FestivalEvent.EVENT_HASH_PREF;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.LaxRedirectStrategy;



/**
 * Created by Dale Seng on 5/30/2016.
 */
public class ItemListInteractor extends AsyncTask<Void, Void, Boolean>  {
    private static final String TAG = ItemListInteractor.class.getSimpleName();
    private CloseableHttpClient nHttpclient = null;
    private BasicCookieStore nCookieStore = null;
    private WebResultListener nListener;
    private DataView nDataView;
    private String nErrorMessage = null;

    public void getStoreListFromWeb(final DataView dataView, final WebResultListener listener) {
            nListener = listener;
            nDataView = dataView;
            this.execute((Void) null);
    }

    @Override
    protected void onPreExecute() {
        Log.v("sengsational", "onPreExecute()..."); //Run order #01
        // set-up a single nHttpclient
        if (nHttpclient != null) {
            Log.e("sengsational", "Attempt to set-up more than one HttpClient!!");
        } else {
            try {
                nCookieStore = new BasicCookieStore();
                HttpClientBuilder clientBuilder = HttpClientBuilder.create();
                nHttpclient = clientBuilder.setRedirectStrategy(new LaxRedirectStrategy()).setDefaultCookieStore(nCookieStore).build();
                nHttpclient.log.enableDebug(true);
                Log.v("sengsational", "nHttpclient object created."); //Run order #02
            } catch (Throwable t) {//
                Log.v("sengsational", "nHttpclient object NOT created. " + t.getMessage());
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                Log.v("sengsational", sw.toString());
                nListener.onError("http client error");
                nErrorMessage = "Problem with the http connection.";
            }
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        // new nHttpclient object each time
        try {
            nHttpclient.close();
            nHttpclient = null;
        } catch (Exception e) {}

        if (success) {
            Log.v("sengsational", "onPostExecute success: " + success);
            nListener.onFinished();
        } else {
            Log.v("sengsational", "onPostExecute fail");
            nListener.onError(nErrorMessage);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (nErrorMessage != null) return false;

        if(!getSiteAccess(RatstatApplication.getDestinationWebDomain())){
            nListener.onError("could not get to the web site");
            nErrorMessage = "Could not reach the web site.";
            return false;
        }

        try {
            do { // This is here to prevent tasted and store lists from updating at the same time.  One will queue behind the other
                Log.v(TAG, "doInBackground is accessing web update lock.");
            } while (!RatstatApplication.getWebUpdateLock(TAG)); // if lock unavailable, this will delay 1/2 second up to 10 seconds, then release.

            nListener.sendStatusToast("Getting list from " + RatstatApplication.getDestinationWebDomain() + "...", Toast.LENGTH_SHORT);
            // Get the currently active beers from the particular store
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(nDataView.getContext());
            String eventHash = prefs.getString(EVENT_HASH_PREF, "Gv7p2eRz");
            String beersWebPage = pullBeersWebPage(RatstatApplication.getDataSourceUrlString(eventHash));
            if(beersWebPage == null){
                nListener.onError("did not get beer list page");
                nErrorMessage = "Did not get the list of items from the site.";
                return false;
            }
            //nListener.sendStatusToast("Got the list.  Loading local table...", Toast.LENGTH_SHORT);

            // Insert the active beers into the database (or just update to active if record exists as a tasted record)
            if (!loadActiveFromSite(beersWebPage)){
                nListener.onError("active beer update error");
                nErrorMessage = "Internal database error...";
                return false;
            }

            nListener.onStoreListSuccess();
            // >>>>>>>>>>Active items now loaded into the database<<<<<<<<<<<<<<<<<
        } catch (Exception e) {
            Log.e("sengsational", LoadDataHelper.getInstance().getStackTraceString(e));
            nErrorMessage = "Exception " + e.getMessage();
            return false;
        } finally {
            RatstatApplication.releaseWebUpdateLock(TAG);
        }
        Log.v(TAG, "Returning " + true + " from doInBackground");
        return true;
    }

    private boolean loadActiveFromSite(String beersWebPage) {
        SQLiteDatabase db = null;
        try {
            RatstatDatabaseAdapter databaseAdapter = new RatstatDatabaseAdapter() ;
            db = databaseAdapter.openDb(nDataView.getContext());                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE

            Log.v("sengsational", "Database was open in SLI.loadActiveFromSite " + db.isOpen());
            int currentCount = getCount(db, RatstatDatabaseAdapter.MAIN_TABLE);
            Log.v("sengsational", "Starting out we had " + currentCount + " records.");

            ////////////////////////////////////////////////////////
            // page contains the active list for the selected store
            ////////////////////////////////////////////////////////
            int updateCount = 0;
            String[] items = beersWebPage.split("\\},\\{");
            if (items.length > 1) {
                Log.v("sengsational", "The active list (including bottled) had " + items.length + " items.");
                for (String string : items) {
                    StationItem modelItem = new StationItem();
                    modelItem.loadFromJson(string);
                    String externalBeerKey = modelItem.getEventBeerId();
                    //Log.v(TAG, "externalBeerKey from external DB is ["  + externalBeerKey +"]");
                    // This could be in the database already, so we need to find out if it is.
                    Cursor cursor = db.query(RatstatDatabaseAdapter.MAIN_TABLE, new String[] {StationDbItem.EVENT_BEER_ID}, StationDbItem.EVENT_BEER_ID + " = ?", new String[] {externalBeerKey}, null, null, null);
                    if (cursor.moveToFirst()) { // true if record found
                        //Log.v(TAG, "record found: " + externalBeerKey);
                        db.update(RatstatDatabaseAdapter.MAIN_TABLE, modelItem.getContentValues(), StationDbItem.EVENT_BEER_ID + " = ?", new String[] {externalBeerKey});
                        updateCount++;
                    } else { // need to insert
                        //Log.v(TAG, "inserting: " + externalBeerKey);
                        db.insert(RatstatDatabaseAdapter.MAIN_TABLE, null, modelItem.getContentValues());
                        currentCount++;

                    }
                    cursor.close();
                    //break; //TODO: REMOVE
                }
                Log.v(TAG, "After loading active, and updating " + updateCount + ", we had " + getCount(db, RatstatDatabaseAdapter.MAIN_TABLE) + " records. That should equal " + currentCount + ".");
            } else {
                Log.v(TAG, "Found nothing in the beersWeb page.");
                nListener.onError("found nothing on the beersweb page");
            }
        } catch (Exception e) {
            Log.e("sengsational", LoadDataHelper.getInstance().getStackTraceString(e));
            nListener.onError("exception in storelistinteractor " + e.getMessage());
            return false;
        } finally {
            try {db.close();} catch (Throwable t){}                               //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CLOSING DATABASE
        }
        return true;
    }

    private String pullBeersWebPage(String dataSourceUrlString) {
        String beersListPage = null;
        try {
            beersListPage = LoadDataHelper.getPageContent(dataSourceUrlString, null, nHttpclient, nCookieStore);        //<<<<<<<<<<<<<<<<PULL STORE'S AVAILABLE LIST
        } catch (Exception e) {
            Log.e("sengsational", "Could not get beersListPage. " + e.getMessage());
        }
        return beersListPage;
    }

    private boolean getSiteAccess(String site) {
        try {
            if (site == null || site.trim().equals("")) {
                return false;
            }
            InetAddress inetAddress = InetAddress.getByName(site);
            Log.v(TAG, "InetAddress " + inetAddress.getHostAddress());
        } catch (Exception e) {
            Log.e(TAG, "Exception on pre-execute getSiteAccess. " + e.getMessage());
            return false;
        }
        return true;
    }

    private int getCount(SQLiteDatabase db, String dbTableName){
        Log.v("sengsational", "getCount() database was open? " + db.isOpen());
        Cursor cursor = db.query(dbTableName, new String[]{"COUNT(*) AS count"}, null, null, null, null, null);
        int count = -1;
        if (cursor.moveToFirst()){
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

}

