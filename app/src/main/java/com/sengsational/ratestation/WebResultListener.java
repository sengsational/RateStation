package com.sengsational.ratestation;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by Dale Seng on 6/1/2016.
 */
public class WebResultListener implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = WebResultListener.class.getSimpleName();
    private final DataView mDataView;
    private AppCompatActivity aActivity;
    private Context mContext;

    public WebResultListener(DataView aView) {
        this.mDataView = aView;
        if (aView instanceof AppCompatActivity) {
            aActivity = (AppCompatActivity)aView;
            mContext = aActivity.getApplicationContext();
        }
    }

    public void onStoreListSuccess() {
        if (aActivity == null || mDataView == null) return;
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.v("sengsational", "WebResultListener.onStoreListSuccess");
                mDataView.showMessage("You've got the current beer list!");
            }
        });
    }

    public void onEventListSuccess(ArrayList<FestivalEvent> festivalEvents) {
        if (aActivity == null || mDataView == null) return;
        String messageToShow = "We found your event!";
        ArrayList<FestivalEvent>  todayEvents = new ArrayList<>();
        for(FestivalEvent festivalEvent :festivalEvents) {
            if (festivalEvent.isToday()) {
                todayEvents.add(festivalEvent);
            }
        }
        // If more than one event, eliminate the test event

        if (todayEvents.size() > 1) {
            for(FestivalEvent festivalEvent :festivalEvents) {
                if (festivalEvent.isToday() && !festivalEvent.isTestEvent()) {
                    todayEvents.add(festivalEvent);
                }
            }
        }

        if (todayEvents.size() != 1) {
            messageToShow = "Didn't get exactly one FestivalEvent from the web: " + todayEvents.size();
            Log.e(TAG, messageToShow)  ;
        } else {
            FestivalEvent discoveredFestivalEvent = festivalEvents.get(0);
            if (aActivity instanceof MainActivity) {
                ((MainActivity)aActivity).saveValidEvent(discoveredFestivalEvent);
            }
        }
        final String fMessageToShow = messageToShow;
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.v("sengsational", "WebResultListener.onEventListSuccess");
                mDataView.showMessage(fMessageToShow);
            }
        });
    }

    public void onOcrScanSuccess(final Intent data) {
        if (aActivity == null || mDataView == null) {
            Log.v(TAG, "WebResultListener.onOcrScanSuccess with activity " + aActivity +  " and view " + mDataView + " not doing anything.");
            return;
        }
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.v(TAG, "WebResultListener.onOcrScanSuccess.");
                //OcrBase ocrBase = (OcrBase)aActivity;
                //Log.v(TAG, "WebResultListener.onOcrScanSuccess..");
                //ocrBase.onActivityResult(OcrBase.OCR_REQUEST, OcrBase.RESULT_OK, data);
            }
        });

    }
    public void onOcrScanProgress(final int completePercent) {
        try {
            aActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Log.v(TAG, "WebResultListener.onOcrScanProgress.");
                    //OcrBase ocrBase = (OcrBase)aActivity;
                    //ocrBase.onActivityProgress(completePercent);
                }
            });
        } catch (Throwable t) {
            Log.v(TAG, "WebResultListener.onOcrScanProgress FAILED: " + t.getLocalizedMessage());
        }
    }

    public void onFinished(String message) {
        if (aActivity == null || mDataView == null) {
            Log.v(TAG, "WebResultListener.onFinished with activity " + aActivity +  " and view " + mDataView + " not doing anything.");
            return;
        }
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.v("sengsational", "WebResultListener.onFinished.");
                mDataView.showProgress(false);
                mDataView.showMessage(message);
                Log.v(TAG, "data view : " + mDataView.getClass().getSimpleName());
                mDataView.navigateToHome();
            }
        });
    }

    // All errors now just go through this one method.  These call methods in the main activity and manage details with the UI elements
    private void handleError(final String errorMessage) {
        if (aActivity == null || mDataView == null) {
            Log.e(TAG, "One or both are null - aActivity: " + aActivity + " mDataView: " + mDataView);
            return;
        }
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.v(TAG, "handleError running " + mDataView.getClass().getSimpleName() +".showMessage(" + errorMessage + ")");
                mDataView.showProgress(false);
                mDataView.showMessage(errorMessage);
                mDataView.setPasswordError(errorMessage);
                mDataView.setUsernameError(errorMessage);
                mDataView.navigateToHome();
            }
        });
    }

    //////////////////////These are the errors that can be thrown by the Interactors (ItemListInteractor and TastedListInteractor)
    public void onError(String message) {
        handleError(message);
    }

    public void sendStatusToast(String message, int toastLength) {
        if (aActivity == null || mDataView == null) return;
        aActivity.runOnUiThread(new Runnable() {
            public void run() {
                mDataView.showMessage(message, toastLength);
            }
        });
    }

    @Override public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mContext == null) {
            Log.v(TAG, "onCreateLoader failed because there was null context.");
        }
        return new CursorLoader(mContext,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), null,

                // Select only cardnoes.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary cardnoes first. Note that there won't be
                // a primary cardno if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

        // **********   Caution: I replaced "ProfileQuery.PROJECTION" above with null. This was after getting rid of the contacts/autocomplete stuff.
    }

    @Override public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    }

    @Override public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}

