package com.sengsational.ratestation;

import android.content.Context;
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
public class VoteSubmitInteractor extends AsyncTask<Void, Void, Boolean>  {
    private static final String TAG = VoteSubmitInteractor.class.getSimpleName();
    private CloseableHttpClient nHttpclient = null;
    private BasicCookieStore nCookieStore = null;
    private WebResultListener nListener;
    private DataView nDataView;
    private String nErrorMessage = null;

    private Context nContext;

    public void pushDataToWeb(final DataView dataView, final WebResultListener listener, Context context) {
        nListener = listener;
        nDataView = dataView;
        nContext = context;
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
            nListener.onFinished("Your inputs have been saved to the server.");
        } else {
            Log.v("sengsational", "onPostExecute fail " + nErrorMessage);
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

            nListener.sendStatusToast("Sending your input to " + RatstatApplication.getDestinationWebDomain() + "...", Toast.LENGTH_SHORT);
            // Get the event list data from the web
            String resultComment = pushUserDataToSite(RatstatApplication.getVoteSubmitUrlString(), nContext);
            if(resultComment.contains("ERROR")){
                nListener.onError("Unable to push data to site. " + resultComment);
                nErrorMessage = "Unable to push data to site. " + resultComment;
                return false;
            }

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

    private String pushUserDataToSite(String voteSubmitUrl, Context context) {
        SQLiteDatabase db = null;
        String processStatusMessage = "Nothing done yet.";
        try {
            // Quit if we don't have a legit dataView (unexpected)
            if (nDataView == null) {Log.e(TAG, "StoreListPresenter had null dataView"); throw new Exception("data view was null"); }
            if (nContext == null) {Log.e(TAG, "StoreListPresenter had null context"); throw new Exception("context was null"); }

            nDataView.showProgress(true);
            String userInputJson = RatstatDatabaseAdapter.getUserInputJson(nContext);
            processStatusMessage = "User input JSON from database " + userInputJson.length() + " characters.";
            Log.v(TAG, "###" + userInputJson + "###");
        } catch (Exception e) {
            Log.e("sengsational", LoadDataHelper.getInstance().getStackTraceString(e));
            nListener.onError("exception in VoteSubmitInteractor " + e.getMessage());
            processStatusMessage = "ERROR: Exception - " + e.getClass().getSimpleName() + " " + e.getMessage();
            return processStatusMessage;
        } finally {
            try {db.close();} catch (Throwable t){}                               //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<CLOSING DATABASE
        }
        return processStatusMessage;
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

