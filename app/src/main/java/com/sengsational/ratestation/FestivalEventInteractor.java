package com.sengsational.ratestation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;

import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.client.LaxRedirectStrategy;



/**
 * Created by Dale Seng on 5/30/2016.
 */
public class FestivalEventInteractor extends AsyncTask<Void, Void, Boolean>  {
    private static final String TAG = FestivalEventInteractor.class.getSimpleName();
    private CloseableHttpClient nHttpclient = null;
    private BasicCookieStore nCookieStore = null;
    private WebResultListener nListener;
    private DataView nDataView;
    private String nErrorMessage = null;
    ArrayList<FestivalEvent> mFestivalEventList = new ArrayList<>();


    public void getEventDataFromWeb(final DataView dataView, final WebResultListener listener) {
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

            nListener.sendStatusToast("Getting events from " + RatstatApplication.getDestinationWebDomain() + "...", Toast.LENGTH_SHORT);
            // Get the event list data from the web
            String eventListWebPage = getEventListWebPage(RatstatApplication.getEventListUrlString());
            if(eventListWebPage == null){
                nListener.onError("did not get event list page");
                nErrorMessage = "Did not get the list of events from the site.";
                return false;
            }

            // Insert the active beers into the database (or just update to active if record exists as a tasted record)
            if (!processEventListFromSite(eventListWebPage)){
                nListener.onError("event list update error");
                nErrorMessage = "Internal database error trying to proces events...";
                return false;
            }

            // Need to decide which of several events to consider "THE" one
            int eventCount = 0;
            FestivalEvent lastFestivalEvent = null;
            for (FestivalEvent festivalEvent: mFestivalEventList ) {
                if (festivalEvent.isToday()) {
                    lastFestivalEvent = festivalEvent;
                    eventCount++;
                }
            }
            if (eventCount == 0) {
                nListener.onError("Did not get any festival events from the web.");
            } else if (eventCount == 1) {
                nListener.onEventListSuccess(mFestivalEventList);
            } else {
                Log.e(TAG, "Got more than one festival event. NEED TO IMPLEMENT DIALOG FOR WHEN MORE THAN ONE FESTIVAL EVENT");
                nListener.onError("Got more than one festival event!!!  Need to implement decision process.");
            }

            // >>>>>>>>>>FestivalEvent object now populated<<<<<<<<<<<<<<<<<
        } catch (Exception e) {
            Log.e("sengsational", LoadDataHelper.getInstance().getStackTraceString(e));
            nErrorMessage = "Exception " + e.getMessage();
            return false;
        } finally {
            RatstatApplication.releaseWebUpdateLock(TAG);
        }
        Log.v(TAG, "Returning true from doInBackground");
        return true;
    }

    private boolean processEventListFromSite(String eventListWebPage) {
        SQLiteDatabase db = null;
        try {
            RatstatDatabaseAdapter databaseAdapter = new RatstatDatabaseAdapter() ;
            db = databaseAdapter.openDb(nDataView.getContext());                                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<OPENING DATABASE

            Log.v("sengsational", "Database was open in SLI.loadActiveFromSite " + db.isOpen());
            int currentCount = getCount(db, RatstatDatabaseAdapter.MAIN_TABLE);
            Log.v("sengsational", "Starting out we had " + currentCount + " records.");

            ////////////////////////////////////////////////////////
            // page contains the details about each event
            ////////////////////////////////////////////////////////
            String[] items = eventListWebPage.split("\\},\\{");
            if (items.length > 0) {
                Log.v("sengsational", "The event list had " + items.length + " items.");
                for (String string : items) {
                    FestivalEvent festivalEvent = new FestivalEvent();
                    festivalEvent.loadFromJson(string);
                    if (festivalEvent.getEventHash() != null) {
                        mFestivalEventList.add(festivalEvent);
                        String eventHash = festivalEvent.getEventHash();
                        Log.v(TAG, "Created festivalEvent with hash " + festivalEvent.getEventHash() + " and name " + festivalEvent.getEventName());
                    }
                }
                Log.v(TAG, "Ended up with " + mFestivalEventList.size() + " festivalEvent objects.");
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

    private String getEventListWebPage(String dataSourceUrlString) {
        String eventListPage = null;
        try {
            eventListPage = LoadDataHelper.getPageContent(dataSourceUrlString, null, nHttpclient, nCookieStore);        //<<<<<<<<<<<<<<<<PULL STORE'S AVAILABLE LIST
        } catch (Exception e) {
            Log.e(TAG, "Could not get eventListPage. " + e.getMessage());
        }
        return eventListPage;
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

