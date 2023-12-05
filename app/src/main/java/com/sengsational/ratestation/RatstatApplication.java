package com.sengsational.ratestation;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.concurrent.Semaphore;

/**
 * Created by Dale Seng, November 2023.
 * This app presents a list of beverages to users, and allows the user to rate and comment on the beverages.
 * The list of beverages, as well as the events where the beverages are being made available are
 * supplied by an external source, which is queried on first run.
 */
public class RatstatApplication extends Application {
    private static final String TAG = RatstatApplication.class.getSimpleName();

    public static boolean oldListCheck = true;
    public static boolean reviewUploadWarning = true;
    private static Cursor mCursor;

    private static final Semaphore tutorialLock = new Semaphore(1,true);

    private static String mPresentationMode;
    private static Context mContext;
    private static Boolean mWebUpdateLocked = false;
    private static int denyCount = 0;

    public RatstatApplication() {
        Log.v(TAG, "RatstatApplication constructor.");
    }

    public static String getDestinationWebDomain() {
        return "braddoro.com";
    }

    public static String getDataSourceUrlString(String eventHash) {
        return "https://" + getDestinationWebDomain() + "/event/server/BeerList.php?h=" + eventHash;
    }

    public static String getEventListUrlString() {
        return "https://" + getDestinationWebDomain() + "/event/server/Event.php?active=Y";
    }

    public static void setContext(Context context) {
        mContext = context;
    }
    private static boolean firstTime = true;

    public static boolean checkContext() {
        return mContext != null;
    }

    public static Context getContext() {
        if (mContext == null ) {
            Log.v(TAG, "mContext was null");
        }
        return mContext;
    }

    // Web Update Lock is not really required since background threads are run serially by default by AsyncTask for API 13+
    public static synchronized Boolean getWebUpdateLock(String identifier) {
        if (!mWebUpdateLocked) {        // Not locked
            mWebUpdateLocked = true;    // Got the lock
            Log.v(TAG, "Web update lock granted to " + identifier + ".");
            denyCount = 0;
            return true;
        } else {                        // Was locked
            denyCount++;
            if (denyCount > 20) {        // If something weird keeps happening, let it go without the lock after 10 seconds
                mWebUpdateLocked = true;
                denyCount = 0;
                Log.v(TAG, "Web update granted, but FORCED for " + identifier + ".");
                return true;
            }
            Log.v(TAG, "Web update lock denied for " + identifier + ". Sleeping 1/2 second.");
            try {Thread.sleep(500);} catch (Exception e){}
            return false;
        }
    }

    public static synchronized void releaseWebUpdateLock(String identifier) {
        Log.v(TAG, "Web update lock released by " + identifier);
        mWebUpdateLocked =  false;      // Release the lock
    }

    public static boolean isFirstTime() {
        boolean returnValue = firstTime;
        firstTime = false;
        return returnValue;
    }

    @Override
    public void onTerminate() {
        Log.v(TAG, "onTerminate() has been called.");
        super.onTerminate();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new BjcpHelper(); // static initializer runs
        // DRS 20200924 - Day Night Theme Added
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RatstatApplication.this);
        if (prefs.getBoolean("dark_mode_switch", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Log.v(TAG, "RatstatApplication onCreate.");
    }

    // Get the lock and release after 1 second. Prevents multiple tutorials from starting on multi-loaded fragment.
    public static boolean getTutorialLock() {
        if (!tutorialLock.tryAcquire()) return false;
        else {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tutorialLock.release();
                }
            }, 1000);
            return true;
        }
    }

    public static void setCursor(Cursor cursor) {
        if (mCursor != null) {
            Log.v(TAG, "setCursor is closing existing cursor.");
            mCursor.close();
        }
        mCursor = cursor;
        //if (queryPkg != null) QueryPkg = queryPkg;
    }

    public static Cursor getCursor(Context context) {

        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor;
        } else {
            Log.v(TAG, "ERROR: The cursor was null!!!");
            if (mContext != null) mCursor = reQuery(mContext);
            else if (context != null) mCursor = reQuery(context);
        }
        if (mCursor == null) {
            Log.e(TAG, "ERROR: mCursor was null, despite all efforts to get a good one. Using a generic one.");
            mCursor = RatstatDatabaseAdapter.getCursor();
        }
        return mCursor;
    }

    public static void closeCursor() {
        if (mCursor != null) {
            Log.v(TAG, "closeCursor()");
            mCursor.close();
        }
    }

    public static void addSearchTextToQueryPackage(String searchText, Context context) {
        QueryPkg.setFullTextSearch(searchText, context);
    }

    public static Cursor reQuery(Context context) {
        Cursor aCursor = RatstatDatabaseAdapter.fetch(context);
        setCursor(aCursor);
        return aCursor;
    }

    public static String getPresentationMode() {
        return mPresentationMode;
    }

    public static void setPresentationMode(String presentationMode) {
        mPresentationMode = presentationMode;
    }

    public static void testUniqueDeviceCode() {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        Log.v(TAG, "androidId [" + androidId + "]");
    }

}
