package com.sengsational.ratestation;

import android.util.Log;

/**
 * Created by Dale Seng on 5/30/2016.
 */
public class StoreListPresenter {
    private final String TAG = StoreListPresenter.class.getSimpleName();
    private DataView dataView;
    private WebResultListener webResultListener;

    public StoreListPresenter(DataView dataView) {
        this.dataView = dataView;
        this.webResultListener = new WebResultListener(dataView);
    }

    // Implementations for StoreListPresenter
    public void getStoreList() {
        // Quit if we don't have a legit dataView (unexpected)
        if (dataView == null) {Log.e(TAG, "StoreListPresenter had null dataView"); return; }

        // Run Async Task (does not block)
        new ItemListInteractor().getStoreListFromWeb(dataView, webResultListener);

        // Call show progress in the view object.  WebResultListener will set to showProgress(false).
        dataView.showProgress(true);
    }


}


